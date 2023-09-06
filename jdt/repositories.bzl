_DEFAULT_REPOSITORIES = [
    "https://repo1.maven.org/maven2",
    "https://maven.google.com",
]

def rules_jdt_dependencies(repositories = _DEFAULT_REPOSITORIES):
    """An utility method to load all dependencies of rules_java.

    Args:
        repositories (array): list of Maven repos

    """
    # because JDT Toolchain is provided as deploy jar, there shouldn't be any public external dependencies

def rules_jdt_toolchains():
    """An utility method to load all Java toolchains.

    It doesn't do anything at the moment.
    """
    pass
