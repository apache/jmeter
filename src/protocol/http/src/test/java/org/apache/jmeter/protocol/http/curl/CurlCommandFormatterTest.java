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

package org.apache.jmeter.protocol.http.curl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.junit.jupiter.api.Test;

class CurlCommandFormatterTest {

    private static HTTPSampleResult result(String method, String url) throws MalformedURLException {
        HTTPSampleResult res = new HTTPSampleResult();
        res.setHTTPMethod(method);
        if (url != null) {
            res.setURL(new URL(url));
        }
        return res;
    }

    @Test
    void testSimpleGetOmitsMethodFlag() throws Exception {
        HTTPSampleResult res = result("GET", "http://example.com/path?a=1");

        assertEquals(
                "curl \\\n  'http://example.com/path?a=1'",
                CurlCommandFormatter.format(res));
    }

    @Test
    void testPostWithHeaderAndBody() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/submit");
        res.setRequestHeaders("Content-Type: application/json");
        res.setQueryString("{\"name\":\"value\"}");

        assertEquals(
                "curl -X 'POST' \\\n"
                        + "  'http://example.com/submit' \\\n"
                        + "  -H 'Content-Type: application/json' \\\n"
                        + "  --data-raw '{\"name\":\"value\"}'",
                CurlCommandFormatter.format(res));
    }

    @Test
    void testRepeatedHeadersArePreserved() throws Exception {
        HTTPSampleResult res = result("GET", "http://example.com/");
        res.setRequestHeaders("X-Trace: a\nX-Trace: b\nAccept: text/html\nAccept: application/json");

        assertEquals(
                "curl \\\n"
                        + "  'http://example.com/' \\\n"
                        + "  -H 'X-Trace: a' \\\n"
                        + "  -H 'X-Trace: b' \\\n"
                        + "  -H 'Accept: text/html' \\\n"
                        + "  -H 'Accept: application/json'",
                CurlCommandFormatter.format(res));
    }

    @Test
    void testConnectionAutoAndPseudoHeadersAreSkipped() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/");
        res.setRequestHeaders("Connection: keep-alive\n"
                + "Content-Length: 5\n"
                + "Transfer-Encoding: chunked\n"
                + "X-LocalAddress: /10.0.0.5\n"
                + "Accept: application/json");
        res.setQueryString("hello");

        assertEquals(
                "curl -X 'POST' \\\n"
                        + "  'http://example.com/' \\\n"
                        + "  -H 'Accept: application/json' \\\n"
                        + "  --data-raw 'hello'",
                CurlCommandFormatter.format(res));
    }

    @Test
    void testHeadUsesHeadFlag() throws Exception {
        HTTPSampleResult res = result("HEAD", "http://example.com/");
        res.setRequestHeaders("Accept: */*");

        String curl = CurlCommandFormatter.format(res);
        assertTrue(curl.startsWith("curl --head "), curl);
        assertFalse(curl.contains("-X "), curl);
        assertFalse(curl.contains("--data"), curl);
    }

    @Test
    void testGetWithBodyKeepsMethodSoCurlDoesNotSwitchToPost() throws Exception {
        HTTPSampleResult res = result("GET", "http://example.com/");
        res.setQueryString("q=1");

        String curl = CurlCommandFormatter.format(res);
        assertTrue(curl.contains("-X 'GET'"), curl);
        assertTrue(curl.contains("--data-raw 'q=1'"), curl);
    }

    @Test
    void testMultipartRebuiltAsFormFlagsWithFileName() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/upload");
        res.setRequestHeaders("Content-Type: multipart/form-data; boundary=xyz");
        // Rendered multipart body as JMeter stores it in the result.
        res.setQueryString("--xyz\r\n"
                + "Content-Disposition: form-data; name=\"comment\"\r\n"
                + "\r\n"
                + "hello\r\n"
                + "--xyz\r\n"
                + "Content-Disposition: form-data; name=\"upload\"; filename=\"report.pdf\"\r\n"
                + "Content-Type: application/pdf\r\n"
                + "\r\n"
                + "<actual file content, not shown here>\r\n"
                + "--xyz--\r\n");

        String curl = CurlCommandFormatter.format(res);
        // The file part is rebuilt with the file name as an editable @placeholder.
        assertTrue(curl.contains("-F 'upload=@report.pdf;type=application/pdf'"), curl);
        // Regular fields use --form-string so the value is never treated as a file.
        assertTrue(curl.contains("--form-string 'comment=hello'"), curl);
        // No raw dump of the placeholder, and curl sets its own multipart Content-Type.
        assertFalse(curl.contains("--data-raw"), curl);
        assertFalse(curl.contains("actual file content"), curl);
        assertFalse(curl.contains("-H 'Content-Type: multipart/form-data"), curl);
    }

    @Test
    void testMultipartTextFieldWithAtPrefixUsesFormString() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/upload");
        res.setRequestHeaders("Content-Type: multipart/form-data; boundary=xyz");
        // A legitimate text field whose value starts with '@' must not be read as a file.
        res.setQueryString("--xyz\r\n"
                + "Content-Disposition: form-data; name=\"handle\"\r\n"
                + "\r\n"
                + "@someuser\r\n"
                + "--xyz--\r\n");

        String curl = CurlCommandFormatter.format(res);
        assertTrue(curl.contains("--form-string 'handle=@someuser'"), curl);
        assertFalse(curl.contains("-F 'handle=@someuser'"), curl);
    }

    @Test
    void testFileAsBodyPlaceholderIsNotReproduced() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/upload");
        res.setRequestHeaders("Content-Type: application/octet-stream");
        res.setQueryString("<actual file content, not shown here>");

        String curl = CurlCommandFormatter.format(res);
        assertFalse(curl.contains("--data-raw"), curl);
    }

    @Test
    void testNonRepeatableBodyPlaceholderIsNotReproduced() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/");
        res.setQueryString("<Entity was not repeatable, cannot view what was sent>");

        String curl = CurlCommandFormatter.format(res);
        assertFalse(curl.contains("--data-raw"), curl);
    }

    @Test
    void testWhitespaceOnlyBodyIsKept() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/");
        res.setQueryString(" ");

        assertTrue(CurlCommandFormatter.format(res).contains("--data-raw ' '"));
    }

    @Test
    void testCookiesAddedAsFlag() throws Exception {
        HTTPSampleResult res = result("GET", "http://example.com/");
        res.setCookies("session=abc; theme=dark");

        assertTrue(CurlCommandFormatter.format(res).contains("-b 'session=abc; theme=dark'"));
    }

    @Test
    void testAcceptEncodingAddsCompressed() throws Exception {
        HTTPSampleResult res = result("GET", "http://example.com/");
        res.setRequestHeaders("Accept-Encoding: gzip, deflate");

        String curl = CurlCommandFormatter.format(res);
        assertTrue(curl.contains("--compressed"), curl);
        // --compressed makes curl send its own Accept-Encoding, so the explicit one is dropped.
        assertFalse(curl.contains("-H 'Accept-Encoding"), curl);
    }

    @Test
    void testSingleQuoteIsEscaped() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/");
        res.setQueryString("name=O'Brien");

        // single quote becomes '\'' so the value stays shell-safe
        assertTrue(CurlCommandFormatter.format(res).contains("--data-raw 'name=O'\\''Brien'"));
    }

    @Test
    void testNullUrlDoesNotFail() throws Exception {
        HTTPSampleResult res = result("GET", null);

        assertEquals("curl", CurlCommandFormatter.format(res));
    }

    @Test
    void testRoundTripThroughParser() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/submit");
        res.setRequestHeaders("X-A: 1\nX-B: 2");
        res.setQueryString("payload");

        String curl = CurlCommandFormatter.format(res);
        BasicCurlParser.Request parsed = new BasicCurlParser().parse(curl);

        assertEquals("POST", parsed.getMethod());
        assertEquals("http://example.com/submit", parsed.getUrl());
        assertEquals("payload", parsed.getPostData());
        List<String> headers = parsed.getHeaders().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.toList());
        assertEquals(List.of("X-A: 1", "X-B: 2"), headers);
    }

    @Test
    void testRoundTripPreservesDuplicateHeaders() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/");
        res.setRequestHeaders("Accept: text/html\nAccept: application/json");
        res.setQueryString("x");

        BasicCurlParser.Request parsed = new BasicCurlParser().parse(CurlCommandFormatter.format(res));
        List<String> accept = parsed.getHeaders().stream()
                .filter(e -> "Accept".equals(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        assertEquals(List.of("text/html", "application/json"), accept);
    }
}
