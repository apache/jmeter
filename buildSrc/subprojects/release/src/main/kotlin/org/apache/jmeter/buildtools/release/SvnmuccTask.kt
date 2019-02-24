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
package org.apache.jmeter.buildtools.release

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.the
import org.gradle.process.ExecSpec
import org.gradle.work.InputChanges
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import javax.inject.Inject

abstract class SvnmuccTask @Inject constructor() : DefaultTask() {
    @Input
    var repository = project.objects.property<URI>()
        .convention(project.provider {
            project.the<ReleaseExtension>().svnDist.url.get()
        })

    abstract fun operations(inputChanges: InputChanges): List<SvnOperation>
    abstract fun message(): String

    private fun ExecSpec.svnCredentials() {
        args("--username", "test")
        args("--password", "test")
    }

    fun exists(path: String): Boolean {
        val os = ByteArrayOutputStream()
        val absolutePath = "${repository.get()}/$path"
        val result = project.exec {
            workingDir = project.projectDir
            commandLine("svn", "ls", "--depth", "empty", absolutePath)
            svnCredentials()
            isIgnoreExitValue = true
            errorOutput = os
        }
        if (result.exitValue == 0) {
            project.logger.debug("Directory {} exists in SVN", absolutePath)
            return true
        }

        val message = os.toString() // Default encoding is expected
        if (message.contains("E200009")) {
            // E200009: Could not list all targets because some targets don't exist
            project.logger.debug("Directory {} does not exist in SVN", absolutePath)
        } else {
            project.logger.warn("Unable to check existence of {}. Error: {}", absolutePath, message)
        }
        return false
    }

    @TaskAction
    fun mucc(inputChanges: InputChanges) {
        println(
            if (inputChanges.isIncremental) "Executing incrementally"
            else "Executing non-incrementally"
        )

        val parentFiles = ParentFilesCollector()
        val muccOps = operations(inputChanges)
        for (o in muccOps) {
            val fileName = when (o) {
                is SvnPut -> o.destination
                is SvnMv -> o.destination
                is SvnMkdir -> o.path + "/tmp"
                else -> null
            }
            fileName?.let { parentFiles.add(it) }
        }

        // Create relevant parent directories first, then put files
        // Note all SvnMkdirs are served via parentFiles, so we skip mkdir from muccOps
        val commands = parentFiles.parents
            .asSequence()
            .filterNot { exists(it) }
            .map { SvnMkdir(it) }
            .plus(muccOps.filter { it !is SvnMkdir })
            .map(SvnOperation::toSvn)
            .joinToString("\n")

        val commandsFile = project.file("${project.buildDir}/svnmucc/$name.txt")
        commandsFile.parentFile.mkdir()
        commandsFile.writeText(commands)

        project.exec {
            workingDir = project.projectDir
            commandLine("svnmucc", "--non-interactive", "--root-url", repository.get())
            svnCredentials()
            args("--extra-args", commandsFile)
            args("--message", message())
            standardOutput = System.out
        }
    }
}

sealed class SvnOperation {
    abstract fun toSvn(): String
}

data class SvnMkdir(val path: String) : SvnOperation() {
    override fun toSvn() = "mkdir\n$path"
}

data class SvnPut(val file: File, val destination: String) : SvnOperation() {
    override fun toSvn() = "put\n$file\n$destination"
}

data class SvnMv(val source: String, val destination: String) : SvnOperation() {
    override fun toSvn() = "mv\n$source\n$destination"
}

data class SvnRm(val path: String) : SvnOperation() {
    override fun toSvn() = "rm\n$path"
}
