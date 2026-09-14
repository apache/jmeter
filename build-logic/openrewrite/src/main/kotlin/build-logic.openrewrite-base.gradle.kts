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

import org.apache.jmeter.buildtools.openrewrite.OpenRewriteBaseTask
import org.apache.jmeter.buildtools.openrewrite.OpenRewriteDiagnoseTask
import org.apache.jmeter.buildtools.openrewrite.OpenRewriteDryRunTask
import org.apache.jmeter.buildtools.openrewrite.OpenRewriteExtension
import org.apache.jmeter.buildtools.openrewrite.OpenRewriteRunTask
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.java.TargetJvmEnvironment
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.language.base.plugins.LifecycleBasePlugin

val openrewrite by configurations.creating {
    description = "OpenRewrite recipe dependencies"
    isCanBeConsumed = false
    isCanBeResolved = false
}

val openrewriteClasspath by configurations.creating {
    description = "Resolvable OpenRewrite recipe classpath"
    isCanBeConsumed = false
    isCanBeResolved = true
    extendsFrom(openrewrite)
    attributes {
        attribute(
            Bundling.BUNDLING_ATTRIBUTE,
            objects.named(Bundling::class.java, Bundling.EXTERNAL)
        )
        attribute(
            TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
            objects.named(TargetJvmEnvironment::class.java, TargetJvmEnvironment.STANDARD_JVM)
        )
    }
}

val openrewriteExtension = extensions.create<OpenRewriteExtension>(OpenRewriteExtension.NAME)

fun configureCommon(task: OpenRewriteBaseTask) {
    task.group = "openrewrite"
    task.activeRecipes.set(openrewriteExtension.activeRecipes)
    task.disabledRecipes.set(openrewriteExtension.disabledRecipes)
    // Additively disable more recipes from the command line, e.g.
    // -PopenrewriteDisable=org.openrewrite.java.ShortenFullyQualifiedTypeReferences,org.openrewrite.staticanalysis.TypecastParenPad
    task.disabledRecipes.addAll(
        providers.gradleProperty("openrewriteDisable")
            .map { prop -> prop.split(",").map { it.trim() }.filter { it.isNotEmpty() } }
            .orElse(emptyList())
    )
    task.activeStyles.set(openrewriteExtension.activeStyles)
    task.rewriteClasspath.from(openrewriteClasspath)
    task.configFile.set(openrewriteExtension.configFile)
    task.projectRootDir.set(layout.projectDirectory)
    task.logCompilationWarningsAndErrors.convention(false)
    // Allow flipping Kotlin processing from the command line, e.g. -PopenrewriteKotlin=true
    task.processKotlin.set(
        providers.gradleProperty("openrewriteKotlin")
            .map { it.toBoolean() }
            .orElse(openrewriteExtension.processKotlin)
    )
}

tasks.register<OpenRewriteRunTask>("rewriteRun") {
    description = "Applies the active OpenRewrite recipes in place"
    configureCommon(this)
}

tasks.register<OpenRewriteDryRunTask>("rewriteDryRun") {
    description = "Reports the changes the active OpenRewrite recipes would make"
    configureCommon(this)
    reportDir.set(layout.buildDirectory.dir("reports/openrewrite"))
    failOnDryRunResults.set(openrewriteExtension.failOnDryRunResults)
}

tasks.register<OpenRewriteDiagnoseTask>("rewriteDiagnose") {
    description = "Runs each active leaf recipe on its own and reports failures and no-ops"
    configureCommon(this)
    reportDir.set(layout.buildDirectory.dir("reports/openrewrite"))
}

plugins.withId("java") {
    the<JavaPluginExtension>().sourceSets.all {
        val sourceSet: SourceSet = this
        val javaRelease = tasks.named<JavaCompile>(sourceSet.compileJavaTaskName)
            .flatMap { it.options.release }
        tasks.withType<OpenRewriteBaseTask>().configureEach {
            sourceSets.maybeCreate(sourceSet.name).apply {
                this.javaRelease.set(javaRelease)
                // Include the compiled classes (Java AND Kotlin output) so the Java parser can
                // resolve types declared in Kotlin sources of the same module.
                compileClasspath.from(sourceSet.compileClasspath)
                compileClasspath.from(sourceSet.output.classesDirs)
                srcDirSets.maybeCreate("java").files.from(sourceSet.java)
            }
        }
    }
}

plugins.withId("org.jetbrains.kotlin.jvm") {
    the<JavaPluginExtension>().sourceSets.all {
        val sourceSet: SourceSet = this
        val kotlin = (sourceSet as ExtensionAware).extensions.getByName("kotlin") as SourceDirectorySet
        tasks.withType<OpenRewriteBaseTask>().configureEach {
            sourceSets.maybeCreate(sourceSet.name)
                .srcDirSets.maybeCreate("kotlin").files.from(kotlin)
        }
    }
}
