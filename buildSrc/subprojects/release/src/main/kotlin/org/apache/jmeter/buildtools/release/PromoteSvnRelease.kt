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

import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.the
import org.gradle.work.InputChanges

abstract class PromoteSvnRelease : SvnmuccTask() {
    @InputFiles
    @PathSensitive(PathSensitivity.NAME_ONLY)
    val files = project.files()

    init {
        outputs.upToDateWhen { false }
    }

    override fun message() =
        project.the<ReleaseExtension>().run {
            "Promoting release candidate ${tlp.get()} ${tag.get()} to release area"
        }

    override fun operations(inputChanges: InputChanges): List<SvnOperation> {
        return mutableListOf<SvnOperation>().apply {
            val ext = project.the<ReleaseExtension>()
            val svnDist = ext.svnDist
            val stageFolder = svnDist.stageFolder.get()
            val releaseFolder = svnDist.finalFolder.get()

            val subfolders = svnDist.releaseSubfolder.get()
            for (f in files) {
                val stagedFile = "$stageFolder/${f.name}"
                val subfolder = subfolders.entries.firstOrNull { f.name.contains(it.key) }?.value
                val releasedFile =
                    "$releaseFolder/${if (subfolder.isNullOrEmpty()) "" else "$subfolder/"}${f.name}"
                for (fileExt in listOf("", ".sha512")) {
                    add(SvnMv(stagedFile + fileExt, releasedFile + fileExt))
                }
            }
            add(SvnRm(stageFolder))
        }
    }
}
