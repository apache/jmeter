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
import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.crlf.filter
import com.github.vlsi.gradle.git.FindGitAttributes
import com.github.vlsi.gradle.git.dsl.gitignore
import com.github.vlsi.gradle.properties.dsl.lastEditYear
import com.github.vlsi.gradle.properties.dsl.props
import com.github.vlsi.gradle.release.RepositoryType
import net.ltgt.gradle.errorprone.errorprone
import org.ajoberstar.grgit.Grgit
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sonarqube.gradle.SonarQubeProperties

plugins {
    java
    kotlin("jvm") apply false
    jacoco
    checkstyle
    id("org.jetbrains.gradle.plugin.idea-ext") apply false
    id("org.nosphere.apache.rat")
    id("com.github.autostyle")
    id("com.github.spotbugs")
    id("net.ltgt.errorprone") apply false
    id("org.sonarqube")
    id("com.github.vlsi.crlf")
    id("com.github.vlsi.gradle-extensions")
    id("com.github.vlsi.ide")
    id("com.github.vlsi.stage-vote-release")
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

val lastEditYear by extra(lastEditYear().toString())

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
    verbose.set(true)
    // Note: patterns are in non-standard syntax for RAT, so we use exclude(..) instead of excludeFile
    exclude(rootDir.resolve(".ratignore").readLines())
    // Gradle validation might assume copyLibs and rat operate on the intersecting set of files
    // Technically speaking, that is false positive since rat ignores *.jar files,
    // and copyLibs copies jar files only
    mustRunAfter(":src:dist:copyBinLibs", ":src:dist:copyLibs")
}

tasks.validateBeforeBuildingReleaseArtifacts {
    dependsOn(rat)
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

val jacocoReport by tasks.registering(JacocoReport::class) {
    group = "Coverage reports"
    description = "Generates an aggregate report from all subprojects"
}

val jacocoEnabled by extra {
    (boolProp("coverage") ?: false) || gradle.startParameter.taskNames.any { it.contains("jacoco") }
}

// Do not enable spotbugs by default. Execute it only when -Pspotbugs is present
val enableSpotBugs = props.bool("spotbugs", default = false)
val enableErrorprone by props()
val ignoreSpotBugsFailures by props()
val skipCheckstyle by props()
val skipAutostyle by props()
val werror by props(true) // treat javac warnings as errors
// Allow to skip building source/binary distributions
val skipDist by extra {
    boolProp("skipDist") ?: false
}
// Inherited from stage-vote-release-plugin: skipSign, useGpgCmd

allprojects {
    apply(plugin = "com.github.vlsi.gradle-extensions")

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

fun SonarQubeProperties.add(name: String, valueProvider: () -> String) {
    properties.getOrPut(name) { mutableSetOf<Any>() }
        .also {
            @Suppress("UNCHECKED_CAST")
            (it as MutableCollection<Any>).add(object {
                // SonarQube calls toString when converting properties to values
                // (see SonarQubeProperties), so we use that to emulate "lazy properties"
                override fun toString() = valueProvider()
            })
        }
}

if (jacocoEnabled) {
    val mergedCoverage = jacocoReport.get().reports.xml.outputLocation.toString()

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
                    add("sonar.java.spotbugs.reportPaths") {
                        // Note: report is created with lower-case xml, and then
                        // the created entry MUST be retrieved as upper-case XML
                        reports.named("XML").get().destination.toString()
                    }
                }
            }
        }
    }
}

fun com.github.autostyle.gradle.BaseFormatExtension.license() {
    licenseHeader(rootProject.ide.licenseHeader) {
        copyrightStyle("bat", com.github.autostyle.generic.DefaultCopyrightStyle.REM)
        copyrightStyle("cmd", com.github.autostyle.generic.DefaultCopyrightStyle.REM)
        addBlankLineAfter.set(true)
    }
    trimTrailingWhitespace()
    endWithNewline()
}

