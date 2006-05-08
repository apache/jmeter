// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.Iterator;
import java.util.Properties;

import org.apache.avalon.excalibur.logger.LogKitLoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.jorphan.util.ClassContext;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.NullOutputLogTarget;
import org.apache.log.output.io.WriterTarget;
import org.xml.sax.SAXException;

/**
 * @version $Revision$ on $Date$
 */
public final class LoggingManager {
	// N.B time pattern is passed to java.text.SimpleDateFormat
	/*
	 * Predefined format patterns, selected by the property log_format_type (see
	 * jmeter.properties) The new-line is added later
	 */
	private static final String DEFAULT_PATTERN = "%{time:yyyy/MM/dd HH:mm:ss} %5.5{priority} - "
			+ "%{category}: %{message} %{throwable}";

	private static final String PATTERN_THREAD_PREFIX = "%{time:yyyy/MM/dd HH:mm:ss} %5.5{priority} "
			+ "%12{thread} %{category}: %{message} %{throwable}";

	private static final String PATTERN_THREAD_SUFFIX = "%{time:yyyy/MM/dd HH:mm:ss} %5.5{priority} "
			+ "%{category}[%{thread}]: %{message} %{throwable}";

	private static PatternFormatter format = null;

	/** Used to hold the default logging target. */
	private static LogTarget target;

	// Hack to detect when System.out has been set as the target, to avoid
	// closing it
	private static boolean isTargetSystemOut = false;// Is the target
														// System.out?

	private static boolean isWriterSystemOut = false;// Is the Writer
														// System.out?

	public final static String LOG_FILE = "log_file";

	public final static String LOG_PRIORITY = "log_level";

	private static LoggingManager logManager = null;

	private LoggingManager() {
		// ensure that target is valid initially
		target = new NullOutputLogTarget();
	}

	public static LoggingManager getLogManager() {
		return logManager;
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
		if (logManager == null) {
			logManager = new LoggingManager();
		}

		setFormat(properties);

		// Set the top-level defaults
		setTarget(makeWriter(properties.getProperty(LOG_FILE, "jmeter.log"), LOG_FILE));
		setPriority(properties.getProperty(LOG_PRIORITY, "INFO"));

		setLoggingLevels(properties);
		// now set the individual categories (if any)

		setConfig(properties);// Further configuration
	}

	private static void setFormat(Properties properties) {
		String pattern = DEFAULT_PATTERN;
		String type = properties.getProperty("log_format_type", "");
		if (type.length() == 0) {
			pattern = properties.getProperty("log_format", DEFAULT_PATTERN);
		} else {
			if (type.equalsIgnoreCase("thread_suffix")) {
				pattern = PATTERN_THREAD_SUFFIX;
			} else if (type.equalsIgnoreCase("thread_prefix")) {
				pattern = PATTERN_THREAD_PREFIX;
			} else {
				pattern = DEFAULT_PATTERN;
			}
		}
		format = new PatternFormatter(pattern + "\n");
	}

	private static void setConfig(Properties p) {
		String cfg = p.getProperty("log_config");
		if (cfg == null)
			return;

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
			// This happens if the default log-target id-ref specifies a
			// non-existent target
			System.out.println("Error processing logging config " + cfg);
			System.out.println(e.toString());
		} catch (NullPointerException e) {
			// This can happen if a log-target id-ref specifies a non-existent
			// target
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
			format = new PatternFormatter(DEFAULT_PATTERN + "\n");
		}
		return format;
	}

	/*
	 * Helper method to handle log target creation. If there is an error
	 * creating the file, then it uses System.out.
	 */
	private static Writer makeWriter(String logFile, String propName) {
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

	/*
	 * Handle LOG_PRIORITY.category=priority and LOG_FILE.category=file_name
	 * properties. If the prefix is detected, then remove it to get the
	 * category.
	 */
	private static void setLoggingLevels(Properties appProperties) {
		Iterator props = appProperties.keySet().iterator();
		while (props.hasNext()) {
			String prop = (String) props.next();
			if (prop.startsWith(LOG_PRIORITY + "."))
			// don't match the empty category
			{
				String category = prop.substring(LOG_PRIORITY.length() + 1);
				setPriority(appProperties.getProperty(prop), category);
			}
			if (prop.startsWith(LOG_FILE + ".")) {
				String category = prop.substring(LOG_FILE.length() + 1);
				String file = appProperties.getProperty(prop);
				setTarget(new WriterTarget(makeWriter(file, prop), getFormat()), category);
			}
		}
	}

	private final static String PACKAGE_PREFIX = "org.apache.";

	/*
	 * Stack contains the follow when the context is obtained: 0 -
	 * getCallerClassNameAt() 1 - this method 2 - getLoggerForClass
	 * 
	 */
	private static String getCallerClassName() {
		String name = ClassContext.getCallerClassNameAt(3);
		return name;
	}

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
		String className = getCallerClassName();
		return Hierarchy.getDefaultHierarchy().getLoggerFor(removePrefix(className));
	}

	public static Logger getLoggerFor(String category) {
		return Hierarchy.getDefaultHierarchy().getLoggerFor(category);
	}

    public static Logger getLoggerForShortName(String category) {
        return Hierarchy.getDefaultHierarchy().getLoggerFor(removePrefix(category));
    }
    
	public static void setPriority(String p, String category) {
		setPriority(Priority.getPriorityForName(p), category);
	}

    /**
     * 
     * @param p - priority, e.g. DEBUG, INFO
     * @param fullName - e.g. org.apache.jmeter.etc
     */
    public static void setPriorityFullName(String p, String fullName) {
        setPriority(Priority.getPriorityForName(p), removePrefix(fullName));
    }

	public static void setPriority(Priority p, String category) {
		Hierarchy.getDefaultHierarchy().getLoggerFor(category).setPriority(p);
	}

	public static void setPriority(String p) {
		setPriority(Priority.getPriorityForName(p));
	}

	public static void setPriority(Priority p) {
		Hierarchy.getDefaultHierarchy().setDefaultPriority(p);
	}

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
	public static void setTarget(Writer targetFile) {
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
}
