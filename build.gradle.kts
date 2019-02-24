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

import com.github.spotbugs.SpotBugsPlugin
import com.github.spotbugs.SpotBugsTask
import org.ajoberstar.grgit.Grgit
import org.apache.jmeter.buildtools.CrLfSpec
import org.apache.jmeter.buildtools.LineEndings
import org.apache.jmeter.buildtools.filter
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import versions.BuildToolVersions

plugins {
    java
    jmeterbuild.ide
    jmeterbuild.witness
    jacoco
    checkstyle
    id("org.nosphere.apache.rat") version "0.4.0"
    id("com.github.ethankhall.semantic-versioning") version "1.1.0"
    id("com.github.spotbugs") version "1.6.10"
    id("org.sonarqube") version "2.7.1"
    signing
    publishing
    jmeterbuild.release
}

fun Project.boolProp(name: String) =
    findProperty(name)
        ?.takeIf { it is String } // project properties include tasks, extensions, etc, and we want to check
        ?.toString()
        ?.equals("false", ignoreCase = true)?.not()

with(version as io.ehdev.version.Version) {
    major = 5
    minor = 2
    patch = 0
    val releaseProp = boolProp("release")
    releaseBuild = releaseProp ?: false
    val nightlyProp = boolProp("nightly")
    // When "nightly" exists, use it to add abbreviation
    // When "nightly" is missing, add abbreviation for non-release builds
    if (nightlyProp ?: (releaseProp != true)) {
        // Append 7 characters of Git commit id for snapshot version
        val grgit: Grgit by project
        preRelease = grgit.head().abbreviatedId
    }
}

println("Building JMeter $version")

apply(from = "$rootDir/gradle/dependencyVerification.gradle.kts")
apply(from = "$rootDir/gradle/release.gradle.kts")

fun reportsForHumans() = !(System.getenv()["CI"]?.toBoolean() ?: false)

val lastEditYear by extra {
    file("$rootDir/NOTICE")
        .readLines()
        .first { it.contains("Copyright") }
        .let {
            """Copyright \d{4}-(\d{4})""".toRegex()
                .find(it)?.groupValues?.get(1)
                ?: throw IllegalStateException("Unable to identify copyright year from $rootDir/NOTICE")
        }
}

val rat by tasks.getting(org.nosphere.apache.rat.RatTask::class) {
    excludes.set(rootDir.resolve("rat-excludes.txt").readLines())
}

releaseParams {
    previewSiteContents.add(copySpec {
        into("rat")
        from(rat)
    })
}

val jacocoReport by tasks.registering(JacocoReport::class) {
    group = "Coverage reports"
    description = "Generates an aggregate report from all subprojects"
}

val jacocoEnabled by extra {
    (boolProp("coverage") ?: false) || gradle.startParameter.taskNames.any { it.contains("jacoco") }
}

// Do not enable spotbugs by default. Execute it only when -Pspotbugs is present
val enableSpotBugs by extra {
    boolProp("spotbugs") ?: false
}

val skipCheckstyle by extra {
    boolProp("skipCheckstyle") ?: false
}

// Allow to skip building source/binary distributions
val skipDist by extra {
    boolProp("skipDist") ?: false
}

allprojects {
    if (project.path != ":src") {
        tasks.register<DependencyInsightReportTask>("allDepInsight") {
            group = LifecycleBasePlugin.BUILD_GROUP
            description =
                "Shows insights where the dependency is used. For instance: allDepInsight --configuration compile --dependency org.jsoup:jsoup"
        }
    }
}

sonarqube {
    properties {
        // See https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Gradle#AnalyzingwithSonarQubeScannerforGradle-Configureanalysisproperties
        property("sonar.sourceEncoding", "UTF-8")
        val projectName = "JMeter"
        property("sonar.projectName", projectName)
        property("sonar.projectKey", "org.apache.jmeter:$projectName")
        property("sonar.projectVersion", project.version.toString())
        property("sonar.host.url", System.getenv()["SONAR_HOST_URL"] ?: "http://localhost:9000")
        property("sonar.login", System.getenv()["SONAR_LOGIN"] ?: "")
        property("sonar.password", System.getenv()["SONAR_PASSWORD"] ?: "")
        property("sonar.links.homepage", "http://jmeter.apache.org")
        property("sonar.links.ci", "https://builds.apache.org/job/JMeter-trunk/")
        property("sonar.links.scm", "http://jmeter.apache.org/svnindex.html")
        property("sonar.links.issue", "http://jmeter.apache.org/issues.html")
    }
}

