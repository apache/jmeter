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
import java.net.URLEncoder;

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
        checkArgument((HTTPArgument)arguments.getArgument(0), "update", "yes", "yes", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "d", "1", "1", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "d", "2", "2", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(3), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(4), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(5), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(6), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(7), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(8), "d", "1", "1", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(9), "d", "2", "2", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(10), "d", "1", "1", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(11), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(12), "d", "", "", "UTF-8", false);

        // A HTTP POST request
        String postBody = "update=yes&d=1&d=2&d=&d=&d=&d=&d=&d=1&d=2&d=1&d=&d=";
        String TEST_POST_REQ = "POST http://localhost/matrix.html HTTP/1.0\n"
                + "Content-type: "
                + HTTPSamplerBase.APPLICATION_X_WWW_FORM_URLENCODED
                + "; charset=UTF-8\r\n"
                + "Content-length: " + postBody.length() + "\r\n"
                + "\r\n"
                + postBody;
        s = getSamplerForRequest(TEST_POST_REQ, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());
        assertFalse(s.getDoMultipartPost());

        // Check arguments
        arguments = s.getArguments();
        assertEquals(13, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "update", "yes", "yes", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "d", "1", "1", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "d", "2", "2", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(3), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(4), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(5), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(6), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(7), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(8), "d", "1", "1", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(9), "d", "2", "2", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(10), "d", "1", "1", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(11), "d", "", "", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(12), "d", "", "", "UTF-8", false);

        // A HTTP POST request, with content-type text/plain
        TEST_POST_REQ = "POST http://localhost/matrix.html HTTP/1.0\n"
                + "Content-type: text/plain; charset=UTF-8\n"
                + postBody;
        s = getSamplerForRequest(TEST_POST_REQ, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());
        assertFalse(s.getDoMultipartPost());

        // Check arguments
        // We should have one argument, with the value equal to the post body
        arguments = s.getArguments();
        assertEquals(1, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "", postBody, postBody, "UTF-8", false);
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
        checkArgument((HTTPArgument)arguments.getArgument(0), "abc?SPACE", "a+b", "a+b", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "space", "a%20b", "a%20b", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "query", "What?", "What?", "UTF-8", false);

        // A HTTP POST request
        String postBody = "abc?SPACE=a+b&space=a%20b&query=What?";
        String TEST_POST_REQ = "POST http://localhost:80/matrix.html HTTP/1.1\n"
            + "Content-type: "
            + HTTPSamplerBase.APPLICATION_X_WWW_FORM_URLENCODED
            + "; charset=UTF-8\n"
            + postBody;
        s = getSamplerForRequest(TEST_POST_REQ, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());
        assertFalse(s.getDoMultipartPost());
        
        // Check arguments
        arguments = s.getArguments();
        assertEquals(3, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "abc?SPACE", "a+b", "a+b", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "space", "a%20b", "a%20b", "UTF-8", false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "query", "What?", "What?", "UTF-8", false);
    }
    
    public void testPostMultipartFormData() throws Exception {
        // A HTTP POST request, multipart/form-data, simple values,
        String contentEncoding = "UTF-8";
        String boundary = "xf8SqlDNvmn6mFYwrioJaeUR2_Z4cLRXOSmB";
        String endOfLine = "\r\n";
        String titleValue = "mytitle";
        String descriptionValue = "mydescription";
        String postBody = createMultipartFormBody(titleValue, descriptionValue, contentEncoding, true, boundary, endOfLine);
        String testPostRequest = createMultipartFormRequest(postBody, boundary, endOfLine);

        HTTPSamplerBase s = getSamplerForRequest(testPostRequest, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());
        assertTrue(s.getDoMultipartPost());
        
        // Check arguments
        Arguments arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "title", titleValue, titleValue, contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "description", descriptionValue, descriptionValue, contentEncoding, false);
        
        // A HTTP POST request, multipart/form-data, simple values,
        // with \r\n as end of line, which is according to spec,
        // and with more headers in each multipart
        endOfLine = "\r\n";
        titleValue = "mytitle";
        descriptionValue = "mydescription";
        postBody = createMultipartFormBody(titleValue, descriptionValue, contentEncoding, true, boundary, endOfLine);
        testPostRequest = createMultipartFormRequest(postBody, boundary, endOfLine);

        s = getSamplerForRequest(testPostRequest, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());
        assertTrue(s.getDoMultipartPost());
        
        // Check arguments
        arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "title", titleValue, titleValue, contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "description", descriptionValue, descriptionValue, contentEncoding, false);

        // A HTTP POST request, multipart/form-data, simple values,
        // with \n as end of line, which should also be handled,
        // and with more headers in each multipart
        endOfLine = "\n";
        titleValue = "mytitle";
        descriptionValue = "mydescription";
        postBody = createMultipartFormBody(titleValue, descriptionValue, contentEncoding, true, boundary, endOfLine);
        testPostRequest = createMultipartFormRequest(postBody, boundary, endOfLine);

        s = getSamplerForRequest(testPostRequest, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());
        assertTrue(s.getDoMultipartPost());
        
        // Check arguments
        arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "title", titleValue, titleValue, contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "description", descriptionValue, descriptionValue, contentEncoding, false);
        
        // A HTTP POST request, multipart/form-data, with value that will change
        // if they are url encoded
        // Values are similar to __VIEWSTATE parameter that .net uses
        endOfLine = "\r\n";
        titleValue = "/wEPDwULLTE2MzM2OTA0NTYPZBYCAgMPZ/rA+8DZ2dnZ2dnZ2d/GNDar6OshPwdJc=";
        descriptionValue = "mydescription";
        postBody = createMultipartFormBody(titleValue, descriptionValue, contentEncoding, true, boundary, endOfLine);
        testPostRequest = createMultipartFormRequest(postBody, boundary, endOfLine);

        s = getSamplerForRequest(testPostRequest, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());
        assertTrue(s.getDoMultipartPost());
        
        // Check arguments
        arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "title", titleValue, titleValue, contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "description", descriptionValue, descriptionValue, contentEncoding, false);
    }

    public void testPostMultipartFileUpload() throws Exception {
        // A HTTP POST request, multipart/form-data, simple values,
        String boundary = "xf8SqlDNvmn6mFYwrioJaeUR2_Z4cLRXOSmB";
        String endOfLine = "\r\n";
        String fileFieldValue = "test_file";
        String fileName = "somefilename.txt";
        String mimeType = "text/plain";
        String fileContent = "somedummycontent\n\ndfgdfg\r\nfgdgdg\nContent-type:dfsfsfds";
        String postBody = createMultipartFileUploadBody(fileFieldValue, fileName, mimeType, fileContent, boundary, endOfLine);
        String testPostRequest = createMultipartFormRequest(postBody, boundary, endOfLine);
        
        HTTPSamplerBase s = getSamplerForRequest(testPostRequest, "UTF-8");
        assertEquals(HTTPSamplerBase.POST, s.getMethod());
        assertTrue(s.getDoMultipartPost());

        // Check arguments
        Arguments arguments = s.getArguments();
        assertEquals(0, arguments.getArgumentCount());
        assertEquals(fileFieldValue, s.getFileField());
        assertEquals(fileName, s.getFilename());
        assertEquals(mimeType, s.getMimetype());
    }        
    
    private String createMultipartFormBody(String titleValue, String descriptionValue, String contentEncoding, boolean includeExtraHeaders, String boundary, String endOfLine) {
        // Title multipart
        String postBody = "--" + boundary + endOfLine
            + "Content-Disposition: form-data; name=\"title\"" + endOfLine;
        if(includeExtraHeaders) {
            postBody += "Content-Type: text/plain; charset=" + contentEncoding + endOfLine
            + "Content-Transfer-Encoding: 8bit" + endOfLine;
        }
        postBody += endOfLine
            + titleValue + endOfLine
            + "--" + boundary + endOfLine;
        // Description multipart
        postBody += "Content-Disposition: form-data; name=\"description\"" + endOfLine;
        if(includeExtraHeaders) {
            postBody += "Content-Type: text/plain; charset=" + contentEncoding + endOfLine
                + "Content-Transfer-Encoding: 8bit" + endOfLine;
        }
        postBody += endOfLine
            + descriptionValue + endOfLine
            + "--" + boundary + "--" + endOfLine;

        return postBody;
    }

    private String createMultipartFileUploadBody(String fileField, String fileName, String fileMimeType, String fileContent, String boundary, String endOfLine) {
        // File upload multipart
        String postBody = "--" + boundary + endOfLine
            + "Content-Disposition: form-data; name=\"" + fileField + "\" filename=\"" + fileName + "\"" + endOfLine
            + "Content-Type: " + fileMimeType + endOfLine
            + "Content-Transfer-Encoding: binary" + endOfLine
            + endOfLine
            + fileContent + endOfLine
            + "--" + boundary + "--" + endOfLine;
        return postBody;
    }
    
    private String createMultipartFormRequest(String postBody, String boundary, String endOfLine) {
        String postRequest = "POST http://localhost:80/matrix.html HTTP/1.1" + endOfLine
            + "Content-type: "
            + HTTPSamplerBase.MULTIPART_FORM_DATA
            + "; boundary=" + boundary + endOfLine
            + "Content-length: " + postBody.length() + endOfLine
            + endOfLine
            + postBody;
        return postRequest;
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
            String expectedEncodedValue,
            String contentEncoding,
            boolean expectedEncoded) throws IOException {
        assertEquals(expectedName, arg.getName());
        assertEquals(expectedValue, arg.getValue());
        assertEquals(expectedEncodedValue, arg.getEncodedValue(contentEncoding));
        assertEquals(expectedEncoded, arg.isAlwaysEncoded());
    }
}
