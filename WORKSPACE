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
    sha256 = "dc3fb206a2cb3441b485eb1e423165b231235a1ea9b031b4433cf7bc1fa460dd",
    strip_prefix = "rules_proto-5.3.0-21.7",
    urls = [
        "https://github.com/bazelbuild/rules_proto/archive/refs/tags/5.3.0-21.7.tar.gz",
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
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/releases/download/0.8.1/rules_pkg-0.8.1.tar.gz",
        "https://github.com/bazelbuild/rules_pkg/releases/download/0.8.1/rules_pkg-0.8.1.tar.gz",
    ],
    sha256 = "8c20f74bca25d2d442b327ae26768c02cf3c99e93fad0381f32be9aab1967675",
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


