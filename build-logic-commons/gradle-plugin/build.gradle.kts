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

import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion

plugins {
    `kotlin-dsl`
}

group = "org.apache.jmeter.build-logic"

dependencies {
    // We use precompiled script plugins (== plugins written as src/kotlin/build-logic.*.gradle.kts files,
    // and we need to declare dependency on org.gradle.kotlin.kotlin-dsl:org.gradle.kotlin.kotlin-dsl.gradle.plugin
    // to make it work.
    // See https://github.com/gradle/gradle/issues/17016 regarding expectedKotlinDslPluginsVersion
    implementation("org.gradle.kotlin.kotlin-dsl:org.gradle.kotlin.kotlin-dsl.gradle.plugin:$expectedKotlinDslPluginsVersion")
}

// We need to figure out a version that is supported by the current JVM, and by the Kotlin Gradle plugin
// So we settle on 17 or 11 if the current JVM supports it
listOf(17, 11)
    .firstOrNull { JavaVersion.toVersion(it) <= JavaVersion.current() }
    ?.let { buildScriptJvmTarget ->
        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(buildScriptJvmTarget))
            }
        }
    }
