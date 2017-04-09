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

/*
 * Created on Jul 25, 2003
 */
package org.apache.jmeter.engine.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Before;
import org.junit.Test;

/*
 * To run this test stand-alone, ensure that ApacheJMeter_functions.jar is on the classpath,
 * as it is needed to resolve the functions.
 */
public class PackageTest extends JMeterTestCase {
    private ReplaceStringWithFunctions transformer;


    private JMeterContext jmctx = null;

    @Before
    public void setUp() {
        jmctx = JMeterContextService.getContext();
        Map<String, String> variables = new HashMap<>();
        variables.put("my_regex", ".*");
        variables.put("server", "jakarta.apache.org");
        SampleResult result = new SampleResult();
        result.setResponseData("<html>hello world</html> costs: $3.47,$5.67", null);
        transformer = new ReplaceStringWithFunctions(new CompoundVariable(), variables);
        jmctx.setVariables(new JMeterVariables());
        jmctx.setSamplingStarted(true);
        jmctx.setPreviousResult(result);
        jmctx.getVariables().put("server", "jakarta.apache.org");
        jmctx.getVariables().put("my_regex", ".*");
    }

    @Test
    public void testFunctionParse1() throws Exception {
        StringProperty prop = new StringProperty("date", "${__javaScript((new Date().getDate() / 100).toString()."
                + "substr(${__javaScript(1+1,d\\,ay)}\\,2),heute)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        newProp.recoverRunningVersion(null);
        assertTrue(Integer.parseInt(newProp.getStringValue()) > -1);
        assertEquals("2", jmctx.getVariables().getObject("d,ay"));
    }

    @Test
    public void testParseExample1() throws Exception {
        StringProperty prop = new StringProperty("html", "${__regexFunction(<html>(.*)</html>,$1$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("hello world", newProp.getStringValue());
    }

    @Test
    public void testParseExample2() throws Exception {
        StringProperty prop = new StringProperty("html", "It should say:\\${${__regexFunction(<html>(.*)</html>,$1$)}}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("It should say:${hello world}", newProp.getStringValue());
    }

    @Test
    public void testParseExample3() throws Exception {
        StringProperty prop = new StringProperty("html", "${__regexFunction(<html>(.*)</html>,$1$)}"
                + "${__regexFunction(<html>(.*o)(.*o)(.*)</html>," + "$1$$3$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("hello worldhellorld", newProp.getStringValue());
    }

    @Test
    public void testParseExample4() throws Exception {
        StringProperty prop = new StringProperty("html", "${non-existing function}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("${non-existing function}", newProp.getStringValue());
    }

    @Test
    public void testParseExample6() throws Exception {
        StringProperty prop = new StringProperty("html", "${server}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("jakarta.apache.org", newProp.getStringValue());
    }

    @Test
    public void testParseExample5() throws Exception {
        StringProperty prop = new StringProperty("html", "");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.StringProperty", newProp.getClass().getName());
        assertEquals("", newProp.getStringValue());
    }

    @Test
    public void testParseExample7() throws Exception {
        StringProperty prop = new StringProperty("html", "${__regexFunction(\\<([a-z]*)\\>,$1$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("html", newProp.getStringValue());
    }

    @Test
    public void testParseExample8() throws Exception {
        StringProperty prop = new StringProperty("html", "${__regexFunction((\\\\$\\d+\\.\\d+),$1$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("$3.47", newProp.getStringValue());
    }

    @Test
    public void testParseExample9() throws Exception {
        StringProperty prop = new StringProperty("html", "${__regexFunction(([$]\\d+\\.\\d+),$1$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("$3.47", newProp.getStringValue());
    }

    @Test
    public void testParseExample10() throws Exception {
        StringProperty prop = new StringProperty("html", "${__regexFunction(\\ "
                + "(\\\\\\$\\d+\\.\\d+\\,\\\\$\\d+\\.\\d+),$1$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("$3.47,$5.67", newProp.getStringValue());
    }

    // Escaped dollar commma and backslash with no variable reference
    @Test
    public void testParseExample11() throws Exception {
        StringProperty prop = new StringProperty("html", "\\$a \\, \\\\ \\x \\ jakarta.apache.org");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.StringProperty", newProp.getClass().getName());
        assertEquals("\\$a \\, \\\\ \\x \\ jakarta.apache.org", newProp.getStringValue());
    }

    // N.B. See Bug 46831 which wanted to changed the behaviour of \$
    // It's too late now, as this would invalidate some existing test plans,
    // so document the current behaviour with some more tests.
    
    // Escaped dollar commma and backslash with variable reference
    @Test
    public void testParseExample12() throws Exception {
        StringProperty prop = new StringProperty("html", "\\$a \\, \\\\ \\x \\ ${server} \\$b \\, \\\\ cd");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        // N.B. Backslashes are removed before dollar, comma and backslash
        assertEquals("$a , \\ \\x \\ jakarta.apache.org $b , \\ cd", newProp.getStringValue());
    }

    // Escaped dollar commma and backslash with missing variable reference
    @Test
    public void testParseExample13() throws Exception {
        StringProperty prop = new StringProperty("html", "\\$a \\, \\\\ \\x \\ ${missing} \\$b \\, \\\\ cd");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        // N.B. Backslashes are removed before dollar, comma and backslash
        assertEquals("$a , \\ \\x \\ ${missing} $b , \\ cd", newProp.getStringValue());
    }

    // Escaped dollar commma and backslash with missing function reference
    @Test
    public void testParseExample14() throws Exception {
        StringProperty prop = new StringProperty("html", "\\$a \\, \\\\ \\x \\ ${__missing(a)} \\$b \\, \\\\ cd");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        // N.B. Backslashes are removed before dollar, comma and backslash
        assertEquals("$a , \\ \\x \\ ${__missing(a)} $b , \\ cd", newProp.getStringValue());
    }

    @Test
    public void testNestedExample1() throws Exception {
        StringProperty prop = new StringProperty("html", "${__regexFunction(<html>(${my_regex})</html>,"
                + "$1$)}${__regexFunction(<html>(.*o)(.*o)(.*)" + "</html>,$1$$3$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("hello worldhellorld", newProp.getStringValue());
    }

    @Test
    public void testNestedExample2() throws Exception {
        StringProperty prop = new StringProperty("html", "${__regexFunction(<html>(${my_regex})</html>,$1$)}");
        JMeterProperty newProp = transformer.transformValue(prop);
        newProp.setRunningVersion(true);
        assertEquals("org.apache.jmeter.testelement.property.FunctionProperty", newProp.getClass().getName());
        assertEquals("hello world", newProp.getStringValue());
    }

}
