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

def rules_jdt_toolchains():
    """An utility method to load all Java toolchains.

    It doesn't do anything at the moment.
    """
    pass


