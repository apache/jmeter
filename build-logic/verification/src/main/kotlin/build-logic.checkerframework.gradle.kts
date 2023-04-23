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

import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.dependencies

plugins {
    id("org.checkerframework")
}

dependencies {
    providers.gradleProperty("checkerframework.version")
        .takeIf { it.isPresent }
        ?.let {
            val checkerframeworkVersion = it.get()
            "checkerFramework"("org.checkerframework:checker:$checkerframeworkVersion")
            if (JavaVersion.current() == JavaVersion.VERSION_1_8) {
                // only needed for JDK 8
                "checkerFrameworkAnnotatedJDK"("org.checkerframework:jdk8:$checkerframeworkVersion")
            }
        } ?: run {
        val checkerframeworkVersion = "3.31.0"
        "checkerFramework"("org.checkerframework:checker:$checkerframeworkVersion")
        if (JavaVersion.current() == JavaVersion.VERSION_1_8) {
            // only needed for JDK 8
            "checkerFrameworkAnnotatedJDK"("org.checkerframework:jdk8:$checkerframeworkVersion")
        }
    }
}

checkerFramework {
    skipVersionCheck = true
    excludeTests = true
    // See https://checkerframework.org/manual/#introduction
    checkers.add("org.checkerframework.checker.nullness.NullnessChecker")
    checkers.add("org.checkerframework.checker.optional.OptionalChecker")
    // checkers.add("org.checkerframework.checker.index.IndexChecker")
    checkers.add("org.checkerframework.checker.regex.RegexChecker")
    extraJavacArgs.add(
        "-Astubs=" +
            fileTree("$rootDir/config/checkerframework") {
                include("*.astub")
            }.asPath
    )
    // The below produces too many warnings :(
    // extraJavacArgs.add("-Alint=redundantNullComparison")
}
