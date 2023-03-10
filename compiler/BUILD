load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "jdt_java_builder_lib",
    srcs = glob(["src/main/java/**/*.java"]),
    deps = [
        ":work_request_handlers",
        ":buildjar",
        ":ecj",
        "//compiler/src/main/protobuf:deps_java_proto",
        "//compiler/src/main/protobuf:java_compilation_java_proto",
        "//compiler/src/main/protobuf:worker_protocol_java_proto",
        "@rules_jdt_guava",
        "@com_google_protobuf//:protobuf_java",
        "@com_google_protobuf//:protobuf_java_util",
    ],
    visibility = ["//visibility:public"],
)

# Bazel's Worker
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

# Bazel's BuildJar
java_library(
    name = "buildjar",
    srcs = glob(["src/main/buildjar/**/*.java"]),
    deps = [
        ":work_request_handlers",
        ":ecj",
        "//compiler/src/main/protobuf:deps_java_proto",
        "//compiler/src/main/protobuf:java_compilation_java_proto",
        "//compiler/src/main/protobuf:worker_protocol_java_proto",
        "//compiler/third_party/auto_value:processor",
        "@rules_jdt_jsr305",
        "@rules_jdt_caffeine",
        "@rules_jdt_guava",
        "@rules_jdt_jacoco_core",
        "@com_google_protobuf//:protobuf_java",
        "@org_ow2_asm_asm",
    ],
)


# Eclipse Java Compiler
java_library(
    name = "ecj",
    srcs = glob(["src/main/ecj/**/*.java"]),
    resources = glob(["src/main/ecj/**"], exclude = ["**/*.java"]),
    resource_strip_prefix = "compiler/src/main/ecj/",
    javacopts = ["-nowarn", "-XepDisableAllChecks"],
    #deps = ["//compiler/third_party/ecj:javax17api"],
)