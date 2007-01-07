/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *  
 */

package org.apache.jmeter.testelement.property;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */
public abstract class AbstractProperty implements JMeterProperty {
    //TODO consider using private logs for each derived class
	protected static final Logger log = LoggingManager.getLoggerForClass();

	private String name;

	transient private boolean runningVersion = false;

	// private static StringProperty defaultProperty = new StringProperty();

	public AbstractProperty(String name) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null");
		this.name = name;
	}

	public AbstractProperty() {
		this("");
	}

	protected boolean isEqualType(JMeterProperty prop) {
		if (this.getClass().equals(prop.getClass())) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see JMeterProperty#isRunningVersion()
	 */
	public boolean isRunningVersion() {
		return runningVersion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see JMeterProperty#getName()
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null");
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see JMeterProperty#setRunningVersion(boolean)
	 */
	public void setRunningVersion(boolean runningVersion) {
		this.runningVersion = runningVersion;
	}

	protected PropertyIterator getIterator(Collection values) {
		return new PropertyIteratorImpl(values);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Object#clone()
	 */
	public Object clone() {
		try {
			AbstractProperty prop = (AbstractProperty) this.getClass().newInstance();
			prop.name = name;
			prop.runningVersion = runningVersion;
			return prop;
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	/**
	 * Returns 0 if string is invalid or null.
	 * 
	 * @see JMeterProperty#getIntValue()
	 */
	public int getIntValue() {
		String val = getStringValue();
		if (val == null) {
			return 0;
		}
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Returns 0 if string is invalid or null.
	 * 
	 * @see JMeterProperty#getLongValue()
	 */
	public long getLongValue() {
		String val = getStringValue();
		if (val == null) {
			return 0;
		}
		try {
			return Long.parseLong(val);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Returns 0 if string is invalid or null.
	 * 
	 * @see JMeterProperty#getDoubleValue()
	 */
	public double getDoubleValue() {
		String val = getStringValue();
		if (val == null) {
			return 0;
		}
		try {
			return Double.parseDouble(val);
		} catch (NumberFormatException e) {
			log.error("Tried to parse a non-number string to an integer", e);
			return 0;
		}
	}

	/**
	 * Returns 0 if string is invalid or null.
	 * 
	 * @see JMeterProperty#getFloatValue()
	 */
	public float getFloatValue() {
		String val = getStringValue();
		if (val == null) {
			return 0;
		}
		try {
			return Float.parseFloat(val);
		} catch (NumberFormatException e) {
			log.error("Tried to parse a non-number string to an integer", e);
			return 0;
		}
	}

	/**
	 * Returns false if string is invalid or null.
	 * 
	 * @see JMeterProperty#getBooleanValue()
	 */
	public boolean getBooleanValue() {
		String val = getStringValue();
		if (val == null) {
			return false;
		}
		return Boolean.valueOf(val).booleanValue();
	}

	/**
	 * Determines if the two objects are equal by comparing names and values
	 * 
	 * @return true if names are equal and values are equal (or both null)
	 */
	public boolean equals(Object o) {
		if (!(o instanceof JMeterProperty))
			return false;
		if (this == o)
			return true;
		JMeterProperty jpo = (JMeterProperty) o;
		if (!name.equals(jpo.getName()))
			return false;
		String s1 = getStringValue();
		String s2 = jpo.getStringValue();
		return s1 == null ? s2 == null : s1.equals(s2);
	}

	public int hashCode() {
		int result = 17;
		result = result * 37 + name.hashCode();// name cannot be null
		String s = getStringValue();
		result = result * 37 + (s == null ? 0 : s.hashCode());
		return result;
	}

	/**
	 * Compares two JMeterProperty object values. N.B. Does not compare names
	 * 
	 * @param arg0
	 *            JMeterProperty to compare against
	 * @return 0 if equal values or both values null; -1 otherwise
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object arg0) {
		if (arg0 instanceof JMeterProperty) {
			// We don't expect the string values to ever be null. But (as in
			// bug 19499) sometimes they are. So have null compare less than
			// any other value. Log a warning so we can try to find the root
			// cause of the null value.
			String val = getStringValue();
			String val2 = ((JMeterProperty) arg0).getStringValue();
			if (val == null) {
				log.warn("Warning: Unexpected null value for property: " + name);

				if (val2 == null) {
					// Two null values -- return equal
					return 0;
				} else {
					return -1;
				}
			}
			return val.compareTo(val2);
		} else {
			return -1;
		}
	}

	/**
	 * Get the property type for this property. Used to convert raw values into
	 * JMeterProperties.
	 */
	protected Class getPropertyType() {
		return getClass();
	}

	protected JMeterProperty getBlankProperty() {
		try {
			JMeterProperty prop = (JMeterProperty) getPropertyType().newInstance();
			if (prop instanceof NullProperty) {
				return new StringProperty();
			}
			return prop;
		} catch (Exception e) {
			return new StringProperty();
		}
	}

	protected static JMeterProperty getBlankProperty(Object item) {
		if (item == null) {
			return new NullProperty();
		}
		if (item instanceof String) {
			return new StringProperty("", item.toString());
		} else if (item instanceof Boolean) {
			return new BooleanProperty("", ((Boolean) item).booleanValue());
		} else if (item instanceof Float) {
			return new FloatProperty("", ((Float) item).floatValue());
		} else if (item instanceof Double) {
			return new DoubleProperty("", ((Double) item).doubleValue());
		} else if (item instanceof Integer) {
			return new IntegerProperty("", ((Integer) item).intValue());
		} else if (item instanceof Long) {
			return new LongProperty("", ((Long) item).longValue());
		} else if (item instanceof Long) {
			return new LongProperty("", ((Long) item).longValue());
		} else {
			return new StringProperty("", item.toString());
		}
	}

	protected Collection normalizeList(Collection coll) {
		Iterator iter = coll.iterator();
		Collection newColl = null;
		while (iter.hasNext()) {
			Object item = iter.next();
			if (newColl == null) {
				try {
					newColl = (Collection) coll.getClass().newInstance();
				} catch (Exception e) {
					log.error("Bad collection", e);
				}
			}
			newColl.add(convertObject(item));
		}
		if (newColl != null) {
			return newColl;
		} else {
			return coll;
		}
	}

	/**
	 * Given a Map, it converts the Map into a collection of JMeterProperty
	 * objects, appropriate for a MapProperty object.
	 */
	protected Map normalizeMap(Map coll) {
		Iterator iter = coll.entrySet().iterator();
		Map newColl = null;
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
            Object item = entry.getKey();
			Object prop = entry.getValue();
			if (newColl == null) {
				try {
					newColl = (Map) coll.getClass().newInstance();
				} catch (Exception e) {
					log.error("Bad collection", e);
				}
			}
			newColl.put(item, convertObject(prop));
		}
		if (newColl != null) {
			return newColl;
		} else {
			return coll;
		}
	}

	public static JMeterProperty createProperty(Object item) {
		JMeterProperty prop = makeProperty(item);
		if (prop == null) {
			prop = getBlankProperty(item);
		}
		return prop;
	}

	/**
	 * @param item
	 * @return
	 */
	protected static JMeterProperty makeProperty(Object item) {
		if (item instanceof JMeterProperty) {
			return (JMeterProperty) item;
		}
		if (item instanceof TestElement) {
			return new TestElementProperty(((TestElement) item).getPropertyAsString(TestElement.NAME),
					(TestElement) item);
		}
		if (item instanceof Collection) {
			return new CollectionProperty("" + item.hashCode(), (Collection) item);
		}
		if (item instanceof Map) {
			return new MapProperty("" + item.hashCode(), (Map) item);
		}
		return null;
	}

	protected JMeterProperty convertObject(Object item) {
		JMeterProperty prop = makeProperty(item);
		if (prop == null) {
			prop = getBlankProperty();
			prop.setName("" + item.hashCode());
			prop.setObjectValue(item);
		}
		return prop;
	}

	/**
	 * Provides the string representation of the property.
	 * 
	 * @return the string value
	 */
	public String toString() {
		// N.B. Other classes rely on this returning just the string.
		return getStringValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.property.JMeterProperty#mergeIn(org.apache.jmeter.testelement.property.JMeterProperty)
	 */
	public void mergeIn(JMeterProperty prop) {
	}
}
