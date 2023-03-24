# Package rules_jdt

## Manually
```
bazel build :relnotes
cat ../bazel-bin/dist/relnotes.txt
tar tzf ../bazel-bin/dist/rules_jdt.tar.gz
```

## Publish Release
1. Ensure all builds & tests are green.
2. Review `_previous_release_tag` in [BUILD](BUILD) matches the last public release on GitHub.
   (Update and commit if necessary.)
3. Tag the release using `_version` defined in [BUILD](BUILD) (`git tag`).
4. Update `_version` to next development version and `_previous_release_tag` to the one you just created.
5. Commit update to [BUILD](BUILD) and push everything with the created tag
6. Wait for GitHub Action [Tagged Release](https://github.com/salesforce/bazel-jdt-java-toolchain/actions/workflows/tagged_release.yml) to create the draft release.
7. Review draft release, tweak description and publish.

