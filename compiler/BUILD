load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "jdt_java_builder_lib",
    srcs = glob(["src/main/java/**/*.java"]),
    deps = [
        "//compiler/src/main/protobuf:deps_java_proto",
        "//compiler/src/main/protobuf:java_compilation_java_proto",
        "//compiler/src/main/protobuf:worker_protocol_java_proto",
        "@rules_jdt_guava",
        "//compiler/third_party/ecj",
    ],
    visibility = ["//visibility:public"],
)
