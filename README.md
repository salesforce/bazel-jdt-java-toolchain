# jdt-java-toolchain
A Java Toolchain for Bazel that uses JDT for compilation.


## Usage

See [releases](https://github.com/salesforce/bazel-jdt-java-toolchain/releases/) for the release
specific notes and instructions what to add to your `WORKSPACE` file.

Once this is completed, add this to your `.bazelrc`:
```
build --extra_toolchains=@bazel_jdt_java_toolchain//jdt:all
```

By default the `jdt_java_toolchain` is using `local_jdk` for compilation. 
Please create your own `default_java_toolchain` if this doesn't work for your use case.

Have a look at `jdt/BUILD` to see which JDKs are supported.


# JDT Notes

Read [ECJ README](compiler/src/main/ecj/README.md) for details about updating JDT.


## Current Limitations
Currently, there are some issues with clients that try to access use this toolchain
by compiling the builder from source. Many of the Bazel macros depend on the current
java toolchain, and because that toolchain has not been built yet, we run into a circular
dependency.

The solution is for `bazel-jdt-java-toolchain` developers to build the toolchain and then
copy the deploy jar into the repository. Clients can then use the `override_repository` command
and point directly at the source of this repo for development and testing.

Unfortunately this means additional steps are required for developers. This script can be
used to facilitate the steps.
```
#!/bin/bash

bazel build :JdtJavaBuilder_deploy.jar
cp -fv bazel-bin/JdtJavaBuilder_deploy.jar compiler/export/

bazel build //compiler/third_party/turbine:turbine_direct_binary_deploy.jar 
cp -fv bazel-bin/compiler/third_party/turbine/turbine_direct_binary_deploy.jar compiler/tools/
```

For your convinience, the `build-toolchain` script is provided in this repository.


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


