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

package org.apache.jmeter.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;

public class TestCreateCssSelectorExtractor {

    @SuppressWarnings("deprecation")
    @Test
    public void testCreateCssSelectorExtractor() throws UnsupportedEncodingException {

        Map<String, String> bodyParameterMap = new HashMap<>();
        bodyParameterMap.put("_csrf", "7d1de481-34af-4342-a9b4-b8288c451f7c");

        List<String> arguments = new ArrayList<>();
        arguments.add("_csrf");

        SampleResult sampleResult = new SampleResult();
        sampleResult.setResponseData("<html><head><title>Login Page</title></head><body onload='document.f.username.focus();'>"
                + "<h3>Login with Username and Password</h3><form name='f' action='/login' method='POST'><table> "
                + "<tr><td>User:</td><td><input type='text' name='username' value=''></td></tr>"
                + "<tr><td>Password:</td><td><input type='password' name='password'/></td></tr>"
                + "<tr><td colspan='2'><input name=\"submit\" type=\"submit\" value=\"Login\"/></td></tr>"
                        + "<input name=\"_csrf\" type=\"hidden\" value=\"7d1de481-34af-4342-a9b4-b8288c451f7c\" />"
                                + "</table></form></body></html>");
        sampleResult.setSampleLabel("2 /login");
        sampleResult.setContentType("text/html;charset=UTF-8");

        Map<String, String> map = CreateCssSelectorExtractor.createCssSelectorExtractor(
        sampleResult.getResponseDataAsString(), "7d1de481-34af-4342-a9b4-b8288c451f7c",
                "_csrf", sampleResult.getSampleLabel(), sampleResult.getContentType());

        assertNotNull(map);

    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCreateCssSelectorExtractorElse() throws UnsupportedEncodingException {

        Map<String, String> bodyParameterMap = new HashMap<>();
        bodyParameterMap.put("_csrf", "7d1de481-34af-4342-a9b4-b8288c451f7c");

        List<String> arguments = new ArrayList<>();
        arguments.add("_csrf");

        SampleResult sampleResult = new SampleResult();
        sampleResult.setResponseData("<html><head><title>Login Page</title></head><body onload='document.f.username.focus();'>"
                + "<h3>Login with Username and Password</h3><form name='f' action='/login' method='POST'><table> "
                + "<tr><td>User:</td><td><input type='text' name='username' value=''></td></tr>"
                + "<tr><td>Password:</td><td><input type='password' name='password'/></td></tr>"
                + "<tr><td colspan='2'><input name=\"submit\" type=\"submit\" value=\"Login\"/></td></tr>"
                + "<input name=\"_csrf\" type=\"hidden\" content=\"7d1de481-34af-4342-a9b4-b8288c451f7c\" />"
                + "</table></form></body></html>");

        sampleResult.setSampleLabel("2 /login");
        sampleResult.setContentType("text/html;charset=UTF-8");

        Map<String,String> map = CreateCssSelectorExtractor.createCssSelectorExtractor(
        sampleResult.getResponseDataAsString(), "7d1de481-34af-4342-a9b4-b8288c451f7c",
                "_csrf", sampleResult.getSampleLabel(), sampleResult.getContentType());

        assertNotNull(map);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCreateCssSelectorExtractorNestedElse() throws UnsupportedEncodingException {

        Map<String, String> bodyParameterMap = new HashMap<>();
        bodyParameterMap.put("_csrf", "7d1de481-34af-4342-a9b4-b8288c451f7c");

        List<String> arguments = new ArrayList<>();
        arguments.add("_csrf");

        SampleResult sampleResult = new SampleResult();
        sampleResult.setResponseData("<html><head><title>Login Page</title></head><body onload='document.f.username.focus();'>"
                + "<h3>Login with Username and Password</h3><form name='f' action='/login' method='POST'><table> "
                + "<tr><td>User:</td><td><input type='text' name='username' value=''></td></tr>"
                + "<tr><td>Password:</td><td><input type='password' name='password'/></td></tr>"
                + "<tr><td colspan='2'><input name=\"submit\" type=\"submit\" value=\"Login\"/></td></tr>"
                + "<input name=\"_csrf\" type=\"hidden\" />"
                + "</table></form></body></html>");

        sampleResult.setSampleLabel("2 /login");
        sampleResult.setContentType("text/html;charset=UTF-8");

        Map<String,String> map = CreateCssSelectorExtractor.createCssSelectorExtractor(
        sampleResult.getResponseDataAsString(), "7d1de481-34af-4342-a9b4-b8288c451f7c",
                "_csrf", sampleResult.getSampleLabel(), sampleResult.getContentType());

        assertEquals(0, map.size());

    }

    @Test
    public void testToAttributeSelector() {
        assertEquals("[id=javax.faces.ViewState]", CreateCssSelectorExtractor.toAttributeSelector("#javax.faces.ViewState"));
    }

}
