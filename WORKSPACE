workspace(name = "rules_jdt")

# --------------------------------------------------------------------------
# Load http_archive
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive", "http_file")

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
# Rules Java (needed to obtain remotejdk_21 for compilation of ECJ)
http_archive(
    name = "rules_java",
    urls = [
        "https://github.com/bazelbuild/rules_java/releases/download/7.3.2/rules_java-7.3.2.tar.gz",
    ],
    sha256 = "3121a00588b1581bd7c1f9b550599629e5adcc11ba9c65f482bbd5cfe47fdf30",
)
load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")
rules_java_dependencies()
rules_java_toolchains()


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
    sha256 = "8c20f74bca25d2d442b327ae26768c02cf3c99e93fad0381f32be9aab1967675",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_pkg/releases/download/0.8.1/rules_pkg-0.8.1.tar.gz",
        "https://github.com/bazelbuild/rules_pkg/releases/download/0.8.1/rules_pkg-0.8.1.tar.gz",
    ],
)

load("@rules_pkg//:deps.bzl", "rules_pkg_dependencies")

rules_pkg_dependencies()

# Needed for making our release notes
load("@rules_pkg//toolchains/git:git_configure.bzl", "experimental_find_system_git")

experimental_find_system_git(
    name = "rules_jdt_git",
    verbose = False,
    workspace_file = "//:WORKSPACE",  # requires exporting in BUILD, see https://github.com/bazelbuild/rules_pkg/issues/439
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
]

jvm_maven_import_external(
    name = "rules_jdt_guava",
    artifact = "com.google.guava:guava:32.1.1-jre",
    artifact_sha256 = "91fbba37f1c8b251cf9ea9e7d3a369eb79eb1e6a5df1d4bbf483dd0380740281",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
    srcjar_sha256 = "5e7b6cebd2e9087a536c1054bf52a2e6a49c284772421f146640cfadc54ba573",
    srcjar_urls = [server + "/com/google/guava/guava/32.1.1-jre/guava-32.1.1-jre-sources.jar" for server in _DEFAULT_REPOSITORIES],
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

# here we need to use the same jacoco/asm version/jars that bazel adds to the runtime classpath
# if the jars are out of sync this error will be reported: NoClassDefFoundError: org/jacoco/agent/rt/internal_{commit-hash}/Offline
# get the matching bazel jacoco jars from https://github.com/bazelbuild/bazel/tree/master/third_party/java/jacoco
http_file(
    name = "bazel_org_jacoco_core_jar",
    downloaded_file_path = "org.jacoco.core.jar",
    urls = [
        "https://github.com/bazelbuild/bazel/raw/ad8ea5aed2c97c4dbbe577fb69adbd51746f6a51/third_party/java/jacoco/org.jacoco.core-0.8.11.jar"
    ],
    sha256 = "fcd188c688473fc8dcc0c6caaf355e7b389502243527c33b9597a3ec28791f47",
)

http_file(
    name = "bazel_org_jacoco_core_srcjar",
    downloaded_file_path = "org.jacoco.core-sources.jar",
    urls = [
        "https://github.com/bazelbuild/bazel/raw/ad8ea5aed2c97c4dbbe577fb69adbd51746f6a51/third_party/java/jacoco/org.jacoco.core-0.8.11-sources.jar"
    ],
    sha256 = "6856d98a837c669f33284df0104130000b12eaf38f04374c6d753eb03c65d93a",
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm",
    artifact = "org.ow2.asm:asm:jar:9.6",
    artifact_sha256 = "3c6fac2424db3d4a853b669f4e3d1d9c3c552235e19a319673f887083c2303a1",
    licenses = ["notice"],
    server_urls = _DEFAULT_REPOSITORIES,
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_analysis",
    artifact = "org.ow2.asm:asm-analysis:jar:9.6",
    artifact_sha256 = "d92832d7c37edc07c60e2559ac6118b31d642e337a6671edcb7ba9fae68edbbb",
    server_urls = _DEFAULT_REPOSITORIES,
    deps = ["@rules_jdt_org_ow2_asm_asm_tree"],
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_commons",
    artifact = "org.ow2.asm:asm-commons:jar:9.6",
    artifact_sha256 = "7aefd0d5c0901701c69f7513feda765fb6be33af2ce7aa17c5781fc87657c511",
    server_urls = _DEFAULT_REPOSITORIES,
    deps = [
        "@rules_jdt_org_ow2_asm_asm",
        "@rules_jdt_org_ow2_asm_asm_analysis",
        "@rules_jdt_org_ow2_asm_asm_tree",
    ],
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_tree",
    artifact = "org.ow2.asm:asm-tree:jar:9.6",
    artifact_sha256 = "c43ecf17b539c777e15da7b5b86553b377e2d39a683de6285567d5283888e7ef",
    server_urls = _DEFAULT_REPOSITORIES,
    deps = ["@rules_jdt_org_ow2_asm_asm"],
)

jvm_maven_import_external(
    name = "rules_jdt_org_ow2_asm_asm_util",
    artifact = "org.ow2.asm:asm-util:jar:9.6",
    artifact_sha256 = "c635a7402f4aa9bf66b2f4230cea62025a0fe1cd63e8729adefc9b1994fac4c3",
    server_urls = _DEFAULT_REPOSITORIES,
    deps = [
        "@rules_jdt_org_ow2_asm_asm",
        "@rules_jdt_org_ow2_asm_asm_analysis",
        "@rules_jdt_org_ow2_asm_asm_tree",
    ],
)
