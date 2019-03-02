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
 * The class GraphConfiguration describes a configuration of a graph.
 *
 * @since 3.0
 */
public class GraphConfiguration extends InstanceConfiguration {

    private boolean excludeControllers;
    private String title;

    /**
     * Checks if controller samples have to be filtered.
     *
     * @return true if controller samples have to be filtered; false otherwise.
     */
    public final boolean excludesControllers() {
        return excludeControllers;
    }

    /**
     * Sets a switch used to check if controller samples have to be filtered.
     *
     * @param excludeControllers
     *            the switch value to set
     */
    public final void setExcludeControllers(boolean excludeControllers) {
        this.excludeControllers = excludeControllers;
    }

    /**
     * Gets the title of the graph.
     *
     * @return the title of the graph
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Sets the title of the graph.
     *
     * @param title
     *            the title of the graph to set
     */
    public final void setTitle(String title) {
        this.title = title;
    }

}
