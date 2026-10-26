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

import com.github.autostyle.gradle.AutostyleTask
import com.github.gradle.node.yarn.task.YarnTask
import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.git.FindGitAttributes
import com.github.vlsi.gradle.git.dsl.gitignore
import com.github.vlsi.gradle.properties.dsl.props
import org.gradle.api.internal.TaskOutputsInternal

plugins {
    id("build-logic.build-params")
    id("com.github.vlsi.crlf")
    id("com.github.vlsi.stage-vote-release")
    id("build-logic.jvm-library")
    id("com.github.node-gradle.node")
}

var jars = arrayOf(
    ":src:bshclient",
    ":src:launcher",
    ":src:components",
    ":src:core",
    // ":src:examples",
    ":src:functions",
    ":src:jorphan",
    ":src:protocol:bolt",
    ":src:protocol:ftp",
    ":src:protocol:http",
    ":src:protocol:java",
    ":src:protocol:jdbc",
    ":src:protocol:jms",
    ":src:protocol:junit",
    ":src:protocol:ldap",
    ":src:protocol:mail",
    ":src:protocol:native",
    ":src:protocol:tcp"
)

// https://github.com/gradle/gradle/pull/16627
inline fun <reified T : Named> AttributeContainer.attribute(attr: Attribute<T>, value: String) =
    attribute(attr, objects.named<T>(value))

// isCanBeConsumed = false ==> other modules must not use the configuration as a dependency
val generatorJar by configurations.creating {
    isCanBeConsumed = false
}
val junitSampleJar by configurations.creating {
    isCanBeConsumed = false
    isTransitive = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, Category.LIBRARY)
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, LibraryElements.JAR)
        attribute(Usage.USAGE_ATTRIBUTE, Usage.JAVA_RUNTIME)
        attribute(Bundling.BUNDLING_ATTRIBUTE, Bundling.EXTERNAL)
    }
}
val binLicense by configurations.creating {
    isCanBeConsumed = false
}
val srcLicense by configurations.creating {
    isCanBeConsumed = false
}

// Note: you can inspect final classpath (list of jars in the binary distribution)  via
// gw dependencies --configuration runtimeClasspath
dependencies {
    for (p in jars) {
        api(project(p))
    }

    runtimeOnly("commons-codec:commons-codec") {
        because("commons-codec was a dependency in previous JMeter versions, so we keep it for compatibility")
    }
    runtimeOnly("commons-io:commons-io") {
        because("commons-io was a dependency in previous JMeter versions, so we keep it for compatibility")
    }
    runtimeOnly("org.apache.commons:commons-text") {
        because("commons-text was a dependency in previous JMeter versions, so we keep it for compatibility")
    }
    runtimeOnly("commons-collections:commons-collections") {
        because("commons-collections was a dependency in previous JMeter versions, so we keep it for compatibility")
    }
    runtimeOnly("org.apache.commons:commons-collections4") {
        because("commons-collections4 was a dependency in previous JMeter versions, so we keep it for compatibility")
    }

    binLicense(project(":src:licenses", "binLicense"))
    srcLicense(project(":src:licenses", "srcLicense"))
    generatorJar(project(":src:generator", "archives"))
    junitSampleJar(project(":src:protocol:junit-sample"))
}

node {
    // Do not declare the repository
    // See https://github.com/node-gradle/gradle-node-plugin/blob/master/docs/faq.md#is-this-plugin-compatible-with-centralized-repositories-declaration
    distBaseUrl = null
    download = true
    version = "25.1.0"
    yarnVersion = "1.22.22"
    // nodeProjectDir = rootProject.layout.projectDirectory.dir("xdocs")
}

tasks.clean {
    // copyLibs uses Sync task, so it can't predict all the possible output files (e.g. from previous executions)
    // So we register patterns to remove explicitly
    delete(fileTree("$rootDir/bin") { include("ApacheJMeter.jar") })
    delete(fileTree("$rootDir/lib") { include("*.jar") })
    delete(fileTree("$rootDir/lib/ext") { include("ApacheJMeter*.jar") })
    delete(fileTree("$rootDir/lib/junit") { include("test.jar") })
}

