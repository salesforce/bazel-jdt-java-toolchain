// Copyright 2015 The Bazel Authors. All rights reserved.
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

// Derived from com.google.devtools.build.buildjar.OptionsParser

package com.salesforce.bazel.jdt.toolchain.builder;

// import javax.annotation.Nullable;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class SimpleOptionsParser {

    private static final String ECLIPSE_PREFERENCES_FILE = "-XSFeclipse_prefs";
    private static final String MAX_STD_OUT_ERR_OPTION = "-XSFmax_stdouterr_bytes";
    /** Print extra information */
    private static final String JDT_DEBUG = "-XSFjdt_debug";
    private static final String USE_DIRECT_DEPS_ONLY = "-XSFuse_direct_deps_only";
    // Max length of the JDT command line that is printed out, if enabled.
    private static final int MAX_JDT_COMMAND_LINE_DEBUG = 1500;

    private final List<String> javacOpts = new ArrayList<>();
    private final Set<String> directJars = new LinkedHashSet<>();
    private final List<String> sourceFiles = new ArrayList<>();
    private final List<String> sourceJars = new ArrayList<>();
    private final List<String> classPath = new ArrayList<>();
    private final List<String> bootClassPath = new ArrayList<>();
    private final List<String> processorPath = new ArrayList<>();
