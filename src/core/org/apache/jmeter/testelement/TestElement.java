// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.testelement;

import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;

/**
 * @author Michael Stover
 * @version $Revision$
 */

public interface TestElement extends Cloneable {
	public final static String NAME = "TestElement.name";

	public final static String GUI_CLASS = "TestElement.gui_class";

	public final static String ENABLED = "TestElement.enabled";

	public final static String TEST_CLASS = "TestElement.test_class";

	public void addTestElement(TestElement child);

	public void setProperty(String key, String value);

	/**
	 * Check if ENABLED property is present and true ; defaults to true
	 * 
	 * @return true if element is enabled
	 */
	public boolean isEnabled();

	/**
	 * Returns true or false whether the element is the running version.
	 */
	public boolean isRunningVersion();

	/**
	 * Test whether a given property is only a temporary resident of the
	 * TestElement
	 * 
	 * @param property
	 * @return boolean
	 */
	public boolean isTemporary(JMeterProperty property);

	/**
	 * Indicate that the given property should be only a temporary property in
	 * the TestElement
	 * 
	 * @param property
	 *            void
	 */
	public void setTemporary(JMeterProperty property);

	/**
	 * Return a property as a boolean value.
	 */
	public boolean getPropertyAsBoolean(String key);

	public long getPropertyAsLong(String key);

	public int getPropertyAsInt(String key);

	public float getPropertyAsFloat(String key);

	/**
	 * Make the test element the running version, or make it no longer the
	 * running version. This tells the test element that it's current state must
	 * be retrievable by a call to recoverRunningVersion(). It is kind of like
	 * making the TestElement Read- Only, but not as strict. Changes can be made
	 * and the element can be modified, but the state of the element at the time
	 * of the call to setRunningVersion() must be recoverable.
	 */
	public void setRunningVersion(boolean run);

	/**
	 * Tells the test element to return to the state it was in when
	 * makeRunningVersion() was called.
	 */
	public void recoverRunningVersion();

	/**
	 * Clear the TestElement of all data.
	 */
	public void clear();

	public String getPropertyAsString(String key);

	/**
	 * Sets and overwrites a property in the TestElement. This call will be
	 * ignored if the TestElement is currently a "running version".
	 */
	public void setProperty(JMeterProperty property);

	/**
	 * Given the name of the property, returns the appropriate property from
	 * JMeter. If it is null, a NullProperty object will be returned.
	 */
	public JMeterProperty getProperty(String propName);

	/**
	 * Get a Property Iterator for the TestElements properties.
	 * 
	 * @return PropertyIterator
	 */
	public PropertyIterator propertyIterator();

	public void removeProperty(String key);

	// lifecycle methods

	public Object clone();

	/**
	 * Convenient way to traverse a test element.
	 */
	public void traverse(TestElementTraverser traverser);

	/**
	 * @return Returns the threadContext.
	 */
	public JMeterContext getThreadContext();

	/**
	 * @param threadContext
	 *            The threadContext to set.
	 */
	public void setThreadContext(JMeterContext threadContext);

	/**
	 * @return Returns the threadName.
	 */
	public String getThreadName();

	/**
	 * @param threadName
	 *            The threadName to set.
	 */
	public void setThreadName(String threadName);

	/**
	 * Called at the start of each thread. TODO - should it hava a parameter?
	 */
	public void threadStarted();

	/**
	 * Called at the end of each thread. TODO - should it hava a parameter?
	 */
	public void threadFinished();

	/**
	 * Called by Remove to determine if it is safe to remove the element. The
	 * element can either clean itself up, and return true, or the element can
	 * return false.
	 * 
	 * @return true if safe to remove the element
	 */
	public boolean canRemove();
}
