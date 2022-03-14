load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "jdt_java_builder_lib",
    srcs = glob(["src/main/java/**/*.java"]),
    deps = [
        ":work_request_handlers",
        "//compiler/src/main/protobuf:deps_java_proto",
        "//compiler/src/main/protobuf:java_compilation_java_proto",
        "//compiler/src/main/protobuf:worker_protocol_java_proto",
        "@com_google_protobuf//:protobuf_java",
        "@rules_jdt_guava",
        "//compiler/third_party/ecj",
    ],
    visibility = ["//visibility:public"],
)

java_library(
    name = "work_request_handlers",
    srcs = glob(["src/main/worker/**/*.java"]),
    deps = [
        "//compiler/src/main/protobuf:worker_protocol_java_proto",
        "@rules_jdt_guava",
        "@com_google_protobuf//:protobuf_java",
        "@com_google_protobuf//:protobuf_java_util",
    ],
)
