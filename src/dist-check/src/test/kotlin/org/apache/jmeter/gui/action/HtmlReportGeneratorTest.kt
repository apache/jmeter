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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.Paths
import java.text.MessageFormat

class HtmlReportGeneratorTest : JMeterTestCase() {
    @TempDir
    lateinit var testDirectory: File

    data class CheckArgumentsCase(val csvPath: String, val userPropertiesPath: String, val outputDirectoryPath: String, val expected: List<String>)

    companion object {
        /**
         * Combine the given path parts to one path with the correct path separator of the current platform.
         * The current JMeter bin directory will be prepended to the path.
         *
         * @param paths to be combined (should contain no path separators)
         * @return combined path as string
         */
        fun combine(vararg paths: String) =
            Paths.get(JMeterUtils.getJMeterBinDir(), *paths).toString()

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
            CheckArgumentsCase(
                "",
                "",
                "",
                listOf(
                    JMeterUtils.getResString("generate_report_ui.csv_file") +
                        MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NO_FILE), ""),
                    JMeterUtils.getResString("generate_report_ui.user_properties_file") +
                        MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NO_FILE), ""),
                    JMeterUtils.getResString("generate_report_ui.output_directory") +
                        MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.CANNOT_CREATE_DIRECTORY), "")
                )
            ),
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
}
