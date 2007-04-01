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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.protocol.http.control.HttpMirrorControl;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import junit.framework.TestCase;

/**
 * Class for performing actual samples for HTTPSampler and HTTPSampler2.
 * The samples are executed against the HttpMirrorServer, which is 
 * started when the unit tests are executed.
 */
public class TestHTTPSamplersAgainstHttpMirrorServer extends TestCase {
    private final static int HTTP_SAMPLER = 0;
    private final static int HTTP_SAMPLER2 = 1;
    private HttpMirrorControl webServerControl;
    private int webServerPort = 8080;

    public TestHTTPSamplersAgainstHttpMirrorServer(String arg0) {
        super(arg0);
    }
    protected void setUp() throws Exception {
        webServerControl = new HttpMirrorControl();
        webServerControl.setPort(webServerPort);
        webServerControl.startHttpMirror();
    }

    protected void tearDown() throws Exception {
        // Shutdown web server
        webServerControl.stopHttpMirror();
        webServerControl = null;
    }
        
    public void testPostRequest_UrlEncoded() throws Exception {
        // Test HTTPSampler
        String samplerDefaultEncoding = "ISO-8859-1".toLowerCase();
        testPostRequest_UrlEncoded(HTTP_SAMPLER, samplerDefaultEncoding);
        
        // Test HTTPSampler2
        samplerDefaultEncoding = "US-ASCII";
//        testPostRequest_UrlEncoded(HTTP_SAMPLER2, samplerDefaultEncoding);
    }
    
/*    	
//  The whole method is commented out, because sending post request as
//  multipart/form-data is not supported by current svn version of PostWriter
    public void testPostRequest_FormMultipart() throws Exception {
        // Test HTTPSampler
        String samplerDefaultEncoding = "ISO-8859-1".toLowerCase();
        testPostRequest_FormMultipart(HTTP_SAMPLER, samplerDefaultEncoding);
        
        // Test HTTPSampler2
        samplerDefaultEncoding = "US-ASCII";
        testPostRequest_FormMultipart(HTTP_SAMPLER2, samplerDefaultEncoding);
    }
*/    

    private void testPostRequest_UrlEncoded(int samplerType, String samplerDefaultEncoding) throws Exception {
        String titleField = "title";
        String titleValue = "mytitle";
        String descriptionField = "description";
        String descriptionValue = "mydescription";

        // Test sending data with default encoding
        HTTPSamplerBase sampler = createHttpSampler(samplerType);
        String contentEncoding = "";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        HTTPSampleResult res = executeSampler(sampler);
        checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);
        
        // Test sending data as ISO-8859-1
        sampler = createHttpSampler(samplerType);
        contentEncoding = "ISO-8859-1";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        res = executeSampler(sampler);
        checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);

        // Test sending data as UTF-8
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        res = executeSampler(sampler);
        checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);

        // Test sending data as UTF-8, where user defined variables are used
        // to set the value for form data
        JMeterUtils.setLocale(Locale.ENGLISH);
        TestPlan testPlan = new TestPlan();
        JMeterVariables vars = new JMeterVariables();
        vars.put("title_prefix", "a test\u00c5");
        vars.put("description_suffix", "the_end");
        JMeterContextService.getContext().setVariables(vars);
        JMeterContextService.getContext().setSamplingStarted(true);
        ValueReplacer replacer = new ValueReplacer();
        replacer.setUserDefinedVariables(testPlan.getUserDefinedVariables());
        
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "${title_prefix}mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5${description_suffix}";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        // Replace the variables in the sampler
        replacer.replaceValues(sampler);
        res = executeSampler(sampler);
        String expectedTitleValue = "a test\u00c5mytitle\u0153\u20a1\u0115\u00c5";
        String expectedDescriptionValue = "mydescription\u0153\u20a1\u0115\u00c5the_end";
        checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, expectedTitleValue, descriptionField, expectedDescriptionValue);
    }

    private void testPostRequest_FormMultipart(int samplerType, String samplerDefaultEncoding) throws Exception {
        String titleField = "title";
        String titleValue = "mytitle";
        String descriptionField = "description";
        String descriptionValue = "mydescription";

        // Test sending data with default encoding
        HTTPSamplerBase sampler = createHttpSampler(samplerType);
        String contentEncoding = "";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
//        sampler.setDoMultipartPost(true);
        HTTPSampleResult res = executeSampler(sampler);
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);
        
        // Test sending data as ISO-8859-1
        sampler = createHttpSampler(samplerType);
        contentEncoding = "ISO-8859-1";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
