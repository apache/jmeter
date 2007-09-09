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

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;

import junit.framework.TestCase;

public class TestHTTPSamplers extends TestCase {

    public TestHTTPSamplers(String arg0) {
        super(arg0);
    }

    // Parse arguments singly
    public void testParseArguments(){
        HTTPSamplerBase sampler = new HTTPNullSampler();
        Arguments args;
        Argument arg;
        
        args = sampler.getArguments();
        assertEquals(0,args.getArgumentCount());
        
        sampler.parseArguments("");
        args = sampler.getArguments();
        assertEquals(0,args.getArgumentCount());
        
        sampler.parseArguments("name1");
        args = sampler.getArguments();
        assertEquals(1,args.getArgumentCount());
        arg=args.getArgument(0);
        assertEquals("name1",arg.getName());
        assertEquals("",arg.getMetaData());
        assertEquals("",arg.getValue());
        
        sampler.parseArguments("name2=");
        args = sampler.getArguments();
        assertEquals(2,args.getArgumentCount());
        arg=args.getArgument(1);
        assertEquals("name2",arg.getName());
        assertEquals("=",arg.getMetaData());
        assertEquals("",arg.getValue());
        
        sampler.parseArguments("name3=value3");
        args = sampler.getArguments();
        assertEquals(3,args.getArgumentCount());
        arg=args.getArgument(2);
        assertEquals("name3",arg.getName());
        assertEquals("=",arg.getMetaData());
        assertEquals("value3",arg.getValue());
        
    }

    // Parse arguments all at once
    public void testParseArguments2(){
        HTTPSamplerBase sampler = new HTTPNullSampler();
        Arguments args;
        Argument arg;
        
        args = sampler.getArguments();
        assertEquals(0,args.getArgumentCount());
        
        sampler.parseArguments("&name1&name2=&name3=value3");
        args = sampler.getArguments();
        assertEquals(3,args.getArgumentCount());
        
        arg=args.getArgument(0);
        assertEquals("name1",arg.getName());
        assertEquals("",arg.getMetaData());
        assertEquals("",arg.getValue());
        
        arg=args.getArgument(1);
        assertEquals("name2",arg.getName());
        assertEquals("=",arg.getMetaData());
        assertEquals("",arg.getValue());
        
        arg=args.getArgument(2);
        assertEquals("name3",arg.getName());
        assertEquals("=",arg.getMetaData());
        assertEquals("value3",arg.getValue());
        
    }

        public void testArgumentWithoutEquals() throws Exception {
            HTTPSamplerBase sampler = new HTTPNullSampler();
            sampler.setProtocol("http");
            sampler.setMethod(HTTPSamplerBase.GET);
            sampler.setPath("/index.html?pear");
            sampler.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?pear", sampler.getUrl().toString());
        }

        public void testMakingUrl() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?param1=value1", config.getUrl().toString());
        }

        public void testMakingUrl2() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html?p1=p2");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?param1=value1&p1=p2", config.getUrl().toString());
        }

        public void testMakingUrl3() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.POST);
            config.addArgument("param1", "value1");
            config.setPath("/index.html?p1=p2");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?p1=p2", config.getUrl().toString());
        }

        // test cases for making Url, and exercise method
        // addArgument(String name,String value,String metadata)

        public void testMakingUrl4() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.GET);
            config.addArgument("param1", "value1", "=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?param1=value1", config.getUrl().toString());
        }

        public void testMakingUrl5() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.GET);
            config.addArgument("param1", "", "=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?param1=", config.getUrl().toString());
        }

        public void testMakingUrl6() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.GET);
            config.addArgument("param1", "", "");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?param1", config.getUrl().toString());
        }

        // test cases for making Url, and exercise method
        // parseArguments(String queryString)

        public void testMakingUrl7() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.GET);
            config.parseArguments("param1=value1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?param1=value1", config.getUrl().toString());
        }

        public void testMakingUrl8() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.GET);
            config.parseArguments("param1=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?param1=", config.getUrl().toString());
        }

        public void testMakingUrl9() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.GET);
            config.parseArguments("param1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html?param1", config.getUrl().toString());
        }

        public void testMakingUrl10() throws Exception {
            HTTPSamplerBase config = new HTTPNullSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSamplerBase.GET);
            config.parseArguments("");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals("http://www.apache.org/index.html", config.getUrl().toString());
        }
}
