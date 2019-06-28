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

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPNullSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.NullSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.junit.Before;
import org.junit.Test;

public class TestURLRewritingModifier extends JMeterTestCase {
        private SampleResult response = null;

        private JMeterContext context = null;

        private URLRewritingModifier mod = null;


        @Before
        public void setUp() {
            context = JMeterContextService.getContext();
            mod = new URLRewritingModifier();
            mod.setThreadContext(context);
        }

        @Test
        public void testNonHTTPSampler() throws Exception {
            Sampler sampler = new NullSampler();
            response = new SampleResult();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
        }

        @Test
        public void testGrabSessionId() throws Exception {
            String html = "location: http://server.com/index.html" + "?session_id=jfdkjdkf%20jddkfdfjkdjfdf%22;";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("session_id");
            HTTPSamplerBase sampler = createSampler();
            sampler.addArgument("session_id", "adfasdfdsafasdfasd");
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("jfdkjdkf jddkfdfjkdjfdf\"", ((Argument) args.getArguments().get(0).getObjectValue())
                    .getValue());
            assertEquals("http://server.com/index.html?" + "session_id=jfdkjdkf+jddkfdfjkdjfdf%22", sampler.toString());
        }

        @Test
        public void testGrabSessionId2() throws Exception {
            String html = "<a href=\"http://server.com/index.html?" + "session_id=jfdkjdkfjddkfdfjkdjfdf\">";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("session_id");
            HTTPSamplerBase sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("jfdkjdkfjddkfdfjkdjfdf", ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
        }

        private HTTPSamplerBase createSampler() {
            HTTPSamplerBase sampler = new HTTPNullSampler();
            sampler.setDomain("server.com");
            sampler.setPath("index.html");
            sampler.setMethod(HTTPConstants.GET);
            sampler.setProtocol("http");
            return sampler;
        }

        @Test
        public void testGrabSessionId3() throws Exception {
            String html = "href='index.html?session_id=jfdkjdkfjddkfdfjkdjfdf'";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("session_id");
            HTTPSamplerBase sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("jfdkjdkfjddkfdfjkdjfdf", ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
        }

        @Test
        public void testGrabSessionId6() throws Exception {
            String html = "location: http://server.com/index.html" + "?session_id=bonjour+monsieur";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("session_id");
            HTTPSamplerBase sampler = createSampler();
            sampler.addArgument("session_id", "adfasdfdsafasdfasd");
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("bonjour monsieur", ((Argument) args.getArguments().get(0).getObjectValue())
                    .getValue());
            assertEquals("http://server.com/index.html?" + "session_id=bonjour+monsieur", sampler.toString());
        }

        @Test
        public void testGrabSessionId7() throws Exception {
            String html = "location: http://server.com/index.html" + "?session_id=bonjour+monsieur";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("session_id");
            mod.setEncode(true);
            HTTPSamplerBase sampler = createSampler();
            sampler.addArgument("session_id", "adfasdfdsafasdfasd");
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("bonjour+monsieur", ((Argument) args.getArguments().get(0).getObjectValue())
                    .getValue());
            assertEquals("http://server.com/index.html?" + "session_id=bonjour%2Bmonsieur", sampler.toString());
        }

        @Test
        public void testGrabSessionIdFromXMLNonPatExtension() throws Exception { // Bug 50286
            String html = "<url>/some/path;jsessionid=123456789</url>";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("jsessionid");
            HTTPSamplerBase sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("123456789", ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
        }

        @Test
        public void testGrabSessionIdFromXMLPatExtension() throws Exception { // Bug 50286
            String html = "<url>/some/path;jsessionid=123456789</url>";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("jsessionid");
            mod.setPathExtension(true);
            HTTPSamplerBase sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            assertEquals("index.html;jsessionid=123456789",sampler.getPath());
        }

        @Test
        public void testBug61314() throws Exception { // Bug 50286
            String html = "<url>/some/path;jsessionid=123456789</url>";

            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("jsessionid");
            mod.setPathExtension(true);
            HTTPSamplerBase sampler = createSampler();
            sampler.setPath("/urlRewrite/index;jsessionid=657CF77A86183868CF30AC36321394B7");
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            assertEquals("/urlRewrite/index;jsessionid=123456789",sampler.getPath());
        }

        @Test
        public void testBug61314WithQuestionMark() throws Exception { // Bug 50286
            String html = "<url>/some/path;jsessionid=123456789</url>";

            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("jsessionid");
            mod.setPathExtension(true);
            HTTPSamplerBase sampler = createSampler();
            sampler.setPath("/urlRewrite/index;jsessionid=657CF77A86183868CF30AC36321394B7?toto=titi");
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            assertEquals("/urlRewrite/index;jsessionid=123456789",sampler.getPath());
        }

        @Test
        public void testGrabSessionIdEndedInTab() throws Exception {
            String html = "href='index.html?session_id=jfdkjdkfjddkfdfjkdjfdf\t";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("session_id");
            HTTPSamplerBase sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("jfdkjdkfjddkfdfjkdjfdf", ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
        }

        @Test
        public void testGrabSessionId4() throws Exception {
            String html = "href='index.html;%24sid%24KQNq3AAADQZoEQAxlkX8uQV5bjqVBPbT'";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("%24sid%24"); // $sid$
            mod.setPathExtension(true);
            mod.setPathExtensionNoEquals(true);
            HTTPSamplerBase sampler = createSampler();
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            assertEquals("index.html;%24sid%24KQNq3AAADQZoEQAxlkX8uQV5bjqVBPbT", sampler.getPath());
        }

        @Test
        public void testGrabSessionId5() throws Exception {
            String html = "location: http://server.com/index.html" + "?session[33]=jfdkjdkf%20jddkfdfjkdjfdf%22;";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("session[33]");
            HTTPSamplerBase sampler = createSampler();
            sampler.addArgument("session[33]", "adfasdfdsafasdfasd");
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("jfdkjdkf jddkfdfjkdjfdf\"", ((Argument) args.getArguments().get(0).getObjectValue())
                    .getValue());
            assertEquals("http://server.com/index.html?session%5B33%5D=jfdkjdkf+jddkfdfjkdjfdf%22", sampler.toString());
        }


        @Test
        public void testGrabSessionIdFromForm() throws Exception {
            String[] html = new String[] {
                    "<input name=\"sid\" value=\"myId\">",
                    "<input name='sid' value='myId'>",
                    "<input value=\"myId\" NAME='sid'>",
                    "<input VALUE='myId' name=\"sid\">",
                    "<input blah blah value=\"myId\" yoda yoda NAME='sid'>",
                    "<input type=\"HIDDEN\" name=\"sid\"      value=\"myId\">",
                    "<input type=\"HIDDEN\" name=\"sid\"\tvalue=\"myId\">",
                    };
            for (int i = 0; i < html.length; i++) {
                response = new SampleResult();
                response.setResponseData(html[i], null);
                URLRewritingModifier newMod = new URLRewritingModifier();
                newMod.setThreadContext(context);
                newMod.setArgumentName("sid");
                newMod.setPathExtension(false);
                HTTPSamplerBase sampler = createSampler();
                context.setCurrentSampler(sampler);
                context.setPreviousResult(response);
                newMod.process();
                Arguments args = sampler.getArguments();
                assertEquals("For case i=" + i, "myId",
                        ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
            }
        }

    @Test
    public void testGrabSessionIdURLinJSON() throws Exception {
            String html =
                "<a href=\"#\" onclick=\"$(\'frame\').src=\'/index?param1=bla&sessionid=xyzxyzxyz\\'";
            response = new SampleResult();
            response.setResponseData(html, null);
            mod.setArgumentName("sessionid");
            HTTPSamplerBase sampler = createSampler();
            sampler.addArgument("sessionid", "xyzxyzxyz");
            context.setCurrentSampler(sampler);
            context.setPreviousResult(response);
            mod.process();
            Arguments args = sampler.getArguments();
            assertEquals("xyzxyzxyz", ((Argument) args.getArguments().get(0).getObjectValue())
                    .getValue());
        }

        @Test
        public void testCache() throws Exception {
            String[] html = new String[] {
                    "<input name=\"sid\" value=\"myId\">",
                    "<html></html>", // No entry; check it is still present
                    };
            URLRewritingModifier newMod = new URLRewritingModifier();
            newMod.setShouldCache(true);
            newMod.setThreadContext(context);
            newMod.setArgumentName("sid");
            newMod.setPathExtension(false);
            for (int i = 0; i < html.length; i++) {
                response = new SampleResult();
                response.setResponseData(html[i], null);
                HTTPSamplerBase sampler = createSampler();
                context.setCurrentSampler(sampler);
                context.setPreviousResult(response);
                newMod.process();
                Arguments args = sampler.getArguments();
                assertEquals("For case i=" + i, "myId",
                        ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
            }
        }
        @Test
        public void testNoCache() throws Exception {
            String[] html = new String[] {
                    "<input name=\"sid\" value=\"myId\">",  "myId",
                    "<html></html>", "",
                    };
            URLRewritingModifier newMod = new URLRewritingModifier();
            newMod.setThreadContext(context);
            newMod.setArgumentName("sid");
            newMod.setPathExtension(false);
            newMod.setShouldCache(false);
            for (int i = 0; i < html.length/2; i++) {
                response = new SampleResult();
                response.setResponseData(html[i*2], null);
                HTTPSamplerBase sampler = createSampler();
                context.setCurrentSampler(sampler);
                context.setPreviousResult(response);
                newMod.process();
                Arguments args = sampler.getArguments();
                assertEquals("For case i=" + i, html[i*2+1],
                        ((Argument) args.getArguments().get(0).getObjectValue()).getValue());
            }
        }
}
