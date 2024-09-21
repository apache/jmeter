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
    constraints {
        api("xerces:xercesImpl:2.12.1") {
            because("Some of the plugins might depend on an older version, and we want using a more recent one")
        }
    }
    api(projects.buildParameters)
    api(projects.verification)
    api("com.github.vlsi.crlf:com.github.vlsi.crlf.gradle.plugin:1.90")
    api("com.github.vlsi.ide:com.github.vlsi.ide.gradle.plugin:1.90")
    api("com.github.vlsi.gradle-extensions:com.github.vlsi.gradle-extensions.gradle.plugin:1.90")
    api("org.nosphere.apache.rat:org.nosphere.apache.rat.gradle.plugin:0.8.1")
    api("org.jetbrains.gradle.plugin.idea-ext:org.jetbrains.gradle.plugin.idea-ext.gradle.plugin:1.1.7")
    api("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:1.9.22")
}
