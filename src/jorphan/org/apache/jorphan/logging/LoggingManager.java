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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.apache.avalon.excalibur.logger.LogKitLoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.NullOutputLogTarget;
import org.apache.log.output.io.WriterTarget;
import org.xml.sax.SAXException;

/**
 * Manages JMeter logging
 */
public final class LoggingManager {
    // N.B time pattern is passed to java.text.SimpleDateFormat
    /*
     * Predefined format patterns, selected by the property log_format_type (see
     * jmeter.properties) The new-line is added later
     */
    public static final String DEFAULT_PATTERN = "%{time:yyyy/MM/dd HH:mm:ss} %5.5{priority} - "  //$NON_NLS-1$
            + "%{category}: %{message} %{throwable}"; //$NON_NLS-1$

    private static final String PATTERN_THREAD_PREFIX = "%{time:yyyy/MM/dd HH:mm:ss} %5.5{priority} "  //$NON_NLS-1$
            + "%20{thread} %{category}: %{message} %{throwable}";  //$NON_NLS-1$

    private static final String PATTERN_THREAD_SUFFIX = "%{time:yyyy/MM/dd HH:mm:ss} %5.5{priority} "  //$NON_NLS-1$
            + "%{category}[%{thread}]: %{message} %{throwable}";  //$NON_NLS-1$

    // Needs to be volatile as may be referenced from multiple threads
    // TODO see if this can be made final somehow
    private static volatile PatternFormatter format = null;

    /** Used to hold the default logging target. */
    //@GuardedBy("this")
    private static LogTarget target = new NullOutputLogTarget();

    // Hack to detect when System.out has been set as the target, to avoid closing it
    private static volatile boolean isTargetSystemOut = false;// Is the target System.out?

    private static volatile boolean isWriterSystemOut = false;// Is the Writer System.out?

    public static final String LOG_FILE = "log_file";  //$NON_NLS-1$

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
     */
    public static void initializeLogging(Properties properties) {
        setFormat(properties);

        // Set the top-level defaults
        setTarget(makeWriter(properties.getProperty(LOG_FILE, "jmeter.log"), LOG_FILE));  //$NON_NLS-1$
        setPriority(properties.getProperty(LOG_PRIORITY, "INFO"));

        setLoggingLevels(properties);
        // now set the individual categories (if any)

        setConfig(properties);// Further configuration
    }

    private static void setFormat(Properties properties) {
        String pattern = DEFAULT_PATTERN;
        String type = properties.getProperty("log_format_type", "");  //$NON_NLS-1$
        if (type.length() == 0) {
            pattern = properties.getProperty("log_format", DEFAULT_PATTERN);  //$NON_NLS-1$
        } else {
            if (type.equalsIgnoreCase("thread_suffix")) {  //$NON_NLS-1$
                pattern = PATTERN_THREAD_SUFFIX;
            } else if (type.equalsIgnoreCase("thread_prefix")) {  //$NON_NLS-1$
                pattern = PATTERN_THREAD_PREFIX;
            } else {
                pattern = DEFAULT_PATTERN;
            }
        }
        format = new PatternFormatter(pattern + "\n"); //$NON_NLS-1$
    }

    private static void setConfig(Properties p) {
        String cfg = p.getProperty("log_config"); //$NON_NLS-1$
        if (cfg == null) {
            return;
        }

        // Make sure same hierarchy is used
        Hierarchy hier = Hierarchy.getDefaultHierarchy();
        LogKitLoggerManager manager = new LogKitLoggerManager(null, hier, null, null);

        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        try {
            Configuration c = builder.buildFromFile(cfg);
            Context ctx = new DefaultContext();
            manager.contextualize(ctx);
            manager.configure(c);
        } catch (IllegalArgumentException e) {
            // This happens if the default log-target id-ref specifies a non-existent target
            System.out.println("Error processing logging config " + cfg);
            System.out.println(e.toString());
        } catch (NullPointerException e) {
            // This can happen if a log-target id-ref specifies a non-existent target
            System.out.println("Error processing logging config " + cfg);
            System.out.println("Perhaps a log target is missing?");
        } catch (ConfigurationException e) {
            System.out.println("Error processing logging config " + cfg);
            System.out.println(e.toString());
        } catch (SAXException e) {
            System.out.println("Error processing logging config " + cfg);
            System.out.println(e.toString());
        } catch (IOException e) {
            System.out.println("Error processing logging config " + cfg);
            System.out.println(e.toString());
        } catch (ContextException e) {
            System.out.println("Error processing logging config " + cfg);
            System.out.println(e.toString());
        }
    }

    /*
     * Helper method to ensure that format is initialised if initializeLogging()
     * has not yet been called.
     */
    private static PatternFormatter getFormat() {
        if (format == null) {
            format = new PatternFormatter(DEFAULT_PATTERN + "\n"); //$NON_NLS-1$
        }
        return format;
    }

