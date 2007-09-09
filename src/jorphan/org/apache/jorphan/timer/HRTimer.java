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

package org.apache.jorphan.timer;

/**
 * A package-private implementation of {@link ITimer} based around native
 * <code>getTime</code> method. It will work on any platform for which a JNI
 * implementation of "hrtlib" library is available.
 * <P>
 * 
 * {@link TimerFactory} acts as the Factory for this class.
 * <P>
 * 
 * MT-safety: an instance of this class is safe to be used within the same
 * thread only.
 * 
 * @author <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>
 * @author Originally published in <a
 *         href="http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html">JavaWorld</a>
 * @version $Revision$
 */
final class HRTimer extends AbstractTimer {
	private static final String HRTIMER_LIB = "hrtlib";

	static {
		try {
			System.loadLibrary(HRTIMER_LIB);
		} catch (UnsatisfiedLinkError e) {
			System.out.println("native lib '" + HRTIMER_LIB + "' not found in 'java.library.path': "
					+ System.getProperty("java.library.path"));

			throw e; // re-throw
		}
	}

	/*
	 * This is supposed to return a fractional count of milliseconds elapsed
	 * since some indeterminate moment in the past. The exact starting point is
	 * not relevant because this timer class reports time differences only.
	 * 
	 * JNI code in HRTIMER_LIB library is supposed to implement this.
	 */
	private static native double getTime();

	protected double getCurrentTime() {
		return getTime();
	}
}
