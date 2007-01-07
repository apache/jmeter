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
 * A null property.
 * 
 * @version $Revision$
 */
public class NullProperty extends AbstractProperty {
	JMeterProperty tempValue;

	public NullProperty(String name) {
		super(name);
	}

	public NullProperty() {
		super();
	}

	/**
	 * @see JMeterProperty#getStringValue()
	 */
	public String getStringValue() {
		if (tempValue != null) {
			return tempValue.getStringValue();
		}
		return "";
	}

	public void setObjectValue(Object v) {
	}

	/**
	 * @see JMeterProperty#getObjectValue()
	 */
	public Object getObjectValue() {
		return null;
	}

	/**
	 * @see JMeterProperty#isRunningVersion()
	 */
	public boolean isRunningVersion() {
		return false;
	}

	/**
	 * see JMeterProperty#isTemporary(TestElement)
	 */
	public boolean isTemporary(TestElement owner) {
		return true;
	}

	/**
	 * @see JMeterProperty#mergeIn(JMeterProperty)
	 */
	public void mergeIn(JMeterProperty prop) {
		tempValue = prop;
	}

	/**
	 * @see Object#clone()
	 */
	public Object clone() {
		return this;
	}

	/**
	 * @see JMeterProperty#getBooleanValue()
	 */
	public boolean getBooleanValue() {
		return false;
	}

	/**
	 * @see JMeterProperty#getDoubleValue()
	 */
	public double getDoubleValue() {
		return 0;
	}

	/**
	 * @see JMeterProperty#getFloatValue()
	 */
	public float getFloatValue() {
		return 0;
	}

	/**
	 * @see JMeterProperty#getIntValue()
	 */
	public int getIntValue() {
		return 0;
	}

	/**
	 * @see JMeterProperty#getLongValue()
	 */
	public long getLongValue() {
		return 0;
	}

	/**
	 * @see JMeterProperty#recoverRunningVersion(TestElement)
	 */
	public void recoverRunningVersion(TestElement owner) {
		tempValue = null;
	}

}
