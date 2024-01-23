package(default_visibility = ["//visibility:public"])

load("@bazel_jdt_java_toolchain//jdt:defs.bzl", "default_jdt_toolchain")
load(
    "@bazel_tools//tools/jdk:default_java_toolchain.bzl",
    "PREBUILT_TOOLCHAIN_CONFIGURATION",
)

# The simplest example. Compiling a single Java class, with no dependencies.
java_binary(
    name = "Simplest",
    main_class = "example.simplest.application.HelloWorld",
    runtime_deps = ["//java:simplest"],
)

# The first simple example. Compiles a class with a dependency on another package.
java_binary(
    name = "Simple1",
    main_class = "example.simple1.application.HelloWithSimpleDependency",
    runtime_deps = ["//java:simple1"],
)

# Example with a dependency on a source jar (jar file with java sources in it)
java_binary(
    name = "Simple2",
    main_class = "example.srcjartest.application.HelloWorldSrcJar",
    runtime_deps = ["//java:srcjartest"],
)

# define a custom jdt toolchain because we want to build with a different set of javacopts
RELEASES = (11, 17, 21)

[
    default_jdt_toolchain(
        name = "my_verbose_example",
        configuration = PREBUILT_TOOLCHAIN_CONFIGURATION,
        misc = [
            "-warn:all",  # turn on warning, which should give us an indicator JDT is used,
            "-verbose",
        ],
        release = release,
    )
    for release in RELEASES
]
