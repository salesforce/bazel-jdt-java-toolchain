package(default_visibility = ["//visibility:public"])

licenses(["notice"])

#
# turbine_direct_binary_deploy.jar need to be generated and copied during the build
#
#    bazel build //compiler/third_party/turbine:turbine_direct_binary_deploy.jar
#    cp bazel-bin/compiler/third_party/turbine/turbine_direct_binary_deploy.jar compiler/tools/
#
#

filegroup(
    name = "TurbineDirect",
    srcs = ["turbine_direct_binary_deploy.jar"],
)


filegroup(
    name = "srcs",
    srcs = glob(["**"]),
    visibility = ["@//:__pkg__"],
)
