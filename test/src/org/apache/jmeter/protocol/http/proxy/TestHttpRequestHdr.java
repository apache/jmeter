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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.junit.Test;

public class TestHttpRequestHdr extends JMeterTestCase {

    @Test
    public void testRepeatedArguments() throws Exception {
        String url = "http://localhost/matrix.html";
        // A HTTP GET request
        String contentEncoding = "UTF-8";
        String testGetRequest = 
            "GET " + url
            + "?update=yes&d=1&d=2&d=&d=&d=&d=&d=&d=1&d=2&d=1&d=&d= "
            + "HTTP/1.0\r\n\r\n";
        HTTPSamplerBase s = getSamplerForRequest(url, testGetRequest, contentEncoding);
        assertEquals(HTTPConstants.GET, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        Arguments arguments = s.getArguments();
        assertEquals(13, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "update", "yes", "yes", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "d", "1", "1", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "d", "2", "2", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(3), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(4), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(5), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(6), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(7), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(8), "d", "1", "1", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(9), "d", "2", "2", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(10), "d", "1", "1", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(11), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(12), "d", "", "", contentEncoding, false);

        // A HTTP POST request
        contentEncoding = "UTF-8";
        String postBody = "update=yes&d=1&d=2&d=&d=&d=&d=&d=&d=1&d=2&d=1&d=&d=";
        String testPostRequest = "POST " + url + " HTTP/1.0\n"
                + "Content-type: "
                + HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED + "\r\n"
                + "Content-length: " + getBodyLength(postBody, contentEncoding) + "\r\n"
                + "\r\n"
                + postBody;
        s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertFalse(s.getDoMultipartPost());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        arguments = s.getArguments();
        assertEquals(13, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "update", "yes", "yes", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "d", "1", "1", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "d", "2", "2", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(3), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(4), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(5), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(6), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(7), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(8), "d", "1", "1", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(9), "d", "2", "2", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(10), "d", "1", "1", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(11), "d", "", "", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(12), "d", "", "", contentEncoding, false);

        // A HTTP POST request, with content-type text/plain
        contentEncoding = "UTF-8";
        postBody = "update=yes&d=1&d=2&d=&d=&d=&d=&d=&d=1&d=2&d=1&d=\uc385&d=";
        testPostRequest = "POST " + url + " HTTP/1.1\r\n"
                + "Content-type: text/plain\r\n"
                + "Content-length: " + getBodyLength(postBody, contentEncoding) + "\r\n"
                + "\r\n"
                + postBody;
        s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertFalse(s.getDoMultipartPost());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        // We should have one argument, with the value equal to the post body
        arguments = s.getArguments();
        assertEquals(1, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "", postBody, postBody, contentEncoding, false);
        
        // A HTTP POST request, with content-type text/plain; charset=UTF-8
        // The encoding should be picked up from the header we send with the request
        contentEncoding = "UTF-8";
        postBody = "update=yes&d=1&d=2&d=&d=&d=&d=&d=&d=1&d=2&d=1&d=\uc385&d=";
        testPostRequest = "POST " + url + " HTTP/1.1\r\n"
                + "Content-type: text/plain; charset=" + contentEncoding + "\r\n"
                + "Content-length: " + getBodyLength(postBody, contentEncoding) + "\r\n"
                + "\r\n"
                + postBody;
        // Use null for url to simulate that HttpRequestHdr do not
        // know the encoding for the page. Specify contentEncoding, so the
        // request is "sent" using that encoding
        s = getSamplerForRequest(null, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertFalse(s.getDoMultipartPost());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        // We should have one argument, with the value equal to the post body
        arguments = s.getArguments();
        assertEquals(1, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "", postBody, postBody, contentEncoding, false);
        
        // A HTTP POST request, with content-type text/plain; charset=UTF-8
        // The encoding should be picked up from the header we send with the request
        contentEncoding = "UTF-8";
        url =  "http://vmdal-hqqa9/retalixhq/GG_Implementation/ScreenEntity/ScreenEntityHTTP.aspx?Action=Save&ET=Vendor&TT=Single&Sid=1347280336092";
        postBody = "<Action UIStatus=\"2\"><Vendor Id=\"9292\" HOST_ID=\"0\" VENDOR=\"9292\" TERMS_TYPE=\"No Terms\" TERMS=\"0 %\""
                + " AUTO_PRICE=\"Use System Default\" VM_VENDOR_TYPE=\"DSD Vendor\" ITEM_FORMAT=\"PLU\" COST_ENTRY_SORT=\"UPC/EAN\""
                + " VM_REPORT_SORT=\"UPC/EAN\" VM_ORDER_SORT=\"UPC/EAN\" VM_RECEIVING_SORT=\"UPC/EAN\" VM_MAX_BACK_ORDERS=\"99\""
                + " MAX_OPEN_DAYS=\"99\" PAY_BASED_ON=\"System Cost\" ORDER_COST_DATE=\"Use System Rule\" VM_CONSIDER_FREE=\"False\""
                + " VM_SHOW_DETAIL=\"False\" VM_UPDATE_COST=\"No\" RD_USE_VENDOR_CC=\"False\" BLIND_RECEIVING=\"Default\""
                + " EXCLUDE_RECEIVED_COST=\"False\" PRINT_ITEM_ADJ=\"False\" PRINT_OVERALL_ADJ=\"False\" PRINT_TAX_DETAIL=\"False\""
                + " BLOCK_PRICE_VIEW=\"False\" DELIVERY_STATUS=\"No Delivery\" AUTO_RECEIVE=\"False\" TARGET_GM_FLAG=\"%\""
                + " MINIMUM_GM_FLAG=\"%\" MARGIN_TYPE=\"Gross Margin\" HOLD_REGULAR=\"Default\" HOLD_SPECIALS=\"Default\""
                + " TRUSTING_VENDOR=\"False\" AUTO_ACCEPT=\"All\" EARLY_RCPT_AFFECTS=\"All Costs\" SBT_ELIGIBLE=\"Not eligible\""
                + " SBT_REPORTING_DAY=\"Monday\" AUTO_BALANCE_FLAG=\"$\" DAX_MANAGED=\"False\" CHANGE_ID=\"QA\" CHANGE_SOURCE=\"Manual Change\""
                + " ORIGINAL_SOURCE=\"Manual Change\" RECORD_STATUS=\"Add\" RECORD_STATUS_DATE=\"9/7/2012 8:34:58 AM\" VENDOR_NAME=\"test\""
                + " UIStatus=\"2\"/></Action>";
        testPostRequest = "POST " + url + " HTTP/1.1\r\n"
                + "x-requested-with: XMLHttpRequest" + "\r\n"
                + "Accept-Language: en-us" + "\r\n"
                + "Referer: http://vmdal-hqqa9/retalixhq/GG_Implementation/ScreenEntity/ScreenEntityPage.aspx?ET=Vendor&TT=Single&"
                + "WM=2&UID=9292&Sid=1347280331908&UITH=Blue&MUID=window_0" + "\r\n"
                + "Accept: */*" + "\r\n"
                + "Content-Type: application/x-www-form-urlencoded" + "\r\n"
                + "Accept-Encoding: gzip, deflate" + "\r\n"
                + "User-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729;"
                + " .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; Tablet PC 2.0)" + "\r\n"
                + "Host: vmdal-hqqa9" + "\r\n"
                + "Content-Length: "+ getBodyLength(postBody, contentEncoding) + "\r\n"
                + "Proxy-Connection: Keep-Alive" + "\r\n"
                + "Pragma: no-cache" + "\r\n"
                + "Cookie: RHQ=sid=5aaeb66c-e174-4f4c-9928-83cffcc62150" + "\r\n"
                + "\r\n"
                + postBody;
        // Use null for url to simulate that HttpRequestHdr do not
        // know the encoding for the page. Specify contentEncoding, so the
        // request is "sent" using that encoding
        s = getSamplerForRequest(null, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertFalse(s.getDoMultipartPost());
        // TODO Should this be OK ?
        //assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        // We should have one argument, with the value equal to the post body
        arguments = s.getArguments();
        assertEquals(1, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "", postBody, postBody, contentEncoding, false);
    }

    @Test
    public void testEncodedArguments() throws Exception {
        String url = "http://localhost/matrix.html";
        testEncodedArguments(url);
    }
     
    
    @Test
    public void testEncodedArgumentsIPv6() throws Exception {
        String url = "http://[::1]:8080/matrix.html";
        testEncodedArguments(url);
    }

    private void testEncodedArguments(String url) throws Exception {
        // A HTTP GET request, with encoding not known 
        String contentEncoding = "";
        String queryString = "abc%3FSPACE=a+b&space=a%20b&query=What%3F"; 
        String testGetRequest = "GET " + url
            + "?" + queryString
            + " HTTP/1.1\r\n\r\n";
        // Use null for url and contentEncoding, to simulate that HttpRequestHdr do not
        // know the encoding for the page
        HTTPSamplerBase s = getSamplerForRequest(null, testGetRequest, null);
        assertEquals(HTTPConstants.GET, s.getMethod());
        assertEquals(queryString, s.getQueryString());
        assertEquals(contentEncoding, s.getContentEncoding());

        // Check arguments
        Arguments arguments = s.getArguments();
        assertEquals(3, arguments.getArgumentCount());
        // When the encoding is not known, the argument will get the encoded value, and the "encode?" set to false
        checkArgument((HTTPArgument)arguments.getArgument(0), "abc%3FSPACE", "a+b", "a+b", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "space", "a%20b", "a%20b", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "query", "What%3F", "What%3F", contentEncoding, false);

        // A HTTP GET request, with UTF-8 encoding 
        contentEncoding = "UTF-8";
        queryString = "abc%3FSPACE=a+b&space=a%20b&query=What%3F"; 
        testGetRequest = "GET " + url
            + "?" + queryString
            + " HTTP/1.1\r\n\r\n";
        s = getSamplerForRequest(url, testGetRequest, contentEncoding);
        assertEquals(HTTPConstants.GET, s.getMethod());
        String expectedQueryString = "abc%3FSPACE=a+b&space=a+b&query=What%3F";
        assertEquals(expectedQueryString, s.getQueryString());
        assertEquals(contentEncoding, s.getContentEncoding());

        // Check arguments
        arguments = s.getArguments();
        assertEquals(3, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "abc?SPACE", "a b", "a+b", contentEncoding, true);
        checkArgument((HTTPArgument)arguments.getArgument(1), "space", "a b", "a+b", contentEncoding, true);
        checkArgument((HTTPArgument)arguments.getArgument(2), "query", "What?", "What%3F", contentEncoding, true);
        
        // A HTTP POST request, with unknown encoding
        contentEncoding = "";
        String postBody = "abc%3FSPACE=a+b&space=a%20b&query=What%3F";
        String testPostRequest = "POST " + url + " HTTP/1.1\r\n"
            + "Content-type: "
            + HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED + "\r\n"
            + "Content-length: " + getBodyLength(postBody, contentEncoding) + "\r\n"
            + "\r\n"
            + postBody;
        // Use null for url and contentEncoding, to simulate that HttpRequestHdr do not
        // know the encoding for the page
        s = getSamplerForRequest(null, testPostRequest, null);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertEquals(queryString, s.getQueryString());
        assertEquals(contentEncoding, s.getContentEncoding());
        assertFalse(s.getDoMultipartPost());
        
        // Check arguments
        arguments = s.getArguments();
        assertEquals(3, arguments.getArgumentCount());
        // When the encoding is not known, the argument will get the encoded value, and the "encode?" set to false
        checkArgument((HTTPArgument)arguments.getArgument(0), "abc%3FSPACE", "a+b", "a+b", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "space", "a%20b", "a%20b", contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(2), "query", "What%3F", "What%3F", contentEncoding, false);

        // A HTTP POST request, with UTF-8 encoding
        contentEncoding = "UTF-8";
        postBody = "abc?SPACE=a+b&space=a%20b&query=What?";
        testPostRequest = "POST " + url + " HTTP/1.1\n"
            + "Content-type: "
            + HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED + "\r\n"
            + "Content-length: " + getBodyLength(postBody, contentEncoding) + "\r\n"
            + "\r\n"
            + postBody;
        s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        expectedQueryString = "abc%3FSPACE=a+b&space=a+b&query=What%3F";
        assertEquals(expectedQueryString, s.getQueryString());
        assertEquals(contentEncoding, s.getContentEncoding());
        assertFalse(s.getDoMultipartPost());
        
        // Check arguments
        arguments = s.getArguments();
        assertEquals(3, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "abc?SPACE", "a b", "a+b", contentEncoding, true);
        checkArgument((HTTPArgument)arguments.getArgument(1), "space", "a b", "a+b", contentEncoding, true);
        checkArgument((HTTPArgument)arguments.getArgument(2), "query", "What?", "What%3F", contentEncoding, true);
    }
    
    @Test
    public void testGetRequestEncodings() throws Exception {
        testGetRequestEncodings("http://localhost/matrix.html");
    }
    
    @Test
    public void testGetRequestEncodingsIPv6() throws Exception {
        testGetRequestEncodings("http://[::1]:8080/matrix.html");
    }
    
    private void testGetRequestEncodings(String url) throws Exception {
        // A HTTP GET request, with encoding not known
        String contentEncoding = "";
        String param1Value = "yes";
        String param2Value = "0+5 -\u00c5\uc385%C3%85";
        String param2ValueEncoded = URLEncoder.encode(param2Value,"UTF-8");
        String testGetRequest = 
            "GET " + url
            + "?param1=" + param1Value + "&param2=" + param2ValueEncoded + " "
            + "HTTP/1.1\r\n\r\n";
        // Use null for url and contentEncoding, to simulate that HttpRequestHdr do not
        // know the encoding for the page
        HTTPSamplerBase s = getSamplerForRequest(null, testGetRequest, null);
        assertEquals(HTTPConstants.GET, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        Arguments arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "param1", param1Value, param1Value, contentEncoding, false);
        // When the encoding is not known, the argument will get the encoded value, and the "encode?" set to false
        checkArgument((HTTPArgument)arguments.getArgument(1), "param2", param2ValueEncoded, param2ValueEncoded, contentEncoding, false);

        // A HTTP GET request, with UTF-8 encoding
        contentEncoding = "UTF-8";
        param1Value = "yes";
        param2Value = "0+5 -\u007c\u2aa1\u266a\u0153\u20a1\u0115\u0364\u00c5\u2052\uc385%C3%85";
        param2ValueEncoded = URLEncoder.encode(param2Value, contentEncoding);
        testGetRequest = 
            "GET " + url
            + "?param1=" + param1Value + "&param2=" + param2ValueEncoded + " "
            + "HTTP/1.1\r\n\r\n";
        s = getSamplerForRequest(url, testGetRequest, contentEncoding);
        assertEquals(HTTPConstants.GET, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "param1", param1Value, param1Value, contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "param2", param2Value, param2ValueEncoded, contentEncoding, true);

        // A HTTP GET request, with ISO-8859-1 encoding
        contentEncoding = "ISO-8859-1";
        param1Value = "yes";
        param2Value = "0+5 -\u00c5%C3%85";
        param2ValueEncoded = URLEncoder.encode(param2Value, contentEncoding);
        testGetRequest = 
            "GET " + url
            + "?param1=" + param1Value + "&param2=" + param2ValueEncoded + " "
            + "HTTP/1.1\r\n\r\n";
        s = getSamplerForRequest(url, testGetRequest, contentEncoding);
        assertEquals(HTTPConstants.GET, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "param1", param1Value, param1Value, contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "param2", param2Value, param2ValueEncoded, contentEncoding, true);
    }

    @Test
    public void testPostRequestEncodings() throws Exception {
        String url = "http://localhost/matrix.html";
        // A HTTP POST request, with encoding not known
        String contentEncoding = "";
        String param1Value = "yes";
        String param2Value = "0+5 -\u00c5%C3%85";
        String param2ValueEncoded = URLEncoder.encode(param2Value,"UTF-8");
        String postBody = "param1=" + param1Value + "&param2=" + param2ValueEncoded + "\r\n"; 
        String testPostRequest = 
            "POST " + url + " HTTP/1.1\r\n"
            + "Content-type: "
            + HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED + "\r\n"
            + "Content-length: " + getBodyLength(postBody, contentEncoding) + "\r\n"
            + "\r\n"
            + postBody;
        
        // Use null for url and contentEncoding, to simulate that HttpRequestHdr do not
        // know the encoding for the page
        HTTPSamplerBase s = getSamplerForRequest(null, testPostRequest, null);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        Arguments arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "param1", param1Value, param1Value, contentEncoding, false);
        // When the encoding is not known, the argument will get the encoded value, and the "encode?" set to false
        checkArgument((HTTPArgument)arguments.getArgument(1), "param2", param2ValueEncoded, param2ValueEncoded, contentEncoding, false);

        // A HTTP POST request, with UTF-8 encoding
        contentEncoding = "UTF-8";
        param1Value = "yes";
        param2Value = "0+5 -\u007c\u2aa1\u266a\u0153\u20a1\u0115\u0364\u00c5\u2052\uc385%C3%85";
        param2ValueEncoded = URLEncoder.encode(param2Value, contentEncoding);
        postBody = "param1=" + param1Value + "&param2=" + param2ValueEncoded + "\r\n"; 
        testPostRequest = 
            "POST " + url + " HTTP/1.1\r\n"
            + "Content-type: "
            + HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED + "\r\n"
            + "Content-length: " + getBodyLength(postBody, contentEncoding) + "\r\n"
            + "\r\n"
            + postBody;

        s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "param1", param1Value, param1Value, contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "param2", param2Value, param2ValueEncoded, contentEncoding, true);

        // A HTTP POST request, with ISO-8859-1 encoding
        contentEncoding = "ISO-8859-1";
        param1Value = "yes";
        param2Value = "0+5 -\u00c5%C3%85";
        param2ValueEncoded = URLEncoder.encode(param2Value, contentEncoding);
        postBody = "param1=" + param1Value + "&param2=" + param2ValueEncoded + "\r\n"; 
        testPostRequest = 
            "POST " + url + " HTTP/1.1\r\n"
            + "Content-type: "
            + HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED + "\r\n"
            + "Content-length: " + getBodyLength(postBody, contentEncoding) + "\r\n"
            + "\r\n"
            + postBody;

        s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
        // Check arguments
        arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "param1", param1Value, param1Value, contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "param2", param2Value, param2ValueEncoded, contentEncoding, true);
    }

