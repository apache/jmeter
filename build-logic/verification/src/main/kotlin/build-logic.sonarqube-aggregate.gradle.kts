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

import com.github.spotbugs.snom.SpotBugsTask
import jacoco.JacocoAggregateReportSpec

plugins {
    `reporting-base`
    id("build-logic.build-params")
    id("org.sonarqube")
}

sonar {
    properties {
        // See https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Gradle#AnalyzingwithSonarQubeScannerforGradle-Configureanalysisproperties
        property("sonar.sourceEncoding", "UTF-8")
        val projectName = "JMeter"
        property("sonar.projectName", projectName)
        property("sonar.projectKey", System.getenv()["SONAR_PROJECT_KEY"] ?: projectName)
        property("sonar.organization", System.getenv()["SONAR_ORGANIZATION"] ?: "apache")
        property("sonar.projectVersion", project.version.toString())
        property("sonar.host.url", System.getenv()["SONAR_HOST_URL"] ?: "http://localhost:9000")
        property("sonar.login", System.getenv()["SONAR_LOGIN"] ?: "")
        property("sonar.password", System.getenv()["SONAR_PASSWORD"] ?: "")
        property("sonar.links.homepage", "https://jmeter.apache.org")
        property("sonar.links.ci", "https://builds.apache.org/job/JMeter-trunk/")
        property("sonar.links.scm", "https://jmeter.apache.org/svnindex.html")
        property("sonar.links.issue", "https://jmeter.apache.org/issues.html")
    }
}

val sonarTask = tasks.sonar

plugins.withId("build-params.jacoco-aggregation") {
    val coverageReportTask =
        reporting.reports.named<JacocoAggregateReportSpec>(JacocoAggregateReportSpec.CODE_COVERAGE_REPORT_NAME)
            .flatMap { it.reportTask }
    val mergedCoverage =
        coverageReportTask
            .map { it.reports.xml.outputLocation }
    sonarTask {
        dependsOn(coverageReportTask)
    }

    subprojects {
        plugins.withId("java-base") {
            sonar {
                properties {
                    property("sonar.coverage.jacoco.xmlReportPaths", mergedCoverage)
                }
            }
        }
    }
}

subprojects {
    plugins.withId("com.github.spotbugs") {
        val spotBugTasks = tasks.withType<SpotBugsTask>().matching {
            // We don't send spotbugs for test classes
            !it.name.endsWith("Test")
        }
        sonarTask {
            dependsOn(spotBugTasks)
        }
        sonar {
            properties {
                property(
                    "sonar.java.spotbugs.reportPaths",
                    spotBugTasks.asSequence()
                        .map {
                            it.reports.named("XML").get().outputLocation.asFile.get()
                        }
                )
            }
        }
    }
}
