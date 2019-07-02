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

package org.apache.jmeter.protocol.http.modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPNullSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.io.TextFile;
import org.junit.Before;
import org.junit.Test;

public class TestAnchorModifier extends JMeterTestCase {
        private AnchorModifier parser = new AnchorModifier();

        private JMeterContext jmctx = null;

        @Before
        public void setUp() {
            jmctx = JMeterContextService.getContext();
            parser.setThreadContext(jmctx);
        }

        private void testProcessingHTMLFile(String HTMLFileName) throws Exception {
            File file = new File(System.getProperty("user.dir") + "/testfiles/load_bug_list.jmx");
            HTTPSamplerBase config = (HTTPSamplerBase) SaveService.loadTree(file).getArray()[0];
            config.setRunningVersion(true);
            HTTPSampleResult result = new HTTPSampleResult();
            file = new File(System.getProperty("user.dir") + "/testfiles/Load_JMeter_Page.jmx");
            HTTPSamplerBase context = (HTTPSamplerBase) SaveService.loadTree(file).getArray()[0];
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            result.setResponseData(new TextFile(System.getProperty("user.dir") + HTMLFileName).getText(), null);
            result.setSampleLabel(context.toString());
            result.setSamplerData(context.toString());
            result.setURL(new URL("http://bz.apache.org/fakepage.html"));
            jmctx.setPreviousResult(result);
            AnchorModifier modifier = new AnchorModifier();
            modifier.setThreadContext(jmctx);
            modifier.process();
            assertEquals("http://bz.apache.org/bugzilla/buglist.cgi?"
                    + "bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED"
                    + "&email1=&emailtype1=substring&emailassigned_to1=1"
                    + "&email2=&emailtype2=substring&emailreporter2=1" + "&bugidtype=include&bug_id=&changedin=&votes="
                    + "&chfieldfrom=&chfieldto=Now&chfieldvalue="
                    + "&product=JMeter&short_desc=&short_desc_type=substring"
                    + "&long_desc=&long_desc_type=substring&bug_file_loc=" + "&bug_file_loc_type=substring&keywords="
                    + "&keywords_type=anywords" + "&field0-0-0=noop&type0-0-0=noop&value0-0-0="
                    + "&cmdtype=doit&order=Reuse+same+sort+as+last+time", config.toString());
            config.recoverRunningVersion();
            assertEquals("http://bz.apache.org/bugzilla/buglist.cgi?"
                    + "bug_status=.*&bug_status=.*&bug_status=.*&email1="
                    + "&emailtype1=substring&emailassigned_to1=1&email2=" + "&emailtype2=substring&emailreporter2=1"
                    + "&bugidtype=include&bug_id=&changedin=&votes=" + "&chfieldfrom=&chfieldto=Now&chfieldvalue="
                    + "&product=JMeter&short_desc=&short_desc_type=substring"
                    + "&long_desc=&long_desc_type=substring&bug_file_loc=" + "&bug_file_loc_type=substring&keywords="
                    + "&keywords_type=anywords&field0-0-0=noop" + "&type0-0-0=noop&value0-0-0=&cmdtype=doit"
                    + "&order=Reuse+same+sort+as+last+time", config.toString());
        }

        @Test
        public void testModifySampler() throws Exception {
            testProcessingHTMLFile("/testfiles/jmeter_home_page.html");
        }

        @Test
        public void testModifySamplerWithRelativeLink() throws Exception {
            testProcessingHTMLFile("/testfiles/jmeter_home_page_with_relative_links.html");
        }

        @Test
        public void testModifySamplerWithBaseHRef() throws Exception {
            testProcessingHTMLFile("/testfiles/jmeter_home_page_with_base_href.html");
        }

        @Test
        public void testNullSampler() {
            jmctx.setCurrentSampler(null);
            jmctx.setPreviousResult(new HTTPSampleResult());
            parser.process(); // should do nothing
        }

        @Test
        public void testNullResult() throws Exception {
            jmctx.setCurrentSampler(makeContext("http://www.apache.org/subdir/previous.html"));
            jmctx.setPreviousResult(null);
            parser.process(); // should do nothing
        }