//        sampler.setDoMultipartPost(true);
        res = executeSampler(sampler);
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);

        // Test sending data as UTF-8
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
//        sampler.setDoMultipartPost(true);
        res = executeSampler(sampler);
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);
        
        // Test sending data as UTF-8, with values that would have been urlencoded
        // if it was not sent as multipart
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle\u0153+\u20a1 \u0115&yes\u00c5";
        descriptionValue = "mydescription \u0153 \u20a1 \u0115 \u00c5";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
//        sampler.setDoMultipartPost(true);
        res = executeSampler(sampler);
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);

        // Test sending data as UTF-8, where user defined variables are used
        // to set the value for form data
        JMeterUtils.setLocale(Locale.ENGLISH);
        TestPlan testPlan = new TestPlan();
        JMeterVariables vars = new JMeterVariables();
        vars.put("title_prefix", "a test\u00c5");
        vars.put("description_suffix", "the_end");
        JMeterContextService.getContext().setVariables(vars);
        JMeterContextService.getContext().setSamplingStarted(true);
        ValueReplacer replacer = new ValueReplacer();
        replacer.setUserDefinedVariables(testPlan.getUserDefinedVariables());
        
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "${title_prefix}mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5${description_suffix}";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
//        sampler.setDoMultipartPost(true);
        // Replace the variables in the sampler
        replacer.replaceValues(sampler);
        res = executeSampler(sampler);
        String expectedTitleValue = "a test\u00c5mytitle\u0153\u20a1\u0115\u00c5";
        String expectedDescriptionValue = "mydescription\u0153\u20a1\u0115\u00c5the_end";
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, expectedTitleValue, descriptionField, expectedDescriptionValue);
    }
    
    private HTTPSampleResult executeSampler(HTTPSamplerBase sampler) {
        sampler.setRunningVersion(true);
        sampler.threadStarted();
        HTTPSampleResult res = (HTTPSampleResult) sampler.sample();
        sampler.threadFinished();
        sampler.setRunningVersion(false);
        return res;
    }
    
    private void checkPostRequestUrlEncoded(
            HTTPSamplerBase sampler,
            HTTPSampleResult res,
            String samplerDefaultEncoding,
            String contentEncoding,
            String titleField,
            String titleValue,
            String descriptionField,
            String descriptionValue) throws IOException {
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = samplerDefaultEncoding;
        }
        // Check URL
        assertEquals(sampler.getUrl(), res.getURL());
        String expectedPostBody = titleField + "=" + URLEncoder.encode(titleValue, contentEncoding) + "&" + descriptionField + "=" + URLEncoder.encode(descriptionValue, contentEncoding);
        // Check request headers
        assertTrue(isInRequestHeaders(res.getRequestHeaders(), HTTPSamplerBase.HEADER_CONTENT_TYPE, HTTPSamplerBase.APPLICATION_X_WWW_FORM_URLENCODED));
        assertTrue(
                isInRequestHeaders(
                        res.getRequestHeaders(),
                        HTTPSamplerBase.HEADER_CONTENT_LENGTH,
                        Integer.toString(expectedPostBody.getBytes(contentEncoding).length)
                )
        );
        // Check post body from the result query string
        checkArraysHaveSameContent(expectedPostBody.getBytes(contentEncoding), res.getQueryString().getBytes(contentEncoding));

        // Find the data sent to the mirror server, which the mirror server is sending back to us
        String dataSentToMirrorServer = new String(res.getResponseData(), contentEncoding);
        int posDividerHeadersAndBody = getPositionOfBody(dataSentToMirrorServer);
        String headersSent = null;
        String bodySent = null;
        if(posDividerHeadersAndBody >= 0) {
            headersSent = dataSentToMirrorServer.substring(0, posDividerHeadersAndBody);
            // Skip the blank line with crlf dividing headers and body
            bodySent = dataSentToMirrorServer.substring(posDividerHeadersAndBody+2);
        }
        // Check response headers
        assertTrue(isInRequestHeaders(headersSent, HTTPSamplerBase.HEADER_CONTENT_TYPE, HTTPSamplerBase.APPLICATION_X_WWW_FORM_URLENCODED));
        assertTrue(
                isInRequestHeaders(
                        headersSent,
                        HTTPSamplerBase.HEADER_CONTENT_LENGTH,
                        Integer.toString(expectedPostBody.getBytes(contentEncoding).length)
                )
        );
        // Check post body which was sent to the mirror server, and
        // sent back by the mirror server
        checkArraysHaveSameContent(expectedPostBody.getBytes(contentEncoding), bodySent.getBytes(contentEncoding));
    }

    private void checkPostRequestFormMultipart(
            HTTPSamplerBase sampler,
            HTTPSampleResult res,
            String samplerDefaultEncoding,
            String contentEncoding,
            String titleField,
            String titleValue,
            String descriptionField,
            String descriptionValue) throws IOException {
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = samplerDefaultEncoding;
        }
        // Check URL
        assertEquals(sampler.getUrl(), res.getURL());
        String boundaryString = getBoundaryStringFromContentType(res.getRequestHeaders());
        assertNotNull(boundaryString);
        byte[] expectedPostBody = createExpectedFormdataOutput(boundaryString, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, true, true);
        // Check request headers
        assertTrue(isInRequestHeaders(res.getRequestHeaders(), HTTPSamplerBase.HEADER_CONTENT_TYPE, "multipart/form-data" + "; boundary=" + boundaryString));
        assertTrue(
                isInRequestHeaders(
                        res.getRequestHeaders(),
                        HTTPSamplerBase.HEADER_CONTENT_LENGTH,
                        Integer.toString(expectedPostBody.length)
                )
        );
        // Check post body from the result query string
        checkArraysHaveSameContent(expectedPostBody, res.getQueryString().getBytes(contentEncoding));

        // Find the data sent to the mirror server, which the mirror server is sending back to us
        String dataSentToMirrorServer = new String(res.getResponseData(), contentEncoding);
        int posDividerHeadersAndBody = getPositionOfBody(dataSentToMirrorServer);
        String headersSent = null;
        String bodySent = null;
        if(posDividerHeadersAndBody >= 0) {
            headersSent = dataSentToMirrorServer.substring(0, posDividerHeadersAndBody);
            // Skip the blank line with crlf dividing headers and body
            bodySent = dataSentToMirrorServer.substring(posDividerHeadersAndBody+2);
        }
        // Check response headers
        assertTrue(isInRequestHeaders(headersSent, HTTPSamplerBase.HEADER_CONTENT_TYPE, "multipart/form-data" + "; boundary=" + boundaryString));
        assertTrue(
                isInRequestHeaders(
                        headersSent,
                        HTTPSamplerBase.HEADER_CONTENT_LENGTH,
                        Integer.toString(expectedPostBody.length)
                )
        );
        // Check post body which was sent to the mirror server, and
        // sent back by the mirror server
        checkArraysHaveSameContent(expectedPostBody, bodySent.getBytes(contentEncoding));
    }    

    private boolean isInRequestHeaders(String requestHeaders, String headerName, String headerValue) {
        return checkRegularExpression(requestHeaders, headerName + ": " + headerValue);
    }

    private boolean checkRegularExpression(String stringToCheck, String regularExpression) {
        Perl5Matcher localMatcher = JMeterUtils.getMatcher();
        Pattern pattern = JMeterUtils.getPattern(regularExpression, Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.SINGLELINE_MASK);
        return localMatcher.contains(stringToCheck, pattern);
    }

    private int getPositionOfBody(String stringToCheck) {
        Perl5Matcher localMatcher = JMeterUtils.getMatcher();
        // The headers and body are divided by a blank line
        String regularExpression = "^.$"; 
        Pattern pattern = JMeterUtils.getPattern(regularExpression, Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.MULTILINE_MASK);
        
        PatternMatcherInput input = new PatternMatcherInput(stringToCheck);
        while(localMatcher.contains(input, pattern)) {
            MatchResult match = localMatcher.getMatch();
            return match.beginOffset(0);
        }
        // No divider was found
        return -1;
    }

    private String getBoundaryStringFromContentType(String requestHeaders) {
        Perl5Matcher localMatcher = JMeterUtils.getMatcher();
        String regularExpression = "^" + HTTPSamplerBase.HEADER_CONTENT_TYPE + ": multipart/form-data; boundary=(.+)$"; 
        Pattern pattern = JMeterUtils.getPattern(regularExpression, Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.MULTILINE_MASK);
        if(localMatcher.contains(requestHeaders, pattern)) {
            MatchResult match = localMatcher.getMatch();
            return match.group(1);
        }
        else {
            return null;
        }
    }

    private void setupUrl(HTTPSamplerBase sampler, String contentEncoding) {
        String protocol = "http";
        // String domain = "localhost";
        String domain = "localhost";
        String path = "/test/somescript.jsp";
        int port = 8080;
        sampler.setProtocol(protocol);
        sampler.setMethod(HTTPSamplerBase.POST);
        sampler.setPath(path);
        sampler.setDomain(domain);
        sampler.setPort(port);
        sampler.setContentEncoding(contentEncoding);
    }

    /**
     * Setup the form data with specified values
     * 
     * @param httpSampler
     */
    private void setupFormData(HTTPSamplerBase httpSampler, boolean isEncoded, String titleField, String titleValue, String descriptionField, String descriptionValue) {
        Arguments args = new Arguments();
        HTTPArgument argument1 = new HTTPArgument(titleField, titleValue, isEncoded);
        HTTPArgument argument2 = new HTTPArgument(descriptionField, descriptionValue, isEncoded);
        args.addArgument(argument1);
        args.addArgument(argument2);
        httpSampler.setArguments(args);
    }

    /**
     * Check that the the two byte arrays have identical content
     * 
     * @param expected
     * @param actual
     */
    private void checkArraysHaveSameContent(byte[] expected, byte[] actual) {
        if(expected != null && actual != null) {
            if(expected.length != actual.length) {
                fail("arrays have different length, expected is " + expected.length + ", actual is " + actual.length);
            }
            else {
                for(int i = 0; i < expected.length; i++) {
                    if(expected[i] != actual[i]) {
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
     * Create the expected output multipart/form-data, with only form data,
     * and no file multipart.
     * This method is copied from the PostWriterTest class
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
        // The encoding used for http headers and control information
        final String httpEncoding = "ISO-8859-1";
        final byte[] CRLF = { 0x0d, 0x0A };
        final byte[] DASH_DASH = new String("--").getBytes(httpEncoding);
        
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        if(firstMultipart) {
            output.write(DASH_DASH);
            output.write(boundaryString.getBytes(httpEncoding));
            output.write(CRLF);
        }
        output.write("Content-Disposition: form-data; name=\"".getBytes(httpEncoding));
        output.write(titleField.getBytes(httpEncoding));
        output.write("\"".getBytes(httpEncoding));
        output.write(CRLF);
        output.write("Content-Type: text/plain".getBytes(httpEncoding));
        if(contentEncoding != null) {
            output.write("; charset=".getBytes(httpEncoding));
            output.write(contentEncoding.getBytes(httpEncoding));
        }
        output.write(CRLF);
        output.write("Content-Transfer-Encoding: 8bit".getBytes(httpEncoding));
        output.write(CRLF);
        output.write(CRLF);
        if(contentEncoding != null) {
            output.write(titleValue.getBytes(contentEncoding));
        }
        else {
            output.write(titleValue.getBytes());
        }
        output.write(CRLF);
        output.write(DASH_DASH);
        output.write(boundaryString.getBytes(httpEncoding));
        output.write(CRLF);
        output.write("Content-Disposition: form-data; name=\"".getBytes(httpEncoding));
        output.write(descriptionField.getBytes(httpEncoding));
        output.write("\"".getBytes(httpEncoding));
        output.write(CRLF);
        output.write("Content-Type: text/plain".getBytes(httpEncoding));
        if(contentEncoding != null) {
            output.write("; charset=".getBytes(httpEncoding));
            output.write(contentEncoding.getBytes(httpEncoding));
        }
        output.write(CRLF);
        output.write("Content-Transfer-Encoding: 8bit".getBytes(httpEncoding));
        output.write(CRLF);
        output.write(CRLF);
        if(contentEncoding != null) {
            output.write(descriptionValue.getBytes(contentEncoding));
        }
        else {
            output.write(descriptionValue.getBytes());
        }
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
    
    private HTTPSamplerBase createHttpSampler(int samplerType) {
        if(samplerType == HTTP_SAMPLER2) {
            return new HTTPSampler2();
        }
        else {
            return new HTTPSampler();
        }
    }
}
