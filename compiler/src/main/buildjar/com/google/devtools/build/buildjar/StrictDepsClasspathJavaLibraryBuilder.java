// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.buildjar;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.io.IOException;
import java.nio.file.Path;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.buildjar.javac.BlazeJavacResult;
import com.google.devtools.build.buildjar.javac.JavacRunner;
import com.google.devtools.build.buildjar.javac.statistics.BlazeJavacStatistics;

/**
 * A variant of SimpleJavaLibraryBuilder that attempts to reduce the
 * compile-time classpath to the direct dependencies only. This mode is enabled
 * via the <code>-Xecj_use_direct_deps_only</code> flag.
 */
public class StrictDepsClasspathJavaLibraryBuilder extends SimpleJavaLibraryBuilder {

	/**
	 * Attempts to minimize the compile-time classpath before invoking javac,
	 * falling back to a regular compile.
	 *
	 * @param build A JavaLibraryBuildRequest request object describing what to
	 *              compile
	 * @throws IOException clean-up up the output directory fails
	 */
	@Override
	BlazeJavacResult compileSources(JavaLibraryBuildRequest build, JavacRunner javacRunner) throws IOException {
		if(build.getDependencyModule().directJars().isEmpty()) {
			// in this case the direct jars list is empty
			return BlazeJavacResult.error("No direct jars information supplied by Bazel. Please adjust Bazel settings to allow submission of direct dependency information (e.g., don't use --strict_java_deps=off).");
		}

		ImmutableList<Path> compressedClasspath = build.getDependencyModule().directJars().stream()
				.collect(toImmutableList());

		BlazeJavacResult result = javacRunner.invokeJavac(build.toBlazeJavacArguments(compressedClasspath));

		BlazeJavacStatistics.Builder stats = result.statistics().toBuilder()
				.minClasspathLength(build.getDependencyModule().getImplicitDependenciesMap().size());
		build.getProcessors().stream().map(p -> p.substring(p.lastIndexOf('.') + 1))
				.forEachOrdered(stats::addProcessor);

		return result.withStatistics(stats.build());
	}
}