        @Test
        public void testWrongResultClass() throws Exception {
            jmctx.setCurrentSampler(makeContext("http://www.apache.org/subdir/previous.html"));
            jmctx.setPreviousResult(new SampleResult());
            parser.process(); // should do nothing
        }

        @Test
        public void testSimpleParse() throws Exception {
            HTTPSamplerBase config = makeUrlConfig(".*/index\\.html");
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<a href=\"index.html\">Goto index page</a></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setSamplerData(context.toString());
            result.setURL(context.getUrl());
            jmctx.setPreviousResult(result);
            parser.process();
            assertEquals("http://www.apache.org/subdir/index.html", config.getUrl().toString());
        }

        // Test https works too
        @Test
        public void testSimpleParse1() throws Exception {
            HTTPSamplerBase config = makeUrlConfig(".*/index\\.html");
            config.setProtocol(HTTPConstants.PROTOCOL_HTTPS);
            config.setPort(HTTPConstants.DEFAULT_HTTPS_PORT);
            HTTPSamplerBase context = makeContext("https://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<a href=\"index.html\">Goto index page</a></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setSamplerData(context.toString());
            result.setURL(context.getUrl());
            jmctx.setPreviousResult(result);
            parser.process();
            assertEquals("https://www.apache.org/subdir/index.html", config.getUrl().toString());
        }

        @Test
        public void testSimpleParse2() throws Exception {
            HTTPSamplerBase config = makeUrlConfig("/index\\.html");
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<a href=\"/index.html\">Goto index page</a>" + "hfdfjiudfjdfjkjfkdjf"
                    + "<b>bold text</b><a href=lowerdir/index.html>lower</a>" + "</body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setURL(context.getUrl());
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            jmctx.setPreviousResult(result);
            parser.process();
            String newUrl = config.getUrl().toString();
            assertTrue("http://www.apache.org/index.html".equals(newUrl)
                    || "http://www.apache.org/subdir/lowerdir/index.html".equals(newUrl));
        }

        @Test
        public void testSimpleParse3() throws Exception {
            HTTPSamplerBase config = makeUrlConfig(".*index.*");
            config.getArguments().addArgument("param1", "value1");
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<a href=\"/home/index.html?param1=value1\">" + "Goto index page</a></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setURL(context.getUrl());
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            jmctx.setPreviousResult(result);
            parser.process();
            String newUrl = config.getUrl().toString();
            assertEquals("http://www.apache.org/home/index.html?param1=value1", newUrl);
        }

        @Test
        public void testSimpleParse4() throws Exception {
            HTTPSamplerBase config = makeUrlConfig("/subdir/index\\..*");
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<A HREF=\"index.html\">Goto index page</A></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setURL(context.getUrl());
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            jmctx.setPreviousResult(result);
            parser.process();
            String newUrl = config.getUrl().toString();
            assertEquals("http://www.apache.org/subdir/index.html", newUrl);
        }

        @Test
        public void testSimpleParse5() throws Exception {
            HTTPSamplerBase config = makeUrlConfig("/subdir/index\\.h.*");
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/one/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<a href=\"../index.html\">Goto index page</a></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setURL(context.getUrl());
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            jmctx.setPreviousResult(result);
            parser.process();
            String newUrl = config.getUrl().toString();
            assertEquals("http://www.apache.org/subdir/index.html", newUrl);
        }

        @Test
        public void testFailSimpleParse1() throws Exception {
            HTTPSamplerBase config = makeUrlConfig(".*index.*?param2=.+1");
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<a href=\"/home/index.html?param1=value1\">" + "Goto index page</a></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            String newUrl = config.getUrl().toString();
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setURL(context.getUrl());
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            jmctx.setPreviousResult(result);
            parser.process();
            assertEquals(newUrl, config.getUrl().toString());
        }

        @Test
        public void testFailSimpleParse3() throws Exception {
            HTTPSamplerBase config = makeUrlConfig("/home/index.html");
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<a href=\"/home/index.html?param1=value1\">" + "Goto index page</a></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            String newUrl = config.getUrl().toString();
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setURL(context.getUrl());
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            jmctx.setPreviousResult(result);
            parser.process();
            assertEquals(newUrl + "?param1=value1", config.getUrl().toString());
        }

        @Test
        public void testFailSimpleParse2() throws Exception {
            HTTPSamplerBase config = makeUrlConfig(".*login\\.html");
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<a href=\"/home/index.html?param1=value1\">" + "Goto index page</a></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setURL(context.getUrl());
            jmctx.setCurrentSampler(context);
            jmctx.setPreviousResult(result);
            parser.process();
            String newUrl = config.getUrl().toString();
            assertNotEquals("http://www.apache.org/home/index.html?param1=value1", newUrl);
            assertEquals(config.getUrl().toString(), newUrl);
        }

        @Test
        public void testSimpleFormParse() throws Exception {
            HTTPSamplerBase config = makeUrlConfig(".*index.html");
            config.addArgument("test", "g.*");
            config.setMethod(HTTPConstants.POST);
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<form action=\"index.html\" method=\"POST\">" + "<input type=\"checkbox\" name=\"test\""
                    + " value=\"goto\">Goto index page</form></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setURL(context.getUrl());
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            jmctx.setPreviousResult(result);
            parser.process();
            assertEquals("http://www.apache.org/subdir/index.html", config.getUrl().toString());
            assertEquals("test=goto", config.getQueryString());
        }

        @Test
        public void testBadCharParse() throws Exception {
            HTTPSamplerBase config = makeUrlConfig(".*index.html");
            config.addArgument("te$st", "g.*");
            config.setMethod(HTTPConstants.POST);
            HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
            String responseText = "<html><head><title>Test page</title></head><body>"
                    + "<form action=\"index.html\" method=\"POST\">" + "<input type=\"checkbox\" name=\"te$st\""
                    + " value=\"goto\">Goto index page</form></body></html>";
            HTTPSampleResult result = new HTTPSampleResult();
            result.setResponseData(responseText, null);
            result.setSampleLabel(context.toString());
            result.setURL(context.getUrl());
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            jmctx.setPreviousResult(result);
            parser.process();
            assertEquals("http://www.apache.org/subdir/index.html", config.getUrl().toString());
            assertEquals("te%24st=goto", config.getQueryString());
        }

        @Test
        public void testSpecialCharParse() throws Exception {
        String specialChars = "-_.!~*'()%25";// These are some of the special characters
        String htmlEncodedFixture = URLEncoder.encode(specialChars, StandardCharsets.UTF_8.name());

        HTTPSamplerBase config = makeUrlConfig(".*index.html");
        config.addArgument("test", ".*");
        config.setMethod(HTTPConstants.POST);
        HTTPSamplerBase context = makeContext("http://www.apache.org/subdir/previous.html");
        String responseText = "<html><head><title>Test page</title></head><body>"
            + "<form action=\"index.html\" method=\"POST\">" + "<input type=\"hidden\" name=\"test\""
            + " value=\"" + htmlEncodedFixture + "\">Goto index page</form></body></html>";

        HTTPSampleResult result = new HTTPSampleResult();
        result.setResponseData(responseText, null);
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        jmctx.setPreviousResult(result);
        parser.process();
        assertEquals("http://www.apache.org/subdir/index.html", config.getUrl().toString());
        assertEquals("test=" + htmlEncodedFixture, config.getQueryString());
      }


        private HTTPSamplerBase makeContext(String url) throws MalformedURLException {
            URL u = new URL(url);
            HTTPSamplerBase context = new HTTPNullSampler();
            context.setDomain(u.getHost());
            context.setPath(u.getPath());
            context.setPort(u.getPort());
            context.setProtocol(u.getProtocol());
            context.parseArguments(u.getQuery());
            return context;
        }

        private HTTPSamplerBase makeUrlConfig(String path) {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setDomain("www.apache.org");
            config.setMethod(HTTPConstants.GET);
            config.setPath(path);
            config.setPort(HTTPConstants.DEFAULT_HTTP_PORT);
            config.setProtocol(HTTPConstants.PROTOCOL_HTTP);
            return config;
        }
}
