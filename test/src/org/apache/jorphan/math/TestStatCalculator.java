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

package org.apache.jorphan.math;

import junit.framework.TestCase;

public class TestStatCalculator extends TestCase {

	StatCalculator calc;

	/**
	 * 
	 */
	public TestStatCalculator() {
		super();
	}

	/**
	 * @param arg0
	 */
	public TestStatCalculator(String arg0) {
		super(arg0);
	}

	public void setUp() {
		calc = new StatCalculator();
	}

	public void testPercentagePoint() throws Exception {
		calc.addValue(10);
		calc.addValue(9);
		calc.addValue(5);
		calc.addValue(6);
		calc.addValue(1);
		calc.addValue(3);
		calc.addValue(8);
		calc.addValue(2);
		calc.addValue(7);
		calc.addValue(4);
		assertEquals(10, calc.getCount());
		assertEquals(9, calc.getPercentPoint(0.8999999).intValue());
	}
	public void testCalculation() {
		calc.addValue(18);
		calc.addValue(10);
		calc.addValue(9);
		calc.addValue(11);
		calc.addValue(28);
		calc.addValue(3);
		calc.addValue(30);
		calc.addValue(15);
		calc.addValue(15);
		calc.addValue(21);
		assertEquals(16, (int) calc.getMean());
		assertEquals(8.0622577F, (float) calc.getStandardDeviation(), 0F);
		assertEquals(30, calc.getMax().intValue());
		assertEquals(3, calc.getMin().intValue());
		assertEquals(15, calc.getMedian().intValue());
	}
}
