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
        id("com.github.vlsi.stage-vote-release") version "1.86"
    }
}

plugins {
    `gradle-enterprise`
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

if (isCiServer) {
    gradleEnterprise {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            tag("CI")
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
        classpath("org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r")
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
    "768DBB3BA2649A7025338197B4703B414983E4608138719B0D69201F7F49E57E0454846B41B775B135B083F820824749DA6121F20A812C4F155A97F09C9B15AC"
        to "org.eclipse.jgit-5.13.0.202109080827-r.jar",
    "E5435852569DDA596BA46138AF8EE9C4ECBA8A7A43F4F1E7897AEB4430523A0F037088A7B63877DF5734578F19D331F03D7B0F32D5AE6C425DF211947B3E6173"
        to "slf4j-api-1.7.30.jar",
    "F7040C571C2A2727F2EED4EA772F5A7C5D9CB393828B7A2331F7167E467429486F5F3E9423883FE9A6D652FFB0484EAE722CDFB46D97180209BCBEEBF9C25DE3"
        to "gradle-enterprise-gradle-plugin-3.4.jar",
    "D5B49D90170DEA96E3D05D893B2B6C04E3B16F3DB6B6BB1DF82D3DE3247E5B0457721F232FAA237E689100980E97F4C04C1234FBEDBDAB7AE0CEAA91C40392C9"
        to "gradle-enterprise-gradle-plugin-3.4.1.jar",
    "AA8D06BDF95A6BAAEFE2B0DAE530FCD324A92238F7B790C0FF4A4B5C6454A6BE83D2C81BFEC013A7368697A0A9FC61B97E91775EF9948EF5572FA1DAA9E82052"
        to "gradle-enterprise-gradle-plugin-3.5.jar",
    "2A01A91008DF02AA0256D64EAB9238B23B85EA2A886E024E07C3880D642C5E4B96E66DE0D90832BCCEFE5F7C8EF045EBB9905B2A74398E38FAD6A5B28BEBA54D"
        to "gradle-enterprise-gradle-plugin-3.6.jar",
    "43BC9061DFDECA0C421EDF4A76E380413920E788EF01751C81BDC004BD28761FBD4A3F23EA9146ECEDF10C0F85B7BE9A857E9D489A95476525565152E0314B5B"
        to "gradle-enterprise-gradle-plugin-3.6.3.jar",
    "CF0F77035EC4E61E310AAAF484AD543D8FFF84D31BF6F93183D09CA6056FB1F87B10F355F08F11198140AC47DD92A4DE4E5FED16C993A8B4C93FE169A61BB3A3"
        to "gradle-enterprise-gradle-plugin-3.7.jar",
    "7AC5F1C070A8C0A2BD096D96E896EB147966C39E0746120ABA5E107DDBDED441FF71F31F167475CD36EE082D8430D1FB98C51D29C6B91D147CC64DCE59C66D49"
        to "gradle-enterprise-gradle-plugin-3.7.2.jar",
    "24A1722CB574BA3126C3C6EBEB3D4A39D2A86ECCEDD378BA96A5508626D1AEAC7BB5FFBC189929E16900B94C1D016AFA83A462DCB2BB03F634FCA9C7FDE9EBA5"
        to "gradle-enterprise-gradle-plugin-3.8.jar",
    "11BED06D6ECD5669DE3F32A0B386A223C2B2BA532A3C80D3E1613B29A1A264E1A75CDF142273F17EE1845287C76CF1F9D68CD97542318B1A9126104CE958824A"
        to "gradle-enterprise-gradle-plugin-3.8.1.jar",
    "B795B889E3E2EC6D3E3DEB7429345A62B3FCCB4E51B88014AA77FBC3F61D3BC4307E023C83B08FEDA49BF2EAC6B99A48D2995CB9425E5C3E76BBE25D4170BDC8"
        to "gradle-enterprise-gradle-plugin-3.11.4.jar",
    "AFC0F3E5B78359131E2BF6C24A128F1CE76F2D128F38DFA8C01F7CF74D1695527C8E953E4B6E7C3ACD925CC03A50845A53B84359AC2B0F5C1F233355E45186D1"
        to "gradle-enterprise-gradle-plugin-3.12.6.jar",
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
