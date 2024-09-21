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

plugins {
    id("maven-publish")
}

val localRepoElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    description =
        "Shares local maven repository directory that contains the artifacts produced by the current project"
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named("maven-repository"))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
    }
}

val localRepoDir = layout.buildDirectory.dir("local-maven-repo")

publishing {
    repositories {
        maven {
            name = "tmp-maven"
            url = uri(localRepoDir)
        }
    }
}

localRepoElements.outgoing.artifact(localRepoDir) {
    builtBy(tasks.named("publishAllPublicationsToTmp-mavenRepository"))
}

val cleanLocalRepository by tasks.registering(Delete::class) {
    description = "Clears local-maven-repo so timestamp-based snapshot artifacts do not consume space"
    delete(localRepoDir)
}

tasks.withType<PublishToMavenRepository>()
    .matching { it.name.endsWith("PublicationToTmp-mavenRepository") }
    .configureEach {
        dependsOn(cleanLocalRepository)
    }
