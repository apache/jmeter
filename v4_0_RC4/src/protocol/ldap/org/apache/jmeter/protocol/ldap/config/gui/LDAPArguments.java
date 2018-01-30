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

package org.apache.jmeter.protocol.ldap.config.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * A set of LDAPArgument objects. author Dolf Smits(Dolf.Smits@Siemens.com)
 * created Aug 09 2003 11:00 AM company Siemens Netherlands N.V..
 *
 * Based on the work of:
 *
 * author Michael Stover author Mark Walsh
 */
public class LDAPArguments extends ConfigTestElement implements Serializable {
    private static final long serialVersionUID = 240L;

    /** The name of the property used to store the arguments. */
    public static final String ARGUMENTS = "Arguments.arguments"; //$NON-NLS$

    /**
     * Create a new Arguments object with no arguments.
     */
    public LDAPArguments() {
        setProperty(new CollectionProperty(ARGUMENTS, new ArrayList<>()));
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
        setProperty(new CollectionProperty(ARGUMENTS, new ArrayList<>()));
    }

    /**
     * Set the list of arguments. Any existing arguments will be lost.
     *
     * @param arguments
     *            the new arguments
     */
    public void setArguments(List<Object> arguments) {
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
        Map<String, String> argMap = new HashMap<>();
        while (iter.hasNext()) {
            LDAPArgument arg = (LDAPArgument) iter.next().getObjectValue();
            argMap.put(arg.getName(), arg.getValue());
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
     * @param opcode
     *            the operation to perform, may be one of <code>add</code>,
     *            <code>delete</code>, <code>remove</code> or
     *            <code>modify</code>.
     */
    public void addArgument(String name, String value, String opcode) {
        addArgument(new LDAPArgument(name, value, opcode, null));
    }

    /**
     * Add a new argument.
     *
     * @param arg
     *            the new argument
     */
    public void addArgument(LDAPArgument arg) {
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
     * @param opcode
     *            the operation to perform, may be one of <code>add</code>,
     *            <code>delete</code>, <code>remove</code> or
     *            <code>modify</code>.
     * @param metadata
     *            the metadata for the argument
     */
    public void addArgument(String name, String value, String opcode, String metadata) {
        addArgument(new LDAPArgument(name, value, opcode, metadata));
    }

    /**
     * Get a PropertyIterator of the arguments.
     *
     * @return an iteration of the arguments
     */
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
            LDAPArgument arg = (LDAPArgument) iter.next().getObjectValue();
            final String metaData = arg.getMetaData();
            str.append(arg.getName());
            if (metaData == null) {
                str.append("=");  //$NON-NLS$
            } else {
                str.append(metaData);
            }
            str.append(arg.getValue());
            if (iter.hasNext()) {
                str.append("&"); //$NON-NLS$
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
    public void removeArgument(LDAPArgument arg) {
        PropertyIterator iter = getArguments().iterator();
        while (iter.hasNext()) {
            LDAPArgument item = (LDAPArgument) iter.next().getObjectValue();
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
            LDAPArgument arg = (LDAPArgument) iter.next().getObjectValue();
            if (arg.getName().equals(argName)) {
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
        addArgument(new LDAPArgument("", "", "", null));
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
    public LDAPArgument getArgument(int row) {
        LDAPArgument argument = null;

        if (row < getArguments().size()) {
            argument = (LDAPArgument) getArguments().get(row).getObjectValue();
        }

        return argument;
    }
}
