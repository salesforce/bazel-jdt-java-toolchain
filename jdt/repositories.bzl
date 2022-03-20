load("@bazel_tools//tools/build_defs/repo:jvm.bzl", "jvm_maven_import_external")

_DEFAULT_REPOSITORIES = [
    "https://repo1.maven.org/maven2",
    "https://maven.google.com",
]

def rules_jdt_dependencies(repositories = _DEFAULT_REPOSITORIES):
    """An utility method to load all dependencies of rules_java.

    Args:
        repositories (array): list of Maven repos

    """
    jvm_maven_import_external(
        name = "rules_jdt_guava",
        artifact = "com.google.guava:guava:31.0.1-jre",
        artifact_sha256 = "d5be94d65e87bd219fb3193ad1517baa55a3b88fc91d21cf735826ab5af087b9",
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
    
def rules_jdt_toolchains():
    """An utility method to load all Java toolchains.

    It doesn't do anything at the moment.
    """
    pass


