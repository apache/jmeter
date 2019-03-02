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

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;

/*******************************************************************************
 *
 * Class representing an argument. Each argument consists of a name/value and
 * opcode combination, as well as (optional) metadata.
 *
 * author Dolf Smits(Dolf.Smits@Siemens.com) created Aug 09 2003 11:00 AM
 * company Siemens Netherlands N.V..
 *
 * Based on the work of:
 *
 * author Michael Stover author Mark Walsh
 */
public class LDAPArgument extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    // ** These constants are used in the JMX files, and so must not be changed **

    /** Name used to store the argument's name. */
    private static final String ARG_NAME = "Argument.name"; //$NON-NLS$

    /** Name used to store the argument's value. */
    private static final String VALUE = "Argument.value"; //$NON-NLS$

    /** Name used to store the argument's value. */
    private static final String OPCODE = "Argument.opcode"; //$NON-NLS$

    /** Name used to store the argument's metadata. */
    private static final String METADATA = "Argument.metadata"; //$NON-NLS$

    /**
     * Create a new Argument without a name, value, or metadata.
     */
    public LDAPArgument() {
    }

    /**
     * Create a new Argument with the specified name and value, and no metadata.
     *
     * @param name
     *            the argument name
     * @param value
     *            the argument value
     * @param opcode
     *            the operation to perform, may be one of <code>add</code>,
     *            <code>delete</code>, <code>remove</code> or
     *            <code>modify</code>.
     */
    public LDAPArgument(String name, String value, String opcode) {
        setProperty(new StringProperty(ARG_NAME, name));
        setProperty(new StringProperty(VALUE, value));
        setProperty(new StringProperty(OPCODE, opcode));
    }

    /**
     * Create a new Argument with the specified name, value, and metadata.
     *
     * @param name
     *            the argument name
     * @param value
     *            the argument value
     * @param opcode
     *            the operation to perform, may be one of <code>add</code>,
     *            <code>delete</code>, <code>remove</code> or
     *            <code>modify</code>.
     * @param metadata
     *            the argument metadata
     */
    public LDAPArgument(String name, String value, String opcode, String metadata) {
        setProperty(new StringProperty(ARG_NAME, name));
        setProperty(new StringProperty(VALUE, value));
        setProperty(new StringProperty(OPCODE, opcode));
        setProperty(new StringProperty(METADATA, metadata));
    }

    /**
     * Set the name of the Argument.
     *
     * @param newName
     *            the new name
     */
    @Override
    public void setName(String newName) {
        setProperty(new StringProperty(ARG_NAME, newName));
    }

    /**
     * Get the name of the Argument.
     *
     * @return the attribute's name
     */
    @Override
    public String getName() {
        return getPropertyAsString(ARG_NAME);
    }

    /**
     * Sets the value of the Argument.
     *
     * @param newValue
     *            the new value
     */
    public void setValue(String newValue) {
        setProperty(new StringProperty(VALUE, newValue));
    }

    /**
     * Gets the value of the Argument object.
     *
     * @return the attribute's value
     */
    public String getValue() {
        return getPropertyAsString(VALUE);
    }

    /**
     * Sets the opcode of the Argument.
     *
     * @param newOpcode
     *            the new value
     */
    public void setOpcode(String newOpcode) {
        setProperty(new StringProperty(OPCODE, newOpcode));
    }

    /**
     * Gets the opcode of the Argument object.
     *
     * @return the attribute's value
     */
    public String getOpcode() {
        return getPropertyAsString(OPCODE);
    }

    /**
     * Sets the Meta Data attribute of the Argument.
     *
     * @param newMetaData
     *            the new metadata
     */
    public void setMetaData(String newMetaData) {
        setProperty(new StringProperty(METADATA, newMetaData));
    }

    /**
     * Gets the Meta Data attribute of the Argument.
     *
     * @return the MetaData value
     */
    public String getMetaData() {
        return getPropertyAsString(METADATA);
    }
}