    /*
     * Helper method to handle log target creation. If there is an error
     * creating the file, then it uses System.out.
     */
    private static Writer makeWriter(String logFile, String propName) {
        // If the name contains at least one set of paired single-quotes, reformat using DateFormat
        final int length = logFile.split("'",-1).length;
        if (length > 1 && length %2 == 1){
            try {
                SimpleDateFormat df = new SimpleDateFormat(logFile);
                logFile = df.format(new Date());
            } catch (Exception ignored) {
            }
        }
        Writer wt;
        isWriterSystemOut = false;
        try {
            wt = new FileWriter(logFile);
        } catch (Exception e) {
            System.out.println(propName + "=" + logFile + " " + e.toString());
            System.out.println("[" + propName + "-> System.out]");
            isWriterSystemOut = true;
            wt = new PrintWriter(System.out);
        }
        return wt;
    }

    /**
     * Handle LOG_PRIORITY.category=priority and LOG_FILE.category=file_name
     * properties. If the prefix is detected, then remove it to get the
     * category.
     */
    public static void setLoggingLevels(Properties appProperties) {
        Iterator<?> props = appProperties.keySet().iterator();
        while (props.hasNext()) {
            String prop = (String) props.next();
            if (prop.startsWith(LOG_PRIORITY + ".")) //$NON_NLS-1$
            // don't match the empty category
            {
                String category = prop.substring(LOG_PRIORITY.length() + 1);
                setPriority(appProperties.getProperty(prop), category);
            }
            if (prop.startsWith(LOG_FILE + ".")) { //$NON_NLS-1$
                String category = prop.substring(LOG_FILE.length() + 1);
                String file = appProperties.getProperty(prop);
                setTarget(new WriterTarget(makeWriter(file, prop), getFormat()), category);
            }
        }
    }

    private static final String PACKAGE_PREFIX = "org.apache."; //$NON_NLS-1$

    /**
     * Removes the standard prefix, i.e. "org.apache.".
     * 
     * @param name from which to remove the prefix
     * @return the name with the prefix removed
     */
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
        return Hierarchy.getDefaultHierarchy().getLoggerFor(removePrefix(className));
    }

    /**
     * Get the Logger for a class.
     * 
     * @param category - the full name of the logger category
     *
     * @return Logger
     */
    public static Logger getLoggerFor(String category) {
        return Hierarchy.getDefaultHierarchy().getLoggerFor(category);
    }

    /**
     * Get the Logger for a class.
     * 
     * @param category - the full name of the logger category, this will have the prefix removed.
     *
     * @return Logger
     */
    public static Logger getLoggerForShortName(String category) {
        return Hierarchy.getDefaultHierarchy().getLoggerFor(removePrefix(category));
    }

    /**
     * Set the logging priority for a category.
     * 
     * @param priority - string containing the priority name, e.g. "INFO", "WARN", "DEBUG", "FATAL_ERROR"
     * @param category - string containing the category
     */
    public static void setPriority(String priority, String category) {
        setPriority(Priority.getPriorityForName(priority), category);
    }

    /**
     * Set the logging priority for a category.
     * 
     * @param priority - priority, e.g. DEBUG, INFO
     * @param fullName - e.g. org.apache.jmeter.etc, will have the prefix removed.
     */
    public static void setPriorityFullName(String priority, String fullName) {
        setPriority(Priority.getPriorityForName(priority), removePrefix(fullName));
    }

    /**
     * Set the logging priority for a category.
     * 
     * @param priority - e.g. Priority.DEBUG
     * @param category - string containing the category
     */
    public static void setPriority(Priority priority, String category) {
        Hierarchy.getDefaultHierarchy().getLoggerFor(category).setPriority(priority);
    }

    public static void setPriority(String p) {
        setPriority(Priority.getPriorityForName(p));
    }

    /**
     * Set the default logging priority.
     * 
     * @param priority e.g. Priority.DEBUG
     */
    public static void setPriority(Priority priority) {
        Hierarchy.getDefaultHierarchy().setDefaultPriority(priority);
    }

    /**
     * Set the logging target for a category.
     * 
     * @param target the LogTarget
     * @param category the category name
     */
    public static void setTarget(LogTarget target, String category) {
        Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor(category);
        logger.setLogTargets(new LogTarget[] { target });
    }

    /**
     * Sets the default log target from the parameter. The existing target is
     * first closed if necessary.
     *
     * @param targetFile
     *            (Writer)
     */
    private static synchronized void setTarget(Writer targetFile) {
        if (target == null) {
            target = getTarget(targetFile, getFormat());
            isTargetSystemOut = isWriterSystemOut;
        } else {
            if (!isTargetSystemOut && target instanceof WriterTarget) {
                ((WriterTarget) target).close();
            }
            target = getTarget(targetFile, getFormat());
            isTargetSystemOut = isWriterSystemOut;
        }
        Hierarchy.getDefaultHierarchy().setDefaultLogTarget(target);
    }

    private static LogTarget getTarget(Writer targetFile, PatternFormatter fmt) {
        return new WriterTarget(targetFile, fmt);
    }

    /**
     * Add logTargets to root logger
     * FIXME What's the clean way to add a LogTarget afterwards ?
     * @param logTargets LogTarget array
     */
    public static void addLogTargetToRootLogger(LogTarget[] logTargets) {
        LogTarget[] newLogTargets = new LogTarget[logTargets.length+1];
        System.arraycopy(logTargets, 0, newLogTargets, 1, logTargets.length);
        newLogTargets[0] = target;
        Hierarchy.getDefaultHierarchy().getRootLogger().setLogTargets(newLogTargets);
    }
}