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

import org.apache.jmeter.testelement.property.StringProperty;

public class LoginConfig extends ConfigTestElement implements Serializable
// TODO: move this to components -- the only reason why it's in core is because
// it's used as a guinea pig by a couple of tests.
{
    private static final long serialVersionUID = 240L;

    /**
     * Constructor for the LoginConfig object.
     */
    public LoginConfig() {
    }

    /**
     * Sets the Username attribute of the LoginConfig object.
     *
     * @param username
     *            the new Username value
     */
    public void setUsername(String username) {
        setProperty(new StringProperty(ConfigTestElement.USERNAME, username));
    }

    /**
     * Sets the Password attribute of the LoginConfig object.
     *
     * @param password
     *            the new Password value
     */
    public void setPassword(String password) {
        setProperty(new StringProperty(ConfigTestElement.PASSWORD, password));
    }

    /**
     * Gets the Username attribute of the LoginConfig object.
     *
     * @return the Username value
     */
    public String getUsername() {
        return getPropertyAsString(ConfigTestElement.USERNAME);
    }

    /**
     * Gets the Password attribute of the LoginConfig object.
     *
     * @return the Password value
     */
    public String getPassword() {
        return getPropertyAsString(ConfigTestElement.PASSWORD);
    }

    @Override
    public String toString() {
        return getUsername() + "=" + getPassword(); //$NON-NLS-1$
    }
}
