load("@rules_java//java:defs.bzl", "java_binary")
load("@bazel_tools//tools/jdk:default_java_toolchain.bzl", "default_java_toolchain", "VANILLA_TOOLCHAIN_CONFIGURATION")

licenses(["notice"])

exports_files(["LICENSE"])

filegroup(
    name = "distribution",
    srcs = [
        "BUILD",
        "LICENSE",
        "//compiler/export:srcs",
        "//compiler/tools:srcs",
        "//jdt:srcs",
    ],
    visibility = ["@//dist:__pkg__"],
)

java_binary(
    name = "JdtJavaBuilder",
    main_class = "com.google.devtools.build.buildjar.BazelEcjJavaBuilder",
    runtime_deps = ["//compiler:buildjar"],
)

exports_files(
    ["WORKSPACE"],
    visibility = ["//:__pkg__"],
)

# a special toolchain for compiling ECJ with JDK17 but targeting 11
default_java_toolchain(
    name = "java11_toolchain_with_javax17api",
    configuration = VANILLA_TOOLCHAIN_CONFIGURATION,
    java_runtime = "@bazel_tools//tools/jdk:remotejdk_17",
    source_version = "11",
    target_version = "11",
)
