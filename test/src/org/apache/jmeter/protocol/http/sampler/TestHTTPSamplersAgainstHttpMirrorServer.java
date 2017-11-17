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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.junit.JMeterTestCaseJUnit;
import org.apache.jmeter.protocol.http.control.HttpMirrorServer;
import org.apache.jmeter.protocol.http.control.TestHTTPMirrorThread;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Assert;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Class for performing actual samples for HTTPSampler and HTTPSampler2.
 * The samples are executed against the HttpMirrorServer, which is 
 * started when the unit tests are executed.
 */
public class TestHTTPSamplersAgainstHttpMirrorServer extends JMeterTestCaseJUnit {
    private static final int HTTP_SAMPLER = 0;
    private static final int HTTP_SAMPLER3 = 2;
    
    /** The encodings used for http headers and control information */
    private static final String ISO_8859_1 = "ISO-8859-1"; // $NON-NLS-1$
    private static final String US_ASCII = "US-ASCII"; // $NON-NLS-1$

    private static final byte[] CRLF = { 0x0d, 0x0A };
    private static final int MIRROR_PORT = 8182; // Different from TestHTTPMirrorThread port and standard mirror server
    private static byte[] TEST_FILE_CONTENT;

    private static File temporaryFile;

    private final int item;

    public TestHTTPSamplersAgainstHttpMirrorServer(String arg0) {
        super(arg0);
        this.item = -1;
    }

    // additional ctor for processing tests which use int parameters
    public TestHTTPSamplersAgainstHttpMirrorServer(String arg0, int item) {
        super(arg0);
        this.item = item;
    }

    // This is used to emulate @before class and @after class
    public static Test suite(){
        final TestSuite testSuite = new TestSuite(TestHTTPSamplersAgainstHttpMirrorServer.class);
        // Add parameterised tests. For simplicity we assume each has cases 0-10
        for(int i=0; i<11; i++) {
            testSuite.addTest(new TestHTTPSamplersAgainstHttpMirrorServer("itemised_testGetRequest_Parameters", i));
            testSuite.addTest(new TestHTTPSamplersAgainstHttpMirrorServer("itemised_testGetRequest_Parameters3", i));

            testSuite.addTest(new TestHTTPSamplersAgainstHttpMirrorServer("itemised_testPostRequest_UrlEncoded", i));
            testSuite.addTest(new TestHTTPSamplersAgainstHttpMirrorServer("itemised_testPostRequest_UrlEncoded3", i));
        }

        TestSetup setup = new TestSetup(testSuite){
            private HttpMirrorServer httpServer;
            @Override
            protected void setUp() throws Exception {
                    httpServer = TestHTTPMirrorThread.startHttpMirror(MIRROR_PORT);
                    // Create the test file content
                    TEST_FILE_CONTENT = "some foo content &?=01234+56789-\u007c\u2aa1\u266a\u0153\u20a1\u0115\u0364\u00c5\u2052\uc385%C3%85".getBytes("UTF-8");

                    // create a temporary file to make sure we always have a file to give to the PostWriter 
                    // Whereever we are or Whatever the current path is.
                    temporaryFile = File.createTempFile("TestHTTPSamplersAgainstHttpMirrorServer", "tmp");
                    OutputStream output = new FileOutputStream(temporaryFile);
                    output.write(TEST_FILE_CONTENT);
                    output.flush();
                    output.close();
            }
            
            @Override
            protected void tearDown() throws Exception {
                    // Shutdown mirror server
                    httpServer.stopServer();
                    httpServer = null;
                    // delete temporay file
                    if(!temporaryFile.delete()) {
                        Assert.fail("Could not delete file:"+temporaryFile.getAbsolutePath());
                    }
            }
        };
        return setup;
    }
    
    public void itemised_testPostRequest_UrlEncoded() throws Exception {
        testPostRequest_UrlEncoded(HTTP_SAMPLER, ISO_8859_1, item);
    }

    public void itemised_testPostRequest_UrlEncoded3() throws Exception {
        testPostRequest_UrlEncoded(HTTP_SAMPLER3, US_ASCII, item);
    }

    public void testPostRequest_FormMultipart_0() throws Exception {
        testPostRequest_FormMultipart(HTTP_SAMPLER, ISO_8859_1);
    }

    public void testPostRequest_FormMultipart3() throws Exception {
        // see https://issues.apache.org/jira/browse/HTTPCLIENT-1665
        testPostRequest_FormMultipart(HTTP_SAMPLER3, US_ASCII);
    }

    public void testPostRequest_FileUpload() throws Exception {
        testPostRequest_FileUpload(HTTP_SAMPLER, ISO_8859_1);
    }

    public void testPostRequest_FileUpload3() throws Exception {        
        // see https://issues.apache.org/jira/browse/HTTPCLIENT-1665
        testPostRequest_FileUpload(HTTP_SAMPLER3, US_ASCII);
    }

    public void testPostRequest_BodyFromParameterValues() throws Exception {
        testPostRequest_BodyFromParameterValues(HTTP_SAMPLER, ISO_8859_1);
    }

    public void testPostRequest_BodyFromParameterValues3() throws Exception {
        testPostRequest_BodyFromParameterValues(HTTP_SAMPLER3, US_ASCII);
    }

    public void testGetRequest() throws Exception {
        testGetRequest(HTTP_SAMPLER);
    }
    
    public void testGetRequest3() throws Exception {
        testGetRequest(HTTP_SAMPLER3);
    }
    
    public void itemised_testGetRequest_Parameters() throws Exception {
        testGetRequest_Parameters(HTTP_SAMPLER, item);
    }  

    public void itemised_testGetRequest_Parameters3() throws Exception {
        testGetRequest_Parameters(HTTP_SAMPLER3, item);
    }   

