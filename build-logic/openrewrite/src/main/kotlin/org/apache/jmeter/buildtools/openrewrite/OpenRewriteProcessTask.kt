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

package org.apache.jmeter.buildtools.openrewrite

import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.domainObjectContainer
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.submit
import org.gradle.kotlin.dsl.the
import org.gradle.workers.WorkerExecutionException
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
abstract class OpenRewriteProcessTask @Inject constructor(
    objects: ObjectFactory,
    private val layout: ProjectLayout,
    private val executor: WorkerExecutor,
    private val fileOperations: FileOperations,
) : DefaultTask() {
    @Input
    val activeRecipes = objects.listProperty<String>().convention(
        project.the<OpenRewriteExtension>().activeRecipes
    )

    @Classpath
    val rewriteRuntimeClasspath = objects.fileCollection()
        .from(project.configurations.named("openrewriteClasspath"))

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    val configFile = objects.fileProperty().convention(
        project.the<OpenRewriteExtension>().configFile
    )

    @get:Nested
    val sourceSets = objects.domainObjectContainer(SourceSetConfig::class)

    @OutputDirectory
    val outputDirectory = objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("openrewrite/$name/formatted"))

    init {
        // Gradle does not have FileSystemOperations.fileTree, so we can't have .files initialization
        // in SourceDirectorySetConfig
        sourceSets.all {
            srcDirSets.all {
                files.set(
                    project.provider {
                        sourceDirectories.map {
                            project.fileTree(it) {
                                matching(filter)
                                include(includes)
                            }
                        }
                    }
                )
            }
        }
    }

    @TaskAction
    fun run() {
        val outputDir = outputDirectory.get().asFile
        // Currently the task is not incremental, so we delete previous data
        fileOperations.delete(outputDir)
        fileOperations.mkdir(outputDir)
        val queue = executor.processIsolation {
            classpath.from(rewriteRuntimeClasspath)
        }
        val taskProps = this
        queue.submit(OpenRewriteWork::class) {
            logCompilationWarningsAndErrors.set(true)
            outputDirectory.set(taskProps.outputDirectory)
            baseDir.set(layout.buildDirectory)
            configFile.set(taskProps.configFile)
            activeRecipes.set(taskProps.activeRecipes)
            rewriteRuntimeClasspath.set(taskProps.rewriteRuntimeClasspath)
            taskProps.sourceSets.all {
                sourceSets.add(toSnapshot())
            }
        }

        try {
            queue.await()
        } catch (e: WorkerExecutionException) {
            // The queue has classpath isolation, so `it is RewriteException` can't be used here
//            e.causes?.singleOrNull()?.cause
//                ?.takeIf { it::class.qualifiedName == RewriteException::class.qualifiedName }
//                ?.let { throw it }
            throw e
        }
    }

}
