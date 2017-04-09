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

package org.apache.jmeter.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * A set of Argument objects.
 *
 */
public class Arguments extends ConfigTestElement implements Serializable, Iterable<JMeterProperty> {
    private static final long serialVersionUID = 240L;

    /** The name of the property used to store the arguments. */
    public static final String ARGUMENTS = "Arguments.arguments"; //$NON-NLS-1$

    /**
     * Create a new Arguments object with no arguments.
     */
    public Arguments() {
        setProperty(new CollectionProperty(ARGUMENTS, new ArrayList<Argument>()));
    }

    /**
     * Get the arguments.
     *
     * @return the arguments
     */
    public CollectionProperty getArguments() {
        return (CollectionProperty) getProperty(ARGUMENTS);
    }

    /**
     * Clear the arguments.
     */
    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(ARGUMENTS, new ArrayList<Argument>()));
    }

    /**
     * Set the list of arguments. Any existing arguments will be lost.
     *
     * @param arguments
     *            the new arguments
     */
    public void setArguments(List<Argument> arguments) {
        setProperty(new CollectionProperty(ARGUMENTS, arguments));
    }

    /**
     * Get the arguments as a Map. Each argument name is used as the key, and
     * its value as the value.
     *
     * @return a new Map with String keys and values containing the arguments
     */
    public Map<String, String> getArgumentsAsMap() {
        PropertyIterator iter = getArguments().iterator();
        Map<String, String> argMap = new LinkedHashMap<>();
        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next().getObjectValue();
            // Because CollectionProperty.mergeIn will not prevent adding two
            // properties of the same name, we need to select the first value so
            // that this element's values prevail over defaults provided by
            // configuration
            // elements:
            if (!argMap.containsKey(arg.getName())) {
                argMap.put(arg.getName(), arg.getValue());
            }
        }
        return argMap;
    }

    /**
     * Add a new argument with the given name and value.
     *
     * @param name
     *            the name of the argument
     * @param value
     *            the value of the argument
     */
    public void addArgument(String name, String value) {
        addArgument(new Argument(name, value, null));
    }

    /**
     * Add a new argument.
     *
     * @param arg
     *            the new argument
     */
    public void addArgument(Argument arg) {
        TestElementProperty newArg = new TestElementProperty(arg.getName(), arg);
        if (isRunningVersion()) {
            this.setTemporary(newArg);
        }
        getArguments().addItem(newArg);
    }

    /**
     * Add a new argument with the given name, value, and metadata.
     *
     * @param name
     *            the name of the argument
     * @param value
     *            the value of the argument
     * @param metadata
     *            the metadata for the argument
     */
    public void addArgument(String name, String value, String metadata) {
        addArgument(new Argument(name, value, metadata));
    }

    /**
     * Add a new argument with the given name, value, metadata and description
     *
     * @param name
     *            the name of the argument
     * @param value
     *            the value of the argument
     * @param metadata
     *            the metadata for the argument
     * @param description
     *            the argument description
     */
    public void addArgument(String name, String value, String metadata, String description) {
        addArgument(new Argument(name, value, metadata, description));
    }

    /**
     * Get a PropertyIterator of the arguments.
     *
     * @return an iteration of the arguments
     */
    @Override
    public PropertyIterator iterator() {
        return getArguments().iterator();
    }

    /**
     * Create a string representation of the arguments.
     *
     * @return the string representation of the arguments
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        PropertyIterator iter = getArguments().iterator();
        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next().getObjectValue();
            final String metaData = arg.getMetaData();
            str.append(arg.getName());
            if (metaData == null) {
                str.append("="); //$NON-NLS-1$
            } else {
                str.append(metaData);
            }
            str.append(arg.getValue());
            final String desc = arg.getDescription();
            if (desc != null) {
                str.append("(");
                str.append(desc);
                str.append(")");
            }
            if (iter.hasNext()) {
                str.append("&"); //$NON-NLS-1$
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
    public void removeArgument(int row) {
        if (row < getArguments().size()) {
            getArguments().remove(row);
        }
    }

    /**
     * Remove the specified argument from the list.
     *
     * @param arg
     *            the argument to remove
     */
    public void removeArgument(Argument arg) {
        PropertyIterator iter = getArguments().iterator();
        while (iter.hasNext()) {
            Argument item = (Argument) iter.next().getObjectValue();
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
    public void removeArgument(String argName) {
        PropertyIterator iter = getArguments().iterator();
        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next().getObjectValue();
            if (arg.getName().equals(argName)) {
                iter.remove();
            }
        }
    }

    /**
     * Remove the argument with the specified name and value.
     *
     * @param argName
     *            the name of the argument to remove
     * @param argValue the value to compare - must not be null
     */
    public void removeArgument(String argName, String argValue) {
        PropertyIterator iter = getArguments().iterator();
        while (iter.hasNext()) {
            Argument arg = (Argument) iter.next().getObjectValue();
            if (arg.getName().equals(argName) && argValue.equals(arg.getValue())) {
                iter.remove();
            }
        }
    }

    /**
     * Remove all arguments from the list.
     */
    public void removeAllArguments() {
        getArguments().clear();
    }

    /**
     * Add a new empty argument to the list. The new argument will have the
     * empty string as its name and value, and null metadata.
     */
    public void addEmptyArgument() {
        addArgument(new Argument("", "", null));
    }

    /**
     * Get the number of arguments in the list.
     *
     * @return the number of arguments
     */
    public int getArgumentCount() {
        return getArguments().size();
    }

    /**
     * Get a single argument.
     *
     * @param row
     *            the index of the argument to return.
     * @return the argument at the specified index, or null if no argument
     *         exists at that index.
     */
    public Argument getArgument(int row) {
        Argument argument = null;

        if (row < getArguments().size()) {
            argument = (Argument) getArguments().get(row).getObjectValue();
        }

        return argument;
    }
}
