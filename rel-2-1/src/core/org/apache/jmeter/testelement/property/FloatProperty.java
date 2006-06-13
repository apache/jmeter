// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class FloatProperty extends NumberProperty {
	float value;

	float savedValue;

	public FloatProperty(String name, float value) {
		super(name);
		this.value = value;
	}

	public FloatProperty() {
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

	public void setValue(float value) {
		this.value = value;
	}

	protected void setNumberValue(Number n) {
		value = n.floatValue();
	}

	protected void setNumberValue(String n) throws NumberFormatException {
		value = Float.parseFloat(n);
	}

	/**
	 * @see JMeterProperty#getStringValue()
	 */
	public String getStringValue() {
		return Float.toString(value);
	}

	/**
	 * @see JMeterProperty#getObjectValue()
	 */
	public Object getObjectValue() {
		return new Float(value);
	}

	/**
	 * @see Object#clone()
	 */
	public Object clone() {
		FloatProperty prop = (FloatProperty) super.clone();
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
		return value;
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
}
