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

package org.apache.jmeter.samplers;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Packages information regarding the target of a sample event, such as the
 * result from that event and the thread group it ran in.
 */
public class SampleEvent implements Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String HOSTNAME;

	static {
        String hn="";
		try {
			hn = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
            log.error("Cannot obtain local host name "+e);
		}
        HOSTNAME=hn;
	}

	SampleResult result;

	String threadGroup; // TODO appears to duplicate the threadName field in SampleResult

	String hostname;

	public SampleEvent() {
	}

	public SampleEvent(SampleResult result, String threadGroup) {
		this.result = result;
		this.threadGroup = threadGroup;
		this.hostname = HOSTNAME;
	}

	/**
	 * Only intended for use when loading results from a file.
	 * 
	 * @param result
	 * @param threadGroup
	 * @param hostname
	 */
	public SampleEvent(SampleResult result, String threadGroup, String hostname) {
		this.result = result;
		this.threadGroup = threadGroup;
		this.hostname = hostname;
	}

	public SampleResult getResult() {
		return result;
	}

	public String getThreadGroup() {
		return threadGroup;
	}

	public String getHostname() {
		return hostname;
	}
}
