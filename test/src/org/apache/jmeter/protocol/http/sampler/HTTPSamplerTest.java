/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jmeter.protocol.http.sampler;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 * 
 */
public class HTTPSamplerTest extends junit.framework.TestCase {

	public HTTPSamplerTest(String name) {
        super(name);
    }

		public void testArgumentWithoutEquals() throws Exception {
			HTTPSampler sampler = new HTTPSampler();
			sampler.setProtocol("http");
			sampler.setMethod(HTTPSampler.GET);
			sampler.setPath("/index.html?pear");
			sampler.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?pear", sampler.getUrl().toString());
		}

		public void testMakingUrl() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.GET);
			config.addArgument("param1", "value1");
			config.setPath("/index.html");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?param1=value1", config.getUrl().toString());
		}

		public void testMakingUrl2() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.GET);
			config.addArgument("param1", "value1");
			config.setPath("/index.html?p1=p2");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?param1=value1&p1=p2", config.getUrl().toString());
		}

		public void testMakingUrl3() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.POST);
			config.addArgument("param1", "value1");
			config.setPath("/index.html?p1=p2");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?p1=p2", config.getUrl().toString());
		}

		// test cases for making Url, and exercise method
		// addArgument(String name,String value,String metadata)

		public void testMakingUrl4() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.GET);
			config.addArgument("param1", "value1", "=");
			config.setPath("/index.html");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?param1=value1", config.getUrl().toString());
		}

		public void testMakingUrl5() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.GET);
			config.addArgument("param1", "", "=");
			config.setPath("/index.html");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?param1=", config.getUrl().toString());
		}

		public void testMakingUrl6() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.GET);
			config.addArgument("param1", "", "");
			config.setPath("/index.html");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?param1", config.getUrl().toString());
		}

		// test cases for making Url, and exercise method
		// parseArguments(String queryString)

		public void testMakingUrl7() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.GET);
			config.parseArguments("param1=value1");
			config.setPath("/index.html");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?param1=value1", config.getUrl().toString());
		}

		public void testMakingUrl8() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.GET);
			config.parseArguments("param1=");
			config.setPath("/index.html");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?param1=", config.getUrl().toString());
		}

		public void testMakingUrl9() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.GET);
			config.parseArguments("param1");
			config.setPath("/index.html");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html?param1", config.getUrl().toString());
		}

		public void testMakingUrl10() throws Exception {
			HTTPSampler config = new HTTPSampler();
			config.setProtocol("http");
			config.setMethod(HTTPSampler.GET);
			config.parseArguments("");
			config.setPath("/index.html");
			config.setDomain("www.apache.org");
			assertEquals("http://www.apache.org/index.html", config.getUrl().toString());
		}
}
