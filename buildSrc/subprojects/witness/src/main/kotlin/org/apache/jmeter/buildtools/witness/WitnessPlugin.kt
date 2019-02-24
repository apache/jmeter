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

package org.apache.jmeter.buildtools.witness

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.kotlin.dsl.the
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

class WitnessPlugin @Inject constructor(val workerExecutor: WorkerExecutor) : Plugin<Project> {
    companion object {
        const val VERIFY_CHECKSUMS_TASK_NAME = "verifyChecksums"
        const val CALCULATE_CHECKSUMS_TASK_NAME = "calculateChecksums"
    }

    override fun apply(project: Project) {
        if (project.parent != null) {
            throw IllegalArgumentException("witness plugin should be applied to the root project only")
        }

        project.extensions.create("dependencyVerification", WitnessExtension::class.java, project)

        project.tasks.register(VERIFY_CHECKSUMS_TASK_NAME) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Verifies checksums of the project dependencies"
            // Avoid task caching just in case
            outputs.upToDateWhen { false }
            doLast {
                val allDependencies = project.collectFiles()
                val verification = project.the<WitnessExtension>()
                val allHashes = verification.hashes.keys.toMutableSet()
                for (dep in allDependencies) {
                    allHashes.remove(dep.key)
                    workerExecutor.submit(CalculateSHA512::class.java) {
                        displayName = "validate checksum for ${dep.key.dependencyNotation}"
                        isolationMode = IsolationMode.NONE
                        params(dep.value.uses.toString(), dep.value.file, verification.hashes[dep.key] ?: "")
                    }
                }
                workerExecutor.await()
                if (allHashes.isEmpty()) {
                    println(
                        "The following artifacts are not longer present in project dependencies," +
                                " however the checksums are registered: ${allHashes.sorted().joinToString(
                                    "\n"
                                )}"
                    )
                }
            }
        }
        project.tasks.register(CALCULATE_CHECKSUMS_TASK_NAME) {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Calculates and prints the checksums of the project dependencies"
            // Avoid task caching just in case
            outputs.upToDateWhen { false }
            doLast {
                val allDependencies = project.collectFiles()
                val hashSums = allDependencies
                    .entries
                    .parallelStream()
                    .map({ e -> e.key.dependencyNotation to e.value.file.sha512() })
                    .sorted(Comparator.comparing({ e -> e.first.toLowerCase(Locale.ROOT) }))
                    .map({ e -> """sha256("${e.first}", "${e.second}")""" })
                    .collect(Collectors.joining("\n"))
                println(hashSums)
            }
        }
    }
}

data class DependencyUse(val projectPath: String, val configuration: String) {
    override fun toString() = "$projectPath/$configuration"
}

data class DependencyAndProject(val file: File, val uses: MutableSet<DependencyUse> = mutableSetOf())

private fun Project.collectFiles(): Map<DependencyKey, DependencyAndProject> {
    val result = sortedMapOf<DependencyKey, DependencyAndProject>()

    for (p: Project in allprojects
             .filter { !it.repositories.isEmpty() }
    )   {
        for(conf in p.let { it.buildscript.configurations + it.configurations }
                .filter { it.isCanBeResolved }) {
            for (art in conf.resolvedConfiguration.resolvedArtifacts) {
                val compId = art.id.componentIdentifier
                if (compId is ModuleComponentIdentifier) {
                    val dependencyKey = DependencyKey(
                        compId,
                        art.classifier,
                        art.extension
                    )
                    result.computeIfAbsent(dependencyKey) {
                        DependencyAndProject(
                            art.file
                        )
                    }
                        .uses.add(
                        DependencyUse(
                            p.path,
                            conf.name
                        )
                    )
                }
            }
        }
    }
    return result
}

private fun File.sha512(): String {
    val md = MessageDigest.getInstance("SHA-512")
    val buf = ByteArray(4096)
    this.inputStream().use {
        while (true) {
            val read = it.read(buf)
            if (read == -1) {
                break
            }
            md.update(buf, 0, read)
        }
    }
    return BigInteger(1, md.digest()).toString(16).toUpperCase()
}

class CalculateSHA512 @Inject constructor(
    private val uses: String,
    private val file: File,
    private val expectedHash: String
) : Runnable {
    override fun run() {
        val actualHash = file.sha512()
        if (expectedHash.isBlank()) {
            throw HashMissingException(uses, file, actualHash)
        }
        if (expectedHash != actualHash) {
            throw HashMismatchException(
                uses,
                file,
                actualHash = actualHash,
                expectedHash = expectedHash
            )
        }
    }
}

class HashMissingException(uses: String, file: File, actualHash: String) :
    java.lang.RuntimeException("Was not specified, current hash is $actualHash, file: $file, used in projects: $uses")

class HashMismatchException(uses: String, file: File, actualHash: String, expectedHash: String) :
    java.lang.RuntimeException("Hash mismatch. Expected: $expectedHash, actual: $actualHash, file: $file, used in projects: $uses")
