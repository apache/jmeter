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

package org.apache.jmeter.util;

import org.apache.jmeter.samplers.SampleResult;

/**
 * Class to calculate various items that don't require all previous results to be saved:
 * - mean = average
 * - standard deviation
 * - minimum
 * - maximum
 */
public class Calculator {

	private double sum = 0;

	private double sumOfSquares = 0;

	private double mean = 0;

	private double deviation = 0;

	private int count = 0;

	private long bytes = 0;
	
	private long maximum = Long.MIN_VALUE;
	
	private long minimum = Long.MAX_VALUE;
    
    private int errors = 0;
	
    private final String label;
    
    public Calculator() {
        this("");
    }

	public Calculator(String label) {
        this.label = label;
    }

    public void clear() {
		maximum = Long.MIN_VALUE;
		minimum = Long.MAX_VALUE;
		sum = 0;
		sumOfSquares = 0;
		mean = 0;
		deviation = 0;
		count = 0;
	}

	public void addValue(long newValue) {
        addValue(newValue,1);
	}

    private void addValue(long newValue, int sampleCount) {
        count += sampleCount;
        minimum=Math.min(newValue, minimum);
        maximum=Math.max(newValue, maximum);
        double currentVal = newValue;
        sum += currentVal;
        sumOfSquares += currentVal * currentVal;
        // Calculate each time, as likely to be called for each add
        mean = sum / count;
        deviation = Math.sqrt((sumOfSquares / count) - (mean * mean));
    }


	public void addBytes(long newValue) {
		bytes += newValue;
	}

    private long startTime = 0;
    private long elapsedTime = 0;

    public void addSample(SampleResult res) {
        addBytes(res.getBytes());
        addValue(res.getTime(),res.getSampleCount());
        errors+=res.getErrorCount(); // account for multiple samples
        if (startTime == 0){
            startTime=res.getStartTime();
        }
        startTime = Math.min(startTime, res.getStartTime());
        elapsedTime = Math.max(elapsedTime, res.getEndTime()-startTime);
    }


    public long getTotalBytes() {
		return bytes;
	}


	public double getMean() {
		return mean;
	}

    public Number getMeanAsNumber() {
        return new Long((long) mean);
    }

	public double getStandardDeviation() {
		return deviation;
	}

	public long getMin() {
		return minimum;
	}

	public long getMax() {
		return maximum;
	}

	public int getCount() {
		return count;
	}

    public String getLabel() {
        return label;
    }

    /**
     * Returns the raw double value of the percentage of samples with errors
     * that were recorded. (Between 0.0 and 1.0)
     * 
     * @return the raw double value of the percentage of samples with errors
     *         that were recorded.
     */
    public double getErrorPercentage() {
        double rval = 0.0;

        if (count == 0) {
            return (rval);
        }
        rval = (double) errors / (double) count;
        return (rval);
    }

    /**
     * Returns the throughput associated to this sampler in requests per second.
     * May be slightly skewed because it takes the timestamps of the first and
     * last samples as the total time passed, and the test may actually have
     * started before that start time and ended after that end time.
     */
    public double getRate() {
        if (elapsedTime == 0)
            return 0.0;

        return ((double) count / (double) elapsedTime ) * 1000;
    }

    /**
     * calculates the average page size, which means divide the bytes by number
     * of samples.
     * 
     * @return average page size
     */
    public double getPageSize() {
        if (count > 0 && bytes > 0) {
            return (double) bytes / count;
        }
        return 0.0;
    }

    /**
     * Throughput in bytes / second
     * 
     * @return throughput in bytes/second
     */
    public double getBytesPerSecond() {
        if (elapsedTime > 0) {
            return bytes / ((double) elapsedTime / 1000); // 1000 = millisecs/sec
        }
        return 0.0;
    }

    /**
     * Throughput in kilobytes / second
     * 
     * @return Throughput in kilobytes / second
     */
    public double getKBPerSecond() {
        return getBytesPerSecond() / 1024; // 1024=bytes per kb
    }

}