//    private final List<String> builtinProcessorNames = new ArrayList<>();
    private final List<String> processorNames = new ArrayList<>();
    private final Map<String, String> customOptions = new HashMap<>();
    private final Optional<Integer> maxStdOutErr = Optional.empty();
    private String outputJar;
    private String generatedSourcesOutputJar = null;
    // @Nullable
    private String nativeHeaderOutput;
    private String outputDepsProtoFile;
    private String manifestProtoPath;
    private String targetLabel;
    private boolean compressJar;

    /**
     * Constructs an {@code SimpleOptionsParser} from a list of command args. Sets the same JavacRunner for
     * both compilation and annotation processing.
     *
     * @param args the list of command line args.
     * @ throws InvalidCommandLineException on any command line error.
     */
    public SimpleOptionsParser(List<String> args) throws InvalidCommandLineException, IOException {
        processCommandlineArgs(expandArguments(args));
    }

    /**
     * Pre-processes an argument list, expanding options @filename to read in the content of the file
     * and add it to the list of arguments.
     *
     * @param args the List of arguments to pre-process.
     * @return the List of pre-processed arguments.
     * @throws java.io.IOException if one of the files containing options cannot be read.
     */
    private static Deque<String> expandArguments(List<String> args) throws IOException {
        Deque<String> expanded = new ArrayDeque<>(args.size());
        for (String arg : args) {
            expandArgument(expanded, arg);
        }
        return expanded;
    }

    /**
     * Expands a single argument, expanding options @filename to read in the content of the file and
     * add it to the list of processed arguments. The @ itself can be escaped with @@.
     *
     * @param expanded the list of processed arguments.
     * @param arg the argument to pre-process.
     * @throws java.io.IOException if one of the files containing options cannot be read.
     */
    private static void expandArgument(Deque<String> expanded, String arg) throws IOException {
        if (arg.startsWith("@@")) {
            expanded.add(arg.substring(1));
        } else if (arg.startsWith("@")) {
            for (String line : Files.readAllLines(Paths.get(arg.substring(1)), UTF_8)) {
                if (line.length() > 0) {
                    expandArgument(expanded, line);
                }
            }
        } else {
            expanded.add(arg);
        }
    }

    /**
     * Collects the arguments for a command line flag until it finds a flag that starts with the
     * terminatorPrefix.
     *
     * @param output where to put the collected flag arguments.
     * @param args
     * @param terminatorPrefix the terminator prefix to stop collecting of argument flags.
     */
    private static void collectFlagArguments(Collection<String> output, Deque<String> args, String terminatorPrefix) {
        for (String arg = args.pollFirst(); arg != null; arg = args.pollFirst()) {
            if (arg.startsWith(terminatorPrefix)) {
                args.addFirst(arg);
                break;
            }
            output.add(arg);
        }
    }

    /**
     * Returns a list of javac options and a map of custom options. Reads options until a terminating {@code "--"} is
     * reached, to support parsing javacopts that start with {@code --} (e.g. --release).
     */
    private static void readCustomJavacopts(List<String> javacopts, Map<String, String> customOptions, Deque<String> argumentDeque) {
        while (!argumentDeque.isEmpty()) {
            String arg = argumentDeque.pollFirst();

            if (arg.startsWith("-XSF")) {
                String[] optionPieces = arg.split("=");
                if (optionPieces.length > 1) {
                    customOptions.put(optionPieces[0], optionPieces[1]);
                } else {
                    customOptions.put(optionPieces[0], optionPieces[0]);
                }
            }

            if (arg.equals("--")) {
                return;
            }
            javacopts.add(arg);
        }
        throw new IllegalArgumentException("javacopts should be terminated by `--`");
    }

    /**
     * Collects the arguments for the --processors command line flag until it finds a flag that starts
     * with the terminatorPrefix.
     *
     * @param output where to put the collected flag arguments.
     * @param args
     * @param terminatorPrefix the terminator prefix to stop collecting of argument flags.
     */
    private static void collectProcessorArguments(List<String> output, Deque<String> args, String terminatorPrefix)
            throws InvalidCommandLineException {
        for (String arg = args.pollFirst(); arg != null; arg = args.pollFirst()) {
            if (arg.startsWith(terminatorPrefix)) {
                args.addFirst(arg);
                break;
            }
            if (arg.contains(",")) {
                throw new InvalidCommandLineException("processor argument may not contain commas: " + arg);
            }
            output.add(arg);
        }
    }

    private static String getArgument(Deque<String> args, String arg) throws InvalidCommandLineException {
        try {
            return args.remove();
        } catch (NoSuchElementException e) {
            throw new InvalidCommandLineException(arg + ": missing argument", e);
        }
    }

    public static int getMaxJdtCommandLineDebug() {
        return MAX_JDT_COMMAND_LINE_DEBUG;
    }

    /**
     * Processes the command line arguments.
     * @ throws InvalidCommandLineException on an invalid option being passed.
     */
    private void processCommandlineArgs(Deque<String> argQueue) throws InvalidCommandLineException {
        for (String arg = argQueue.pollFirst(); arg != null; arg = argQueue.pollFirst()) {
            switch (arg) {
                case "--javacopts":
                    readCustomJavacopts(javacOpts, customOptions, argQueue);
//                    sourcePathFromJavacOpts();
                    break;
                case "--direct_dependencies":
                    collectFlagArguments(directJars, argQueue, "--");
                    break;
                case "--output_deps_proto":
                    outputDepsProtoFile = getArgument(argQueue, arg);
                    break;
                case "--generated_sources_output":
                    generatedSourcesOutputJar = getArgument(argQueue, arg);
                    break;
                case "--output_manifest_proto":
                    manifestProtoPath = getArgument(argQueue, arg);
                    break;
                case "--sources":
                    collectFlagArguments(sourceFiles, argQueue, "-");
                    break;
                case "--source_jars":
                    collectFlagArguments(sourceJars, argQueue, "-");
                    break;
                case "--classpath":
                    collectFlagArguments(classPath, argQueue, "-");
                    break;
                case "--sourcepath":
                    // TODO: Needs investigation on how this should be handled, if necessary
//                    collectFlagArguments(sourcePath, argQueue, "-");
                    break;
                case "--bootclasspath":
                    collectFlagArguments(bootClassPath, argQueue, "-");
                    break;
                case "--processorpath":
                    collectFlagArguments(processorPath, argQueue, "-");
                    break;
                case "--processors":
                    collectProcessorArguments(processorNames, argQueue, "-");
                    break;
                case "--builtin_processors":
                    // TODO: This may be needed for annotation processing
//                    collectProcessorArguments(builtinProcessorNames, argQueue, "-");
                    break;
                case "--output":
                    outputJar = getArgument(argQueue, arg);
                    break;
                case "--native_header_output":
                    nativeHeaderOutput = getArgument(argQueue, arg);
                    break;
                case "--compress_jar":
                    compressJar = true;
                    break;
                case "--target_label":
                    targetLabel = getArgument(argQueue, arg);
                    break;
                default:
                    // FIXME: throw InvalidCommandLineException on unknown option when all options are supported
//                default:
//                    throw new InvalidCommandLineException("unknown option : '" + arg + "'");
            }
        }
    }

    public Optional<String> getEclipsePreferencesFile() {
        if (customOptions.containsKey(ECLIPSE_PREFERENCES_FILE)) {
            return Optional.of(customOptions.get(ECLIPSE_PREFERENCES_FILE));
        }
        return Optional.empty();
    }

    public List<String> getJavacOpts() {
        return javacOpts;
    }

    public Optional<Integer> getMaxStdOutErr() {
        if (customOptions.containsKey(MAX_STD_OUT_ERR_OPTION)) {
            try {
                String option = customOptions.get(MAX_STD_OUT_ERR_OPTION);
                return Optional.of(Integer.parseInt(option));
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("-XSFmax_stdouterr_bytes argument format must be -Xmax_stdouterr_bytes=1234", nfe);
            }
        }
        return Optional.empty();
    }

    public Optional<Boolean> getUseDirectDepsOnly() {
        if (customOptions.containsKey(USE_DIRECT_DEPS_ONLY)) {
            String option = customOptions.get(USE_DIRECT_DEPS_ONLY);
            return Optional.of(Boolean.parseBoolean(option));
        }
        return Optional.empty();
    }

    public Optional<Boolean> getJdtDebug() {
        if (customOptions.containsKey(JDT_DEBUG)) {
            String option = customOptions.get(JDT_DEBUG);
            return Optional.of(Boolean.parseBoolean(option));
        }
        return Optional.empty();
    }

    public Set<String> getDirectJars() {
        return directJars;
    }

    public List<String> getSourceFiles() {
        return sourceFiles;
    }

    public List<String> getSourceJars() {
        return sourceJars;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public List<String> getBootClassPath() {
        return bootClassPath;
    }

    public String getOutputJar() {
        return outputJar;
    }

    public String getGeneratedSourcesOutputJar() {
        return generatedSourcesOutputJar;
    }

    public String getManifestProtoPath() {
        return manifestProtoPath;
    }

    public String getOutputDepsProtoFile() {
        return outputDepsProtoFile;
    }

    public String getTargetLabel() {
        return targetLabel;
    }

    public boolean isCompressJar() {
        return compressJar;
    }

    public List<String> getProcessorPath() {
        return processorPath;
    }

    public List<String> getProcessorNames() {
        return processorNames;
    }

    // @Nullable
    public String getNativeHeaderOutput() {
        return nativeHeaderOutput;
    }
}
