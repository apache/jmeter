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
package org.apache.jmeter.testelement;

import java.io.File;

import javax.swing.JComponent;

import org.apache.jmeter.report.DataSet;
import org.apache.jmeter.save.SaveGraphicsService;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class LineGraphTest extends JMeterTestCase {

    private static final Logger log = LoggingManager.getLoggerForClass();

    /**
     * @param arg0
     */
    public LineGraphTest(String arg0) {
        super(arg0);
    }

    public void testGenerateLineChart() {
        log.info("jtl version=" + JMeterUtils.getProperty("file_format.testlog"));
        // String sampleLog = "C:/eclipse3/workspace/jmeter-21/bin/testfiles/sample_log1.jtl";
        String sampleLog = findTestPath("testfiles/sample_log1.jtl");
        String sampleLog2 = findTestPath("testfiles/sample_log1b.jtl");
        String sampleLog3 = findTestPath("testfiles/sample_log1c.jtl");
        JTLData input = new JTLData();
        JTLData input2 = new JTLData();
        JTLData input3 = new JTLData();
        input.setDataSource(sampleLog);
        input.loadData();
        input2.setDataSource(sampleLog2);
        input2.loadData();
        input3.setDataSource(sampleLog3);
        input3.loadData();

        assertTrue((input.getStartTimestamp() > 0));
        assertTrue((input.getEndTimestamp() > input.getStartTimestamp()));
        assertTrue((input.getURLs().size() > 0));
        log.info("URL count=" + input.getURLs().size());
        java.util.ArrayList<DataSet> list = new java.util.ArrayList<DataSet>();
        list.add(input);
        list.add(input2);
        list.add(input3);

        LineChart lgraph = new LineChart();
        lgraph.setTitle("Sample Line Graph");
        lgraph.setCaption("Sample");
        lgraph.setName("Sample");
        lgraph.setYAxis("milliseconds");
        lgraph.setYLabel("Test Runs");
        lgraph.setXAxis(AbstractTable.REPORT_TABLE_MAX);
        lgraph.setXLabel(AbstractChart.X_DATA_FILENAME_LABEL);
        lgraph.setURLs("jakarta_home,jmeter_home");
        JComponent gr = lgraph.renderChart(list);
        assertNotNull(gr);
        SaveGraphicsService serv = new SaveGraphicsService();
        String filename = lgraph.getTitle();
        filename = filename.replace(' ','_');
        if (!"true".equalsIgnoreCase(System.getProperty("java.awt.headless"))){
            String outPfx = findTestPath("./testfiles/" + filename);
            serv.saveJComponent(outPfx,SaveGraphicsService.PNG,gr);
            assertTrue("Should have created file",new File(outPfx+".png").exists());
        }
    }
}
