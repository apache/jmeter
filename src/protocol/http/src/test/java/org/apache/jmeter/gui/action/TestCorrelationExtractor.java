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

package org.apache.jmeter.gui.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.protocol.http.gui.action.CorrelationExtractor;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestCorrelationExtractor {

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";
    private static final String TEXT_HTML = "text/html";

    private static final String OTHER = "other";

    private static final String HTML_CONTENT_TYPE = "text/html;charset=UTF-8";
    private static final String XML_CONTENT_TYPE = "application/xml;charset=UTF-8";
    private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final String RESPONSE_HEADER = "HTTP/1.1 200 OK";

    private static final String BOUNDARY = "boundary";
    private static final String HEADER = "header";
    private static final String SAMPLE_LABEL = "2 /login";

    Map<String, String> parameterMap;
    List<Map<String, String>> listOfMap;
    SampleResult sampleResult;
    String argument;

    @BeforeEach
    public void setup() {
        parameterMap = new HashMap<>();
        sampleResult = new SampleResult();
        listOfMap = new ArrayList<>();
        sampleResult.setSampleLabel(SAMPLE_LABEL);
        sampleResult.setResponseHeaders(RESPONSE_HEADER);
        // create argument and parameterMap
        argument = "_csrf";
        parameterMap.put("_csrf", "7d1de481-34af-4342-a9b4-b8288c451f7c");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCreateExtractorHtmlExtractor() {
        CorrelationExtractor.getListOfMap().clear();
        // Case 1: Parameter in value attribute
        sampleResult.setResponseData("<html><body><form>"
                + "<input name=\"_csrf\" type=\"hidden\" value=\"7d1de481-34af-4342-a9b4-b8288c451f7c\" />"
                + "</form></body></html>");
        sampleResult.setContentType(HTML_CONTENT_TYPE);
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("HtmlExtractor.refname", "_csrf");
                put("HtmlExtractor.expr", "html > body > form > input");
                put("HtmlExtractor.attribute", "value");
                put("HtmlExtractor.match_number", "1");
                put("testname", "2 /login");
                put("contentType", HTML_CONTENT_TYPE);
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, TEXT_HTML);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 2: Parameter in content attribute
        sampleResult.setResponseData("<html><head>"
                + "<meta name=\"_csrf\" content=\"7d1de481-34af-4342-a9b4-b8288c451f7c\" />"
                + "</head></html>");
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("HtmlExtractor.refname", "_csrf");
                put("HtmlExtractor.expr", "html > head > meta");
                put("HtmlExtractor.attribute", "content");
                put("HtmlExtractor.match_number", "1");
                put("testname", "2 /login");
                put("contentType", HTML_CONTENT_TYPE);
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, TEXT_HTML);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 3: css selector is id selector with id containing (.) or (:)
        sampleResult.setResponseData("<html><body><form>"
                + "<input name=\"_csrf\" id=\"csrf.token\" value=\"7d1de481-34af-4342-a9b4-b8288c451f7c\" />"
                + "</form></body></html>");
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("HtmlExtractor.refname", "_csrf");
                put("HtmlExtractor.expr", "[id=csrf.token]");
                put("HtmlExtractor.attribute", "value");
                put("HtmlExtractor.match_number", "1");
                put("testname", "2 /login");
                put("contentType", HTML_CONTENT_TYPE);
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, TEXT_HTML);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 4: css selector is id selector
        sampleResult.setResponseData("<html><body><form>"
                + "<input name=\"_csrf\" id=\"csrfToken\" value=\"7d1de481-34af-4342-a9b4-b8288c451f7c\" />"
                + "</form></body></html>");
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("HtmlExtractor.refname", "_csrf");
                put("HtmlExtractor.expr", "#csrfToken");
                put("HtmlExtractor.attribute", "value");
                put("HtmlExtractor.match_number", "1");
                put("testname", "2 /login");
                put("contentType", HTML_CONTENT_TYPE);
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, TEXT_HTML);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 5: no parameter in response data
        // This case shouldn't occur but it is still tested
        sampleResult.setResponseData("<html><head><title>Response</title></head></html>");
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, TEXT_HTML);
        // No change in result
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 6: invalid/null response data
        sampleResult.setResponseData("");
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, TEXT_HTML);
        // No change in result
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCreateExtractorXPath2Extractor() {
        CorrelationExtractor.getListOfMap().clear();
        // Case 1: Parameter in XML text
        sampleResult.setResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<token><_csrf>7d1de481-34af-4342-a9b4-b8288c451f7c</_csrf></token>");
        sampleResult.setContentType(XML_CONTENT_TYPE);
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("XPathExtractor2.refname", "_csrf");
                put("XPathExtractor2.xpathQuery", "/token[1]/_csrf[1]/text()");
                put("XPathExtractor2.matchNumber", "1");
                put("testname", "2 /login");
                put("contentType", XML_CONTENT_TYPE);
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, APPLICATION_XML);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 2: Parameter in XML tag's attribute
        sampleResult.setResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<token value=\"7d1de481-34af-4342-a9b4-b8288c451f7c\">_csrf</token>");
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("XPathExtractor2.refname", "_csrf");
                put("XPathExtractor2.xpathQuery", "/token[1]/@value");
                put("XPathExtractor2.matchNumber", "1");
                put("testname", "2 /login");
                put("contentType", XML_CONTENT_TYPE);
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, APPLICATION_XML);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 3: Parameter not present in response
        // This case shouldn't occur but it is still tested
        sampleResult.setResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<token>Response</token>");
        // No change in result
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, APPLICATION_XML);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 4: Invalid response
        sampleResult.setResponseData("{ \"_csrf\": \"7d1de481-34af-4342-a9b4-b8288c451f7c\" }");
        // No change in result and throws TransformerException
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, APPLICATION_XML);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCreateExtractorJsonExtractor() {
        CorrelationExtractor.getListOfMap().clear();
        // Case 1: Parameter in JSON text
        sampleResult.setResponseData("{ \"_csrf\": \"7d1de481-34af-4342-a9b4-b8288c451f7c\" }");
        sampleResult.setContentType(JSON_CONTENT_TYPE);
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("JSONPostProcessor.referenceNames", "_csrf");
                put("JSONPostProcessor.jsonPathExprs", "$['_csrf']");
                put("JSONPostProcessor.match_numbers", "1");
                put("testname", "2 /login");
                put("contentType", JSON_CONTENT_TYPE);
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, APPLICATION_JSON);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 2: Parameter not present in json
        // This case shouldn't occur but it is still tested
        sampleResult.setResponseData("{ \"_csrf\": \"7d1de481-34af-4342\" }");
        // No change in result, throws PathNotFoundException
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, APPLICATION_JSON);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 3: Invalid response
        sampleResult.setResponseData("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<token value=\"7d1de481-34af-4342-a9b4-b8288c451f7c\">_csrf</token>");
        // No change in result
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, APPLICATION_JSON);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCreateExtractorRegexExtractorForBody() {
        CorrelationExtractor.getListOfMap().clear();
        // Case 1: Parameter in response
        sampleResult.setResponseData(
                "<input name=\"_csrf\" type=\"hidden\" value=\"7d1de481-34af-4342-a9b4-b8288c451f7c\" />\r\n"
                + "<test data>");
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("RegexExtractor.refname", "_csrf");
                put("RegexExtractor.expr", "_csrf\" type=\"hidden\" value=\"(.*?)\" />");
                put("RegexExtractor.match_number", "1");
                put("testname", "2 /login");
                put("RegexExtractor.template", "$1$");
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, OTHER);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 2: Parameter in response with two occurrences of the parameter name
        sampleResult.setResponseData(
                "<input name=\"_csrf\" id=\"_csrf\" value=\"7d1de481-34af-4342-a9b4-b8288c451f7c\" />");
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("RegexExtractor.refname", "_csrf");
                put("RegexExtractor.expr", "_csrf\" value=\"(.*?)\" />");
                put("RegexExtractor.match_number", "1");
                put("testname", "2 /login");
                put("RegexExtractor.template", "$1$");
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, OTHER);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 3: Parameter not in response
        sampleResult.setResponseData("response data");
        // No change in result
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, OTHER);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
    }

    @Test
    public void testCreateExtractorRegexExtractorForHeader() {
        CorrelationExtractor.getListOfMap().clear();
        // Case 1: Parameter in Header as single value
        sampleResult.setResponseHeaders("HTTP/1.1 200 OK\r\n" +
                "Authorization: Bearer hfkjdsafbdzjhkdkjsv\r\n" +
                "Content-Language: en-US");
        parameterMap.put("access_token", "Bearer hfkjdsafbdzjhkdkjsv");
        argument = "access_token";
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("RegexExtractor.refname", "access_token");
                put("RegexExtractor.expr", "Authorization: (.*?)$");
                put("RegexExtractor.match_number", "1");
                put("RegexExtractor.useHeaders", "true");
                put("testname", "2 /login");
                put("RegexExtractor.template", "$1$");
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, HEADER);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 2: Parameter in Header in Cookie
        sampleResult.setResponseHeaders("HTTP/1.1 200 OK\r\n" +
                "Set-Cookie: X-CSRF-TOKEN=aghvvcga27cac7cacdccdv32; STATE=/;\r\n" +
                "Content-Language: en-US");
        parameterMap.put("_csrf(1)", "aghvvcga27cac7cacdccdv32");
        argument = "_csrf(1)";
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("RegexExtractor.refname", "_csrf(1)");
                put("RegexExtractor.expr", "Set-Cookie: X-CSRF-TOKEN=(.*?);");
                put("RegexExtractor.match_number", "1");
                put("RegexExtractor.useHeaders", "true");
                put("testname", "2 /login");
                put("RegexExtractor.template", "$1$");
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, HEADER);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
        // Case 3: Parameter not in Header
        // This case shouldn't occur but it is still tested
        sampleResult.setResponseHeaders("response data");
        // No change in result
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, OTHER);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCreateBoundaryExtractor() {
        CorrelationExtractor.getListOfMap().clear();
        sampleResult.setResponseData("[1,2,3,4,['randomstringtoken',9],9,8,6]");
        parameterMap.put("txnid", "randomstringtoken");
        argument = "txnid";
        // Result Data
        listOfMap.add(new HashMap<String, String>() {
            private static final long serialVersionUID = 1L;
            {
                put("BoundaryExtractor.refname", "txnid");
                put("BoundaryExtractor.lboundary", "4,['");
                put("BoundaryExtractor.rboundary", "',9]");
                put("BoundaryExtractor.match_number", "1");
                put("testname", "2 /login");
            }
        });
        CorrelationExtractor.createExtractor(sampleResult, argument, parameterMap, BOUNDARY);
        Assertions.assertEquals(listOfMap, CorrelationExtractor.getListOfMap());
    }
}
