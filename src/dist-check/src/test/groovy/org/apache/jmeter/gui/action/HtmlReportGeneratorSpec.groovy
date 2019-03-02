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

package org.apache.jmeter.gui.action

import java.nio.file.Paths
import java.net.URL;
import java.text.MessageFormat

import org.apache.commons.io.FileUtils
import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jmeter.util.JMeterUtils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class HtmlReportGeneratorSpec extends JMeterSpec{

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

    def "check if generation from csv: '#csvPath' with properties: '#userPropertiesPath' in folder: '#outputDirectoryPath' contains the expected error"(){
        when:
            HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(csvPath, userPropertiesPath, outputDirectoryPath)
            List<String> resultList = htmlReportGenerator.checkArguments()
        then:
            resultList.equals(expected)
        where:
            csvPath                                               | userPropertiesPath                  | outputDirectoryPath                     | expected
            combine("testfiles", "HTMLReportTestFile.csv")        | combine("user.properties")          | combine("testfiles", "testReport")      | []
            combine("testfiles", "HTMLReportTestFile.csv")        | combine("user.properties")          | combine("testfiles")                    | [
                JMeterUtils.getResString("generate_report_ui.output_directory") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NOT_EMPTY_DIRECTORY), outputDirectoryPath)
            ]
            combine("testfiles", "HTMLReportTestFileMissing.csv") | combine("user.properties")          | combine("testfiles", "testReport")      | [
                JMeterUtils.getResString("generate_report_ui.csv_file") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NO_FILE), csvPath)
            ]
            ""                                                    | ""                                  | ""                                      | [
                JMeterUtils.getResString("generate_report_ui.csv_file") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NO_FILE), csvPath),
                JMeterUtils.getResString("generate_report_ui.user_properties_file") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NO_FILE), userPropertiesPath),
                JMeterUtils.getResString("generate_report_ui.output_directory") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.CANNOT_CREATE_DIRECTORY), outputDirectoryPath)
            ]
            combine("testfiles", "HTMLReportTestFile.csv")        | combine("user.properties")          | combine("testfiles", "testReport", "oneLevel", "twolevel") | [
                JMeterUtils.getResString("generate_report_ui.output_directory") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.CANNOT_CREATE_DIRECTORY), outputDirectoryPath)
            ]
    }

    def "check that report generation succeeds and statistic are generated"(){
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
            URL expected = HtmlReportGenerator.class.getResource("/org/apache/jmeter/gui/report/HTMLReportExpect.json");
            JsonNode expectedRoot = null;
            expected.withReader { jsonFileReader ->
                expectedRoot = mapper.readTree(jsonFileReader)
            }
        when:
            HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(
                    combine("testfiles", "HTMLReportTestFile.csv"),
                    combine("user.properties"),
                    testDirectory.toString())
            List<String> resultList = htmlReportGenerator.run()
            File statistics = new File(combine("testfiles", "testReport", "statistics.json"))
            JsonNode actualRoot = null;
            if (statistics.exists()) {
                statistics.withReader { jsonFileReader ->
                    actualRoot = mapper.readTree(jsonFileReader)
                }
            }
        then:
            resultList.isEmpty()
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
            HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(
                combine("testfiles", "HTMLReportFalseTestFile.csv"),
                combine("user.properties"),
                testDirectory.toString())
            List<String> resultList = htmlReportGenerator.run()
        then:
            testDirectory.list().length == 0
            resultList.get(0).contains("An error occurred: Error while processing samples:Consumer failed with message")
        cleanup:
            if(testDirectory.exists()) {
                if (testDirectory.list().length>0) {
                    FileUtils.cleanDirectory(testDirectory)
                }
            }
    }
}
