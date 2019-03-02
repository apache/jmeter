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

package org.apache.jmeter.protocol.http.util.accesslog;

/**
 * Description:<br>
 * <br>
 *
 */

public class NVPair {

    protected String NAME = "";

    protected String VALUE = "";

    public NVPair() {
    }

    /**
     * The constructor takes a name and value which represent HTTP request
     * parameters.
     *
     * @param name name of the request parameter
     * @param value value of the request parameter
     */
    public NVPair(String name, String value) {
        this.NAME = name;
        this.VALUE = value;
    }

    /**
     * Set the name
     *
     * @param name name of the request parameter
     */
    public void setName(String name) {
        this.NAME = name;
    }

    /**
     * Set the value
     *
     * @param value value of the request parameter
     */
    public void setValue(String value) {
        this.VALUE = value;
    }

    /**
     * Return the name
     *
     * @return name
     */
    public String getName() {
        return this.NAME;
    }

    /**
     * Return the value
     *
     * @return value
     */
    public String getValue() {
        return this.VALUE;
    }
}
