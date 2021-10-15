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
        artifact = "com.google.guava:guava:30.1-jre",
        artifact_sha256 = "e6dd072f9d3fe02a4600688380bd422bdac184caf6fe2418cfdd0934f09432aa",
        licenses = ["notice"],
        server_urls = _DEFAULT_REPOSITORIES,
    )
    
def rules_jdt_toolchains():
    """An utility method to load all Java toolchains.

    It doesn't do anything at the moment.
    """
    pass


