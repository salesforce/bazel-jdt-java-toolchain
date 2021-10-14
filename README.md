# jdt-java-toolchain
A Java Toolchain for Bazel that uses JDT for compilation.

This project depends on the [bazel-maven-proxy](https://github.com/salesforce/bazel-maven-proxy)

## IntelliJ project
For best results when importing the project into IntelliJ as a Bazel project,
add the following entries into the .ijwb/.bazelproject file:

```
directories:
  # Add the directories you want added as source here. By default, includes entire workspace ('.')
  .
  -bazel-bin
  -bazel-jdt-java-toolchain
  -bazel-out
  -bazel-testlogs
  -builder/src/test/bazel-bin
  -builder/src/test/bazel-out
  -builder/src/test/bazel-test
  -builder/src/test/bazel-testlogs

# Do not automatically includes all relevant targets under the 'directories' above
derive_targets_from_directories: false

targets:
  # If source code isn't resolving, add additional targets that compile it here
  //builder/src/main/java:JdtJavaBuilder_deploy.jar
  # Exclude test/example targets because they exist in a different WORKSPACE
  -//builder/src/test:all

java_language_level: 11
```

## Current Limitations
Currently, there are some issues with clients that try to access use this toolchain
by compiling the builder from source. Many of the Bazel macros depend on the current
java toolchain, and because that toolchain has not been built yet, we run into a circular
dependency.

The temporary solution is for jdt-java-toolchain developers to build the toolchain and then
check in the deploy jar into the repository. Clients can then use the git_repository macro
and point directly at the binary jar as the builder.

Unfortunately this means additional steps are required for developers. This script can be
used to facilitate the steps.
```
#!/bin/bash

bazel build //builder/src/main/java:JdtJavaBuilder_deploy.jar && echo "Deploy Jar built"
rm -f builder/export/JdtJavaBuilder_deploy.jar
cp bazel-out/k8-fastbuild/bin/builder/src/main/java/JdtJavaBuilder_deploy.jar builder/export && echo "Deploy jar copied into repository"
chmod +w builder/export/JdtJavaBuilder_deploy.jar
```

Unfortunately, due to issues with circular dependencies (a client that wants to use this builder, and
tried to build the builder from source)

## Debugging
For debugging the toolchain's Java code (including ECJ compiler) here are a few notes:

Override repository in your project's `.bazelrc` (or via command line) and add debug statements as follows:

```
build --override_repository=jdt_java_toolchain=/Users/.../bazel-jdt-java-toolchain/
common --subcommands=pretty_print
common --verbose_failures
```

This will give output during the build how Bazel is invoking JDT compiler:

```
bazel build //some-java-target:target
INFO: Analyzed target //some-java-target:target (...).
INFO: Found 1 target...
SUBCOMMAND: # //ome-java-target:target [action 'Building some-java-target/libtarget-class.jar ...]
(cd /private/var/tmp/_bazel_username/hash/execroot/core && \
  exec env - \
    LC_CTYPE=en_US.UTF-8 \
    LD_LIBRARY_PATH='' \
    PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
  /Users/username/tools/Darwin/jdk/bin/java \
    -jar \
    external/jdt_java_toolchain/builder/export/JdtJavaBuilder_deploy.jar \
    @bazel-out/darwin-fastbuild/bin/some-java-target/libtarget-class.jar-0.params \
    @bazel-out/darwin-fastbuild/bin/some-java-target/libtarget-class.jar-1.params)
ERROR: /Users/username/app/main/core/some-java-target/BUILD.bazel:4:13: Building some-java-target/libtarget-class.jar ... failed: (Exit 1): java failed: error executing command
  (cd /private/var/tmp/_bazel_username/hash/execroot/core && \
  exec env - \
    LC_CTYPE=en_US.UTF-8 \
    LD_LIBRARY_PATH='' \
    PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
  /Users/username/tools/Darwin/jdk/bin/java -jar external/jdt_java_toolchain/builder/export/JdtJavaBuilder_deploy.jar @bazel-out/darwin-fastbuild/bin/some-java-target/libtarget-class.jar-0.params @bazel-out/darwin-fastbuild/bin/some-java-target/libtarget-class.jar-1.params)
```

Either take the *SUBCOMMAND* or *ERROR* command. 
You can ignore the `exec env` part. 

The interesting two steps are:

```
cd /private/var/tmp/_bazel_username/hash/execroot/core
```

```
/Users/username/tools/Darwin/jdk/bin/java -jar external/jdt_java_toolchain/builder/export/JdtJavaBuilder_deploy.jar @bazel-out/darwin-fastbuild/bin/some-java-target/libtarget-class.jar-0.params @bazel-out/darwin-fastbuild/bin/some-java-target/libtarget-class.jar-1.params
```

The first is the execution directory and the latter the command.
You need to cd into the execution directory and then run the command yourself.
But this time add the remote debug arguments (before `-jar`) as follows:

```
/Users/username/tools/Darwin/jdk/bin/java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000 -jar external/jdt_java_toolchain/builder/export/JdtJavaBuilder_deploy.jar @bazel-out/darwin-fastbuild/bin/some-java-target/libtarget-class.jar-0.params @bazel-out/darwin-fastbuild/bin/some-java-target/libtarget-class.jar-1.params
```

The important part is `suspend=y`.
Without it the compiler would be finished before you are able to connect.

Once you run the command, connect to the waiting process with your IDE's remote debugger.