    @Test
    public void testPostMultipartFormData() throws Exception {
        String url = "http://localhost/matrix.html";
        // A HTTP POST request, multipart/form-data, simple values,
        String contentEncoding = "UTF-8";
        String boundary = "xf8SqlDNvmn6mFYwrioJaeUR2_Z4cLRXOSmB";
        String endOfLine = "\r\n";
        String titleValue = "mytitle";
        String descriptionValue = "mydescription";
        String postBody = createMultipartFormBody(titleValue, descriptionValue, contentEncoding, true, boundary, endOfLine);
        String testPostRequest = createMultipartFormRequest(url, postBody, contentEncoding, boundary, endOfLine);

        HTTPSamplerBase s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
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
        testPostRequest = createMultipartFormRequest(url, postBody, contentEncoding, boundary, endOfLine);

        s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
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
        testPostRequest = createMultipartFormRequest(url, postBody, contentEncoding, boundary, endOfLine);

        s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
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
        testPostRequest = createMultipartFormRequest(url, postBody, contentEncoding, boundary, endOfLine);

        s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
        assertTrue(s.getDoMultipartPost());
        
        // Check arguments
        arguments = s.getArguments();
        assertEquals(2, arguments.getArgumentCount());
        checkArgument((HTTPArgument)arguments.getArgument(0), "title", titleValue, titleValue, contentEncoding, false);
        checkArgument((HTTPArgument)arguments.getArgument(1), "description", descriptionValue, descriptionValue, contentEncoding, false);
    }

