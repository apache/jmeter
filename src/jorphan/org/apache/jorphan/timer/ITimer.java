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
 * A simple interface for measuring time intervals. An instance of this goes
 * through the following lifecycle states:
 * <dl>
 * <dt><em>ready</em></dt>
 * <dd>timer is ready to start a new measurement</dd>
 * <dt><em>started</em></dt>
 * <dd>timer has recorded the starting time interval point</dd>
 * <dt><em>stopped</em></dt>
 * <dd>timer has recorded the ending time interval point</dd>
 * </dl>
 * See individual methods for details.
 * <p>
 * If this library has been compiled with
 * {@link ITimerConstants#DO_STATE_CHECKS} set to 'true' the implementation will
 * enforce this lifecycle model and throw IllegalStateException when it is
 * violated.
 * 
 * @author <a href="mailto:vroubtsov@illinoisalumni.org">Vlad Roubtsov</a>
 * @author Originally published in <a
 *         href="http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html">JavaWorld</a>
 * @version $Revision$
 */
public interface ITimer {
	/**
	 * Starts a new time interval and advances this timer instance to 'started'
	 * state. This method can be called from 'ready' state only.
	 */
	void start();

	/**
	 * Terminates the current time interval and advances this timer instance to
	 * 'stopped' state. Interval duration will be available via
	 * {@link #getDuration()} method. This method can be called from 'started'
	 * state only.
	 */
	void stop();

	/**
	 * Returns the duration of the time interval that elapsed between the last
	 * calls to {@link #start()} and {@link #stop()}. This method can be called
	 * any number of times from 'stopped' state and will return the same value
	 * each time.
	 * 
	 * @return interval duration in milliseconds
	 */
	double getDuration();

	/**
	 * This method can be called from any state and will reset this timer
	 * instance back to 'ready' state.
	 */
	void reset();

}
