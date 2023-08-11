load("@rules_pkg//:pkg.bzl", "pkg_tar")
load("@rules_pkg//pkg/releasing:defs.bzl", "print_rel_notes")
load("@rules_pkg//pkg/releasing:git.bzl", "git_changelog")

package(
    default_visibility = ["//visibility:private"],
)

# the upcoming release to publish
_version = "0.1.6"

# the last public release on GitHub
_previous_release_tag = "0.1.5"

# Build the artifact to put on the github release page.
pkg_tar(
    name = "rules_jdt-%s" % _version,
    srcs = ["//:distribution"],
    extension = "tar.gz",
    mode = "0444",
    owner = "0.0",
    package_dir = ".",
    strip_prefix = ".",
)

# Generates release notes template
print_rel_notes(
    name = "relnotes",
    outs = ["relnotes.txt"],
    changelog = ":changelog",
    deps_method = "rules_jdt_dependencies",
    org="salesforce",
    repo = "bazel-jdt-java-toolchain",
    setup_file = "jdt:repositories.bzl",
    toolchains_method = "rules_jdt_toolchains",
    artifact_name = "rules_jdt-%s.tar.gz" % _version,
    version = _version,
)

# Builds a Git changelog
# NOTE: a replacement may be available with https://github.com/softprops/action-gh-release and GitHub's automatic release notes generator
git_changelog(
    name = "changelog",
    out = "changelog.txt",
    from_ref = _previous_release_tag,
)

# see https://github.com/bazelbuild/rules_pkg/blob/05d4ebe47cddccb493b3670f9ca39eff2858ac99/pkg/distro/BUILD for other interesting things (eg., stardoc)
