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

package org.apache.jmeter.testelement;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.control.NextIsNullException;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.PropertyIteratorImpl;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 */
public abstract class AbstractTestElement implements TestElement, Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private Map propMap = Collections.synchronizedMap(new LinkedHashMap());

	private transient Set temporaryProperties;

	private transient boolean runningVersion = false;

	// Thread-specific variables saved here to save recalculation
	private transient JMeterContext threadContext = null;

	private transient String threadName = null;

	public Object clone() {
		TestElement clonedElement = null;
		try {
			clonedElement = (TestElement) this.getClass().newInstance();

			PropertyIterator iter = propertyIterator();
			while (iter.hasNext()) {
				clonedElement.setProperty((JMeterProperty) iter.next().clone());
			}
			clonedElement.setRunningVersion(runningVersion);
		} catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
		return clonedElement;
	}

	public void clear() {
		propMap.clear();
	}

	public void removeProperty(String key) {
		propMap.remove(key);
	}

	public boolean equals(Object o) {
		if (o instanceof AbstractTestElement) {
			return ((AbstractTestElement) o).propMap.equals(propMap);
		} else {
			return false;
		}
	}

	// TODO temporary hack to avoid unnecessary bug reports for subclasses
	
	public int hashCode(){
		return System.identityHashCode(this);
	}
	/*
	 * URGENT: TODO - sort out equals and hashCode() - at present equal
	 * instances can/will have different hashcodes - problem is, when a proper
	 * hashcode is used, tests stop working, e.g. listener data disappears when
	 * switching views... This presumably means that instances currently
	 * regarded as equal, aren't really equal...
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	// This would be sensible, but does not work:
	// public int hashCode()
	// {
	// return propMap.hashCode();
	// }
	public void addTestElement(TestElement el) {
		mergeIn(el);
	}

	public void setName(String name) {
		setProperty(new StringProperty(TestElement.NAME, name));
	}

	public String getName() {
		return getProperty(TestElement.NAME).getStringValue();
	}

	/**
	 * Get the named property. If it doesn't exist, a new NullProperty object is
	 * created with the same name and returned.
	 */
	public JMeterProperty getProperty(String key) {
		JMeterProperty prop = (JMeterProperty) propMap.get(key);
		if (prop == null) {
			prop = new NullProperty(key);
		}
		return prop;
	}

	public void traverse(TestElementTraverser traverser) {
		PropertyIterator iter = propertyIterator();
		traverser.startTestElement(this);
		while (iter.hasNext()) {
			traverseProperty(traverser, iter.next());
		}
		traverser.endTestElement(this);
	}

	protected void traverseProperty(TestElementTraverser traverser, JMeterProperty value) {
		traverser.startProperty(value);
		if (value instanceof TestElementProperty) {
			((TestElement) value.getObjectValue()).traverse(traverser);
		} else if (value instanceof CollectionProperty) {
			traverseCollection((CollectionProperty) value, traverser);
		} else if (value instanceof MapProperty) {
			traverseMap((MapProperty) value, traverser);
		}
		traverser.endProperty(value);
	}

	protected void traverseMap(MapProperty map, TestElementTraverser traverser) {
		PropertyIterator iter = map.valueIterator();
		while (iter.hasNext()) {
			traverseProperty(traverser, iter.next());
		}
	}

	protected void traverseCollection(CollectionProperty col, TestElementTraverser traverser) {
		PropertyIterator iter = col.iterator();
		while (iter.hasNext()) {
			traverseProperty(traverser, iter.next());
		}
	}

	public int getPropertyAsInt(String key) {
		return getProperty(key).getIntValue();
	}

	public boolean getPropertyAsBoolean(String key) {
		return getProperty(key).getBooleanValue();
	}

	public boolean getPropertyAsBoolean(String key, boolean defaultVal) {
		JMeterProperty jmp = getProperty(key);
		return jmp instanceof NullProperty ? defaultVal : jmp.getBooleanValue();
	}

	public float getPropertyAsFloat(String key) {
		return getProperty(key).getFloatValue();
	}

	public long getPropertyAsLong(String key) {
		return getProperty(key).getLongValue();
	}

	public double getPropertyAsDouble(String key) {
		return getProperty(key).getDoubleValue();
	}

	public String getPropertyAsString(String key) {
		return getProperty(key).getStringValue();
	}

    public String getPropertyAsString(String key, String defaultValue) {
        JMeterProperty jmp = getProperty(key);
        return jmp instanceof NullProperty ? defaultValue : jmp.getStringValue();
    }

	protected void addProperty(JMeterProperty property) {
		if (isRunningVersion()) {
			setTemporary(property);
		} else {
			clearTemporary(property);
		}
		JMeterProperty prop = getProperty(property.getName());
        //removeProperty(property.getName());

		if (prop instanceof NullProperty || (prop instanceof StringProperty && prop.getStringValue().equals(""))) {
			propMap.put(property.getName(), property);
		} else {
            //propMap.put(property.getName(), prop);
			prop.mergeIn(property);
		}
	}

	protected void clearTemporary(JMeterProperty property) {
		if (temporaryProperties != null) {
			temporaryProperties.remove(property);
		}
	}

	/**
	 * Log the properties of the test element
	 * 
	 * @see TestElement#setProperty(JMeterProperty)
	 */
	protected void logProperties() {
		if (log.isDebugEnabled()) {
			PropertyIterator iter = propertyIterator();
			while (iter.hasNext()) {
				JMeterProperty prop = iter.next();
				log.debug("Property " + prop.getName() + " is temp? " + isTemporary(prop) + " and is a "
						+ prop.getObjectValue());
			}
		}
	}

	public void setProperty(JMeterProperty property) {
		if (isRunningVersion()) {
			if (getProperty(property.getName()) instanceof NullProperty) {
				addProperty(property);
			} else {
				getProperty(property.getName()).setObjectValue(property.getObjectValue());
			}
		} else {
			propMap.put(property.getName(), property);
		}
	}

	public void setProperty(String name, String value) {
		setProperty(new StringProperty(name, value));
	}

	public void setProperty(String name, boolean value) {
		setProperty(new StringProperty(name, Boolean.toString(value)));
	}

	public PropertyIterator propertyIterator() {
		return new PropertyIteratorImpl(propMap.values());
	}

	protected void mergeIn(TestElement element) {
		PropertyIterator iter = element.propertyIterator();
		while (iter.hasNext()) {
			JMeterProperty prop = iter.next();
			addProperty(prop);
		}
	}

	/**
	 * Returns the runningVersion.
	 */
	public boolean isRunningVersion() {
		return runningVersion;
	}

	/**
	 * Sets the runningVersion.
	 * 
	 * @param runningVersion
	 *            the runningVersion to set
	 */
	public void setRunningVersion(boolean runningVersion) {
		this.runningVersion = runningVersion;
		PropertyIterator iter = propertyIterator();
		while (iter.hasNext()) {
			iter.next().setRunningVersion(runningVersion);
		}
	}

	public void recoverRunningVersion() {
		Iterator iter = propMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			JMeterProperty prop = (JMeterProperty) entry.getValue();
			if (isTemporary(prop)) {
				iter.remove();
				clearTemporary(prop);
			} else {
				prop.recoverRunningVersion(this);
			}
		}
		emptyTemporary();
	}

	protected void emptyTemporary() {
		if (temporaryProperties != null) {
			temporaryProperties.clear();
		}
	}

	protected Sampler nextIsNull() throws NextIsNullException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestElement#isTemporary(org.apache.jmeter.testelement.property.JMeterProperty)
	 */
	public boolean isTemporary(JMeterProperty property) {
		if (temporaryProperties == null) {
			return false;
		} else {
			return temporaryProperties.contains(property);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestElement#setTemporary(org.apache.jmeter.testelement.property.JMeterProperty)
	 */
	public void setTemporary(JMeterProperty property) {
		if (temporaryProperties == null) {
			temporaryProperties = new LinkedHashSet();
		}
		temporaryProperties.add(property);
		if (property instanceof MultiProperty) {
			PropertyIterator iter = ((MultiProperty) property).iterator();
			while (iter.hasNext()) {
				setTemporary(iter.next());
			}
		}
	}

	/**
	 * @return Returns the threadContext.
	 */
	public JMeterContext getThreadContext() {
		if (threadContext == null) {
			/*
			 * Only samplers have the thread context set up by JMeterThread at
			 * present, so suppress the warning for now
			 */
			// log.warn("ThreadContext was not set up - should only happen in
			// JUnit testing..."
			// ,new Throwable("Debug"));
			threadContext = JMeterContextService.getContext();
		}
		return threadContext;
	}

	/**
	 * @param inthreadContext
	 *            The threadContext to set.
	 */
	public void setThreadContext(JMeterContext inthreadContext) {
		if (threadContext != null) {
			if (inthreadContext != threadContext)
				throw new RuntimeException("Attempting to reset the thread context");
		}
		this.threadContext = inthreadContext;
	}

	/**
	 * @return Returns the threadName.
	 */
	public String getThreadName() {
		return threadName;
	}

	/**
	 * @param inthreadName
	 *            The threadName to set.
	 */
	public void setThreadName(String inthreadName) {
		if (threadName != null) {
			if (!threadName.equals(inthreadName))
				throw new RuntimeException("Attempting to reset the thread name");
		}
		this.threadName = inthreadName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestElement#threadFinished()
	 */
	public void threadFinished() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestElement#threadStarted()
	 */
	public void threadStarted() {
	}

	/**
	 * 
	 */
	public AbstractTestElement() {
		super();
	}

	// Default implementation
	public boolean canRemove() {
		return true;
	}

	// Moved from JMeter class
	public boolean isEnabled() {
		return getProperty(TestElement.ENABLED) instanceof NullProperty || getPropertyAsBoolean(TestElement.ENABLED);
	}
}
