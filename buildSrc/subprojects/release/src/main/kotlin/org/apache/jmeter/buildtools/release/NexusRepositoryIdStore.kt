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

import org.gradle.api.Project
import java.util.concurrent.ConcurrentHashMap

class NexusRepositoryIdStore(private val project: Project) {
    private val savedIds = ConcurrentHashMap<String, String>()

    private fun storeDir() = "${project.buildDir}/stagingRepositories"

    private fun filePath(repositoryName: String) = "${storeDir()}/$repositoryName.txt"

    operator fun get(name: String) = savedIds[name]

    operator fun set(name: String, id: String) {
        if (savedIds.putIfAbsent(name, id) == null) {
            project.logger.info("Saving stagingRepositoryId for repository $name -> $id")
            val file = project.file(filePath(name))
            file.parentFile.mkdirs()
            file.writeText(id)
        }
    }

    fun load() {
        for (f in project.file(storeDir()).listFiles({ f -> f.name.endsWith("*.txt") })) {
            savedIds[f.name.removeSuffix(".txt")] = f.readText()
        }
    }
}