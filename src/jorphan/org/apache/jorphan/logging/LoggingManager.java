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
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;

import org.apache.jorphan.util.ClassContext;
import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.NullOutputLogTarget;
import org.apache.log.output.io.WriterTarget;

/**
 * @author Michael Stover (mstover1 at apache.org)
 * @version $Revision$
 */
public final class LoggingManager
{
    private static PatternFormatter format =
        new PatternFormatter(
            "%{time:yyyy/MM/dd HH:mm:ss} %5.5{priority} - "
                + "%{category}: %{message} %{throwable}\n");
           // time pattern is passed to java.text.SimpleDateFormat

    /** Used to hold the default logging target. */
    private static LogTarget target;
    
    // Hack to detect when System.out has been set as the target, to avoid closing it
    private static boolean isTargetSystemOut = false;// Is the target System.out?
	private static boolean isWriterSystemOut = false;// Is the Writer System.out?

    public final static String LOG_FILE = "log_file";
    public final static String LOG_PRIORITY = "log_level";
    private static LoggingManager logManager = null;

    private LoggingManager()
    {
        // ensure that target is valid initially
        target = new NullOutputLogTarget();
    }

    public static LoggingManager getLogManager()
    {
        return logManager;
    }

    /**
     * Initialise the logging system from the Jmeter properties.
     * Logkit loggers inherit from their parents.
     * 
     * Normally the jmeter properties file defines a single log file, so
     * set this as the default from "log_file", default "jmeter.log"
     * The default priority is set from "log_level", with a default of INFO
     * 
     */
    public static void initializeLogging(Properties properties)
    {
        if (logManager == null)
        {
            logManager = new LoggingManager();
        }

        // Set the top-level defaults
        setTarget(
            makeWriter(
                properties.getProperty(LOG_FILE, "jmeter.log"),
                LOG_FILE));
        setPriority(properties.getProperty(LOG_PRIORITY, "INFO"));

        setLoggingLevels(properties);
        // now set the individual categories (if any)
    }

    /*
     * Helper method to handle log target creation.
     * If there is an error creating the file, then it uses System.out.
     */
    private static Writer makeWriter(String logFile, String propName)
    {
        Writer wt;
        isWriterSystemOut=false;
        try
        {
            wt = new FileWriter(logFile);
        }
        catch (Exception e)
        {
            System.out.println(propName + "=" + logFile + " " + e.toString());
            System.out.println("[" + propName + "-> System.out]");
			isWriterSystemOut=true;
            wt = new PrintWriter(System.out);
        }
        return wt;
    }

    /*
     * Handle LOG_PRIORITY.category=priority and LOG_FILE.category=file_name
     * properties. If the prefix is detected, then remove it to get the
     * category.
     */
    private static void setLoggingLevels(Properties appProperties)
    {
        Iterator props = appProperties.keySet().iterator();
        while (props.hasNext())
        {
            String prop = (String) props.next();
            if (prop.startsWith(LOG_PRIORITY + "."))
                // don't match the empty category
            {
                String category = prop.substring(LOG_PRIORITY.length() + 1);
                setPriority(appProperties.getProperty(prop), category);
            }
            if (prop.startsWith(LOG_FILE + "."))
            {
                String category = prop.substring(LOG_FILE.length() + 1);
                String file = appProperties.getProperty(prop);
                setTarget(
                    new WriterTarget(makeWriter(file, prop), format),
                    category);
            }
        }
    }

    private final static String PACKAGE_PREFIX = "org.apache.";

    /*
     * Stack contains the follow when the context is obtained:
     * 0 - getCallerClassNameAt()
     * 1 - this method
     * 2 - getLoggerForClass
     * 
     */
    private static String getCallerClassName()
    {
        String name = ClassContext.getCallerClassNameAt(3);
        if (name.startsWith(PACKAGE_PREFIX))
        { // remove the package prefix
            name = name.substring(PACKAGE_PREFIX.length());
        }
        return name;
    }

    /**
     * Get the Logger for a class - no argument needed because the calling
     * class name is derived automatically from the call stack.
     * 
     * @return Logger
     */
    public static Logger getLoggerForClass()
    {
        String className = getCallerClassName();
        return Hierarchy.getDefaultHierarchy().getLoggerFor(className);
    }

    public static Logger getLoggerFor(String category)
    {
        return Hierarchy.getDefaultHierarchy().getLoggerFor(category);
    }

    public static void setPriority(String p, String category)
    {
        setPriority(Priority.getPriorityForName(p), category);
    }
    public static void setPriority(Priority p, String category)
    {
        Hierarchy.getDefaultHierarchy().getLoggerFor(category).setPriority(p);
    }
    public static void setPriority(String p)
    {
        setPriority(Priority.getPriorityForName(p));
    }
    public static void setPriority(Priority p)
    {
        Hierarchy.getDefaultHierarchy().setDefaultPriority(p);
    }
    public static void setTarget(LogTarget target, String category)
    {
        Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor(category);
        logger.setLogTargets(new LogTarget[] { target });
    }
    
    /**
     * Sets the default log target from the parameter.
     * The existing target is first closed if necessary.
     * 
     * @param targetFile (Writer)
     */
    public static void setTarget(Writer targetFile)
    {
        if (target == null)
        {
            target = new WriterTarget(targetFile, format);
            isTargetSystemOut=isWriterSystemOut;
        }
        else
        {
            if (!isTargetSystemOut && target instanceof WriterTarget)
            {
                ((WriterTarget) target).close();
            }
            target = new WriterTarget(targetFile, format);
			isTargetSystemOut=isWriterSystemOut;
        }
        Hierarchy.getDefaultHierarchy().setDefaultLogTarget(target);
    }
}
