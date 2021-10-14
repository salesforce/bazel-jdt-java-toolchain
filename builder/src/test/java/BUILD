load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "simplest",
    srcs = ["example/simplest/application/HelloWorld.java"],
)

java_library(
    name = "simple1",
    srcs = ["example/simple1/application/HelloWithSimpleDependency.java"],
    deps = [":simplest"],
)

filegroup(
    name = "greeting_src_jar",
    srcs = ["greeting.srcjar"],
)

java_library(
    name = "srcjartest",
    srcs = [
        "example/srcjartest/application/HelloWorldSrcJar.java",
        ":greeting_src_jar",
    ],
)
