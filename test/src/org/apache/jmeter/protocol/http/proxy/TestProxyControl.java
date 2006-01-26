/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
 * 
 */

package org.apache.jmeter.protocol.http.proxy;

import junit.framework.TestCase;

import org.apache.jmeter.protocol.http.sampler.HTTPNullSampler;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;

public class TestProxyControl  extends TestCase {
		HTTPSamplerBase sampler;

		ProxyControl control;

		public TestProxyControl(String name) {
			super(name);
		}

		public void setUp() {
			control = new ProxyControl();
			control.addIncludedPattern(".*\\.jsp");
			control.addExcludedPattern(".*apache.org.*");
			sampler = new HTTPNullSampler();
		}

		public void testFilter1() throws Exception {
			sampler.setDomain("jakarta.org");
			sampler.setPath("index.jsp");
			assertTrue("Should find jakarta.org/index.jsp", control.filterUrl(sampler));
		}

		public void testFilter2() throws Exception {
			sampler.setPath("index.jsp");
			sampler.setDomain("www.apache.org");
			assertFalse("Should not match www.apache.org", control.filterUrl(sampler));
		}

		public void testFilter3() throws Exception {
			sampler.setPath("header.gif");
			sampler.setDomain("jakarta.org");
			assertFalse("Should not match header.gif", control.filterUrl(sampler));
		}
}
