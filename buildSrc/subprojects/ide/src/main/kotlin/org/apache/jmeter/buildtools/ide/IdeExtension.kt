/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.buildtools.ide

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.io.File

open class IdeExtension(private val project: Project) {
    fun generatedJavaSources(task: Task, generationOutput: File) {
        val sourceSets: SourceSetContainer by project

        project.tasks.named(JavaPlugin.COMPILE_JAVA_TASK_NAME) {
            dependsOn(task)
        }

        sourceSets["main"].java.srcDir(generationOutput)

        project.configure<IdeaModel> {
            module.generatedSourceDirs.add(generationOutput)
        }

        project.rootProject.configure<IdeaModel> {
            project {
                settings {
                    taskTriggers {
                        // Build the `customInstallation` after the initial import to:
                        // 1. ensure generated code is available to the IDE
                        // 2. allow integration tests to be executed
                        afterSync(task)
                    }
                }
            }
        }
    }
}
