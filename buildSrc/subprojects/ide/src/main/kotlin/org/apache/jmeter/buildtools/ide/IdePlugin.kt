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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaProject
import org.jetbrains.gradle.ext.*

object JMeterCopyright {
    val profileName = "ASL2"
    val keyword = "Copyright"
    val notice = """
        Licensed to the Apache Software Foundation (ASF) under one or more
        contributor license agreements.  See the NOTICE file distributed with
        this work for additional information regarding copyright ownership.
        The ASF licenses this file to You under the Apache License, Version 2.0
        (the "License"); you may not use this file except in compliance with
        the License.  You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.

    """.trimIndent()
}

open class IdePlugin : Plugin<Project> {
    lateinit var ext: IdeExtension;

    override fun apply(project: Project): Unit = project.run {
        ext = extensions.create("ide", IdeExtension::class.java, project)
        configureEclipse()
        configureIdea()
    }

    fun Project.configureEclipse() = allprojects {
        apply(plugin = "eclipse")
    }

    fun Project.configureIdea() {
        apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")
        tasks.named("idea") {
            doFirst {
                throw RuntimeException("To import in IntelliJ IDEA, please follow the instructions here: https://github.com/apache/jmeter/blob/master/CONTRIBUTING.md#intellij")
            }
        }

        if (this == rootProject) {
            configureIdeaForRootProject()
        }
    }

    fun Project.configureIdeaForRootProject() {
//        val rootProject = this
        plugins.withType<IdeaPlugin> {
            with(model) {
                project {
                    vcs = "Git"

                    settings {
                        doNotDetectFrameworks("android", "web")
                        configureCopyright()
//                            configureCompilerSettings(rootProject)
                        // TODO The idea-ext plugin does not yet support customizing inspections.
                        // TODO Delete .idea/inspectionProfiles and uncomment the code below when it does
//                            configureRunConfigurations(rootProject)
                    }
                }
            }
        }
    }

    private fun ProjectSettings.configureCopyright() {
        copyright {
            useDefault = JMeterCopyright.profileName
            profiles {
                create(JMeterCopyright.profileName) {
                    notice = JMeterCopyright.notice
                    keyword = JMeterCopyright.keyword
                }
            }
        }
    }
}

fun IdeaProject.settings(configuration: ProjectSettings.() -> kotlin.Unit) =
    (this as ExtensionAware).configure(configuration)

fun ProjectSettings.taskTriggers(configuration: TaskTriggersConfig.() -> kotlin.Unit) =
    (this as ExtensionAware).configure(configuration)

fun ProjectSettings.compiler(configuration: IdeaCompilerConfiguration.() -> kotlin.Unit) =
    (this as ExtensionAware).configure(configuration)

fun ProjectSettings.groovyCompiler(configuration: GroovyCompilerConfiguration.() -> kotlin.Unit) =
    (this as ExtensionAware).configure(configuration)

fun ProjectSettings.copyright(configuration: CopyrightConfiguration.() -> kotlin.Unit) =
    (this as ExtensionAware).configure(configuration)