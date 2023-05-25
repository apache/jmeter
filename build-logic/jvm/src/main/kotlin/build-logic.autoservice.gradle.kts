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

plugins {
    id("java-library")
}

dependencies {
    annotationProcessor("com.google.auto.service:auto-service")
    compileOnlyApi("com.google.auto.service:auto-service-annotations")
}

plugins.withId("org.jetbrains.kotlin.jvm") {
    apply(plugin = "org.jetbrains.kotlin.kapt")

    dependencies {
        "kapt"("com.google.auto.service:auto-service")
        // Unfortunately, plugins.withId("..kapt") can't be used as kapt plugin adds configuration via plugins.withId
        findProject(":src:bom-thirdparty")?.let {
            "kapt"(platform(it))
        }
    }
}

tasks.configureEach<Jar> {
    manifest {
        attributes["JMeter-Skip-Class-Scanning"] = "true"
    }
}
