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
package org.apache.jmeter.report.processor.graph;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * @since 2.14
 *
 */
public class GraphConsumerResult {
    /**
     * The class KeyResult provides a map of the points of the graph.
     */
    public static class KeyResult extends TreeMap<Double, Double> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1624054352804358620L;
    }

    /**
     * The class SeriesResult is a map used to store graph data by series.
     */
    public static class SeriesResult extends TreeMap<String, KeyResult> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1198918741042365979L;
    }

    /**
     * The class GroupResult is a map used to store graph result by group.
     */
    public static class GroupResult extends HashMap<String, SeriesResult> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 597243835019881209L;

    }

    private double minX = Double.POSITIVE_INFINITY;
    private double maxX = Double.NEGATIVE_INFINITY;
    private double minY = Double.POSITIVE_INFINITY;
    private double maxY = Double.NEGATIVE_INFINITY;
    private GroupResult groupResult = new GroupResult();

    /**
     * @return the minX
     */
    public final double getMinX() {
	return minX;
    }

    /**
     * @param minX
     *            the minX to set
     */
    public final void setMinX(double minX) {
	this.minX = Math.min(this.minX, minX);
    }

    /**
     * @return the maxX
     */
    public final double getMaxX() {
	return maxX;
    }

    /**
     * @param maxX
     *            the maxX to set
     */
    public final void setMaxX(double maxX) {
	this.maxX = Math.max(this.maxX, maxX);
    }

    /**
     * @return the minY
     */
    public final double getMinY() {
	return minY;
    }

    /**
     * @param minY
     *            the minY to set
     */
    public final void setMinY(double minY) {
	this.minY = Math.min(this.minY, minY);
    }

    /**
     * @return the maxY
     */
    public final double getMaxY() {
	return maxY;
    }

    /**
     * @param maxY
     *            the maxY to set
     */
    public final void setMaxY(double maxY) {
	this.maxY = Math.max(this.maxY, maxY);
    }

    /**
     * Gets the group result.
     *
     * @return the group result
     */
    public final GroupResult getGroupResult() {
	return groupResult;
    }

}