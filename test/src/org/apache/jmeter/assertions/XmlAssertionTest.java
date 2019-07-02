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

package org.apache.jmeter.assertions;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class XmlAssertionTest extends JMeterTestCase {

    private XMLAssertion assertion;
    private SampleResult sampleResult;
    private AssertionResult result;
    private final String invalidXml = "<note><to>Tove</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note1>";
    private final String validXml = "<note><to>Tove</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note>";
    private final String noXml = "response Data";

    @Before
    public void setUp() {
        JMeterContext jmctx = JMeterContextService.getContext();
        assertion = new XMLAssertion();
        assertion.setThreadContext(jmctx);
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        sampleResult = new SampleResult();
    }

    @Test
    public void testValidXML() throws Exception {
        sampleResult.setResponseData(validXml, null);
        result = assertion.getResult(sampleResult);
        Assert.assertFalse(result.isFailure());
        Assert.assertFalse(result.isError());
        Assert.assertNull(result.getFailureMessage());
    }

    @Test
    public void testInvalidXML() throws Exception {
        sampleResult.setResponseData(invalidXml, null);
        result = assertion.getResult(sampleResult);
        Assert.assertTrue(result.isFailure());
        Assert.assertTrue(result.isError());
        Assert.assertNotNull(result.getFailureMessage());
    }

    @Test
    public void testNoXML() throws Exception {
        sampleResult.setResponseData(noXml, null);
        result = assertion.getResult(sampleResult);
        Assert.assertTrue(result.isFailure());
        Assert.assertTrue(result.isError());
        Assert.assertNotNull(result.getFailureMessage());
        Assert.assertTrue(result.getFailureMessage().contains("Content is not allowed in prolog"));
    }
}
