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

import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;

public interface TestElement extends Cloneable {
    public final static String NAME = "TestElement.name"; //$NON-NLS-1$

    public final static String GUI_CLASS = "TestElement.gui_class"; //$NON-NLS-1$

    public final static String ENABLED = "TestElement.enabled"; //$NON-NLS-1$

    public final static String TEST_CLASS = "TestElement.test_class"; //$NON-NLS-1$

    // Needed by AbstractTestElement.
    // Also TestElementConverter and TestElementPropertyConverter for handling empty comments
    public final static String COMMENTS = "TestPlan.comments"; //$NON-NLS-1$
    // N.B. Comments originally only applied to Test Plans, hence the name - which can now not be easily changed

    public void addTestElement(TestElement child);
    
    /**
     * This method should clear any test element properties that are merged
     * by {@link #addTestElement(TestElement)}.
     */
    public void clearTestElementChildren();

    public void setProperty(String key, String value);

    public void setProperty(String key, String value, String dflt);

    public void setProperty(String key, boolean value);

    public void setProperty(String key, boolean value, boolean dflt);

    public void setProperty(String key, int value);

    public void setProperty(String key, int value, int dflt);

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

    public boolean getPropertyAsBoolean(String key, boolean defaultValue);

    public long getPropertyAsLong(String key);

    public long getPropertyAsLong(String key, long defaultValue);

    public int getPropertyAsInt(String key);

    public int getPropertyAsInt(String key, int defaultValue);

    public float getPropertyAsFloat(String key);

    public double getPropertyAsDouble(String key);

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
     * setRunningVersion(true) was called.
     */
    public void recoverRunningVersion();

    /**
     * Clear the TestElement of all data.
     */
    public void clear();
    // TODO - yet another ambiguous name - does it need changing?
    // See also: Clearable, JMeterGUIComponent

    public String getPropertyAsString(String key);

    public String getPropertyAsString(String key, String defaultValue);

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
     * Called by Remove to determine if it is safe to remove the element. The
     * element can either clean itself up, and return true, or the element can
     * return false.
     *
     * @return true if safe to remove the element
     */
    public boolean canRemove();

    public String getName();

    public void setName(String name);

    public String getComment();

    public void setComment(String comment);
}