// Libs are populated dynamically since we can't get the full set of dependencies
// before we execute all the build scripts
val libs = copySpec {
    // Third-party dependencies + jorphan.jar
}

val libsExt = copySpec {
    // Apache JMeter jars
}

val binLibs = copySpec {
    // ApacheJMeter.jar launcher
}

// Splits jar dependencies to "lib", "lib/ext", and "bin" folders
val populateLibs by tasks.registering {
    dependsOn(configurations.runtimeClasspath)
    doLast {
        val deps = configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts
        // This ensures project exists, if project is renamed, names should be corrected here as wells
        val launcherProject = projects.src.launcher.path
        val bshclientProject = projects.src.bshclient.path
        val jorphanProject = projects.src.jorphan.path
        listOf(libs, libsExt, binLibs).forEach {
            it.filePermissions {
                unix("rw-r--r--")
            }
            it.dirPermissions {
                unix("rwxr-xr-x")
            }
        }
        for (dep in deps) {
            val compId = dep.id.componentIdentifier
            if (compId !is ProjectComponentIdentifier) {
                // Move all non-JMeter jars to lib folder
                libs.from(dep.file)
                continue
            }
            // JMeter jars are spread across $root/bin, $root/libs, and $root/libs/ext
            // for historical reasons
            when (compId.projectPath) {
                launcherProject -> binLibs
                jorphanProject, bshclientProject -> libs
                else -> libsExt
            }.from(dep.file) {
                // Remove version from the file name
                rename { dep.name + "." + dep.extension }
            }
        }
    }
}

val updateExpectedJars by props()

// -PupdateExpectedJars sets total-size-limit to the current total size plus this headroom,
// so a dependency update can grow the release a little without touching the expected file.
val releaseJarsSizeHeadroom = 5L * 1024 * 1024