    private void testPostRequest_UrlEncoded(int samplerType, String samplerDefaultEncoding, int test) throws Exception {
        String titleField = "title";
        String titleValue = "mytitle";
        String descriptionField = "description";
        String descriptionValue = "mydescription";
        HTTPSamplerBase sampler = createHttpSampler(samplerType);
        HTTPSampleResult res;
        String contentEncoding;

        switch(test) {
            case 0:
                // Test sending data with default encoding
                contentEncoding = "";
                setupUrl(sampler, contentEncoding);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, false);
                break;
            case 1:
                // Test sending data as ISO-8859-1
                contentEncoding = ISO_8859_1;
                setupUrl(sampler, contentEncoding);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, false);
                break;
            case 2:
                // Test sending data as UTF-8
                contentEncoding = "UTF-8";
                titleValue = "mytitle2\u0153\u20a1\u0115\u00c5";
                descriptionValue = "mydescription2\u0153\u20a1\u0115\u00c5";
                setupUrl(sampler, contentEncoding);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, false);
                break;
            case 3:
                // Test sending data as UTF-8, with values that will change when urlencoded
                contentEncoding = "UTF-8";
                titleValue = "mytitle3/=";
                descriptionValue = "mydescription3   /\\";
                setupUrl(sampler, contentEncoding);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, false);
                break;
            case 4:
                // Test sending data as UTF-8, with values that have been urlencoded
                contentEncoding = "UTF-8";
                titleValue = "mytitle4%2F%3D";
                descriptionValue = "mydescription4+++%2F%5C";
                setupUrl(sampler, contentEncoding);
                setupFormData(sampler, true, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, true);
                break;
            case 5:
                // Test sending data as UTF-8, with values similar to __VIEWSTATE parameter that .net uses
                contentEncoding = "UTF-8";
                titleValue = "/wEPDwULLTE2MzM2OTA0NTYPZBYCAgMPZ/rA+8DZ2dnZ2dnZ2d/GNDar6OshPwdJc=";
                descriptionValue = "mydescription5";
                setupUrl(sampler, contentEncoding);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, false);
                break;
            case 6:
                // Test sending data as UTF-8, with values similar to __VIEWSTATE parameter that .net uses,
                // with values urlencoded, but the always encode set to false for the arguments
                // This is how the HTTP Proxy server adds arguments to the sampler
                contentEncoding = "UTF-8";
                titleValue = "%2FwEPDwULLTE2MzM2OTA0NTYPZBYCAgMPZ%2FrA%2B8DZ2dnZ2dnZ2d%2FGNDar6OshPwdJc%3D";
                descriptionValue = "mydescription6";
                setupUrl(sampler, contentEncoding);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                ((HTTPArgument)sampler.getArguments().getArgument(0)).setAlwaysEncoded(false);
                ((HTTPArgument)sampler.getArguments().getArgument(1)).setAlwaysEncoded(false);
                res = executeSampler(sampler);
                assertFalse(((HTTPArgument)sampler.getArguments().getArgument(0)).isAlwaysEncoded());
                assertFalse(((HTTPArgument)sampler.getArguments().getArgument(1)).isAlwaysEncoded());
                checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, true);
                break;
            case 7:
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
                
                contentEncoding = "UTF-8";
                titleValue = "${title_prefix}mytitle7\u0153\u20a1\u0115\u00c5";
                descriptionValue = "mydescription7\u0153\u20a1\u0115\u00c5${description_suffix}";
                setupUrl(sampler, contentEncoding);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                // Replace the variables in the sampler
                replacer.replaceValues(sampler);
                res = executeSampler(sampler);
                String expectedTitleValue = "a test\u00c5mytitle7\u0153\u20a1\u0115\u00c5";
                String expectedDescriptionValue = "mydescription7\u0153\u20a1\u0115\u00c5the_end";
                checkPostRequestUrlEncoded(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, expectedTitleValue, descriptionField, expectedDescriptionValue, false);
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            default:
                fail("Unexpected switch value: "+test);
        }
        




        

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
        sampler.setDoMultipartPost(true);
        HTTPSampleResult res = executeSampler(sampler);
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);
        
        // Test sending data as ISO-8859-1
        sampler = createHttpSampler(samplerType);
        contentEncoding = ISO_8859_1;
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        sampler.setDoMultipartPost(true);
        res = executeSampler(sampler);
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);

        // Test sending data as UTF-8
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        sampler.setDoMultipartPost(true);
        res = executeSampler(sampler);
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);
        
        // Test sending data as UTF-8, with values that would have been urlencoded
        // if it was not sent as multipart
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle/=";
        descriptionValue = "mydescription   /\\";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        sampler.setDoMultipartPost(true);
        res = executeSampler(sampler);
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue);

        // Test sending data as UTF-8, with values that have been urlencoded
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle%2F%3D";
        descriptionValue = "mydescription+++%2F%5C";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, true, titleField, titleValue, descriptionField, descriptionValue);
        sampler.setDoMultipartPost(true);
        res = executeSampler(sampler);
        String expectedTitleValue = "mytitle/=";
        String expectedDescriptionValue = "mydescription   /\\";
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, expectedTitleValue, descriptionField, expectedDescriptionValue);

        // Test sending data as UTF-8, with values similar to __VIEWSTATE parameter that .net uses
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "/wEPDwULLTE2MzM2OTA0NTYPZBYCAgMPZ/rA+8DZ2dnZ2dnZ2d/GNDar6OshPwdJc=";
        descriptionValue = "mydescription";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        sampler.setDoMultipartPost(true);
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
        sampler.setDoMultipartPost(true);
        // Replace the variables in the sampler
        replacer.replaceValues(sampler);
        res = executeSampler(sampler);
        expectedTitleValue = "a test\u00c5mytitle\u0153\u20a1\u0115\u00c5";
        expectedDescriptionValue = "mydescription\u0153\u20a1\u0115\u00c5the_end";
        checkPostRequestFormMultipart(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, expectedTitleValue, descriptionField, expectedDescriptionValue);
    }

    private void testPostRequest_FileUpload(int samplerType, String samplerDefaultEncoding) throws Exception {
        String titleField = "title";
        String titleValue = "mytitle";
        String descriptionField = "description";
        String descriptionValue = "mydescription";
        String fileField = "file1";
        String fileMimeType = "text/plain";

        // Test sending data with default encoding
        HTTPSamplerBase sampler = createHttpSampler(samplerType);
        String contentEncoding = "";
        setupUrl(sampler, contentEncoding);
        setupFileUploadData(sampler, false, titleField, titleValue, descriptionField, descriptionValue, fileField, temporaryFile, fileMimeType);
        HTTPSampleResult res = executeSampler(sampler);
        checkPostRequestFileUpload(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, fileField, temporaryFile, fileMimeType, TEST_FILE_CONTENT);
        
        // Test sending data as ISO-8859-1
        sampler = createHttpSampler(samplerType);
        contentEncoding = ISO_8859_1;
        setupUrl(sampler, contentEncoding);
        setupFileUploadData(sampler, false, titleField, titleValue, descriptionField, descriptionValue, fileField, temporaryFile, fileMimeType);
        res = executeSampler(sampler);
        checkPostRequestFileUpload(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, fileField, temporaryFile, fileMimeType, TEST_FILE_CONTENT);

        // Test sending data as UTF-8
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5";
        setupUrl(sampler, contentEncoding);
        setupFileUploadData(sampler, false, titleField, titleValue, descriptionField, descriptionValue, fileField, temporaryFile, fileMimeType);
        res = executeSampler(sampler);
        checkPostRequestFileUpload(sampler, res, samplerDefaultEncoding, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, fileField, temporaryFile, fileMimeType, TEST_FILE_CONTENT);
    }

    private void testPostRequest_BodyFromParameterValues(int samplerType, String samplerDefaultEncoding) throws Exception {
        final String titleField = ""; // ensure only values are used
        String titleValue = "mytitle";
        final String descriptionField = ""; // ensure only values are used
        String descriptionValue = "mydescription";

        // Test sending data with default encoding
        HTTPSamplerBase sampler = createHttpSampler(samplerType);
        String contentEncoding = "";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        ((HTTPArgument)sampler.getArguments().getArgument(0)).setAlwaysEncoded(false);
        ((HTTPArgument)sampler.getArguments().getArgument(1)).setAlwaysEncoded(false);
        HTTPSampleResult res = executeSampler(sampler);
        String expectedPostBody = titleValue + descriptionValue;
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);

        // Test sending data as ISO-8859-1
        sampler = createHttpSampler(samplerType);
        contentEncoding = ISO_8859_1;
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        ((HTTPArgument)sampler.getArguments().getArgument(0)).setAlwaysEncoded(false);
        ((HTTPArgument)sampler.getArguments().getArgument(1)).setAlwaysEncoded(false);
        res = executeSampler(sampler);
        expectedPostBody = titleValue + descriptionValue;
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);

        // Test sending data as UTF-8
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle\u0153\u20a1\u0115\u00c5";
        descriptionValue = "mydescription\u0153\u20a1\u0115\u00c5";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        ((HTTPArgument)sampler.getArguments().getArgument(0)).setAlwaysEncoded(false);
        ((HTTPArgument)sampler.getArguments().getArgument(1)).setAlwaysEncoded(false);
        res = executeSampler(sampler);
        expectedPostBody = titleValue + descriptionValue;
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);

        // Test sending data as UTF-8, with values that will change when urlencoded
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle/=";
        descriptionValue = "mydescription   /\\";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        ((HTTPArgument)sampler.getArguments().getArgument(0)).setAlwaysEncoded(false);
        ((HTTPArgument)sampler.getArguments().getArgument(1)).setAlwaysEncoded(false);
        res = executeSampler(sampler);
        expectedPostBody = titleValue + descriptionValue;
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);

        // Test sending data as UTF-8, with values that will change when urlencoded, and where
        // we tell the sampler to urlencode the parameter value
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle/=";
        descriptionValue = "mydescription   /\\";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, true, titleField, titleValue, descriptionField, descriptionValue);
        res = executeSampler(sampler);
        expectedPostBody = URLEncoder.encode(titleValue + descriptionValue, contentEncoding);
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);

        // Test sending data as UTF-8, with values that have been urlencoded
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle%2F%3D";
        descriptionValue = "mydescription+++%2F%5C";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        ((HTTPArgument)sampler.getArguments().getArgument(0)).setAlwaysEncoded(false);
        ((HTTPArgument)sampler.getArguments().getArgument(1)).setAlwaysEncoded(false);
        res = executeSampler(sampler);
        expectedPostBody = titleValue + descriptionValue;
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);

        // Test sending data as UTF-8, with values that have been urlencoded, and
        // where we tell the sampler to urlencode the parameter values
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle%2F%3D";
        descriptionValue = "mydescription+++%2F%5C";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, true, titleField, titleValue, descriptionField, descriptionValue);
        res = executeSampler(sampler);
        expectedPostBody = titleValue + descriptionValue;
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);

        // Test sending data as UTF-8, with values similar to __VIEWSTATE parameter that .net uses
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "/wEPDwULLTE2MzM2OTA0NTYPZBYCAgMPZ/rA+8DZ2dnZ2dnZ2d/GNDar6OshPwdJc=";
        descriptionValue = "mydescription";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        ((HTTPArgument)sampler.getArguments().getArgument(0)).setAlwaysEncoded(false);
        ((HTTPArgument)sampler.getArguments().getArgument(1)).setAlwaysEncoded(false);
        res = executeSampler(sampler);
        expectedPostBody = titleValue + descriptionValue;
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);

        // Test sending data as UTF-8, with + as part of the value,
        // where the value is set in sampler as not urluencoded, but the 
        // isalwaysencoded flag of the argument is set to false.
        // This mimics the HTTPConstants.addNonEncodedArgument, which the
        // Proxy server calls in some cases
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        titleValue = "mytitle++";
        descriptionValue = "mydescription+";
        setupUrl(sampler, contentEncoding);
        setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
        ((HTTPArgument)sampler.getArguments().getArgument(0)).setAlwaysEncoded(false);
        ((HTTPArgument)sampler.getArguments().getArgument(1)).setAlwaysEncoded(false);
        res = executeSampler(sampler);
        expectedPostBody = titleValue + descriptionValue;
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);

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
        ((HTTPArgument)sampler.getArguments().getArgument(0)).setAlwaysEncoded(false);
        ((HTTPArgument)sampler.getArguments().getArgument(1)).setAlwaysEncoded(false);
        // Replace the variables in the sampler
        replacer.replaceValues(sampler);
        res = executeSampler(sampler);
        String expectedTitleValue = "a test\u00c5mytitle\u0153\u20a1\u0115\u00c5";
        String expectedDescriptionValue = "mydescription\u0153\u20a1\u0115\u00c5the_end";
        expectedPostBody = expectedTitleValue+ expectedDescriptionValue;
        checkPostRequestBody(sampler, res, samplerDefaultEncoding, contentEncoding, expectedPostBody);
    }

    private void testGetRequest(int samplerType) throws Exception {
        // Test sending simple HTTP get
        // Test sending data with default encoding
        HTTPSamplerBase sampler = createHttpSampler(samplerType);
        String contentEncoding = "";
        setupUrl(sampler, contentEncoding);
        sampler.setMethod(HTTPConstants.GET);
        HTTPSampleResult res = executeSampler(sampler);
        checkGetRequest(sampler, res);
        
        // Test sending data with ISO-8859-1 encoding
        sampler = createHttpSampler(samplerType);
        contentEncoding = ISO_8859_1;
        setupUrl(sampler, contentEncoding);
        sampler.setMethod(HTTPConstants.GET);
        res = executeSampler(sampler);
        checkGetRequest(sampler, res);

        // Test sending data with UTF-8 encoding
        sampler = createHttpSampler(samplerType);
        contentEncoding = "UTF-8";
        setupUrl(sampler, contentEncoding);
        sampler.setMethod(HTTPConstants.GET);
        res = executeSampler(sampler);
        checkGetRequest(sampler, res);
    }

    private void testGetRequest_Parameters(int samplerType, int test) throws Exception {
        String titleField = "title";
        String titleValue = "mytitle";
        String descriptionField = "description";
        String descriptionValue = "mydescription";
        HTTPSamplerBase sampler = createHttpSampler(samplerType);
        String contentEncoding;
        HTTPSampleResult res;
        URL executedUrl;
   
        switch(test) {
            case 0:
                // Test sending simple HTTP get
                // Test sending data with default encoding
                contentEncoding = "";
                setupUrl(sampler, contentEncoding);
                sampler.setMethod(HTTPConstants.GET);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                sampler.setRunningVersion(true);
                executedUrl = sampler.getUrl();
                sampler.setRunningVersion(false);
                checkGetRequest_Parameters(sampler, res, contentEncoding, executedUrl, titleField, titleValue, descriptionField, descriptionValue, false);
                break;
            case 1:
                // Test sending data with ISO-8859-1 encoding
                sampler = createHttpSampler(samplerType);
                contentEncoding = ISO_8859_1;
                titleValue = "mytitle1\uc385";
                descriptionValue = "mydescription1\uc385";
                setupUrl(sampler, contentEncoding);
                sampler.setMethod(HTTPConstants.GET);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                sampler.setRunningVersion(true);
                executedUrl = sampler.getUrl();
                sampler.setRunningVersion(false);
                checkGetRequest_Parameters(sampler, res, contentEncoding, executedUrl, titleField, titleValue, descriptionField, descriptionValue, false);
                break;
            case 2:
                // Test sending data with UTF-8 encoding
                sampler = createHttpSampler(samplerType);
                contentEncoding = "UTF-8";
                titleValue = "mytitle2\u0153\u20a1\u0115\u00c5";
                descriptionValue = "mydescription2\u0153\u20a1\u0115\u00c5";
                setupUrl(sampler, contentEncoding);
                sampler.setMethod(HTTPConstants.GET);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                sampler.setRunningVersion(true);
                executedUrl = sampler.getUrl();
                sampler.setRunningVersion(false);
                checkGetRequest_Parameters(sampler, res, contentEncoding, executedUrl, titleField, titleValue, descriptionField, descriptionValue, false);
                break;
            case 3:
                // Test sending data as UTF-8, with values that changes when urlencoded
                sampler = createHttpSampler(samplerType);
                contentEncoding = "UTF-8";
                titleValue = "mytitle3\u0153+\u20a1 \u0115&yes\u00c5";
                descriptionValue = "mydescription3 \u0153 \u20a1 \u0115 \u00c5";
                setupUrl(sampler, contentEncoding);
                sampler.setMethod(HTTPConstants.GET);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                sampler.setRunningVersion(true);
                executedUrl = sampler.getUrl();
                sampler.setRunningVersion(false);
                checkGetRequest_Parameters(sampler, res, contentEncoding, executedUrl, titleField, titleValue, descriptionField, descriptionValue, false);
                break;
            case 4:
                // Test sending data as UTF-8, with values that have been urlencoded
                sampler = createHttpSampler(samplerType);
                contentEncoding = "UTF-8";
                titleValue = "mytitle4%2F%3D";
                descriptionValue = "mydescription4+++%2F%5C";
                setupUrl(sampler, contentEncoding);
                sampler.setMethod(HTTPConstants.GET);
                setupFormData(sampler, true, titleField, titleValue, descriptionField, descriptionValue);
                res = executeSampler(sampler);
                sampler.setRunningVersion(true);
                executedUrl = sampler.getUrl();
                sampler.setRunningVersion(false);
                checkGetRequest_Parameters(sampler, res, contentEncoding, executedUrl, titleField, titleValue, descriptionField, descriptionValue, true);
                break;
            case 5:
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
                titleValue = "${title_prefix}mytitle5\u0153\u20a1\u0115\u00c5";
                descriptionValue = "mydescription5\u0153\u20a1\u0115\u00c5${description_suffix}";
                setupUrl(sampler, contentEncoding);
                sampler.setMethod(HTTPConstants.GET);
                setupFormData(sampler, false, titleField, titleValue, descriptionField, descriptionValue);
                // Replace the variables in the sampler
                replacer.replaceValues(sampler);
                res = executeSampler(sampler);
                String expectedTitleValue = "a test\u00c5mytitle5\u0153\u20a1\u0115\u00c5";
                String expectedDescriptionValue = "mydescription5\u0153\u20a1\u0115\u00c5the_end";
                sampler.setRunningVersion(true);
                executedUrl = sampler.getUrl();
                sampler.setRunningVersion(false);
                checkGetRequest_Parameters(sampler, res, contentEncoding, executedUrl, titleField, expectedTitleValue, descriptionField, expectedDescriptionValue, false);
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            case 10:
                break;
            default:
                fail("Unexpected switch value: "+test);
        }
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
            String descriptionValue,
            boolean valuesAlreadyUrlEncoded) throws IOException {
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = samplerDefaultEncoding;
        }
        // Check URL
        assertEquals(sampler.getUrl(), res.getURL());
        String expectedPostBody = null;
        if(!valuesAlreadyUrlEncoded) {
            String expectedTitle = URLEncoder.encode(titleValue, contentEncoding);
            String expectedDescription = URLEncoder.encode(descriptionValue, contentEncoding);
            expectedPostBody = titleField + "=" + expectedTitle + "&" + descriptionField + "=" + expectedDescription;
        }
        else {
            expectedPostBody = titleField + "=" + titleValue + "&" + descriptionField + "=" + descriptionValue;
        }
        // Check the request
        checkPostRequestBody(
            sampler,
            res,
            samplerDefaultEncoding,
            contentEncoding,
            expectedPostBody
        );        
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
        checkHeaderTypeLength(res.getRequestHeaders(), "multipart/form-data" + "; boundary=" + boundaryString, expectedPostBody.length);
        // Check post body from the result query string
        checkArraysHaveSameContent(expectedPostBody, res.getQueryString().getBytes(contentEncoding), contentEncoding, res);

        // Find the data sent to the mirror server, which the mirror server is sending back to us
        String dataSentToMirrorServer = new String(res.getResponseData(), contentEncoding);
        int posDividerHeadersAndBody = getPositionOfBody(dataSentToMirrorServer);
        String headersSent = null;
        String bodySent = "";
        if(posDividerHeadersAndBody >= 0) {
            headersSent = dataSentToMirrorServer.substring(0, posDividerHeadersAndBody);
            // Skip the blank line with crlf dividing headers and body
            bodySent = dataSentToMirrorServer.substring(posDividerHeadersAndBody+2);
        }
        else {
            fail("No header and body section found");
        }
         // Check response headers
        checkHeaderTypeLength(headersSent, "multipart/form-data" + "; boundary=" + boundaryString, expectedPostBody.length);
        // Check post body which was sent to the mirror server, and
        // sent back by the mirror server
        checkArraysHaveSameContent(expectedPostBody, bodySent.getBytes(contentEncoding), contentEncoding, res);
        // Check method, path and query sent
        checkMethodPathQuery(headersSent, sampler.getMethod(), sampler.getPath(), (String) null, res);
    }
    
    private void checkPostRequestFileUpload(
            HTTPSamplerBase sampler,
            HTTPSampleResult res,
            String samplerDefaultEncoding,
            String contentEncoding,
            String titleField,
            String titleValue,
            String descriptionField,
            String descriptionValue,
            String fileField,
            File fileValue,
            String fileMimeType,
            byte[] fileContent) throws IOException {
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = samplerDefaultEncoding;
        }
        // Check URL
        assertEquals(sampler.getUrl(), res.getURL());
        String boundaryString = getBoundaryStringFromContentType(res.getRequestHeaders());
        assertNotNull(boundaryString);
        byte[] expectedPostBody = createExpectedFormAndUploadOutput(boundaryString, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, fileField, fileValue, fileMimeType, fileContent);
        // Check request headers
        checkHeaderTypeLength(res.getRequestHeaders(), "multipart/form-data" + "; boundary=" + boundaryString, expectedPostBody.length);
        // We cannot check post body from the result query string, since that will not contain
        // the actual file content, but placeholder text for file content
        //checkArraysHaveSameContent(expectedPostBody, res.getQueryString().getBytes(contentEncoding));

        // Find the data sent to the mirror server, which the mirror server is sending back to us
        String headersSent = getHeadersSent(res.getResponseData());
        if(headersSent == null) {
            fail("No header and body section found");
        }
        // Check response headers
        checkHeaderTypeLength(headersSent, "multipart/form-data" + "; boundary=" + boundaryString, expectedPostBody.length);
        byte[] bodySent = getBodySent(res.getResponseData());
        assertNotNull("Sent body should not be null", bodySent);
        // Check post body which was sent to the mirror server, and
        // sent back by the mirror server
        checkArraysHaveSameContent(expectedPostBody, bodySent, contentEncoding, res);
        // Check method, path and query sent
        checkMethodPathQuery(headersSent, sampler.getMethod(), sampler.getPath(), (String) null, res);
    }

    private void checkPostRequestBody(
            HTTPSamplerBase sampler,
            HTTPSampleResult res,
            String samplerDefaultEncoding,
            String contentEncoding,
            String expectedPostBody) throws IOException {
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = samplerDefaultEncoding;
        }
        // Check URL
        assertEquals(sampler.getUrl(), res.getURL());
        // Check request headers
        checkHeaderTypeLength(res.getRequestHeaders(), HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED, expectedPostBody.getBytes(contentEncoding).length);
         // Check post body from the result query string
        checkArraysHaveSameContent(expectedPostBody.getBytes(contentEncoding), res.getQueryString().getBytes(contentEncoding), contentEncoding, res);

        // Find the data sent to the mirror server, which the mirror server is sending back to us
        String dataSentToMirrorServer = new String(res.getResponseData(), contentEncoding);
        int posDividerHeadersAndBody = getPositionOfBody(dataSentToMirrorServer);
        String headersSent = null;
        String bodySent = "";
        if(posDividerHeadersAndBody >= 0) {
            headersSent = dataSentToMirrorServer.substring(0, posDividerHeadersAndBody);
            // Skip the blank line with crlf dividing headers and body
            bodySent = dataSentToMirrorServer.substring(posDividerHeadersAndBody+2);
        }
        else {
            fail("No header and body section found");
        }
        // Check response headers
        checkHeaderTypeLength(headersSent, HTTPConstants.APPLICATION_X_WWW_FORM_URLENCODED, expectedPostBody.getBytes(contentEncoding).length);
        // Check post body which was sent to the mirror server, and
        // sent back by the mirror server
        checkArraysHaveSameContent(expectedPostBody.getBytes(contentEncoding), bodySent.getBytes(contentEncoding), contentEncoding, res);
        // Check method, path and query sent
        checkMethodPathQuery(headersSent, sampler.getMethod(), sampler.getPath(), (String) null, res);
    }

    private void checkGetRequest(
            HTTPSamplerBase sampler,
            HTTPSampleResult res
            ) throws IOException {
        // Check URL
        assertEquals(sampler.getUrl(), res.getURL());
        // Check method
        assertEquals(sampler.getMethod(), res.getHTTPMethod());
        // Check that the query string is empty
        assertEquals(0, res.getQueryString().length());

        // Find the data sent to the mirror server, which the mirror server is sending back to us
        String dataSentToMirrorServer = new String(res.getResponseData(), EncoderCache.URL_ARGUMENT_ENCODING);
        int posDividerHeadersAndBody = getPositionOfBody(dataSentToMirrorServer);
        String headersSent = null;
        String bodySent = "";
        if(posDividerHeadersAndBody >= 0) {
            headersSent = dataSentToMirrorServer.substring(0, posDividerHeadersAndBody);
            // Skip the blank line with crlf dividing headers and body
            bodySent = dataSentToMirrorServer.substring(posDividerHeadersAndBody+2);
        }
        else {
            fail("No header and body section found");
        }
        // No body should have been sent
        assertEquals(bodySent.length(), 0);
        // Check method, path and query sent
        checkMethodPathQuery(headersSent, sampler.getMethod(), sampler.getPath(), (String) null, res);
    }
    
    private void checkGetRequest_Parameters(
            HTTPSamplerBase sampler,
            HTTPSampleResult res,
            String contentEncoding,
            URL executedUrl,
            String titleField,
            String titleValue,
            String descriptionField,
            String descriptionValue,
            boolean valuesAlreadyUrlEncoded) throws IOException {
        if(contentEncoding == null || contentEncoding.length() == 0) {
            contentEncoding = EncoderCache.URL_ARGUMENT_ENCODING;
        }
        // Check URL
        assertEquals(executedUrl, res.getURL());
        // Check method
        assertEquals(sampler.getMethod(), res.getHTTPMethod());
        // Cannot check the query string of the result, because the mirror server
        // replies without including query string in URL
        
        String expectedQueryString = null;
        if(!valuesAlreadyUrlEncoded) {
            String expectedTitle = URLEncoder.encode(titleValue, contentEncoding);
            String expectedDescription = URLEncoder.encode(descriptionValue, contentEncoding);
            expectedQueryString = titleField + "=" + expectedTitle + "&" + descriptionField + "=" + expectedDescription;
        }
        else {
            expectedQueryString = titleField + "=" + titleValue + "&" + descriptionField + "=" + descriptionValue;
        }

        // Find the data sent to the mirror server, which the mirror server is sending back to us
        String dataSentToMirrorServer = new String(res.getResponseData(), EncoderCache.URL_ARGUMENT_ENCODING);
        int posDividerHeadersAndBody = getPositionOfBody(dataSentToMirrorServer);
        String headersSent = null;
        String bodySent = "";
        if(posDividerHeadersAndBody >= 0) {
            headersSent = dataSentToMirrorServer.substring(0, posDividerHeadersAndBody);
            // Skip the blank line with crlf dividing headers and body
            bodySent = dataSentToMirrorServer.substring(posDividerHeadersAndBody+2);
        }
        else {
            fail("No header and body section found in: ["+dataSentToMirrorServer+"]");
        }
        // No body should have been sent
        assertEquals(bodySent.length(), 0);
        // Check method, path and query sent
        checkMethodPathQuery(headersSent, sampler.getMethod(), sampler.getPath(), expectedQueryString, res);
    }
    
    private void checkMethodPathQuery(
            String headersSent,
            String expectedMethod,
            String expectedPath,
            String expectedQueryString,
            HTTPSampleResult res
            )
            throws IOException {
        // Check the Request URI sent to the mirror server, and
        // sent back by the mirror server
        int indexFirstSpace = headersSent.indexOf(' ');
        int indexSecondSpace = headersSent.indexOf(' ', headersSent.length() > indexFirstSpace ? indexFirstSpace + 1 : indexFirstSpace);
        if(indexFirstSpace <= 0 && indexSecondSpace <= 0 || indexFirstSpace == indexSecondSpace) {
            fail("Could not find method and URI sent");
        }
        String methodSent = headersSent.substring(0, indexFirstSpace);
        assertEquals(expectedMethod, methodSent);
        String uriSent = headersSent.substring(indexFirstSpace + 1, indexSecondSpace);
        int indexQueryStart = uriSent.indexOf('?');
        if(expectedQueryString != null && expectedQueryString.length() > 0) {
            // We should have a query string part
            if(indexQueryStart <= 0 || (indexQueryStart == uriSent.length() - 1)) {
                fail("Could not find query string in URI");
            }
        }
        else {
            if(indexQueryStart > 0) {
                // We should not have a query string part
                fail("Query string present in URI");
            }
            else {
                indexQueryStart = uriSent.length();
            }
        }
        // Check path
        String pathSent = uriSent.substring(0, indexQueryStart);
        assertEquals(expectedPath, pathSent);
        // Check query
        if(expectedQueryString != null && expectedQueryString.length() > 0) {
            String queryStringSent = uriSent.substring(indexQueryStart + 1);
            // Is it only the parameter values which are encoded in the specified
            // content encoding, the rest of the query is encoded in UTF-8
            // Therefore we compare the whole query using UTF-8
            checkArraysHaveSameContent(expectedQueryString.getBytes(EncoderCache.URL_ARGUMENT_ENCODING), queryStringSent.getBytes(EncoderCache.URL_ARGUMENT_ENCODING), EncoderCache.URL_ARGUMENT_ENCODING, res);
        }
    }

    private String getHeadersSent(byte[] responseData) throws IOException {
        // Find the data sent to the mirror server, which the mirror server is sending back to us
        // We assume the headers are in ISO_8859_1, and the body can be in any content encoding.
        String dataSentToMirrorServer = new String(responseData, ISO_8859_1);
        int posDividerHeadersAndBody = getPositionOfBody(dataSentToMirrorServer);
        String headersSent = null;
        if(posDividerHeadersAndBody >= 0) {
            headersSent = dataSentToMirrorServer.substring(0, posDividerHeadersAndBody);
        }
        return headersSent;
    }

    private byte[] getBodySent(byte[] responseData) throws IOException {
        // Find the data sent to the mirror server, which the mirror server is sending back to us
        // We assume the headers are in ISO_8859_1, and the body can be in any content encoding.
        // Therefore we get the data sent in ISO_8859_1, to be able to determine the end of the
        // header part, and then we just construct a byte array to hold the body part, not taking
        // encoding of the body into consideration, because it can contain file data, which is
        // sent as raw byte data
        byte[] bodySent = null;
        String headersSent = getHeadersSent(responseData);
        if(headersSent != null) {
            // Get the content length, it tells us how much data to read
            // TODO : Maybe support chunked encoding, then we cannot rely on content length
            String contentLengthValue = getSentRequestHeaderValue(headersSent, HTTPConstants.HEADER_CONTENT_LENGTH);
            int contentLength = -1;
            if(contentLengthValue != null) {
                contentLength = Integer.parseInt(contentLengthValue);
            }
            else {
                fail("Did not receive any content-length header");
            }
            bodySent = new byte[contentLength];
            System.arraycopy(responseData, responseData.length - contentLength, bodySent, 0, contentLength);
        }
        return bodySent;
    }

    private boolean isInRequestHeaders(String requestHeaders, String headerName, String headerValue) {
        return checkRegularExpression(requestHeaders, headerName + ": " + headerValue);
    }
  
    // Java 1.6.0_22+ no longer allows Content-Length to be set, so don't check it.
    // See: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6996110
    // TODO any point in checking the other headers?
    private void checkHeaderTypeLength(String requestHeaders, String contentType, int contentLen) {
        boolean typeOK = isInRequestHeaders(requestHeaders, HTTPConstants.HEADER_CONTENT_TYPE, contentType);
        if (!typeOK){
            fail("Expected type:" + contentType + " in:\n"+ requestHeaders);
        }
    }
   
    private String getSentRequestHeaderValue(String requestHeaders, String headerName) {
        Perl5Matcher localMatcher = JMeterUtils.getMatcher();
        String expression = ".*" + headerName + ": (\\d*).*";
        Pattern pattern = JMeterUtils.getPattern(expression, Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.SINGLELINE_MASK);
        if(localMatcher.matches(requestHeaders, pattern)) {
            // The value is in the first group, group 0 is the whole match
            return localMatcher.getMatch().group(1);
        }
        return null;
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
        String regularExpression = "^" + HTTPConstants.HEADER_CONTENT_TYPE + ": multipart/form-data; boundary=(.+)$"; 
        Pattern pattern = JMeterUtils.getPattern(regularExpression, Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK | Perl5Compiler.MULTILINE_MASK);
        if(localMatcher.contains(requestHeaders, pattern)) {
            MatchResult match = localMatcher.getMatch();
            String matchString =  match.group(1);
            // Header may contain ;charset= , regexp extracts it so computed boundary is wrong
            int indexOf = matchString.indexOf(';');
            if(indexOf>=0) {
                return matchString.substring(0, indexOf);
            } else {
                return matchString;
            }
        }
        else {
            return null;
        }
    }

    private void setupUrl(HTTPSamplerBase sampler, String contentEncoding) {
        String protocol = "http";
        String domain = "localhost";
        String path = "/test/somescript.jsp";
        sampler.setProtocol(protocol);
        sampler.setMethod(HTTPConstants.POST);
        sampler.setPath(path);
        sampler.setDomain(domain);
        sampler.setPort(MIRROR_PORT);
        sampler.setContentEncoding(contentEncoding);
    }

    /**
     * Setup the form data with specified values
     * 
     * @param httpSampler
     */
    private void setupFormData(HTTPSamplerBase httpSampler, boolean isEncoded, String titleField, String titleValue, String descriptionField, String descriptionValue) {
        if(isEncoded) {
            httpSampler.addEncodedArgument(titleField, titleValue);
            httpSampler.addEncodedArgument(descriptionField, descriptionValue);
        }
        else {
            httpSampler.addArgument(titleField, titleValue);
            httpSampler.addArgument(descriptionField, descriptionValue);
        }
    }

    /**
     * Setup the form data with specified values, and file to upload
     * 
     * @param httpSampler
     */
    private void setupFileUploadData(
            HTTPSamplerBase httpSampler,
            boolean isEncoded,
            String titleField,
            String titleValue,
            String descriptionField,
            String descriptionValue,
            String fileField,
            File fileValue,
            String fileMimeType) {
        // Set the form data
        setupFormData(httpSampler, isEncoded, titleField, titleValue, descriptionField, descriptionValue);
        // Set the file upload data
        HTTPFileArg[] hfa = {new HTTPFileArg(fileValue == null ? "" : fileValue.getAbsolutePath(), fileField, fileMimeType)};
        httpSampler.setHTTPFiles(hfa);

    }

    /**
     * Check that the two byte arrays have identical content
     * 
     * @param expected
     * @param actual
     * @throws UnsupportedEncodingException 
     */
    private void checkArraysHaveSameContent(byte[] expected, byte[] actual, String encoding, HTTPSampleResult res) throws UnsupportedEncodingException {
        if(expected != null && actual != null) {
            if(expected.length != actual.length) {
                System.out.println("\n>>>>>>>>>>>>>>>>>>>> expected:");
                System.out.println(new String(expected, encoding));
                System.out.println("==================== actual:");
                System.out.println(new String(actual, encoding));
                System.out.println("<<<<<<<<<<<<<<<<<<<<");
                if (res != null) {
                    System.out.println("URL="+res.getUrlAsString());
                }
                fail("arrays have different length, expected is " + expected.length + ", actual is " + actual.length);
            }
            else {
                for(int i = 0; i < expected.length; i++) {
                    if(expected[i] != actual[i]) {
                        System.out.println("\n>>>>>>>>>>>>>>>>>>>> expected:");
                        System.out.println(new String(expected,0,i+1, encoding));
                        System.out.println("==================== actual:");
                        System.out.println(new String(actual,0,i+1, encoding));
                        System.out.println("<<<<<<<<<<<<<<<<<<<<");
/*                        
                        // Useful to when debugging
                        for(int j = 0; j  < expected.length; j++) {
                            System.out.print(expected[j] + " ");
                        }
                        System.out.println();
                        for(int j = 0; j  < actual.length; j++) {
                            System.out.print(actual[j] + " ");
                        }
                        System.out.println();
*/                        
                        if (res != null) {
                            System.out.println("URL="+res.getUrlAsString());
                        }
                        fail("byte at position " + i + " is different, expected is " + expected[i] + ", actual is " + actual[i]);
                    }
                }
            }
        }
        else {
            if (res != null) {
                System.out.println("URL="+res.getUrlAsString());
            }
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
        final byte[] DASH_DASH = "--".getBytes(ISO_8859_1);

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        if(firstMultipart) {
            output.write(DASH_DASH);
            output.write(boundaryString.getBytes(ISO_8859_1));
            output.write(CRLF);
        }
        output.write("Content-Disposition: form-data; name=\"".getBytes(ISO_8859_1));
        output.write(titleField.getBytes(ISO_8859_1));
        output.write("\"".getBytes(ISO_8859_1));
        output.write(CRLF);
        output.write("Content-Type: text/plain".getBytes(ISO_8859_1));
        if(contentEncoding != null) {
            output.write("; charset=".getBytes(ISO_8859_1));
            output.write(contentEncoding.getBytes(ISO_8859_1));
        }
        output.write(CRLF);
        output.write("Content-Transfer-Encoding: 8bit".getBytes(ISO_8859_1));
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
        output.write(boundaryString.getBytes(ISO_8859_1));
        output.write(CRLF);
        output.write("Content-Disposition: form-data; name=\"".getBytes(ISO_8859_1));
        output.write(descriptionField.getBytes(ISO_8859_1));
        output.write("\"".getBytes(ISO_8859_1));
        output.write(CRLF);
        output.write("Content-Type: text/plain".getBytes(ISO_8859_1));
        if(contentEncoding != null) {
            output.write("; charset=".getBytes(ISO_8859_1));
            output.write(contentEncoding.getBytes(ISO_8859_1));
        }
        output.write(CRLF);
        output.write("Content-Transfer-Encoding: 8bit".getBytes(ISO_8859_1));
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
        output.write(boundaryString.getBytes(ISO_8859_1));
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
        final byte[] DASH_DASH = "--".getBytes(ISO_8859_1);
        
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        if(firstMultipart) {
            output.write(DASH_DASH);
            output.write(boundaryString.getBytes(ISO_8859_1));
            output.write(CRLF);
        }
        // replace all backslash with double backslash
        String filename = file.getName();
        output.write("Content-Disposition: form-data; name=\"".getBytes(ISO_8859_1));
        output.write(fileField.getBytes(ISO_8859_1));
        output.write(("\"; filename=\"" + filename + "\"").getBytes(ISO_8859_1));
        output.write(CRLF);
        output.write("Content-Type: ".getBytes(ISO_8859_1));
        output.write(mimeType.getBytes(ISO_8859_1));
        output.write(CRLF);
        output.write("Content-Transfer-Encoding: binary".getBytes(ISO_8859_1));
        output.write(CRLF);
        output.write(CRLF);
        output.write(fileContent);
        output.write(CRLF);
        output.write(DASH_DASH);
        output.write(boundaryString.getBytes(ISO_8859_1));
        if(lastMultipart) {
            output.write(DASH_DASH);
        }
        output.write(CRLF);
        
        output.flush();
        output.close();

        return output.toByteArray();
    }
    
    /**
     * Create the expected output post body for form data and file multiparts
     * with specified values, when request is multipart
     */
    private byte[] createExpectedFormAndUploadOutput(
            String boundaryString,
            String contentEncoding,
            String titleField,
            String titleValue,
            String descriptionField,
            String descriptionValue,
            String fileField,
            File fileValue,
            String fileMimeType,
            byte[] fileContent) throws IOException {
        // Create the multiparts
        byte[] formdataMultipart = createExpectedFormdataOutput(boundaryString, contentEncoding, titleField, titleValue, descriptionField, descriptionValue, true, false);
        byte[] fileMultipart = createExpectedFilepartOutput(boundaryString, fileField, fileValue, fileMimeType, fileContent, false, true);
        
        // Join the two multiparts
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(formdataMultipart);
        output.write(fileMultipart);
        
        output.flush();
        output.close();
        
        return output.toByteArray();
    }
    
    private HTTPSamplerBase createHttpSampler(int samplerType) {
        switch(samplerType) {
            case HTTP_SAMPLER:
                return new HTTPSampler();
            case HTTP_SAMPLER3:
                return new HTTPSampler3();
            default:
                break;
        }
        throw new IllegalArgumentException("Unexpected type: "+samplerType);
    }
}
