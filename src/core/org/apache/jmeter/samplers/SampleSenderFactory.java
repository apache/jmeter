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

package org.apache.jmeter.samplers;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.samplers.StatisticalSampleSender;

/**
 * @author Michael Freeman
 * 10/24/2005 - added statistical mode for distributed testing
 */
public class SampleSenderFactory {
	/**
	 * Checks for the Jmeter property mode and returns the required class.
	 * 
	 * @param listener
	 * @return the appropriate class. Standard Jmeter functionality,
	 *         hold_samples until end of test or batch samples.
	 */
	static SampleSender getInstance(RemoteSampleListener listener) {
		// Support original property name
		boolean holdSamples = JMeterUtils.getPropDefault("hold_samples", false);

		// Extended property name
		String type = JMeterUtils.getPropDefault("mode", "Standard");

		if (holdSamples || type.equalsIgnoreCase("Hold")) {
			HoldSampleSender h = new HoldSampleSender(listener);
			return h;
		} else if (type.equalsIgnoreCase("Batch")) {
			BatchSampleSender b = new BatchSampleSender(listener);
			return b;
		} else if (type.equalsIgnoreCase("Statistical")) {
			StatisticalSampleSender s = new StatisticalSampleSender(listener);
			return s;
		} else {
			StandardSampleSender s = new StandardSampleSender(listener);
			return s;
		}
	}
}