val verifyReleaseDependencies by tasks.registering {
    description = "Verifies the set of external modules and the total size of external jars in the binary release"
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    val expectedFile = file("src/dist/expected_release_artifacts.txt")
    val projectPath = project.path
    inputs.file(expectedFile)
    inputs.files(configurations.runtimeClasspath)
    inputs.property("updateExpectedJars", updateExpectedJars)
    val actualJars = layout.buildDirectory.file("dist/release_jars.csv")
    outputs.file(actualJars)
    if (updateExpectedJars) {
        // Otherwise the update does nothing when the previous run passed with the same inputs
        outputs.upToDateWhen { false }
    }
    doLast {
        val caseInsensitive = String.CASE_INSENSITIVE_ORDER.thenBy { it }
        val expectedPath = expectedFile.relativeTo(rootDir)
        val actualFile = actualJars.get().asFile
        val actualPath = actualFile.relativeTo(rootDir)

        val artifacts = configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts
            .filter { it.id.componentIdentifier !is ProjectComponentIdentifier }
        val jarsByModule = artifacts
            .groupBy {
                // Coordinates without the version, so a version update keeps the expected file intact
                val id = it.id.componentIdentifier as? ModuleComponentIdentifier
                    ?: throw GradleException("${it.file.name} in runtimeClasspath comes from ${it.id.componentIdentifier.displayName}, but only modules from a repository and JMeter projects are expected")
                buildString {
                    append(id.moduleIdentifier)
                    it.classifier?.let { classifier -> append(':').append(classifier) }
                    if (it.extension != "jar") {
                        append('@').append(it.extension)
                    }
                }
            }
            .toSortedMap(caseInsensitive)
        val totalSize = artifacts.sumOf { it.file.length() }

        actualFile.writeText(
            artifacts.sortedWith(compareBy(caseInsensitive) { it.file.name })
                .joinToString("\n", postfix = "\n") { "${it.file.length()},${it.file.name}" }
        )

        // The expected file lists modules, not jars, so a second jar of a listed module would go unnoticed
        val ambiguous = jarsByModule.filterValues { it.size > 1 }
        if (ambiguous.isNotEmpty()) {
            throw GradleException(
                "Each module in $expectedPath must resolve to a single jar, but these resolve to several: " +
                    ambiguous.entries.joinToString("; ") { (module, files) -> "$module (${files.joinToString { it.file.name }})" }
            )
        }

        val sizeLimitPrefix = "total-size-limit="
        val expectedLines = expectedFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
        val existingSizeLimit = expectedLines.singleOrNull { it.startsWith(sizeLimitPrefix) }
            ?.removePrefix(sizeLimitPrefix)?.toLongOrNull()

        val mib = 1024L * 1024
        if (updateExpectedJars) {
            val sizeLimit = (totalSize + releaseJarsSizeHeadroom + mib - 1) / mib * mib
            println("Updating $expectedPath")
            expectedFile.writeText(
                listOf(
                    "# External modules in the binary release, and the upper bound for the total size of their jars in bytes.",
                    "# $path fails when a module is added or removed, or when the jars outgrow total-size-limit.",
                    "# Regenerate this file with ./gradlew $path -PupdateExpectedJars",
                    "# The regenerated limit is the current total size plus ${releaseJarsSizeHeadroom / mib} MiB, rounded up to a whole MiB.",
                    "$sizeLimitPrefix$sizeLimit",
                ).plus(jarsByModule.keys).joinToString("\n", postfix = "\n")
            )
            return@doLast
        }

        val sizeLimit = existingSizeLimit
            ?: throw GradleException("$expectedPath must have exactly one $sizeLimitPrefix<bytes> line")
        val expectedModules = expectedLines.filterNot { it.startsWith(sizeLimitPrefix) }.toSet()

        val added = jarsByModule.keys - expectedModules
        val removed = expectedModules.sortedWith(caseInsensitive) - jarsByModule.keys
        if (added.isEmpty() && removed.isEmpty() && totalSize <= sizeLimit) {
            return@doLast
        }

        throw GradleException(
            buildString {
                append("The external jars of the binary release do not match $expectedPath.")
                append(" The check catches dependencies that enter the release unnoticed, often transitively, and unexpected growth of the release.")
                if (added.isNotEmpty()) {
                    append("\n\nModules that are in the release but not in $expectedPath:")
                    for (module in added) {
                        val file = jarsByModule.getValue(module).single().file
                        append("\n  + $module (${file.name}, ${file.length()} bytes)")
                    }
                    append("\nTo see which dependency brings a module in, run")
                    append(" ./gradlew $projectPath:dependencyInsight --configuration runtimeClasspath --dependency <group:artifact>")
                }
                if (removed.isNotEmpty()) {
                    append("\n\nModules that are in $expectedPath but no longer in the release:")
                    for (module in removed) {
                        append("\n  - $module")
                    }
                }
                if (totalSize > sizeLimit) {
                    append("\n\nThe total size of external jars is $totalSize bytes (${totalSize / mib} MiB),")
                    append(" which exceeds $sizeLimitPrefix$sizeLimit (${sizeLimit / mib} MiB) in $expectedPath by ${totalSize - sizeLimit} bytes.")
                    append("\nTo find which jars grew, compare $actualPath with the same file built from the base branch. The largest jars now:")
                    for (jar in artifacts.sortedByDescending { it.file.length() }.take(10)) {
                        append("\n  ${jar.file.length().toString().padStart(9)} ${jar.file.name}")
                    }
                }
                append("\n\nIf the change is intended, run ./gradlew $path -PupdateExpectedJars and commit $expectedPath.")
                append(" The update lists the current modules and sets total-size-limit to the current size plus ${releaseJarsSizeHeadroom / mib} MiB.")
                append(" Name the added or removed modules and the size growth in the pull request description, with the dependency update that caused them.")
                append("\nIf the change is not intended, exclude the module or revert the dependency update that brought it in.")
                append("\nThe full list of jars with their sizes is in $actualPath")
            }
        )
    }
}

tasks.check {
    dependsOn(verifyReleaseDependencies)
}

// This adds dependency on "populateLibs" task
// This makes uses of these copySpecs transparently depend on the builder task
libs.from(populateLibs)
libsExt.from(populateLibs)
binLibs.from(populateLibs)

