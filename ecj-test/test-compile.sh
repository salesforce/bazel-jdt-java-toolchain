#!/usr/bin/env bash

set -eu

function announce {
    echo -e "\x1B[1;32mINFO:\x1B[0m $@"
}
function warn {
    echo -e "\x1B[1;33mWARNING: $@ \x1B[0m"
}
function abort {
    echo -e "\x1B[1;31mERROR: $1 ...Aborting!\x1B[0m"
    exit ${2:-1}
}


[ -f "ecj-4.30.jar" ] || {
    abort "please download ecj-4.30.jar into $(pwd)"
}

# JDK 11 as target platform
[ -x "remotejdk_11/bin/java" ] || {
    readonly zulu="zulu11.66.15-ca-jdk11.0.20"
    readonly target="remotejdk_11"

    os=$(uname -s|tr '[A-Z]' '[a-z]')
    [ "$os" == "darwin" ] && os="macosx" # use the correct os

    arch=$(uname -m|tr '[A-Z]' '[a-z]')
    [ "$arch" == "arm64" ] && arch="aarch64" # use the correct architecture
    [ "$arch" == "x86_64" ] && arch="x64" # use the correct architecture

    announce ""
    announce "Downloading JDK 11 for your platform from:"
    announce "  https://cdn.azul.com/zulu/bin/${zulu}-${os}_${arch}.tar.gz"
    announce ""
    announce "And unziping into: ${target} (removing the ${zulu}-${os}_${arch} prefix)"
    announce ""

    curl "https://cdn.azul.com/zulu/bin/${zulu}-${os}_${arch}.tar.gz" -o "${zulu}-${os}_${arch}.tar.gz"
    tar -xvvzf "${zulu}-${os}_${arch}.tar.gz"
    mv "${zulu}-${os}_${arch}" "${target}"

    [ -x "remotejdk_11/bin/java" ] || abort "Download failed!"
}
announce "Using remotejdk_11 as target platform:"
remotejdk_11/bin/java --version

# JDK 21 for ECJ
[ -x "remotejdk_21/bin/java" ] || {
    readonly zulu="zulu21.28.85-ca-jdk21.0.0"
    readonly target="remotejdk_21"

    os=$(uname -s|tr '[A-Z]' '[a-z]')
    [ "$os" == "darwin" ] && os="macosx" # use the correct os

    arch=$(uname -m|tr '[A-Z]' '[a-z]')
    [ "$arch" == "arm64" ] && arch="aarch64" # use the correct architecture
    [ "$arch" == "x86_64" ] && arch="x64" # use the correct architecture

    announce ""
    announce "Downloading JDK 21 for your platform from:"
    announce "  https://cdn.azul.com/zulu/bin/${zulu}-${os}_${arch}.tar.gz"
    announce ""
    announce "And unziping into: ${target} (removing the ${zulu}-${os}_${arch} prefix)"
    announce ""

    curl "https://cdn.azul.com/zulu/bin/${zulu}-${os}_${arch}.tar.gz" -o "${zulu}-${os}_${arch}.tar.gz"
    tar -xvvzf "${zulu}-${os}_${arch}.tar.gz"
    mv "${zulu}-${os}_${arch}" "${target}"

    [ -x "remotejdk_21/bin/java" ] || abort "Download failed!"
}
announce "Using remotejdk_21 for compiler:"
remotejdk_21/bin/java --version


[ -f "remotejdk_11/platformclasses.jar" ] || {
    announce "Extracting bootclasspath from remotejdk_11"
    remotejdk_21/bin/java -jar ecj-4.30.jar -d bin --release 11 src/DumpPlatformClassPath.java
    remotejdk_21/bin/java -cp bin DumpPlatformClassPath remotejdk_11/platformclasses.jar remotejdk_11
}


announce "remotejdk_21/bin/javac @compile.params"
remotejdk_21/bin/javac @compile.params || true

announce "remotejdk_21/bin/javac @compile2.params"
remotejdk_21/bin/javac @compile2.params || true

announce "remotejdk_21/bin/java -jar ecj-4.30.jar @compile.params"
remotejdk_21/bin/java -jar ecj-4.30.jar @compile.params || true

announce "remotejdk_21/bin/java -jar ecj-4.30.jar @compile2.params"
remotejdk_21/bin/java -jar ecj-4.30.jar @compile2.params || true

