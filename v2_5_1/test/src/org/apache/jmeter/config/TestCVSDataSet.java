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

/**
 * Package to test FileServer methods 
 */
     
package org.apache.jmeter.config;

import java.io.IOException;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

public class TestCVSDataSet extends JMeterTestCase {

    private JMeterVariables threadVars;
    
    public TestCVSDataSet(String arg0) {
        super(arg0);
    }

    @Override
    public void setUp(){
        JMeterContext jmcx = JMeterContextService.getContext();
        jmcx.setVariables(new JMeterVariables());
        threadVars = jmcx.getVariables();        
        threadVars.put("b", "value");
    }

    @Override
    public void tearDown() throws IOException{
        FileServer.getFileServer().closeFiles();
    }
    
    public void testopen() throws Exception {
        CSVDataSet csv = new CSVDataSet();
        csv.setFilename("No.such.filename");
        csv.setVariableNames("a,b,c");
        csv.setDelimiter(",");
        csv.iterationStart(null);
        assertEquals("<EOF>",threadVars.get("a"));
        assertEquals("<EOF>",threadVars.get("b"));
        assertEquals("<EOF>",threadVars.get("c"));

        csv = new CSVDataSet();
        csv.setFilename(findTestPath("testfiles/testempty.csv"));
        csv.setVariableNames("a,b,c");
        csv.setDelimiter(",");
        
        csv.iterationStart(null);
        assertEquals("",threadVars.get("a"));
        assertEquals("b1",threadVars.get("b"));
        assertEquals("c1",threadVars.get("c"));

        csv.iterationStart(null);
        assertEquals("a2",threadVars.get("a"));
        assertEquals("",threadVars.get("b"));
        assertEquals("c2",threadVars.get("c"));

        csv.iterationStart(null);
        assertEquals("a3",threadVars.get("a"));
        assertEquals("b3",threadVars.get("b"));
        assertEquals("",threadVars.get("c"));


        csv.iterationStart(null);
        assertEquals("a4",threadVars.get("a"));
        assertEquals("b4",threadVars.get("b"));
        assertEquals("c4",threadVars.get("c"));
        
        csv.iterationStart(null); // Restart file
        assertEquals("",threadVars.get("a"));
        assertEquals("b1",threadVars.get("b"));
        assertEquals("c1",threadVars.get("c"));
    }
    
    public void testutf8() throws Exception {
    	
        CSVDataSet csv = new CSVDataSet();
        csv.setFilename(findTestPath("testfiles/testutf8.csv"));
        csv.setVariableNames("a,b,c,d");
        csv.setDelimiter(",");
        csv.setQuotedData( true );
        csv.setFileEncoding( "UTF-8" );
        
        csv.iterationStart(null);
        assertEquals("a1",threadVars.get("a"));
        assertEquals("b1",threadVars.get("b"));
        assertEquals("\u00e71",threadVars.get("c"));
        assertEquals("d1",threadVars.get("d"));

        csv.iterationStart(null);
        assertEquals("a2",threadVars.get("a"));
        assertEquals("b2",threadVars.get("b"));
        assertEquals("\u00e72",threadVars.get("c"));
        assertEquals("d2",threadVars.get("d"));

        csv.iterationStart(null);
        assertEquals("a3",threadVars.get("a"));
        assertEquals("b3",threadVars.get("b"));
        assertEquals("\u00e73",threadVars.get("c"));
        assertEquals("d3",threadVars.get("d"));

        csv.iterationStart(null);
        assertEquals("a4",threadVars.get("a"));
        assertEquals("b4",threadVars.get("b"));
        assertEquals("\u00e74",threadVars.get("c"));
        assertEquals("d4",threadVars.get("d"));
    }

    // Test CSV file with a header line
    public void testHeaderOpen(){
        CSVDataSet csv = new CSVDataSet();
        csv.setFilename(findTestPath("testfiles/testheader.csv"));
        csv.setDelimiter("|");
        assertNull(csv.getVariableNames());
        csv.iterationStart(null);
        assertNull(threadVars.get("a"));
        assertEquals("a1",threadVars.get("A"));
        assertEquals("b1",threadVars.get("B"));
        assertEquals("c1",threadVars.get("C"));
        assertEquals("d1",threadVars.get("D|1"));
        csv.iterationStart(null);
        assertNull(threadVars.get("a"));
        assertEquals("a2",threadVars.get("A"));
        assertEquals("b2",threadVars.get("B"));
        assertEquals("c2",threadVars.get("C"));
        assertEquals("d2",threadVars.get("D|1"));
    }
    
    // Test CSV file with a header line and recycle is true
    public void testHeaderOpenAndRecycle(){
        CSVDataSet csv = new CSVDataSet();
        csv.setFilename(findTestPath("testfiles/testheader.csv"));
        csv.setDelimiter("|");
        csv.setRecycle(true);
        assertNull(csv.getVariableNames()); // read 1st line
        // read 5 lines + restart to file begin
        csv.iterationStart(null); // line 2
        csv.iterationStart(null); // line 3
        csv.iterationStart(null); // line 4
        csv.iterationStart(null); // line 5
        csv.iterationStart(null); // return to 2nd line (first line is names)
        assertEquals("a1",threadVars.get("A"));
        assertEquals("b1",threadVars.get("B"));
        assertEquals("c1",threadVars.get("C"));
        assertEquals("d1",threadVars.get("D|1"));
    }
    
    private CSVDataSet initCSV(){
        CSVDataSet csv = new CSVDataSet();
        csv.setFilename(findTestPath("testfiles/test.csv"));
        csv.setVariableNames("a,b,c");
        csv.setDelimiter(",");
        return csv;
    }

    public void testShareMode(){
        
        new CSVDataSetBeanInfo(); // needs to be initialised
        CSVDataSet csv0 = initCSV();
        CSVDataSet csv1 = initCSV();
        assertNull(csv1.getShareMode());
        csv1.setShareMode("abc");
        assertEquals("abc",csv1.getShareMode());
        csv1.iterationStart(null);
        assertEquals("a1",threadVars.get("a"));
        csv1.iterationStart(null);
        assertEquals("a2",threadVars.get("a"));
        CSVDataSet csv2 = initCSV();
        csv2.setShareMode("abc");
        assertEquals("abc",csv2.getShareMode());
        csv2.iterationStart(null);
        assertEquals("a3",threadVars.get("a"));        
        csv0.iterationStart(null);
        assertEquals("a1",threadVars.get("a"));        
        csv1.iterationStart(null);
        assertEquals("a4",threadVars.get("a"));
    }
}
