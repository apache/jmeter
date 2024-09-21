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

import com.github.vlsi.gradle.dsl.configureEach
import com.github.vlsi.gradle.properties.dsl.props
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("build-logic.build-params")
    id("build-logic.java")
    id("build-logic.test-base")
    id("com.github.autostyle")
    kotlin("jvm")
    kotlin("kapt") apply false
}

val String.v: String get() = rootProject.extra["$this.version"] as String

kotlin {
    // Require explicit access modifiers and require explicit types for public APIs.
    // See https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors
    if (props.bool("kotlin.explicitApi", default = true)) {
        explicitApi()
    }
    jvmToolchain {
        configureToolchain(buildParameters.buildJdk)
    }
}

tasks.configureEach<KotlinCompile> {
    kotlinOptions {
        if (!name.startsWith("compileTest")) {
            apiVersion = "kotlin.api".v
        }
        freeCompilerArgs += "-Xjvm-default=all"
        val jdkRelease = buildParameters.targetJavaVersion.toString()
        freeCompilerArgs += "-Xjdk-release=$jdkRelease"
        kotlinOptions.jvmTarget = jdkRelease
    }
}

if (file("src/main/kotlin").isDirectory) {
    // When main code contains Kotlin, use Dokka instead of Javadoc
    apply(plugin = "build-logic.dokka-javadoc")
}
