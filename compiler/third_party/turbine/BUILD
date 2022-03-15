java_binary(
    name = "turbine_direct_binary",
    main_class = "com.google.turbine.main.Main",
    runtime_deps = [
        "//compiler/src/main/protobuf:deps_java_proto",
        "@rules_jdt_guava",
        ":turbine_direct",
    ],
)

java_import(
    name = "turbine_direct",
    jars = ["turbine_direct.jar"],
    visibility = ["@//:__pkg__"],
)

