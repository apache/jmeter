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
import static org.junit.Assert.assertNotNull;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.junit.Test;

public class TestHTTPSamplers {


    // Parse arguments singly
    @Test
    public void testParseArguments(){
        HTTPSamplerBase sampler = new HTTPNullSampler();
        Arguments args;
        Argument arg;

        args = sampler.getArguments();
        assertEquals(0,args.getArgumentCount());
        assertEquals(0,sampler.getHTTPFileCount());

        sampler.parseArguments("");
        args = sampler.getArguments();
        assertEquals(0,args.getArgumentCount());
        assertEquals(0,sampler.getHTTPFileCount());

        sampler.parseArguments("name1");
        args = sampler.getArguments();
        assertEquals(1,args.getArgumentCount());
        arg=args.getArgument(0);
        assertEquals("name1",arg.getName());
        assertEquals("",arg.getMetaData());
        assertEquals("",arg.getValue());
        assertEquals(0,sampler.getHTTPFileCount());

        sampler.parseArguments("name2=");
        args = sampler.getArguments();
        assertEquals(2,args.getArgumentCount());
        arg=args.getArgument(1);
        assertEquals("name2",arg.getName());
        assertEquals("=",arg.getMetaData());
        assertEquals("",arg.getValue());
        assertEquals(0,sampler.getHTTPFileCount());

        sampler.parseArguments("name3=value3");
        args = sampler.getArguments();
        assertEquals(3,args.getArgumentCount());
        arg=args.getArgument(2);
        assertEquals("name3",arg.getName());
        assertEquals("=",arg.getMetaData());
        assertEquals("value3",arg.getValue());
        assertEquals(0,sampler.getHTTPFileCount());
    }

    // Parse arguments all at once
    @Test
    public void testParseArguments2(){
        HTTPSamplerBase sampler = new HTTPNullSampler();
        Arguments args;
        Argument arg;

        args = sampler.getArguments();
        assertEquals(0,args.getArgumentCount());
        assertEquals(0,sampler.getHTTPFileCount());

        sampler.parseArguments("&name1&name2=&name3=value3");
        args = sampler.getArguments();
        assertEquals(3,args.getArgumentCount());
        assertEquals(0,sampler.getHTTPFileCount());

        arg=args.getArgument(0);
        assertEquals("name1",arg.getName());
        assertEquals("",arg.getMetaData());
        assertEquals("",arg.getValue());
        assertEquals(0,sampler.getHTTPFileCount());

        arg=args.getArgument(1);
        assertEquals("name2",arg.getName());
        assertEquals("=",arg.getMetaData());
        assertEquals("",arg.getValue());
        assertEquals(0,sampler.getHTTPFileCount());

        arg=args.getArgument(2);
        assertEquals("name3",arg.getName());
        assertEquals("=",arg.getMetaData());
        assertEquals("value3",arg.getValue());
        assertEquals(0,sampler.getHTTPFileCount());
    }

        @Test
        public void testArgumentWithoutEquals() throws Exception {
            HTTPSamplerBase sampler = new HTTPNullSampler();
            sampler.setProtocol("http");
            sampler.setMethod(HTTPConstants.GET);
            sampler.setPath("/index.html?pear");
            sampler.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?pear", sampler.getUrl().toString());
        }