    @Test
    public void testParse1() throws Exception {// no space after :
        HttpRequestHdr req = new HttpRequestHdr();
        ByteArrayInputStream bis = null;
        bis = new ByteArrayInputStream("GET xxx HTTP/1.0\r\nname:value \r\n".getBytes("ISO-8859-1"));
        req.parse(bis);
        bis.close();
        HeaderManager mgr = req.getHeaderManager();
        Header header;
        mgr.getHeaders();
        header = mgr.getHeader(0);
        assertEquals("name",header.getName());
        assertEquals("value",header.getValue());
    }
    

    @Test
    public void testParse2() throws Exception {// spaces after :
        HttpRequestHdr req = new HttpRequestHdr();
        ByteArrayInputStream bis = null;
        bis = new ByteArrayInputStream("GET xxx HTTP/1.0\r\nname:           value \r\n".getBytes("ISO-8859-1"));
        req.parse(bis);
        bis.close();
        HeaderManager mgr = req.getHeaderManager();
        Header header;
        mgr.getHeaders();
        header = mgr.getHeader(0);
        assertEquals("name",header.getName());
        assertEquals("value",header.getValue());
    }

    @Test
    public void testPostMultipartFileUpload() throws Exception {
        String url = "http://localhost/matrix.html";
        // A HTTP POST request, multipart/form-data, simple values,
        String contentEncoding = "UTF-8";
        String boundary = "xf8SqlDNvmn6mFYwrioJaeUR2_Z4cLRXOSmB";
        String endOfLine = "\r\n";
        String fileFieldValue = "test_file";
        String fileName = "somefilename.txt";
        String mimeType = "text/plain";
        String fileContent = "somedummycontent\n\ndfgdfg\r\nfgdgdg\nContent-type:dfsfsfds";
        String postBody = createMultipartFileUploadBody(fileFieldValue, fileName, mimeType, fileContent, boundary, endOfLine);
        String testPostRequest = createMultipartFormRequest(url, postBody, contentEncoding, boundary, endOfLine);
        
        HTTPSamplerBase s = getSamplerForRequest(url, testPostRequest, contentEncoding);
        assertEquals(HTTPConstants.POST, s.getMethod());
        assertEquals(contentEncoding, s.getContentEncoding());
        assertEquals("", s.getQueryString());
        assertTrue(s.getDoMultipartPost());

        // Check arguments
        Arguments arguments = s.getArguments();
        assertEquals(0, arguments.getArgumentCount());
        HTTPFileArg hfa = s.getHTTPFiles()[0]; // Assume there's at least one file
        assertEquals(fileFieldValue, hfa.getParamName());
        assertEquals(fileName, hfa.getPath());
        assertEquals(mimeType, hfa.getMimeType());
    }        

