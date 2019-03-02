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

package org.apache.jorphan.logging;

import java.util.Properties;

import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.slf4j.LoggerFactory;

/**
 * Manages JMeter logging
 * @deprecated since 3.2, use SLF4J for logger creation
 */
@Deprecated
public final class LoggingManager {

    /**
     * Predefined format patterns, selected by the property log_format_type (see
     * jmeter.properties) The new-line is added later
     * @deprecated  since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static final String DEFAULT_PATTERN = "%{time:yyyy/MM/dd HH:mm:ss} %5.5{priority} - "  //$NON_NLS-1$
            + "%{category}: %{message} %{throwable}"; //$NON_NLS-1$

    /**
     * @deprecated  since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static final String LOG_FILE = "log_file";  //$NON_NLS-1$

    /**
     * @deprecated  since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static final String LOG_PRIORITY = "log_level";  //$NON_NLS-1$

    private LoggingManager() {
        // non-instantiable - static methods only
    }

    /**
     * Initialise the logging system from the Jmeter properties. Logkit loggers
     * inherit from their parents.
     *
     * Normally the jmeter properties file defines a single log file, so set
     * this as the default from "log_file", default "jmeter.log" The default
     * priority is set from "log_level", with a default of INFO
     *
     * @param properties
     *            {@link Properties} to be used for initialization
     * @deprecated  since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static void initializeLogging(Properties properties) {
        // NOP
    }

    /**
     * Handle LOG_PRIORITY.category=priority and LOG_FILE.category=file_name
     * properties. If the prefix is detected, then remove it to get the
     * category.
     *
     * @param appProperties
     *            {@link Properties} that contain the
     *            {@link LoggingManager#LOG_PRIORITY LOG_PRIORITY} and
     *            {@link LoggingManager#LOG_FILE LOG_FILE} prefixed entries
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static void setLoggingLevels(Properties appProperties) {
        // NOP
    }

    /**
     * @deprecated
     */
    @Deprecated
    private static final String PACKAGE_PREFIX = "org.apache."; //$NON_NLS-1$

    /**
     * Removes the standard prefix, i.e. "org.apache.".
     * 
     * @param name from which to remove the prefix
     * @return the name with the prefix removed
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static String removePrefix(String name){
        if (name.startsWith(PACKAGE_PREFIX)) { // remove the package prefix
            name = name.substring(PACKAGE_PREFIX.length());
        }
        return name;
    }

    /**
     * Get the Logger for a class - no argument needed because the calling class
     * name is derived automatically from the call stack.
     *
     * @return Logger
     */
    public static Logger getLoggerForClass() {
        String className = new Exception().getStackTrace()[1].getClassName();
        return new Slf4jLogkitLogger(LoggerFactory.getLogger(className));
    }

    /**
     * Get the Logger for a class.
     * 
     * @param category - the full name of the logger category
     *
     * @return Logger
     */
    public static Logger getLoggerFor(String category) {
        return new Slf4jLogkitLogger(LoggerFactory.getLogger(category));
    }

    /**
     * Get the Logger for a class.
     * 
     * @param category - the full name of the logger category, this will have the prefix removed.
     *
     * @return Logger
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static Logger getLoggerForShortName(String category) {
        return getLoggerFor(category);
    }

    /**
     * Set the logging priority for a category.
     * 
     * @param priority - string containing the priority name, e.g. "INFO", "WARN", "DEBUG", "FATAL_ERROR"
     * @param category - string containing the category
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static void setPriority(String priority, String category) {
        // NOP
    }

    /**
     * Set the logging priority for a category.
     * 
     * @param priority - priority, e.g. DEBUG, INFO
     * @param fullName - e.g. org.apache.jmeter.etc, will have the prefix removed.
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static void setPriorityFullName(String priority, String fullName) {
        // NOP
    }

    /**
     * Set the logging priority for a category.
     * 
     * @param priority - e.g. Priority.DEBUG
     * @param category - string containing the category
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static void setPriority(Priority priority, String category) {
        // NOP
    }

    /**
     * Set the logging priority.
     * 
     * @param priority - e.g. Priority.DEBUG
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static void setPriority(String priority) {
        // NOP
    }

    /**
     * Set the default logging priority.
     * 
     * @param priority e.g. Priority.DEBUG
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static void setPriority(Priority priority) {
        // NOP
    }

    /**
     * Set the logging target for a category.
     * 
     * @param target the LogTarget
     * @param category the category name
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static void setTarget(LogTarget target, String category) {
        // NOP
    }

    /**
     * Add logTargets to root logger
     * FIXME What's the clean way to add a LogTarget afterwards ?
     * @param logTargets LogTarget array
     * @deprecated since 3.2, use SLF4J for logging
     */
    @Deprecated
    public static void addLogTargetToRootLogger(LogTarget[] logTargets) {
        // NOP
    }
}