val copyLibs by tasks.registering(Sync::class) {
    // Can't use $rootDir since Gradle somehow reports .gradle/caches/ as "always modified"
    rootSpec.into("$rootDir/lib")
    with(libs)
    preserve {
        // Sync does not really know which files it copied during previous times, so
        // it just removes everything it sees.
        // We configure it to keep txt files that should be present there (the files come from Git source tree)
        include("**/*.txt")
        // Keep jars in lib/ext so developers don't have to re-install the plugins again and again
        include("ext/*.jar")
        exclude("ext/ApacheJMeter*.jar")
    }
    into("ext") {
        with(libsExt)
        from(files(generatorJar)) {
            rename { "ApacheJMeter_generator.jar" }
        }
    }
    into("junit") {
        from(files(junitSampleJar)) {
            rename { "test.jar" }
        }
    }
}

val copyBinLibs by tasks.registering(Copy::class) {
    // Can't use $rootDir since Gradle somehow reports .gradle/caches/ as "always modified"
    rootSpec.into("$rootDir/bin")
    with(binLibs)
    // :src:config:jar conflicts with copyBinLibs on bin, bin/templates, bin/report-template folders
    // so we add explicit ordering
    mustRunAfter(":src:config:jar")
}

val createDist by tasks.registering {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Copies JMeter jars and dependencies to projectRoot/lib/ folder"
    dependsOn(copyLibs)
    dependsOn(copyBinLibs)
}

// This task scans the project for gitignore / gitattributes, and that is reused for building
// source/binary artifacts with the appropriate eol/executable file flags
val gitProps by rootProject.tasks.existing(FindGitAttributes::class)

val xdocs = rootProject.layout.projectDirectory.dir("xdocs")

fun CopySpec.docCssAndImages() {
    from(xdocs) {
        include(".htaccess")
        include("css/**")
        include("images/**")
    }
}

fun CopySpec.manuals() {
    from(xdocs) {
        include("demos/**")
        include("extending/jmeter_tutorial.pdf")
        include("usermanual/**/*.pdf")
    }
}

fun CopySpec.docFontsAndCss() {
    into("fonts") {
        from(
            fileTree(xdocs.dir("node_modules/@fontsource/merriweather/files")) {
                builtBy(yarn_install)
            }
        ) {
            include("*400*normal*.woff2")
        }
        from(xdocs.dir("node_modules/@fortawesome/fontawesome-free/webfonts")) {
            include("fa-brands*.woff2")
        }
    }
    into("css") {
        from(xdocs.dir("node_modules/@fontsource/merriweather")) {
            include("400.css")
            rename { "merriweather.css" }
            filter {
                it.replace("./files", "../fonts")
            }
        }
        from(xdocs.dir("node_modules/@fortawesome/fontawesome-free/css")) {
            include("fontawesome.min.css")
            include("brands.min.css")
            filter {
                it.replace("../webfonts", "../fonts")
            }
        }
    }
}

tasks.yarnSetup {
    mustRunAfter(tasks.withType<AutostyleTask>())
}

tasks.nodeSetup {
    mustRunAfter(tasks.withType<AutostyleTask>())
}

val yarn_install = tasks.named<YarnTask>("yarn_install") {
    workingDir = xdocs
    mustRunAfter(":rat")
    inputs.file(xdocs.file("package.json")).withPropertyName("package_json").withPathSensitivity(PathSensitivity.NONE)
    outputs.file(xdocs.file("yarn.lock")).withPropertyName("yarn.lock")
    outputs.dir(xdocs.dir("node_modules")).withPropertyName("node_modules")
}

val lastEditYear: String by rootProject.extra

fun xslt(
    subdir: String,
    outputDir: String,
    includes: Array<String> = arrayOf("*.xml"),
    excludes: Array<String> = arrayOf("extending.xml")
) {

    val relativePath = if (subdir.isEmpty()) "." else ".."
    ant.withGroovyBuilder {
        "xslt"(
            "style" to "$xdocs/stylesheets/website-style.xsl",
            "basedir" to "$xdocs/$subdir",
            "destdir" to "$outputDir/$subdir",
            "excludes" to excludes.joinToString(" "),
            "includes" to includes.joinToString(" ")
        ) {
            "param"("name" to "relative-path", "expression" to relativePath)
            "param"("name" to "subdir", "expression" to subdir)
            "param"("name" to "year", "expression" to lastEditYear)
        }
    }
}

