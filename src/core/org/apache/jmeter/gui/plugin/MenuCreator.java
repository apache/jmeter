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

package org.apache.jmeter.gui.plugin;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

/**
 * @since 2.10
 */
public interface MenuCreator {
    enum MENU_LOCATION {
        FILE,
        EDIT,
        RUN,
        OPTIONS,
        HELP,
        SEARCH,
        TOOLS
    }
    
    /**
     * MenuItems to be added in location menu
     * @param location in top menu
     * @return array of {@link JMenuItem}
     */
    JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location);

    /**
     * @return array of JMenu to be put as top level menu between Options and Help
     */
    JMenu[] getTopLevelMenus();

    /**
     * @param menu MenuElement
     * @return true if menu was concerned by Locale change
     */
    boolean localeChanged(MenuElement menu);

    /**
     * Update Top Level menu on Locale Change
     */
    void localeChanged();
}
