load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "jdt_java_builder_lib",
    srcs = [
        "com/salesforce/bazel/jdt/toolchain/builder/InvalidCommandLineException.java",
        "com/salesforce/bazel/jdt/toolchain/builder/JdtJavaBuilder.java",
        "com/salesforce/bazel/jdt/toolchain/builder/SimpleOptionsParser.java",
    ],
    deps = [
        "//builder/src/main/java/com/salesforce/bazel/jdt/toolchain/builder/jarhelper",
        "//builder/src/main/protobuf:deps_java_proto",
        "//builder/src/main/protobuf:java_compilation_java_proto",
        "//builder/src/main/protobuf:worker_protocol_java_proto",
        "@maven//:com_google_guava_guava",
        "//builder/third_party/ecj",
    ],
    visibility = ["//visibility:public"],
)
