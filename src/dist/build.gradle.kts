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

import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.git.FindGitAttributes
import com.github.vlsi.gradle.git.dsl.gitignore
import org.gradle.api.internal.TaskOutputsInternal

plugins {
    id("com.github.vlsi.crlf")
    id("com.github.vlsi.stage-vote-release")
    signing
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
        ":src:protocol:tcp")

// isCanBeConsumed = false ==> other modules must not use the configuration as a dependency
val buildDocs by configurations.creating {
    isCanBeConsumed = false
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
        testCompile(project(p, "testClasses"))
    }
    runtimeOnly("com.github.bulenkov.darcula:darcula") {
        because("""
            It just looks good, however Darcula is not used explicitly,
             so the dependency is added for distribution only""".trimIndent())
    }

    binLicense(project(":src:licenses", "binLicense"))
    srcLicense(project(":src:licenses", "srcLicense"))

    buildDocs(platform(project(":src:bom")))
    buildDocs("org.apache.velocity:velocity")
    buildDocs("commons-lang:commons-lang")
    buildDocs("commons-collections:commons-collections")
    buildDocs("org.jdom:jdom")
}

tasks.named(BasePlugin.CLEAN_TASK_NAME).configure {
    doLast {
        // createDist can't yet remove outdated jars (e.g. when dependency is updated to a newer version)
        // so we enhance "clean" task to kill the jars
        delete(fileTree("$rootDir/bin") { include("ApacheJMeter.jar") })
        delete(fileTree("$rootDir/lib") { include("*.jar") })
        delete(fileTree("$rootDir/lib/ext") { include("ApacheJMeter*.jar") })
    }
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
        val launcherProject = project(":src:launcher").path
        val bshclientProject = project(":src:bshclient").path
        val jorphanProject = project(":src:jorphan").path
        listOf(libs, libsExt, binLibs).forEach {
            it.fileMode = "644".toInt(8)
            it.dirMode = "755".toInt(8)
        }
        for (dep in deps) {
            val compId = dep.id.componentIdentifier
            // The path is "relative" to rootDir/lib
            when (compId) {
                is ProjectComponentIdentifier ->
                    (when (compId.projectPath) {
                        launcherProject -> binLibs
                        jorphanProject, bshclientProject -> libs
                        else -> libsExt
                    }).from(dep.file) {
                        // Technically speaking, current JMeter artifacts do not have version in the name
                        // however rename is here just in case
                        rename { dep.name + "." + dep.extension }
                    }
                else -> libs.from(dep.file)
            }
        }
    }
}

// This adds dependency on "populateLibs" task
// This makes uses of these copySpecs transparently depend on the builder task
libs.from(populateLibs)
libsExt.from(populateLibs)
binLibs.from(populateLibs)

val copyLibs by tasks.registering(Sync::class) {
    val junitSampleJar = project(":src:protocol:junit-sample").tasks.named(JavaPlugin.JAR_TASK_NAME)
    dependsOn(junitSampleJar)
    val generatorJar = project(":src:generator").tasks.named(JavaPlugin.JAR_TASK_NAME)
    // Can't use $rootDir since Gradle somehow reports .gradle/caches/ as "always modified"
    rootSpec.into("$rootDir/lib")
    with(libs)
    preserve {
        // Sync does not really know which files it copied during previous times, so
        // it just removes everything it sees.
        // We configure it to keep txt files that should be present there (the files come from Git source tree)
        include("**/*.txt")
        // Keep jars in lib/ext so developers don't have to re-install the plugsin again and again
        include("ext/*.jar")
    }
    into("ext") {
        with(libsExt)
        from(generatorJar)
    }
    into("junit") {
        from(junitSampleJar) {
            rename { "test.jar" }
        }
    }
}

val copyBinLibs by tasks.registering(Copy::class) {
    // Can't use $rootDir since Gradle somehow reports .gradle/caches/ as "always modified"
    rootSpec.into("$rootDir/bin")
    with(binLibs)
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
                    it.appendln("# Auto-generated from $velocityProperties to pass absolute path to Velocity")
                    for (line in lines) {
                        it.appendln(line)
                    }
                }
            }
        }
    }

    return tasks.register(taskName) {
        inputs.file("$baseDir/$style")
        inputs.file("$baseDir/$projectFile")
        inputs.files(fileTree(baseDir) {
            include(*includes)
            exclude(*excludes)
        })
        inputs.property("extension", extension)
        outputs.dir(outputDir)
        dependsOn(prepareProps)

        doLast {
            ant.withGroovyBuilder {
                "taskdef"("name" to "anakia",
                        "classname" to "org.apache.velocity.anakia.AnakiaTask",
                        "classpath" to buildDocs.asPath)
                "anakia"("basedir" to baseDir,
                        "destdir" to outputDir,
                        "extension" to extension,
                        "style" to style,
                        "projectFile" to projectFile,
                        "excludes" to excludes.joinToString(" "),
                        "includes" to includes.joinToString(" "),
                        "lastModifiedCheck" to "true",
                        "velocityPropertiesFile" to prepareProps.get().outputs.files.singleFile)
            }
        }
    }
}

val xdocs = "$rootDir/xdocs"

fun CopySpec.docCssAndImages() {
    from(xdocs) {
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

val buildPrintableDoc = createAnakiaTask("buildPrintableDoc", baseDir = xdocs,
        style = "stylesheets/site_printable.vsl",
        velocityProperties = "$xdocs/velocity.properties",
        projectFile = "stylesheets/printable_project.xml",
        excludes = arrayOf("**/stylesheets/**", "extending.xml", "extending/*.xml"),
        includes = arrayOf("**/*.xml"))

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
        "xslt"("style" to "$xdocs/stylesheets/website-style.xsl",
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
    inputs.files(xdocs)
    inputs.property("year", lastEditYear)
    outputs.dir(outputDir)

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

val skipDist: Boolean by rootProject.extra

// Generates distZip, distTar, distZipSource, and distTarSource tasks
// The archives and checksums are put to build/distributions
for (type in listOf("binary", "source")) {
    if (skipDist) {
        break
    }
    for (archive in listOf(Zip::class, Tar::class)) {
        val taskName = "dist${archive.simpleName}${type.replace("binary", "").capitalize()}"
        val archiveTask = tasks.register(taskName, archive) {
            val eol = if (archive == Tar::class) LineEndings.LF else LineEndings.CRLF
            group = distributionGroup
            description = "Creates $type distribution with $eol line endings for text files"
            if (this is Tar) {
                compression = Compression.GZIP
            }
            // Gradle does not track "filters" as archive/copy task dependencies,
            // So a mere change of a file attribute won't trigger re-execution of a task
            // So we add a custom property to re-execute the task in case attributes change
            inputs.property("gitproperties", gitProps.map { it.props.attrs.toString() })

            // Gradle defaults to the following pattern, and JMeter was using apache-jmeter-5.1_src.zip
            // [baseName]-[appendix]-[version]-[classifier].[extension]
            archiveBaseName.set("apache-jmeter-${rootProject.version}${if (type == "source") "_src" else ""}")
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

val runGui by tasks.registering() {
    group = "Development"
    description = "Builds and starts JMeter GUI"
    dependsOn(createDist)

    doLast {
        javaexec {
            workingDir = File(project.rootDir, "bin")
            main = "org.apache.jmeter.NewDriver"
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
        }
    }
}
