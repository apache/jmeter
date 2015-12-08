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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * A set of JMSProperty objects.
 * @since 2.11
 */
public class JMSProperties extends AbstractTestElement implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2896138201054314563L;
    /** The name of the property used to store the JmsProperties. */
    public static final String JMS_PROPERTIES = "JMSProperties.properties"; //$NON-NLS-1$

    /**
     * Create a new JmsPropertys object with no JmsProperties
     */
    public JMSProperties() {
        setProperty(new CollectionProperty(JMS_PROPERTIES, new ArrayList<JMSProperty>()));
    }

    /**
     * Get the JmsPropertiess.
     *
     * @return the JmsProperties
     */
    public CollectionProperty getProperties() {
        return (CollectionProperty) getProperty(JMS_PROPERTIES);
    }

    /**
     * Clear the JmsProperties.
     */
    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(JMS_PROPERTIES, new ArrayList<JMSProperty>()));
    }

    /**
     * Set the list of JmsProperties. Any existing JmsProperties will be lost.
     *
     * @param jmsProperties
     *            the new JmsProperties
     */
    public void setProperties(List<JMSProperty> jmsProperties) {
        setProperty(new CollectionProperty(JMS_PROPERTIES, jmsProperties));
    }

    /**
     * Get the JmsProperties as a Map. Each JMSProperty name is used as the key, and
     * its value as the value.
     *
     * @return a new Map with String keys and values containing the JmsProperties
     */
    public Map<String, Object> getJmsPropertysAsMap() {
        Map<String, Object> argMap = new LinkedHashMap<>();
        for (JMeterProperty jMeterProperty : getProperties()) {
            JMSProperty arg = (JMSProperty) jMeterProperty.getObjectValue();
            // Because CollectionProperty.mergeIn will not prevent adding two
            // properties of the same name, we need to select the first value so
            // that this element's values prevail over defaults provided by
            // configuration elements:
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), arg.getValueAsObject());
            }
        }
        return argMap;
    }

    /**
     * Add a new JMSProperty with the given name and value.
     *
     * @param name
     *            the name of the JMSProperty
     * @param value
     *            the value of the JMSProperty
     */
    public void addJmsProperty(String name, String value) {
        addJmsProperty(new JMSProperty(name, value));
    }

    /**
     * Add a new argument.
     *
     * @param arg
     *            the new argument
     */
    public void addJmsProperty(JMSProperty arg) {
        TestElementProperty newArg = new TestElementProperty(arg.getName(), arg);
        if (isRunningVersion()) {
            this.setTemporary(newArg);
        }
        getProperties().addItem(newArg);
    }

    /**
     * Add a new argument with the given name, value, and metadata.
     *
     * @param name
     *            the name of the argument
     * @param value
     *            the value of the argument
     * @param type
     *            the type for the argument
     */
    public void addJmsProperty(String name, String value, String type) {
        addJmsProperty(new JMSProperty(name, value, type));
    }

    /**
     * Get a PropertyIterator of the JmsProperties.
     *
     * @return an iteration of the JmsProperties
     */
    public PropertyIterator iterator() {
        return getProperties().iterator();
    }

    /**
     * Create a string representation of the JmsProperties.
     *
     * @return the string representation of the JmsProperties
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        PropertyIterator iter = getProperties().iterator();
        while (iter.hasNext()) {
            JMSProperty arg = (JMSProperty) iter.next().getObjectValue();
            str.append(arg.toString());
            if (iter.hasNext()) {
                str.append(","); //$NON-NLS-1$
            }
        }
        return str.toString();
    }

    /**
     * Remove the specified argument from the list.
     *
     * @param row
     *            the index of the argument to remove
     */
    public void removeJmsProperty(int row) {
        if (row < getProperties().size()) {
            getProperties().remove(row);
        }
    }

    /**
     * Remove the specified argument from the list.
     *
     * @param arg
     *            the argument to remove
     */
    public void removeJmsProperty(JMSProperty arg) {
        PropertyIterator iter = getProperties().iterator();
        while (iter.hasNext()) {
            JMSProperty item = (JMSProperty) iter.next().getObjectValue();
            if (arg.equals(item)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove the argument with the specified name.
     *
     * @param argName
     *            the name of the argument to remove
     */
    public void removeJmsProperty(String argName) {
        PropertyIterator iter = getProperties().iterator();
        while (iter.hasNext()) {
            JMSProperty arg = (JMSProperty) iter.next().getObjectValue();
            if (arg.getName().equals(argName)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove all JmsProperties from the list.
     */
    public void removeAllJmsPropertys() {
        getProperties().clear();
    }

    /**
     * Get the number of JmsProperties in the list.
     *
     * @return the number of JmsProperties
     */
    public int getJmsPropertyCount() {
        return getProperties().size();
    }

    /**
     * Get a single JMSProperty.
     *
     * @param row
     *            the index of the JMSProperty to return.
     * @return the JMSProperty at the specified index, or null if no JMSProperty
     *         exists at that index.
     */
    public JMSProperty getJmsProperty(int row) {
        JMSProperty argument = null;

        if (row < getProperties().size()) {
            argument = (JMSProperty) getProperties().get(row).getObjectValue();
        }

        return argument;
    }
}
