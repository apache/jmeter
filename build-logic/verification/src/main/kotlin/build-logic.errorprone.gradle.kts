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
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

plugins {
    id("java")
    id("build-logic.build-params")
}

if (buildParameters.enableErrorprone) {
    apply(plugin = "net.ltgt.errorprone")

    dependencies {
        "errorprone"("com.google.errorprone:error_prone_core:2.19.1")
        "annotationProcessor"("com.google.guava:guava-beta-checker:1.0")
    }

    tasks.configureEach<JavaCompile> {
        if ("Test" in name) {
            // Ignore warnings in test code
            options.errorprone.isEnabled.set(false)
        } else {
            // Errorprone requires Java 11+
            options.errorprone.isEnabled.set(
                javaCompiler.map { it.metadata.languageVersion.canCompileOrRun(11) }
            )
            options.compilerArgs.addAll(listOf("-Xmaxerrs", "10000", "-Xmaxwarns", "10000"))
            options.errorprone {
                disableWarningsInGeneratedCode.set(true)
                enable(
                    "MissingDefault",
                    "PackageLocation",
                    "RedundantOverride",
                    "StronglyTypeTime",
                    "UnescapedEntity",
                    "UnnecessaryAnonymousClass",
                    "UnnecessaryDefaultInEnumSwitch",
                )
                warn(
                    "FieldCanBeFinal",
                    "FieldCanBeStatic",
                    "ForEachIterable",
                    "MethodCanBeStatic",
                )
                disable(
                    "ComplexBooleanConstant",
                    "EqualsGetClass",
                    "InlineMeSuggester",
                    "OperatorPrecedence",
                    "MutableConstantField",
                    // "ReferenceEquality",
                    "SameNameButDifferent",
                    "TypeParameterUnusedInFormals"
                )
                // Analyze issues, and enable the check
                disable(
                    "BigDecimalEquals",
                    "EmptyBlockTag",
                    "MissingSummary",
                    "StringSplitter",
                    "BanJNDI",
                )
            }
        }
    }
}
