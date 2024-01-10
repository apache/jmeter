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
    id("build-logic.kotlin-dsl-gradle-plugin")
}

val latest = "latest.release"

dependencies {
    api(projects.buildParameters)
    api("org.openrewrite.rewrite:org.openrewrite.rewrite.gradle.plugin:6.6.3")

    implementation(platform("org.openrewrite:rewrite-bom:latest.integration"))
    implementation("org.openrewrite:rewrite-core")
    implementation("org.openrewrite:rewrite-gradle")
    implementation("org.openrewrite:rewrite-groovy")
    implementation("org.openrewrite:rewrite-hcl")
    implementation("org.openrewrite:rewrite-java")
    implementation("org.openrewrite:rewrite-json")
    implementation("org.openrewrite:rewrite-kotlin:latest.integration")
    implementation("org.openrewrite:rewrite-properties")
    implementation("org.openrewrite:rewrite-protobuf")
    implementation("org.openrewrite:rewrite-xml")
    implementation("org.openrewrite:rewrite-yaml")
    implementation("org.openrewrite:rewrite-polyglot:latest.integration")
    // implementation("com.puppycrawl.tools:checkstyle:9.3")
    implementation("org.jetbrains.kotlin.kapt:org.jetbrains.kotlin.kapt.gradle.plugin:1.9.22")
}
