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

package org.apache.jmeter.testelement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.jmeter.engine.util.LightweightClone;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.FunctionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.PropertyIteratorImpl;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class AbstractTestElement implements TestElement, Serializable, Searchable {
    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(AbstractTestElement.class);

    /**
     * Protects access to {@link #propMap} and {@link #temporaryProperties} when the element is shared across threads.
     * The assumption is that the properties are not changed during a test run, so read locks are used
     * to allow concurrent reads.
     * Note: acquiring locks (read or write) allocates memory, and accesses {@link ThreadLocal}, so it can be expensive
     * in both CPU and allocation terms.
     */
    private transient final ReentrantReadWriteLock lock =
            this instanceof NoThreadClone
                    // Note: thread groups are cloned for every thread, however, JMeterContext contains a reference
                    // to a non-cloned ThreadGroup instance.
                    // That causes jmeterContext.getThreadGroup().getName() to access the same ThreadGroup instance
                    // even though each thread has its own copy, so we use read-write lock approach for ThreadGroups
                    || this instanceof AbstractThreadGroup
                    ? new ReentrantReadWriteLock()
                    : null;

    private transient final ResourceLock readUnlock = lock == null ? null : lock.readLock()::unlock;

    /**
     * Downgrades input lock from write to read on close.
     * It enables using {@link #writeLock()} in try-with-resources to upgrade the lock.
     */
    interface ResourceLock extends AutoCloseable {
        @Override
        void close();

        ResourceLock NO_LOCK = () -> { };

        /**
         * Upgrades lock to write if needed, and returns a callback for releasing the write lock and restoring read locks.
         * @see <a href="https://www.javaspecialists.eu/archive/Issue279-Upgrading-ReadWriteLock.html>Issue279-Upgrading-ReadWriteLock</a>
         */
        static ResourceLock releaseWrite(ReentrantReadWriteLock lock) {
            int readHoldCount = lock.getWriteHoldCount() == 0 ? lock.getReadHoldCount() : 0;
            // We need to unlock read locks since ReentrantReadWriteLock does not support lock upgrade
            for (int i = 0; i < readHoldCount; i++) {
                lock.readLock().unlock();
            }
            lock.writeLock().lock();
            // Restore locks on close
            return () -> {
                for (int i = 0; i < readHoldCount; i++) {
                    lock.readLock().lock();
                }
                lock.writeLock().unlock();
            };
        }
    }

    /**
     * When the element is shared between threads, then {@link #lock} protects the access,
     * however, when element in not shared, then adds overhead as every lock and unlock allocates memory.
     * So in case of cloned-per-thread elements, we use {@link Collections#synchronizedMap(Map)} instead.
     */
    // @GuardedBy("lock")
    private final Map<String, JMeterProperty> propMap =
            lock != null
                    ? new LinkedHashMap<>()
                    : Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Every shared element has a concurrent map with properties, so that we can read values without acquiring locks.
     * The contents of the map must exactly match {@link #propMap}, so if {@link #propMap} is modified, then
     * this map should be updated as well.
     * Note: test plan serialization needs to have a consistent element order on load and save,
     * so we can't use {@link ConcurrentHashMap} for {@link #propMap} in case a test element is shared between threads.
     * <p>The purpose of this map is to avoid synchronization and acquiring logs on read-only operations.
     * For instance, every {@link org.apache.jmeter.samplers.SampleEvent} calls {@link ThreadGroup#getName()},
     * which calls {@link #getProperty(String)}, so it is important to keep the read path as fast as possible.</p>
     */
    private final transient Map<String, JMeterProperty> propMapConcurrent =
            lock != null
                    ? new ConcurrentHashMap<>()
                    : null;


    /**
     * Holds properties added when isRunningVersion is true
     */
    // @GuardedBy("lock")
    private transient Set<JMeterProperty> temporaryProperties;

    private transient boolean runningVersion = false;

    /**
     * Indicates whether this element's properties are shared with another element.
     * When true, the propMap is shared and should be treated as read-only.
     */
    private transient boolean propertiesShared = false;

    // Thread-specific variables saved here to save recalculation
    private transient JMeterContext threadContext = null;

    private transient String threadName = null;

    @Override
    public Object clone() {
        try {
            TestElement clonedElement = this.getClass().getDeclaredConstructor().newInstance();

            // Default constructor might be configuring non-default properties, and we want
            // the clone to be identical to the source, so we remove properties before copying.
            // For example, LoopController in JMeter 5.5
            // Note: clonedElement.clear(); might set unwanted options as well.
            PropertyIterator clonedProps = clonedElement.propertyIterator();
            while (clonedProps.hasNext()) {
                clonedProps.next();
                clonedProps.remove();
            }
            PropertyIterator iter = propertyIterator();
            while (iter.hasNext()) {
                clonedElement.setProperty(iter.next().clone());
            }
            clonedElement.setRunningVersion(runningVersion);
            return clonedElement;
        } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
            throw new AssertionError(e); // clone should never return null
        }
    }

    /**
     * Creates a lightweight clone that shares properties with this element.
     * This is more memory-efficient than a full clone for elements that implement
     * {@link LightweightClone} and don't modify properties during test execution.
     *
     * <p>The lightweight clone:
     * <ul>
     *   <li>Creates a new instance with default constructor</li>
     *   <li>Shares the property map with the original element (no deep copy)</li>
     *   <li>Has fresh transient state (threadContext, threadName, etc.)</li>
     * </ul>
     *
     * @return a lightweight clone of this element
     * @throws LinkageError if cloning fails due to reflection errors
     */
    public Object lightweightClone() {
        try {
            AbstractTestElement clone = this.getClass().getDeclaredConstructor().newInstance();
            // Clear default properties set by constructor
            clone.clear();
            // Share property map reference with this element
            clone.sharePropertiesFrom(this);
            clone.setRunningVersion(isRunningVersion());
            return clone;
        } catch (ReflectiveOperationException e) {
            throw new LinkageError(e.getMessage(), e);
        }
    }

    /**
     * Shares the property map from the source element.
     * <p>
     * This method is called only for elements that have NO variable properties
     * (verified by {@link #hasVariableProperties()} in TreeCloner before calling
     * {@link #lightweightClone()}). Therefore, it's safe to share property references
     * directly without cloning - this provides significant memory savings.
     * </p>
     * <p>
     * TestElementProperty is still cloned because nested TestElements may have
     * transient runtime state that needs to be per-thread.
     * </p>
     *
     * @param source the element to share properties from
     */
    void sharePropertiesFrom(AbstractTestElement source) {
        try (ResourceLock ignored = writeLock()) {
            // Share property references directly for maximum memory efficiency
            // This is safe because TreeCloner only calls lightweightClone() for
            // elements that have no variable properties (hasVariableProperties() == false)
            for (Map.Entry<String, JMeterProperty> entry : source.propMap.entrySet()) {
                JMeterProperty prop = entry.getValue();
                // Only clone TestElementProperty because nested TestElements
                // may have transient state that needs to be per-thread
                if (prop instanceof TestElementProperty) {
                    prop = prop.clone();
                }
                // All other properties are shared by reference - same String objects,
                // same property instances across all threads
                this.propMap.put(entry.getKey(), prop);
            }
            Map<String, JMeterProperty> propMapConcurrent = this.propMapConcurrent;
            if (propMapConcurrent != null) {
                propMapConcurrent.putAll(this.propMap);
            }
        }
        // Mark as shared to skip recoverRunningVersion and enable copy-on-write
        this.propertiesShared = true;
    }

    /**
     * Returns whether this element's properties are shared with another element.
     *
     * @return true if properties are shared (element was created via lightweightClone)
     */
    public boolean isPropertiesShared() {
        return propertiesShared;
    }

    /**
     * Checks if this element has any properties containing JMeter variables or functions.
     * Elements with variable properties should not use lightweight cloning because
     * the variable evaluation needs per-thread state.
     *
     * @return true if any property contains variables (${...}) or functions (__())
     */
    public boolean hasVariableProperties() {
        try (ResourceLock ignored = readLock()) {
            for (JMeterProperty prop : propMap.values()) {
                if (propertyHasVariables(prop)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a single property contains variables or functions.
     */
    private static boolean propertyHasVariables(JMeterProperty prop) {
        // FunctionProperty is created when StringProperty with ${...} is transformed
        if (prop instanceof FunctionProperty) {
            return true;
        }
        // Check StringProperty for variable patterns (before transformation)
        if (prop instanceof StringProperty stringProperty) {
            String value = stringProperty.getStringValue();
            if (value != null && (value.contains("${") || value.contains("__("))) {
                return true;
            }
        }
        // Check nested properties in collections
        if (prop instanceof CollectionProperty collectionProperty) {
            for (JMeterProperty nested : collectionProperty) {
                if (propertyHasVariables(nested)) {
                    return true;
                }
            }
        }
        // Check nested properties in maps
        if (prop instanceof MapProperty mapProperty) {
            PropertyIterator iter = mapProperty.valueIterator();
            while (iter.hasNext()) {
                if (propertyHasVariables(iter.next())) {
                    return true;
                }
            }
        }
        // Check nested TestElement
        if (prop instanceof TestElementProperty testElementProperty) {
            TestElement nested = testElementProperty.getElement();
            if (nested instanceof AbstractTestElement abstractNested) {
                if (abstractNested.hasVariableProperties()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Ensures this element has its own copy of properties (copy-on-write).
     * If properties are currently shared with another element, this method
     * creates deep clones of all properties before any modification can occur.
     * <p>
     * This allows lightweight clones to share properties for memory efficiency,
     * while still supporting runtime modifications when needed (e.g., when a
     * Header Manager has a variable like ${updateme} that gets modified).
     */
    protected void ensureOwnProperties() {
        if (!propertiesShared) {
            return;
        }
        try (ResourceLock ignored = writeLock()) {
            // Double-check after acquiring lock
            if (!propertiesShared) {
                return;
            }
            // Create deep copies of all properties
            Map<String, JMeterProperty> newPropMap = new LinkedHashMap<>();
            for (Map.Entry<String, JMeterProperty> entry : propMap.entrySet()) {
                newPropMap.put(entry.getKey(), entry.getValue().clone());
            }
            propMap.clear();
            propMap.putAll(newPropMap);
            if (propMapConcurrent != null) {
                propMapConcurrent.clear();
                propMapConcurrent.putAll(propMap);
            }
            propertiesShared = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        try (ResourceLock ignored = writeLock()) {
            propMap.clear();
            Map<String, JMeterProperty> propMapConcurrent = this.propMapConcurrent;
            if (propMapConcurrent != null) {
                propMapConcurrent.clear();
            }
        }
    }

    /**
     * Acquires write lock, and upgrades the current read lock to write lock if needed.
     * Note: this should be used in try-with-resources for automatic cleanup.
     * @return ResourceLock to close when the write lock is no longer needed
     */
    private ResourceLock writeLock() {
        if (lock == null) {
            return ResourceLock.NO_LOCK;
        }
        return ResourceLock.releaseWrite(lock);
    }

    private ResourceLock readLock() {
        if (lock == null) {
            return ResourceLock.NO_LOCK;
        }
        lock.readLock().lock();
        return readUnlock;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Default implementation - does nothing
     */
    @Override
    public void clearTestElementChildren(){
        // NOOP
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeProperty(String key) {
        // Copy-on-write: ensure we have our own properties before modifying
        if (isRunningVersion()) {
            ensureOwnProperties();
        }
        try (ResourceLock ignored = writeLock()) {
            propMap.remove(key);
            Map<String, JMeterProperty> propMapConcurrent = this.propMapConcurrent;
            if (propMapConcurrent != null) {
                propMapConcurrent.remove(key);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof AbstractTestElement other) {
            try (ResourceLock ignored = readLock()) {
                return other.propMap.equals(propMap);
            }
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return propMap.hashCode();
    }

    /*
     * URGENT: TODO - sort out equals and hashCode() - at present equal
     * instances can/will have different hashcodes - problem is, when a proper
     * hashcode is used, tests stop working, e.g. listener data disappears when
     * switching views... This presumably means that instances currently
     * regarded as equal, aren't really equal.
     *
     * @see java.lang.Object#hashCode()
     */
    // This would be sensible, but does not work:
    // public int hashCode()
    // {
    // return propMap.hashCode();
    // }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTestElement(TestElement el) {
        mergeIn(el);
    }

    @Override
    public void setName(String name) {
        set(getSchema().getName(), name);
    }

    @Override
    public String getName() {
        return get(getSchema().getName());
    }

    @Override
    public void setComment(String comment){
        set(getSchema().getComments(), comment);
    }

    @Override
    public String getComment(){
        return get(getSchema().getComments());
    }

    /**
     * Get the named property. If it doesn't exist, a new NullProperty object is
     * created with the same name and returned.
     */
    @Override
    public JMeterProperty getProperty(String key) {
        JMeterProperty prop = getPropertyOrNull(key);
        if (prop == null) {
            prop = new NullProperty(key);
        }
        return prop;
    }

    /**
     * {@inheritDoc}
     * @since 5.6
     */
    @Override
    @API(status = API.Status.EXPERIMENTAL, since = "5.6")
    public JMeterProperty getPropertyOrNull(String key) {
        Map<String, JMeterProperty> propMapConcurrent = this.propMapConcurrent;
        if (propMapConcurrent != null) {
            return propMapConcurrent.get(key);
        }

        return propMap.get(key);
    }

    @Override
    public void traverse(TestElementTraverser traverser) {
        try (ResourceLock ignored = readLock()) {
            PropertyIterator iter = propertyIterator();
            traverser.startTestElement(this);
            while (iter.hasNext()) {
                traverseProperty(traverser, iter.next());
            }
            traverser.endTestElement(this);
        }
    }

    protected void traverseProperty(TestElementTraverser traverser, JMeterProperty value) {
        traverser.startProperty(value);
        if (value instanceof TestElementProperty) {
            ((TestElement) value.getObjectValue()).traverse(traverser);
        } else if (value instanceof CollectionProperty collectionProperty) {
            traverseCollection(collectionProperty, traverser);
        } else if (value instanceof MapProperty mapProperty) {
            traverseMap(mapProperty, traverser);
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
        for (JMeterProperty jMeterProperty :  col) {
            traverseProperty(traverser, jMeterProperty);
        }
    }

    @Override
    public int getPropertyAsInt(String key) {
        return getProperty(key).getIntValue();
    }

    @Override
    public int getPropertyAsInt(String key, int defaultValue) {
        JMeterProperty jmp = getPropertyOrNull(key);
        return jmp == null || jmp instanceof NullProperty ? defaultValue : jmp.getIntValue();
    }

    @Override
    public boolean getPropertyAsBoolean(String key) {
        return getProperty(key).getBooleanValue();
    }

    @Override
    public boolean getPropertyAsBoolean(String key, boolean defaultVal) {
        JMeterProperty jmp = getPropertyOrNull(key);
        return jmp == null || jmp instanceof NullProperty ? defaultVal : jmp.getBooleanValue();
    }

    @Override
    public float getPropertyAsFloat(String key) {
        return getProperty(key).getFloatValue();
    }

    @Override
    public long getPropertyAsLong(String key) {
        return getProperty(key).getLongValue();
    }

    @Override
    public long getPropertyAsLong(String key, long defaultValue) {
        JMeterProperty jmp = getPropertyOrNull(key);
        return jmp == null || jmp instanceof NullProperty ? defaultValue : jmp.getLongValue();
    }

    @Override
    public double getPropertyAsDouble(String key) {
        return getProperty(key).getDoubleValue();
    }

    @Override
    public String getPropertyAsString(String key) {
        return getProperty(key).getStringValue();
    }

    @Override
    public String getPropertyAsString(String key, String defaultValue) {
        JMeterProperty jmp = getPropertyOrNull(key);
        return jmp == null || jmp instanceof NullProperty ? defaultValue : jmp.getStringValue();
    }

    /**
     * Add property to test element
     * @param property {@link JMeterProperty} to add to current Test Element
     * @param clone clone property
     */
    protected void addProperty(JMeterProperty property, boolean clone) {
        JMeterProperty propertyToPut = property;
        if(clone) {
            propertyToPut = property.clone();
        }
        if (isRunningVersion()) {
            // Copy-on-write: ensure we have our own properties before modifying
            ensureOwnProperties();
            setTemporary(propertyToPut);
        } else {
            clearTemporary(property);
        }
        JMeterProperty prop = getProperty(property.getName());

        if (prop instanceof NullProperty || (prop instanceof StringProperty && prop.getStringValue().isEmpty())) {
            try (ResourceLock ignored = writeLock()) {
                propMap.put(property.getName(), propertyToPut);
                Map<String, JMeterProperty> propMapConcurrent = this.propMapConcurrent;
                if (propMapConcurrent != null) {
                    propMapConcurrent.put(property.getName(), propertyToPut);
                }
            }
        } else {
            prop.mergeIn(propertyToPut);
        }
    }

    /**
     * Add property to test element without cloning it
     * @param property {@link JMeterProperty}
     */
    protected void addProperty(JMeterProperty property) {
        addProperty(property, false);
    }

    /**
     * Remove property from temporaryProperties
     * @param property {@link JMeterProperty}
     */
    protected void clearTemporary(JMeterProperty property) {
        try (ResourceLock ignored = writeLock()) {
            if (temporaryProperties != null) {
                temporaryProperties.remove(property);
            }
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
                log.debug("Property {} is temp? {} and is a {}", prop.getName(), isTemporary(prop),
                        prop.getObjectValue());
            }
        }
    }

    @Override
    public void setProperty(JMeterProperty property) {
        if (isRunningVersion()) {
            // Copy-on-write: ensure we have our own properties before modifying
            ensureOwnProperties();
            if (getProperty(property.getName()) instanceof NullProperty) {
                addProperty(property);
            } else {
                getProperty(property.getName()).setObjectValue(property.getObjectValue());
            }
        } else {
            try (ResourceLock ignored = writeLock()) {
                if (property instanceof StringProperty && property.getStringValue() == null) {
                    // Avoid storing properties with null values since they will be skipped anyway when saving
                    // the test plan
                    removeProperty(property.getName());
                } else {
                    propMap.put(property.getName(), property);
                    Map<String, JMeterProperty> propMapConcurrent = this.propMapConcurrent;
                    if (propMapConcurrent != null) {
                        propMapConcurrent.put(property.getName(), property);
                    }
                }
            }
        }
    }

    @Override
    public void setProperty(String name, String value) {
        setProperty(new StringProperty(name, value));
    }

    /**
     * Create a String property - but only if it is not the default.
     * This is intended for use when adding new properties to JMeter
     * so that JMX files are not expanded unnecessarily.
     *
     * N.B. - must agree with the default applied when reading the property.
     *
     * @param name property name
     * @param value current value
     * @param dflt default
     */
    @Override
    public void setProperty(String name, String value, String dflt) {
        if (dflt.equals(value)) {
            removeProperty(name);
        } else {
            setProperty(new StringProperty(name, value));
        }
    }

    @Override
    public void setProperty(String name, boolean value) {
        setProperty(new BooleanProperty(name, value));
    }

    /**
     * Create a boolean property - but only if it is not the default.
     * This is intended for use when adding new properties to JMeter
     * so that JMX files are not expanded unnecessarily.
     *
     * N.B. - must agree with the default applied when reading the property.
     *
     * @param name property name
     * @param value current value
     * @param dflt default
     */
    @Override
    public void setProperty(String name, boolean value, boolean dflt) {
        if (value == dflt) {
            removeProperty(name);
        } else {
            setProperty(new BooleanProperty(name, value));
        }
    }

    @Override
    public void setProperty(String name, int value) {
        setProperty(new IntegerProperty(name, value));
    }

    /**
     * Create an int property - but only if it is not the default.
     * This is intended for use when adding new properties to JMeter
     * so that JMX files are not expanded unnecessarily.
     *
     * N.B. - must agree with the default applied when reading the property.
     *
     * @param name property name
     * @param value current value
     * @param dflt default
     */
    @Override
    public void setProperty(String name, int value, int dflt) {
        if (value == dflt) {
            removeProperty(name);
        } else {
            setProperty(new IntegerProperty(name, value));
        }
    }

    @Override
    public void setProperty(String name, long value) {
        setProperty(new LongProperty(name, value));
    }

    /**
     * Create a long property - but only if it is not the default.
     * This is intended for use when adding new properties to JMeter
     * so that JMX files are not expanded unnecessarily.
     *
     * N.B. - must agree with the default applied when reading the property.
     *
     * @param name property name
     * @param value current value
     * @param dflt default
     */
    @Override
    public void setProperty(String name, long value, long dflt) {
        if (value == dflt) {
            removeProperty(name);
        } else {
            setProperty(new LongProperty(name, value));
        }
    }

    @Override
    public PropertyIterator propertyIterator() {
        // Note: can't use ConcurrentMap here as it would return elements in unpredictable order
        try (ResourceLock ignored = readLock()) {
            if (propMap.isEmpty()) {
                return PropertyIteratorImpl.EMPTY_ITERATOR;
            }
            // TODO: copy the contents of the iterator to avoid ConcurrentModificationException?
            return new PropertyIteratorImpl(this, propMap.values());
        }
    }

    /**
     * Add to this the properties of element (by reference)
     * @param element {@link TestElement}
     */
    protected void mergeIn(TestElement element) {
        try (ResourceLock ignored = writeLock()) {
            PropertyIterator iter = element.propertyIterator();
            while (iter.hasNext()) {
                JMeterProperty prop = iter.next();
                addProperty(prop, false);
            }
        }
    }

    /**
     * Returns the runningVersion.
     */
    @Override
    public boolean isRunningVersion() {
        return runningVersion;
    }

    /**
     * Sets the runningVersion.
     *
     * @param runningVersion
     *            the runningVersion to set
     */
    @Override
    public void setRunningVersion(boolean runningVersion) {
        try (ResourceLock ignored = writeLock()) {
            this.runningVersion = runningVersion;
            // Note: Even for shared properties (lightweight clones), we must call
            // property.setRunningVersion() to ensure variable substitution works.
            // The property transformation is idempotent and thread-safe.
            PropertyIterator iter = propertyIterator();
            Map<String, JMeterProperty> propMapConcurrent = this.propMapConcurrent;
            while (iter.hasNext()) {
                JMeterProperty property = iter.next();
                property.setRunningVersion(runningVersion);
                if (propMapConcurrent != null) {
                    propMapConcurrent.put(property.getName(), property);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recoverRunningVersion() {
        if (this instanceof NoThreadClone) {
            // The element is shared between threads, so there's nothing to recover
            // See https://github.com/apache/jmeter/issues/5875
            return;
        }
        if (propertiesShared) {
            // Properties are shared with other elements (lightweight clone),
            // so there's nothing to recover - the properties are read-only
            return;
        }
        try (ResourceLock ignored = writeLock()) {
            Iterator<Map.Entry<String, JMeterProperty>> iter = propMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, JMeterProperty> entry = iter.next();
                JMeterProperty prop = entry.getValue();
                if (isTemporary(prop)) {
                    iter.remove();
                    clearTemporary(prop);
                } else {
                    prop.recoverRunningVersion(this);
                }
            }
            emptyTemporary();
        }
    }

    /**
     * Clears temporaryProperties
     */
    protected void emptyTemporary() {
        try (ResourceLock ignored = writeLock()) {
            if (temporaryProperties != null) {
                temporaryProperties.clear();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTemporary(JMeterProperty property) {
        try (ResourceLock ignored = readLock()) {
            return temporaryProperties != null && temporaryProperties.contains(property);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTemporary(JMeterProperty property) {
        try (ResourceLock ignored = writeLock()) {
            if (temporaryProperties == null) {
                LinkedHashSet<JMeterProperty> set = new LinkedHashSet<>();
                temporaryProperties = lock != null ? set : Collections.synchronizedSet(set) ;
            }
            temporaryProperties.add(property);
            if (isMergingEnclosedProperties(property)) {
                for (JMeterProperty jMeterProperty : (MultiProperty) property) {
                    setTemporary(jMeterProperty);
                }
            }
        }
    }

    // While TestElementProperty is implementing MultiProperty, it works differently.
    // It doesn't merge the inner properties one by one as MultiProperty would do.
    // Therefore we must not mark the enclosed properties of TestElementProperty as
    // temporary (Bug 65336)
    private static boolean isMergingEnclosedProperties(JMeterProperty property) {
        return property instanceof MultiProperty && !(property instanceof TestElementProperty);
    }

    /**
     * @return Returns the threadContext.
     */
    @Override
    public JMeterContext getThreadContext() {
        if (threadContext == null) {
            // Only samplers have the thread context set up by JMeterThread at
            // present, so suppress the warning for now
            threadContext = JMeterContextService.getContext();
        }
        return threadContext;
    }

    /**
     * @param inthreadContext
     *            The threadContext to set.
     */
    @Override
    public void setThreadContext(JMeterContext inthreadContext) {
        if (threadContext != null) {
            if (inthreadContext != threadContext) {
                throw new RuntimeException("Attempting to reset the thread context");
            }
        }
        this.threadContext = inthreadContext;
    }

    /**
     * @return Returns the threadName.
     */
    @Override
    @SuppressWarnings("deprecation")
    public String getThreadName() {
        return threadName;
    }

    /**
     * @param inthreadName
     *            The threadName to set.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void setThreadName(String inthreadName) {
        if (threadName != null) {
            if (!threadName.equals(inthreadName)) {
                throw new RuntimeException("Attempting to reset the thread name");
            }
        }
        this.threadName = inthreadName;
    }

    protected AbstractTestElement() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    // Default implementation
    @Override
    public boolean canRemove() {
        return true;
    }

    // Moved from JMeter class
    @Override
    public boolean isEnabled() {
        return get(getSchema().getEnabled());
    }

    @Override
    public void setEnabled(boolean enabled) {
        set(getSchema().getEnabled(), enabled);
    }

    /**
     * {@inheritDoc}}
     */
    @Override
    public List<String> getSearchableTokens() {
        List<String> result = new ArrayList<>(25);
        try (ResourceLock ignored = readLock()) {
            PropertyIterator iterator = propertyIterator();
            while(iterator.hasNext()) {
                JMeterProperty jMeterProperty = iterator.next();
                result.add(jMeterProperty.getName());
                result.add(jMeterProperty.getStringValue());
            }
        }
        return result;
    }

    /**
     * Add to result the values of propertyNames
     * @param result List of values of propertyNames
     * @param propertyNames Set of names of properties to extract
     */
    protected final void addPropertiesValues(List<? super String> result, Set<String> propertyNames) {
        try (ResourceLock ignored = readLock()) {
            PropertyIterator iterator = propertyIterator();
            while (iterator.hasNext()) {
                JMeterProperty jMeterProperty = iterator.next();
                if (propertyNames.contains(jMeterProperty.getName())) {
                    result.add(jMeterProperty.getStringValue());
                }
            }
        }
    }
}
