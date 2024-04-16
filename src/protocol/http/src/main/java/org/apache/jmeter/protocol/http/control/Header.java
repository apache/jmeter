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

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.schema.PropertiesAccessor;

/**
 * This class is an HTTP Header encapsulator.
 *
 */
public class Header extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    /**
     * Create the header. Uses an empty name and value as default
     */
    public Header() {
        this("", ""); //$NON-NLS-1$ $NON-NLS-2$
    }

    /**
     * Create the header.
     *
     * @param name
     *            name of the header
     * @param value
     *            name of the header
     */
    public Header(String name, String value) {
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public HeaderSchema getSchema() {
        return HeaderSchema.INSTANCE;
    }

    @Override
    public PropertiesAccessor<? extends Header, ? extends HeaderSchema> getProps() {
        return new PropertiesAccessor<>(this, getSchema());
    }

    public void addConfigElement(ConfigElement config) {
    }

    public boolean expectsModification() {
        return false;
    }

    /**
     * Get the name for this object.
     *
     * @return the name of this header
     */
    @Override
    public String getName() {
        return get(getSchema().getHeaderName());
    }

    /**
     * Set the name for this object.
     *
     * @param name the name of this header
     */
    @Override
    public void setName(String name) {
        set(getSchema().getHeaderName(), name);
    }

    /**
     * Get the value for this object.
     *
     * @return the value of this header
     */
    public String getValue() {
        return get(getSchema().getValue());
    }

    /**
     * Set the value for this object.
     *
     * @param value the value of this header
     */
    public void setValue(String value) {
        set(getSchema().getValue(), value);
    }

    /**
     * Creates a string representation of this header.
     */
    @Override
    public String toString() {
        return getName() + "\t" + getValue(); //$NON-NLS-1$
    }
}
