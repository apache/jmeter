import org.apache.jmeter.buildtools.openrewrite.OpenRewriteExtension
import org.apache.jmeter.buildtools.openrewrite.OpenRewriteProcessTask

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

val openrewrite by configurations.creating {
    description = "OpenRewrite dependencies"
    isCanBeConsumed = false
    isCanBeResolved = false
}

val openrewriteClasspath by configurations.creating {
    description = "OpenRewrite classpath"
    isCanBeConsumed = false
    isCanBeResolved = true
    extendsFrom(openrewrite)
    attributes {
        // TODO: add toolchain and prefer the relevant JVM target variant
        attribute(
            Bundling.BUNDLING_ATTRIBUTE,
            project.objects.named(Bundling::class.java, Bundling.EXTERNAL)
        )
        attribute(
            TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE,
            project.objects.named(TargetJvmEnvironment::class.java, TargetJvmEnvironment.STANDARD_JVM)
        )
    }
}

val openrewriteExtension = project.extensions.create<OpenRewriteExtension>(OpenRewriteExtension.NAME)

val rewriteRun by tasks.registering(OpenRewriteProcessTask::class) {
}

val rewriteDryRun by tasks.registering {
}

plugins.withId("java") {
    val java = project.the<JavaPluginExtension>()
    java.sourceSets.whenObjectRemoved {
        val removedName = name
        rewriteRun.configure {
            sourceSets.remove(sourceSets[removedName])
        }
    }
    java.sourceSets.all {
        val sourceSet = this
        rewriteRun.configure {
            sourceSets.create(sourceSet.name) {
                compileClasspath.from(sourceSet.compileClasspath)
                compileClasspath.from(sourceSet.output.classesDirs)
                javaRelease.set(
                    tasks.named<JavaCompile>(sourceSet.compileJavaTaskName)
                        .flatMap { it.options.release }
                )
                srcDirSets.create("resources") {
                    sourceDirectories.from(sourceSet.resources.sourceDirectories)
                    filter = sourceSet.resources.filter
                }
                srcDirSets.create("java") {
                    sourceDirectories.from(sourceSet.java.sourceDirectories)
                    filter = sourceSet.java.filter
                }
            }
        }
    }
}

plugins.withId("org.jetbrains.kotlin.jvm") {
    val java = project.the<JavaPluginExtension>()
    java.sourceSets.all {
        val sourceSet = this
        val kotlin: SourceDirectorySet by (sourceSet as ExtensionAware).extensions
        rewriteRun.configure {
            sourceSets.named(sourceSet.name) {
                srcDirSets.create("kotlin") {
                    sourceDirectories.from(kotlin.sourceDirectories)
                    filter = kotlin.filter
                    // Kotlin source set includes java, however OpenRewrite does not need it for Kotlin.
                    // OpenRewrite uses compiled classes instead to get Java types
                    includes.add("**/*.kt")
                }
            }
        }
    }
}
