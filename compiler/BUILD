load("@rules_java//java:defs.bzl", "java_library")

# Bazel's Worker
java_library(
    name = "work_request_handlers",
    srcs = glob(["src/main/worker/**/*.java"]),
    deps = [
        "//compiler/src/main/protobuf:worker_protocol_java_proto",
        "@com_google_protobuf//:protobuf_java",
        "@com_google_protobuf//:protobuf_java_util",
        "@rules_jdt_guava",
    ],
)

# Bazel's BuildJar

java_import(
    name = "bazel_org_jacoco_core",
    jars = ["@bazel_org_jacoco_core_jar//file"],
    srcjar = "@bazel_org_jacoco_core_srcjar//file",
)

java_library(
    name = "buildjar",
    srcs = glob(["src/main/buildjar/**/*.java"]),
    javacopts = [
        "--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.resources=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    ],
    visibility = ["//:__subpackages__"],
    runtime_deps = [
        "@rules_jdt_org_ow2_asm_asm",
        "@rules_jdt_org_ow2_asm_asm_analysis",
        "@rules_jdt_org_ow2_asm_asm_commons",
        "@rules_jdt_org_ow2_asm_asm_tree",
        "@rules_jdt_org_ow2_asm_asm_util",
    ],
    deps = [
        ":bazel_org_jacoco_core",
        ":ecj",
        ":work_request_handlers",
        "//compiler/src/main/protobuf:deps_java_proto",
        "//compiler/src/main/protobuf:java_compilation_java_proto",
        "//compiler/src/main/protobuf:worker_protocol_java_proto",
        "//compiler/third_party/auto_value:processor",
        "@com_google_protobuf//:protobuf_java",
        "@rules_jdt_caffeine",
        "@rules_jdt_guava",
        "@rules_jdt_jsr305",
    ],
)

# Eclipse Java Compiler
java_library(
    name = "ecj",
    srcs = glob(["src/main/ecj/**/*.java"]),
    javacopts = [
        "-nowarn",
        "-XepDisableAllChecks",
    ],
    resource_strip_prefix = "compiler/src/main/ecj/",
    resources = glob(
        ["src/main/ecj/**"],
        exclude = ["**/*.java"],
    ),
    #deps = ["//compiler/third_party/ecj:javax17api"],
)
