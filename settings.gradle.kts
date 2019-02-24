/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
        when(extra.has(name)) {
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
                classpath("com.github.vlsi.gradle:checksum-dependency-plugin:1.19.0")
                // Alternative option is to use local jar file via
                // classpath(files("checksum-dependency-plugin-1.19.0.jar"))
        }
        repositories {
                gradlePluginPortal()
        }
}

// Note: we need to verify the checksum for checksum-dependency-plugin itself
val expectedSha512 =
        "D7B1A0C7937DCB11536F97C52FE25752BD7DA6011299E81FA59AD446A843265A6FA079ECA1D5FD49C4B3C2496A363C60C5939268BED0B722EFB8BB6787A2B193"

fun File.sha512(): String {
        val md = java.security.MessageDigest.getInstance("SHA-512")
        forEachBlock { buffer, bytesRead ->
                md.update(buffer, 0, bytesRead)
        }
        return BigInteger(1, md.digest()).toString(16).toUpperCase()
}

val checksumDependencyJar: File = buildscript.configurations["classpath"].resolve().first()
val actualSha512 = checksumDependencyJar.sha512()
if (actualSha512 != expectedSha512) {
        throw GradleException(
                """
                Checksum mismatch for $checksumDependencyJar
                Expected: $expectedSha512
                  Actual: $actualSha512
                """.trimIndent()
        )
}

apply(plugin = "com.github.vlsi.checksum-dependency")
