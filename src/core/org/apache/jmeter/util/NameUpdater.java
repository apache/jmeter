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

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * @author ano ano
 * @version $Revision$
 */
public final class NameUpdater {
	private static Properties nameMap;

	private static Logger log = LoggingManager.getLoggerForClass();

	static {
		nameMap = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(JMeterUtils.getJMeterHome()
								+ JMeterUtils.getPropDefault("upgrade_properties", "/bin/upgrade.properties"));
			nameMap.load(fis);
		} catch (Exception e) {
			log.error("Bad upgrade file", e);
		} finally {
			JOrphanUtils.closeQuietly(fis);
		}
	}

	public static String getCurrentName(String className) {
		if (nameMap.containsKey(className)) {
			String newName = nameMap.getProperty(className);
			log.info("Upgrading class " + className + " to " + newName);
			return newName;
		}
		return className;
	}
    /**
     * Looks up test element / gui class combination; if that
     * does not exist in the map, then defaults to getCurrentName.
     * 
     * @param testClassName - test element class name
     * @param guiClassName - associated gui class name
     * @return new test class name
     */

    public static String getCurrentTestName(String testClassName, String guiClassName) {
        String key = testClassName + "|" + guiClassName;
        if (nameMap.containsKey(key)) {
            String newName = nameMap.getProperty(key);
            log.info("Upgrading " + key + " to " + newName);
            return newName;
        }
        return getCurrentName(testClassName);
    }

	public static String getCurrentName(String propertyName, String className) {
		String key = className + "/" + propertyName;
		if (nameMap.containsKey(key)) {
			String newName = nameMap.getProperty(key);
			log.info("Upgrading property " + propertyName + " to " + newName);
			return newName;
		}
		return propertyName;
	}

	public static String getCurrentName(String value, String propertyName, String className) {
		String key = className + "." + propertyName + "/" + value;
		if (nameMap.containsKey(key)) {
			String newValue = nameMap.getProperty(key);
			log.info("Upgrading value " + value + " to " + newValue);
			return newValue;
		}
		return value;
	}

	/**
	 * Private constructor to prevent instantiation.
	 */
	private NameUpdater() {
	}
}
