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

package org.apache.jmeter.logging;

import java.util.HashMap;
import java.util.Map;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Hierarchy;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * Implementation of {@link ILoggerFactory} for Logkit
 * @since 3.0
 */
public class LogkitLoggerFactory implements ILoggerFactory {
    // key: name (String), value: a Log4jLogger;
    private final Map<String, Logger> loggerMap;

    /**
     * 
     */
    public LogkitLoggerFactory() {
        loggerMap = new HashMap<>();
    }

    /**
     * 
     * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
     */
    @Override
    public Logger getLogger(String name) {
        Logger slf4jLogger = null;
        if(name == null) {
            name = Logger.ROOT_LOGGER_NAME;
        }
        // protect against concurrent access of loggerMap
        synchronized (this) {
            slf4jLogger = loggerMap.get(name);
            if (slf4jLogger == null) {
                org.apache.log.Logger logkitLogger;
                if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
                    logkitLogger = Hierarchy.getDefaultHierarchy().getRootLogger();
                } else {
                    logkitLogger = LoggingManager.getLoggerFor(name);
                }
                slf4jLogger = new LogkitLoggerAdapter(logkitLogger);
                loggerMap.put(name, slf4jLogger);
            }
        }
        return slf4jLogger;
    }
}
