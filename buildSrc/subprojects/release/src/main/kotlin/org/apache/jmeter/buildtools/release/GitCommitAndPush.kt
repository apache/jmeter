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

import org.apache.jmeter.buildtools.jgit.dsl.add
import org.apache.jmeter.buildtools.jgit.dsl.commit
import org.apache.jmeter.buildtools.jgit.dsl.push
import org.apache.jmeter.buildtools.jgit.dsl.setCredentials
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.EmptyCommitException
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File

abstract class GitCommitAndPush : DefaultTask() {
    @Input
    val repository = project.objects.property<GitConfig>()
    @Input
    val commitMessage = project.objects.property<String>()

    @TaskAction
    fun execute() {
        val repo = repository.get()
        val repoDir = File(project.buildDir, repo.name)
        Git.open(repoDir).use {
            it.add {
                // Add new files
                addFilepattern(".")
            }
            it.add {
                // Remove removed files
                addFilepattern(".")
                setUpdate(true)
            }
            try {
                it.commit {
                    setMessage(commitMessage.get())
                    setAllowEmpty(false)
                }
                println("Pushing ${repo.name} to $repo")
                it.push {
                    setCredentials(repo)
                    setRemote(repo.remote.get())
                }
            } catch (e: EmptyCommitException) {
                println("Nothing to push for ${repo.name}, $repo is up to date")
            }
        }
    }
}
