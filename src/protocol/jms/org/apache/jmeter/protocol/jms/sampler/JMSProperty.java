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

package org.apache.jmeter.protocol.jms.sampler;

import java.io.Serializable;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * JMS Property with type
 * @since 2.11
 */
public class JMSProperty extends AbstractTestElement implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 6371090992800805753L;

    /** Name used to store the JmsProperty's name. */
    public static final String PROP_NAME = "JMSProperty.name"; // $NON-NLS-1$

    /** Name used to store the JmsProperty's value. */
    public static final String PROP_VALUE = "JMSProperty.value"; // $NON-NLS-1$

    /** Name used to store the JmsProperty's description. */
    public static final String PROP_TYPE = "JMSProperty.type"; // $NON-NLS-1$

    private static final String DFLT_TYPE = String.class.getName();

    /**
     * Create a new JmsProperty without a name, value, or metadata.
     */
    public JMSProperty() {
    }

    /**
     * Create a new JmsProperty with the specified name and value, and String type.
     *
     * @param name
     *            the prop name
     * @param value
     *            the prop value
     */
    public JMSProperty(String name, String value) {
        this(name, value, DFLT_TYPE);
    }

    /**
     * Create a new JmsProperty with the specified name and value, and String type.
     *
     * @param name
     *            the prop name
     * @param value
     *            the prop value
     * @param type
     *            the type type
     */
    public JMSProperty(String name, String value, String type) {
        setProperty(new StringProperty(PROP_NAME, name));
        setProperty(new StringProperty(PROP_VALUE, value));
        setProperty(new StringProperty(PROP_TYPE, type));
    }

    /**
     * Set the name of the JmsProperty.
     *
     * @param newName
     *            the new name
     */
    @Override
    public void setName(String newName) {
        setProperty(new StringProperty(PROP_NAME, newName));
    }

    /**
     * Get the name of the JmsProperty.
     *
     * @return the attribute's name
     */
    @Override
    public String getName() {
        return getPropertyAsString(PROP_NAME);
    }

    /**
     * Sets the value of the JmsProperty.
     *
     * @param newValue
     *            the new value
     */
    public void setValue(String newValue) {
        setProperty(new StringProperty(PROP_VALUE, newValue));
    }

    /**
     * Gets the value of the JmsProperty object.
     *
     * @return the attribute's value
     */
    public String getValue() {
        return getPropertyAsString(PROP_VALUE);
    }

    /**
     * Sets the Meta Data attribute of the JmsProperty.
     *
     * @param type
     *            the new type
     */
    public void setType(String type) {
        setProperty(new StringProperty(PROP_TYPE, type));
    }

    /**
     * Gets the Meta Data attribute of the JmsProperty.
     *
     * @return the MetaData value
     */
    public String getType() {
        return getPropertyAsString(PROP_TYPE);
    }

    @Override
    public String toString() {
        return getName() + "," + getValue()+","+getType();
    }

    public Object getValueAsObject() {
        String type = getType();
        String value = getValue();

        if(type.equals(Boolean.class.getName())) { //NOSONAR
            return Boolean.valueOf(value);
        } else if(type.equals(Byte.class.getName())) {//NOSONAR
            return Byte.valueOf(value);
        } else if(type.equals(Short.class.getName())) {//NOSONAR
            return Short.valueOf(value);
        } else if(type.equals(Integer.class.getName())) {//NOSONAR
            return Integer.valueOf(value);
        } else if(type.equals(Long.class.getName())) {//NOSONAR
            return Long.valueOf(value);
        } else if(type.equals(Float.class.getName())) {//NOSONAR
            return Float.valueOf(value);
        } else if(type.equals(Double.class.getName())) {//NOSONAR
            return Double.valueOf(value);
        } else if(type.equals(String.class.getName())) {//NOSONAR
            return value;
        } else {
            return null;
        }
    }
}