        @Test
        public void testMakingUrl() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?param1=value1", config.getUrl().toString());
        }

        @Test
        public void testRedirect() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.setDomain("192.168.0.1");
            HTTPSampleResult res = new HTTPSampleResult();
            res.sampleStart();
            res.setURL(config.getUrl());
            res.setResponseCode("301");
            res.sampleEnd();

            res.setRedirectLocation("./");
            config.followRedirects(res , 0);
            assertEquals("http://192.168.0.1/", config.getUrl().toString());
            
            res.setRedirectLocation(".");
            config.followRedirects(res , 0);
            assertEquals("http://192.168.0.1/", config.getUrl().toString());
            
            res.setRedirectLocation("../");
            config.followRedirects(res , 0);
            assertEquals("http://192.168.0.1/", config.getUrl().toString());
        }

        @Test
        public void testMakingUrl2() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html?p1=p2");
            config.setDomain("192.168.0.1");
            assertEquals("http://192.168.0.1/index.html?param1=value1&p1=p2", config.getUrl().toString());
        }

        @Test
        public void testMakingUrl3() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.POST);
            config.addArgument("param1", "value1");
            config.setPath("/index.html?p1=p2");
            config.setDomain("192.168.0.1");
            assertEquals("http://192.168.0.1/index.html?p1=p2", config.getUrl().toString());
        }

        // test cases for making Url, and exercise method
        // addArgument(String name,String value,String metadata)

        @Test
        public void testMakingUrl4() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.addArgument("param1", "value1", "=");
            config.setPath("/index.html");
            config.setDomain("192.168.0.1");
            assertEquals("http://192.168.0.1/index.html?param1=value1", config.getUrl().toString());
        }

        @Test
        public void testMakingUrl5() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.addArgument("param1", "", "=");
            config.setPath("/index.html");
            config.setDomain("192.168.0.1");
            assertEquals("http://192.168.0.1/index.html?param1=", config.getUrl().toString());
        }

        @Test
        public void testMakingUrl6() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.addArgument("param1", "", "");
            config.setPath("/index.html");
            config.setDomain("192.168.0.1");
            assertEquals("http://192.168.0.1/index.html?param1", config.getUrl().toString());
        }

        // test cases for making Url, and exercise method
        // parseArguments(String queryString)

        @Test
        public void testMakingUrl7() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.parseArguments("param1=value1");
            config.setPath("/index.html");
            config.setDomain("192.168.0.1");
            assertEquals("http://192.168.0.1/index.html?param1=value1", config.getUrl().toString());
        }

        @Test
        public void testMakingUrl8() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.parseArguments("param1=");
            config.setPath("/index.html");
            config.setDomain("192.168.0.1");
            assertEquals("http://192.168.0.1/index.html?param1=", config.getUrl().toString());
        }

        @Test
        public void testMakingUrl9() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.parseArguments("param1");
            config.setPath("/index.html");
            config.setDomain("192.168.0.1");
            assertEquals("http://192.168.0.1/index.html?param1", config.getUrl().toString());
        }

        @Test
        public void testMakingUrl10() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPConstants.GET);
            config.parseArguments("");
            config.setPath("/index.html");
            config.setDomain("192.168.0.1");
            assertEquals("http://192.168.0.1/index.html", config.getUrl().toString());
        }
        
        @Test
        public void testFileList(){
            HTTPSamplerBase config = new HTTPNullSampler();
            HTTPFileArg[] arg;
            arg = config.getHTTPFiles();
            assertNotNull(arg);
            assertEquals(0,arg.length);

            config.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("","","")});
            arg = config.getHTTPFiles();
            assertNotNull(arg);
            assertEquals(0,arg.length);
            
            config.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("","","text/plain")});
            arg = config.getHTTPFiles();
            assertNotNull(arg);
            assertEquals(1,arg.length);
            assertEquals("text/plain",arg[0].getMimeType());
            assertEquals("",arg[0].getPath());
            assertEquals("",arg[0].getParamName());
            
            config.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("/tmp/test123.tmp","test123.tmp","text/plain")});
            arg = config.getHTTPFiles();
            assertNotNull(arg);
            assertEquals(1,arg.length);
            assertEquals("text/plain",arg[0].getMimeType());
            assertEquals("/tmp/test123.tmp",arg[0].getPath());
            assertEquals("test123.tmp",arg[0].getParamName());
            
            HTTPFileArg[] files = {};
            
            // Ignore empty file specs
            config.setHTTPFiles(files);
            arg = config.getHTTPFiles();
            assertNotNull(arg);
            assertEquals(0,arg.length);
            files = new HTTPFileArg[]{
                    new HTTPFileArg(),
                    new HTTPFileArg(),
                    };
            config.setHTTPFiles(files);
            arg = config.getHTTPFiles();
            assertNotNull(arg);
            assertEquals(0,arg.length);

            // Ignore trailing empty spec
            files = new HTTPFileArg[]{
                    new HTTPFileArg("file"),
                    new HTTPFileArg(),
                    };
            config.setHTTPFiles(files);
            arg = config.getHTTPFiles();
            assertNotNull(arg);
            assertEquals(1,arg.length);

            // Ignore leading empty spec
            files = new HTTPFileArg[]{
                    new HTTPFileArg(),
                    new HTTPFileArg("file1"),
                    new HTTPFileArg(),
                    new HTTPFileArg("file2"),
                    new HTTPFileArg(),
                    };
            config.setHTTPFiles(files);
            arg = config.getHTTPFiles();
            assertNotNull(arg);
            assertEquals(2,arg.length);
     }

    @Test
    public void testSetAndGetFileField() {
        HTTPSamplerBase sampler = new HTTPNullSampler();
        sampler.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("","param","")});
        HTTPFileArg file = sampler.getHTTPFiles()[0];
        assertEquals("param", file.getParamName());

        sampler.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("","param2","")});
        file = sampler.getHTTPFiles()[0];
        assertEquals("param2", file.getParamName());
}

    @Test
    public void testSetAndGetFilename() {
        HTTPSamplerBase sampler = new HTTPNullSampler();
        sampler.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("name","","")});
        HTTPFileArg file = sampler.getHTTPFiles()[0];
        assertEquals("name", file.getPath());

        sampler.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("name2","","")});
        file = sampler.getHTTPFiles()[0];
        assertEquals("name2", file.getPath());
    }

    @Test
    public void testSetAndGetMimetype() {
        HTTPSamplerBase sampler = new HTTPNullSampler();
        sampler.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("","","mime")});
        HTTPFileArg file = sampler.getHTTPFiles()[0];
        assertEquals("mime", file.getMimeType());

        sampler.setHTTPFiles(new HTTPFileArg[]{new HTTPFileArg("","","mime2")});
        file = sampler.getHTTPFiles()[0];
        assertEquals("mime2", file.getMimeType());
    }
}
