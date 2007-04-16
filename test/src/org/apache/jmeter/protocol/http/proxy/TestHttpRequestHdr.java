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

package org.apache.jmeter.protocol.http.proxy;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPArgument;

public class TestHttpRequestHdr  extends JMeterTestCase {
    public TestHttpRequestHdr(String name) {
        super(name);
    }

    public void testRepeatedArguments() throws Exception {
        // A HTTP GET request
        String TEST_GET_REQ = 
            "GET http://localhost/matrix.html" 
            + "?update=yes&d=1&d=2&d=&d=&d=&d=&d=&d=1&d=2&d=1&d=&d= "
            + "HTTP/1.0\n\n";
        HTTPSamplerBase s = getSamplerForRequest(TEST_GET_REQ, "UTF-8");
        assertEquals(HTTPSamplerBase.GET, s.getMethod());

        // Check arguments
        Arguments arguments = s.getArguments();
        assertEquals(13, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "update", "yes", false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "d", "1", false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "d", "2", false);
        checkArgument((HTTPArgument)arguments.getArgument(3), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(4), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(5), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(6), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(7), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(8), "d", "1", false);
        checkArgument((HTTPArgument)arguments.getArgument(9), "d", "2", false);
        checkArgument((HTTPArgument)arguments.getArgument(10), "d", "1", false);
        checkArgument((HTTPArgument)arguments.getArgument(11), "d", "", false);
        // I see that the value gets trimmed, not sure if that is correct
        checkArgument((HTTPArgument)arguments.getArgument(12), "d", "", false);

        // A HTTP POST request
        String postBody = "update=yes&d=1&d=2&d=&d=&d=&d=&d=&d=1&d=2&d=1&d=&d= ";
        String TEST_POST_REQ = "POST http://localhost/matrix.html HTTP/1.0\n"
                + "Content-type: "
                + HTTPSamplerBase.APPLICATION_X_WWW_FORM_URLENCODED
                + "; charset=UTF-8\n"
                + postBody;
        s = getSamplerForRequest(TEST_POST_REQ, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());

        // Check arguments
        arguments = s.getArguments();
        assertEquals(13, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "update", "yes", false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "d", "1", false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "d", "2", false);
        checkArgument((HTTPArgument)arguments.getArgument(3), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(4), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(5), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(6), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(7), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(8), "d", "1", false);
        checkArgument((HTTPArgument)arguments.getArgument(9), "d", "2", false);
        checkArgument((HTTPArgument)arguments.getArgument(10), "d", "1", false);
        checkArgument((HTTPArgument)arguments.getArgument(11), "d", "", false);
        checkArgument((HTTPArgument)arguments.getArgument(12), "d", " ", false);

        // A HTTP POST request, with content-type text/plain
        TEST_POST_REQ = "POST http://localhost/matrix.html HTTP/1.0\n"
                + "Content-type: text/plain; charset=UTF-8\n"
                + postBody;
        s = getSamplerForRequest(TEST_POST_REQ, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());

        // Check arguments
        // We should have one argument, with the value equal to the post body
        arguments = s.getArguments();
        assertEquals(1, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "", postBody, false);
    }
        
    // TODO: will need changing if arguments can be saved in decoded form 
    public void testEncodedArguments() throws Exception {
            // A HTTP GET request
        String TEST_GET_REQ = "GET http://localhost:80/matrix.html"
            + "?abc"
            + "?SPACE=a+b"
            + "&space=a%20b"
            + "&query=What?"
            + " HTTP/1.1\n\n";
        HTTPSamplerBase s = getSamplerForRequest(TEST_GET_REQ, "UTF-8");
        assertEquals(HTTPSamplerBase.GET, s.getMethod());

        // Check arguments
        Arguments arguments = s.getArguments();
        assertEquals(3, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "abc?SPACE", "a+b", false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "space", "a%20b", false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "query", "What?", false);

        // A HTTP POST request
        String postBody = "abc?SPACE=a+b&space=a%20b&query=What?";
        String TEST_POST_REQ = "POST http://localhost:80/matrix.html HTTP/1.1\n"
            + "Content-type: "
            + HTTPSamplerBase.APPLICATION_X_WWW_FORM_URLENCODED
            + "; charset=UTF-8\n"
            + postBody;
        s = getSamplerForRequest(TEST_POST_REQ, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());
        
        // Check arguments
        arguments = s.getArguments();
        assertEquals(3, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "abc?SPACE", "a+b", false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "space", "a%20b", false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "query", "What?", false);
    }

    private HTTPSamplerBase getSamplerForRequest(String request, String contentEncoding)
            throws IOException {
        HttpRequestHdr req = new HttpRequestHdr();
        ByteArrayInputStream bis = new ByteArrayInputStream(request.getBytes(contentEncoding));
        req.parse(bis);
        bis.close();
        return req.getSampler();
    }
    
    private void checkArgument(
            HTTPArgument arg,
            String expectedName,
            String expectedValue,
            boolean expectedEncoded) {
        assertEquals(expectedName, arg.getName());
        assertEquals(expectedValue, arg.getValue());
        assertEquals(expectedEncoded, arg.isAlwaysEncoded());
    }
}
