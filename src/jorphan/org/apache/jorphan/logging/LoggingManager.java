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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log.Hierarchy;
import org.apache.log.LogTarget;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.apache.log.format.PatternFormatter;
import org.apache.log.output.NullOutputLogTarget;
import org.apache.log.output.io.WriterTarget;
/**
 * @author Michael Stover (mstover1 at apache.org)
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class LoggingManager
{
	private static PatternFormatter format =
		new PatternFormatter("%{time:MM/dd/yyyy h:mm:ss a} %{priority} - %{category}: %{message} %{throwable}\n");
	private static LogTarget target;
	public final static String LOG_FILE = "log_file";
	public final static String LOG_PRIORITY = "log_level";
	private static LoggingManager logManager = null;
	
	static
	{
		Map initProps = new HashMap();
		initProps.put("log_level.jorphan","ERROR");
		initializeLogging(initProps);
	}
	
	private LoggingManager()
	{
		target = new NullOutputLogTarget();
	}
	public static LoggingManager getLogManager()
	{
		return logManager;
	}
	public static void initializeLogging(Map properties)
	{
		if (logManager == null)
		{
			logManager = new LoggingManager();
		}
		setLoggingLevels(properties,(String)properties.get(LOG_FILE));
	}
	private static void setLoggingLevels(Map appProperties,String logFile)
	{
		WriterTarget tempTarget = null;
		try
		{
			tempTarget = new WriterTarget(new FileWriter(logFile),format);
		}
		catch(Exception e){
			target = new WriterTarget(new PrintWriter(System.out),format);
		}
		Iterator names = appProperties.keySet().iterator();
		while (names.hasNext())
		{
			String prop = (String) names.next();
			if (prop.startsWith(LOG_PRIORITY))
			{
				String name = prop.substring(LOG_PRIORITY.length() + 1);
				logManager.setPriority(
					Priority.getPriorityForName((String) appProperties.get(prop)),
					name);
				if(tempTarget != null)
				{
					logManager.setTarget(tempTarget,name);
				}
				else
				{
					logManager.setTarget(target,name);
				}
			}
		}
	}
	public static Logger getLoggerFor(String category)
	{
		return Hierarchy.getDefaultHierarchy().getLoggerFor(category);
	}
    
	public void setPriority(Priority p, String category)
	{
		Hierarchy.getDefaultHierarchy().getLoggerFor(category).setPriority(p);
	}
	
	public void setTarget(LogTarget target,String category)
	{
		Logger logger = getLoggerFor(category);
		logger.setLogTargets(new LogTarget[]{target});
	}
	public void setTarget(Writer targetFile)
	{
		if (target == null)
		{
			target = new WriterTarget(targetFile, format);
			Hierarchy.getDefaultHierarchy().setDefaultLogTarget(
				new WriterTarget(targetFile, format));
		}
		else
		{
			if(target instanceof WriterTarget)
			{
				((WriterTarget)target).close();
			}
			target = new WriterTarget(targetFile, format);
			Hierarchy.getDefaultHierarchy().setDefaultLogTarget(
				new WriterTarget(targetFile, format));
		}
	}
}
