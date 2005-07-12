// $Header$
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

package org.apache.jmeter.timers;

import java.io.Serializable;

import org.apache.jmeter.util.JMeterUtils;

/**
 * This class implements those methods needed by RandomTimer to be instantiable
 * and implements a random delay with an average value and a uniformly
 * distributed variation.
 * 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision$ $Date$
 */
public class UniformRandomTimer extends RandomTimer implements Serializable {
	public long delay() {
		return (long) Math.abs((this.random.nextDouble() * getRange()) + super.delay());
	}

	public String toString() {
		return JMeterUtils.getResString("uniform_timer_memo");
	}

}
