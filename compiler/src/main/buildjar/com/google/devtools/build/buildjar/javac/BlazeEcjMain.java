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
import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.tools.Diagnostic;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.batch.ClasspathLocation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.ClasspathAnswer;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.build.buildjar.InvalidCommandLineException;
import com.google.devtools.build.buildjar.javac.BlazeJavacResult.Status;
import com.google.devtools.build.buildjar.javac.FormattedDiagnostic.Listener;
import com.google.devtools.build.buildjar.javac.plugins.BlazeJavaCompilerPlugin;
import com.google.devtools.build.buildjar.javac.plugins.dependency.DependencyModule;
import com.google.devtools.build.buildjar.javac.plugins.dependency.StrictJavaDepsPlugin;
import com.google.devtools.build.buildjar.javac.plugins.processing.AnnotationProcessingModule;
import com.google.devtools.build.buildjar.javac.plugins.processing.AnnotationProcessingPlugin;
import com.google.devtools.build.buildjar.javac.statistics.BlazeJavacStatistics;
import com.google.devtools.build.buildjar.proto.JavaCompilation.CompilationUnit;
import com.google.devtools.build.lib.view.proto.Deps.Dependency;

/**
 * BlazeJavacMain adapted to JDT Compiler
 */
public class BlazeEcjMain {

	static enum UsedDependencyCollectionMode {
		All, DirectOnly, None;

		static UsedDependencyCollectionMode fromOptionValue(String value) {
			if (value != null) {
				switch (value) {
				case "all":
					return All;
				case "direct_only":
					return DirectOnly;
				case "none":
					return None;
				default:
					throw new IllegalArgumentException("Unknown option value for -Xecj_collect_used_deps: " + value);
				}
			}
			return All; // default to all
		}
	}

	static class BlazeEclipseBatchCompiler extends Main {

		final AnnotationProcessingModule processingModule;
		final Path sandboxPathPrefix;
		final Map<Path, Path> sourceFilesByAbsoluteOrCanonicalPath;
		final Set<Path> processedJars = new HashSet<>();
		final DependencyModule dependencyModule;
		final ImmutableSet<Path> directJars;
		final Map<Path, Dependency> directDependenciesMap;
		final Map<Path, Dependency> noneDirectDependenciesMap;
		final UsedDependencyCollectionMode usedDependencyCollectionMode;

