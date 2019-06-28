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

package org.apache.jmeter.protocol.http.sampler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jorphan.util.JOrphanUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostWriterTest {

    private static final Logger log = LoggerFactory.getLogger(PostWriterTest.class);

    private static final String UTF_8 = "UTF-8";
    private static final String HTTP_ENCODING = "ISO-8859-1";
    private static final byte[] CRLF = { 0x0d, 0x0A };
    private static byte[] TEST_FILE_CONTENT;

    private StubURLConnection connection;
    private HTTPSampler sampler;
    private File temporaryFile;

    private PostWriter postWriter;

    @Before
    public void setUp() throws Exception {
        establishConnection();
        sampler = new HTTPSampler();// This must be the original (Java) HTTP sampler
        postWriter=new PostWriter();

        // Create the test file content
        TEST_FILE_CONTENT = "foo content &?=01234+56789-\u007c\u2aa1\u266a\u0153\u20a1\u0115\u0364\u00c5\u2052".getBytes(UTF_8);

        // create a temporary file to make sure we always have a file to give to the PostWriter
        // Whereever we are or Whatever the current path is.
        temporaryFile = File.createTempFile("foo", "txt");
        OutputStream output = null;
        try {
            output = new FileOutputStream(temporaryFile);
            output.write(TEST_FILE_CONTENT);
            output.flush();
        } finally {
            JOrphanUtils.closeQuietly(output);
        }
    }

    @After
    public void tearDown() throws Exception {
        // delete temporay file
        if(!temporaryFile.delete()) {
            fail("Could not delete file:"+temporaryFile.getAbsolutePath());
        }
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.postWriter.sendPostData(URLConnection, HTTPSampler)'
     * This method test sending a request which contains both formdata and file content
     */
    @Test
    public void testSendPostData() throws IOException {
        sampler.setMethod(HTTPConstants.POST);
        setupFilepart(sampler);
        String titleValue = "mytitle";
        String descriptionValue = "mydescription";
        setupFormData(sampler, titleValue, descriptionValue);

        // Test sending data with default encoding
        String contentEncoding = "";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        byte[] expectedFormBody = createExpectedOutput(PostWriter.BOUNDARY, null, titleValue, descriptionValue, TEST_FILE_CONTENT);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as ISO-8859-1
        establishConnection();
        contentEncoding = "ISO-8859-1";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        expectedFormBody = createExpectedOutput(PostWriter.BOUNDARY, contentEncoding, titleValue, descriptionValue, TEST_FILE_CONTENT);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as UTF-8
        establishConnection();
        titleValue = "mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5";
        contentEncoding = UTF_8;
        sampler.setContentEncoding(contentEncoding);
        setupFormData(sampler, titleValue, descriptionValue);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);
        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        expectedFormBody = createExpectedOutput(PostWriter.BOUNDARY, contentEncoding, titleValue, descriptionValue, TEST_FILE_CONTENT);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending UTF-8 data with ISO-8859-1 content encoding
        establishConnection();
        contentEncoding = UTF_8;
        sampler.setContentEncoding("ISO-8859-1");
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);
        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        expectedFormBody = createExpectedOutput(PostWriter.BOUNDARY, contentEncoding, titleValue, descriptionValue, TEST_FILE_CONTENT);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveDifferentContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.postWriter.sendPostData(URLConnection, HTTPSampler)'
     * This method test sending a HTTPSampler with form parameters, and only
     * the filename of a file.
     */
    @Test
    public void testSendPostData_NoFilename() throws IOException {
        setupNoFilename(sampler);
        sampler.setMethod(HTTPConstants.POST);
        String titleValue = "mytitle";
        String descriptionValue = "mydescription";
        setupFormData(sampler, titleValue, descriptionValue);

        // Test sending data with default encoding
        String contentEncoding = "";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        byte[] expectedUrl = "title=mytitle&description=mydescription".getBytes(); // TODO - charset?
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        expectedUrl = "title=mytitle&description=mydescription".getBytes(UTF_8);
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as ISO-8859-1
        establishConnection();
        contentEncoding = "ISO-8859-1";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        expectedUrl = "title=mytitle&description=mydescription".getBytes(contentEncoding);
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        expectedUrl = "title=mytitle&description=mydescription".getBytes(UTF_8);
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        connection.disconnect();
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.postWriter.sendPostData(URLConnection, HTTPSampler)'
     * This method test sending file content as the only content of the post body
     */
    @Test
    public void testSendPostData_FileAsBody() throws IOException {
        sampler.setMethod(HTTPConstants.POST);
        setupFilepart(sampler, "", temporaryFile, "");

        // Check using default encoding
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentLength(connection, TEST_FILE_CONTENT.length);
        checkArraysHaveSameContent(TEST_FILE_CONTENT, connection.getOutputStreamContent());
        connection.disconnect();

        // Check using a different encoding

        String otherEncoding;
        final String fileEncoding = System.getProperty( "file.encoding");// $NON-NLS-1$
        log.info("file.encoding: {}", fileEncoding);
        if (UTF_8.equalsIgnoreCase(fileEncoding) || "UTF8".equalsIgnoreCase(fileEncoding)){// $NON-NLS-1$
            otherEncoding="ISO-8859-1"; // $NON-NLS-1$
        } else {
            otherEncoding=UTF_8;
        }
        log.info("Using other encoding: {}", otherEncoding);
        establishConnection();
        sampler.setContentEncoding(otherEncoding);
        // File content is sent as binary, so the content encoding should not change the file data
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentLength(connection, TEST_FILE_CONTENT.length);
        checkArraysHaveSameContent(TEST_FILE_CONTENT, connection.getOutputStreamContent());
        // Check that other encoding is not the current encoding
        checkArraysHaveDifferentContent(new String(TEST_FILE_CONTENT) // TODO - charset?
            .getBytes(otherEncoding), connection.getOutputStreamContent());

        // If we have both file as body, and form data, then only form data will be sent
        setupFormData(sampler);
        establishConnection();
        sampler.setContentEncoding("");
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        byte[] expectedUrl = "title=mytitle&description=mydescription".getBytes(); // TODO - charset?
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.postWriter.sendPostData(URLConnection, HTTPSampler)'
     * This method test sending only a file multipart.
     */
    @Test
    public void testSendFileData_Multipart() throws IOException {
        sampler.setMethod(HTTPConstants.POST);
        String fileField = "upload";
        String mimeType = "text/plain";
        File file = temporaryFile;
        byte[] fileContent = TEST_FILE_CONTENT;
        setupFilepart(sampler, fileField, file, mimeType);

        // Test sending data with default encoding
        String contentEncoding = "";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        byte[] expectedFormBody = createExpectedFilepartOutput(PostWriter.BOUNDARY, fileField, file, mimeType, fileContent, true, true);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as ISO-8859-1
        establishConnection();
        contentEncoding = "ISO-8859-1";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        expectedFormBody = createExpectedFilepartOutput(PostWriter.BOUNDARY, fileField, file, mimeType, fileContent, true, true);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as UTF-8
        establishConnection();
        fileField = "some_file_field";
        mimeType = "image/png";
        contentEncoding = UTF_8;
        sampler.setContentEncoding(contentEncoding);
        setupFilepart(sampler, fileField, file, mimeType);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        expectedFormBody = createExpectedFilepartOutput(PostWriter.BOUNDARY, fileField, file, mimeType, fileContent, true, true);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.postWriter.sendPostData(URLConnection, HTTPSampler)'
     * This method test sending only a formdata, as a multipart/form-data request.
     */
    @Test
    public void testSendFormData_Multipart() throws IOException {
        sampler.setMethod(HTTPConstants.POST);
        String titleField = "title";
        String titleValue = "mytitle";
        String descriptionField = "description";
        String descriptionValue = "mydescription";
        setupFormData(sampler, titleValue, descriptionValue);
        // Tell sampler to do multipart, even if we have no files to upload
        sampler.setDoMultipart(true);

        // Test sending data with default encoding
        String contentEncoding = "";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        byte[] expectedFormBody = createExpectedFormdataOutput(
                PostWriter.BOUNDARY, null, titleField, titleValue,
                descriptionField, descriptionValue, true, true);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as ISO-8859-1
        establishConnection();
        contentEncoding = "ISO-8859-1";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        expectedFormBody = createExpectedFormdataOutput(PostWriter.BOUNDARY,
                contentEncoding, titleField, titleValue, descriptionField,
                descriptionValue, true, true);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as ISO-8859-1, with values that need to be urlencoded
        establishConnection();
        titleValue = "mytitle+123 456&yes";
        descriptionValue = "mydescription and some spaces";
        contentEncoding = "ISO-8859-1";
        sampler.setContentEncoding(contentEncoding);
        setupFormData(sampler, titleValue, descriptionValue);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        expectedFormBody = createExpectedFormdataOutput(PostWriter.BOUNDARY,
                contentEncoding, titleField, titleValue, descriptionField,
                descriptionValue, true, true);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as UTF-8
        establishConnection();
        titleValue = "mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5";
        contentEncoding = UTF_8;
        sampler.setContentEncoding(contentEncoding);
        setupFormData(sampler, titleValue, descriptionValue);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        expectedFormBody = createExpectedFormdataOutput(PostWriter.BOUNDARY,
                contentEncoding, titleField, titleValue, descriptionField,
                descriptionValue, true, true);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as UTF-8, with values that would have been urlencoded
        // if it was not sent as multipart
        establishConnection();
        titleValue = "mytitle\u0153+\u20a1 \u0115&yes\u00c5";
        descriptionValue = "mydescription \u0153 \u20a1 \u0115 \u00c5";
        contentEncoding = UTF_8;
        sampler.setContentEncoding(contentEncoding);
        setupFormData(sampler, titleValue, descriptionValue);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
        expectedFormBody = createExpectedFormdataOutput(PostWriter.BOUNDARY,
                contentEncoding, titleField, titleValue, descriptionField,
                descriptionValue, true, true);
        checkContentLength(connection, expectedFormBody.length);
        checkArraysHaveSameContent(expectedFormBody, connection.getOutputStreamContent());
        connection.disconnect();
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.postWriter.sendPostData(URLConnection, HTTPSampler)'
     * This method test sending only a formdata, as urlencoded data
     */
    @Test
    public void testSendFormData_Urlencoded() throws IOException {
        String titleValue = "mytitle";
        String descriptionValue = "mydescription";
        setupFormData(sampler, titleValue, descriptionValue);

        // Test sending data with default encoding
        String contentEncoding = "";
        sampler.setContentEncoding(contentEncoding);
        sampler.setMethod(HTTPConstants.POST);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        byte[] expectedUrl = ("title=" + titleValue + "&description=" + descriptionValue).getBytes("US-ASCII");
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        assertEquals(
                URLDecoder.decode(new String(expectedUrl, "US-ASCII"), "ISO-8859-1"),
                URLDecoder.decode(new String(connection.getOutputStreamContent(), "US-ASCII"), "ISO-8859-1"));
        connection.disconnect();

        // Test sending data as ISO-8859-1
        establishConnection();
        contentEncoding = "ISO-8859-1";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        expectedUrl = new StringBuilder("title=").append(titleValue).append("&description=")
                .append(descriptionValue).toString().getBytes("US-ASCII");
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        assertEquals(
                URLDecoder.decode(new String(expectedUrl, "US-ASCII"), contentEncoding),
                URLDecoder.decode(new String(connection.getOutputStreamContent(), "US-ASCII"), contentEncoding));
        connection.disconnect();

        // Test sending data as ISO-8859-1, with values that need to be urlencoded
        establishConnection();
        titleValue = "mytitle+123 456&yes";
        descriptionValue = "mydescription and some spaces";
        contentEncoding = "ISO-8859-1";
        sampler.setContentEncoding(contentEncoding);
        setupFormData(sampler, titleValue, descriptionValue);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        String expectedString = "title="
                + URLEncoder.encode(titleValue, contentEncoding)
                + "&description="
                + URLEncoder.encode(descriptionValue, contentEncoding);
        expectedUrl = expectedString.getBytes(contentEncoding);
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        assertEquals(
                URLDecoder.decode(new String(expectedUrl, "US-ASCII"), contentEncoding),
                URLDecoder.decode(new String(connection.getOutputStreamContent(), "US-ASCII"), contentEncoding));
        String unencodedString = "title=" + titleValue + "&description=" + descriptionValue;
        byte[] unexpectedUrl = unencodedString.getBytes(UTF_8);
        checkArraysHaveDifferentContent(unexpectedUrl, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending data as UTF-8
        establishConnection();
        titleValue = "mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5";
        contentEncoding = UTF_8;
        sampler.setContentEncoding(contentEncoding);
        setupFormData(sampler, titleValue, descriptionValue);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        expectedString = "title="
                + URLEncoder.encode(titleValue, contentEncoding)
                + "&description="
                + URLEncoder.encode(descriptionValue, contentEncoding);
        expectedUrl = expectedString.getBytes("US-ASCII");
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        assertEquals(
                URLDecoder.decode(new String(expectedUrl, "US-ASCII"), contentEncoding),
                URLDecoder.decode(new String(connection.getOutputStreamContent(), "US-ASCII"), contentEncoding));
        connection.disconnect();

        // Test sending data as UTF-8, with values that needs to be urlencoded
        establishConnection();
        titleValue = "mytitle\u0153+\u20a1 \u0115&yes\u00c5";
        descriptionValue = "mydescription \u0153 \u20a1 \u0115 \u00c5";
        contentEncoding = UTF_8;
        sampler.setContentEncoding(contentEncoding);
        setupFormData(sampler, titleValue, descriptionValue);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        expectedString = "title=" + URLEncoder.encode(titleValue, UTF_8) + "&description=" + URLEncoder.encode(descriptionValue, UTF_8);
        expectedUrl = expectedString.getBytes("US-ASCII");
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        assertEquals(
                URLDecoder.decode(new String(expectedUrl, "US-ASCII"), contentEncoding),
                URLDecoder.decode(new String(connection.getOutputStreamContent(), "US-ASCII"), contentEncoding));
        unencodedString = "title=" + titleValue + "&description=" + descriptionValue;
        unexpectedUrl = unencodedString.getBytes("US-ASCII");
        checkArraysHaveDifferentContent(unexpectedUrl, connection.getOutputStreamContent());
        connection.disconnect();

        // Test sending parameters which are urlencoded beforehand
        // The values must be URL encoded with UTF-8 encoding, because that
        // is what the HTTPArgument assumes
        // %C3%85 in UTF-8 is the same as %C5 in ISO-8859-1, which is the same as &Aring;
        titleValue = "mytitle%20and%20space%2Ftest%C3%85";
        descriptionValue = "mydescription+and+plus+as+space%2Ftest%C3%85";
        setupFormData(sampler, true, titleValue, descriptionValue);

        // Test sending data with default encoding
        establishConnection();
        contentEncoding = "";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        StringBuilder sb = new StringBuilder();
        expectedUrl = sb.append("title=").append(titleValue.replaceAll("%20", "+").replaceAll("%C3%85", "%C5"))
                .append("&description=").append(descriptionValue.replaceAll("%C3%85", "%C5")).toString().getBytes("US-ASCII");
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        assertEquals(
                // HTTPSampler uses ISO-8859-1 as default encoding
                URLDecoder.decode(new String(expectedUrl, "US-ASCII"), "ISO-8859-1"),
                URLDecoder.decode(new String(connection.getOutputStreamContent(), "US-ASCII"), "ISO-8859-1"));
        connection.disconnect();

        // Test sending data as ISO-8859-1
        establishConnection();
        contentEncoding = "ISO-8859-1";
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        sb = new StringBuilder();
        expectedUrl = sb.append("title=").append(titleValue.replaceAll("%20", "+").replaceAll("%C3%85", "%C5"))
                .append("&description=").append(descriptionValue.replaceAll("%C3%85", "%C5")).toString().getBytes("US-ASCII");
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        assertEquals(
                URLDecoder.decode(new String(expectedUrl, "US-ASCII"), contentEncoding),
                URLDecoder.decode(new String(connection.getOutputStreamContent(), "US-ASCII"), contentEncoding));
        connection.disconnect();

        // Test sending data as UTF-8
        establishConnection();
        contentEncoding = UTF_8;
        sampler.setContentEncoding(contentEncoding);
        postWriter.setHeaders(connection, sampler);
        postWriter.sendPostData(connection, sampler);

        checkNoContentType(connection);
        sb = new StringBuilder();
        expectedUrl = sb.append("title=")
                .append(titleValue.replaceAll("%20", "+"))
                .append("&description=").append(descriptionValue).toString()
                .getBytes("US-ASCII");
        checkContentLength(connection, expectedUrl.length);
        checkArraysHaveSameContent(expectedUrl, connection.getOutputStreamContent());
        assertEquals(
                URLDecoder.decode(new String(expectedUrl, "US-ASCII"), contentEncoding),
                URLDecoder.decode(new String(connection.getOutputStreamContent(), "US-ASCII"), contentEncoding));
        connection.disconnect();
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.postWriter.setHeaders(URLConnection, HTTPSampler)'
     */
    @Test
    public void testSetHeaders() throws IOException {
        sampler.setMethod(HTTPConstants.POST);
        setupFilepart(sampler);
        setupFormData(sampler);

        postWriter.setHeaders(connection, sampler);
        checkContentTypeMultipart(connection, PostWriter.BOUNDARY);
    }

    /*
     * Test method for 'org.apache.jmeter.protocol.http.sampler.postWriter.setHeaders(URLConnection, HTTPSampler)'
     */
    @Test
    public void testSetHeaders_NoFilename() throws IOException {
        sampler.setMethod(HTTPConstants.POST);
        setupNoFilename(sampler);
        setupFormData(sampler);

        postWriter.setHeaders(connection, sampler);
        checkNoContentType(connection);
        checkContentLength(connection, "title=mytitle&description=mydescription".length());
    }

    /**
     * setup commons parts of HTTPSampler with a no filename.
     *
     * @param httpSampler
     */
    private void setupNoFilename(HTTPSampler httpSampler) {
        setupFilepart(sampler, "upload", null, "application/octet-stream");
    }

    /**
     * Setup the filepart with default values
     *
     * @param httpSampler
     */
    private void setupFilepart(HTTPSampler httpSampler) {
        setupFilepart(sampler, "upload", temporaryFile, "text/plain");
    }

    /**
     * Setup the filepart with specified values
     *
     * @param httpSampler
     */
    private void setupFilepart(HTTPSampler httpSampler, String fileField, File file, String mimeType) {
        HTTPFileArg[] hfa = {new HTTPFileArg(file == null ? "" : file.getAbsolutePath(), fileField, mimeType)};
        httpSampler.setHTTPFiles(hfa);
    }

    /**
     * Setup the form data with default values
     *
     * @param httpSampler
     */
    private void setupFormData(HTTPSampler httpSampler) {
        setupFormData(httpSampler, "mytitle", "mydescription");
    }

    /**
     * Setup the form data with specified values
     *
     * @param httpSampler
     */
    private void setupFormData(HTTPSampler httpSampler, String titleValue, String descriptionValue) {
        setupFormData(sampler, false, titleValue, descriptionValue);
    }

    /**
     * Setup the form data with specified values
     *
     * @param httpSampler
     */
    private void setupFormData(HTTPSampler httpSampler, boolean isEncoded, String titleValue, String descriptionValue) {
        Arguments args = new Arguments();
        HTTPArgument argument1 = new HTTPArgument("title", titleValue, isEncoded);
        HTTPArgument argument2 = new HTTPArgument("description", descriptionValue, isEncoded);
        args.addArgument(argument1);
        args.addArgument(argument2);
        httpSampler.setArguments(args);
    }

    private void establishConnection() throws MalformedURLException {
        connection = new StubURLConnection("http://fake_url/test");
    }

    /**
     * Create the expected output post body for form data and file multiparts
     * with default values for field names
     */
    private byte[] createExpectedOutput(
            String boundaryString,
            String contentEncoding,
            String titleValue,
            String descriptionValue,
            byte[] fileContent) throws IOException {
        return createExpectedOutput(boundaryString, contentEncoding, "title",
                titleValue, "description", descriptionValue, "upload",
                fileContent);
    }

    /**
     * Create the expected output post body for form data and file multiparts
     * with specified values
     */
    private byte[] createExpectedOutput(
            String boundaryString,
            String contentEncoding,
            String titleField,
            String titleValue,
            String descriptionField,
            String descriptionValue,
            String fileField,
            byte[] fileContent) throws IOException {
        // Create the multiparts
        byte[] formdataMultipart = createExpectedFormdataOutput(boundaryString,
                contentEncoding, titleField, titleValue, descriptionField,
                descriptionValue, true, false);
        byte[] fileMultipart = createExpectedFilepartOutput(boundaryString,
                fileField, temporaryFile, "text/plain", fileContent, false,
                true);

        // Join the two multiparts
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(formdataMultipart);
        output.write(fileMultipart);

        output.flush();
        output.close();

        return output.toByteArray();
    }

    /**
     * Create the expected output multipart/form-data, with only form data,
     * and no file multipart
     *
     * @param lastMultipart true if this is the last multipart in the request
     */
    private byte[] createExpectedFormdataOutput(
            String boundaryString,
            String contentEncoding,
            String titleField,
            String titleValue,
            String descriptionField,
            String descriptionValue,
            boolean firstMultipart,
            boolean lastMultipart) throws IOException {
        final byte[] DASH_DASH = "--".getBytes(HTTP_ENCODING);
        // All form parameter always have text/plain as mime type
        final String mimeType="text/plain";//TODO make this a parameter?

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        if(firstMultipart) {
            output.write(DASH_DASH);
            output.write(boundaryString.getBytes(HTTP_ENCODING));
            output.write(CRLF);
        }
        output.write("Content-Disposition: form-data; name=\"".getBytes(HTTP_ENCODING));
        output.write(titleField.getBytes(HTTP_ENCODING));
        output.write("\"".getBytes(HTTP_ENCODING));
        output.write(CRLF);
        output.write("Content-Type: ".getBytes(HTTP_ENCODING));
        output.write(mimeType.getBytes(HTTP_ENCODING));
        output.write("; charset=".getBytes(HTTP_ENCODING));
        output.write((contentEncoding==null ? PostWriter.ENCODING : contentEncoding).getBytes(HTTP_ENCODING));
        output.write(CRLF);
        output.write("Content-Transfer-Encoding: 8bit".getBytes(HTTP_ENCODING));
        output.write(CRLF);
        output.write(CRLF);
        if(contentEncoding != null) {
            output.write(titleValue.getBytes(contentEncoding));
        }
        else {
            output.write(titleValue.getBytes()); // TODO - charset?
        }
        output.write(CRLF);
        output.write(DASH_DASH);
        output.write(boundaryString.getBytes(HTTP_ENCODING));
        output.write(CRLF);
        output.write("Content-Disposition: form-data; name=\"".getBytes(HTTP_ENCODING));
        output.write(descriptionField.getBytes(HTTP_ENCODING));
        output.write("\"".getBytes(HTTP_ENCODING));
        output.write(CRLF);
        output.write("Content-Type: ".getBytes(HTTP_ENCODING));
        output.write(mimeType.getBytes(HTTP_ENCODING));
        output.write("; charset=".getBytes(HTTP_ENCODING));
        output.write((contentEncoding==null ? PostWriter.ENCODING : contentEncoding).getBytes(HTTP_ENCODING));
        output.write(CRLF);
        output.write("Content-Transfer-Encoding: 8bit".getBytes(HTTP_ENCODING));
        output.write(CRLF);
        output.write(CRLF);
        if(contentEncoding != null) {
            output.write(descriptionValue.getBytes(contentEncoding));
        }
        else {
            output.write(descriptionValue.getBytes()); // TODO - charset?
        }
        output.write(CRLF);
        output.write(DASH_DASH);
        output.write(boundaryString.getBytes(HTTP_ENCODING));
        if(lastMultipart) {
            output.write(DASH_DASH);
        }
        output.write(CRLF);

        output.flush();
        output.close();

        return output.toByteArray();
    }

    /**
     * Create the expected file multipart
     *
     * @param lastMultipart true if this is the last multipart in the request
     */
    private byte[] createExpectedFilepartOutput(
            String boundaryString,
            String fileField,
            File file,
            String mimeType,
            byte[] fileContent,
            boolean firstMultipart,
            boolean lastMultipart) throws IOException {
        // The encoding used for http headers and control information
        final String httpEncoding = "ISO-8859-1";
        final byte[] DASH_DASH = "--".getBytes(httpEncoding);

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        if(firstMultipart) {
            output.write(DASH_DASH);
            output.write(boundaryString.getBytes(httpEncoding));
            output.write(CRLF);
        }
        // replace all backslash with double backslash
        String filename = file.getName();
        output.write("Content-Disposition: form-data; name=\"".getBytes(httpEncoding));
        output.write(fileField.getBytes(httpEncoding));
        output.write(("\"; filename=\"" + filename + "\"").getBytes(httpEncoding));
        output.write(CRLF);
        output.write("Content-Type: ".getBytes(httpEncoding));
        output.write(mimeType.getBytes(httpEncoding));
        output.write(CRLF);
        output.write("Content-Transfer-Encoding: binary".getBytes(httpEncoding));
        output.write(CRLF);
        output.write(CRLF);
        output.write(fileContent);
        output.write(CRLF);
        output.write(DASH_DASH);
        output.write(boundaryString.getBytes(httpEncoding));
        if(lastMultipart) {
            output.write(DASH_DASH);
        }
        output.write(CRLF);

        output.flush();
        output.close();

        return output.toByteArray();
    }

    /**
     * Check that the two byte arrays have identical content
     *
     * @param expected
     * @param actual
     * @throws UnsupportedEncodingException
     */
    private void checkArraysHaveSameContent(byte[] expected, byte[] actual) throws UnsupportedEncodingException {
        if(expected != null && actual != null) {
            if(expected.length != actual.length) {
                System.out.println(new String(expected,UTF_8));
                System.out.println("--------------------");
                System.out.println(new String(actual,UTF_8));
                System.out.println("====================");
                fail("arrays have different length, expected is " + expected.length + ", actual is " + actual.length);
            }
            else {
                for(int i = 0; i < expected.length; i++) {
                    if(expected[i] != actual[i]) {
                        System.out.println(new String(expected,0,i+1, UTF_8));
                        System.out.println("--------------------");
                        System.out.println(new String(actual,0,i+1, UTF_8));
                        System.out.println("====================");
                        fail("byte at position " + i + " is different, expected is " + expected[i] + ", actual is " + actual[i]);
                    }
                }
            }
        }
        else {
            fail("expected or actual byte arrays were null");
        }
    }

    /**
     * Check that the two byte arrays different content
     *
     * @param expected
     * @param actual
     */
    private void checkArraysHaveDifferentContent(byte[] expected, byte[] actual) {
        if(expected != null && actual != null) {
            if(expected.length == actual.length) {
                boolean allSame = true;
                for(int i = 0; i < expected.length; i++) {
                    if(expected[i] != actual[i]) {
                        allSame = false;
                        break;
                    }
                }
                if(allSame) {
                    fail("all bytes were equal");
                }
            }
        }
        else {
            fail("expected or actual byte arrays were null");
        }
    }

    private void checkContentTypeMultipart(HttpURLConnection conn, String boundaryString) {
        assertEquals("multipart/form-data; boundary=" + boundaryString, conn.getRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE));
    }

    private void checkNoContentType(HttpURLConnection conn) {
        assertNull(conn.getRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE));
    }
    private void checkContentTypeUrlEncoded(HttpURLConnection conn) {
        assertEquals(HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED, conn.getRequestProperty(HTTPConstants.HEADER_CONTENT_TYPE));
    }

    private void checkContentLength(HttpURLConnection conn, int length) {
        assertEquals(Integer.toString(length), conn.getRequestProperty(HTTPConstants.HEADER_CONTENT_LENGTH));
    }

    /**
     * Mock an HttpURLConnection.
     * extends HttpURLConnection instead of just URLConnection because there is a cast in PostWriter.
     */
    private static class StubURLConnection extends HttpURLConnection {
        private ByteArrayOutputStream output = new ByteArrayOutputStream();
        private Map<String, String> properties = new HashMap<>();

        public StubURLConnection(String url) throws MalformedURLException {
            super(new URL(url));
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return output;
        }

        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public String getRequestProperty(String key) {
            return properties.get(key);
        }

        @Override
        public void setRequestProperty(String key, String value) {
            properties.put(key, value);
        }

        public byte[] getOutputStreamContent() {
            return output.toByteArray();
        }
    }
}
