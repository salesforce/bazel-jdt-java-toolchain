package(default_visibility = ["//visibility:public"])

# The simplest example. Compiling a single Java class, with no dependencies.
java_binary(
    name = "Simplest",
    main_class = "example.simplest.application.HelloWorld",
    runtime_deps = ["//java:simplest"],
)

# The first simple example. Compiles a class with a dependency on another package.
java_binary(
    name = "Simple1",
    main_class = "example.simple1.application.HelloWithSimpleDependency",
    runtime_deps = ["//java:simple1"],
)

# Example with a dependency on a source jar (jar file with java sources in it)
java_binary(
    name = "Simple2",
    main_class = "example.srcjartest.application.HelloWorldSrcJar",
    runtime_deps = ["//java:srcjartest"],
)
