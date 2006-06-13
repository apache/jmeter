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
 * 
 */

package org.apache.jmeter.protocol.http.sampler;

import java.net.URL;

/**
 * Dummy HTTPSampler class for use by classes that need an HTTPSampler, but that
 * don't need an actual sampler, e.g. for Parsing testing.
 */
public final class HTTPNullSampler extends HTTPSamplerBase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase#sample(java.net.URL,
	 *      java.lang.String, boolean, int)
	 */
	protected HTTPSampleResult sample(URL u, String s, boolean b, int i) {
		throw new UnsupportedOperationException("For test purposes only");
	}

}
