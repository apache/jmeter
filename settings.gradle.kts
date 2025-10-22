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
import java.util.*

pluginManagement {
    plugins {
        id("com.github.vlsi.stage-vote-release") version "1.90"
    }
}

plugins {
    id("com.gradle.develocity") version "3.18.2"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.0.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // TODO: support enableMavenLocal
        mavenCentral()
    }
}

if (JavaVersion.current() < JavaVersion.VERSION_17) {
    throw UnsupportedOperationException("Please use Java 17 or 21 for launching Gradle when building JMeter, the current Java is ${JavaVersion.current().majorVersion}")
}

// This is the name of a current project
// Note: it cannot be inferred from the directory name as developer might clone JMeter to jmeter_tmp folder
rootProject.name = "jmeter"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic-commons")
includeBuild("build-logic")

include(
    "src:bom",
    "src:bom-testing",
    "src:bom-thirdparty",
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
    "src:protocol:native",
    "src:protocol:tcp",
    "src:release",
    "src:testkit",
    "src:testkit-wiremock",
    "src:test-services",
    "src:dist",
    "src:dist-check"
)

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

if (property("localReleasePlugins").toBool(nullAs = false, blankAs = true, default = false)) {
    // This enables to use local clone of vlsi-release-plugins for debugging purposes
    includeBuild("../vlsi-release-plugins")
}

val isCiServer = System.getenv().containsKey("CI")

develocity {
    server = "https://develocity.apache.org"
    allowUntrustedServer = false
    projectId = "jmeter"

    buildScan {
        uploadInBackground = !isCiServer
        publishing.onlyIf { it.isAuthenticated }
        obfuscation {
            ipAddresses { addresses -> addresses.map { "0.0.0.0" } }
        }
    }
}

buildscript {
    dependencies {
        // Remove when Autostyle updates jgit dependency
        classpath("org.eclipse.jgit:org.eclipse.jgit:6.10.1.202505221210-r")
    }
    repositories {
        gradlePluginPortal()
    }
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

buildCache {
    local {
        isEnabled = !isCiServer
    }
    remote(develocity.buildCache) {
        isEnabled = false
    }
}
