# Package rules_jdt

```
bazel build :relnotes
cat ../bazel-bin/dist/relnotes.txt
tar tzf ../bazel-bin/dist/rules_jdt.tar.gz
```

1. Ensure all builds & tests are green.
2. Review `_previous_release_tag` is the last public release on GitHub.
   Update and commit if necessary.
3. Tag the release using `_version` defined in [BUILD](BUILD)
4. Update `_version` to next development version
5. Commit and push with tags
6. Review draft release created automatically by GitHub action and publish

