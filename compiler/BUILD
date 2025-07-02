load("@rules_java//java:defs.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "java_plugin_artifact")

# Bazel's Worker
java_library(
    name = "work_request_handlers",
    srcs = glob(["src/main/worker/**/*.java"]),
    deps = [
        "//compiler/src/main/protobuf:worker_protocol_java_proto",
        "@protobuf//:protobuf_java",
        "@protobuf//:protobuf_java_util",
        "@maven//:com_google_guava_guava",
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
        "@maven//:org_ow2_asm_asm",
        "@maven//:org_ow2_asm_asm_analysis",
        "@maven//:org_ow2_asm_asm_commons",
        "@maven//:org_ow2_asm_asm_tree",
        "@maven//:org_ow2_asm_asm_util",
    ],
    deps = [
        ":bazel_org_jacoco_core",
        ":work_request_handlers",
        "//compiler/src/main/protobuf:deps_java_proto",
        "//compiler/src/main/protobuf:java_compilation_java_proto",
        "//compiler/src/main/protobuf:worker_protocol_java_proto",
        "@protobuf//:protobuf_java",
        "@maven//:com_google_auto_value_auto_value_annotations",
        "@maven//:com_github_ben_manes_caffeine_caffeine",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:org_eclipse_jdt_ecj",
    ],
    plugins = [
        java_plugin_artifact("com.google.auto.value:auto-value", "com.google.auto.value.processor.AutoValueProcessor"),
    ],
)

