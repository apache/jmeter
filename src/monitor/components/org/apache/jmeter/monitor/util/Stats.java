// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
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
 */
package org.apache.jmeter.monitor.util;

import org.apache.jmeter.monitor.model.Connector;
import org.apache.jmeter.monitor.model.Status;

/**
 * 
 * Description:<p>
 * Stats is responsible for calculating the load and
 * health of a given server. It uses tomcat's status
 * servlet results. A schema was generated for the
 * XML output and JAXB was used to generate classes.
 * <p>
 * The equations are:<p>
 * memory weight = (int)(33 * (used/max))<br>
 * thread weight = (int)(67 * (current/max))<p>
 * The load factors are stored in the properties
 * files. Simply change the values in the properties
 * to change how load is calculated. The defaults
 * values are memory (33) and threads (67). The sum
 * of the factors must equal 100.
 */
public class Stats
{

	public static final int DEAD = 0;
	public static final int ACTIVE = 2;
	public static final int WARNING = 1;
	public static final int HEALTHY = 3;

	public static final int DEFAULT_MEMORY_FACTOR = 50;
	public static final int DEFAULT_THREAD_FACTOR = 50;
	public static final double HEALTHY_PER = 0.00;
	public static final double ACTIVE_PER = 0.25;
	public static final double WARNING_PER = 0.67;

	/**
	 * The method is responsible for taking a status
	 * object and calculating an int value from 1 to
	 * 100. We use a combination of free memory and
	 * free threads. Since memory is a bigger risk,
	 * it counts for 2/3.<p>
	 * @param stat
	 * @return calculated load value
	 */
	public static int calculateLoad(Status stat){
		if (stat != null){
			// equation for calculating the weight
			// w = (int)(33 * (used/max))
			long totMem = stat.getJvm().getMemory().getTotal();
			long usedMem = stat.getJvm().getMemory().getFree();
			double memdiv = (double)usedMem/(double)totMem;
			double memWeight = DEFAULT_MEMORY_FACTOR * memdiv;

			Connector cntr = (Connector)stat.getConnector().get(0);
			int maxThread = cntr.getThreadInfo().getMaxThreads();
			int curThread = cntr.getThreadInfo().getCurrentThreadsBusy();
			double thdiv = (double)curThread/(double)maxThread;
			double threadWeight = DEFAULT_THREAD_FACTOR * thdiv;
			return (int)(memWeight + threadWeight);
		} else {
			return 0;
		}
	}
	
	/**
	 * Method should calculate if the server is:
	 * dead, active, warning or healthy. We do
	 * this by looking at the current busy threads.
	 * <ol>
	 * <li> free &gt; spare is healthy
	 * <li> free &lt; spare is active
	 * <li> busy threads &gt; 75% is warning
	 * <li> none of the above is dead
	 * </ol>
	 * @param stat
	 * @return integer representing the status
	 */
	public static int calculateStatus(Status stat){
		if (stat != null){
			Connector cntr = (Connector)stat.getConnector().get(0);
			int max = cntr.getThreadInfo().getMaxThreads();
			int current = cntr.getThreadInfo().getCurrentThreadsBusy();
			//int spare = cntr.getThreadInfo().getMaxSpareThreads();
			double per = (double)current/(double)max;
			if (per > WARNING_PER){
				return WARNING;
			} else if (per >= ACTIVE_PER && per <= WARNING_PER){
				return ACTIVE;
			} else if (per < ACTIVE_PER && per > HEALTHY_PER){
				return HEALTHY;
			} else {
				return DEAD;
			}
		} else {
			return DEAD;
		}
	}

	/**
	 * Method will calculate the memory load:
	 * used / max = load. The load value is an
	 * integer between 1 and 100. It is the
	 * percent memory used.
	 * @param stat
	 * @return memory load
	 */	
	public static int calculateMemoryLoad(Status stat){
		double load = 0;
		if (stat != null){
			double total = (double)stat.getJvm().getMemory().getTotal();
			double used = (double)stat.getJvm().getMemory().getFree();
			load = (used/total);
		}
		return (int)(load * 100);
	}

	/**
	 * Method will calculate the thread load:
	 * busy / max = load. The value is an
	 * integer between 1 and 100. It is the
	 * percent busy.
	 * @param stat
	 * @return thread load
	 */	
	public static int calculateThreadLoad(Status stat){
		int load = 0;
		if (stat != null){
			Connector cntr = (Connector)stat.getConnector().get(0);
			double max = (double)cntr.getThreadInfo().getMaxThreads();
			double current =
				(double)cntr.getThreadInfo().getCurrentThreadsBusy();
			load = (int)((current/max) * 100);
		}
		return load;
	}
	
}
