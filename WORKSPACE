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
    artifact = "com.google.guava:guava:31.0.1-jre",
    artifact_sha256 = "d5be94d65e87bd219fb3193ad1517baa55a3b88fc91d21cf735826ab5af087b9",
    srcjar_urls = [server + "/com/google/guava/guava/31.0.1-jre/guava-31.0.1-jre-sources.jar" for server in _DEFAULT_REPOSITORIES],
    srcjar_sha256 = "fc0fb66f315f10b8713fc43354936d3649a8ad63f789d42fd7c3e55ecf72e092",
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
    artifact = "org.ow2.asm:asm:jar:9.3",
    artifact_sha256 = "1263369b59e29c943918de11d6d6152e2ec6085ce63e5710516f8c67d368e4bc",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_analysis",
    artifact = "org.ow2.asm:asm-analysis:jar:9.3",
    artifact_sha256 = "37fd5392bb2cf4c15f202ffefd46d0e92bb34ff848c549f30d426a60d6b29495",
    deps = ["@rules_jdt_org_ow2_asm_asm_tree"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_commons",
    artifact = "org.ow2.asm:asm-commons:jar:9.3",
    artifact_sha256 = "a347c24732db2aead106b6e5996a015b06a3ef86e790a4f75b61761f0d2f7f39",
    deps = [
        "@rules_jdt_org_ow2_asm_asm",
        "@rules_jdt_org_ow2_asm_asm_analysis",
        "@rules_jdt_org_ow2_asm_asm_tree",
    ],
    server_urls = _DEFAULT_REPOSITORIES,
)
jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_tree",
    artifact = "org.ow2.asm:asm-tree:jar:9.3",
    artifact_sha256 = "ae629c2609f39681ef8d140a42a23800464a94f2d23e36d8f25cd10d5e4caff4",
    deps = ["@rules_jdt_org_ow2_asm_asm"],
    server_urls = _DEFAULT_REPOSITORIES,
)
jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_util",
    artifact = "org.ow2.asm:asm-util:jar:9.3",
    artifact_sha256 = "70f78f291ca0298afdb567fa85c5667869bc3da3914784816413853994962192",
    deps = [
        "@rules_jdt_org_ow2_asm_asm",
        "@rules_jdt_org_ow2_asm_asm_analysis",
        "@rules_jdt_org_ow2_asm_asm_tree",
    ],
    server_urls = _DEFAULT_REPOSITORIES,
)
    
