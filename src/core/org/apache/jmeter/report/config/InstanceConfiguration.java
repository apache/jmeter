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
package org.apache.jmeter.report.config;

/**
 * The class InstanceConfiguration describe the configuration of an item that
 * can be instantiated by its class name.
 *
 * @since 3.0
 */
public class InstanceConfiguration extends SubConfiguration {

    private String className;

    /**
     * Gets the class name of the item.
     *
     * @return the class name of the item
     */
    public final String getClassName() {
        return className;
    }

    /**
     * Sets the class name of the item.
     *
     * @param className
     *            the new class name
     */
    public final void setClassName(String className) {
        this.className = className;
    }

}
