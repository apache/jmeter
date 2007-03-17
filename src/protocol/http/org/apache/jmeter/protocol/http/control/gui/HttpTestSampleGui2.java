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

package org.apache.jmeter.protocol.http.control.gui;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * HTTP Sampler GUI for Apache HTTPClient HTTP implementation
 */
public class HttpTestSampleGui2 extends HttpTestSampleGui {

	public HttpTestSampleGui2() {
		super.init();
	}

	public TestElement createTestElement() {
		HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance(HTTPSamplerFactory.HTTP_SAMPLER_APACHE);
		modifyTestElement(sampler);
		return sampler;
	}

	public String getStaticLabel() {
		return JMeterUtils.getResString("web_testing2_title");
	}

    // Documentation is shared with our parent
    public String getDocAnchor() {
        return super.getStaticLabel().replace(' ', '_'); // $NON-NLS-1$  // $NON-NLS-2$
    }

}
