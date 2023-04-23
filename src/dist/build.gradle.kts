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

import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.git.FindGitAttributes
import com.github.vlsi.gradle.git.dsl.gitignore
import com.github.vlsi.gradle.properties.dsl.props
import org.gradle.api.internal.TaskOutputsInternal
import kotlin.math.absoluteValue

plugins {
    id("build-logic.build-params")
    id("com.github.vlsi.crlf")
    id("com.github.vlsi.stage-vote-release")
    id("build-logic.jvm-library")
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
    ":src:protocol:mongodb",
    ":src:protocol:native",
    ":src:protocol:tcp"
)

// https://github.com/gradle/gradle/pull/16627
inline fun <reified T : Named> AttributeContainer.attribute(attr: Attribute<T>, value: String) =
    attribute(attr, objects.named<T>(value))

// isCanBeConsumed = false ==> other modules must not use the configuration as a dependency
val buildDocs by configurations.creating {
    isCanBeConsumed = false
}
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

val allTestClasses by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

// Note: you can inspect final classpath (list of jars in the binary distribution)  via
// gw dependencies --configuration runtimeClasspath
dependencies {
    for (p in jars) {
        api(project(p))
        allTestClasses(project(p, "testClasses"))
    }

    binLicense(project(":src:licenses", "binLicense"))
    srcLicense(project(":src:licenses", "srcLicense"))
    generatorJar(project(":src:generator", "archives"))
    junitSampleJar(project(":src:protocol:junit-sample"))

    buildDocs(platform(projects.src.bomThirdparty))
    buildDocs("org.apache.velocity:velocity")
    buildDocs("commons-lang:commons-lang")
    buildDocs("org.apache.commons:commons-collections4")
    buildDocs("org.jdom:jdom")
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
        val launcherProject = projects.src.launcher.dependencyProject.path
        val bshclientProject = projects.src.bshclient.dependencyProject.path
        val jorphanProject = projects.src.jorphan.dependencyProject.path
        listOf(libs, libsExt, binLibs).forEach {
            it.fileMode = "644".toInt(8)
            it.dirMode = "755".toInt(8)
        }
        for (dep in deps) {
            val compId = dep.id.componentIdentifier
            if (compId !is ProjectComponentIdentifier || !compId.build.isCurrentBuild) {
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

val verifyReleaseDependencies by tasks.registering {
    description = "Verifies if binary release archive contains the expected set of external jars"
    group = LifecycleBasePlugin.VERIFICATION_GROUP

    dependsOn(configurations.runtimeClasspath)
    val expectedLibs = file("src/dist/expected_release_jars.csv")
    inputs.file(expectedLibs)
    val actualLibs = File(buildDir, "dist/expected_release_jars.csv")
    outputs.file(actualLibs)
    doLast {
        val caseInsensitive: Comparator<String> = compareBy(String.CASE_INSENSITIVE_ORDER, { it })

        val deps = configurations.runtimeClasspath.get().resolvedConfiguration.resolvedArtifacts
        val libs = deps.asSequence()
            .filter {
                val compId = it.id.componentIdentifier
                compId !is ProjectComponentIdentifier || !compId.build.isCurrentBuild
            }
            .map { it.file.name to it.file.length() }
            .sortedWith(compareBy(caseInsensitive) { it.first })
            .associate { it }

        val expected = expectedLibs.readLines().asSequence()
            .filter { "," in it }
            .map {
                val (length, name) = it.split(",", limit = 2)
                name to length.toLong()
            }
            .associate { it }

        if (libs == expected) {
            return@doLast
        }

        val sb = StringBuilder()
        sb.append("External dependencies differ (you could update ${expectedLibs.relativeTo(rootDir)} if you run $path -PupdateExpectedJars):")

        val sizeBefore = expected.values.sum()
        val sizeAfter = libs.values.sum()
        if (sizeBefore != sizeAfter) {
            sb.append("\n  $sizeBefore => $sizeAfter bytes")
            sb.append(" (${if (sizeAfter > sizeBefore) "+" else "-"}${(sizeAfter - sizeBefore).absoluteValue} byte")
            if ((sizeAfter - sizeBefore).absoluteValue > 1) {
                sb.append("s")
            }
            sb.append(")")
        }
        if (libs.size != expected.size) {
            sb.append("\n  ${expected.size} => ${libs.size} files")
            sb.append(" (${if (libs.size > expected.size) "+" else "-"}${(libs.size - expected.size).absoluteValue})")
        }
        sb.appendLine()
        for (dep in (libs.keys + expected.keys).sortedWith(caseInsensitive)) {
            val old = expected[dep]
            val new = libs[dep]
            if (old == new) {
                continue
            }
            sb.append("\n")
            if (old != null) {
                sb.append("-").append(old.toString().padStart(8))
            } else {
                sb.append("+").append(new.toString().padStart(8))
            }
            sb.append(" ").append(dep)
        }
        val newline = System.getProperty("line.separator")
        actualLibs.writeText(
            libs.map { "${it.value},${it.key}" }.joinToString(newline, postfix = newline)
        )
        if (updateExpectedJars) {
            println("Updating ${expectedLibs.relativeTo(rootDir)}")
            actualLibs.copyTo(expectedLibs, overwrite = true)
        } else {
            throw GradleException(sb.toString())
        }
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

fun createAnakiaTask(
    taskName: String,
    baseDir: String,
    extension: String = ".html",
    style: String,
    velocityProperties: String,
    projectFile: String,
    excludes: Array<String>,
    includes: Array<String>
): TaskProvider<Task> {
    val outputDir = "$buildDir/docs/$taskName"

    val prepareProps = tasks.register("prepareProperties$taskName") {
        // AnakiaTask can't use relative paths, and it forbids ../, so we create a dedicated
        // velocity.properties file that contains absolute path
        inputs.file(velocityProperties)
        val outputProps = "$buildDir/docProps/$taskName/velocity.properties"
        outputs.file(outputProps)
        doLast {
            // Unfortunately, Velocity does not use Java properties format.
            // For instance, Properties escape : as \:, however Velocity does not understand that.
            // Thus it tries to use c\:\path\to\workspace which does not work
            val p = `java.util`.Properties()
            file(velocityProperties).reader().use {
                p.load(it)
            }
            p["resource.loader"] = "file"
            p["file.resource.loader.path"] = baseDir
            p["file.resource.loader.class"] = "org.apache.velocity.runtime.resource.loader.FileResourceLoader"
            val specials = Regex("""([,\\])""")
            val lines = p.entries
                .map { (it.key as String) + "=" + ((it.value as String).replace(specials, """\\$1""")) }
                .sorted()
            file(outputProps).apply {
                parentFile.run { isDirectory || mkdirs() } || throw IllegalStateException("Unable to create directory $parentFile")

                writer().use {
                    it.appendLine("# Auto-generated from $velocityProperties to pass absolute path to Velocity")
                    for (line in lines) {
                        it.appendLine(line)
                    }
                }
            }
        }
    }

    return tasks.register(taskName) {
        inputs.file("$baseDir/$style").withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("styleDir")
        inputs.file("$baseDir/$projectFile").withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("projectDir")
        inputs.files(
            fileTree(baseDir) {
                include(*includes)
                exclude(*excludes)
            }
        ).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("baseDir")
        inputs.property("extension", extension)
        outputs.dir(outputDir)
        outputs.cacheIf { true }
        dependsOn(prepareProps)

        doLast {
            ant.withGroovyBuilder {
                "taskdef"(
                    "name" to "anakia",
                    "classname" to "org.apache.velocity.anakia.AnakiaTask",
                    "classpath" to buildDocs.asPath
                )
                "anakia"(
                    "basedir" to baseDir,
                    "destdir" to outputDir,
                    "extension" to extension,
                    "style" to style,
                    "projectFile" to projectFile,
                    "excludes" to excludes.joinToString(" "),
                    "includes" to includes.joinToString(" "),
                    "lastModifiedCheck" to "true",
                    "velocityPropertiesFile" to prepareProps.get().outputs.files.singleFile
                )
            }
        }
    }
}

val xdocs = "$rootDir/xdocs"

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

fun CopySpec.printableDocumentation() {
    into("docs") {
        docCssAndImages()
    }
    into("printable_docs") {
        from(buildPrintableDoc)
        manuals()
    }
}

val buildPrintableDoc = createAnakiaTask(
    "buildPrintableDoc", baseDir = xdocs,
    style = "stylesheets/site_printable.vsl",
    velocityProperties = "$xdocs/velocity.properties",
    projectFile = "stylesheets/printable_project.xml",
    excludes = arrayOf("**/stylesheets/**", "extending.xml", "extending/*.xml"),
    includes = arrayOf("**/*.xml")
)

val previewPrintableDocs by tasks.registering(Copy::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Creates preview of a printable documentation to build/docs/printable_preview"
    into("$buildDir/docs/printable_preview")
    CrLfSpec().run {
        gitattributes(gitProps)
        printableDocumentation()
    }
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
    val outputDir = "$buildDir/siteXslt"
    inputs.files(xdocs).withPathSensitivity(PathSensitivity.RELATIVE).withPropertyName("xdocs")
    inputs.property("year", lastEditYear)
    outputs.dir(outputDir)
    outputs.cacheIf { true }

    doLast {
        for (f in (outputs as TaskOutputsInternal).previousOutputFiles) {
            f.delete()
        }
        for (i in arrayOf("", "usermanual", "localising")) {
            xslt(i, outputDir)
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
}

// See https://github.com/gradle/gradle/issues/10960
val previewSiteDir = buildDir.resolve("site")
val previewSite by tasks.registering(Sync::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Creates preview of a site to build/docs/site"
    into(previewSiteDir)
    CrLfSpec().run {
        gitattributes(gitProps)
        siteLayout()
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
        printableDocumentation()
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
    setDestinationDir(file("$buildDir/docs/javadocAggregate"))
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
