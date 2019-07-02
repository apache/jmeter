/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;

import org.apache.jmeter.testelement.AbstractTestElement;

/**
 * Bean containing mapping from name to array of Addresses
 * @since 3.2
 */
public class StaticHost extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SNAME = "StaticHost.Name";
    private static final String SADDRESS = "StaticHost.Address";

    public StaticHost() {
        this("", "");
    }

    public StaticHost(String name, String address) {
        setProperty(SNAME, name);
        setProperty(SADDRESS, address);
    }

    @Override
    public void setName(String name) {
        setProperty(SNAME, name);
    }

    @Override
    public String getName() {
        return getPropertyAsString(SNAME);
    }

    public void setAddress(String address) {
        setProperty(SADDRESS, address);
    }

    public String getAddress() {
        return getPropertyAsString(SADDRESS);
    }

    @Override
    public String toString() {
        return String.format("StaticHost(%s, %s)", getName(), getAddress());
    }
}
