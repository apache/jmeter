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

    api(projects.src.components) {
        because("we need SearchTextExtension")
    }
    testImplementation(project(":src:components", "testClasses"))

    api("com.thoughtworks.xstream:xstream") {
        because("HTTPResultConverter uses XStream in public API")
    }

    compileOnly("javax.activation:javax.activation-api") {
        because("ParseCurlCommandAction uses new MimetypesFileTypeMap()")
    }

    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("commons-io:commons-io") {
        because("IOUtils")
    }
    implementation("org.apache.commons:commons-lang3") {
        because("StringUtils")
    }
    implementation("org.apache.commons:commons-text") {
        because("StringEscapeUtils")
    }
    implementation("org.jodd:jodd-lagarto")
    implementation("org.jsoup:jsoup")
    implementation("oro:oro")
    implementation("org.apache.commons:commons-collections4")
    implementation("commons-net:commons-net")
    implementation("com.helger.commons:ph-commons") {
        // We don't really need to use/distribute jsr305
        exclude("com.google.code.findbugs", "jsr305")
    }
    implementation("com.helger:ph-css") {
        // We don't really need to use/distribute jsr305
        exclude("com.google.code.findbugs", "jsr305")
    }
    implementation("dnsjava:dnsjava")
    implementation("org.apache.httpcomponents:httpmime")
    implementation("org.apache.httpcomponents:httpcore")
    implementation("org.brotli:dec")
    implementation("com.miglayout:miglayout-swing")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation(testFixtures(projects.src.testkitWiremock))
    testImplementation("com.github.tomakehurst:wiremock-jre8")
    // For some reason JMeter bundles just tika-core and tika-parsers without transitive
    // dependencies. So we exclude those
    implementation("org.apache.tika:tika-core") {
        isTransitive = false
    }
    runtimeOnly("org.apache.tika:tika-parsers") {
        isTransitive = false
    }

    testImplementation(project(":src:core", "testClasses"))
}
