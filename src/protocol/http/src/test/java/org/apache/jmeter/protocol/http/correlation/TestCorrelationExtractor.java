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

package org.apache.jmeter.protocol.http.correlation;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.protocol.http.correlation.extractordata.BoundaryExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.HtmlExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.JsonPathExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.RegexExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.XPath2ExtractorData;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestCorrelationExtractor {

    CreateExtractorInterface createJsonExtractorInterface = null;
    CreateExtractorInterface createBoundryExtractorInterface = null;
    CreateExtractorInterface createRexgexExtractorInterface = null;
    CreateExtractorInterface createXpathExtractorInterface = null;
    CreateExtractorInterface createCssExtractorInterface = null;

    ExtractorCreatorData extractorCreatorData = null;

    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_XML = "application/xml";
    private static final String TEXT_HTML = "text/html";

    private static final String OTHER = "other";

    private static final String HTML_CONTENT_TYPE = "text/html;charset=UTF8";
    private static final String XML_CONTENT_TYPE = "application/xml;charset=UTF8";
    private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF8";
    private static final String RESPONSE_HEADER = "HTTP/1.1 200 OK";

    private static final String BOUNDARY = "boundary";
    private static final String HEADER = "header";
    private static final String SAMPLE_LABEL = "2 /login";

    Map<String, String> parameterMap;
    SampleResult sampleResult;
    String argument;

    @BeforeEach
    public void setup() {
        parameterMap = new HashMap<>();
        sampleResult = new SampleResult();
        sampleResult.setSampleLabel(SAMPLE_LABEL);
        sampleResult.setResponseHeaders(RESPONSE_HEADER);
        // create argument and parameterMap
        argument = "_csrf";
        parameterMap.put("_csrf", "7d1de48134af4342a9b4b8288c451f7c");

    }

    @Test
    public void testCreateExtractorHtmlExtractor() {

        createCssExtractorInterface = new CreateCssSelectorExtractor();
        // Case 1: Parameter in value attribute
        sampleResult.setResponseData("<html><body><form>"
                + "<input name=\"_csrf\" type=\"hidden\" value=\"7d1de48134af4342a9b4b8288c451f7c\" />"
                + "</form></body></html>", StandardCharsets.UTF_8.name());
        sampleResult.setContentType(HTML_CONTENT_TYPE);
        extractorCreatorData = new ExtractorCreatorData();
        extractorCreatorData.setContentType(TEXT_HTML);
        extractorCreatorData.setParameter("html > body > form > input");
        extractorCreatorData.setParameterValue(parameterMap.get(argument));
        extractorCreatorData.setSampleResult(sampleResult);
        Assertions.assertEquals(new HtmlExtractorData("_csrf", "html > body > form > input", "value", "1",
                HTML_CONTENT_TYPE, "2 /login"), CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 2: Parameter in content attribute
        sampleResult.setResponseData("<html><head>"
                + "<meta name=\"_csrf\" content=\"7d1de48134af4342a9b4b8288c451f7c\" />" + "</head></html>",
                StandardCharsets.UTF_8.name());
        // Creating ExtractorCreatorData for HtmlExtractor
        extractorCreatorData = new ExtractorCreatorData();
        extractorCreatorData.setContentType(TEXT_HTML);
        extractorCreatorData.setParameter("_csrf");
        extractorCreatorData.setParameterValue(parameterMap.get(argument));
        extractorCreatorData.setSampleResult(sampleResult);
        Assertions.assertEquals(
                new HtmlExtractorData("_csrf", "html > head > meta", "content", "1", HTML_CONTENT_TYPE, "2 /login"),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 3: css selector is id selector with id containing (.) or (:)
        sampleResult.setResponseData("<html><body><form>"
                + "<input name=\"_csrf\" id=\"csrf.token\" value=\"7d1de48134af4342a9b4b8288c451f7c\" />"
                + "</form></body></html>", StandardCharsets.UTF_8.name());
        Assertions.assertEquals(
                new HtmlExtractorData("_csrf", "[id=csrf.token]", "value", "1", HTML_CONTENT_TYPE, "2 /login"),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 4: css selector is id selector
        sampleResult.setResponseData("<html><body><form>"
                + "<input name=\"_csrf\" id=\"csrfToken\" value=\"7d1de48134af4342a9b4b8288c451f7c\" />"
                + "</form></body></html>", StandardCharsets.UTF_8.name());
        Assertions.assertEquals(
                new HtmlExtractorData("_csrf", "#csrfToken", "value", "1", HTML_CONTENT_TYPE, "2 /login"),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 5: no parameter in response data
        // This case shouldn't occur but it is still tested
        sampleResult.setResponseData("<html><head><title>Response</title></head></html>",
                StandardCharsets.UTF_8.name());
        Assertions.assertNull(CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 6: invalid/null response data
        sampleResult.setResponseData("", StandardCharsets.UTF_8.name());
        Assertions.assertNull(CorrelationExtractor.createExtractor(extractorCreatorData));
    }

    @Test
    public void testCreateExtractorXPath2Extractor() {
        // Case 1: Parameter in XML text
        sampleResult.setResponseData(
                "<?xml version=\"1.0\" encoding=\"UTF8\"?>"
                        + "<token><_csrf>7d1de48134af4342a9b4b8288c451f7c</_csrf></token>",
                StandardCharsets.UTF_8.name());
        sampleResult.setContentType(XML_CONTENT_TYPE);
        // Creating ExtractorCreatorData for XPath2Extractor
        extractorCreatorData = new ExtractorCreatorData();
        extractorCreatorData.setContentType(APPLICATION_XML);
        extractorCreatorData.setParameter("_csrf");
        extractorCreatorData.setParameterValue(parameterMap.get(argument));
        extractorCreatorData.setSampleResult(sampleResult);
        Assertions.assertEquals(
                new XPath2ExtractorData("_csrf", "/token[1]/_csrf[1]/text()", "1", XML_CONTENT_TYPE, "2 /login"),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 2: Parameter in XML tag's attribute
        sampleResult.setResponseData(
                "<?xml version=\"1.0\" encoding=\"UTF8\"?>"
                        + "<token value=\"7d1de48134af4342a9b4b8288c451f7c\">_csrf</token>",
                StandardCharsets.UTF_8.name());
        Assertions.assertEquals(new XPath2ExtractorData("_csrf", "/token[1]/@value", "1", XML_CONTENT_TYPE, "2 /login"),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 3: Parameter not present in response
        // This case shouldn't occur but it is still tested
        sampleResult.setResponseData("<?xml version=\"1.0\" encoding=\"UTF8\"?>" + "<token>Response</token>",
                StandardCharsets.UTF_8.name());
        Assertions.assertNull(CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 4: Invalid response
        sampleResult.setResponseData("{ \"_csrf\": \"7d1de48134af4342a9b4b8288c451f7c\" }",
                StandardCharsets.UTF_8.name());
        Assertions.assertNull(CorrelationExtractor.createExtractor(extractorCreatorData));
    }

    @Test
    public void testCreateExtractorJsonExtractor() {
        // Case 1: Parameter in JSON text
        sampleResult.setResponseData("{ \"_csrf\": \"7d1de48134af4342a9b4b8288c451f7c\" }",
                StandardCharsets.UTF_8.name());
        sampleResult.setContentType(JSON_CONTENT_TYPE);
        // Creating ExtractorCreatorData for JsonExtractor
        extractorCreatorData = new ExtractorCreatorData();
        extractorCreatorData.setContentType(APPLICATION_JSON);
        extractorCreatorData.setParameter("_csrf");
        extractorCreatorData.setParameterValue(parameterMap.get(argument));
        extractorCreatorData.setSampleResult(sampleResult);

        Assertions.assertEquals(new JsonPathExtractorData("_csrf", "$['_csrf']", "1", JSON_CONTENT_TYPE, "2 /login"),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 2: Parameter not present in JSON
        // This case shouldn't occur but it is still tested
        sampleResult.setResponseData("{ \"_csrf\": \"7d1de48134af4342\" }", StandardCharsets.UTF_8.name());
        Assertions.assertNull(CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 3: Invalid response
        sampleResult.setResponseData(
                "<?xml version=\"1.0\" encoding=\"UTF8\"?>"
                        + "<token value=\"7d1de48134af4342a9b4b8288c451f7c\">_csrf</token>",
                StandardCharsets.UTF_8.name());
        Assertions.assertNull(CorrelationExtractor.createExtractor(extractorCreatorData));
    }

    @Test
    public void testCreateExtractorRegexExtractorForBody() {
        CorrelationExtractor.getListOfExtractor().clear();

        // Case 1: Parameter in response
        sampleResult.setResponseData(
                "<input name=\"_csrf\" type=\"hidden\" value=\"7d1de48134af4342a9b4b8288c451f7c\" />\r\n"
                        + "<test data>",
                StandardCharsets.UTF_8.name());
        // Creating ExtractorCreatorData for RegexExtractorForBody
        extractorCreatorData = new ExtractorCreatorData();
        extractorCreatorData.setContentType(OTHER);
        extractorCreatorData.setParameter("_csrf");
        extractorCreatorData.setParameterValue(parameterMap.get(argument));
        extractorCreatorData.setSampleResult(sampleResult);
        Assertions.assertEquals(new RegexExtractorData("_csrf\" type=\"hidden\" value=\"(.*?)\" />", "_csrf",
                "2 /login", "1", "$1$", false), CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 2: Parameter in response with two occurrences of the parameter name
        sampleResult.setResponseData("<input name=\"_csrf\" id=\"_csrf\" value=\"7d1de48134af4342a9b4b8288c451f7c\" />",
                StandardCharsets.UTF_8.name());
        Assertions.assertEquals(
                new RegexExtractorData("_csrf\" value=\"(.*?)\" />", "_csrf", "2 /login", "1", "$1$", false),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 3: Parameter not in response
        sampleResult.setResponseData("response data", StandardCharsets.UTF_8.name());
        // No change in result
        ;
        Assertions.assertNull(CorrelationExtractor.createExtractor(extractorCreatorData));
    }

    @Test
    public void testCreateExtractorRegexExtractorForHeader() {
        CorrelationExtractor.getListOfExtractor().clear();
        // Case 1: Parameter in Header as single value
        sampleResult.setResponseHeaders(
                "HTTP/1.1 200 OK\r\n" + "Authorization: Bearer hfkjdsafbdzjhkdkjsv\r\n" + "ContentLanguage: enUS");
        parameterMap.put("access_token", "hfkjdsafbdzjhkdkjsv");
        argument = "access_token";
        // Creating ExtractorCreatorData for RegexExtractorForHeader
        extractorCreatorData = new ExtractorCreatorData();
        extractorCreatorData.setContentType(HEADER);
        extractorCreatorData.setParameter(argument);
        extractorCreatorData.setParameterValue(parameterMap.get(argument));
        extractorCreatorData.setSampleResult(sampleResult);
        Assertions.assertEquals(
                new RegexExtractorData("Authorization: Bearer (.*?)$", "access_token", "2 /login", "1", "$1$", true),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 2: Parameter in Header in Cookie
        sampleResult.setResponseHeaders("HTTP/1.1 200 OK\r\n"
                + "SetCookie: XCSRFTOKEN=aghvvcga27cac7cacdccdv32; STATE=/;\r\n" + "ContentLanguage: enUS");
        parameterMap.put("_csrf(1)", "aghvvcga27cac7cacdccdv32");
        argument = "_csrf(1)";
        extractorCreatorData.setParameter(argument);
        extractorCreatorData.setParameterValue(parameterMap.get(argument));
        Assertions.assertEquals(
                new RegexExtractorData("SetCookie: XCSRFTOKEN=(.*?);", "_csrf(1)", "2 /login", "1", "$1$", true),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // Case 3: Parameter not in Header
        // This case shouldn't occur but it is still tested
        sampleResult.setResponseHeaders("response data");
        Assertions.assertNull(CorrelationExtractor.createExtractor(extractorCreatorData));
    }

    @Test
    public void testCreateBoundaryExtractor() {
        CorrelationExtractor.getListOfExtractor().clear();
        createBoundryExtractorInterface = new CreateBoundaryExtractor();

        // case 1: value has 4 characters boundary on the left and right
        sampleResult.setResponseData("[1,2,3,4,['randomstringtoken',9],9,8,6]", StandardCharsets.UTF_8.name());
        parameterMap.put("txnid", "randomstringtoken");
        argument = "txnid";
        // Creating ExtractorCreatorData for BoundaryExtractor
        extractorCreatorData = new ExtractorCreatorData();
        extractorCreatorData.setContentType(BOUNDARY);
        extractorCreatorData.setParameter(argument);
        extractorCreatorData.setParameterValue(parameterMap.get(argument));
        extractorCreatorData.setSampleResult(sampleResult);
        Assertions.assertEquals(new BoundaryExtractorData("txnid", "4,['", "',9]", "1", "2 /login"),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // case 2: value has less than 4 character boundary on left and right
        sampleResult.setResponseData("['randomstringtoken',9", StandardCharsets.UTF_8.name());
        Assertions.assertEquals(new BoundaryExtractorData("txnid", "['", "',9", "1", "2 /login"),
                CorrelationExtractor.createExtractor(extractorCreatorData));
        // case 3: value = responseData
        sampleResult.setResponseData("randomstringtoken", StandardCharsets.UTF_8.name());
        Assertions.assertNull(CorrelationExtractor.createExtractor(extractorCreatorData));
    }
}