allprojects {
    group = "org.apache.jmeter"
    version = rootProject.version

    repositories {
        // RAT and Autostyle dependencies
        mavenCentral()
    }

    // JMeter ClassFinder parses "class.path" and tries to find jar names there,
    // so we should produce jars without versions names for now
    // version = rootProject.version
    if (!skipAutostyle) {
        apply(plugin = "com.github.autostyle")
        autostyle {
            kotlinGradle {
                license()
                ktlint("ktlint".v)
            }
            format("configs") {
                filter {
                    include("**/*.sh", "**/*.bsh", "**/*.cmd", "**/*.bat")
                    include("**/*.properties", "**/*.yml")
                    include("**/*.xsd", "**/*.xsl", "**/*.xml")
                    // Autostyle does not support gitignore yet https://github.com/autostyle/autostyle/issues/13
                    exclude("out/**")
                    if (project == rootProject) {
                        exclude(rootDir.resolve(".ratignore").readLines())
                        exclude("gradlew*")
                        // Generated by batch tests. It ignores log4j2.xml, however it is not that important
                        // The configuration will be removed when Autostyle will use .gitignore
                        exclude("bin/*.xml")
                    } else {
                        exclude("bin/**")
                    }
                }
                license()
            }
            format("markdown") {
                filter.include("**/*.md")
                endWithNewline()
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
                configProperties = mapOf(
                    "cache_file" to buildDir.resolve("checkstyle/cacheFile")
                )
            }
            val sourceSets: SourceSetContainer by project
            if (sourceSets.isNotEmpty()) {
                val checkstyleTasks = tasks.withType<Checkstyle>()
                checkstyleTasks.configureEach {
                    // Checkstyle 8.26 does not need classpath, see https://github.com/gradle/gradle/issues/14227
                    classpath = files()
                }

                tasks.register("checkstyleAll") {
                    dependsOn(checkstyleTasks)
                }
                tasks.register("checkstyle") {
                    group = LifecycleBasePlugin.VERIFICATION_GROUP
                    description = "Executes Checkstyle verifications"
                    dependsOn("checkstyleAll")
                    dependsOn("autostyleCheck")
                }
                // Autostyle produces more meaningful error messages, so we ensure it is executed before Checkstyle
                if (!skipAutostyle) {
                    for (s in sourceSets.names) {
                        tasks.named("checkstyle" + s.capitalize()) {
                            mustRunAfter("autostyleApply")
                            mustRunAfter("autostyleCheck")
                        }
                    }
                }
            }
        }
        apply(plugin = "com.github.spotbugs")

        spotbugs {
            toolVersion.set("spotbugs".v)
            ignoreFailures.set(ignoreSpotBugsFailures)
        }

        if (!skipAutostyle) {
            autostyle {
                java {
                    license()
                    importOrder("static ", "java.", "javax", "org", "net", "com", "")
                    removeUnusedImports()
                    indentWithSpaces(4)
                }
            }
        }
        tasks.register("style") {
            group = LifecycleBasePlugin.VERIFICATION_GROUP
            description = "Formats code (license header, import order, whitespace at end of line, ...) and executes Checkstyle verifications"
            if (!skipAutostyle) {
                dependsOn("autostyleApply")
            }
            if (!skipCheckstyle) {
                dependsOn("checkstyleAll")
            }
        }

        if (enableErrorprone) {
            apply(plugin = "net.ltgt.errorprone")
            dependencies {
                "errorprone"("com.google.errorprone:error_prone_core:${"errorprone".v}")
                "annotationProcessor"("com.google.guava:guava-beta-checker:1.0")
            }
            tasks.withType<JavaCompile>().configureEach {
                options.compilerArgs.addAll(listOf("-Xmaxerrs", "10000", "-Xmaxwarns", "10000"))
                options.errorprone {
                    disableWarningsInGeneratedCode.set(true)
                    errorproneArgs.add("-XepExcludedPaths:.*/javacc/.*")
                    disable(
                        "ComplexBooleanConstant",
                        "EqualsGetClass",
                        "InlineMeSuggester",
                        "OperatorPrecedence",
                        "MutableConstantField",
                        // "ReferenceEquality",
                        "SameNameButDifferent",
                        "TypeParameterUnusedInFormals"
                    )
                    // Analyze issues, and enable the check
                    disable(
                        "MissingSummary",
                        "EmptyBlockTag",
                        "BigDecimalEquals",
                        "StringSplitter"
                    )
                }
            }
        }
    }
    plugins.withId("groovy") {
        if (!skipAutostyle) {
            autostyle {
                groovy {
                    license()
                    importOrder("static ", "java.", "javax", "org", "net", "com", "")
                    indentWithSpaces(4)
                }
            }
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        configure<KotlinJvmProjectExtension> {
            // Require explicit access modifiers and require explicit types for public APIs.
            // See https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors
            if (props.bool("kotlin.explicitApi", default = true)) {
                explicitApi()
            }
        }
        tasks.withType<KotlinCompile>().configureEach {
            kotlinOptions {
                if (!name.startsWith("compileTest")) {
                    apiVersion = "kotlin.api".v
                }
            }
        }
        if (!skipAutostyle) {
            autostyle {
                kotlin {
                    license()
                    trimTrailingWhitespace()
                    ktlint("ktlint".v)
                    endWithNewline()
                }
            }
        }
    }

    plugins.withType<JacocoPlugin> {
        the<JacocoPluginExtension>().toolVersion = "jacoco".v

        val testTasks = tasks.withType<Test>()
        val javaExecTasks = tasks.withType<JavaExec>()
            .matching { it.name != "runGui" }
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
                html.required.set(reportsForHumans())
                xml.required.set(!reportsForHumans())
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
                classDirectories.from(
                    mainCode.output.asFileTree.matching {
                        exclude("module-info.class")
                    }
                )
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

    plugins.withType<JavaPlugin> {
        // This block is executed right after `java` plugin is added to a project
        java {
            sourceCompatibility = JavaVersion.VERSION_1_8
            consistentResolution {
                useCompileClasspathVersions()
            }
        }

        repositories {
            mavenCentral()
        }

        tasks {
            withType<JavaCompile>().configureEach {
                options.encoding = "UTF-8"
                options.compilerArgs.add("-Xlint:deprecation")
                if (werror) {
                    options.compilerArgs.add("-Werror")
                }
            }
            withType<ProcessResources>().configureEach {
                filteringCharset = "UTF-8"
                eachFile {
                    if (name.endsWith(".properties")) {
                        filteringCharset = "UTF-8"
                        // apply native2ascii conversion since Java 8 expects properties to have ascii symbols only
                        filter(org.apache.tools.ant.filters.EscapeUnicode::class)
                        filter(LineEndings.LF)
                    } else if (name.endsWith(".dtd") || name.endsWith(".svg") ||
                        name.endsWith(".txt")
                    ) {
                        filter(LineEndings.LF)
                    }
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

                outputs.cacheIf("test outcomes sometimes depends on third-party systems, so we should not cache it for now") {
                    false
                }

                // Pass the property to tests
                fun passProperty(name: String, default: String? = null) {
                    val value = System.getProperty(name) ?: default
                    value?.let { systemProperty(name, it) }
                }
                System.getProperties().filter {
                    it.key.toString().startsWith("jmeter.properties.")
                }.forEach {
                    systemProperty(it.key.toString().substring("jmeter.properties.".length), it.value)
                }
                passProperty("java.awt.headless")
                passProperty("skip.test_TestDNSCacheManager.testWithCustomResolverAnd1Server")
                passProperty("junit.jupiter.execution.parallel.enabled", "true")
                passProperty("junit.jupiter.execution.timeout.default", "2 m")
                // Spock tests use cglib proxies that access ClassLoader.defineClass reflectively
                // See https://github.com/apache/jmeter/pull/5763
                if (JavaVersion.current().isJava9Compatible) {
                    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
                }
            }
            withType<SpotBugsTask>().configureEach {
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                if (enableSpotBugs) {
                    description = "$description (skipped by default, to enable it add -Pspotbugs)"
                }
                reports {
                    // xml goes for SonarQube, so we always create it just in case
                    create("xml")
                    if (reportsForHumans()) {
                        create("html")
                    }
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
