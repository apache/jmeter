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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.gui.Searchable;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
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
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class AbstractTestElement implements TestElement, Serializable, Searchable {
    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(AbstractTestElement.class);

    private final Map<String, JMeterProperty> propMap =
        Collections.synchronizedMap(new LinkedHashMap<String, JMeterProperty>());

    /**
     * Holds properties added when isRunningVersion is true
     */
    private transient Set<JMeterProperty> temporaryProperties;

    private transient boolean runningVersion = false;

    // Thread-specific variables saved here to save recalculation
    private transient JMeterContext threadContext = null;

    private transient String threadName = null;

    @Override
    public Object clone() {
        try {
            TestElement clonedElement = this.getClass().getDeclaredConstructor().newInstance();

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
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        propMap.clear();
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
        propMap.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof AbstractTestElement) {
            return ((AbstractTestElement) o).propMap.equals(propMap);
        } else {
            return false;
        }
    }

    // TODO temporary hack to avoid unnecessary bug reports for subclasses

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return System.identityHashCode(this);
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
        setProperty(TestElement.NAME, name);
    }

    @Override
    public String getName() {
        return getPropertyAsString(TestElement.NAME);
    }

    @Override
    public void setComment(String comment){
        setProperty(new StringProperty(TestElement.COMMENTS, comment));
    }

    @Override
    public String getComment(){
        return getProperty(TestElement.COMMENTS).getStringValue();
    }

    /**
     * Get the named property. If it doesn't exist, a new NullProperty object is
     * created with the same name and returned.
     */
    @Override
    public JMeterProperty getProperty(String key) {
        JMeterProperty prop = propMap.get(key);
        if (prop == null) {
            prop = new NullProperty(key);
        }
        return prop;
    }

    /**
     * Null property are wrapped in a {@link NullProperty}
     * This method avoids this wrapping
     * for internal use only
     * @since 3.1
     */
    private JMeterProperty getRawProperty(String key) {
        return propMap.get(key);
    }

    @Override
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
        JMeterProperty jmp = getRawProperty(key);
        return jmp == null || jmp instanceof NullProperty ? defaultValue : jmp.getIntValue();
    }

    @Override
    public boolean getPropertyAsBoolean(String key) {
        return getProperty(key).getBooleanValue();
    }

    @Override
    public boolean getPropertyAsBoolean(String key, boolean defaultVal) {
        JMeterProperty jmp = getRawProperty(key);
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
        JMeterProperty jmp = getRawProperty(key);
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
        JMeterProperty jmp = getRawProperty(key);
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
            setTemporary(propertyToPut);
        } else {
            clearTemporary(property);
        }
        JMeterProperty prop = getProperty(property.getName());

        if (prop instanceof NullProperty || (prop instanceof StringProperty && prop.getStringValue().isEmpty())) {
            propMap.put(property.getName(), propertyToPut);
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
                log.debug("Property {} is temp? {} and is a {}", prop.getName(), isTemporary(prop),
                        prop.getObjectValue());
            }
        }
    }

    @Override
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
        return new PropertyIteratorImpl(propMap.values());
    }

    /**
     * Add to this the properties of element (by reference)
     * @param element {@link TestElement}
     */
    protected void mergeIn(TestElement element) {
        PropertyIterator iter = element.propertyIterator();
        while (iter.hasNext()) {
            JMeterProperty prop = iter.next();
            addProperty(prop, false);
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
        this.runningVersion = runningVersion;
        PropertyIterator iter = propertyIterator();
        while (iter.hasNext()) {
            iter.next().setRunningVersion(runningVersion);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recoverRunningVersion() {
        Iterator<Map.Entry<String, JMeterProperty>>  iter = propMap.entrySet().iterator();
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

    /**
     * Clears temporaryProperties
     */
    protected void emptyTemporary() {
        if (temporaryProperties != null) {
            temporaryProperties.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTemporary(JMeterProperty property) {
        if (temporaryProperties == null) {
            return false;
        } else {
            return temporaryProperties.contains(property);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTemporary(JMeterProperty property) {
        if (temporaryProperties == null) {
            temporaryProperties = new LinkedHashSet<>();
        }
        temporaryProperties.add(property);
        if (property instanceof MultiProperty) {
            for (JMeterProperty jMeterProperty : (MultiProperty) property) {
                setTemporary(jMeterProperty);
            }
        }
    }

    /**
     * @return Returns the threadContext.
     */
    @Override
    public JMeterContext getThreadContext() {
        if (threadContext == null) {
            /*
             * Only samplers have the thread context set up by JMeterThread at
             * present, so suppress the warning for now
             */
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
    public String getThreadName() {
        return threadName;
    }

    /**
     * @param inthreadName
     *            The threadName to set.
     */
    @Override
    public void setThreadName(String inthreadName) {
        if (threadName != null) {
            if (!threadName.equals(inthreadName)) {
                throw new RuntimeException("Attempting to reset the thread name");
            }
        }
        this.threadName = inthreadName;
    }

    public AbstractTestElement() {
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
        return getProperty(TestElement.ENABLED) instanceof NullProperty || getPropertyAsBoolean(TestElement.ENABLED);
    }

    @Override
    public void setEnabled(boolean enabled) {
        setProperty(new BooleanProperty(TestElement.ENABLED, enabled));
    }

    /**
     * {@inheritDoc}}
     */
    @Override
    public List<String> getSearchableTokens() {
        List<String> result = new ArrayList<>(25);
        PropertyIterator iterator = propertyIterator();
        while(iterator.hasNext()) {
            JMeterProperty jMeterProperty = iterator.next();
            result.add(jMeterProperty.getName());
            result.add(jMeterProperty.getStringValue());
        }
        return result;
    }

    /**
     * Add to result the values of propertyNames
     * @param result List of values of propertyNames
     * @param propertyNames Set of names of properties to extract
     */
    protected final void addPropertiesValues(List<String> result, Set<String> propertyNames) {
        PropertyIterator iterator = propertyIterator();
        while(iterator.hasNext()) {
            JMeterProperty jMeterProperty = iterator.next();
            if(propertyNames.contains(jMeterProperty.getName())) {
                result.add(jMeterProperty.getStringValue());
            }
        }
    }
}