    private String createMultipartFormBody(String titleValue, String descriptionValue,
            String contentEncoding, boolean includeExtraHeaders,
            String boundary, String endOfLine) {
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

    private String createMultipartFileUploadBody(String fileField, String fileName,
            String fileMimeType, String fileContent, String boundary, String endOfLine) {
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
    
    private String createMultipartFormRequest(String url, String postBody, String contentEncoding, String boundary, String endOfLine)
            throws IOException {
        String postRequest = "POST " + url + " HTTP/1.1" + endOfLine
            + "Content-type: "
            + HTTPConstants.MULTIPART_FORM_DATA
            + "; boundary=" + boundary + endOfLine
            + "Content-length: " + getBodyLength(postBody, contentEncoding) + endOfLine
            + endOfLine
            + postBody;
        return postRequest;
    }

    private HTTPSamplerBase getSamplerForRequest(String url, String request, String contentEncoding)
            throws Exception {
        HttpRequestHdr req = new HttpRequestHdr();
        ByteArrayInputStream bis = null;
        if(contentEncoding != null) {
            bis = new ByteArrayInputStream(request.getBytes(contentEncoding));
            
        }
        else {
            // Most browsers use ISO-8859-1 as default encoding, even if spec says UTF-8
            bis = new ByteArrayInputStream(request.getBytes("ISO-8859-1"));
        }
        req.parse(bis);
        bis.close();
        Map<String, String> pageEncodings = Collections.synchronizedMap(new HashMap<String, String>());
        Map<String, String> formEncodings = Collections.synchronizedMap(new HashMap<String, String>());
        if(url != null && contentEncoding != null) {
            pageEncodings.put(url, contentEncoding);
        }
        SamplerCreatorFactory creatorFactory = new SamplerCreatorFactory();
        SamplerCreator creator = creatorFactory.getSamplerCreator(req, pageEncodings, formEncodings);
        HTTPSamplerBase sampler = creator.createSampler(req, pageEncodings, formEncodings);
        creator.populateSampler(sampler, req, pageEncodings, formEncodings);
        return sampler;
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
        if(contentEncoding != null && contentEncoding.length() > 0) {
            assertEquals(expectedEncodedValue, arg.getEncodedValue(contentEncoding));
        }
        else {
            // Most browsers use ISO-8859-1 as default encoding, even if spec says UTF-8
            assertEquals(expectedEncodedValue, arg.getEncodedValue("ISO-8859-1"));
        }
        assertPrimitiveEquals(expectedEncoded, arg.isAlwaysEncoded());
    }
    
    private int getBodyLength(String postBody, String contentEncoding) throws IOException {
        if(contentEncoding != null && contentEncoding.length() > 0) {
            return postBody.getBytes(contentEncoding).length;            
        }
        else {
            // Most browsers use ISO-8859-1 as default encoding, even if spec says UTF-8
            return postBody.getBytes().length; // TODO - charset?
        }
    }
}
