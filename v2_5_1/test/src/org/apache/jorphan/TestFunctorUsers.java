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

package org.apache.jorphan;

import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.ldap.config.gui.LDAPArgumentsPanel;
import org.apache.jmeter.visualizers.StatGraphVisualizer;
import org.apache.jmeter.visualizers.StatVisualizer;
import org.apache.jmeter.visualizers.SummaryReport;
import org.apache.jmeter.visualizers.TableVisualizer;

/*
 * Unit tests for classes that use Functors
 * 
 */
public class TestFunctorUsers extends JMeterTestCase {

    public TestFunctorUsers(String arg0) {
        super(arg0);
    }
    
    @SuppressWarnings("deprecation")
    public void testSummaryReport() throws Exception{
        assertTrue("SummaryReport Functor",SummaryReport.testFunctors());
    }
    
    public void testTableVisualizer() throws Exception{
        assertTrue("TableVisualizer Functor",TableVisualizer.testFunctors());
    }
    
    public void testStatGraphVisualizer() throws Exception{
        assertTrue("StatGraphVisualizer Functor",StatGraphVisualizer.testFunctors());
    }
    
    @SuppressWarnings("deprecation")
    public void testStatVisualizer() throws Exception{
        assertTrue("StatVisualizer Functor",StatVisualizer.testFunctors());
    }
    
    public void testArgumentsPanel() throws Exception{
        assertTrue("ArgumentsPanel Functor",ArgumentsPanel.testFunctors());
    }
    
    public void testHTTPArgumentsPanel() throws Exception{
        assertTrue("HTTPArgumentsPanel Functor",HTTPArgumentsPanel.testFunctors());
    }
    
    public void testLDAPArgumentsPanel() throws Exception{
        assertTrue("LDAPArgumentsPanel Functor",LDAPArgumentsPanel.testFunctors());
    }
}
