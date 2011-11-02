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

package org.apache.jmeter.plugin;

import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.jmeter.gui.GUIFactory;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public final class PluginManager {
    private static final PluginManager instance = new PluginManager();

    private static final Logger log = LoggingManager.getLoggerForClass();

    private PluginManager() {
    }

    /**
     * Installs a plugin.
     *
     * @param plugin
     *            the plugin to install
     * @param useGui
     *            indication of whether or not the gui will be used
     */
    public static void install(JMeterPlugin plugin, boolean useGui) {
        if (useGui) {
            instance.installPlugin(plugin);
        }
    }

    private void installPlugin(JMeterPlugin plugin) {
        String[][] icons = plugin.getIconMappings();
        ClassLoader classloader = plugin.getClass().getClassLoader();

        for (int i = 0; i < icons.length; i++) {
            URL resource = classloader.getResource(icons[i][1].trim());

            if (resource == null) {
                log.warn("Can't find icon for " + icons[i][0] + " - " + icons[i][1]);
            } else {
                GUIFactory.registerIcon(icons[i][0], new ImageIcon(resource));
                if (icons[i].length > 2 && icons[i][2] != null) {
                    URL resource2 = classloader.getResource(icons[i][2].trim());
                    if (resource2 == null) {
                        log.info("Can't find disabled icon for " + icons[i][0] + " - " + icons[i][2]);
                    } else {
                        GUIFactory.registerDisabledIcon(icons[i][0], new ImageIcon(resource2));
                    }
                }
            }
        }
    }
}
