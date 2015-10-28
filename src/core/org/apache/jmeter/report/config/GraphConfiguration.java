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

import java.util.HashMap;
import java.util.Map;

/**
 * The class GraphConfiguration describes a configuration of a graph.
 *
 * @since 2.14
 */
public class GraphConfiguration {

    private Double abscissaMax;
    private Double abscissaMin;
    private String className;
    private boolean excludeControllers;
    private Double ordinateMin;
    private Double ordinateMax;
    private HashMap<String, String> properties = new HashMap<String, String>();
    private String title;

    /**
     * Gets the maximum abscissa.
     *
     * @return the maximum abscissa
     */
    public final Double getAbscissaMax() {
	return abscissaMax;
    }

    /**
     * Sets the maximum abscissa.
     *
     * @param abscissaMax
     *            the maximum abscissa to set
     */
    public final void setAbscissaMax(Double abscissaMax) {
	this.abscissaMax = abscissaMax;
    }

    /**
     * Gets the minimum abscissa.
     *
     * @return the minimum abscissa
     */
    public final Double getAbscissaMin() {
	return abscissaMin;
    }

    /**
     * Sets the minimum abscissa.
     *
     * @param abscissaMin
     *            the minimum abscissa to set
     */
    public final void setAbscissaMin(Double abscissaMin) {
	this.abscissaMin = abscissaMin;
    }

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
     * Gets the class name of the graph.
     *
     * @return the class name of the graph
     */
    public final String getClassName() {
	return className;
    }

    /**
     * Sets the class name of the graph.
     *
     * @param className
     *            the new class name
     */
    public final void setClassName(String className) {
	this.className = className;
    }

    /**
     * Gets the minimum ordinate.
     *
     * @return the minimum ordinate
     */
    public final Double getOrdinateMin() {
	return ordinateMin;
    }

    /**
     * Sets the minimum ordinate.
     *
     * @param ordinateMin
     *            the minimum ordinate to set
     */
    public final void setOrdinateMin(Double ordinateMin) {
	this.ordinateMin = ordinateMin;
    }

    /**
     * Gets the maximum ordinate.
     *
     * @return the maximum ordinate
     */
    public final Double getOrdinateMax() {
	return ordinateMax;
    }

    /**
     * Sets the maximum ordinate.
     *
     * @param ordinateMax
     *            the maximum ordinate to set
     */
    public final void setOrdinateMax(Double ordinateMax) {
	this.ordinateMax = ordinateMax;
    }

    /**
     * Gets the properties of the graph.
     *
     * @return the properties of the graph
     */
    public final Map<String, String> getProperties() {
	return properties;
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
