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
import java.util.Map;

import org.apache.jmeter.testelement.TestElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProperty implements JMeterProperty {
    private static final long serialVersionUID = 241L;

    //TODO consider using private logs for each derived class
    protected static final Logger log = LoggerFactory.getLogger(AbstractProperty.class);

    private String name;

    private transient boolean runningVersion = false;

    public AbstractProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    public AbstractProperty() {
        this("");
    }

    protected boolean isEqualType(JMeterProperty prop) {
        return this.getClass().equals(prop.getClass());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRunningVersion() {
        return runningVersion;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public void setRunningVersion(boolean runningVersion) {
        this.runningVersion = runningVersion;
    }

    protected PropertyIterator getIterator(Collection<JMeterProperty> values) {
        return new PropertyIteratorImpl(values);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractProperty clone() {
        try {
            AbstractProperty prop = (AbstractProperty) super.clone();
            prop.name = name;
            prop.runningVersion = runningVersion;
            return prop;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // clone should never return null
        }
    }

    /**
     * Returns 0 if string is invalid or null.
     *
     * @see JMeterProperty#getIntValue()
     */
    @Override
    public int getIntValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
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
    @Override
    public long getLongValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
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
    @Override
    public double getDoubleValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
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
    @Override
    public float getFloatValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
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
    @Override
    public boolean getBooleanValue() {
        String val = getStringValue();
        if (val == null || val.length()==0) {
            return false;
        }
        return Boolean.parseBoolean(val);
    }

    /**
     * Determines if the two objects are equal by comparing names and values
     *
     * @return true if names are equal and values are equal (or both null)
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JMeterProperty)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        JMeterProperty jpo = (JMeterProperty) o;
        if (!name.equals(jpo.getName())) {
            return false;
        }
        Object o1 = getObjectValue();
        Object o2 = jpo.getObjectValue();
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = 17;
        result = result * 37 + name.hashCode();// name cannot be null
        Object o = getObjectValue();
        result = result * 37 + (o == null ? 0 : o.hashCode());
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
    @Override
    public int compareTo(JMeterProperty arg0) {
        // We don't expect the string values to ever be null. But (as in
        // bug 19499) sometimes they are. So have null compare less than
        // any other value. Log a warning so we can try to find the root
        // cause of the null value.
        String val = getStringValue();
        String val2 = arg0.getStringValue();
        if (val == null) {
            log.warn("Warning: Unexpected null value for property: {}", name);

            if (val2 == null) {
                // Two null values -- return equal
                return 0;
            } else {
                return -1;
            }
        }
        return val.compareTo(val2);
    }

    /**
     * Get the property type for this property. Used to convert raw values into
     * JMeterProperties.
     *
     * @return property type of this property
     */
    protected Class<? extends JMeterProperty> getPropertyType() {
        return getClass();
    }

    protected JMeterProperty getBlankProperty() {
        try {
            JMeterProperty prop = getPropertyType().newInstance();
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
        } else {
            return new StringProperty("", item.toString());
        }
    }

    /**
     * Convert a collection of objects into JMeterProperty objects.
     * 
     * @param coll Collection of any type of object
     * @return Collection of JMeterProperty objects
     */
    protected Collection<JMeterProperty> normalizeList(Collection<?> coll) {
        try {
            @SuppressWarnings("unchecked") // empty collection
            Collection<JMeterProperty> newColl = coll.getClass().newInstance();
            for (Object item : coll) {
                newColl.add(convertObject(item));
            }
            return newColl;
        } catch (Exception e) {// should not happen
            log.error("Cannot create copy of {}", coll.getClass(), e);
            return null;
        }
    }

    /**
     * Given a Map, it converts the Map into a collection of JMeterProperty
     * objects, appropriate for a MapProperty object.
     *
     * @param coll
     *            Map to convert
     * @return converted Map
     */
    protected Map<String, JMeterProperty> normalizeMap(Map<?,?> coll) {
        try {
            @SuppressWarnings("unchecked") // empty collection
            Map<String, JMeterProperty> newColl = coll.getClass().newInstance();
            for (Map.Entry<?,?> entry : ((Map<?,?>)coll).entrySet()) {
                Object key = entry.getKey();
                Object prop = entry.getValue();
                String item=null;
                if (key instanceof String) {
                    item = (String) key;
                } else {
                    if (key != null) {
                        log.error("Expected key type String, found: {}", key.getClass());
                        item = key.toString();
                    }
                }
                newColl.put(item, convertObject(prop));
            }
            return newColl;
        } catch (Exception e) {// should not happen
            log.error("Cannot create copy of {}", coll.getClass(), e);
            return null;
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
     * Create a JMeterProperty from an object.
     * The object can be one of:
     * <ul>
     * <li>JMeterProperty - returned unchanged</li>
     * <li>TestElement =&gt; TestElementProperty with the same name</li>
     * <li>Map|Collection =&gt; Map|CollectionProperty with the name = item.hashCode</li>
     * </ul>
     * @param item object to be turned into a property
     * @return the JMeterProperty
     */
    protected static JMeterProperty makeProperty(Object item) {
        if (item instanceof JMeterProperty) {
            return (JMeterProperty) item;
        }
        if (item instanceof TestElement) {
            return new TestElementProperty(((TestElement) item).getName(),
                    (TestElement) item);
        }
        if (item instanceof Collection<?>) {
            return new CollectionProperty(Integer.toString(item.hashCode()), (Collection<?>) item);
        }
        if (item instanceof Map<?, ?>) {
            return new MapProperty(Integer.toString(item.hashCode()), (Map<?, ?>) item);
        }
        return null;
    }

    protected JMeterProperty convertObject(Object item) {
        JMeterProperty prop = makeProperty(item);
        if (prop == null) {
            prop = getBlankProperty();
            prop.setName(Integer.toString(item.hashCode()));
            prop.setObjectValue(item);
        }
        return prop;
    }

    /**
     * Provides the string representation of the property.
     *
     * @return the string value
     */
    @Override
    public String toString() {
        // N.B. Other classes rely on this returning just the string.
        return getStringValue();
    }

    /** {@inheritDoc} */
    @Override
    public void mergeIn(JMeterProperty prop) {
        // NOOP
    }
}
