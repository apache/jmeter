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

package org.apache.jmeter.protocol.http.visualizers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.junit.jupiter.api.Test;

class RequestViewCurlTest {

    private static HTTPSampleResult result(String method, String url) throws MalformedURLException {
        HTTPSampleResult res = new HTTPSampleResult();
        res.setHTTPMethod(method);
        if (url != null) {
            res.setURL(new URL(url));
        }
        return res;
    }

    @Test
    void testSimpleGet() throws Exception {
        HTTPSampleResult res = result("GET", "http://example.com/path?a=1");
        String curl = RequestViewCurl.buildCurlCommand(res);

        assertTrue(curl.startsWith("curl -X 'GET'"), curl);
        assertTrue(curl.contains("'http://example.com/path?a=1'"), curl);
        assertFalse(curl.contains("--data-raw"), curl);
    }

    @Test
    void testHeadersAreEmitted() throws Exception {
        HTTPSampleResult res = result("GET", "http://example.com/");
        res.setRequestHeaders("Accept: application/json\nUser-Agent: JMeter");
        String curl = RequestViewCurl.buildCurlCommand(res);

        assertTrue(curl.contains("-H 'Accept: application/json'"), curl);
        assertTrue(curl.contains("-H 'User-Agent: JMeter'"), curl);
    }

    @Test
    void testPostBody() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/submit");
        res.setRequestHeaders("Content-Type: application/json");
        res.setQueryString("{\"name\":\"value\"}");
        String curl = RequestViewCurl.buildCurlCommand(res);

        assertTrue(curl.contains("-X 'POST'"), curl);
        assertTrue(curl.contains("--data-raw '{\"name\":\"value\"}'"), curl);
    }

    @Test
    void testConnectionAndAutoHeadersAreSkipped() throws Exception {
        HTTPSampleResult res = result("POST", "https://example.com/");
        res.setRequestHeaders("Connection: keep-alive\n"
                + "Content-Length: 140\n"
                + "Transfer-Encoding: chunked\n"
                + "Content-Type: application/json\n"
                + "Accept: application/json");
        res.setQueryString("{}");
        String curl = RequestViewCurl.buildCurlCommand(res);

        // curl manages these / they are forbidden in HTTP/2, so they must be dropped
        assertFalse(curl.contains("Connection"), curl);
        assertFalse(curl.contains("Content-Length"), curl);
        assertFalse(curl.contains("Transfer-Encoding"), curl);
        // genuine request headers are still kept
        assertTrue(curl.contains("-H 'Content-Type: application/json'"), curl);
        assertTrue(curl.contains("-H 'Accept: application/json'"), curl);
    }

    @Test
    void testCookiesAddedAsFlag() throws Exception {
        HTTPSampleResult res = result("GET", "http://example.com/");
        res.setCookies("session=abc; theme=dark");
        String curl = RequestViewCurl.buildCurlCommand(res);

        assertTrue(curl.contains("-b 'session=abc; theme=dark'"), curl);
    }

    @Test
    void testCookieHeaderNotDuplicated() throws Exception {
        HTTPSampleResult res = result("GET", "http://example.com/");
        res.setRequestHeaders("Cookie: session=abc");
        res.setCookies("session=abc");
        String curl = RequestViewCurl.buildCurlCommand(res);

        assertTrue(curl.contains("-H 'Cookie: session=abc'"), curl);
        assertFalse(curl.contains("-b "), curl);
    }

    @Test
    void testSingleQuoteIsEscaped() throws Exception {
        HTTPSampleResult res = result("POST", "http://example.com/");
        res.setQueryString("name=O'Brien");
        String curl = RequestViewCurl.buildCurlCommand(res);

        // single quote becomes '\'' so the value stays shell-safe
        assertTrue(curl.contains("--data-raw 'name=O'\\''Brien'"), curl);
    }

    @Test
    void testNullUrlDoesNotFail() throws Exception {
        HTTPSampleResult res = result("GET", null);
        String curl = RequestViewCurl.buildCurlCommand(res);

        assertEquals("curl -X 'GET'", curl);
    }
}
