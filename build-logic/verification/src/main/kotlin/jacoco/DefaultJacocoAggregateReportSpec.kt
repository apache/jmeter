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

package jacoco

import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.testing.jacoco.tasks.JacocoReport
import javax.inject.Inject

abstract class DefaultJacocoAggregateReportSpec @Inject constructor(
    private val name: String,
    tasks: TaskContainer
) : JacocoAggregateReportSpec {
    override fun getName(): String = name

    override val reportTask: TaskProvider<JacocoReport> =
        tasks.register<JacocoReport>(name) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Generates aggregated code coverage report"

            reports {
                xml.required.convention(true)
                html.required.convention(true)
            }
        }
}
