load(
    "@rules_java//toolchains:default_java_toolchain.bzl",
    "BASE_JDK9_JVM_OPTS",
    "DEFAULT_TOOLCHAIN_CONFIGURATION",
    "default_java_toolchain",
)

package(default_visibility = ["//visibility:public"])

licenses(["notice"])

filegroup(
    name = "srcs",
    srcs = glob(["**"]),
    visibility = ["@//:__pkg__"],
)

RELEASES = (11, 17, 21)

[
    default_java_toolchain(
        name = "jdt_toolchain_java%d" % release,
        configuration = DEFAULT_TOOLCHAIN_CONFIGURATION,
        javabuilder = ["//compiler/export:JdtJavaBuilder"],
        jvm_opts = BASE_JDK9_JVM_OPTS + ["-XX:-MaxFDLimit"],
        source_version = "%s" % release,
        target_version = "%s" % release,
    )
    for release in RELEASES
]
