// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.buildjar.javac;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.MoreCollectors.toOptional;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.tools.Diagnostic;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.build.buildjar.InvalidCommandLineException;
import com.google.devtools.build.buildjar.javac.BlazeJavacResult.Status;
import com.google.devtools.build.buildjar.javac.FormattedDiagnostic.Listener;
import com.google.devtools.build.buildjar.javac.plugins.BlazeJavaCompilerPlugin;
import com.google.devtools.build.buildjar.javac.plugins.processing.AnnotationProcessingModule;
import com.google.devtools.build.buildjar.javac.plugins.processing.AnnotationProcessingPlugin;
import com.google.devtools.build.buildjar.javac.statistics.BlazeJavacStatistics;
import com.google.devtools.build.buildjar.proto.JavaCompilation.CompilationUnit;

/**
 * BlazeJavacMain adapted to JDT Compiler
 */
public class BlazeEcjMain {

	static class BlazeEclipseBatchCompiler extends Main {

		private final AnnotationProcessingModule processingModule;
		private Path sandboxPathPrefix;
		private Map<Path, Path> sourceFilesByAbsoluteOrCanonicalPath;

		public BlazeEclipseBatchCompiler(PrintWriter outWriter, PrintWriter errWriter,
				ImmutableList<BlazeJavaCompilerPlugin> plugins, String sandboxPathPrefix, Map<Path, Path> sourceFilesByAbsoluteOrCanonicalPath) {
			super(outWriter, errWriter, false /* systemExitWhenFinished */, null /* customDefaultOptions */,
					null /* compilationProgress */);
			this.sandboxPathPrefix = Path.of(sandboxPathPrefix);
			this.sourceFilesByAbsoluteOrCanonicalPath = sourceFilesByAbsoluteOrCanonicalPath;
			this.processingModule = ((AnnotationProcessingPlugin) plugins.stream()
					.filter(AnnotationProcessingPlugin.class::isInstance).findAny().get()).getProcessingModule();
		}

		@Override
		public ICompilerRequestor getBatchRequestor() {
			ICompilerRequestor delegate = super.getBatchRequestor();
			return new ICompilerRequestor() {
				private final Set<ICompilationUnit> toplevels = new HashSet<>();

				@Override
				public void acceptResult(CompilationResult result) {
					delegate.acceptResult(result);

					ICompilationUnit compilationUnit = result.getCompilationUnit();
					if (compilationUnit != null && toplevels.add(compilationUnit)) {
						recordAnnotationProcessingInfo(result);
					}
				}
			};
		}

		protected void recordAnnotationProcessingInfo(CompilationResult result) {
			CompilationUnit.Builder builder = CompilationUnit.newBuilder();

			if (result.getFileName() != null) {
				// paths we get from ECJ are absolute, we have to translate them back to the exec-root relative path
				Path path = Path.of(new String(result.getFileName()));
				if(sourceFilesByAbsoluteOrCanonicalPath.containsKey(path))
					path = sourceFilesByAbsoluteOrCanonicalPath.get(path);
				else
					path = sandboxPathPrefix.relativize(path);

				builder.setPath(processingModule.stripSourceRoot(path).toString());
				builder.setGeneratedByAnnotationProcessor(processingModule.isGenerated(path));
			}

			if (result.packageName != null) {
				builder.setPkg(CharOperation.toString(result.packageName));
			}

			if (result.compiledTypes != null) {
				for (Object typename : result.compiledTypes.keySet()) {
					String typeName = new String((char[])typename);
					int lastSlashPos = typeName.lastIndexOf('/');
					if(lastSlashPos > -1) {
						typeName = typeName.substring(lastSlashPos+1);
					}
					builder.addTopLevel(typeName.replace('$', '.'));
				}
			}

			processingModule.recordUnit(builder.build());
		}
	}

//  /**
//   * Sets up a BlazeJavaCompiler with the given plugins within the given context.
//   *
//   * @param context JavaCompiler's associated Context
//   */
//  @VisibleForTesting
//  static void setupBlazeJavaCompiler(
//      ImmutableList<BlazeJavaCompilerPlugin> plugins, Context context) {
//    for (BlazeJavaCompilerPlugin plugin : plugins) {
//      plugin.initializeContext(context);
//    }
//    BlazeJavaCompiler.preRegister(context, plugins);
//  }

