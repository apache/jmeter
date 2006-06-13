// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

package org.apache.jorphan.timer;

/**
 * This non-instantiable non-extendible class acts as a Factory for
 * {@link ITimer} implementations.
 * 
 * @author <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>
 * @author Originally published in <a
 *         href="http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html">JavaWorld</a>
 * @version $Revision$
 */
public final class TimerFactory {
	/**
	 * Creates a new instance of {@link ITimer} which is returned in 'ready'
	 * state. If the JNI-based/high-resolution implementation is not available
	 * this will return an instance of <code>JavaSystemTimer</code>, so this
	 * method is guaranteed not to fail.
	 * 
	 * @return ITimer a new timer instance in 'ready' state [never null]
	 */
	public static ITimer newTimer() {
		try {
			return new HRTimer();
		} catch (Throwable t) {
			return new JavaSystemTimer();
		}
	}

	/**
	 * Private default constructor to prevent instantiation.
	 */
	private TimerFactory() {
	}
}