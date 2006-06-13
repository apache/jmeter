/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

    public TestCVSDataSet() {
        super();
    }

    public TestCVSDataSet(String arg0) {
        super(arg0);
    }

    public void tearDown() throws IOException{
    	FileServer.getFileServer().closeFiles();
    }
    
    public void testopen() throws Exception {
		JMeterContext jmcx = JMeterContextService.getContext();
		jmcx.setVariables(new JMeterVariables());
		JMeterVariables threadVars = jmcx.getVariables();
    	threadVars.put("b", "value");

    	CSVDataSet csv = new CSVDataSet();
    	csv.setFilename("No.such.filename");
    	csv.setVariableNames("a,b,c");
    	csv.setDelimiter(",");
    	csv.iterationStart(null);
		assertNull(threadVars.get("a"));
		assertEquals("value",threadVars.get("b"));
		assertNull(threadVars.get("c"));

		csv = new CSVDataSet();
    	csv.setFilename("testfiles/testempty.csv");
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
}
