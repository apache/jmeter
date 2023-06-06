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
import com.gradle.enterprise.gradleplugin.internal.extension.BuildScanExtensionWithHiddenFeatures

import java.util.*

pluginManagement {
    plugins {
        id("com.github.vlsi.stage-vote-release") version "1.89"
    }
}

plugins {
    id("com.gradle.enterprise") version "3.13.2"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "1.10"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // TODO: support enableMavenLocal
        mavenCentral()
    }
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
    "src:protocol:mongodb",
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

gradleEnterprise {
    server = "https://ge.apache.org"
    allowUntrustedServer = false

    buildScan {
        capture { isTaskInputFiles = true }
        isUploadInBackground = !isCiServer
        publishAlways()
        this as BuildScanExtensionWithHiddenFeatures
        publishIfAuthenticated()
        obfuscation {
            ipAddresses { addresses -> addresses.map { "0.0.0.0" } }
        }
    }
}

// Checksum plugin sources can be validated at https://github.com/vlsi/vlsi-release-plugins
buildscript {
    dependencies {
        classpath("com.github.vlsi.gradle:checksum-dependency-plugin:${settings.extra["com.github.vlsi.checksum-dependency.version"]}") {
            // Gradle ships kotlin-stdlib which is good enough
            exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        }
        // Remove when Autostyle updates jgit dependency
        classpath("org.eclipse.jgit:org.eclipse.jgit:5.13.1.202206130422-r")
    }
    repositories {
        gradlePluginPortal()
    }
}

// Note: we need to verify the checksum for checksum-dependency-plugin itself
val expectedSha512 = mapOf(
    // TODO: remove JavaEWAH-1.1.12.jar, org.eclipse.jgit-5.13.0.202109080827-r.jar, slf4j-api-1.7.30.jar when Autostyle updates jgit
    "ECBBFD1C6593AFBAA4CA7F65E10BC67B12FF75EB221E490051522FAC8D291B202A0A165644A9C5AC9E5618CD771817C9052F171E5E6210CB021DF5A6E4CBF787"
        to "JavaEWAH-1.1.12.jar",
    "5A3821A07EEBC9B56F87054C71B9BAFA0D9D0B556AA16191F8676D748CEC755A741CD5500E8676493FFCA69E6C9608CAF86D80B60B5934EE108D77030C490194"
        to "JavaEWAH-1.1.13.jar",
    "F9ED8AA41A3B085F534B5298DD9552773AA5D4194C60C1C6299F7EAC82CBD02C26C834CDE7D9B00605D5DF85F50FDE2A144653F1186B9BFC916D84569230518"
        to "common-custom-user-data-gradle-plugin-1.10.jar",
    "768DBB3BA2649A7025338197B4703B414983E4608138719B0D69201F7F49E57E0454846B41B775B135B083F820824749DA6121F20A812C4F155A97F09C9B15AC"
        to "org.eclipse.jgit-5.13.0.202109080827-r.jar",
    "DD1453E479CAC4E0D20143A1955654DC6C163916CA07C11F70F482F317BBF00A5FEDC8B198AF60BF17E9843E76DDA3103DAB3463384536F73A90AC0206CFB0E0"
        to "org.eclipse.jgit-5.13.1.202206130422-r.jar",
    "E5435852569DDA596BA46138AF8EE9C4ECBA8A7A43F4F1E7897AEB4430523A0F037088A7B63877DF5734578F19D331F03D7B0F32D5AE6C425DF211947B3E6173"
        to "slf4j-api-1.7.30.jar",
    "2625BD9FF15E17D47F678453B856E161E70E9460DB4DC05275CFE1EF7F6ACF8D2DC2CA8F2D59BB1B6E6DD1AD9CA617A41358FA03840A4D9FF18E231278FCAE13"
        to "gradle-enterprise-gradle-plugin-3.13.2.jar",
    "4E240B7811EF90C090E83A181DACE41DA487555E4136221861B0060F9AF6D8B316F2DD0472F747ADB98CA5372F46055456EF04BDC0C3992188AB13302922FCE9"
        to "bcpg-jdk15on-1.70.jar",
    "7DCCFC636EE4DF1487615818CFA99C69941081DF95E8EF1EAF4BCA165594DFF9547E3774FD70DE3418ABACE77D2C45889F70BCD2E6823F8539F359E68EAF36D1"
        to "bcprov-jdk15on-1.70.jar",
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
    return BigInteger(1, md.digest()).toString(16).uppercase(Locale.ROOT)
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
    throw GradleException("Buildscript classpath has files that were not explicitly permitted:\n  $violations")
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

buildCache {
    local {
        isEnabled = !isCiServer
    }
    remote(gradleEnterprise.buildCache) {
        isEnabled = false
    }
}