  public static BlazeJavacResult compile(BlazeJavacArguments arguments) {
    // JDT uses getAbsolutePath/getCanonicalPath, which makes reporting of file paths to point into Bazel sandbox
    // this causes issues in IDEs (IntelliJ and others) relying on parsing compiler output to map back errors/warnings
    String sandboxPathPrefix;
    try {
    	sandboxPathPrefix = detectWorkingDirPathPrefix(arguments);
    } catch (final IOException e) {
	    return BlazeJavacResult.error(e.getMessage());
	}

    try {
      processPluginArgs(
          arguments.plugins(), arguments.javacOptions(), arguments.blazeJavacOptions());
    } catch (InvalidCommandLineException e) {
      return BlazeJavacResult.error(e.getMessage());
    }

    StringWriter errOutput = new StringWriter();
    PrintWriter errWriter = new PrintWriter(errOutput);

    List<String> ecjArguments = new ArrayList<>();
    setLocations(ecjArguments, arguments);
    ecjArguments.addAll(arguments.javacOptions());
    arguments.sourceFiles().stream().map(Path::toString).forEach(ecjArguments::add);

    errWriter.println();
    errWriter.println(ecjArguments.stream().collect(joining(System.lineSeparator())));
    errWriter.println();
    errWriter.println();

    Map<Path, Path> sourceFilesByAbsoluteOrCanonicalPath = new HashMap<>();
    for (Path sourceFile : arguments.sourceFiles()) {
    	sourceFilesByAbsoluteOrCanonicalPath.put(sourceFile.toAbsolutePath(), sourceFile);
    	try {
			sourceFilesByAbsoluteOrCanonicalPath.put(sourceFile.toRealPath(), sourceFile);
		} catch (IOException e) {
			return BlazeJavacResult.error(e.getMessage());
		}
	}

    BlazeEclipseBatchCompiler compiler = new BlazeEclipseBatchCompiler(errWriter, errWriter, arguments.plugins(), sandboxPathPrefix, sourceFilesByAbsoluteOrCanonicalPath);
    boolean compileResult = compiler.compile((String[]) ecjArguments.toArray(new String[ecjArguments.size()]));


    BlazeJavacStatistics.Builder builder = BlazeJavacStatistics.newBuilder();

    Status status =  compileResult ? Status.OK : Status.ERROR;
    Listener diagnosticsBuilder = new Listener(arguments.failFast());


    errWriter.flush();
    ImmutableList<FormattedDiagnostic> diagnostics = diagnosticsBuilder.build();

    boolean werror =
        diagnostics.stream().anyMatch(d -> d.getCode().equals("compiler.err.warnings.and.werror"));
    if (status.equals(Status.OK)) {
      Optional<WerrorCustomOption> maybeWerrorCustom =
          arguments.blazeJavacOptions().stream()
              .filter(arg -> arg.startsWith("-Werror:"))
              .collect(toOptional())
              .map(WerrorCustomOption::create);
      if (maybeWerrorCustom.isPresent()) {
        WerrorCustomOption werrorCustom = maybeWerrorCustom.get();
        if (diagnostics.stream().anyMatch(d -> isWerror(werrorCustom, d))) {
          errOutput.append("error: warnings found and -Werror specified\n");
          status = Status.ERROR;
          werror = true;
        }
      }
    }

    String output = errOutput.toString();

    // JDT uses getAbsolutePath/getCanonicalPath, which makes reporting of file paths to point into Bazel sandbox
    // this causes issues in IDEs (IntelliJ and others) relying on parsing compiler output to map back errors/warnings
    String canonicalPathPrefix;
    try {
    	canonicalPathPrefix = detectWorkingDirPathPrefix(arguments);


    } catch (final IOException e) {
		e.printStackTrace(errWriter);
	    errWriter.flush();
	    return BlazeJavacResult.error(e.getMessage());
	}

    if(canonicalPathPrefix != null) {
    	output = output.replace(canonicalPathPrefix, "");
    }
    return BlazeJavacResult.createFullResult(
        status,
        filterDiagnostics(werror, diagnostics),
        output,
        builder.build());
  }