val processSiteXslt by tasks.registering {
    val outputDir = layout.buildDirectory.dir("siteXslt").get().asFile
    inputs.files(
        fileTree(xdocs) {
            exclude("node_modules")
            exclude("package.json")
            exclude("package-lock.json")
            exclude("yarn.lock")
        }
    ).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("xdocs")
    inputs.property("year", lastEditYear)
    outputs.dir(outputDir)
    outputs.cacheIf { true }

    doLast {
        for (f in (outputs as TaskOutputsInternal).previousOutputFiles) {
            f.delete()
        }
        for (i in arrayOf("", "usermanual", "localising")) {
            xslt(i, outputDir.absolutePath)
        }
    }
}

fun CopySpec.siteLayout() {
    // TODO: generate doap_JMeter.rdf
    from("$xdocs/download_jmeter.cgi")
    into("api") {
        javadocs()
    }
    from(processSiteXslt)
    docCssAndImages()
    manuals()
    docFontsAndCss()
}

// See https://github.com/gradle/gradle/issues/10960
val previewSiteDir = layout.buildDirectory.dir("site")

val previewSite by tasks.registering(Sync::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Creates preview of a site to build/docs/site"
    into(previewSiteDir)
    CrLfSpec().run {
        gitattributes(gitProps)
        siteLayout()
    }
    doLast {
        println("Site preview synchronized to ${previewSiteDir.get().file("index.html")}")
    }
}

val distributionGroup = "distribution"
val baseFolder = "apache-jmeter-${rootProject.version}"

fun CopySpec.javadocs() = from(javadocAggregate)

fun CopySpec.excludeLicenseFromSourceRelease() {
    // Source release has "/licenses" folder with licenses for third-party dependencies
    // It is populated by "dependencyLicenses" above,
    // so we ignore the folder when building source releases
    exclude("licenses/**")
    exclude("LICENSE")
}

fun CrLfSpec.binaryLayout() = copySpec {
    gitattributes(gitProps)
    into(baseFolder) {
        // Note: license content is taken from "/build/..", so gitignore should not be used
        // Note: this is a "license + third-party licenses", not just Apache-2.0
        // Note: files(...) adds both "files" and "dependency"
        from(files(binLicense))
        from(rootDir) {
            gitignore(gitProps)
            exclude("bin/testfiles")
            exclude("bin/rmi_keystore.jks")
            include("bin/**")
            include("lib/ext/**")
            include("lib/junit/**")
            include("extras/**")
            include("README.md")
            excludeLicenseFromSourceRelease()
        }
        into("bin") {
            with(binLibs)
        }
        into("lib") {
            with(libs)
            into("ext") {
                with(libsExt)
            }
        }
        into("docs") {
            from(processSiteXslt)
            docCssAndImages()
            manuals()
            docFontsAndCss()
        }
        into("docs/api") {
            javadocs()
        }
    }
}

fun CrLfSpec.sourceLayout() = copySpec {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    gitattributes(gitProps)
    into(baseFolder) {
        // Note: license content is taken from "/build/..", so gitignore should not be used
        // Note: this is a "license + third-party licenses", not just Apache-2.0
        // Note: files(...) adds both "files" and "dependency"
        from(files(srcLicense))
        // Include all the source files
        from(rootDir) {
            gitignore(gitProps)
            excludeLicenseFromSourceRelease()
            exclude("xdocs/node_modules")
            exclude("xdocs/package.json")
            exclude("xdocs/package-lock.json")
            exclude("xdocs/yarn.lock")
        }
    }
}

