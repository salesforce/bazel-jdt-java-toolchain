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
    
    jvm_maven_import_external(
        name = "org_ow2_asm_asm",
        artifact = "org.ow2.asm:asm:jar:9.3",
        artifact_sha256 = "1263369b59e29c943918de11d6d6152e2ec6085ce63e5710516f8c67d368e4bc",
        licenses = ["notice"],
        server_urls = _DEFAULT_REPOSITORIES,
    )

    jvm_maven_import_external(
        name = "org_ow2_asm_asm_analysis",
        artifact = "org.ow2.asm:asm-analysis:jar:9.3",
        artifact_sha256 = "37fd5392bb2cf4c15f202ffefd46d0e92bb34ff848c549f30d426a60d6b29495",
        deps = ["@org_ow2_asm_asm_tree"],
        server_urls = _DEFAULT_REPOSITORIES,
    )

    jvm_maven_import_external(
        name = "org_ow2_asm_asm_commons",
        artifact = "org.ow2.asm:asm-commons:jar:9.3",
        artifact_sha256 = "a347c24732db2aead106b6e5996a015b06a3ef86e790a4f75b61761f0d2f7f39",
        deps = [
            "@org_ow2_asm_asm",
            "@org_ow2_asm_asm_analysis",
            "@org_ow2_asm_asm_tree",
        ],
        server_urls = _DEFAULT_REPOSITORIES,
    )
    jvm_maven_import_external(
        name = "org_ow2_asm_asm_tree",
        artifact = "org.ow2.asm:asm-tree:jar:9.3",
        artifact_sha256 = "ae629c2609f39681ef8d140a42a23800464a94f2d23e36d8f25cd10d5e4caff4",
        deps = ["@org_ow2_asm_asm"],
        server_urls = _DEFAULT_REPOSITORIES,
    )
    jvm_maven_import_external(
        name = "org_ow2_asm_asm_util",
        artifact = "org.ow2.asm:asm-util:jar:9.3",
        artifact_sha256 = "70f78f291ca0298afdb567fa85c5667869bc3da3914784816413853994962192",
        deps = [
            "@org_ow2_asm_asm",
            "@org_ow2_asm_asm_analysis",
            "@org_ow2_asm_asm_tree",
        ],
        server_urls = _DEFAULT_REPOSITORIES,
    )

def rules_jdt_toolchains():
    """An utility method to load all Java toolchains.

    It doesn't do anything at the moment.
    """
    pass


