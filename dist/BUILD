load("@rules_pkg//:pkg.bzl", "pkg_tar")
load("@rules_pkg//releasing:defs.bzl", "print_rel_notes")
load("@rules_pkg//releasing:git.bzl", "git_changelog")

package(
    default_visibility = ["//visibility:private"],
)

# Build the artifact to put on the github release page.
pkg_tar(
    name = "rules_jdt",
    srcs = ["//:distribution"],
    extension = "tar.gz",
    mode = "0444",
    owner = "0.0",
    package_dir = ".",
    strip_prefix = ".",
)

# this currently fails because of https://github.com/bazelbuild/rules_pkg/issues/435
print_rel_notes(
    name = "relnotes",
    outs = ["relnotes.txt"],
    changelog = ":changelog",
    deps_method = "rules_jdt_dependencies",
    org="salesforce",
    repo = "bazel-jdt-java-toolchain",
    setup_file = "jdt:repositories.bzl",
    toolchains_method = "rules_jdt_toolchains",
    tarball_name = "rules_jdt.tar.gz",
    version = "latest",
)

# this currently fails because of https://github.com/bazelbuild/rules_pkg/issues/437
git_changelog(
    name = "changelog",
    out = "changelog.txt",
)

# see https://github.com/bazelbuild/rules_pkg/blob/05d4ebe47cddccb493b3670f9ca39eff2858ac99/pkg/distro/BUILD for other interesting things (eg., stardoc)