val javadocAggregate by tasks.registering(Javadoc::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Generates aggregate javadoc for all the artifacts"

    val sourceSets = jars.map { project(it).sourceSets.main }

    classpath = files(sourceSets.map { set -> set.map { it.output + it.compileClasspath } })
    // Aggregate javadoc needs to include generated JMeterVersion class
    // So we use delay computation of source files
    setSource(sourceSets.map { set -> set.map { it.allJava } })
    setDestinationDir(layout.buildDirectory.dir("docs/javadocAggregate").get().asFile)
}

// Generates distZip, distTar, distZipSource, and distTarSource tasks
// The archives and checksums are put to build/distributions
for (type in listOf("binary", "source")) {
    if (buildParameters.skipDist) {
        break
    }
    for (archive in listOf(Zip::class, Tar::class)) {
        val taskName = "dist${archive.simpleName}${type.replace("binary", "").replaceFirstChar { it.titlecaseChar() }}"
        val archiveTask = tasks.register(taskName, archive) {
            val eol = if (archive == Tar::class) LineEndings.LF else LineEndings.CRLF
            group = distributionGroup
            description = "Creates $type distribution with $eol line endings for text files"
            if (this is Tar) {
                compression = Compression.GZIP
            }
            // dist task excludes jar files from bin/, and lib/ however Gradle does not see that
            // So we add an artificial dependency
            mustRunAfter(copyBinLibs)
            mustRunAfter(copyLibs)
            mustRunAfter(":src:dist-check:copyExtraTestLibs")
            // Gradle does not track "filters" as archive/copy task dependencies,
            // So a mere change of a file attribute won't trigger re-execution of a task
            // So we add a custom property to re-execute the task in case attributes change
            inputs.property("gitproperties", gitProps.map { it.props.attrs.toString() })

            // Gradle defaults to the following pattern, and JMeter was using apache-jmeter-5.1_src.zip
            // [baseName]-[appendix]-[version]-[classifier].[extension]
            archiveBaseName.set("apache-jmeter-${rootProject.version}${if (type == "source") "_src" else ""}")
            // Discard project version since we want it to be added before "_src"
            archiveVersion.set("")
            CrLfSpec(eol).run {
                wa1191SetInputs(gitProps)
                with(if (type == "source") sourceLayout() else binaryLayout())
            }
        }
        releaseArtifacts {
            artifact(archiveTask)
        }
    }
}

releaseArtifacts {
    previewSite(previewSite) {
        into("site") {
            from(previewSiteDir)
        }
    }
}

val runGui by tasks.registering(JavaExec::class) {
    group = "Development"
    description = "Builds and starts JMeter GUI"
    dependsOn(createDist)
    buildParameters.testJdk?.let {
        javaLauncher.set(javaToolchains.launcherFor(it))
    }

    workingDir = File(project.rootDir, "bin")
    mainClass.set("org.apache.jmeter.NewDriver")
    classpath("$rootDir/bin/ApacheJMeter.jar")
    jvmArgs("-Xss256k")
    jvmArgs("-XX:MaxMetaspaceSize=256m")

    val osName = System.getProperty("os.name")
    if (osName.contains(Regex("mac os x|darwin|osx", RegexOption.IGNORE_CASE))) {
        jvmArgs("-Xdock:name=JMeter")
        jvmArgs("-Xdock:icon=$rootDir/xdocs/images/jmeter_square.png")
        jvmArgs("-Dapple.laf.useScreenMenuBar=true")
        jvmArgs("-Dapple.eawt.quitStrategy=CLOSE_ALL_WINDOWS")
    }

    fun passProperty(name: String, default: String? = null) {
        val value = System.getProperty(name) ?: default
        value?.let { systemProperty(name, it) }
    }

    passProperty("java.awt.headless")

    val props = System.getProperties()
    @Suppress("UNCHECKED_CAST")
    for (e in props.propertyNames() as `java.util`.Enumeration<String>) {
        // Pass -Djmeter.* and -Ddarklaf.* properties to the JMeter process
        if (e.startsWith("jmeter.") || e.startsWith("darklaf.")) {
            passProperty(e)
        }
        if (e == "darklaf.native") {
            systemProperty("darklaf.decorations", "true")
            systemProperty("darklaf.allowNativeCode", "true")
        }
    }
}
