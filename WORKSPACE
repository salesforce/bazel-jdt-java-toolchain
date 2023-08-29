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

#
# note: use jvm_maven_import_external below for dev/test-only dependencies
#       public dependencies must go into jdt/repositories.bzl
#

load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")
_DEFAULT_REPOSITORIES = [
    "https://repo1.maven.org/maven2",
    "https://maven.google.com",
]

jvm_maven_import_external(
    name = "rules_jdt_guava",
    artifact = "com.google.guava:guava:32.1.1-jre",
    artifact_sha256 = "91fbba37f1c8b251cf9ea9e7d3a369eb79eb1e6a5df1d4bbf483dd0380740281",
    srcjar_urls = [server + "/com/google/guava/guava/32.1.1-jre/guava-32.1.1-jre-sources.jar" for server in _DEFAULT_REPOSITORIES],
    srcjar_sha256 = "5e7b6cebd2e9087a536c1054bf52a2e6a49c284772421f146640cfadc54ba573",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_autovalue",
    artifact = "com.google.auto.value:auto-value:1.9",
    artifact_sha256 = "fd39087fa111da2b12b14675fee740043f0e78e4bfc7055cf3443bfffa3f572b",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_autovalue_annotations",
    artifact = "com.google.auto.value:auto-value-annotations:1.9",
    artifact_sha256 = "fa5469f4c44ee598a2d8f033ab0a9dcbc6498a0c5e0c998dfa0c2adf51358044",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_jsr305",
    artifact = "com.google.code.findbugs:jsr305:3.0.2",
    artifact_sha256 = "766ad2a0783f2687962c8ad74ceecc38a28b9f72a2d085ee438b7813e928d0c7",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_caffeine",
    artifact = "com.github.ben-manes.caffeine:caffeine:3.0.5",
    artifact_sha256 = "8a9b54d3506a3b92ee46b217bcee79196b21ca6d52dc2967c686a205fb2f9c15",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_jacoco_core",
    artifact = "org.jacoco:org.jacoco.core:0.8.7",
    artifact_sha256 = "ad7739b5fb5969aa1a8aead3d74ed54dc82ed012f1f10f336bd1b96e71c1a13c",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm",
    artifact = "org.ow2.asm:asm:jar:9.5",
    artifact_sha256 = "b62e84b5980729751b0458c534cf1366f727542bb8d158621335682a460f0353",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_analysis",
    artifact = "org.ow2.asm:asm-analysis:jar:9.5",
    artifact_sha256 = "39f1cf1791335701c3b02cae7b2bc21057ec9a55b2240789cb6d552b2b2c62fa",
    deps = ["@rules_jdt_org_ow2_asm_asm_tree"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_commons",
    artifact = "org.ow2.asm:asm-commons:jar:9.5",
    artifact_sha256 = "72eee9fbafb9de8d9463f20dd584a48ceeb7e5152ad4c987bfbe17dd4811c9ae",
    deps = [
        "@rules_jdt_org_ow2_asm_asm",
        "@rules_jdt_org_ow2_asm_asm_analysis",
        "@rules_jdt_org_ow2_asm_asm_tree",
    ],
    server_urls = _DEFAULT_REPOSITORIES,
)
jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_tree",
    artifact = "org.ow2.asm:asm-tree:jar:9.5",
    artifact_sha256 = "3c33a648191079aeaeaeb7c19a49b153952f9e40fe86fbac5205554ddd9acd94",
    deps = ["@rules_jdt_org_ow2_asm_asm"],
    server_urls = _DEFAULT_REPOSITORIES,
)
jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_util",
    artifact = "org.ow2.asm:asm-util:jar:9.5",
    artifact_sha256 = "70f78f291ca0298afdb567fa85c5667869bc3da3914784816413853994962192",
    deps = [
        "@rules_jdt_org_ow2_asm_asm",
        "@rules_jdt_org_ow2_asm_asm_analysis",
        "@rules_jdt_org_ow2_asm_asm_tree",
    ],
    server_urls = _DEFAULT_REPOSITORIES,
)

