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
public class TestElementProperty extends MultiProperty {
	private TestElement value;

	private transient TestElement savedValue = null;

	public TestElementProperty(String name, TestElement value) {
		super(name);
		this.value = value;
	}

	public TestElementProperty() {
		super();
	}

	/**
	 * Determines if two test elements are equal.
	 * 
	 * @return true if the value is not null and equals the other Objects value;
	 *         false otherwise (even if both values are null)
	 */
	public boolean equals(Object o) {
		if (o instanceof TestElementProperty) {
			if (this == o)
				return true;
			if (value != null) {
				return value.equals(((JMeterProperty) o).getObjectValue());
			}
		}
		return false;
	}

	public int hashCode() {
		return value == null ? 0 : value.hashCode();
	}

	/*
	 * (non-Javadoc) #getStringValue()
	 */
	public String getStringValue() {
		return value.toString();
	}

	public void setObjectValue(Object v) {
		if (v instanceof TestElement) {
			value = (TestElement) v;
		}
	}

	/*
	 * (non-Javadoc) #getObjectValue()
	 */
	public Object getObjectValue() {
		return value;
	}

	public TestElement getElement() {
		return value;
	}

	public void setElement(TestElement el) {
		value = el;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		TestElementProperty prop = (TestElementProperty) super.clone();
		prop.value = (TestElement) value.clone();
		return prop;
	}

	/*
	 * (non-Javadoc) #mergeIn(JMeterProperty)
	 */
	public void mergeIn(JMeterProperty prop) {
		if (isEqualType(prop)) {
			value.addTestElement((TestElement) prop.getObjectValue());
		}
	}

	/*
	 * (non-Javadoc) #recoverRunningVersion(TestElement)
	 */
	public void recoverRunningVersion(TestElement owner) {
		if (savedValue != null) {
			value = savedValue;
		}
		value.recoverRunningVersion();
	}

	/*
	 * (non-Javadoc) #setRunningVersion(boolean)
	 */
	public void setRunningVersion(boolean runningVersion) {
		super.setRunningVersion(runningVersion);
		value.setRunningVersion(runningVersion);
		if (runningVersion) {
			savedValue = value;
		} else {
			savedValue = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MultiProperty#addProperty(JMeterProperty)
	 */
	public void addProperty(JMeterProperty prop) {
		value.setProperty(prop);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MultiProperty#clear()
	 */
	public void clear() {
		value.clear();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see MultiProperty#iterator()
	 */
	public PropertyIterator iterator() {
		return value.propertyIterator();
	}
}
