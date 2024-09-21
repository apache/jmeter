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

import buildparameters.BuildParametersExtension
import org.gradle.api.JavaVersion

class ToolchainProperties(
    val version: Int,
    val vendor: String?,
    val implementation: String?,
)

val BuildParametersExtension.buildJdk: ToolchainProperties?
    get() = jdkBuildVersion.takeIf { it != 0 }
        ?.let { ToolchainProperties(it, jdkBuildVendor.orNull, jdkBuildImplementation.orNull) }

val BuildParametersExtension.buildJdkVersion: Int
    get() = buildJdk?.version ?: JavaVersion.current().majorVersion.toInt()

val BuildParametersExtension.testJdk: ToolchainProperties?
    get() = jdkTestVersion.orNull?.takeIf { it != 0 }
        ?.let { ToolchainProperties(it, jdkTestVendor.orNull, jdkTestImplementation.orNull) }
        ?: buildJdk
