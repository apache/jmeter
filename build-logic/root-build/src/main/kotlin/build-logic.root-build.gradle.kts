/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.vlsi.gradle.git.FindGitAttributes
import com.github.vlsi.gradle.git.dsl.gitignore

plugins {
    id("build-logic.build-params")
    id("build-logic.jacoco-aggregate")
    id("build-logic.sonarqube-aggregate")
    id("com.github.vlsi.crlf")
    id("com.github.vlsi.ide")
    id("org.nosphere.apache.rat")
    id("org.jetbrains.gradle.plugin.idea-ext")
    kotlin("jvm") apply false
}

ide {
    copyrightToAsf()
    ideaInstructionsUri =
        uri("https://github.com/apache/jmeter/blob/master/CONTRIBUTING.md#intellij")
    doNotDetectFrameworks("android", "jruby")
}

// This task scans the project for gitignore / gitattributes, and that is reused for building
// source/binary artifacts with the appropriate eol/executable file flags
// It enables to automatically exclude patterns from .gitignore
val gitProps by tasks.registering(FindGitAttributes::class) {
    // Scanning for .gitignore and .gitattributes files in a task avoids doing that
    // when distribution build is not required (e.g. code is just compiled)
    root.set(rootDir)
}

val rat by tasks.getting(org.nosphere.apache.rat.RatTask::class) {
    gitignore(gitProps)
    verbose.set(true)
    // Note: patterns are in non-standard syntax for RAT, so we use exclude(..) instead of excludeFile
    exclude(rootDir.resolve(".ratignore").readLines())
    // Gradle's validation might assume copyLibs and rat operate on the intersecting set of files
    // Technically speaking, that is false positive since rat ignores *.jar files,
    // and copyLibs copies jar files only
    mustRunAfter(":src:dist:copyBinLibs", ":src:dist:copyLibs")
    mustRunAfter(":src:dist-check:copyExtraTestLibs")
}

if (buildParameters.coverage) {
    dependencies {
        subprojects {
            if (path !in listOf(":src", ":src:protocol", ":src:release")) {
                jacocoAggregation(this)
            }
        }
    }
}