		public BlazeEclipseBatchCompiler(PrintWriter outWriter, PrintWriter errWriter,
				ImmutableList<BlazeJavaCompilerPlugin> plugins, String sandboxPathPrefix,
				Map<Path, Path> sourceFilesByAbsoluteOrCanonicalPath,
				UsedDependencyCollectionMode usedDependencyCollectionMode,
				Path problemSeverityPreferences) {
			super(outWriter, errWriter, false /* systemExitWhenFinished */, null /* customDefaultOptions */,
					null /* compilationProgress */);
			this.usedDependencyCollectionMode = usedDependencyCollectionMode;
			this.sandboxPathPrefix = Path.of(sandboxPathPrefix);
			this.sourceFilesByAbsoluteOrCanonicalPath = sourceFilesByAbsoluteOrCanonicalPath;
			this.processingModule = ((AnnotationProcessingPlugin) plugins.stream()
					.filter(AnnotationProcessingPlugin.class::isInstance).findAny().get()).getProcessingModule();
			this.dependencyModule = ((StrictJavaDepsPlugin) plugins.stream()
					.filter(StrictJavaDepsPlugin.class::isInstance).findAny().get()).getDependencyModule();
			this.directJars = dependencyModule.directJars();
			this.directDependenciesMap = dependencyModule.getExplicitDependenciesMap();
			this.noneDirectDependenciesMap = dependencyModule.getImplicitDependenciesMap();

			if(problemSeverityPreferences != null) {
				this.options.putAll(loadProblemSeverityPreferences(problemSeverityPreferences));
			}

			switch (dependencyModule.getStrictJavaDeps()) {
			case ERROR:
				setSeverity(CompilerOptions.OPTION_ReportForbiddenReference, ProblemSeverities.Error, true);
				setSeverity(CompilerOptions.OPTION_ReportDiscouragedReference, ProblemSeverities.Error, true);
				break;

			case WARN:
				setSeverity(CompilerOptions.OPTION_ReportForbiddenReference, ProblemSeverities.Warning, true);
				setSeverity(CompilerOptions.OPTION_ReportDiscouragedReference, ProblemSeverities.Warning, true);
				break;

			case OFF:
				setSeverity(CompilerOptions.OPTION_ReportForbiddenReference, ProblemSeverities.Ignore, true);
				setSeverity(CompilerOptions.OPTION_ReportDiscouragedReference, ProblemSeverities.Ignore, true);
				break;
			}
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
						recordAnnotationProcessingAndPackageInfo(result);
					}
				}
			};
		}

		@Override
		public FileSystem getLibraryAccess() {
			// we use this to collect information about all used dependencies during
			// compilation
			FileSystem nameEnvironment = super.getLibraryAccess();
			nameEnvironment.nameEnvironmentListener = this::recordNameEnvironmentAnswer;
			return nameEnvironment;
		}

		protected void recordAnnotationProcessingAndPackageInfo(CompilationResult result) {
			CompilationUnit.Builder builder = CompilationUnit.newBuilder();

			if (result.getFileName() != null) {
				// paths we get from ECJ are absolute, we have to translate them back to the
				// exec-root relative path
				Path path = Path.of(new String(result.getFileName()));
				if (sourceFilesByAbsoluteOrCanonicalPath.containsKey(path))
					path = sourceFilesByAbsoluteOrCanonicalPath.get(path);
				else
					path = sandboxPathPrefix.relativize(path);

				builder.setPath(processingModule.stripSourceRoot(path).toString());
				builder.setGeneratedByAnnotationProcessor(processingModule.isGenerated(path));
			}

			if (result.packageName != null) {
				String packageName = CharOperation.toString(result.packageName);
				builder.setPkg(packageName);
				dependencyModule.addPackage(packageName);
			}

			if (result.compiledTypes != null) {
				for (Object typename : result.compiledTypes.keySet()) {
					String typeName = new String((char[]) typename);
					int lastSlashPos = typeName.lastIndexOf('/');
					if (lastSlashPos > -1) {
						typeName = typeName.substring(lastSlashPos + 1);
					}
					builder.addTopLevel(typeName.replace('$', '.'));
				}
			}

			processingModule.recordUnit(builder.build());
		}

		protected void recordNameEnvironmentAnswer(ClasspathAnswer classpathAnswer) {
			Classpath classpath = classpathAnswer.source;
			if (classpath instanceof ClasspathLocation) {
				String jar = ((ClasspathLocation) classpath).getPath();
				if (jar != null && jar.endsWith(".jar")) {
					Path jarPath = Path.of(jar);
					if (processedJars.add(jarPath)) {
						// we assume jars come from the execroot; JDT uses absolute/canonical paths
						// therefore we translate the path back into an execroot relative path for Bazel
						// to be happy
						jarPath = sandboxPathPrefix.relativize(jarPath);

						// update the dependency proto
						if (usedDependencyCollectionMode != UsedDependencyCollectionMode.None) {
							if (directJars.contains(jarPath)) {
								if ((usedDependencyCollectionMode == UsedDependencyCollectionMode.All
										|| usedDependencyCollectionMode == UsedDependencyCollectionMode.DirectOnly)
										&& !directDependenciesMap.containsKey(jarPath)) {
									Dependency dep = Dependency.newBuilder()
											// Path.toString uses the platform separator (`\` on Windows) which may not
											// match the format in params files (which currently always use `/`, see
											// bazelbuild/bazel#4108). JavaBuilder should always parse Path strings into
											// java.nio.file.Paths before comparing them.
											.setPath(jarPath.toString()).setKind(Dependency.Kind.EXPLICIT).build();
									directDependenciesMap.put(jarPath, dep);
								}
							} else {
								if (usedDependencyCollectionMode == UsedDependencyCollectionMode.All
										&& !noneDirectDependenciesMap.containsKey(jarPath)) {
									Dependency dep = Dependency.newBuilder()
											// Path.toString uses the platform separator (`\` on Windows) which may not
											// match the format in params files (which currently always use `/`, see
											// bazelbuild/bazel#4108). JavaBuilder should always parse Path strings into
											// java.nio.file.Paths before comparing them.
											.setPath(jarPath.toString()).setKind(Dependency.Kind.IMPLICIT).build();
									noneDirectDependenciesMap.put(jarPath, dep);
								}
							}
						}
					}
				}
			}
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
		// JDT uses getAbsolutePath/getCanonicalPath, which makes reporting of file
		// paths to point into Bazel sandbox
		// this causes issues in IDEs (IntelliJ and others) relying on parsing compiler
		// output to map back errors/warnings
		String sandboxPathPrefix;
		try {
			sandboxPathPrefix = detectWorkingDirPathPrefix(arguments);
		} catch (final IOException e) {
			return BlazeJavacResult.error(e.getMessage());
		}

		try {
			processPluginArgs(arguments.plugins(), arguments.javacOptions(), arguments.blazeJavacOptions());
		} catch (InvalidCommandLineException e) {
			return BlazeJavacResult.error(e.getMessage());
		}

		Map<Path, Path> sourceFilesByAbsoluteOrCanonicalPath = new HashMap<>();
		for (Path sourceFile : arguments.sourceFiles()) {
			sourceFilesByAbsoluteOrCanonicalPath.put(sourceFile.toAbsolutePath(), sourceFile);
			try {
				sourceFilesByAbsoluteOrCanonicalPath.put(sourceFile.toRealPath(), sourceFile);
			} catch (IOException e) {
				return BlazeJavacResult.error(e.toString());
			}
		}

		StringWriter errOutput = new StringWriter();
		PrintWriter errWriter = new PrintWriter(errOutput);

		// note, all -Xecj... are "blaze" specific javac options
		String collectUsedDepsOption = getJavacOptionValue(arguments.blazeJavacOptions(), "-Xecj_collect_used_deps");
		String problemSeverityPreferences = getJavacOptionValue(arguments.blazeJavacOptions(), "-Xecj_problem_severity_preferences");

		BlazeEclipseBatchCompiler compiler = new BlazeEclipseBatchCompiler(errWriter, errWriter, arguments.plugins(),
				sandboxPathPrefix, sourceFilesByAbsoluteOrCanonicalPath, UsedDependencyCollectionMode
						.fromOptionValue(collectUsedDepsOption), problemSeverityPreferences != null ? Path.of(problemSeverityPreferences) : null);

		List<String> ecjArguments = new ArrayList<>();
		setLocations(ecjArguments, arguments, compiler.dependencyModule);
		ecjArguments.addAll(arguments.javacOptions());
//    ecjArguments.add("-referenceInfo"); // easy way to get used dependencies
		arguments.sourceFiles().stream().map(Path::toString).forEach(ecjArguments::add);

//    if(compiler.verbose) {
//    	errWriter.println("ECJ Command Line:");
//    	errWriter.println(ecjArguments.stream().collect(joining(System.lineSeparator())));
//    	errWriter.println();
//    	errWriter.println();
//    }

		boolean compileResult = compiler.compile((String[]) ecjArguments.toArray(new String[ecjArguments.size()]));

		BlazeJavacStatistics.Builder builder = BlazeJavacStatistics.newBuilder();

		Status status = compileResult ? Status.OK : Status.ERROR;
		Listener diagnosticsBuilder = new Listener(arguments.failFast());

		errWriter.flush();
		ImmutableList<FormattedDiagnostic> diagnostics = diagnosticsBuilder.build();

		boolean werror = diagnostics.stream().anyMatch(d -> d.getCode().equals("compiler.err.warnings.and.werror"));
		if (status.equals(Status.OK)) {
			Optional<WerrorCustomOption> maybeWerrorCustom = arguments.blazeJavacOptions().stream()
					.filter(arg -> arg.startsWith("-Werror:")).collect(toOptional()).map(WerrorCustomOption::create);
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

		// JDT uses getAbsolutePath/getCanonicalPath, which makes reporting of file
		// paths to point into Bazel sandbox
		// this causes issues in IDEs (IntelliJ and others) relying on parsing compiler
		// output to map back errors/warnings
		String canonicalPathPrefix;
		try {
			canonicalPathPrefix = detectWorkingDirPathPrefix(arguments);

		} catch (final IOException e) {
			e.printStackTrace(errWriter);
			errWriter.flush();
			return BlazeJavacResult.error(e.getMessage());
		}

		if (canonicalPathPrefix != null) {
			output = output.replace(canonicalPathPrefix, "");
		}
		return BlazeJavacResult.createFullResult(status, filterDiagnostics(werror, diagnostics), output,
				builder.build());
	}

	private static String getJavacOptionValue(List<String> javacOptions, String optionName) {
		for (int i = 0; i < javacOptions.size(); i++) {
			String option = javacOptions.get(i);
			if (option.startsWith(optionName)) {
				int separatorPos = option.indexOf('=');
				if (separatorPos == -1 && javacOptions.size() > i + 1) {
					return javacOptions.get(i + 1);
				} else {
					return option.substring(separatorPos + 1).trim();
				}
			}
		}
		return null;
	}

	static Map<String, String> loadProblemSeverityPreferences(Path compilerPreferencesFile) {
		final Properties properties = new Properties();
		try (InputStream is = new BufferedInputStream(newInputStream(compilerPreferencesFile))) {
			properties.load(is);
		} catch (IOException e) {
			throw new IllegalStateException(format("Error loading problem severity preferences '%s': %s", compilerPreferencesFile, e.getMessage()), e);
		}

		Set<String> warningOptions = Set.of(CompilerOptions.warningOptionNames());
		return properties.entrySet().stream() //
				.filter(e -> warningOptions.contains(e.getKey()))
				.collect(toMap(e -> (String) e.getKey(), e -> ((String) e.getValue()).trim()));
	}

	private static String detectWorkingDirPathPrefix(BlazeJavacArguments arguments) throws IOException {
		// since the JDT compiler is executed from within the sandbox, the absolute path
		// will be resolved to the working directory
		// we simple remove the working directory
		String workDir = System.getProperty("user.dir");
		if (workDir == null)
			throw new IOException("No working directory returned by JVM for property user.dir!");

		if (!workDir.endsWith("/")) {
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
			throw new IOException(
					String.format("Unable to confirm working dir '%s' using file '%s' with absolute path '%s'!",
							workDir, filePath, absoluteFilePath));
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

	private static final ImmutableSet<String> IGNORED_DIAGNOSTIC_CODES = ImmutableSet.of(
			"compiler.note.deprecated.filename", "compiler.note.deprecated.plural",
			"compiler.note.deprecated.recompile", "compiler.note.deprecated.filename.additional",
			"compiler.note.deprecated.plural.additional", "compiler.note.unchecked.filename",
			"compiler.note.unchecked.plural", "compiler.note.unchecked.recompile",
			"compiler.note.unchecked.filename.additional", "compiler.note.unchecked.plural.additional",
			"compiler.warn.sun.proprietary",
			// avoid warning spam when enabling processor options for an entire tree, only a
			// subset
			// of which actually runs the processor
			"compiler.warn.proc.unmatched.processor.options",
			// don't want about v54 class files when running javac9 on JDK 10
			// TODO(cushon): remove after the next javac update
			"compiler.warn.big.major.version",
			// don't want about incompatible processor source versions when running javac9
			// on JDK 10
			// TODO(cushon): remove after the next javac update
			"compiler.warn.proc.processor.incompatible.source.version",
			// https://github.com/bazelbuild/bazel/issues/5985
			"compiler.warn.unknown.enum.constant", "compiler.warn.unknown.enum.constant.reason");

	private static ImmutableList<FormattedDiagnostic> filterDiagnostics(boolean werror,
			ImmutableList<FormattedDiagnostic> diagnostics) {
		return diagnostics.stream().filter(d -> shouldReportDiagnostic(werror, d))
				// Print errors last to make them more visible.
				.sorted(comparing(FormattedDiagnostic::getKind).reversed()).collect(toImmutableList());
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
	static void processPluginArgs(ImmutableList<BlazeJavaCompilerPlugin> plugins,
			ImmutableList<String> standardJavacopts, ImmutableList<String> blazeJavacopts)
			throws InvalidCommandLineException {
		for (BlazeJavaCompilerPlugin plugin : plugins) {
			plugin.processArgs(standardJavacopts, blazeJavacopts);
		}
	}

	private static void setLocations(List<String> ecjArguments, BlazeJavacArguments arguments,
			DependencyModule dependencyModule) {
		if (!arguments.processorPath().isEmpty()) {
			ecjArguments.add("-processorpath");
			ecjArguments.add(arguments.processorPath().stream().map(Path::toString).collect(joining(":")));
			// if release/target >= JDK 9 then -processorpath will be ignored by JDT but
			// --processor-module-path is expected instead
			// (we set both to let JDT pick)
			ecjArguments.add("--processor-module-path");
			ecjArguments.add(arguments.processorPath().stream().map(Path::toString).collect(joining(":")));
		}

		if (!arguments.classPath().isEmpty()) {
			ImmutableSet<Path> directJars = dependencyModule.directJars();
			ecjArguments.add("-classpath");
			ecjArguments.add(arguments.classPath().stream()
					.map(p -> directJars.contains(p) ? p.toString() : format("%s[-**/*]", p.toString()))
					.collect(joining(":")));
		}

		// modular dependencies must be on the module path, not the classpath
		// [ECJ fails if both are
		// set]fileManager.setLocationFromPaths(StandardLocation.MODULE_PATH,
		// arguments.classPath());

//	if(compilerOptions.complianceLevel <= ClassFileConstants.JDK1_8) {
//		if(!arguments.bootClassPath().isEmpty()) {
//			ecjArguments.add("-bootclasspath");
//			ecjArguments.add(arguments.bootClassPath().stream().map(Path::toString).collect(joining(":")));
//		}
//	}

		ecjArguments.add("-d");
		ecjArguments.add(arguments.classOutput().toString());

		if (arguments.sourceOutput() != null) {
			ecjArguments.add("-s");
			ecjArguments.add(arguments.sourceOutput().toString());
		}

		if (!arguments.sourcePath().isEmpty()) {
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
			// In earlier releases, set 'null' as the parent to delegate to the boot class
			// loader.
			return null;
		}
	}

	private BlazeEcjMain() {
	}
}