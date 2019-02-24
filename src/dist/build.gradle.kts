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

import org.apache.jmeter.buildtools.CrLfSpec
import org.apache.jmeter.buildtools.LineEndings
import org.apache.jmeter.buildtools.release.ReleaseExtension
import org.gradle.api.internal.TaskOutputsInternal
import versions.BuildTools
import versions.Libs

var jars = arrayOf(
        ":src:launcher",
        ":src:components",
        ":src:core",
        //":src:examples",
        ":src:functions",
        ":src:jorphan",
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

val buildDocs by configurations.creating

dependencies {
    for (p in jars) {
        compile(project(p))
        testCompile(project(p, "testClasses"))
    }
    compile(Libs.darcula) {
        because("""
            It just looks good, however Darcula is not used explicitly,
             so the dependency is added for distribution only""".trimIndent())
    }

    buildDocs(BuildTools.velocity)
    buildDocs(Libs.commons_lang)
    buildDocs(Libs.commons_collections)
    buildDocs(Libs.jdom)
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
    dependsOn(configurations.runtime)
    doLast {
        val deps = configurations.runtime.get().resolvedConfiguration.resolvedArtifacts
        // This ensures project exists, if project is renamed, names should be corrected here as wells
        val launcherProject = project(":src:launcher").path
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
                        jorphanProject -> libs
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

val copyLibs by tasks.registering(Copy::class) {
    dependsOn(populateLibs)
    val junitSampleJar = project(":src:protocol:junit-sample").tasks.named(JavaPlugin.JAR_TASK_NAME)
    dependsOn(junitSampleJar)
    val generatorJar = project(":src:generator").tasks.named(JavaPlugin.JAR_TASK_NAME)
    // Can't use $rootDir since Gradle somehow reports .gradle/caches/ as "always modified"
    rootSpec.into("$rootDir/lib")
    with(libs)
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
    dependsOn(populateLibs)
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

fun CrLfSpec.licenseNotice(licenseType: String) = copySpec {
    textFrom(rootDir) {
        text("NOTICE")
    }
    // Project :src:license-* might not be evaluated yet, so "generateLicense" task might not yet exist
    // So we provide the file via "provider" that just delays the evaluation
    textFrom(provider { project(":src:license-$licenseType").tasks["generateLicense"] })
    into("licenses") {
        textFrom("$rootDir/licenses/README.txt")
        into("src") {
            textFrom("$rootDir/licenses/src")
        }
        if (licenseType == "source") {
            textFrom("$rootDir/licenses") {
                text("apache2.txt")
                text("license.for.third.party.dependencies.txt")
            }
        }
        if (licenseType == "binary") {
            into("bin") {
                textFrom(provider { project(":src:license-$licenseType").tasks["generateLicenseReport"] })
            }
        }
    }
}

fun CrLfSpec.commonFiles(layoutType: String) = copySpec {
    filteringCharset = "UTF-8"
    with(licenseNotice(layoutType))
    into("bin") {
        textFrom("$rootDir/bin") {
            text("*.bshrc", "*.properties", "*.parameters", "*.xml", "*.conf")
            text("utility.groovy")
            exclude("*.log", "*.jmx")
            shell("create-rmi-keystore",
                    "heapdump",
                    "jmeter",
                    "jmeter-n",
                    "jmeter-n-r",
                    "jmeter-server",
                    "jmeter-t",
                    "jmeterw",
                    "mirror-server",
                    "shutdown",
                    "stoptest",
                    "threaddump")
        }
        into("templates") {
            textFrom("$rootDir/bin/templates") {
                text("*.jmx", "*.dtd", "*.xml")
            }
        }
        into("examples") {
            textFrom("$rootDir/bin/examples") {
                text("**/*.jmx", "**/*.jsp", "**/*.csv")
                binary("**/*.png")
            }
        }
        into("report-template") {
            textFrom("$rootDir/bin/report-template") {
                text("**/*") // all except binary
                binary("**/*.png", "**/*.ttf", "**/*.woff", "**/*.woff2", "**/*.eot", "**/*.otf")
            }
        }
    }
    into("lib/ext") {
        textFrom("$rootDir/lib/ext") {
            text("readme.txt")
        }
    }
    into("extras") {
        textFrom("$rootDir/extras") {
            shell("proxycert", "schematic")
            text("*.json", "*.jmx", "*.txt", "*.xml", "*.bsh", "*.xsl")
            binary("*.jar", "*.png")
        }
    }
}

fun createAnakiaTask(taskName: String,
                     baseDir: String, extension: String = ".html", style: String,
                     velocityProperties: String, projectFile: String, excludes: Array<String>,
                     includes: Array<String>): TaskProvider<Task> {
    val outputDir = "$buildDir/docs/$taskName"

    val prepareProps = tasks.register("prepareProperties$taskName") {
        // AnakiaTask can't use relative paths, and it forbids ../, so we create a dedicated
        // velocity.properties file that contains absolute path
        inputs.file(velocityProperties)
        val outputProps = "$buildDir/docProps/$taskName/velocity.properties"
        outputs.file(outputProps)
        doLast {
            val p = `java.util`.Properties()
            file(velocityProperties).reader().use {
                p.load(it)
            }
            p["resource.loader"] = "file"
            p["file.resource.loader.path"] = baseDir
            p["file.resource.loader.class"] = "org.apache.velocity.runtime.resource.loader.FileResourceLoader"
            file(outputProps).apply {
                parentFile.run { isDirectory || mkdirs() } || throw IllegalStateException("Unable to create directory $parentFile")

                writer().use {
                    p.store(it, "Auto-generated from $velocityProperties to pass absolute path to Velocity")
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
        inputs.properties["extension"] = extension
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

fun CrLfSpec.docCssAndImages() = copySpec {
    filteringCharset = "UTF-8"

    into("css") {
        textFrom("$xdocs/css")
    }
    into("images") {
        from("$xdocs/images")
    }
}

fun CrLfSpec.manuals() = copySpec {
    into("demos") {
        textFrom("$xdocs/demos")
    }
    into("extending") {
        from("$xdocs/extending/jmeter_tutorial.pdf")
    }
    into("usermanual") {
        from("$xdocs/usermanual") {
            include("*.pdf")
        }
    }
}

fun CrLfSpec.printableDocumentation() = copySpec {
    filteringCharset = "UTF-8"

    into("docs") {
        with(docCssAndImages())
    }
    into("printable_docs") {
        textFrom(buildPrintableDoc)
        with(manuals())
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
    with(CrLfSpec().printableDocumentation())
}

val lastEditYear: String by rootProject.extra

fun xslt(subdir: String,
         outputDir: String,
         includes: Array<String> = arrayOf("*.xml"),
         excludes: Array<String> = arrayOf("extending.xml")) {

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
    inputs.properties["year"] = lastEditYear
    outputs.dir(outputDir)

    doLast {
        for(f in (outputs as TaskOutputsInternal).previousOutputFiles) {
            f.delete()
        }
        for(i in arrayOf("", "usermanual", "localising")) {
            xslt(i, outputDir)
        }
    }
}

fun CrLfSpec.siteLayout() = copySpec {
    // TODO: certain files contain </br>, however it should probably be removed in the source files,
    //       not after conversion to html
    // TODO: generate doap_JMeter.rdf
    textFrom("$xdocs/download_jmeter.cgi", eol = LineEndings.LF)
    into("api") {
        with(javadocs())
    }
    from(processSiteXslt)
    with(docCssAndImages())
    with(manuals())
}

val previewSite by tasks.registering(Copy::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Creates preview of a site to build/docs/site"
    into("$buildDir/site")
    with(CrLfSpec().siteLayout())
}

val distributionGroup = "distribution"
val baseFolder = "apache-jmeter-${rootProject.version}"

fun CrLfSpec.javadocs() = copySpec {
    textFrom(javadocAggregate) {
        text("**/*") // all except binary
        binary("**/*.zip", "**/*.png")
    }
}

fun CrLfSpec.binaryLayout() = copySpec {
    into(baseFolder) {
        with(commonFiles(layoutType = "binary"))
        textFrom(rootDir) {
            text("README.md")
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
        with(printableDocumentation())
        into("docs/api") {
            with(javadocs())
        }
    }
}

fun CrLfSpec.sourceLayout() = copySpec {
    into(baseFolder) {
        with(commonFiles(layoutType = "source"))
        // Even though we do not produce a git repository, .gitattributes is used in build scripts
        // to produce "preview site" repository
        textFrom("$rootDir/.gitattributes")
        into("gradle") {
            textFrom("$rootDir/gradle") {
                text("**/*.kts", "**/*.properties")
                binary("wrapper/gradle-wrapper.jar")
            }
        }
        textFrom(rootDir) {
            text("*.kts", "*.md", "*.yml", "*.xml", "*.xsl")
            text("rat-excludes.txt")
            shell("gradlew")
            exclude(".codecov.yml", ".travis.yml")
        }
        into("buildSrc") {
            textFrom("$rootDir/buildSrc") {
                text("**/*.kts", "**/*.kt", "**/*.properties")
                exclude("build", ".gradle")
                exclude("subprojects/*/build")
            }
        }
        into("config") {
            textFrom("$rootDir/config") {
                text("**/*.xml")
                text("**/*.regex")
            }
        }
        into("src") {
            filteringCharset = "Cp1252"
            textFrom("$rootDir/src") {
                text("**/*cp1252*")
                exclude("*/build", "*/out")
                exclude("protocol/*/build", "protocol/*/out")
            }
        }
        into("bin/testfiles") {
            textFrom("$rootDir/bin/testfiles") {
                text("**/*.xml", "**/*.xsd", "**/*.dtd", "**/*.csv", "**/*.txt", "**/*.tsv", "**/*.json")
                text("**/*.html", "**/*.htm", "**/*.css")
                text("**/*.jmx", "**/*.jtl")
                text("**/*.bsh")
                text("**/*.all", "**/*.set") // e.g. HTMLParserTestFile_2.all
                text("**/*.properties")
                exclude("testReport*")
            }
        }
        into("src") {
            textFrom("$rootDir/src") {
                text("**/*.java", "**/*.groovy", "**/*.kts")
                // resources
                text("**/*.properties")
                text("**/*.html", "**/*.htm", "**/*.css", "**/*.svg")
                text("**/*.xml", "**/*.dtd", "**/*.csv", "**/*.txt", "**/*.jmx")
                text("**/*.yml")
                text("**/*.eml", "**/*.pem")
                text("**/*.all", "**/*.set") // e.g. HTMLParserTestCase.all
                text("**/.git*", "**/.git*")
                binary("**/*.png", "**/*.gif", "**/*.jpg")
                // Default encoding is UTF-8, so cp1252 files are included above
                exclude("**/*cp1252*")
                exclude("**/jmeter.log")
                exclude("**/*.iml")
                exclude("*/build", "*/out")
                exclude("protocol/*/build", "protocol/*/out")
            }
        }
        into("xdocs") {
            textFrom("$rootDir/xdocs") {
                text("**/*.html", "**/*.htm", "**/*.css", "**/*.svg")
                text("**/*.xml", "**/*.dtd", "**/*.csv", "**/*.txt", "**/*.jmx")
                text("**/*.txt", "**/*.TXT")
                text("**/*.properties")
                text("**/*.vsl", "**/*.xsl", "**/*.xsd")
                binary("**/*") // All the rest as binary
                exclude("**/jmeter.log")
            }
        }
    }
}

val javadocAggregate by tasks.registering(Javadoc::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Generates aggregate javadoc for all the artifacts"

    classpath = files(jars.map { project(it).configurations.compileClasspath })
    setSource(jars.flatMap { project(it).sourceSets.main.get().allJava })
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
            // Gradle defaults to the following pattern, and JMeter was using apache-jmeter-5.1_src.zip
            // [baseName]-[appendix]-[version]-[classifier].[extension]
            archiveBaseName.set("apache-jmeter-${rootProject.version}${if (type == "source") "_src" else ""}")
            CrLfSpec(eol).run {
                with(if (type == "source") sourceLayout() else binaryLayout())
            }
            doLast {
                ant.withGroovyBuilder {
                    "checksum"("file" to archiveFile.get(),
                        "algorithm" to "SHA-512",
                        "fileext" to ".sha512")
                }
            }
        }
        val archiveTasks = mutableListOf<Task>(archiveTask.get())
        configure<SigningExtension> {
            archiveTasks.addAll(sign(archiveTasks.first()))
        }
        tasks.named(BasePlugin.ASSEMBLE_TASK_NAME).configure {
            dependsOn(archiveTasks)
        }
        rootProject.configure<ReleaseExtension> {
            archives.addAll(archiveTask)
        }
    }
}

val cleanWs by tasks.registering() {
    doLast {
        val wsDir = "$buildDir/cleanWs"
//        project.exec {
//            commandLine("git", "clone", "--depth", "100", "--reference", "$rootDir", "https://github.com/apache/jmeter.git", wsDir)
//            standardOutput = System.out
//        }
        project.exec {
            workingDir = file(wsDir)
//            commandLine("/bin/sh", "gradlew", "check")
            commandLine("./gradlew", "check")
            standardOutput = System.out
        }
    }
}

rootProject.configure<ReleaseExtension> {
    previewSiteContents.add(
        CrLfSpec().run {
            copySpec {
                into("site") {
                    with(siteLayout())
                }
            }
        })
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
        }
    }
}
