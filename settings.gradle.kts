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

pluginManagement {
    plugins {
        fun String.v() = extra["$this.version"].toString()
        fun PluginDependenciesSpec.idv(id: String, key: String = id) = id(id) version key.v()

        idv("com.github.autostyle")
        idv("com.github.spotbugs")
        idv("com.github.vlsi.crlf", "com.github.vlsi.vlsi-release-plugins")
        idv("com.github.vlsi.gradle-extensions", "com.github.vlsi.vlsi-release-plugins")
        idv("com.github.vlsi.ide", "com.github.vlsi.vlsi-release-plugins")
        idv("com.github.vlsi.stage-vote-release", "com.github.vlsi.vlsi-release-plugins")
        idv("net.ltgt.errorprone")
        idv("org.jetbrains.gradle.plugin.idea-ext")
        idv("org.nosphere.apache.rat")
        idv("org.sonarqube")
    }
}

plugins {
    // Gradle Enterprise requires downloading a plugin from the Gradle portal, which might
    // be blocked in restricted environments. Allow opting in via USE_GRADLE_ENTERPRISE=true
    // so local/offline builds can proceed without trying to resolve the plugin.
    if (System.getenv("USE_GRADLE_ENTERPRISE")?.equals("true", ignoreCase = true) == true) {
        `gradle-enterprise`
    }
}

// This is the name of a current project
// Note: it cannot be inferred from the directory name as developer might clone JMeter to jmeter_tmp folder
rootProject.name = "jmeter"

include(
        "src:bom",
        "src:bshclient",
        "src:launcher",
        "src:components",
        "src:config",
        "src:core",
        "src:examples",
        "src:functions",
        "src:generator",
        "src:jorphan",
        "src:licenses",
        "src:protocol:bolt",
        "src:protocol:ftp",
        "src:protocol:http",
        "src:protocol:java",
        "src:protocol:jdbc",
        "src:protocol:jms",
        "src:protocol:junit",
        "src:protocol:junit-sample",
        "src:protocol:ldap",
        "src:protocol:mail",
        "src:protocol:mongodb",
        "src:protocol:native",
        "src:protocol:tcp",
        "src:release",
        "src:testkit",
        "src:testkit-wiremock",
        "src:dist",
        "src:dist-check")

// See https://github.com/gradle/gradle/issues/1348#issuecomment-284758705 and
// https://github.com/gradle/gradle/issues/5321#issuecomment-387561204
// Gradle inherits Ant "default excludes", however we do want to archive those files
org.apache.tools.ant.DirectoryScanner.removeDefaultExclude("**/.gitattributes")
org.apache.tools.ant.DirectoryScanner.removeDefaultExclude("**/.gitignore")

fun String?.toBool(nullAs: Boolean, blankAs: Boolean, default: Boolean) =
    when {
        this == null -> nullAs
        isBlank() -> blankAs
        default -> !equals("false", ignoreCase = true)
        else -> equals("true", ignoreCase = true)
    }

fun property(name: String) =
        when (extra.has(name)) {
                true -> extra.get(name) as? String
                else -> null
        }

val useGradleEnterprise = System.getenv("USE_GRADLE_ENTERPRISE")?.toBool(nullAs = false, blankAs = false, default = false) ?: false
val useChecksumPlugin = System.getenv("USE_CHECKSUM_PLUGIN")?.toBool(nullAs = false, blankAs = false, default = false) ?: false

if (property("localReleasePlugins").toBool(nullAs = false, blankAs = true, default = false)) {
        // This enables to use local clone of vlsi-release-plugins for debugging purposes
        includeBuild("../vlsi-release-plugins")
}

val isCiServer = System.getenv().containsKey("CI")

if (useChecksumPlugin) {
    println("USE_CHECKSUM_PLUGIN is enabled, but checksum verification is disabled in this environment.")
}

// This enables to try local Autostyle
property("localAutostyle")?.ifBlank { "../autostyle" }?.let {
    println("Importing project '$it'")
    includeBuild(it)
}

property("localDarklaf")?.ifBlank { "../darklaf" }?.let {
    println("Importing project '$it'")
    includeBuild(it)
}
