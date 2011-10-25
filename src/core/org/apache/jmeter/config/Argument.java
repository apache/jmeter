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

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * Class representing an argument. Each argument consists of a name/value pair,
 * as well as (optional) metadata.
 *
 */
public class Argument extends AbstractTestElement implements Serializable {
    private static final long serialVersionUID = 240L;

/** Name used to store the argument's name. */
    public static final String ARG_NAME = "Argument.name";

    /** Name used to store the argument's value. */
    public static final String VALUE = "Argument.value";

    /** Name used to store the argument's description. */
    public static final String DESCRIPTION = "Argument.desc";

    /** Name used to store the argument's metadata. */
    public static final String METADATA = "Argument.metadata";

    /**
     * Create a new Argument without a name, value, or metadata.
     */
    public Argument() {
    }

    /**
     * Create a new Argument with the specified name and value, and no metadata.
     *
     * @param name
     *            the argument name
     * @param value
     *            the argument value
     */
    public Argument(String name, String value) {
        setProperty(new StringProperty(ARG_NAME, name));
        setProperty(new StringProperty(VALUE, value));
    }

    /**
     * Create a new Argument with the specified name, value, and metadata.
     *
     * @param name
     *            the argument name
     * @param value
     *            the argument value
     * @param metadata
     *            the argument metadata
     */
    public Argument(String name, String value, String metadata) {
        setProperty(new StringProperty(ARG_NAME, name));
        setProperty(new StringProperty(VALUE, value));
        setProperty(new StringProperty(METADATA, metadata));
    }

    /**
     * Create a new Argument with the specified name, value, and metadata.
     *
     * @param name
     *            the argument name
     * @param value
     *            the argument value
     * @param metadata
     *            the argument metadata
     * @param description 
     *            the argument description
     */
    public Argument(String name, String value, String metadata, String description) {
        setProperty(new StringProperty(ARG_NAME, name));
        setProperty(new StringProperty(VALUE, value));
        setProperty(new StringProperty(METADATA, metadata));
        setProperty(new StringProperty(DESCRIPTION, description));
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
     * Sets the Description attribute of the Argument.
     *
     * @param description
     *            the new description
     */
    public void setDescription(String description) {
        setProperty(new StringProperty(DESCRIPTION, description));
    }

    /**
     * Gets the Meta Data attribute of the Argument.
     *
     * @return the MetaData value
     */
    public String getDescription() {
        return getPropertyAsString(DESCRIPTION);
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

    @Override
    public String toString() {
        return getName() + getMetaData() + getValue();
    }

    /**
     * Is this parameter skippable, i.e. empty/blank string
     * or it looks like an unrecognised variable.
     *
     * @param parameterName - parameter name
     * @return true if parameter should be skipped
     */
    public boolean isSkippable(String parameterName) {
        if (parameterName.trim().length()==0){
            return true; // Skip parameters with a blank name (allows use of optional variables in parameter lists)
        }
        // TODO: improve this test
        if (parameterName.trim().startsWith("${") && parameterName.endsWith("}")){// $NON-NLS-1$ $NON-NLS-2$
            return true; // Missing variable name
        }
        return false;
    }

}
