/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XmlAssertionTest extends JMeterTestCase {

    private XMLAssertion assertion;
    private SampleResult sampleResult;
    private AssertionResult result;
    private static final String INVALID_XML = "<note><to>Tove</to><from>Jani</from><heading>Reminder</heading><body>"
            + "Don't forget me this weekend!</body></note1>";
    private static final String VALID_XML = "<note><to>Tove</to><from>Jani</from><heading>Reminder</heading><body>Don't forget me this weekend!</body></note>";
    private static final String NO_XML = "response Data";
    private static final String UNSECURE_XML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + "<!DOCTYPE foo [\n"
            + "   <!ENTITY xxe SYSTEM \"file:///etc/passwd\" > ]>\n" + "<foo>&xxe;</foo>";

    @BeforeEach
    public void setUp() {
        JMeterContext jmctx = JMeterContextService.getContext();
        assertion = new XMLAssertion();
        assertion.setThreadContext(jmctx);
        JMeterVariables vars = new JMeterVariables();
        jmctx.setVariables(vars);
        sampleResult = new SampleResult();
    }

    @Test
    public void testUnsecureX() throws Exception {
        sampleResult.setResponseData(UNSECURE_XML, null);
        result = assertion.getResult(sampleResult);
        assertTrue(result.isFailure());
        assertTrue(result.isError());
        assertEquals("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.",
                    result.getFailureMessage());
    }

    @Test
    public void testValidXML() throws Exception {
        sampleResult.setResponseData(VALID_XML, null);
        result = assertion.getResult(sampleResult);
        assertFalse(result.isFailure());
        assertFalse(result.isError());
        assertNull(result.getFailureMessage());
    }

    @Test
    public void testInvalidXML() throws Exception {
        sampleResult.setResponseData(INVALID_XML, null);
        result = assertion.getResult(sampleResult);
        assertTrue(result.isFailure());
        assertTrue(result.isError());
        assertNotNull(result.getFailureMessage());
    }

    @Test
    public void testNoXML() throws Exception {
        sampleResult.setResponseData(NO_XML, null);
        result = assertion.getResult(sampleResult);
        assertTrue(result.isFailure());
        assertTrue(result.isError());
        assertNotNull(result.getFailureMessage());
        assertTrue(result.getFailureMessage().contains("Content is not allowed in prolog"));
    }
}
