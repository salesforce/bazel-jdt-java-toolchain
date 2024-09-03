load(
    "@bazel_tools//tools/jdk:default_java_toolchain.bzl",
    "PREBUILT_TOOLCHAIN_CONFIGURATION",
    "default_java_toolchain",
)

# test toolchains with different source and target versions
RELEASES = [
    (11, 17),
    (17, 21),
    (21, 21),
]

[
    default_java_toolchain(
        name = "my_verbose_jdt_source_target_example%d" % (release[0]) ,
        configuration = PREBUILT_TOOLCHAIN_CONFIGURATION,
        javabuilder = ["@bazel_jdt_java_toolchain//compiler/export:JdtJavaBuilder"],
        source_version = "%d" % release[0],
        target_version = "%d" % release[1],
        misc = [
            "-warn:all",  # turn on warning, which should give us an indicator JDT is used,
            "-%d" % release[1], # set compliance level for JDT compiler to target for supporting lower source
        ],
    )
    for release in RELEASES
]
