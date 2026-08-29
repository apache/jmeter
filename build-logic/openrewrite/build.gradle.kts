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

dependencies {
    // The OpenRewrite libraries are needed only to COMPILE the worker: at runtime the worker
    // runs in an isolated process and gets rewrite-core/java/kotlin plus the recipe modules
    // from the project's `openrewrite` configuration. Keeping them compileOnly avoids pulling
    // OpenRewrite (and ClassGraph) onto the Gradle build-logic classpath.
    compileOnly(platform("org.openrewrite.recipe:rewrite-recipe-bom:3.34.0"))
    compileOnly("org.openrewrite:rewrite-core")
    compileOnly("org.openrewrite:rewrite-java")
    compileOnly("org.openrewrite:rewrite-kotlin")
    compileOnly("org.openrewrite:rewrite-xml")
}
