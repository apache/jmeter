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

plugins {
    id("build-logic.jvm-published-library")
}

dependencies {
    api(projects.src.core)

    testImplementation(project(":src:core", "testClasses"))
    api("com.github.ben-manes.caffeine:caffeine") {
        because("MessageRenderer#getValueFromFile(..., caffeine.cache.Cache)")
    }
    // TODO: technically speaking, jms_1.1_spec should be compileOnly
    // since we either include a JMS implementation or we can't use JMS at all
    implementation("org.apache.geronimo.specs:geronimo-jms_1.1_spec")
    implementation("org.apache.commons:commons-lang3") {
        because("StringUtils")
    }
    implementation("commons-io:commons-io") {
        because("IOUtils")
    }
    implementation("com.miglayout:miglayout-swing")

    testImplementation(project(":src:core", "testClasses"))
}
