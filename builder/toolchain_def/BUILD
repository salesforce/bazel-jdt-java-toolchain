load(
    "@bazel_tools//tools/jdk:default_java_toolchain.bzl",
    "DEFAULT_JAVACOPTS",
    "default_java_toolchain",
)

package(default_visibility = ["//visibility:public"])

default_java_toolchain(
    name = "jdt_java_toolchain",
    forcibly_disable_header_compilation = True,
    javabuilder = ["//builder/export:JdtJavaToolchainRef"],
    jvm_opts = [],
    misc = DEFAULT_JAVACOPTS + [
        "-XSFmax_stdouterr_bytes=6000000",
    ],
    source_version = "",
    target_version = "",
)
