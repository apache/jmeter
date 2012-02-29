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
 */
package org.apache.jmeter.monitor.model;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.junit.JMeterTestCase;

public class TestObjectFactory extends JMeterTestCase {

    private ObjectFactory of;
    
    private Status status;

    @Override
    public void setUp(){
        of = ObjectFactory.getInstance();
    }


    public void testStatus() throws Exception {
        status = of.parseString("<status></status>");
        assertNotNull(status);
    }

    public void testNoStatus() throws Exception {
        status = of.parseString("<a></a>");
        assertNull(status);
    }

    public void testFileData() throws Exception {
        byte[] bytes= FileUtils.readFileToByteArray(findTestFile("testfiles/monitorStatus.xml"));
        status = of.parseBytes(bytes);
        checkResult();
    }
    
    public void testStringData() throws Exception {
        String content = FileUtils.readFileToString(findTestFile("testfiles/monitorStatus.xml"));
        status = of.parseString(content);
        checkResult();
    }
    
    private void checkResult(){
        assertNotNull(status);
        final Jvm jvm = status.getJvm();
        assertNotNull(jvm);
        final Memory memory = jvm.getMemory();
        assertNotNull(memory);
        assertEquals(10807352, memory.getFree());
        assertEquals(16318464, memory.getTotal());
        assertEquals(259522560, memory.getMax());
        final List<Connector> connector = status.getConnector();
        assertNotNull(connector);
        assertEquals(2, connector.size());
        Connector conn = connector.get(0);
        assertEquals(200, conn.getThreadInfo().getMaxThreads());
    }
}
