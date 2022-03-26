"""Starlark rules for building Java projects with JDT."""
load(
  "@bazel_tools//tools/jdk:default_java_toolchain.bzl",
  "default_java_toolchain", "DEFAULT_TOOLCHAIN_CONFIGURATION"
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
        header_compiler = [Label("//compiler/tools:TurbineDirect")],
        header_compiler_direct = [Label("//compiler/tools:TurbineDirect")],
        javabuilder = [Label("//compiler/export:JdtJavaBuilder")],
        java_runtime = "@local_jdk//:jdk",
        source_version = "%s" % release,
        target_version = "%s" % release,
        **kwargs
    )
