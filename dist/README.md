# Package rules_jdt

```
bazel build :relnotes
cat ../bazel-bin/dist/relnotes.txt
tar tzf ../bazel-bin/dist/rules_jdt.tar.gz
```

1. Ensure all builds & tests are green.
2. Tag the release using `version` defined in [//jdt:defs.bzl](../jdt/defs.bzl)
3. Update `version` to next version
4. Commit and push with tags
5. Review draft release created automatically by GitHub action and publish

