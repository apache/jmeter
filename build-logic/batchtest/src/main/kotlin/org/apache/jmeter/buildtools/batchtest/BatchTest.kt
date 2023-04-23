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

package org.apache.jmeter.buildtools.batchtest

import org.eclipse.jgit.diff.DiffAlgorithm
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.diff.RawText
import org.eclipse.jgit.diff.RawTextComparator
import org.eclipse.jgit.util.io.AutoCRLFInputStream
import org.gradle.api.GradleException
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.property
import java.io.File
import java.net.InetAddress
import javax.inject.Inject

open class BatchTest @Inject constructor(objects: ObjectFactory) : JavaExec() {
    companion object {
        const val BATCH_TESTS_GROUP_NAME = "Batch test"
    }

    @Input
    val ignoreErrorLogs = objects.property<Boolean>().convention(false)

    @Input
    val testName = objects.property<String>()

    @InputDirectory
    @IgnoreEmptyDirectories
    val inputDirectory = objects.directoryProperty().convention(
        project.rootProject.layout.projectDirectory.dir("bin/testfiles")
    )

    @InputFile
    val batchProperties = objects.fileProperty().convention(
        project.rootProject.layout.projectDirectory.file("bin/testfiles/jmeter-batch.properties")
    )

    @InputFile
    val log4jXml = objects.fileProperty().convention(
        project.rootProject.layout.projectDirectory.file("bin/testfiles/log4j2-batch.xml")
    )

    @Input
    val userLanguage = objects.property<String>().convention("en")

    @Input
    val userRegion = objects.property<String>().convention("en")

    @Input
    val userCountry = objects.property<String>().convention("US")

    @InputFile
    val jmx = objects.fileProperty()
        .convention(inputDirectory.file(testName.map { "$it.jmx" }))

    @Internal
    val outputDirectory = objects.directoryProperty()
        .convention(project.rootProject.layout.projectDirectory.dir("bin"))

    @Internal
    val logFile = objects.fileProperty()
        .convention(outputDirectory.file(testName.map { "$it.log" }))

    @Internal
    val jtlFile = objects.fileProperty()
        .convention(outputDirectory.file(testName.map { "$it.jtl" }))

    @Internal
    val csvFile = objects.fileProperty()
        .convention(outputDirectory.file(testName.map { "$it.csv" }))

    @Internal
    val xmlFile = objects.fileProperty()
        .convention(outputDirectory.file(testName.map { "$it.xml" }))

    @Internal
    val errFile = objects.fileProperty()
        .convention(outputDirectory.file(testName.map { "$it.err" }))

    // Re-run the task when jar contents is changed
    @get:InputFiles
    val jars
        get() = project.rootProject.layout.projectDirectory.dir("lib").asFileTree

    @get:InputFile
    val jmeterJar
        get() = project.rootProject.layout.projectDirectory.dir("bin").file("ApacheJMeter.jar")

    init {
        group = BATCH_TESTS_GROUP_NAME
        description = "Runs jmx file via process fork and verifies outputs"
        workingDir = File(project.rootDir, "bin")
        mainClass.set("org.apache.jmeter.NewDriver")
        classpath(jmeterJar)

        // This does not depend on the task configuration, so the properties are initialized early
        // It enables to override the properties later (e.g. in the build script)
        maxHeapSize = "128m"
        jvmArgs("-Xss256k", "-XX:MaxMetaspaceSize=128m")
        systemProperty("java.rmi.server.hostname", InetAddress.getLocalHost().canonicalHostName)
        systemProperty("java.awt.headless", "true")
    }

    fun jmeterArgument(name: String, value: String) {
        args("-J$name=$value")
    }

    private fun deleteWorkfiles() {
        project.delete(csvFile, xmlFile, logFile, jtlFile, errFile)
    }

    private fun File.readAsCrLf() =
        inputStream().use { AutoCRLFInputStream(it, false).readBytes() }

    fun compareFiles(summary: MutableList<String>, actualFile: File): Boolean {
        val actual = actualFile.readAsCrLf()
        val fileName = actualFile.name
        val expectedFile = inputDirectory.file(fileName).get().asFile
        val expected = expectedFile.readAsCrLf()
        if (expected.contentEquals(actual)) {
            return true
        }

        summary.add("unexpected output $fileName")
        println("ERROR: unexpected output for $fileName:")
        println("  - expected ${expectedFile.length()} bytes, $expectedFile")
        println("  + actual ${actualFile.length()} bytes, $actualFile")

        val e = RawText(expected)
        val a = RawText(actual)
        val diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM)
        val edits = diffAlgorithm.diff(RawTextComparator.DEFAULT, e, a)
        DiffFormatter(System.out).format(edits, e, a)
        return false
    }

    override fun exec() {
        systemProperty("user.language", userLanguage.get())
        systemProperty("user.region", userRegion.get())
        systemProperty("user.country", userCountry.get())
        args("-pjmeter.properties")
        args("-q", batchProperties.get())
        args("-n")
        args("-t", jmx.get())
        args("-i", log4jXml.get())
        args("-j", logFile.get())
        args("-l", jtlFile.get())
        // Check properties can be passed to local/remote tests
        args("-Jmodule=Module")
        args("-Gmodule=Module")
        // Check property can be used for filenames in local/remote tests (no need to defined as -G)
        args("-JCSVFILE=${csvFile.get()}")

        deleteWorkfiles()

        super.exec()

        val summary = mutableListOf<String>()
        checkErrors(summary)

        if (summary.isNotEmpty()) {
            throw GradleException("Failures detected while testing ${jmx.get()}: $summary")
        }
        deleteWorkfiles()
    }

    protected open fun checkErrors(summary: MutableList<String>) {
        compareFiles(summary, csvFile.get().asFile)
        compareFiles(summary, xmlFile.get().asFile)
        val log = logFile.get().asFile
        catLogFile(log, summary, ignoreErrorLogs.get())
    }

    protected fun catLogFile(
        log: File,
        summary: MutableList<String>,
        ignoreErrors: Boolean = false
    ) {
        if (log.length() == 0L) {
            println("No errors present in the logfile $log (the file is empty)")
            return
        }
        if (ignoreErrors) {
            println("WARNING: the task was configured to ignore errors in the output log")
        } else {
            summary.add("errors in ${log.name}")
        }
        println("Logfile contents (errors during the test, see $log):")
        log.inputStream().use { it.copyTo(System.out) }
    }
}
