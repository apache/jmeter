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

import com.github.vlsi.gradle.properties.dsl.lastEditYear
import com.github.vlsi.gradle.release.RepositoryType
import org.ajoberstar.grgit.Grgit

plugins {
    id("build-logic.root-build")
    id("com.github.vlsi.stage-vote-release")
}

fun Project.boolProp(name: String) =
    findProperty(name)
        // Project properties include tasks, extensions, etc, and we want only String properties
        // We don't want to use "task" as a boolean property
        ?.let { it as? String }
        ?.equals("false", ignoreCase = true)?.not()

// Release candidate index
val String.v: String get() = rootProject.extra["$this.version"] as String
version = "jmeter".v + releaseParams.snapshotSuffix

allprojects {
    group = "org.apache.jmeter"
    version = rootProject.version
}

val platformProjects by extra {
    setOf(
        projects.src.bom,
        projects.src.bomThirdparty,
    ).mapTo(mutableSetOf()) { it.dependencyProject }
}

val notPublishedProjects by extra {
    listOf(
        projects.jmeter,
        projects.src,
        projects.src.bshclient,
        projects.src.dist,
        projects.src.distCheck,
        projects.src.examples,
        projects.src.generator,
        projects.src.licenses,
        projects.src.protocol,
        projects.src.release,
        projects.src.testkit,
        projects.src.testkitWiremock,
    ).mapTo(mutableSetOf()) { it.dependencyProject }
}

val publishedProjects by extra {
    allprojects - notPublishedProjects
}

notPublishedProjects.forEach { project ->
    if (project != rootProject) {
        project.plugins.withId("maven-publish") {
            throw IllegalStateException(
                "Project ${project.path} is listed in notPublishedProjects, however it has maven-publish plugin applied. " +
                    "Please remove maven-publish plugin (e.g. replace build-logic.jvm-published-library with build-logic.jvm-library) or " +
                    "move the project to the list of published ones"
            )
        }
    }
}

publishedProjects.forEach {project ->
    project.afterEvaluate {
        if (!pluginManager.hasPlugin("maven-publish")) {
            throw IllegalStateException(
                "Project ${project.path} is listed in publishedProjects, however it misses maven-publish plugin. " +
                    "Please add maven-publish plugin (e.g. replace build-logic.jvm-library with build-logic.jvm-published-library) or " +
                    "move the project to the list of published ones"
            )
        }
    }
}

val displayVersion by extra {
    version.toString() +
        if (releaseParams.release.get()) {
            ""
        } else {
            // Append 7 characters of Git commit id for snapshot version
            val grgit: Grgit? by project
            grgit?.let { " " + it.head().abbreviatedId }
        }
}

println("Building JMeter $version")

fun reportsForHumans() = !(System.getenv()["CI"]?.toBoolean() ?: boolProp("CI") ?: false)

val lastEditYear by extra(lastEditYear().toString())


tasks.validateBeforeBuildingReleaseArtifacts {
    dependsOn(tasks.rat)
}

releaseArtifacts {
    fromProject(projects.src.dist.dependencyProject.path)
    previewSite {
        into("rat")
        from(tasks.rat) {
            filteringCharset = "UTF-8"
            // XML is not really interesting for now
            exclude("rat-report.xml")
            // RAT reports have absolute paths, and we don't want to expose them
            filter { str: String -> str.replace(rootDir.absolutePath, "") }
        }
    }
}

releaseParams {
    tlp.set("JMeter")
    releaseTag.set("rel/v${project.version}")
    rcTag.set(rc.map { "v${project.version}-rc$it" })
    svnDist {
        // All the release versions are put under release/jmeter/{source,binary}
        releaseFolder.set("release/jmeter")
        releaseSubfolder.apply {
            put(Regex("_src\\."), "source")
            put(Regex("."), "binaries")
        }
        staleRemovalFilters {
            excludes.add(Regex("release/.*/HEADER\\.html"))
        }
    }
    nexus {
        if (repositoryType.get() == RepositoryType.PROD) {
            // org.apache.jmeter at repository.apache.org
            stagingProfileId.set("4d29c092016673")
        }
    }
}
