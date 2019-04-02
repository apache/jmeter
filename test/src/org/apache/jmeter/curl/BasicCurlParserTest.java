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
package org.apache.jmeter.curl;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 5.1
 */
public class BasicCurlParserTest {
    /**
     * 
     */
    public BasicCurlParserTest() {
        super();
    }

    @Test
    public void testFFParsing() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'DNT: 1' "
                + "-H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("http://jmeter.apache.org/", request.getUrl());
        Assert.assertEquals(6, request.getHeaders().size());
        Assert.assertTrue(request.isCompressed());
        Assert.assertEquals("GET", request.getMethod());
        String resParser = "Request [compressed=true, url=http://jmeter.apache.org/, method=GET, "
                + "headers={User-Agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 "
                + "Firefox/63.0, Accept=text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8, "
                + "Accept-Language=en-US,en;q=0.5, DNT=1, Connection=keep-alive, Upgrade-Insecure-Requests=1}]";
        Assert.assertEquals("The method 'toString' should get all parameters correctly", resParser, request.toString());
    }

    @Test
    public void testChromeParsing() {
        String cmdLine = "curl 'https://jmeter.apache.org/' -H 'Proxy-Connection: keep-alive' "
                + "-H 'Proxy-Authorization: Basic XXXXXXXXX/' -H 'Upgrade-Insecure-Requests: 1' "
                + "-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/70.0.3538.102 Mobile Safari/537.36' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8' "
                + "-H 'Accept-Encoding: gzip, deflate' -H 'Accept-Language: en-US,en;q=0.9,fr;q=0.8' --compressed";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("https://jmeter.apache.org/", request.getUrl());
        Assert.assertEquals(7, request.getHeaders().size());
        Assert.assertTrue(request.isCompressed());
        Assert.assertEquals("GET", request.getMethod());
    }

    @Test
    public void testDoubleQuote() {
        String cmdLine = "curl \"http://jmeter.apache.org/\"";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("The method 'translateCommandline' should get the url correctly When using double quotes, ",
                "http://jmeter.apache.org/", request.getUrl());
    }

    @Test
    public void testChromeParsingNotCompressed() {
        String cmdLine = "curl 'https://jmeter.apache.org/' -H 'Proxy-Connection: keep-alive' "
                + "-H 'Proxy-Authorization: Basic XXXXXXXXX/' -H 'Upgrade-Insecure-Requests: 1' "
                + "-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko)"
                + " Chrome/70.0.3538.102 Mobile Safari/537.36' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8' "
                + "-H 'Accept-Encoding: gzip, deflate' " + "-H 'Accept-Language: en-US,en;q=0.9,fr;q=0.8'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("https://jmeter.apache.org/", request.getUrl());
        Assert.assertEquals(7, request.getHeaders().size());
        Assert.assertFalse(request.isCompressed());
        Assert.assertEquals("GET", request.getMethod());
    }

    @Test
    public void testChromeParsingNoHeaders() {
        String cmdLine = "curl 'https://jmeter.apache.org/'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("https://jmeter.apache.org/", request.getUrl());
        Assert.assertTrue(request.getHeaders().isEmpty());
        Assert.assertFalse(request.isCompressed());
        Assert.assertEquals("GET", request.getMethod());
    }

    @Test
    public void testNullCommand() {
        String cmdLine = "";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("The method 'translateCommandline' should return 'null' when command is empty, ",
                "Request [compressed=false, url=null, method=GET, headers={}]", request.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnbalancedQuotes() {
        String cmdLine = "curl \"https://jmeter.apache.org/'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        basicCurlParser.parse(cmdLine);
        Assert.fail("The method 'translateCommandline shouldn't run when the quotes are not balanced,");
    }

    @Test
    public void testPost() {
        String cmdLine = "curl 'https://jmeter.apache.org/test' -X 'POST' " 
                + "-H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' -H 'Accept: */*' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'Referer: https://www.example.com/' "
                + "-H 'content-type: application/json;charset=UTF-8' -H 'Origin: https://www.example.com' "
                + "-H 'DNT: 1' -H 'Connection: keep-alive' -H 'TE: Trailers' "
                + "--data '{\"abc\":\"123\",\"no\":\"matter on sunshine\"}'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("https://jmeter.apache.org/test", request.getUrl());
        Assert.assertEquals(9, request.getHeaders().size());
        Assert.assertTrue(request.isCompressed());
        Assert.assertEquals("POST", request.getMethod());
        Assert.assertEquals("The method 'getPostData' should return the data correctly",
                "{\"abc\":\"123\",\"no\":\"matter on sunshine\"}", request.getPostData());
    }

    @Test
    public void testMethodPut() {
        String cmdLine = "curl -X 'PUT' 'https://jmeter.apache.org/test' "
                + "-H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' -H 'Accept: */*' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'Referer: https://www.example.com/' "
                + "-H 'content-type: application/json;charset=UTF-8' -H 'Origin: https://www.example.com' "
                + "-H 'DNT: 1' -H 'Connection: keep-alive' -H 'TE: Trailers'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("PUT", request.getMethod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testError() {
        String cmdLine = "curl 'https://jmeter.apache.org/' -u -H 'Proxy-Connection: keep-alive' "
                + "-H 'Proxy-Authorization: Basic XXXXXXXXX/' "
                + "-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko)"
                + " Chrome/70.0.3538.102 Mobile Safari/537.36' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8' "
                + "-H 'Accept-Encoding: gzip, deflate' " + "-H 'Accept-Language: en-US,en;q=0.9,fr;q=0.8'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        basicCurlParser.parse(cmdLine);
    }

    @Test
    public void testUserAuth() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'User-Agent: Mozilla/5.0 (Macintosh;"
                + " Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'DNT: 1' "
                + "-H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1' -A 'Mozilla/5.0'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser', the quantity of Headers should be 6' ", 6,
                request.getHeaders().size());
        Assert.assertEquals("With method 'parser', Headers need to add 'user-agent' with value 'Mozilla/5.0' ",
                "Mozilla/5.0", request.getHeaders().get("User-Agent"));
    }

    @Test
    public void testConnectMax() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'User-Agent: Mozilla/5.0 "
                + "(Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'DNT: 1' "
                + "-H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1' --connect-timeout '2000'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("With method 'parser', " + "request should contain 'connect_timeout=2000'", "2000",
                request.getConnectTimeout());
    }

    @Test
    public void testCookie() {
        String cmdLine = "curl 'http://jmeter.apache.org/' -H 'User-Agent: Mozilla/5.0"
                + " (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' "
                + "-H 'Accept-Language: en-US,en;q=0.5' --compressed -H 'DNT: 1' "
                + "-H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1' -b 'name=Tom;password=123456'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        BasicCurlParser.Request request = basicCurlParser.parse(cmdLine);
        Assert.assertEquals("http://jmeter.apache.org/", request.getUrl());
        Assert.assertTrue(request.isCompressed());
        Assert.assertEquals("GET", request.getMethod());
        List<Cookie> cookies = new ArrayList<>();
        Cookie cookie1 = new Cookie();
        cookie1.setName("name");
        cookie1.setValue("Tom");
        cookie1.setDomain("jmeter.apache.org");
        cookie1.setPath("/");
        cookies.add(cookie1);
        Cookie cookie2 = new Cookie();
        cookie2.setName("password");
        cookie2.setValue("123456");
        cookie1.setDomain("jmeter.apache.org");
        cookie1.setPath("/");
        cookies.add(cookie2);
        Assert.assertTrue("With method 'parser',request should contain the parameters of the cookie",
                request.getCookies().contains(cookie1));
        Assert.assertEquals("With method 'parser',request should contain the parameters of the cookie", 2,
                request.getCookies().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUrlErrorForCookie() {
        String cmdLine = "curl 'jmeter.apache.org' -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:63.0) Gecko/20100101 Firefox/63.0' "
                + "-H 'Connection: keep-alive' -H 'Upgrade-Insecure-Requests: 1' -b 'name=Tom;password=123456'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        basicCurlParser.parse(cmdLine);
        Assert.fail("The method 'parser() can't transfer cookies Unqualified URLs,");
    }
}
