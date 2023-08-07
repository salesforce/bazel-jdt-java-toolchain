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
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.MoreCollectors.toOptional;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;

import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.build.buildjar.InvalidCommandLineException;
import com.google.devtools.build.buildjar.javac.BlazeJavacResult.Status;
import com.google.devtools.build.buildjar.javac.FormattedDiagnostic.Listener;
import com.google.devtools.build.buildjar.javac.plugins.BlazeJavaCompilerPlugin;
import com.google.devtools.build.buildjar.javac.statistics.BlazeJavacStatistics;

/**
 * BlazeJavacMain adapted to EclipseCompiler tool impl.
 */
public class BlazeEcjToolMain {

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

    List<String> javacArguments = arguments.javacOptions();
    try {
      processPluginArgs(
          arguments.plugins(), arguments.javacOptions(), arguments.blazeJavacOptions());
    } catch (InvalidCommandLineException e) {
      return BlazeJavacResult.error(e.getMessage());
    }

//    Context context = new Context();
//    BlazeJavacStatistics.preRegister(context);
//    CacheFSInfo.preRegister(context);
//    setupBlazeJavaCompiler(arguments.plugins(), context);
    BlazeJavacStatistics.Builder builder = BlazeJavacStatistics.newBuilder();

    Status status = Status.ERROR;
    StringWriter errOutput = new StringWriter();
    // TODO(cushon): where is this used when a diagnostic listener is registered? Consider removing
    // it and handling exceptions directly in callers.
    PrintWriter errWriter = new PrintWriter(errOutput);
    Listener diagnosticsBuilder = new Listener(arguments.failFast());
    JavaCompiler compiler = new EclipseCompiler();

    // Initialize parts of context that the filemanager depends on
//    context.put(DiagnosticListener.class, diagnosticsBuilder);
//    Log.instance(context).setWriters(errWriter);
//    Options.instance(context).put("-Xlint:path", "path");

    try (StandardJavaFileManager fileManager =
        new EclipseFileManager(null, UTF_8)) {

    setLocations(fileManager, arguments);

    Iterable<Path> sourceFiles = arguments.sourceFiles(); // avoid NoSuchMethodError: 'java.lang.Iterable javax.tools.StandardJavaFileManager.getJavaFileObjectsFromPaths(java.util.Collection)'
	CompilationTask task =
    		  compiler
              .getTask(
                  errWriter,
                  fileManager,
                  diagnosticsBuilder,
                  javacArguments,
                  /* classes= */ ImmutableList.of(),
                  fileManager.getJavaFileObjectsFromPaths(sourceFiles));
//      try {
        status = fromResult(task.call());
//      } catch (PropagatedException e) {
//        throw e.getCause();
//      }
    } catch (RuntimeException | IOException | LinkageError | AssertionError t) {
      t.printStackTrace(errWriter);
      status = Status.CRASH;
//    } finally {
//      if (status == Status.OK) {
//        // There could be situations where we incorrectly skip Error Prone and the compilation
//        // ends up succeeding, e.g., if there are errors that are fixed by subsequent round of
//        // annotation processing.  This check ensures that if there were any flow events at all,
//        // then plugins were run.  There may legitimately not be any flow events, e.g. -proc:only
//        // or empty source files.
//        if (compiler.skippedFlowEvents() > 0 && compiler.flowEvents() == 0) {
//          errWriter.println("Expected at least one FLOW event");
//          status = Status.ERROR;
//        }
//      }
    }

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

    // JDT uses getAbsolutePath, which makes reporting of file paths to point into Bazel sandbox
    // this causes issues in IntelliJ
    String canonicalPathPrefix = null;
    try {
		canonicalPathPrefix = detectWorkingDirPathPrefix(arguments);
		if(canonicalPathPrefix != null) {
			output = output.replace(canonicalPathPrefix, "");
		}
    } catch (IOException e) {
		e.printStackTrace(errWriter);
	    errWriter.flush();
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

  private static Status fromResult(Boolean result) {
	if(result == null)
		return Status.CRASH;

	return result.booleanValue() ? Status.OK : Status.ERROR;
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

  private static void setLocations(StandardJavaFileManager fileManager, BlazeJavacArguments arguments) {
    try {
      fileManager.setLocationFromPaths(StandardLocation.CLASS_PATH, arguments.classPath());
      // modular dependencies must be on the module path, not the classpath
      //[ECJ fails if both are set]fileManager.setLocationFromPaths(StandardLocation.MODULE_PATH, arguments.classPath());

      fileManager.setLocationFromPaths(
          StandardLocation.CLASS_OUTPUT, ImmutableList.of(arguments.classOutput()));
      if (arguments.nativeHeaderOutput() != null) {
        fileManager.setLocationFromPaths(
            StandardLocation.NATIVE_HEADER_OUTPUT,
            ImmutableList.of(arguments.nativeHeaderOutput()));
      }

      ImmutableList<Path> sourcePath = arguments.sourcePath();
      if (sourcePath.isEmpty()) {
        // javac expects a module-info-relative source path to be set when compiling modules,
        // otherwise it reports an error:
        // "file should be on source path, or on patch path for module"
        ImmutableList<Path> moduleInfos =
            arguments.sourceFiles().stream()
                .filter(f -> f.getFileName().toString().equals("module-info.java"))
                .collect(toImmutableList());
        if (moduleInfos.size() == 1) {
          sourcePath = ImmutableList.of(getOnlyElement(moduleInfos).getParent());
        }
      }
      fileManager.setLocationFromPaths(StandardLocation.SOURCE_PATH, sourcePath);

      Path system = arguments.system();
      if (system != null) {
        fileManager.setLocationFromPaths(
            StandardLocation.locationFor("SYSTEM_MODULES"), ImmutableList.of(system));
      }
      // The bootclasspath may legitimately be empty if --release is being used.
      Collection<Path> bootClassPath = arguments.bootClassPath();
      if (!bootClassPath.isEmpty()) {
    	//[ECJ fails if both are set]fileManager.setLocationFromPaths(StandardLocation.PLATFORM_CLASS_PATH, bootClassPath);
      }
      fileManager.setLocationFromPaths(
          StandardLocation.ANNOTATION_PROCESSOR_PATH, arguments.processorPath());
      // if release/traget >= JDK 9 then -processorpath will be ignored by JDT but --processor-module-path is expected instead
      // (we set both to let JDT pick)
      fileManager.setLocationFromPaths(
              StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, arguments.processorPath());
      if (arguments.sourceOutput() != null) {
        fileManager.setLocationFromPaths(
            StandardLocation.SOURCE_OUTPUT, ImmutableList.of(arguments.sourceOutput()));
      }
    } catch (IOException e) {
      throw new IOError(e);
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

  private BlazeEcjToolMain() {}
}