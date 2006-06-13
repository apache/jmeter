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
 * A package-private collection of constants used by {@link ITimer}
 * implementations in <code>HRTimer</code> and <code>JavaSystemTimer</code>
 * classes.
 * 
 * @author <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>
 * @author Originally published in <a
 *         href="http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html">JavaWorld</a>
 * @version $Revision$
 */
interface ITimerConstants {
	/**
	 * Timer state enumeration.
	 */
	static final int STATE_READY = 0, STATE_STARTED = 1, STATE_STOPPED = 2;

	/**
	 * User-friendly timer state names indexed by their state values.
	 */
	static final String[] STATE_NAMES = { "READY", "STARTED", "STOPPED" };
}