	private static String detectWorkingDirPathPrefix(BlazeJavacArguments arguments) throws IOException {
		// since the JDT compiler is executed from within the sandbox, the absolute path will be resolved to the working directory
		// we simple remove the working directory
		String workDir = System.getProperty("user.dir");
		if(workDir == null)
			throw new IOException("No working directory returned by JVM for property user.dir!");

		if(!workDir.endsWith("/")) {
			workDir += "/";
		}

		// the following code is only for our own sanity
		Optional<Path> first = arguments.sourcePath().stream().findFirst();
		if (!first.isPresent()) {
			first = arguments.sourceFiles().stream().findFirst();
		}

		String absoluteFilePath = first.get().toAbsolutePath().toString();
		if (!absoluteFilePath.startsWith(workDir)) {
			String filePath = first.get().toString();
			throw new IOException(String.format("Unable to confirm working dir '%s' using file '%s' with absolute path '%s'!", workDir, filePath, absoluteFilePath));
		}

		return workDir;
	}

  private static boolean isWerror(WerrorCustomOption werrorCustom, FormattedDiagnostic diagnostic) {
    switch (diagnostic.getKind()) {
      case WARNING:
      case MANDATORY_WARNING:
        return werrorCustom.isEnabled(diagnostic.getLintCategory());
      default:
        return false;
    }
  }

  private static final ImmutableSet<String> IGNORED_DIAGNOSTIC_CODES =
      ImmutableSet.of(
          "compiler.note.deprecated.filename",
          "compiler.note.deprecated.plural",
          "compiler.note.deprecated.recompile",
          "compiler.note.deprecated.filename.additional",
          "compiler.note.deprecated.plural.additional",
          "compiler.note.unchecked.filename",
          "compiler.note.unchecked.plural",
          "compiler.note.unchecked.recompile",
          "compiler.note.unchecked.filename.additional",
          "compiler.note.unchecked.plural.additional",
          "compiler.warn.sun.proprietary",
          // avoid warning spam when enabling processor options for an entire tree, only a subset
          // of which actually runs the processor
          "compiler.warn.proc.unmatched.processor.options",
          // don't want about v54 class files when running javac9 on JDK 10
          // TODO(cushon): remove after the next javac update
          "compiler.warn.big.major.version",
          // don't want about incompatible processor source versions when running javac9 on JDK 10
          // TODO(cushon): remove after the next javac update
          "compiler.warn.proc.processor.incompatible.source.version",
          // https://github.com/bazelbuild/bazel/issues/5985
          "compiler.warn.unknown.enum.constant",
          "compiler.warn.unknown.enum.constant.reason");

  private static ImmutableList<FormattedDiagnostic> filterDiagnostics(
      boolean werror, ImmutableList<FormattedDiagnostic> diagnostics) {
    return diagnostics.stream()
        .filter(d -> shouldReportDiagnostic(werror, d))
        // Print errors last to make them more visible.
        .sorted(comparing(FormattedDiagnostic::getKind).reversed())
        .collect(toImmutableList());
  }

  private static boolean shouldReportDiagnostic(boolean werror, FormattedDiagnostic diagnostic) {
    if (!IGNORED_DIAGNOSTIC_CODES.contains(diagnostic.getCode())) {
      return true;
    }
    // show compiler.warn.sun.proprietary if we're running with -Werror
    if (werror && diagnostic.getKind() != Diagnostic.Kind.NOTE) {
      return true;
    }
    return false;
  }

  /** Processes Plugin-specific arguments and removes them from the args array. */
  @VisibleForTesting
  static void processPluginArgs(
      ImmutableList<BlazeJavaCompilerPlugin> plugins,
      ImmutableList<String> standardJavacopts,
      ImmutableList<String> blazeJavacopts)
      throws InvalidCommandLineException {
    for (BlazeJavaCompilerPlugin plugin : plugins) {
      plugin.processArgs(standardJavacopts, blazeJavacopts);
    }
  }

