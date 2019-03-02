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

/*
 * Created on Jun 13, 2003
 */
package org.apache.jmeter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NameUpdater {
    private static final Properties nameMap;
    // Read-only access after class has been initialised

    private static final Logger log = LoggerFactory.getLogger(NameUpdater.class);

    private static final String NAME_UPDATER_PROPERTIES = 
            "META-INF/resources/org.apache.jmeter.nameupdater.properties"; // $NON-NLS-1$

    static {
        nameMap = new Properties();
        FileInputStream fis = null;
        File f = new File(JMeterUtils.getJMeterHome(),
                JMeterUtils.getPropDefault("upgrade_properties", // $NON-NLS-1$
                        "/bin/upgrade.properties")); // $NON-NLS-1$
        try {
            fis = new FileInputStream(f);
            nameMap.load(fis);
        } catch (FileNotFoundException e) {
            log.error("Could not find upgrade file.", e);
        } catch (IOException e) {
            log.error("Error processing upgrade file: {}", f, e);
        } finally {
            JOrphanUtils.closeQuietly(fis);
        }

        //load additional name conversion rules from plugins
        Enumeration<URL> enu = null;

        try {
           enu = JMeterUtils.class.getClassLoader().getResources(NAME_UPDATER_PROPERTIES);
        } catch (IOException e) {
           log.error("Error in finding additional nameupdater.properties files.", e);
        }

        if(enu != null) {
            while(enu.hasMoreElements()) {
                URL ressourceUrl = enu.nextElement();
                log.info("Processing {}", ressourceUrl);
                Properties prop = new Properties();
                InputStream is = null;
                try {
                    is = ressourceUrl.openStream();
                    prop.load(is);
                } catch (IOException e) {
                    log.error("Error processing upgrade file: {}", ressourceUrl.getPath(), e);
                } finally {
                    JOrphanUtils.closeQuietly(is);
                }

                @SuppressWarnings("unchecked") // names are Strings
                Enumeration<String> propertyNames = (Enumeration<String>) prop.propertyNames();
                while (propertyNames.hasMoreElements()) {
                    String key = propertyNames.nextElement();
                    if (!nameMap.containsKey(key)) {
                       nameMap.put(key, prop.get(key));
                       log.info("Added additional nameMap entry: {}", key);
                    } else {
                       log.warn("Additional nameMap entry: '{}' rejected as already defined.", key);
                    }
                }
            }
        }
    }

    /**
     * Looks up the class name; if that does not exist in the map, 
     * then defaults to the input name.
     * 
     * @param className the classname from the script file
     * @return the class name to use, possibly updated.
     */
    public static String getCurrentName(String className) {
        if (nameMap.containsKey(className)) {
            String newName = nameMap.getProperty(className);
            log.info("Upgrading class {} to {}", className, newName);
            return newName;
        }
        return className;
    }

    /**
     * Looks up test element / gui class combination; if that
     * does not exist in the map, then defaults to getCurrentName(testClassName).
     *
     * @param testClassName - test element class name
     * @param guiClassName - associated gui class name
     * @return new test class name
     */
    public static String getCurrentTestName(String testClassName, String guiClassName) {
        String key = testClassName + "|" + guiClassName;
        if (nameMap.containsKey(key)) {
            String newName = nameMap.getProperty(key);
            log.info("Upgrading {} to {}", key, newName);
            return newName;
        }
        return getCurrentName(testClassName);
    }

    /**
     * Looks up class name / property name combination; if that
     * does not exist in the map, then defaults to input property name.
     *
     * @param propertyName - property name to check
     * @param className - class name containing the property
     * @return possibly updated property name
     */
    public static String getCurrentName(String propertyName, String className) {
        String key = className + "/" + propertyName;
        if (nameMap.containsKey(key)) {
            String newName = nameMap.getProperty(key);
            log.info("Upgrading property {} to {}", propertyName, newName);
            return newName;
        }
        return propertyName;
    }

    /**
     * Looks up class name . property name / value combination;
     * if that does not exist in the map, returns the original value.
     * 
     * @param value the value to be checked
     * @param propertyName the name of the property
     * @param className the class containing the propery.
     * @return the value, updated if necessary
     */
    public static String getCurrentName(String value, String propertyName, String className) {
        String key = className + "." + propertyName + "/" + value;
        if (nameMap.containsKey(key)) {
            String newValue = nameMap.getProperty(key);
            log.info("Upgrading value {} to {}", value, newValue);
            return newValue;
        }
        return value;
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private NameUpdater() {
    }

    /**
     * Check if a key is in the map; intended for use by 
     * {@link org.apache.jmeter.save.SaveService#checkClasses() SaveService#checkClasses()}
     * only.
     * 
     * @param key name of the key to check
     * @return true if the key is in the map
     */
    public static boolean isMapped(String key) {
        return nameMap.containsKey(key);
    }
}
