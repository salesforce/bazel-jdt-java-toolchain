workspace(name = "rules_jdt")

# --------------------------------------------------------------------------
# Load http_archive
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# Load Bazel Skylib
http_archive(
    name = "bazel_skylib",
    sha256 = "1c531376ac7e5a180e0237938a2536de0c54d93f5c278634818e0efc952dd56c",
    urls = [
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.0.3/bazel-skylib-1.0.3.tar.gz",
    ],
)
load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")
bazel_skylib_workspace()

# --------------------------------------------------------------------------
# rules_proto defines abstract rules for building Protocol Buffers.
http_archive(
    name = "rules_proto",
    sha256 = "2490dca4f249b8a9a3ab07bd1ba6eca085aaf8e45a734af92aad0c42d9dc7aaf",
    strip_prefix = "rules_proto-218ffa7dfa5408492dc86c01ee637614f8695c45",
    urls = [
        "https://github.com/bazelbuild/rules_proto/archive/218ffa7dfa5408492dc86c01ee637614f8695c45.tar.gz",
    ],
)
load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")
rules_proto_dependencies()
rules_proto_toolchains()

# --------------------------------------------------------------------------
# rules_pkg dused to produce the release artifacts.
http_archive(
    name = "rules_pkg",
    urls = [
        #"https://github.com/bazelbuild/rules_pkg/releases/download/0.6.0/rules_pkg-0.6.0.tar.gz",
        "https://github.com/bazelbuild/rules_pkg/archive/c74ef150b11633763b127174c897205b729db832.tar.gz",
    ],
    strip_prefix = "rules_pkg-c74ef150b11633763b127174c897205b729db832",
    sha256 = "5ba45978f0dec2b67bb7c6672ba67e4cf5428aaeb4fde8090d7c165c0adbb337",
)
load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")
rules_pkg_dependencies()

# Needed for making our release notes
load("@rules_pkg//toolchains/git:git_configure.bzl", "experimental_find_system_git")
experimental_find_system_git(
    name = "rules_jdt_git",
    workspace_file = "//:WORKSPACE", # requires exporting in BUILD, see https://github.com/bazelbuild/rules_pkg/issues/439
    verbose = False,
)

# --------------------------------------------------------------------------
# Maven Dependencies
load("//jdt:repositories.bzl", "rules_jdt_dependencies")
rules_jdt_dependencies()

# note: use rules_jvm_external and maven_install below for test-only dependencies


