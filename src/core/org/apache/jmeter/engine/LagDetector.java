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

package org.apache.jmeter.engine;

/**
 * TODO - appears to be unused
 * 
 * @version $Revision$
 */
public class LagDetector extends Thread {
	private long incr = MAX_SLEEP / DIV;

	private long totalLag;

	private int count;

	private boolean running;

	public final static long MAX_SLEEP = 500;

	private final static long DIV = 3;

	/**
	 * Constructor for the LagDetector object.
	 */
	public LagDetector() {
	}

	/**
	 * Gets the AveLag attribute of the LagDetector object.
	 * 
	 * @return the AveLag value
	 */
	public float getAveLag() {
		return ((float) totalLag / (float) count);
	}

	/**
	 * Gets the LagRatio attribute of the LagDetector object.
	 * 
	 * @return the LagRatio value
	 */
	public float getLagRatio() {
		return ((float) totalLag / (float) count) / incr;
	}

	public void stopRunning() {
		running = false;
	}

	/**
	 * Main processing method for the LagDetector object.
	 */
	public void run() {
		running = true;
		long time;
		totalLag = 0;
		count = 0;
		while (running) {
			time = System.currentTimeMillis();
			try {
				Thread.sleep(incr);
			} catch (InterruptedException e) {
			}
			time = System.currentTimeMillis() - time;
			totalLag += time - incr;
			count++;
		}
	}
}
