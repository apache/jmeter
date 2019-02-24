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
import org.gradle.api.file.CopySpec
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.*
import java.net.URI
import javax.inject.Inject

/**
 * Setting up local release environment:
 *
 * ```
 * git clone https://github.com/vlsi/asflike-release-environment.git
 * cd asflike-release-environment && docker-compose up
 * ```
 */
open class ReleaseExtension @Inject constructor(
    private val project: Project,
    private val objects: ObjectFactory
) {
    internal val repositoryIdStore = NexusRepositoryIdStore(project)

    val repositoryType = objects.property<RepositoryType>()
        .convention(RepositoryType.TEST)

    val prefixForProperties = objects.property<String>().convention("asf")
    val prefix = prefixForProperties.map {
        it + when (repositoryType.get()) {
            RepositoryType.PROD -> ""
            RepositoryType.TEST -> "Test"
        }
    }

    val tlp = objects.property<String>()
    val tlpUrl
        get() = tlp.get().toLowerCase()
    val voteText = objects.property<(ReleaseParams) -> String>()
    val tag = objects.property<String>()
        .convention(project.provider { "v${project.version}" })

    val archives = objects.listProperty<Any>()
    val previewSiteContents = objects.listProperty<CopySpec>()

    val svnDist = objects.newInstance<SvnDistConfig>(this, project)
    fun svnDist(action: SvnDistConfig.() -> Unit) = svnDist.action()

    val nexus = objects.newInstance<NexusConfig>(this, project)
    fun nexus(action: NexusConfig.() -> Unit) = nexus.action()

    private val git = project.container<GitConfig> {
        objects.newInstance(it, this)
    }

    private fun GitConfig.gitUrlConvention(suffix: String = "") {
        urls.convention(repositoryType.map {
            when (it) {
                RepositoryType.PROD -> GitHub("apache", "$tlpUrl$suffix")
                RepositoryType.TEST -> GitDaemon("127.0.0.1", "$tlpUrl$suffix")
            }
        })
    }

    val source by git.registering {
        branch.convention("master")
        gitUrlConvention()
    }

    val sitePreview by git.registering {
        branch.convention("asf-site")
        gitUrlConvention("-site")
    }

    val site by git.registering {
        branch.convention("asf-site")
        gitUrlConvention("-preview")
    }
}

private fun ReleaseExtension.defaultValue(property: String) = prefix.map { it + property }

open class SvnDistConfig @Inject constructor(
    private val ext: ReleaseExtension,
    private val project: Project,
    private val objects: ObjectFactory
) {
    val credentials = objects.newInstance<Credentials>("asfSvn", ext)

    val url = objects.property<URI>()
        .convention(
            ext.repositoryType.map {
                when (it) {
                    RepositoryType.PROD -> project.uri("https://dist.apache.org/repos/dist")
                    RepositoryType.TEST -> project.uri("http://127.0.0.1/svn/dist")
                }
            })

    val stageFolder = objects.property<String>()
        .convention(project.provider {
            "dev/${ext.tlpUrl}/${ext.tag.get()}"
        })

    val finalFolder = objects.property<String>()
        .convention(project.provider {
            "release/${ext.tlpUrl}"
        })

    val releaseSubfolder = objects.mapProperty<Regex, String>()
}

open class NexusConfig @Inject constructor(
    private val ext: ReleaseExtension,
    private val project: Project,
    objects: ObjectFactory
) {
    val url = objects.property<URI>()
        .convention(
            ext.repositoryType.map {
                when (it) {
                    RepositoryType.PROD -> project.uri("https://repository.apache.org")
                    RepositoryType.TEST -> project.uri("http://127.0.0.1:8080")
                }
            })

    val credentials = objects.newInstance<Credentials>("Nexus", ext)

    val packageGroup = objects.property<String>().convention(
        project.provider {
            project.group.toString()
        })
    val stagingProfileId = objects.property<String>()
}

open class GitConfig @Inject constructor(
    val name: String,
    private val ext: ReleaseExtension,
    objects: ObjectFactory
) {
    val urls = objects.property<GitUrlConventions>()
    val remote = objects.property<String>()
        .convention(ext.repositoryType.map {
            when (it) {
                RepositoryType.PROD -> "origin"
                RepositoryType.TEST -> "origin-test"
            }
        })
    val branch = objects.property<String>()

    val credentials = objects.newInstance<Credentials>(name.capitalize(), ext)

    override fun toString() = "${urls.get().pushUrl}, branch: ${branch.get()}"
}

open class Credentials @Inject constructor(
    val name: String,
    private val ext: ReleaseExtension,
    objects: ObjectFactory
) {
    operator fun invoke(action: Credentials.() -> Unit) = apply { action() }

    val username = objects.property<String>()
        .convention(ext.defaultValue("${name}Username"))

    val password = objects.property<String>()
        .convention(ext.defaultValue("${name}Password"))
}

class ReleaseArtifact(
    val name: String,
    val sha512: String
)

class ReleaseParams(
    val tlp: String,
    val version: String,
    val gitSha: String,
    val tag: String,
    val artifacts: List<ReleaseArtifact>,
    val svnStagingUri: URI,
    val nexusRepositoryUri: URI,
    val previewSiteUri: URI,
    val sourceCodeTagUrl: URI
) {
    val shortGitSha
        get() = gitSha.subSequence(0, 10)

    val tlpUrl
        get() = tlp.toLowerCase()
}

enum class RepositoryType {
    PROD, TEST
}
