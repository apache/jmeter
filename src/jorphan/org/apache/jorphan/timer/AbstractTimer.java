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
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Revision$
 */
public abstract class AbstractTimer implements ITimer, ITimerConstants {
	/** Used to keep track of timer state. */
	private int m_state;

	/** Timing data. */
	private double m_data;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jorphan.timer.ITimer#start()
	 */
	public void start() {
		if (m_state != STATE_READY) {
			throw new IllegalStateException(this + ": start() must be called from READY state, " + "current state is "
					+ STATE_NAMES[m_state]);
		}

		m_state = STATE_STARTED;
		m_data = getCurrentTime();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jorphan.timer.ITimer#stop()
	 */
	public void stop() {
		// Latch stop time in a local var before doing anything else
		final double data = getCurrentTime();

		if (m_state != STATE_STARTED) {
			throw new IllegalStateException(this + ": stop() must be called from STARTED state, " + "current state is "
					+ STATE_NAMES[m_state]);
		}

		m_data = data - m_data;
		m_state = STATE_STOPPED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jorphan.timer.ITimer#getDuration()
	 */
	public double getDuration() {
		if (m_state != STATE_STOPPED) {
			throw new IllegalStateException(this + ": getDuration() must be called from STOPPED state, "
					+ "current state is " + STATE_NAMES[m_state]);
		}
		return m_data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jorphan.timer.ITimer#reset()
	 */
	public void reset() {
		m_state = STATE_READY;
	}

	protected abstract double getCurrentTime();
}
