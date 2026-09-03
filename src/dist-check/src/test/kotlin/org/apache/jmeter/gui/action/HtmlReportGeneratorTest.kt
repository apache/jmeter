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

package org.apache.jmeter.gui.action

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.util.JMeterUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.Path
import java.text.MessageFormat

class HtmlReportGeneratorTest : JMeterTestCase() {
    @TempDir
    lateinit var testDirectory: File

    data class CheckArgumentsCase(val csvPath: String, val userPropertiesPath: String, val outputDirectoryPath: String, val expected: List<String>)

    /**
     * Assert that a file exists at the given path relative to a base directory
     */
    private fun assertFileExists(baseDir: File, relativePath: String, message: String? = null) {
        val file = File(baseDir, relativePath)
        assertTrue(file.exists()) { message ?: "$relativePath should exist" }
    }

    /**
     * Assert that a file does NOT exist at the given path relative to a base directory
     */
    private fun assertFileNotExists(baseDir: File, relativePath: String, message: String? = null) {
        val file = File(baseDir, relativePath)
        assertFalse(file.exists()) { message ?: "$relativePath should NOT exist" }
    }

    companion object {
        /**
         * Combine the given path parts to one path with the correct path separator of the current platform.
         * The current JMeter bin directory will be prepended to the path.
         *
         * @param paths to be combined (should contain no path separators)
         * @return combined path as string
         */
        fun combine(vararg paths: String) =
            Path.of(JMeterUtils.getJMeterBinDir(), *paths).toString()

        @JvmStatic
        fun checkArgumentsCases() = listOf(
            CheckArgumentsCase(
                combine("testfiles", "HTMLReportTestFile.csv"),
                combine("user.properties"),
                combine("testfiles", "testReport"),
                listOf()
            ),
            combine("testfiles").let { outputDirectoryPath ->
                CheckArgumentsCase(
                    combine("testfiles", "HTMLReportTestFile.csv"),
                    combine("user.properties"),
                    outputDirectoryPath,
                    listOf(
                        JMeterUtils.getResString("generate_report_ui.output_directory") +
                            MessageFormat.format(
                                JMeterUtils.getResString(HtmlReportGenerator.NOT_EMPTY_DIRECTORY),
                                outputDirectoryPath
                            )
                    )
                )
            },
            combine("testfiles", "HTMLReportTestFileMissing.csv").let { csvPath ->
                CheckArgumentsCase(
                    csvPath,
                    combine("user.properties"),
                    combine("testfiles", "testReport"),
                    listOf(
                        JMeterUtils.getResString("generate_report_ui.csv_file") +
                            MessageFormat.format(
                                JMeterUtils.getResString(HtmlReportGenerator.NO_FILE),
                                csvPath
                            )
                    )
                )
            },
            combine("testfiles", "testReport", "oneLevel", "twolevel").let { outputDirectoryPath ->
                CheckArgumentsCase(
                    combine("testfiles", "HTMLReportTestFile.csv"),
                    combine("user.properties"),
                    outputDirectoryPath,
                    listOf(
                        JMeterUtils.getResString("generate_report_ui.output_directory") +
                            MessageFormat.format(
                                JMeterUtils.getResString(HtmlReportGenerator.CANNOT_CREATE_DIRECTORY),
                                outputDirectoryPath
                            )
                    )
                )
            },
        )
    }

    @ParameterizedTest
    @MethodSource("checkArgumentsCases")
    fun checkArguments(case: CheckArgumentsCase) {
        val htmlReportGenerator = HtmlReportGenerator(case.csvPath, case.userPropertiesPath, case.outputDirectoryPath)
        val resultList = htmlReportGenerator.checkArguments()

        assertEquals(case.expected, resultList, "resultList")
    }

    @Test
    fun `check that report generation succeeds and statistic are generated`() {
        val mapper = ObjectMapper()
        val expected = HtmlReportGenerator::class.java.getResource("/org/apache/jmeter/gui/report/HTMLReportExpect.json")
        val expectedRoot = mapper.readTree(expected)
        val htmlReportGenerator = HtmlReportGenerator(
            combine("testfiles", "HTMLReportTestFile.csv"),
            combine("user.properties"),
            testDirectory.toString()
        )
        val resultList = htmlReportGenerator.run()
        val statistics = File(testDirectory, "statistics.json")
        val actualRoot = mapper.readTree(statistics)

        assertEquals(expectedRoot, actualRoot, "test report json file")
    }

    @Test
    fun `report generation fails when format does not match and error is reported`() {
        val htmlReportGenerator = HtmlReportGenerator(
            combine("testfiles", "HTMLReportFalseTestFile.csv"),
            combine("user.properties"),
            testDirectory.toString()
        )
        val resultList = htmlReportGenerator.run()
        assertEquals("[]", testDirectory.list()?.contentDeepToString(), "testDirectory contents")
        val firstMessage = resultList.firstOrNull()
        val expectedError = "An error occurred: Error while processing samples: Consumer failed with message"
        if (firstMessage?.contains(expectedError) != true) {
            fail("First result message should contain '$expectedError', but was '$firstMessage'")
        }
    }

    @Test
    fun `report generation creates correct directory structure for HTML and JS files`() {
        val htmlReportGenerator = HtmlReportGenerator(
            combine("testfiles", "HTMLReportTestFile.csv"),
            combine("user.properties"),
            testDirectory.toString()
        )
        htmlReportGenerator.run()

        // Verify directory structure exists
        assertFileExists(testDirectory, "content")
        assertFileExists(testDirectory, "content/pages")
        assertFileExists(testDirectory, "content/js")

        // Verify HTML pages are in correct location (content/pages/)
        assertFileExists(testDirectory, "content/pages/OverTime.html")
        assertFileExists(testDirectory, "content/pages/ResponseTimes.html")
        assertFileExists(testDirectory, "content/pages/Throughput.html")
        assertFileExists(testDirectory, "content/pages/CustomsGraphs.html")

        // Verify JavaScript files are in correct location (content/js/)
        assertFileExists(testDirectory, "content/js/dashboard.js")
        assertFileExists(testDirectory, "content/js/graph.js")
        assertFileExists(testDirectory, "content/js/dashboard-commons.js")
        assertFileExists(testDirectory, "content/js/customGraph.js")

        // Verify files are NOT at root level (catches the bug!)
        assertFileNotExists(testDirectory, "OverTime.html", "OverTime.html should NOT be at root level")
        assertFileNotExists(testDirectory, "ResponseTimes.html", "ResponseTimes.html should NOT be at root level")
        assertFileNotExists(testDirectory, "Throughput.html", "Throughput.html should NOT be at root level")
        assertFileNotExists(testDirectory, "CustomsGraphs.html", "CustomsGraphs.html should NOT be at root level")
        assertFileNotExists(testDirectory, "dashboard.js", "dashboard.js should NOT be at root level")
        assertFileNotExists(testDirectory, "graph.js", "graph.js should NOT be at root level")

        // Verify index.html is at root (this should be correct)
        assertFileExists(testDirectory, "index.html", "index.html should exist at root level")
    }
}
