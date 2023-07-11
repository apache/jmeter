import groovy.util.Node
import groovy.util.NodeList
import java.lang.IllegalStateException

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
    id("build-logic.build-params")
    id("build-logic.publish-to-tmp-maven-repo")
}

val repoUrl = "https://github.com/apache/jmeter"

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (buildParameters.suppressPomMetadataWarnings) {
            suppressPomMetadataWarningsFor("testFixturesApiElements")
            suppressPomMetadataWarningsFor("testFixturesRuntimeElements")
        }
        // Use the resolved versions in pom.xml
        // Gradle might have different resolution rules, so we set the versions
        // that were used in Gradle build/test.
        versionMapping {
            usage(Usage.JAVA_RUNTIME) {
                fromResolutionResult()
            }
        }
        plugins.withId("java") {
            versionMapping {
                usage(Usage.JAVA_API) {
                    fromResolutionOf("runtimeClasspath")
                }
            }
        }
        pom {
            withXml {
                val pom = asNode()
                // Maven does not support dependencyManagement, so remove it anyway
                for (dependencyManagement in (pom["dependencyManagement"] as NodeList)) {
                    pom.remove(dependencyManagement as Node)
                }
                // Gradle maps test fixtures to optional=true, so we remove those elements form the POM to avoid
                // confusion
                // See https://github.com/gradle/gradle/issues/14936
                // See https://github.com/apache/jmeter/issues/6030
                val dependencies = pom["dependencies"] as NodeList
                for (dependenciesNode in dependencies) {
                    dependenciesNode as Node
                    for (dependency in (dependenciesNode["dependency"] as NodeList)) {
                        dependency as Node
                        if ((dependency["optional"] as NodeList).firstOrNull()?.let { it as? Node }
                                ?.text() == "true") {
                            dependenciesNode.remove(dependency)
                            continue
                        }
                        if ((dependency["version"] as NodeList).isEmpty()) {
                            throw IllegalStateException("Generated pom.xml contains a dependency without <version> for dependency ${dependency}. It will cause issues with Maven resolution, see https://github.com/apache/jmeter/issues/6041")
                        }
                    }
                }
            }
            name.set("Apache JMeter ${project.name.replaceFirstChar { it.titlecaseChar() }}")
            // This code might be executed before project-related build.gradle.kts is evaluated
            // So we delay access to project.description
            description.set(
                project.provider { project.description }
            )
            inceptionYear.set("1098")
            url.set("https://jmeter.apache.org/")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    comments.set("A business-friendly OSS license")
                }
            }
            issueManagement {
                system.set("GitHub Issues")
                url.set("$repoUrl/issues")
            }
            scm {
                connection.set("scm:git:$repoUrl.git")
                developerConnection.set("scm:git:$repoUrl.git")
                url.set(repoUrl)
                tag.set("HEAD")
            }
        }
    }
}
