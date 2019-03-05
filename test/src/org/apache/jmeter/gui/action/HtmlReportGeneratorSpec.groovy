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

import spock.lang.IgnoreIf
import spock.lang.Unroll

import java.text.MessageFormat

import org.apache.commons.io.FileUtils
import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jmeter.util.JMeterUtils

import com.fasterxml.jackson.annotation.JsonMerge
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class HtmlReportGeneratorSpec extends JMeterSpec{

    def "check if generation from csv: '#csvPath' with properties: '#userPropertiesPath' in folder: '#outputDirectoryPath' contains the expected error"(){
        when:
            HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(csvPath, userPropertiesPath, outputDirectoryPath)
            List<String> resultList = htmlReportGenerator.checkArguments()
        then:
            resultList.equals(expected)
        where:
            csvPath                                                           | userPropertiesPath                                        | outputDirectoryPath                                   | expected
            JMeterUtils.getJMeterBinDir()+"/testfiles/HTMLReportTestFile.csv" | JMeterUtils.getJMeterBinDir()+"/user.properties"          | JMeterUtils.getJMeterBinDir()+"/testfiles/testReport" | []
            JMeterUtils.getJMeterBinDir()+"/testfiles/HTMLReportTestFile.csv" | JMeterUtils.getJMeterBinDir()+"/user.properties"          | JMeterUtils.getJMeterBinDir()+"/testfiles" | [
                JMeterUtils.getResString("generate_report_ui.output_directory") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NOT_EMPTY_DIRECTORY), outputDirectoryPath),
            ]
            JMeterUtils.getJMeterBinDir()+"/testfiles/HTMLReportTestFileMissing.csv" | JMeterUtils.getJMeterBinDir()+"/user.properties"          | JMeterUtils.getJMeterBinDir()+"/testfiles/testReport" | [
                JMeterUtils.getResString("generate_report_ui.csv_file") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NO_FILE), csvPath)
            ]
            ""                                                                | ""                                                        | ""                                                    | [
                JMeterUtils.getResString("generate_report_ui.csv_file") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NO_FILE), csvPath),
                JMeterUtils.getResString("generate_report_ui.user_properties_file") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.NO_FILE), userPropertiesPath),
                JMeterUtils.getResString("generate_report_ui.output_directory") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.CANNOT_CREATE_DIRECTORY), outputDirectoryPath),
            ]
            JMeterUtils.getJMeterBinDir()+"/testfiles/HTMLReportTestFile.csv" | JMeterUtils.getJMeterBinDir()+"/user.properties" | JMeterUtils.getJMeterBinDir()+"/testfiles/testReport/oneLevel/twolevel"            | [
                JMeterUtils.getResString("generate_report_ui.output_directory") + MessageFormat.format(JMeterUtils.getResString(HtmlReportGenerator.CANNOT_CREATE_DIRECTORY), outputDirectoryPath)
            ]
    }
    
    def "check that report generation succeeds and statistic are generated"(){
        setup:
            File testDirectory = new File(JMeterUtils.getJMeterBinDir(), "/testfiles/testReport")
            if(testDirectory.exists()) {
                if (testDirectory.list().length>0) {
                    FileUtils.cleanDirectory(testDirectory)
                }
            } else {
                testDirectory.mkdir()
            }
        when:
            HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(
                    JMeterUtils.getJMeterBinDir() + "/testfiles/HTMLReportTestFile.csv",
                    JMeterUtils.getJMeterBinDir() + "/user.properties", 
                    JMeterUtils.getJMeterBinDir() + "/testfiles/testReport")
            List<String> resultList = htmlReportGenerator.run()
            File statistics = new File(JMeterUtils.getJMeterBinDir(), "/testfiles/testReport/statistics.json")
            ObjectMapper mapper = new ObjectMapper()
            JsonNode root =null;
            if (statistics.exists()) {
                statistics.withReader { jsonFileReader -> 
                    root = mapper.readTree(jsonFileReader)
                }
            }
        then:
            resultList.isEmpty()
            statistics.exists()
            8615 == root.path("Total").path("sampleCount").intValue()
        cleanup:
            if(testDirectory.exists()) {
                if (testDirectory.list().length>0) {
                    FileUtils.cleanDirectory(testDirectory)
                }
            }
    }
    
    def "check that report generation fails when format does not match and error is reported"(){
        setup:
            File testDirectory = new File(JMeterUtils.getJMeterBinDir(),"/testfiles/testReportThatShouldBeEmpty")
            if(testDirectory.exists()) {
                if (testDirectory.list().length>0) {
                    FileUtils.cleanDirectory(testDirectory)
                }
            } else {
                testDirectory.mkdir()
            }
        when:
            HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(
                JMeterUtils.getJMeterBinDir() + "/testfiles/HTMLReportFalseTestFile.csv",
                JMeterUtils.getJMeterBinDir() + "/user.properties", 
                JMeterUtils.getJMeterBinDir() + "/testfiles/testReport")
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
