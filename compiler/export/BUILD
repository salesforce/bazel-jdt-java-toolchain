package(default_visibility = ["//visibility:public"])

licenses(["notice"])

#
# JdtJavaBuilder_deploy.jar need to be generated and copied during the build
#
#    bazel build :JdtJavaBuilder_deploy
#    cp bazel-bin/JdtJavaBuilder_deploy compiler/export/
#

filegroup(
    name = "JdtJavaBuilder",
    srcs = ["JdtJavaBuilder_deploy.jar"],
)

filegroup(
    name = "srcs",
    srcs = glob(["**"]),
    visibility = ["@//:__pkg__"],
)
