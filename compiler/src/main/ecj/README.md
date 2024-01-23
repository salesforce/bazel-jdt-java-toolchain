# Eclipse Java Compiler

We are using a patched version of ECJ 4.30 to incorporate fixes for:

```
* Bugzilla [533199](https://bugs.eclipse.org/bugs/show_bug.cgi?id=533199)
  - Reverting part of commit [Bug 552082 - Fix the applicability of a no-@target annotation type](https://github.com/eclipse-jdt/eclipse.jdt.core/commit/c07bc1c3061d9d8cee7ea123d74e67f097c7ad56)
  - [JDK specification discussion](https://mail.openjdk.org/pipermail/compiler-dev/2019-September/013705.html)
```

As well as additional modification to support:

* Collection of used dependencies during compilation (`FileSystem.nameEnvironmentListener`, see commit history)


## Updating

1. Download `ecjsrc-4.??.jar` ECJ source code from https://download.eclipse.org/eclipse/downloads/
2. Extract `org` folder from source jar into this directory.
   Do a proper diff, i.e. delete files no longer part of ECJ!
3. Delete Ant compile adapter (not needed)
4. Re-apply patches listed above.
5. Test and ship
