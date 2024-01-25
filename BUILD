load("@rules_java//java:defs.bzl", "java_binary")
load("@bazel_tools//tools/jdk:default_java_toolchain.bzl", "VANILLA_TOOLCHAIN_CONFIGURATION", "default_java_toolchain")

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

# a special toolchain for compiling ECJ with JDK21 but targeting 17
default_java_toolchain(
    name = "java17_toolchain_with_javax21api",
    configuration = VANILLA_TOOLCHAIN_CONFIGURATION,
    java_runtime = "@rules_java//toolchains:remotejdk_21",
    source_version = "17",
    target_version = "17",
)
