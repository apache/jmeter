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

package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class DoubleProperty extends NumberProperty {
	double value;

	double savedValue;

	public DoubleProperty(String name, double value) {
		super(name);
		this.value = value;
	}

	public DoubleProperty() {
	}

	public void setValue(float value) {
		this.value = value;
	}

	protected void setNumberValue(Number n) {
		value = n.doubleValue();
	}

	protected void setNumberValue(String n) throws NumberFormatException {
		value = Double.parseDouble(n);
	}

	/**
	 * @see JMeterProperty#getStringValue()
	 */
	public String getStringValue() {
		return Double.toString(value);
	}

	/**
	 * @see JMeterProperty#getObjectValue()
	 */
	public Object getObjectValue() {
		return new Double(value);
	}

	public Object clone() {
		DoubleProperty prop = (DoubleProperty) super.clone();
		prop.value = value;
		return prop;
	}

	/**
	 * @see JMeterProperty#getBooleanValue()
	 */
	public boolean getBooleanValue() {
		return value > 0 ? true : false;
	}

	/**
	 * @see JMeterProperty#getDoubleValue()
	 */
	public double getDoubleValue() {
		return value;
	}

	/**
	 * @see JMeterProperty#getFloatValue()
	 */
	public float getFloatValue() {
		return (float) value;
	}

	/**
	 * @see JMeterProperty#getIntValue()
	 */
	public int getIntValue() {
		return (int) value;
	}

	/**
	 * @see JMeterProperty#getLongValue()
	 */
	public long getLongValue() {
		return (long) value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.property.JMeterProperty#setRunningVersion(boolean)
	 */
	public void setRunningVersion(boolean runningVersion) {
		savedValue = value;
		super.setRunningVersion(runningVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.property.JMeterProperty#recoverRunningVersion(org.apache.jmeter.testelement.TestElement)
	 */
	public void recoverRunningVersion(TestElement owner) {
		value = savedValue;
	}
}
