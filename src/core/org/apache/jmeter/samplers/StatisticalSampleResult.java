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

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Lars Krog-Jensen
 *         Created: 2005-okt-04
 */
public class StatisticalSampleResult extends SampleResult implements
		Serializable {
    private static final Logger log = LoggingManager.getLoggerForClass();
    
	private int errorCount;

    public StatisticalSampleResult(){
       log.warn("Constructor only intended for use in testing"); // $NON-NLS-1$
    }
    
	public StatisticalSampleResult(SampleResult res) {
		// Copy data that is shared between samples:
		setSampleLabel(res.getSampleLabel());
		setThreadName(res.getThreadName());
		setSuccessful(true); // Assume result is OK
		setSampleCount(0); // because we add the sample count in later
	}

	public void add(SampleResult res) {
		// Add Sample Counter
		setSampleCount(getSampleCount() + res.getSampleCount());

		setBytes(getBytes() + res.getBytes());

		// Add Error Counter
		if (!res.isSuccessful()) {
			errorCount++;
		}

		// Set start/end times
        if (getStartTime()==0){ // Bug 40954 - ensure start time gets started!
            this.setStartTime(res.getStartTime());
        } else {
		    this.setStartTime(Math.min(getStartTime(), res.getStartTime()));
        }
		this.setEndTime(Math.max(getEndTime(), res.getEndTime()));
		
		setLatency(getLatency()+ res.getLatency());

	}

	public long getTime() {
		return getEndTime() - getStartTime() - this.getIdleTime();
	}

	public long getTimeStamp() {
		return getEndTime();
	}

	public int getErrorCount() {
		return errorCount;
	}

	public static String getKey(SampleEvent event) {
		String key = event.getResult().getSampleLabel() + "-"
				+ event.getThreadGroup();

		return key;
	}
}
