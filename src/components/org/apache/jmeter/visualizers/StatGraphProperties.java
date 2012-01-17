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
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.util.JMeterUtils;
import org.jCharts.properties.LegendProperties;

public class StatGraphProperties {

    public static final String[] fontSize = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "24", "28", "32"};

    public static Map<String, String> getFontNameMap() {
        Map<String, String> fontNameMap = new HashMap<String, String>();
        fontNameMap.put(JMeterUtils.getResString("font.sansserif"), "SansSerif");
        fontNameMap.put(JMeterUtils.getResString("font.serif"), "Serif");
        return fontNameMap;
    }

    @SuppressWarnings("boxing")
    public static Map<String, Integer> getFontStyleMap() {
        Map<String, Integer> fontStyleMap = new HashMap<String, Integer>();
        fontStyleMap.put(JMeterUtils.getResString("fontstyle.normal"), Font.PLAIN);
        fontStyleMap.put(JMeterUtils.getResString("fontstyle.bold"), Font.BOLD);
        fontStyleMap.put(JMeterUtils.getResString("fontstyle.italic"), Font.ITALIC);
        return fontStyleMap;
    }

    @SuppressWarnings("boxing")
    public static Map<String, Integer> getPlacementNameMap() {
        Map<String, Integer> placementNameMap = new HashMap<String, Integer>();
        placementNameMap.put(JMeterUtils.getResString("aggregate_graph_legend.placement.bottom"), LegendProperties.BOTTOM);
        placementNameMap.put(JMeterUtils.getResString("aggregate_graph_legend.placement.right"), LegendProperties.RIGHT);
        placementNameMap.put(JMeterUtils.getResString("aggregate_graph_legend.placement.left"), LegendProperties.LEFT);
        placementNameMap.put(JMeterUtils.getResString("aggregate_graph_legend.placement.top"), LegendProperties.TOP);
        return placementNameMap;
    }
}
