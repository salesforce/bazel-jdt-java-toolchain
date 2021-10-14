load("@rules_java//java:defs.bzl", "java_binary")

java_binary(
    name = "JdtJavaBuilder",
    main_class = "com.salesforce.bazel.jdt.toolchain.builder.JdtJavaBuilder",
    visibility = ["//visibility:public"],
    runtime_deps = ["//builder/src/main/java:jdt_java_builder_lib"],
)