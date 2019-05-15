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

package org.apache.jmeter.functions;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.test.JMeterSerialTest;
import org.junit.Before;
import org.junit.Test;

public class TestSimpleFunctions extends JMeterTestCase implements JMeterSerialTest {
    private SampleResult result;

    private Collection<CompoundVariable> params;
    private JMeterVariables vars;
    private JMeterContext jmctx;

    @Before
    public void setUp() {
        result = new SampleResult();
        jmctx = JMeterContextService.getContext();
        String data = "The quick brown fox";
        result.setResponseData(data, null);
        vars = new JMeterVariables();
        jmctx.setVariables(vars);
        jmctx.setPreviousResult(result);
        params = new LinkedList<>();
        Thread.currentThread().setName("ThreadGroup-1");
    }

    @Test
    public void testUUIDParameterCount() throws Exception {
        AbstractFunction function = new Uuid();
        checkInvalidParameterCounts(function, 0, 0);
    }

    @Test
    public void testThreadNumberParameterCount() throws Exception {
        AbstractFunction function = new ThreadNumber();
        checkInvalidParameterCounts(function, 0, 0);
    }

    @Test
    public void testEscapeHtmlParameterCount() throws Exception {
        AbstractFunction function = new EscapeHtml();
        checkInvalidParameterCounts(function, 1, 1);
    }

    @Test
    public void testUnEscapeHtmlParameterCount() throws Exception {
        AbstractFunction function = new UnEscapeHtml();
        checkInvalidParameterCounts(function, 1, 1);
    }

    @Test
    public void testEscapeXmlParameterCount() throws Exception {
        AbstractFunction function = new EscapeXml();
        checkInvalidParameterCounts(function, 1, 1);
    }

    @Test
    public void testUnEscapeParameterCount() throws Exception {
        AbstractFunction function = new UnEscape();
        checkInvalidParameterCounts(function, 1, 1);
    }

    @Test
    public void testTestPlanParameterCount() throws Exception {
        AbstractFunction function = new TestPlanName();
        checkInvalidParameterCounts(function, 0, 0);
    }

    @Test
    public void testThreadNumber() throws Exception {
        AbstractFunction function = new ThreadNumber();
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("1", ret);
    }


    @Test
    public void testUuid() throws Exception {
        AbstractFunction function = new Uuid();
        function.setParameters(params);
        String ret = function.execute(result, null);
        UUID.fromString(ret);
    }

    @Test
    public void testEscapeHtml() throws Exception {
        AbstractFunction function = new EscapeHtml();
        params.add(new CompoundVariable("\"bread\" & \"butter\""));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("&quot;bread&quot; &amp; &quot;butter&quot;", ret);
    }

    @Test
    public void testUnEscapeHtml() throws Exception {
        AbstractFunction function = new UnEscapeHtml();
        params.add(new CompoundVariable("&quot;bread&quot; &amp; &quot;butter&quot;"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("\"bread\" & \"butter\"", ret);
    }

    @Test
    public void testUnEscapeHtml2() throws Exception {
        AbstractFunction function = new UnEscapeHtml();
        params.add(new CompoundVariable("&lt;Fran&ccedil;ais&gt;"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("<Français>", ret);
    }

    @Test
    public void testUnEscapeHtml3() throws Exception {
        AbstractFunction function = new UnEscapeHtml();
        params.add(new CompoundVariable("&gt;&zzzz;x"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals(">&zzzz;x", ret);
    }

    @Test
    public void testEscapeXml() throws Exception {
        AbstractFunction function = new EscapeXml();
        params.add(new CompoundVariable("\"bread\" & <'butter'>"));
        function.setParameters(params);
        String ret = function.execute(result, null);
        assertEquals("&quot;bread&quot; &amp; &lt;&apos;butter&apos;&gt;", ret);
    }

    @Test
    public void testTestPlanName() throws Exception {
        AbstractFunction function = new TestPlanName();
        try {
            FileServer.getFileServer().setScriptName("Test");
            function.setParameters(params);
            String ret = function.execute(result, null);
            assertEquals("Test", ret);
        } finally {
            FileServer.getFileServer().setScriptName(null);
        }
    }

    @Test
    public void testThreadGroupName() throws Exception {
        AbstractFunctionByKey function = new ThreadGroupName();
        try {
            HTTPSamplerProxy httpRequest = new HTTPSamplerProxy();
            ThreadGroup threadGroup = new ThreadGroup();
            threadGroup.setName("ThreadGroup-1");
            JMeterContext context = JMeterContextService.getContext();
            context.setCurrentSampler(httpRequest);
            context.setThreadGroup(threadGroup);
            String ret = function.execute(result, httpRequest);
            assertEquals("ThreadGroup-1", ret);
        } finally {
            FileServer.getFileServer().setScriptName(null);
        }
    }
    
    @Test
    public void testThreadGroupNameBug63241() throws Exception {
        AbstractFunctionByKey function = new ThreadGroupName();
        try {
            HTTPSamplerProxy httpRequest = new HTTPSamplerProxy();
            JMeterContext context = JMeterContextService.getContext();
            // This is the state when called from a non test thread
            context.setThreadGroup(null);
            context.setCurrentSampler(httpRequest);
            String ret = function.execute(result, httpRequest);
            assertEquals("", ret);
        } finally {
            FileServer.getFileServer().setScriptName(null);
        }
    }

    @Test
    public void testThreadGroupNameParameterCount() throws Exception {
        AbstractFunctionByKey function = new ThreadGroupName();
        checkInvalidParameterCounts(function, 0, 0);
    }
}
