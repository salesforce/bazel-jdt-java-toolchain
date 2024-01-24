package(default_visibility = ["//visibility:public"])

load(
    "@bazel_tools//tools/jdk:default_java_toolchain.bzl",
    "PREBUILT_TOOLCHAIN_CONFIGURATION",
    "default_java_toolchain"
)

# define a custom javac toolchain because we want to build with a different set of javacopts
RELEASES = (11, 17, 21)

[
    default_java_toolchain(
        name = "my_verbose_javac_example_%d" % release,
        configuration = PREBUILT_TOOLCHAIN_CONFIGURATION,
        misc = [
            "-Xlint:all",  # turn on warning, which should give us an indicator javac is used,
        ],
        source_version = "%s" % release,
        target_version = "11", # ensure we produce class files for 11 only (so we can test with multiple execution environments)
    )
    for release in RELEASES
]
