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
    public void testChromeParsingNotCompressed() {
        String cmdLine = "curl 'https://jmeter.apache.org/' -H 'Proxy-Connection: keep-alive' "
                + "-H 'Proxy-Authorization: Basic XXXXXXXXX/' -H 'Upgrade-Insecure-Requests: 1' "
                + "-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko)"
                    + " Chrome/70.0.3538.102 Mobile Safari/537.36' "
                + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8' "
                + "-H 'Accept-Encoding: gzip, deflate' "
                + "-H 'Accept-Language: en-US,en;q=0.9,fr;q=0.8'";
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
    public void testPost() {
        String cmdLine = "curl 'https://jmeter.apache.org/test' "
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
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testError() {
        String cmdLine = "curl 'https://jmeter.apache.org/' -u -H 'Proxy-Connection: keep-alive' "
                + "-H 'Proxy-Authorization: Basic XXXXXXXXX/' "
                + "-H 'Upgrade-Insecure-Requests: 1' "
                + "-H 'User-Agent: Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko)"
                    + " Chrome/70.0.3538.102 Mobile Safari/537.36' "
                    + "-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8' "
                    + "-H 'Accept-Encoding: gzip, deflate' "
                    + "-H 'Accept-Language: en-US,en;q=0.9,fr;q=0.8'";
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        basicCurlParser.parse(cmdLine);
    }
}
