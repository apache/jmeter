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
    api(projects.basics)
    api(projects.buildParameters)
    api("com.github.autostyle:com.github.autostyle.gradle.plugin:3.2")
    api("com.github.spotbugs:com.github.spotbugs.gradle.plugin:5.0.14")
    api("com.github.vlsi.ide:com.github.vlsi.ide.gradle.plugin:1.89")
    api("com.github.vlsi.gradle-extensions:com.github.vlsi.gradle-extensions.gradle.plugin:1.89")
    api("de.thetaphi.forbiddenapis:de.thetaphi.forbiddenapis.gradle.plugin:3.5.1")
    api("net.ltgt.errorprone:net.ltgt.errorprone.gradle.plugin:3.1.0")
    api("org.checkerframework:org.checkerframework.gradle.plugin:0.6.27")
    api("org.jetbrains.gradle.plugin.idea-ext:org.jetbrains.gradle.plugin.idea-ext.gradle.plugin:1.1.7")
    api("org.sonarqube:org.sonarqube.gradle.plugin:4.0.0.2929")
}
