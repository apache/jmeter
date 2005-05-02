/*
 * Copyright 2005 The Apache Software Foundation.
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

import org.apache.jmeter.util.JMeterUtils;

/**
 * Factory to return the appropriate HTTPSampler for use with classes
 * that need an HTTPSampler
 * 
 */
public class HTTPSamplerFactory {
	
	private static final String HTTP_SAMPLER = "HTTPSampler"; //$NON-NLS-1$
	private static final String HTTP_SAMPLER_APACHE = "HTTPSampler2"; //$NON-NLS-1$
	private static final String DEFAULT_CLASSNAME =
		JMeterUtils.getPropDefault("jmeter.httpsampler",HTTP_SAMPLER); //$NON-NLS-1$
	
	private HTTPSamplerFactory()
	{
		//Not intended to be instantiated
	}
	
	public static HTTPSamplerBase newInstance()
	{
		return newInstance(DEFAULT_CLASSNAME);
	}

	public static HTTPSamplerBase newInstance(String classname)
	{
		if (classname.equals(HTTP_SAMPLER))
		{
			return new HTTPSampler();
		}
		if (classname.equals(HTTP_SAMPLER_APACHE))
		{
			return new HTTPSampler2();
		}
		throw new UnsupportedOperationException("Cannot create class: "+classname);
	}
}
