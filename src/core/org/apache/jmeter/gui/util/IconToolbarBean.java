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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterException;
import org.apache.log.Logger;

public final class IconToolbarBean {
    
    private static final Logger log = LoggingManager.getLoggerForClass();

    private final String i18nKey;
    
    private final String actionName;
    
    private final String iconPath;
    
    private final String iconPathPressed;

    /**
     * Constructor to transform a line value (from icon set file) to a icon bean for toolbar.
     * @param strToSplit - the line value (i18n key, ActionNames ID, icon path, optional icon pressed path)
     * @throws JMeterException if error in parsing.
     */
    public IconToolbarBean(final String strToSplit) throws JMeterException {
        if (strToSplit == null) {
            throw new JMeterException("No icon definition"); //$NON-NLS-1$
        }
        final String tmp[] = strToSplit.split(";"); //$NON-NLS-1$
        if (tmp.length > 2) {
            this.i18nKey = tmp[0];
            this.actionName = tmp[1];
            final String icons[] = tmp[2].split(" "); //$NON-NLS-1$
            this.iconPath = icons[0];
            this.iconPathPressed = (icons.length > 1) ? icons[1] : icons[0];
        } else {
            throw new JMeterException();
        }
    }

    /**
     * Resolve action name ID declared in icon set file to ActionNames value
     * @return the resolve actionName
     */
    public synchronized String getActionNameResolve() {
        final String aName;
        try {
            aName = (String) (ActionNames.class.getField(this.actionName).get(null));
        } catch (Exception e) {
            log.warn("Toolbar icon Action names error: " + this.actionName + ", use unknown action."); //$NON-NLS-1$
            return this.actionName; // return unknown action names for display error msg
        }
        return aName;
    }
    
    /**
     * @return the i18nKey
     */
    public synchronized String getI18nKey() {
        return i18nKey;
    }

    /**
     * @return the actionName
     */
    public synchronized String getActionName() {
        return actionName;
    }

    /**
     * @return the iconPath
     */
    public synchronized String getIconPath() {
        return iconPath;
    }

    /**
     * @return the iconPathPressed
     */
    public synchronized String getIconPathPressed() {
        return iconPathPressed;
    }

}
