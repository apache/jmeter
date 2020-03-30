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
        idv("org.jetbrains.gradle.plugin.idea-ext")
        idv("org.nosphere.apache.rat")
        idv("org.sonarqube")
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

if (property("localReleasePlugins").toBool(nullAs = false, blankAs = true, default = false)) {
        // This enables to use local clone of vlsi-release-plugins for debugging purposes
        includeBuild("../vlsi-release-plugins")
}

// Checksum plugin sources can be validated at https://github.com/vlsi/vlsi-release-plugins
buildscript {
    dependencies {
        classpath("com.github.vlsi.gradle:checksum-dependency-plugin:${settings.extra["com.github.vlsi.checksum-dependency.version"]}") {
            // Gradle ships kotlin-stdlib which is good enough
            exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        }
    }
    repositories {
        gradlePluginPortal()
    }
}

// Note: we need to verify the checksum for checksum-dependency-plugin itself
val expectedSha512 = mapOf(
    "43BC9061DFDECA0C421EDF4A76E380413920E788EF01751C81BDC004BD28761FBD4A3F23EA9146ECEDF10C0F85B7BE9A857E9D489A95476525565152E0314B5B"
            to "bcpg-jdk15on-1.62.jar",
    "2BA6A5DEC9C8DAC2EB427A65815EB3A9ADAF4D42D476B136F37CD57E6D013BF4E9140394ABEEA81E42FBDB8FC59228C7B85C549ED294123BF898A7D048B3BD95"
            to "bcprov-jdk15on-1.62.jar",
    "17DAAF511BE98F99007D7C6B3762C9F73ADD99EAB1D222985018B0258EFBE12841BBFB8F213A78AA5300F7A3618ACF252F2EEAD196DF3F8115B9F5ED888FE827"
            to "okhttp-4.1.0.jar",
    "93E7A41BE44CC17FB500EA5CD84D515204C180AEC934491D11FC6A71DAEA761FB0EECEF865D6FD5C3D88AAF55DCE3C2C424BE5BA5D43BEBF48D05F1FA63FA8A7"
            to "okio-2.2.2.jar",
    settings.extra["com.github.vlsi.checksum-dependency.sha512"].toString()
            to "checksum-dependency-plugin.jar"
)

fun File.sha512(): String {
    val md = java.security.MessageDigest.getInstance("SHA-512")
    forEachBlock { buffer, bytesRead ->
        md.update(buffer, 0, bytesRead)
    }
    return BigInteger(1, md.digest()).toString(16).toUpperCase()
}

val violations =
    buildscript.configurations["classpath"]
        .resolve()
        .sortedBy { it.name }
        .associateWith { it.sha512() }
        .filterNot { (_, sha512) -> expectedSha512.contains(sha512) }
        .entries
        .joinToString("\n  ") { (file, sha512) -> "SHA-512(${file.name}) = $sha512 ($file)" }

if (violations.isNotBlank()) {
    throw GradleException("Buildscript classpath has non-whitelisted files:\n  $violations")
}

apply(plugin = "com.github.vlsi.checksum-dependency")

// This enables to try local Autostyle
property("localAutostyle")?.ifBlank { "../autostyle" }?.let {
    println("Importing project '$it'")
    includeBuild(it)
}

property("localDarklaf")?.ifBlank { "../darklaf" }?.let {
    println("Importing project '$it'")
    includeBuild(it)
}
