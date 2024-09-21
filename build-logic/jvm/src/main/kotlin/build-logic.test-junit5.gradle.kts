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
    `java-library`
    id("build-logic.test-base")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.hamcrest:hamcrest")
}

plugins.withId("java-test-fixtures") {
    dependencies {
        "testFixturesImplementation"("org.junit.jupiter:junit-jupiter")
    }
}

tasks.configureEach<Test> {
    useJUnitPlatform()
    // Pass the property to tests
    fun passProperty(name: String, default: String? = null) {
        val value = System.getProperty(name) ?: default
        value?.let { systemProperty(name, it) }
    }
    passProperty("junit.jupiter.execution.parallel.enabled", "true")
    passProperty("junit.jupiter.execution.timeout.default", "2 m")
}
