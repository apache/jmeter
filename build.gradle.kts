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
import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.crlf.filter
import com.github.vlsi.gradle.git.FindGitAttributes
import com.github.vlsi.gradle.git.dsl.gitignore
import com.github.vlsi.gradle.release.RepositoryType
import org.ajoberstar.grgit.Grgit
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.sonarqube.gradle.SonarQubeProperties

plugins {
    java
    jacoco
    checkstyle
    id("org.jetbrains.gradle.plugin.idea-ext") version "0.5" apply false
    id("org.nosphere.apache.rat") version "0.5.2"
    id("com.diffplug.gradle.spotless") version "3.24.3"
    id("com.github.spotbugs") version "2.0.0"
    id("org.sonarqube") version "2.7.1"
    id("com.github.vlsi.crlf") version "1.33.0"
    id("com.github.vlsi.ide") version "1.33.0"
    id("com.github.vlsi.stage-vote-release") version "1.33.0"
    signing
    publishing
}

ide {
    copyrightToAsf()
    ideaInstructionsUri =
        uri("https://github.com/apache/jmeter/blob/master/CONTRIBUTING.md#intellij")
    doNotDetectFrameworks("android", "jruby")
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

// This task scans the project for gitignore / gitattributes, and that is reused for building
// source/binary artifacts with the appropriate eol/executable file flags
// It enables to automatically exclude patterns from .gitignore
val gitProps by tasks.registering(FindGitAttributes::class) {
    // Scanning for .gitignore and .gitattributes files in a task avoids doing that
    // when distribution build is not required (e.g. code is just compiled)
    root.set(rootDir)
}

val rat by tasks.getting(org.nosphere.apache.rat.RatTask::class) {
    gitignore(gitProps)
    // Note: patterns are in non-standard syntax for RAT, so we use exclude(..) instead of excludeFile
    exclude(rootDir.resolve("rat-excludes.txt").readLines())
}

releaseArtifacts {
    fromProject(":src:dist")
    previewSite {
        into("rat")
        from(rat) {
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
            put(Regex("_src\\."), "sources")
            put(Regex("."), "binaries")
        }
    }
    nexus {
        if (repositoryType.get() == RepositoryType.PROD) {
            // org.apache.jmeter at repository.apache.org
            stagingProfileId.set("4d29c092016673")
        }
    }
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

val ignoreSpotBugsFailures by extra {
    boolProp("ignoreSpotBugsFailures") ?: false
}

val skipCheckstyle by extra {
    boolProp("skipCheckstyle") ?: false
}

val skipSpotless by extra {
    boolProp("skipSpotless") ?: false
}

// Allow to skip building source/binary distributions
val skipDist by extra {
    boolProp("skipDist") ?: false
}

// By default use Java implementation to sign artifacts
// When useGpgCmd=true, then gpg command line tool is used for signing
val useGpgCmd by extra {
    boolProp("useGpgCmd") ?: false
}

// Signing is required for RELEASE version
val skipSigning by extra {
    boolProp("skipSigning") ?: boolProp("skipSign") ?: false
}

allprojects {
    if (project.path != ":src") {
        tasks.register<DependencyInsightReportTask>("allDependencyInsight") {
            group = HelpTasksPlugin.HELP_GROUP
            description =
                "Shows insights where the dependency is used. For instance: allDependencyInsight --configuration compile --dependency org.jsoup:jsoup"
        }
    }
}

sonarqube {
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

fun SonarQubeProperties.add(name: String, value: String) {
    properties.getOrPut(name) { mutableSetOf<String>() }
        .also {
            @Suppress("UNCHECKED_CAST")
            (it as MutableCollection<String>).add(value)
        }
}

if (jacocoEnabled) {
    val mergedCoverage = jacocoReport.get().reports.xml.destination.toString()

    // For every module we pass merged coverage report
    // That enables to see ":src:core" lines covered even in case they are covered from
    // "batch tests"
    subprojects {
        if (File(projectDir, "src/main").exists()) {
            apply(plugin = "org.sonarqube")
            sonarqube {
                properties {
                    property("sonar.coverage.jacoco.xmlReportPaths", mergedCoverage)
                }
            }
        }
    }

    tasks.sonarqube {
        dependsOn(jacocoReport)
    }
}

if (enableSpotBugs) {
    // By default sonarqube does not depend on spotbugs
    val sonarqubeTask = tasks.sonarqube

    // See https://jira.sonarsource.com/browse/SONARGRADL-59
    // Unfortunately, report paths must be specified manually for now
    allprojects {
        if (!File(projectDir, "src/main").exists()) {
            return@allprojects
        }
        val spotBugTasks = tasks.withType<SpotBugsTask>().matching {
            // We don't send spotbugs for test classes
            !it.name.endsWith("Test")
        }
        sonarqubeTask {
            dependsOn(spotBugTasks)
        }
        apply(plugin = "org.sonarqube")
        sonarqube {
            properties {
                spotBugTasks.configureEach {
                    add("sonar.java.spotbugs.reportPaths", reports.xml.destination.toString())
                }
            }
        }
    }
}

val licenseHeaderFile = file("config/license.header.java")
allprojects {
    group = "org.apache.jmeter"
    // JMeter ClassFinder parses "class.path" and tries to find jar names there,
    // so we should produce jars without versions names for now
    // version = rootProject.version
    if (!skipSpotless) {
        apply(plugin = "com.diffplug.gradle.spotless")
        spotless {
            kotlinGradle {
                ktlint()
                trimTrailingWhitespace()
                endWithNewline()
            }
            if (project == rootProject) {
                // Spotless does not exclude subprojects when using target(...)
                // So **/*.md is enough to scan all the md files in JMeter codebase
                // See https://github.com/diffplug/spotless/issues/468
                format("markdown") {
                    target("**/*.md")
                    // Flot is known to have trailing whitespace, so the files
                    // are kept in their original format (e.g. to simplify diff on library upgrade)
                    targetExclude("bin/report-template/**/flot*/*.md")
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }
        }
    }
    plugins.withType<JavaPlugin> {
        // We don't intend to resolve that configuration
        // It is in line with further Gradle versions: https://github.com/gradle/gradle/issues/8585
        dependencies {
            configurations {
                compileOnly(platform(project(":src:bom")))
            }
        }

        apply<IdeaPlugin>()
        apply<EclipsePlugin>()
        if (!skipCheckstyle) {
            apply<CheckstylePlugin>()
            checkstyle {
                toolVersion = "checkstyle".v
            }
            val sourceSets: SourceSetContainer by project
            if (sourceSets.isNotEmpty()) {
                tasks.register("checkstyleAll") {
                    dependsOn(sourceSets.names.map { "checkstyle" + it.capitalize() })
                }
                tasks.register("checkstyle") {
                    group = LifecycleBasePlugin.VERIFICATION_GROUP
                    description = "Executes Checkstyle verifications"
                    dependsOn("checkstyleAll")
                    dependsOn("spotlessCheck")
                }
                // Spotless produces more meaningful error messages, so we ensure it is executed before Checkstyle
                if (!skipSpotless) {
                    for (s in sourceSets.names) {
                        tasks.named("checkstyle" + s.capitalize()) {
                            mustRunAfter("spotlessApply")
                            mustRunAfter("spotlessCheck")
                        }
                    }
                }
            }
        }
        apply<SpotBugsPlugin>()

        spotbugs {
            toolVersion = "spotbugs".v
            isIgnoreFailures = ignoreSpotBugsFailures
        }

        if (!skipSpotless) {
            spotless {
                java {
                    licenseHeaderFile(licenseHeaderFile)
                    importOrder("static ", "java.", "javax", "org", "net", "com", "")
                    removeUnusedImports()
                    trimTrailingWhitespace()
                    indentWithSpaces(4)
                    endWithNewline()
                }
            }
        }
        tasks.register("style") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Formats code (license header, import order, whitespace at end of line, ...) and executes Checkstyle verifications"
            if (!skipSpotless) {
                dependsOn("spotlessApply")
            }
            if (!skipCheckstyle) {
                dependsOn("checkstyleAll")
            }
        }
    }
    plugins.withId("groovy") {
        if (!skipSpotless) {
            spotless {
                groovy {
                    licenseHeaderFile(licenseHeaderFile)
                    importOrder("static ", "java.", "javax", "org", "net", "com", "")
                    trimTrailingWhitespace()
                    indentWithSpaces(4)
                    endWithNewline()
                }
            }
        }
    }

    plugins.withType<JacocoPlugin> {
        the<JacocoPluginExtension>().toolVersion = "jacoco".v

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
        apply<SigningPlugin>()
        // Sign all the published artifacts
        signing {
            sign(publishing.publications)
        }
    }

    plugins.withType<SigningPlugin> {
        if (useGpgCmd) {
            configure<SigningExtension> {
                useGpgCmd()
            }
        }
        afterEvaluate {
            configure<SigningExtension> {
                val release = rootProject.releaseParams.release.get()
                // Note it would still try to sign the artifacts,
                // however it would fail only when signing a RELEASE version fails
                isRequired = release && !skipSigning
            }
        }
    }

    plugins.withType<JavaPlugin> {
        // This block is executed right after `java` plugin is added to a project
        java {
            sourceCompatibility = JavaVersion.VERSION_1_8
        }

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
                            // Note: we need "generic Apache-2.0" text without third-party items
                            // So we use the text from $rootDir/config/ since source distribution
                            // contains altered text at $rootDir/LICENSE
                            textFrom("$rootDir/config/LICENSE")
                            textFrom("$rootDir/NOTICE")
                        }
                    }
                }
            }
            withType<Jar>().configureEach {
                manifest {
                    attributes["Bundle-License"] = "Apache-2.0"
                    attributes["Specification-Title"] = "Apache JMeter"
                    attributes["Specification-Vendor"] = "Apache Software Foundation"
                    attributes["Implementation-Vendor"] = "Apache Software Foundation"
                    attributes["Implementation-Vendor-Id"] = "org.apache"
                    attributes["Implementation-Version"] = rootProject.version
                }
            }
            withType<Test>().configureEach {
                useJUnitPlatform()
                testLogging {
                    exceptionFormat = TestExceptionFormat.FULL
                    showStandardStreams = true
                }
                // Pass the property to tests
                fun passProperty(name: String, default: String? = null) {
                    val value = System.getProperty(name) ?: default
                    value?.let { systemProperty(name, it) }
                }
                passProperty("java.awt.headless")
                passProperty("skip.test_TestDNSCacheManager.testWithCustomResolverAnd1Server")
                passProperty("junit.jupiter.execution.parallel.enabled", "true")
                passProperty("junit.jupiter.execution.timeout.default", "2 m")
                // https://github.com/junit-team/junit5/issues/2041
                // Gradle does not print parameterized test names yet :(
                afterTest(KotlinClosure2<TestDescriptor, TestResult, Any>({ descriptor, result ->
                    if (result.resultType != TestResult.ResultType.SUCCESS) {
                        val test = descriptor as org.gradle.api.internal.tasks.testing.TestDescriptorInternal
                        val classDisplayName = test.className?.let {
                            if (it.endsWith(test.classDisplayName)) it else "${test.className} [${test.classDisplayName}]"
                        } ?: test.classDisplayName
                        val testDisplayName = if (test.name == test.displayName) test.displayName else "${test.name} [${test.displayName}]"
                        println("\n$classDisplayName > $testDisplayName: ${result.resultType}")
                    }
                }))
            }
            withType<SpotBugsTask>().configureEach {
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                if (enableSpotBugs) {
                    description = "$description (skipped by default, to enable it add -Dspotbugs)"
                }
                reports {
                    html.isEnabled = reportsForHumans()
                    xml.isEnabled = !reportsForHumans()
                    // This is for Sonar
                    xml.isWithMessages = true
                }
                enabled = enableSpotBugs
            }
            withType<Javadoc>().configureEach {
                (options as StandardJavadocDocletOptions).apply {
                    noTimestamp.value = true
                    showFromProtected()
                    locale = "en"
                    docEncoding = "UTF-8"
                    charSet = "UTF-8"
                    encoding = "UTF-8"
                    docTitle = "Apache JMeter ${project.name} API"
                    windowTitle = "Apache JMeter ${project.name} API"
                    header = "<b>Apache JMeter</b>"
                    addStringOption("source", "8")
                    bottom =
                        "Copyright © 1998-$lastEditYear Apache Software Foundation. All Rights Reserved."
                    if (JavaVersion.current() >= JavaVersion.VERSION_1_9) {
                        addBooleanOption("html5", true)
                        links("https://docs.oracle.com/javase/9/docs/api/")
                    } else {
                        links("https://docs.oracle.com/javase/8/docs/api/")
                    }
                }
            }
        }
    }
}
