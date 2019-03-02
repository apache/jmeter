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

package org.apache.jmeter.gui.util;

import org.apache.jmeter.gui.action.ActionNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IconToolbarBean {

    private static final Logger log = LoggerFactory.getLogger(IconToolbarBean.class);

    private static final String ICON_FIELD_SEP = ",";  //$NON-NLS-1$

    private final String i18nKey;

    private final String actionName;

    private final String iconPath;

    private final String iconPathPressed;

    /**
     * Constructor to transform a line value (from icon set file) to a icon bean for toolbar.
     * @param strToSplit - the line value (i18n key, ActionNames ID, icon path, optional icon pressed path)
     * @throws IllegalArgumentException if error in parsing.
     */
    IconToolbarBean(final String strToSplit, final String iconSize) throws IllegalArgumentException {
        if (strToSplit == null) {
            throw new IllegalArgumentException("Icon definition must not be null"); //$NON-NLS-1$
        }
        final String[] tmp = strToSplit.split(ICON_FIELD_SEP);
        if (tmp.length > 2) {
            this.i18nKey = tmp[0];
            this.actionName = tmp[1];
            this.iconPath = tmp[2].replace("<SIZE>", iconSize); //$NON-NLS-1$
            this.iconPathPressed = (tmp.length > 3) ? tmp[3].replace("<SIZE>", iconSize) : this.iconPath; //$NON-NLS-1$
        } else {
            throw new IllegalArgumentException("Incorrect argument format - expected at least 2 fields separated by " + ICON_FIELD_SEP);
        }
    }

    /**
     * Resolve action name ID declared in icon set file to ActionNames value
     * @return the resolve actionName
     */
    public String getActionNameResolve() {
        final String aName;
        try {
            aName = (String) (ActionNames.class.getField(this.actionName).get(null));
        } catch (Exception e) {
            log.warn("Toolbar icon Action names error: {}, use unknown action.", this.actionName); //$NON-NLS-1$
            return this.actionName; // return unknown action names for display error msg
        }
        return aName;
    }

    /**
     * @return the i18nKey
     */
    public String getI18nKey() {
        return i18nKey;
    }

    /**
     * @return the actionName
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * @return the iconPath
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * @return the iconPathPressed
     */
    public String getIconPathPressed() {
        return iconPathPressed;
    }

}
