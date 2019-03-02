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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.visualizers;

import java.awt.Font;
import java.awt.Shape;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jmeter.util.JMeterUtils;
import org.jCharts.properties.LegendAreaProperties;
import org.jCharts.properties.PointChartProperties;

public class StatGraphProperties {

    private static final String[] FONT_SIZE = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "24", "28", "32"};

    private static final String[] STROKE_WIDTH = { "1.0f", "1.5f", "2.0f", "2.5f", "3.0f", "3.5f", "4.0f", "4.5f", "5.0f", "5.5f", "6.0f", "6.5f"};

    public static Map<String, String> getFontNameMap() {
        Map<String, String> fontNameMap = new LinkedHashMap<>(2);
        fontNameMap.put(JMeterUtils.getResString("font.sansserif"), "SansSerif"); //$NON-NLS-1$ //$NON-NLS-1$
        fontNameMap.put(JMeterUtils.getResString("font.serif"), "Serif"); //$NON-NLS-1$
        return fontNameMap;
    }
    
    /**
     * @return array of String containing font sizes
     */
    public static final String[] getFontSize() {
        String[] fontSize = new String[FONT_SIZE.length];
        System.arraycopy(FONT_SIZE, 0, fontSize, 0, FONT_SIZE.length);
        return fontSize;
    }

    /**
     * @return array of String containing stroke widths
     */
    public static final String[] getStrokeWidth() {
        String[] strokeWidth = new String[STROKE_WIDTH.length];
        System.arraycopy(STROKE_WIDTH, 0, strokeWidth, 0, STROKE_WIDTH.length);
        return strokeWidth;
    }

    @SuppressWarnings("boxing")
    public static Map<String, Integer> getFontStyleMap() {
        Map<String, Integer> fontStyleMap = new LinkedHashMap<>(3);
        fontStyleMap.put(JMeterUtils.getResString("fontstyle.normal"), Font.PLAIN); //$NON-NLS-1$
        fontStyleMap.put(JMeterUtils.getResString("fontstyle.bold"), Font.BOLD); //$NON-NLS-1$
        fontStyleMap.put(JMeterUtils.getResString("fontstyle.italic"), Font.ITALIC); //$NON-NLS-1$
        return fontStyleMap;
    }

    @SuppressWarnings("boxing")
    public static Map<String, Integer> getPlacementNameMap() {
        Map<String, Integer> placementNameMap = new LinkedHashMap<>(4);
        placementNameMap.put(JMeterUtils.getResString("aggregate_graph_legend.placement.bottom"), LegendAreaProperties.BOTTOM); //$NON-NLS-1$
        placementNameMap.put(JMeterUtils.getResString("aggregate_graph_legend.placement.right"), LegendAreaProperties.RIGHT); //$NON-NLS-1$
        placementNameMap.put(JMeterUtils.getResString("aggregate_graph_legend.placement.left"), LegendAreaProperties.LEFT); //$NON-NLS-1$
        placementNameMap.put(JMeterUtils.getResString("aggregate_graph_legend.placement.top"), LegendAreaProperties.TOP); //$NON-NLS-1$
        return placementNameMap;
    }
    
    public static Map<String, Shape> getPointShapeMap() {
        // We want to retain insertion order, so LinkedHashMap is necessary
        Map<String, Shape> pointShapeMap = new LinkedHashMap<>(5);
        pointShapeMap.put(JMeterUtils.getResString("graph_pointshape_circle"), PointChartProperties.SHAPE_CIRCLE); //$NON-NLS-1$
        pointShapeMap.put(JMeterUtils.getResString("graph_pointshape_diamond"), PointChartProperties.SHAPE_DIAMOND); //$NON-NLS-1$
        pointShapeMap.put(JMeterUtils.getResString("graph_pointshape_square"), PointChartProperties.SHAPE_SQUARE); //$NON-NLS-1$
        pointShapeMap.put(JMeterUtils.getResString("graph_pointshape_triangle"), PointChartProperties.SHAPE_TRIANGLE); //$NON-NLS-1$
        pointShapeMap.put(JMeterUtils.getResString("graph_pointshape_none"), null); //$NON-NLS-1$
        return pointShapeMap;
    }
}
