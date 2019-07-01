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

package org.apache.jmeter.report.dashboard

import java.nio.file.Paths
import java.net.URL;

import org.apache.commons.io.FileUtils
import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jmeter.util.JMeterUtils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class ReportGeneratorSpec extends JMeterSpec{

    /**
     * Combine the given path parts to one path with the correct path separator of the current platform.
     * The current JMeter bin directory will be prepended to the path.
     *
     * @param paths to be combined (should contain no path separators)
     * @return combined path as string
     */
    def combine(String... paths) {
       Paths.get(JMeterUtils.getJMeterBinDir(), paths).toString()
    }

    def "check that report generation succeeds and statistics.json are generated"(){
        setup:
            File testDirectory = new File(combine("testfiles", "testReport"))
            if(testDirectory.exists()) {
                if (testDirectory.list().length>0) {
                    FileUtils.cleanDirectory(testDirectory)
                }
            } else {
                testDirectory.mkdir()
            }
            ObjectMapper mapper = new ObjectMapper()
            URL expected = ReportGenerator.class.getResource("/org/apache/jmeter/gui/report/HTMLReportExpect.json");
            JsonNode expectedRoot = null;
            expected.withReader { jsonFileReader ->
                expectedRoot = mapper.readTree(jsonFileReader)
            }
        when:
            JMeterUtils.setProperty("jmeter.reportgenerator.outputdir", testDirectory.getAbsolutePath())
            ReportGenerator reportGenerator = new ReportGenerator(
                    combine("testfiles", "HTMLReportTestFile.csv"),null)
            reportGenerator.generate()
            File statistics = new File(combine("testfiles", "testReport", "statistics.json"))
            JsonNode actualRoot = null;
            if (statistics.exists()) {
                statistics.withReader { jsonFileReader ->
                    actualRoot = mapper.readTree(jsonFileReader)
                }
            }
        then:
            statistics.exists()
            expectedRoot != null
            expectedRoot == actualRoot
        cleanup:
            if(testDirectory.exists()) {
                if (testDirectory.list().length>0) {
                    FileUtils.cleanDirectory(testDirectory)
                }
            }
    }

    def "check that report generation fails when format does not match and error is reported"(){
        setup:
            File testDirectory = new File(combine("testfiles", "testReportThatShouldBeEmpty"))
            if(testDirectory.exists()) {
                if (testDirectory.list().length>0) {
                    FileUtils.cleanDirectory(testDirectory)
                }
            } else {
                testDirectory.mkdir()
            }
        when:
            ReportGenerator reportGenerator = new ReportGenerator(
                combine("testfiles", "HTMLReportFalseTestFile.csv"), null)
            reportGenerator.generate()
        then:
            thrown(GenerationException)
            testDirectory.list().length == 0
        cleanup:
            if(testDirectory.exists()) {
                if (testDirectory.list().length>0) {
                    FileUtils.cleanDirectory(testDirectory)
                }
            }
    }
}
