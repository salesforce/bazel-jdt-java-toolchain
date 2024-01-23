"""Starlark rules for building Java projects with JDT."""

load(
    "@bazel_tools//tools/jdk:default_java_toolchain.bzl",
    "DEFAULT_TOOLCHAIN_CONFIGURATION",
    "default_java_toolchain",
)

def default_jdt_toolchain(name, release, **kwargs):
    """A convenience macro for creating a default_java_toolchain using ECJ compiler.

    Args:
     name: toolchain name
     release: release version (eg. 11)
     **kwargs: further arguments to pass to default_java_toolchain
    """
    default_java_toolchain(
        name = "%s_jdt_toolchain_java%d" % (name, release),
        configuration = DEFAULT_TOOLCHAIN_CONFIGURATION,
        javabuilder = [Label("//compiler/export:JdtJavaBuilder")],
        source_version = "%s" % release,
        target_version = "%s" % release,
        **kwargs
    )
