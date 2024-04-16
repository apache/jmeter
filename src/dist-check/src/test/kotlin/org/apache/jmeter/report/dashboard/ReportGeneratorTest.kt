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

package org.apache.jmeter.report.dashboard

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.jmeter.junit.JMeterTestCase
import org.apache.jmeter.util.JMeterUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.Isolated
import java.io.File
import java.nio.file.Paths

@Isolated("modifies shared properties")
class ReportGeneratorTest : JMeterTestCase() {
    @TempDir
    lateinit var testDirectory: File

    /**
     * Combine the given path parts to one path with the correct path separator of the current platform.
     * The current JMeter bin directory will be prepended to the path.
     *
     * @param paths to be combined (should contain no path separators)
     * @return combined path as string
     */
    fun combine(vararg paths: String) =
        Paths.get(JMeterUtils.getJMeterBinDir(), *paths).toString()

    @Test
    fun `check that report generation succeeds and statistics json are generated`() {
        val mapper = ObjectMapper()
        val expected = ReportGenerator::class.java.getResource("/org/apache/jmeter/gui/report/HTMLReportExpect.json")
        val expectedRoot = mapper.readTree(expected)

        JMeterUtils.setProperty("jmeter.reportgenerator.outputdir", testDirectory.absolutePath)
        val reportGenerator = ReportGenerator(
            combine("testfiles", "HTMLReportTestFile.csv"), null
        )
        reportGenerator.generate()
        val statistics = File(testDirectory, "statistics.json")
        val actualRoot = mapper.readTree(statistics)

        assertEquals(expectedRoot, actualRoot, "test report json file")
    }

    @Test
    fun `check that report generation fails when format does not match and error is reported`() {
        assertThrows<GenerationException> {
            val reportGenerator = ReportGenerator(
                combine("testfiles", "HTMLReportFalseTestFile.csv"), null
            )
            reportGenerator.generate()
        }
        assertEquals("[]", testDirectory.list()?.contentDeepToString(), "testDirectory contents")
    }
}
