# Eclipse Java Compiler

We are using a patched version of ECJ 4.23 to incorporate fixes for:

* https://bugs.eclipse.org/bugs/show_bug.cgi?id=574111
  https://git.eclipse.org/r/c/jdt/eclipse.jdt.core/+/181728


## Issue with source/target and JDKs

Note there is an issue in Bazel and JDT 4.23, which seems to require JDK17.
I tried getting it to produce 11 jars but it still failed.

https://github.com/bazelbuild/bazel/issues/6652#issuecomment-1061838734


## Updating

1. Download `ecjsrc-4.??.jar` ECJ source code from https://download.eclipse.org/eclipse/downloads/
2. Extract `org` folder from source jar into this directory.
   Do a proper diff, i.e. delete files no longer part of ECJ!
3. Delete Ant compile adapter (not needed)
4. Re-apply patches listed above.
5. Test and ship
