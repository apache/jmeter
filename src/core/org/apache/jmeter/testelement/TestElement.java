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
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;

public interface TestElement extends Cloneable {
    String NAME = "TestElement.name"; //$NON-NLS-1$

    String GUI_CLASS = "TestElement.gui_class"; //$NON-NLS-1$

    String ENABLED = "TestElement.enabled"; //$NON-NLS-1$

    String TEST_CLASS = "TestElement.test_class"; //$NON-NLS-1$

    // Needed by AbstractTestElement.
    // Also TestElementConverter and TestElementPropertyConverter for handling empty comments
    String COMMENTS = "TestPlan.comments"; //$NON-NLS-1$
    // N.B. Comments originally only applied to Test Plans, hence the name - which can now not be easily changed

    void addTestElement(TestElement child);

    /**
     * This method should clear any test element properties that are merged
     * by {@link #addTestElement(TestElement)}.
     */
    void clearTestElementChildren();

    void setProperty(String key, String value);

    void setProperty(String key, String value, String dflt);

    void setProperty(String key, boolean value);

    void setProperty(String key, boolean value, boolean dflt);

    void setProperty(String key, int value);

    void setProperty(String key, int value, int dflt);

    void setProperty(String name, long value);

    void setProperty(String name, long value, long dflt);

    /**
     * Check if ENABLED property is present and true ; defaults to true
     *
     * @return true if element is enabled
     */
    boolean isEnabled();

    /**
     * Set the enabled status of the test element
     * @param enabled the status to set
     */
    void setEnabled(boolean enabled);

    /**
     * Returns true or false whether the element is the running version.
     *
     * @return <code>true</code> if the element is the running version
     */
    boolean isRunningVersion();

    /**
     * Test whether a given property is only a temporary resident of the
     * TestElement
     *
     * @param property
     *            the property to be tested
     * @return <code>true</code> if property is temporary
     */
    boolean isTemporary(JMeterProperty property);

    /**
     * Indicate that the given property should be only a temporary property in
     * the TestElement
     *
     * @param property
     *            void
     */
    void setTemporary(JMeterProperty property);

    /**
     * Return a property as a boolean value.
     *
     * @param key
     *            the name of the property to get
     * @return the value of the property
     */
    boolean getPropertyAsBoolean(String key);

    /**
     * Return a property as a boolean value or a default value if no property
     * could be found.
     *
     * @param key
     *            the name of the property to get
     * @param defaultValue
     *            the default value to use
     * @return the value of the property, or <code>defaultValue</code> if no
     *         property could be found
     */
    boolean getPropertyAsBoolean(String key, boolean defaultValue);

    /**
     * Return a property as a long value.
     *
     * @param key
     *            the name of the property to get
     * @return the value of the property
     */
    long getPropertyAsLong(String key);

    /**
     * Return a property as a long value or a default value if no property
     * could be found.
     *
     * @param key
     *            the name of the property to get
     * @param defaultValue
     *            the default value to use
     * @return the value of the property, or <code>defaultValue</code> if no
     *         property could be found
     */
    long getPropertyAsLong(String key, long defaultValue);

    /**
     * Return a property as an int value.
     *
     * @param key
     *            the name of the property to get
     * @return the value of the property
     */
    int getPropertyAsInt(String key);

    /**
     * Return a property as an int value or a default value if no property
     * could be found.
     *
     * @param key
     *            the name of the property to get
     * @param defaultValue
     *            the default value to use
     * @return the value of the property, or <code>defaultValue</code> if no
     *         property could be found
     */
    int getPropertyAsInt(String key, int defaultValue);

    /**
     * Return a property as a float value.
     *
     * @param key
     *            the name of the property to get
     * @return the value of the property
     */
    float getPropertyAsFloat(String key);

    /**
     * Return a property as a double value.
     *
     * @param key
     *            the name of the property to get
     * @return the value of the property
     */
    double getPropertyAsDouble(String key);

    /**
     * Make the test element the running version, or make it no longer the
     * running version. This tells the test element that it's current state must
     * be retrievable by a call to recoverRunningVersion(). It is kind of like
     * making the TestElement Read- Only, but not as strict. Changes can be made
     * and the element can be modified, but the state of the element at the time
     * of the call to setRunningVersion() must be recoverable.
     *
     * @param run
     *            flag whether this element should be the running version
     */
    void setRunningVersion(boolean run);

    /**
     * Tells the test element to return to the state it was in when
     * setRunningVersion(true) was called.
     */
    void recoverRunningVersion();

    /**
     * Clear the TestElement of all data.
     */
    void clear();

    /**
     * Return a property as a string value.
     *
     * @param key
     *            the name of the property to get
     * @return the value of the property
     */
    String getPropertyAsString(String key);

    /**
     * Return a property as an string value or a default value if no property
     * could be found.
     *
     * @param key
     *            the name of the property to get
     * @param defaultValue
     *            the default value to use
     * @return the value of the property, or <code>defaultValue</code> if no
     *         property could be found
     */
    String getPropertyAsString(String key, String defaultValue);

    /**
     * Sets and overwrites a property in the TestElement. This call will be
     * ignored if the TestElement is currently a "running version".
     *
     * @param property
     *            the property to be set
     */
    void setProperty(JMeterProperty property);

    /**
     * Given the name of the property, returns the appropriate property from
     * JMeter. If it is null, a NullProperty object will be returned.
     *
     * @param propName
     *            the name of the property to get
     * @return {@link JMeterProperty} stored under the name, or
     *         {@link NullProperty} if no property can be found
     */
    JMeterProperty getProperty(String propName);

    /**
     * Get a Property Iterator for the TestElements properties.
     *
     * @return PropertyIterator
     */
    PropertyIterator propertyIterator();

    /**
     * Remove property stored under the <code>key</code>
     *
     * @param key
     *            name of the property to be removed
     */
    void removeProperty(String key);

    // lifecycle methods

    Object clone();

    /**
     * Convenient way to traverse a test element.
     *
     * @param traverser
     *            The traverser that is notified of the contained elements
     */
    void traverse(TestElementTraverser traverser);

    /**
     * @return Returns the threadContext.
     */
    JMeterContext getThreadContext();

    /**
     * @param threadContext
     *            The threadContext to set.
     */
    void setThreadContext(JMeterContext threadContext);

    /**
     * @return Returns the threadName.
     */
    String getThreadName();

    /**
     * @param threadName
     *            The threadName to set.
     */
    void setThreadName(String threadName);

    /**
     * Called by Remove to determine if it is safe to remove the element. The
     * element can either clean itself up, and return true, or the element can
     * return false.
     *
     * @return true if safe to remove the element
     */
    boolean canRemove();

    /**
     * Get the name of this test element
     * @return name of this element
     */
    String getName();

    /**
     * @param name
     *            of this element
     */
    void setName(String name);

    /**
     * @return comment associated with this element
     */
    String getComment();

    /**
     * Associates a comment with this element
     *
     * @param comment
     *            to be associated
     */
    void setComment(String comment);

    /**
     * Called when the test element is removed from the test plan.
     * Must not throw any exception
     */
    default void removed() {
        // NOOP
    }
}