allprojects {
    group = "org.apache.jmeter"
    // JMeter ClassFinder parses "class.path" and tries to find jar names there,
    // so we should produce jars without versions names for now
    // version = rootProject.version
    plugins.withType<JavaPlugin> {
        apply<IdeaPlugin>()
        apply<EclipsePlugin>()
        if (!skipCheckstyle) {
            apply<CheckstylePlugin>()
            checkstyle {
                toolVersion = BuildToolVersions.checkstyle
            }
        }
        apply<SigningPlugin>()
        apply<SpotBugsPlugin>()

        spotbugs {
            toolVersion = BuildToolVersions.spotbugs
        }
    }

    plugins.withType<JacocoPlugin> {
        the<JacocoPluginExtension>().toolVersion = BuildToolVersions.jacoco

        val testTasks = tasks.withType<Test>()
        val javaExecTasks = tasks.withType<JavaExec>()
        // This configuration must be postponed since JacocoTaskExtension might be added inside
        // configure block of a task (== before this code is run). See :src:dist-check:createBatchTask
        afterEvaluate {
            for (t in arrayOf(testTasks, javaExecTasks)) {
                t.configureEach {
                    extensions.findByType<JacocoTaskExtension>()?.apply {
                        // Do not collect coverage when not asked (e.g. via jacocoReport or -Pcoverage)
                        isEnabled = jacocoEnabled
                        // We don't want to collect coverage for third-party classes
                        includes?.add("org.apache.jmeter.*")
                        includes?.add("org.apache.jorphan.*")
                        includes?.add("org.apache.commons.cli.*")
                    }
                }
            }
        }

        jacocoReport {
            // Note: this creates a lazy collection
            // Some of the projects might fail to create a file (e.g. no tests or no coverage),
            // So we check for file existence. Otherwise JacocoMerge would fail
            val execFiles =
                files(testTasks, javaExecTasks).filter { it.exists() && it.name.endsWith(".exec") }
            executionData(execFiles)
        }

        tasks.withType<JacocoReport>().configureEach {
            reports {
                html.isEnabled = reportsForHumans()
                xml.isEnabled = !reportsForHumans()
            }
        }
        // Add each project to combined report
        configure<SourceSetContainer> {
            val mainCode = main.get()
            jacocoReport.configure {
                additionalSourceDirs.from(mainCode.allJava.srcDirs)
                sourceDirectories.from(mainCode.allSource.srcDirs)
                // IllegalStateException: Can't add different class with same name: module-info
                // https://github.com/jacoco/jacoco/issues/858
                classDirectories.from(mainCode.output.asFileTree.matching {
                    exclude("module-info.class")
                })
            }
        }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        // Ensure builds are reproducible
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        dirMode = "775".toInt(8)
        fileMode = "664".toInt(8)
    }

    // Not all the modules use publishing plugin
    plugins.withType<PublishingPlugin> {
        // Sign all the published artifacts
        signing {
            sign(publishing.publications)
        }
    }

    plugins.withType<JavaPlugin> {
        // This block is executed right after `java` plugin is added to a project

        repositories {
            jcenter()
            ivy {
                url = uri("https://github.com/bulenkov/Darcula/raw/")
                content {
                    includeModule("com.github.bulenkov.darcula", "darcula")
                }
                patternLayout {
                    artifact("[revision]/build/[module].[ext]")
                }
                metadataSources {
                    artifact() // == don't try downloading .pom file from the repository
                }
            }
        }

        tasks {
            withType<JavaCompile>().configureEach {
                options.encoding = "UTF-8"
            }
            withType<ProcessResources>().configureEach {
                from(source) {
                    include("**/*.properties")
                    filteringCharset = "UTF-8"
                    // apply native2ascii conversion since Java 8 expects properties to have ascii symbols only
                    filter(org.apache.tools.ant.filters.EscapeUnicode::class)
                    filter(LineEndings.LF)
                }
                // Text-like resources are normalized to LF (just for consistency purposes)
                // This makes to produce exactly the same jar files no matter which OS is used for the build
                from(source) {
                    include("**/*.dtd")
                    include("**/*.svg")
                    include("**/*.txt")
                    // Test resources have files in CP1252, and we don't want to parse them as UTF-8
                    exclude("**/*cp1252*")
                    filteringCharset = "UTF-8"
                    filter(LineEndings.LF)
                }
            }
            afterEvaluate {
                // Add default license/notice when missing (e.g. see :src:config that overrides LICENSE)
                withType<Jar>().configureEach {
                    CrLfSpec(LineEndings.LF).run {
                        into("META-INF") {
                            filteringCharset = "UTF-8"
                            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                            textFrom(rootDir) {
                                text("LICENSE")
                                text("NOTICE")
                            }
                        }
                    }
                }
            }
            withType<Jar>().configureEach {
                manifest {
                    attributes["Specification-Title"] = "Apache JMeter"
                    attributes["Specification-Vendor"] = "Apache Software Foundation"
                    attributes["Implementation-Vendor"] = "Apache Software Foundation"
                    attributes["Implementation-Vendor-Id"] = "org.apache"
                    attributes["Implementation-Version"] = rootProject.version
                }
            }
            withType<Test>().configureEach {
                testLogging {
                    exceptionFormat = TestExceptionFormat.FULL
                }
                // Pass the property to tests
                systemProperty("java.awt.headless", System.getProperty("java.awt.headless"))
            }
            withType<SpotBugsTask>().configureEach {
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                if (enableSpotBugs) {
                    description = "$description (skipped by default, to enable it add -Dspotbugs)"
                }
                reports {
                    html.isEnabled = reportsForHumans()
                    xml.isEnabled = !reportsForHumans()
                }
                enabled = enableSpotBugs
            }
            withType<Javadoc>().configureEach {
                (options as StandardJavadocDocletOptions).apply {
                    noTimestamp.value = true
                    showFromProtected()
                    docEncoding = "UTF-8"
                    charSet = "UTF-8"
                    encoding = "UTF-8"
                    docTitle = "Apache JMeter ${project.name} API"
                    windowTitle = "Apache JMeter ${project.name} API"
                    header = "<b>Apache JMeter</b>"
                    bottom =
                        "Copyright © 1998-$lastEditYear Apache Software Foundation. All Rights Reserved."
                    links("https://docs.oracle.com/javase/8/docs/api/")
                }
            }
        }

        configure<JavaPluginConvention> {
            sourceCompatibility = JavaVersion.VERSION_1_8
        }
    }
}
