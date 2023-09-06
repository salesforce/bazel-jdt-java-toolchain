java_plugin(
    name = "plugin",
    processor_class = "com.google.auto.value.processor.AutoValueProcessor",
    deps = [
        "@rules_jdt_autovalue",
        "@rules_jdt_autovalue_annotations",
        "@rules_jdt_guava",
    ],
)

java_library(
    name = "processor",
    exported_plugins = [
        ":plugin",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "@rules_jdt_autovalue",
        "@rules_jdt_autovalue_annotations",
    ],
)
