// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

/*
 * Created on Apr 30, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.junit.stubs;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;

/**
 * @author ano ano
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TestSampler extends AbstractSampler {

	private long wait = 0;

	private long samples = 0; // number of samples taken

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.Sampler#sample(org.apache.jmeter.samplers.Entry)
	 */
	public SampleResult sample(Entry e) {
		if (wait > 0) {
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e1) {
				// ignore
			}
		}
		samples++;
		return null;
	}

	public TestSampler(String name, long wait) {
		setName(name);
		this.wait = wait;
	}

	public TestSampler(String name) {
		setName(name);
	}

	public TestSampler() {
	}

	public String toString() {
		return getName();
	}

	public long getSamples() {
		return samples;
	}
}
