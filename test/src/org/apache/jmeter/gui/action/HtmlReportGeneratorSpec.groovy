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

package org.apache.jmeter.gui.action;

import spock.lang.IgnoreIf
import spock.lang.Unroll

import org.apache.commons.io.FileUtils
import org.apache.jmeter.junit.spock.JMeterSpec
import org.apache.jmeter.util.JMeterUtils

import com.fasterxml.jackson.annotation.JsonMerge
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class HtmlReportGeneratorSpec extends JMeterSpec{

    def "check if generation contains the right file error"(){
        setup:
        File testDirectory = new File(JMeterUtils.getJMeterBinDir() + "/testfiles/testReport");
        if(testDirectory.exists()) {
            if (testDirectory.list().length>0) {
                FileUtils.cleanDirectory(testDirectory);
            }
        } else {
            testDirectory.mkdir();
        }
        when:
        HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(csvPath, userPropertiesPath, outputDirectoryPath)
        List<String> resultList = htmlReportGenerator.run()
        then:
        resultList.equals(expected)
        where:
        csvPath                                                           | userPropertiesPath                                        | outputDirectoryPath                                   || expected
        ""                                                                | ""                                                        | ""                                                    || [
            JMeterUtils.getResString("csv_file")+JMeterUtils.getResString("no_such_file"),
            JMeterUtils.getResString("user_properties_file")+JMeterUtils.getResString("no_such_file"),
            JMeterUtils.getResString("output_directory")+JMeterUtils.getResString("no_such_directory")
        ]
        JMeterUtils.getJMeterBinDir()+"/testfiles/XPathTest2.xml"         | JMeterUtils.getJMeterBinDir()+"/testfiles/XPathTest2.xml" | JMeterUtils.getJMeterBinDir()+"/testfiles"            || [
            JMeterUtils.getResString("csv_file")+JMeterUtils.getResString("wrong_type"),
            JMeterUtils.getResString("user_properties_file")+JMeterUtils.getResString("wrong_type"),
            JMeterUtils.getResString("output_directory")+JMeterUtils.getResString("directory_not_empty")
        ]
        JMeterUtils.getJMeterBinDir()+"/testfiles/HTMLReportTestFile.csv" | JMeterUtils.getJMeterBinDir()+"/user.properties"          | JMeterUtils.getJMeterBinDir()+"/testfiles/testReport" || []
    }
    
    def "check if generation is correct"(){
        setup:
        File testDirectory = new File(JMeterUtils.getJMeterBinDir() + "/testfiles/testReport");
        if(testDirectory.exists()) {
            if (testDirectory.list().length>0) {
                FileUtils.cleanDirectory(testDirectory);
            }
        } else {
            testDirectory.mkdir();
        }
        when:
        HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(
                JMeterUtils.getJMeterBinDir() + "/testfiles/HTMLReportTestFile.csv",
                JMeterUtils.getJMeterBinDir() + "/user.properties", JMeterUtils.getJMeterBinDir() + "/testfiles/testReport");
        then:
        new ArrayList<String>().equals(htmlReportGenerator.run())
        ObjectMapper mapper = new ObjectMapper();
        FileReader jsonFileReader = new FileReader(JMeterUtils.getJMeterBinDir() + "/testfiles/testReport/statistics.json");
        JsonNode root = mapper.readTree(jsonFileReader);
        jsonFileReader.close();
        8615 == root.path("Total").path("sampleCount").intValue();
    }
    
    def "check if generation is wrong"(){
        setup:
        File testDirectory = new File(JMeterUtils.getJMeterBinDir() + "/testfiles/testReport");
        if(testDirectory.exists()) {
            if (testDirectory.list().length>0) {
                FileUtils.cleanDirectory(testDirectory);
            }
        } else {
            testDirectory.mkdir();
        }
        when:
        HtmlReportGenerator htmlReportGenerator = new HtmlReportGenerator(
                JMeterUtils.getJMeterBinDir() + "/testfiles/HTMLReportFalseTestFile.csv",
                JMeterUtils.getJMeterBinDir() + "/user.properties", JMeterUtils.getJMeterBinDir() + "/testfiles/testReport");
        then:
        htmlReportGenerator.run().get(0).contains("An error occurred:");
        testDirectory.list().length == 0
    }
}
