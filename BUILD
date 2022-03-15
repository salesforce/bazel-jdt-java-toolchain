load("@rules_java//java:defs.bzl", "java_binary")

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
    main_class = "com.salesforce.bazel.jdt.toolchain.builder.JdtJavaBuilder",
    runtime_deps = ["//compiler:jdt_java_builder_lib"],
)

exports_files(
    ["WORKSPACE"],
    visibility = ["//:__pkg__"],
)
