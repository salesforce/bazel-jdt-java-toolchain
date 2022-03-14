load(
  "@bazel_tools//tools/jdk:default_java_toolchain.bzl",
  "default_java_toolchain", "DEFAULT_TOOLCHAIN_CONFIGURATION"
)
   
package(default_visibility = ["//visibility:public"])

licenses(["notice"])

filegroup(
    name = "srcs",
    srcs = glob(["**"]),
    visibility = ["@//:__pkg__"],
)

RELEASES = (8, 9, 10, 11)
[
    default_java_toolchain(
        name = "jdt_toolchain_java%d" % release,
        configuration = DEFAULT_TOOLCHAIN_CONFIGURATION,
        javabuilder = ["//compiler/export:JdtJavaBuilder"],
        java_runtime = "@local_jdk//:jdk",
        source_version = "%s" % release,
        target_version = "%s" % release,
    )
    for release in RELEASES
]

RELEASES_EMPTY_CONFIG = (15, 17)
[
    default_java_toolchain(
        name = "jdt_toolchain_java%d" % release,
        configuration = dict(),
        javabuilder = ["//compiler/export:JdtJavaBuilder"],
        java_runtime = "@local_jdk//:jdk",
        source_version = "%s" % release,
        target_version = "%s" % release,
    )
    for release in RELEASES_EMPTY_CONFIG
]
