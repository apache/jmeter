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
import org.jetbrains.kotlin.gradle.tasks.Kapt

plugins {
    id("build-logic.jvm-library")
}

dependencies {
    implementation(projects.src.jorphan)
    implementation(projects.src.core)
}

tasks.configureEach<Kapt> {
    // ServiceNotImplementingInterface does not implement the service interface for testing purposes,
    // so we disable AutoService verifications
    annotationProcessorOptionProviders.add(
        listOf(CommandLineArgumentProvider { listOf("-Averify=false") })
    )
}