  private static void setLocations(List<String> ecjArguments, BlazeJavacArguments arguments) {
	if(!arguments.processorPath().isEmpty()) {
		ecjArguments.add("-processorpath");
		ecjArguments.add(arguments.processorPath().stream().map(Path::toString).collect(joining(":")));
	    // if release/target >= JDK 9 then -processorpath will be ignored by JDT but --processor-module-path is expected instead
	    // (we set both to let JDT pick)
		ecjArguments.add("--processor-module-path");
		ecjArguments.add(arguments.processorPath().stream().map(Path::toString).collect(joining(":")));
	}

	if(!arguments.classPath().isEmpty()) {
		ecjArguments.add("-classpath");
		ecjArguments.add(arguments.classPath().stream().map(Path::toString).collect(joining(":")));
	}

  // modular dependencies must be on the module path, not the classpath
  //[ECJ fails if both are set]fileManager.setLocationFromPaths(StandardLocation.MODULE_PATH, arguments.classPath());

	ecjArguments.add("-d");
	ecjArguments.add(arguments.classOutput().toString());

    if (arguments.sourceOutput() != null) {
    	ecjArguments.add("-s");
    	ecjArguments.add(arguments.sourceOutput().toString());
    }

	if(!arguments.sourcePath().isEmpty()) {
		ecjArguments.add("-sourcepath");
		ecjArguments.add(arguments.sourcePath().stream().map(Path::toString).collect(joining(":")));
	}

	if (arguments.system() != null) {
    	ecjArguments.add("--system");
    	ecjArguments.add(arguments.system().toString());
	}
  }

//  /**
//   * When Bazel invokes JavaBuilder, it puts javac.jar on the bootstrap class path and
//   * JavaBuilder_deploy.jar on the user class path. We need Error Prone to be available on the
//   * annotation processor path, but we want to mask out any other classes to minimize class version
//   * skew.
//   */
//  private static class ClassloaderMaskingFileManager extends EclipseFileManager {
//
//    private final ImmutableSet<String> builtinProcessors;
//
//    public ClassloaderMaskingFileManager(ImmutableSet<String> builtinProcessors) {
//      super(null, UTF_8);
//      this.builtinProcessors = builtinProcessors;
//    }
//
//
//    @Override
//    protected ClassLoader createClassLoader(URL[] urls) {
//      return new URLClassLoader(
//          urls,
//          new ClassLoader(getPlatformClassLoader()) {
//            @Override
//            protected Class<?> findClass(String name) throws ClassNotFoundException {
//              if (name.startsWith("com.google.errorprone.")
//                  || name.startsWith("com.google.common.collect.")
//                  || name.startsWith("com.google.common.base.")
//                  || name.startsWith("com.google.common.graph.")
//                  || name.startsWith("org.checkerframework.shaded.dataflow.")
//                  || name.startsWith("com.sun.source.")
//                  || name.startsWith("com.sun.tools.")
//                  || name.startsWith("com.google.devtools.build.buildjar.javac.statistics.")
//                  || name.startsWith("dagger.model.")
//                  || name.startsWith("dagger.spi.")
//                  || builtinProcessors.contains(name)) {
//                return Class.forName(name);
//              }
//              throw new ClassNotFoundException(name);
//            }
//          });
//    }
//  }

  public static ClassLoader getPlatformClassLoader() {
    try {
      // In JDK 9+, all platform classes are visible to the platform class loader:
      // https://docs.oracle.com/javase/9/docs/api/java/lang/ClassLoader.html#getPlatformClassLoader--
      return (ClassLoader) ClassLoader.class.getMethod("getPlatformClassLoader").invoke(null);
    } catch (ReflectiveOperationException e) {
      // In earlier releases, set 'null' as the parent to delegate to the boot class loader.
      return null;
    }
  }

  private BlazeEcjMain() {}
}