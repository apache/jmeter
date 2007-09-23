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

/**
 * Aggregates sample results for use by the Statistical remote batch mode.
 * Samples are aggregated by the key defined by getKey().
 * TODO: merge error count into parent class? 
 */
public class StatisticalSampleResult extends SampleResult implements
		Serializable {
    
	private static final long serialVersionUID = 24L;

	private int errorCount;

    public StatisticalSampleResult(){// May be called by XStream
    }
    
	/**
	 * Allow OldSaveService to generate a suitable result when sample/error counts have been saved.
	 * 
	 * @deprecated Needs to be replaced when multiple sample results are sorted out
	 * 
	 * @param stamp
	 * @param elapsed
	 */
	public StatisticalSampleResult(long stamp, long elapsed) {
		super(stamp, elapsed);
	}

	public StatisticalSampleResult(SampleResult res) {
		// Copy data that is shared between samples (i.e. the key items):
		setSampleLabel(res.getSampleLabel());
		// Nothing else can be saved, as the samples may come from any thread

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
			this.setSuccessful(false);
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

	public int getErrorCount() {// Overrides SampleResult
		return errorCount;
	}

	public void setErrorCount(int e) {// for reading CSV files
		errorCount = e;
	}

	/**
	 * Generates the key to be used for aggregating samples as follows:<br/>
	 * <code>sampleLabel</code> "-" <code>threadGroup</code>
	 * 
	 * N.B. the key should agree with the fixed items that are saved in the sample.
	 * 
	 * @param event sample event whose key is to be calculated
	 * @return the key to use for aggregating samples
	 */
	public static String getKey(SampleEvent event) {
		StringBuffer sb = new StringBuffer(80);
		sb.append(event.getResult().getSampleLabel()).append("-").append(event.getThreadGroup());
		return sb.toString();
	}
}
