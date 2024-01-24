# Eclipse Java Compiler

We are using a patched version of ECJ 4.30 to incorporate fixes for:

* None at this time

As well as additional modification to support:

* Collection of used dependencies during compilation (`FileSystem.nameEnvironmentListener`, see commit history)


## Updating

1. Download `ecjsrc-4.??.jar` ECJ source code from https://download.eclipse.org/eclipse/downloads/
2. Extract `org` folder from source jar into this directory.
   Do a proper diff, i.e. delete files no longer part of ECJ!
3. Delete Ant compile adapter (not needed)
4. Re-apply patches listed above.
5. Test and ship
