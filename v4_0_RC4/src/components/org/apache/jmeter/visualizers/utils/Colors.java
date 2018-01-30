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

package org.apache.jmeter.visualizers.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Colors {

    private static final Logger log = LoggerFactory.getLogger(Colors.class);

    private static final String ENTRY_SEP = ",";  //$NON-NLS-1$

    private static final String ORDER_PROP_NAME = "order"; //$NON-NLS-1$

    protected static final String DEFAULT_COLORS_PROPERTY_FILE = "org/apache/jmeter/visualizers/utils/colors.properties"; //$NON-NLS-1$

    protected static final String USER_DEFINED_COLORS_PROPERTY_FILE = "jmeter.colors"; //$NON-NLS-1$
    
    private static final String COLORS_ORDER = "jmeter.order";
    
    public static final Color LIGHT_RED = new Color(0xFF, 0x80, 0x80);
    /**
     * Parse icon set file.
     * @return List of icons/action definition
     */
    public static List<Color> getColors() {
        Properties defaultProps = JMeterUtils.loadProperties(DEFAULT_COLORS_PROPERTY_FILE);
        if (defaultProps == null) {
            JOptionPane.showMessageDialog(null, 
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }
        Properties p;
        String userProp = JMeterUtils.getProperty(USER_DEFINED_COLORS_PROPERTY_FILE); 
        if (userProp != null){
            p = JMeterUtils.loadProperties(userProp, defaultProps);
        } else {
            p=defaultProps;
        }

        String order = JMeterUtils.getPropDefault(COLORS_ORDER, p.getProperty(ORDER_PROP_NAME));

        if (order == null) {
            log.warn("Could not find order list");
            JOptionPane.showMessageDialog(null, 
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JMeterUtils.getResString("toolbar_icon_set_not_found"), // $NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
            return null;
        }

        String[] oList = order.split(ENTRY_SEP);

        List<Color> listColors = new ArrayList<>();
        for (String key : oList) {
            String trimmed = key.trim();
            String property = p.getProperty(trimmed);
            try {
                String[] lcol = property.split(ENTRY_SEP);
                Color itb = new Color(Integer.parseInt(lcol[0]), Integer.parseInt(lcol[1]), Integer.parseInt(lcol[2]));
                listColors.add(itb);
            } catch (java.lang.Exception e) {
                log.warn("Error in colors.properties, current property={}", property); // $NON-NLS-1$
            }
        }
        return listColors;
    }

}
