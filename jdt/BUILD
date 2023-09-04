load(
  "@bazel_tools//tools/jdk:default_java_toolchain.bzl",
  "default_java_toolchain", "DEFAULT_TOOLCHAIN_CONFIGURATION", "BASE_JDK9_JVM_OPTS"
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
        header_compiler = ["//compiler/tools:TurbineDirect"],
        header_compiler_direct = ["//compiler/tools:TurbineDirect"],
        javabuilder = ["//compiler/export:JdtJavaBuilder"],
        java_runtime = "@local_jdk//:jdk",
        jvm_opts = BASE_JDK9_JVM_OPTS + ["-XX:-MaxFDLimit"],
        source_version = "%s" % release,
        target_version = "%s" % release,
    )
    for release in RELEASES
]

RELEASES_EMPTY_CONFIG = (15, 17)
[
    default_java_toolchain(
        name = "jdt_toolchain_java%d" % release,
        configuration = dict(),   # FIXME: why is this empty?
        header_compiler = ["//compiler/tools:TurbineDirect"],
        header_compiler_direct = ["//compiler/tools:TurbineDirect"],
        javabuilder = ["//compiler/export:JdtJavaBuilder"],
        java_runtime = "@local_jdk//:jdk",
        jvm_opts = BASE_JDK9_JVM_OPTS + ["-XX:-MaxFDLimit"],
        source_version = "%s" % release,
        target_version = "%s" % release,
    )
    for release in RELEASES_EMPTY_CONFIG
]
