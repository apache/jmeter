/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
            "%{time:MM/dd/yyyy h:mm:ss a} %5.5{priority} - "
                + "%{category}: %{message} %{throwable}\n");

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

    /**
     * @param unused ignored
     * @deprecated this version is temporary; use the no-argument version
     *             instead.
     * @return
     */
    public static Logger getLoggerForClass(String unused)
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
