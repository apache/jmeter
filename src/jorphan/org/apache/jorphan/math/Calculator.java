/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.jorphan.math;

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
		count++;
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

	public long getTotalBytes() {
		return bytes;
	}


	public double getMean() {
		return mean;
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
}