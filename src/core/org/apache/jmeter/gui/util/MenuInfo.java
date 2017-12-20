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

package org.apache.jmeter.gui.util;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.JMeterGUIComponent;

/**
 * Class to hold additional information needed when building the GUI lists
 */
public class MenuInfo {

    public static final int SORT_ORDER_DEFAULT = 100;
    private final String label;
    private final String className;
    private final JMeterGUIComponent guiComp;
    private final int sortOrder;

    public MenuInfo(String displayLabel, String classFullName) {
        label = displayLabel;
        className = classFullName;
        guiComp = null;
        sortOrder = getSortOrderFromName(classFullName);
    }

    public MenuInfo(JMeterGUIComponent item, String classFullName) {
        label = item.getStaticLabel();
        className = classFullName;
        guiComp = item;
        sortOrder = getSortOrderFromName(classFullName);
    }

    private int getSortOrderFromName(String classFullName) {
        try {
            GUIMenuSortOrder menuSortOrder = Class.forName(classFullName)
                    .getDeclaredAnnotation(GUIMenuSortOrder.class);
            if (menuSortOrder != null) {
                return menuSortOrder.value();
            }
        } catch (ClassNotFoundException ignored) {
        }
        return SORT_ORDER_DEFAULT;
    }

    public String getLabel() {
        if (guiComp != null) {
            return guiComp.getStaticLabel();
        }
        return label;
    }

    public String getClassName(){
        return className;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
