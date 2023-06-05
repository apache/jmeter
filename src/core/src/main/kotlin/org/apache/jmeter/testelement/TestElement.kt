/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.testelement

import org.apache.jmeter.testelement.property.JMeterProperty
import org.apache.jmeter.testelement.property.NullProperty
import org.apache.jmeter.testelement.property.PropertyIterator
import org.apache.jmeter.threads.JMeterContext
import org.apiguardian.api.API

public interface TestElement : Cloneable {
    public companion object {
        public const val NAME: String = "TestElement.name" // $NON-NLS-1$
        public const val GUI_CLASS: String = "TestElement.gui_class" // $NON-NLS-1$
        public const val ENABLED: String = "TestElement.enabled" // $NON-NLS-1$
        public const val TEST_CLASS: String = "TestElement.test_class" // $NON-NLS-1$

        // Needed by AbstractTestElement.
        // Also TestElementConverter and TestElementPropertyConverter for handling empty comments
        public const val COMMENTS: String = "TestPlan.comments" // $NON-NLS-1$
    }

    public fun addTestElement(child: TestElement)

    /**
     * This method should clear any test element properties that are merged
     * by [.addTestElement].
     */
    public fun clearTestElementChildren()
    public fun setProperty(key: String, value: String)
    public fun setProperty(key: String, value: String, dflt: String)
    public fun setProperty(key: String, value: Boolean)
    public fun setProperty(key: String, value: Boolean, dflt: Boolean)
    public fun setProperty(key: String, value: Int)
    public fun setProperty(key: String, value: Int, dflt: Int)
    public fun setProperty(name: String, value: Long)
    public fun setProperty(name: String, value: Long, dflt: Long)

    /**
     * Configures if the current test element should be enabled or not.
     */
    public var isEnabled: Boolean

    /**
     * Make the test element the running version, or make it no longer the
     * running version. This tells the test element that it's current state must
     * be retrievable by a call to recoverRunningVersion(). It is kind of like
     * making the TestElement Read-Only, but not as strict. Changes can be made
     * and the element can be modified, but the state of the element at the time
     * of the call to setRunningVersion() must be recoverable.
     *
     * flag whether this element should be the running version
     */
    public var isRunningVersion: Boolean

    /**
     * Test whether a given property is only a temporary resident of the
     * TestElement
     *
     * @param property the property to be tested
     * @return `true` if property is temporary
     */
    public fun isTemporary(property: JMeterProperty): Boolean

    /**
     * Indicate that the given property should be only a temporary property in
     * the TestElement.
     *
     * @param property property to set as temporary one
     */
    public fun setTemporary(property: JMeterProperty)

    /**
     * Return a property as a boolean value.
     *
     * @param key the name of the property to get
     * @return the value of the property
     */
    public fun getPropertyAsBoolean(key: String): Boolean

    /**
     * Return a property as a boolean value or a default value if no property
     * could be found.
     *
     * @param key the name of the property to get
     * @param defaultValue the default value to use
     * @return the value of the property, or `defaultValue` if no property could be found
     */
    public fun getPropertyAsBoolean(key: String, defaultValue: Boolean): Boolean

    /**
     * Return a property as a long value.
     *
     * @param key the name of the property to get
     * @return the value of the property
     */
    public fun getPropertyAsLong(key: String): Long

    /**
     * Return a property as a long value or a default value if no property
     * could be found.
     *
     * @param key the name of the property to get
     * @param defaultValue the default value to use
     * @return the value of the property, or `defaultValue` if no property could be found
     */
    public fun getPropertyAsLong(key: String, defaultValue: Long): Long

    /**
     * Return a property as an int value.
     *
     * @param key
     * the name of the property to get
     * @return the value of the property
     */
    public fun getPropertyAsInt(key: String): Int

    /**
     * Return a property as an int value or a default value if no property
     * could be found.
     *
     * @param key the name of the property to get
     * @param defaultValue the default value to use
     * @return the value of the property, or `defaultValue` if no property could be found
     */
    public fun getPropertyAsInt(key: String, defaultValue: Int): Int

    /**
     * Return a property as a float value.
     *
     * @param key
     * the name of the property to get
     * @return the value of the property
     */
    public fun getPropertyAsFloat(key: String): Float

    /**
     * Return a property as a double value.
     *
     * @param key
     * the name of the property to get
     * @return the value of the property
     */
    public fun getPropertyAsDouble(key: String): Double

    /**
     * Tells the test element to return to the state it was in when
     * setRunningVersion(true) was called.
     */
    public fun recoverRunningVersion()

    /**
     * Clear the TestElement of all data.
     */
    public fun clear()

    /**
     * Return a property as a string value.
     *
     * @param key the name of the property to get
     * @return the value of the property
     */
    public fun getPropertyAsString(key: String): String

    /**
     * Return a property as an string value or a default value if no property
     * could be found.
     *
     * @param key
     * the name of the property to get
     * @param defaultValue
     * the default value to use
     * @return the value of the property, or `defaultValue` if no
     * property could be found
     */
    public fun getPropertyAsString(key: String, defaultValue: String): String

    /**
     * Sets and overwrites a property in the TestElement. This call will be
     * ignored if the TestElement is currently a "running version".
     *
     * @param property the property to be set
     */
    public fun setProperty(property: JMeterProperty)

    /**
     * Given the name of the property, returns the appropriate property from
     * JMeter. If it is null, a [NullProperty] object will be returned.
     *
     * @param propName the name of the property to get
     * @return [JMeterProperty] stored under the name, or [NullProperty] if no property can be found
     */
    public fun getProperty(propName: String): JMeterProperty

    /**
     * Get a Property Iterator for the TestElements properties.
     *
     * @return PropertyIterator
     */
    public fun propertyIterator(): PropertyIterator

    /**
     * Remove property stored under the `key`
     *
     * @param key name of the property to be removed
     */
    public fun removeProperty(key: String)

    // lifecycle methods
    public override fun clone(): Any

    /**
     * Convenient way to traverse a test element.
     *
     * @param traverser The traverser that is notified of the contained elements
     */
    public fun traverse(traverser: TestElementTraverser)

    /**
     * Associates a thread context with this element.
     */
    public var threadContext: JMeterContext

    /**
     * Associates a thread name with this element.
     * @deprecated Use `JMeterContextService.getContext().thread.threadName` instead
     */
    @Deprecated(
        message = "Use JMeterContextService.getContext().thread.threadName instead",
        replaceWith = ReplaceWith(
            "JMeterContextService.getContext().thread.threadName",
            imports = ["org.apache.jmeter.threads.JMeterContextService"]
        )
    )
    @get:API(status = API.Status.DEPRECATED, since = "5.6")
    @set:API(status = API.Status.DEPRECATED, since = "5.6")
    public var threadName: String

    /**
     * Called by Remove to determine if it is safe to remove the element. The
     * element can either clean itself up, and return true, or the element can
     * return false.
     *
     * @return true if safe to remove the element
     */
    public fun canRemove(): Boolean

    /**
     * Associates a name with this element.
     */
    public var name: String?

    /**
     * Associates a comment with this element.
     */
    public var comment: String?

    /**
     * Called when the test element is removed from the test plan.
     * Must not throw any exception
     */
    public fun removed() {
        // NOOP
    }
}
