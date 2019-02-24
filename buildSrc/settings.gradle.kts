import org.gradle.api.internal.artifacts.result.DefaultResolvedDependencyResult
import java.security.MessageDigest
import java.util.*

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

include("batchtest")

val upperCaseLetters = "\\p{Upper}".toRegex()

fun String.toKebabCase() =
    replace(upperCaseLetters) { "-${it.value.toLowerCase()}" }

fun buildFileNameFor(projectDirName: String) =
    "$projectDirName.gradle.kts"

for (project in rootProject.children) {
    val projectDirName = project.name.toKebabCase()
    project.projectDir = file("subprojects/$projectDirName")
    project.buildFileName = buildFileNameFor(projectDirName)
    assert(project.projectDir.isDirectory)
    assert(project.buildFile.isFile)
}

buildscript {
    dependencies {
        classpath("com.github.vlsi.gradle:checksum-dependency-plugin:1.19.0")
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
//
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
