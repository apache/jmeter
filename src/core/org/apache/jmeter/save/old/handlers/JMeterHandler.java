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
package org.apache.jmeter.save.old.handlers;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.save.old.SaveHandler;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;


/**
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author     Michael Stover
 *@created    June 8, 2001
 *@version    1.0
 */

public class JMeterHandler
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.util");
	private static String TOKENS = "&'\"<>\n\r\t\f\b\\";
	private static Map guiClassMap = new HashMap();
	private static Map propertyConversion = new HashMap();
	private static Map componentMap = new HashMap();
	
	static
	{
		guiClassMap.put("org.apache.jmeter.protocol.http.config.UrlConfig","org.apache.jmeter.protocol.http.config.gui.UrlConfigGui");
		guiClassMap.put("org.apache.jmeter.control.ModifyController","org.apache.jmeter.control.gui.LogicControllerGui");
		guiClassMap.put("org.apache.jmeter.control.LogicController",
				"org.apache.jmeter.control.gui.LogicControllerGui");
		guiClassMap.put("org.apache.jmeter.timers.GaussianRandomTimer",
				"org.apache.jmeter.timers.gui.GaussianRandomTimerGui");
		guiClassMap.put("org.apache.jmeter.timers.ConstantTimer",
				"org.apache.jmeter.timers.gui.ConstantTimerGui");
		guiClassMap.put("org.apache.jmeter.timers.UniformRandomTimer",
				"org.apache.jmeter.timers.gui.UniformRandomTimerGui");
		guiClassMap.put("org.apache.jmeter.visualizers.GraphModel",
				"org.apache.jmeter.visualizers.GraphVisualizer");
		guiClassMap.put("org.apache.jmeter.reporters.AssertionReporter",
				"org.apache.jmeter.visualizers.AssertionVisualizer");
		guiClassMap.put("org.apache.jmeter.reporters.ResultCollector",
				"org.apache.jmeter.visualizers.ViewResultsFullVisualizer");
		guiClassMap.put("org.apache.jmeter.control.LoopController",
				"org.apache.jmeter.control.gui.LoopControlPanel");		
		guiClassMap.put("org.apache.jmeter.protocol.http.modifier.AnchorModifier",
				"org.apache.jmeter.protocol.http.modifier.gui.AnchorModifierGui");
		guiClassMap.put("org.apache.jmeter.control.InterleaveControl",
				"org.apache.jmeter.control.gui.InterleaveControlGui");
		guiClassMap.put("org.apache.jmeter.control.OnceOnlyController",
				"org.apache.jmeter.control.gui.OnceOnlyControllerGui");	
		guiClassMap.put("org.apache.jmeter.protocol.jdbc.config.DbConfig",
				"org.apache.jmeter.protocol.jdbc.config.gui.DbConfigGui");	
		guiClassMap.put("org.apache.jmeter.protocol.jdbc.config.PoolConfig",
				"org.apache.jmeter.protocol.jdbc.config.gui.PoolConfigGui");	
		guiClassMap.put("org.apache.jmeter.protocol.jdbc.config.SqlConfig",
				"org.apache.jmeter.protocol.jdbc.config.gui.SqlConfigGui");				
		guiClassMap.put("org.apache.jmeter.protocol.ftp.config.FtpConfig",
				"org.apache.jmeter.protocol.ftp.config.gui.FtpConfigGui");				
		guiClassMap.put("org.apache.jmeter.assertions.Assertion",
				"org.apache.jmeter.assertions.gui.AssertionGui");
					
		
		propertyConversion.put("arguments",HTTPSampler.ARGUMENTS);
		propertyConversion.put("port",HTTPSampler.PORT);
		propertyConversion.put("PROTOCOL",HTTPSampler.PROTOCOL);
		propertyConversion.put("method",HTTPSampler.METHOD);
		propertyConversion.put("domain",HTTPSampler.DOMAIN);
		propertyConversion.put("path",HTTPSampler.PATH);
		propertyConversion.put("sampler.RESPONSE",ResponseAssertion.RESPONSE_DATA);
		propertyConversion.put("sampler.LABEL",ResponseAssertion.SAMPLE_LABEL);
		
		propertyConversion.put("name",TestElement.NAME);
		
		componentMap.put("org.apache.jmeter.visualizers.GraphModel","org.apache.jmeter.reporters.ResultCollector");
		componentMap.put("org.apache.jmeter.reporters.AssertionReporter","org.apache.jmeter.reporters.ResultCollector");
		componentMap.put("org.apache.jmeter.reporters.ResultCollector","org.apache.jmeter.reporters.ResultCollector");
		componentMap.put("org.apache.jmeter.protocol.http.config.UrlConfig","org.apache.jmeter.config.ConfigTestElement");
		componentMap.put("org.apache.jmeter.protocol.http.config.MultipartUrlConfig","org.apache.jmeter.config.ConfigTestElement");
		componentMap.put("org.apache.jmeter.assertions.Assertion",
				"org.apache.jmeter.assertions.ResponseAssertion");
		
	}
		

	/**
	 *  Constructor for the JMeterHandler object
	 */
	public JMeterHandler()
	{
	}
	
	public static String getComponentConversion(String old)
	{
		if(componentMap.containsKey(old))
		{
			return (String)componentMap.get(old);
		}
		else 
		{
			return old;
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  configs          Description of Parameter
	 *@param  out              Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public static void writeConfigElements(Collection configs, Writer out) throws IOException
	{
		out.write("<configElements>\n");
		writeObjects(configs, out);
		out.write("</configElements>\n");
	}

	/**
	 *  Description of the Method
	 *
	 *@param  controls         Description of Parameter
	 *@param  out              Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public static void writeControllers(Collection controls, Writer out) throws IOException
	{
		out.write("<controllers>\n");
		writeObjects(controls, out);
		out.write("</controllers>\n");
	}

	/**
	 *  Description of the Method
	 *
	 *@param  groups           Description of Parameter
	 *@param  out              Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public static void writeThreadGroups(Collection groups, Writer out) throws IOException
	{
		out.write("<threadgroups>\n");
		writeObjects(groups, out);
		out.write("</threadgroups>\n");
	}

	/**
	 *  Description of the Method
	 *
	 *@param  listeners        Description of Parameter
	 *@param  out              Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public static void writeListeners(Collection listeners, Writer out) throws IOException
	{
		out.write("<listeners>\n");
		writeObjects(listeners, out);
		out.write("</listeners>\n");
	}

	/**
	 *  Description of the Method
	 *
	 *@param  timers           Description of Parameter
	 *@param  out              Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public static void writeTimers(Collection timers, Writer out) throws IOException
	{
		out.write("<timers>\n");
		writeObjects(timers, out);
		out.write("</timers>\n");
	}

	/**
	 *  Description of the Method
	 *
	 *@param  objects          Description of Parameter
	 *@param  out              Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public static void writeObjects(Collection objects, Writer out) throws IOException
	{
		Iterator iter = objects.iterator();
		while (iter.hasNext())
		{
			writeObject(iter.next(), out);
			out.write("\n");
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  obj              Description of Parameter
	 *@param  out              Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public static void writeObject(Object obj, Writer out) throws IOException
	{
		if (obj instanceof Saveable)
		{
			try
			{
				((SaveHandler) ((Saveable) obj).getTagHandlerClass().newInstance()).save(
						(Saveable) obj, out);
			}
			catch (Exception ex)
			{
				log.error("",ex);
			}
		}
		else
		{
			out.write(convertToXML(obj.toString()));
		}
	}
	
	public static String getGuiClass(String testClass)
	{
		return (String)guiClassMap.get(testClass);
	}
	
	public static String convertProperty(String prop)
	{
		if(propertyConversion.containsKey(prop))
		{
			return (String)propertyConversion.get(prop);
		}
		else return prop;
	}

	/**
	 *  Description of the Method
	 *
	 *@param  input  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	public static String convertToXML(String input)
	{
		if (input == null)
		{
			return null;
		}
		String retVal = "";
		StringBuffer buffer = new StringBuffer(input);
		int length = buffer.length();
		for (int i = 0; i < length; i++)
		{
			char ch = buffer.charAt(i);
			int chInt = (int) ch;
			if (Character.isLetterOrDigit(ch) || ch == '\n' || ch == '\r' || ch == '\t' || ch == ' ')
			{
				continue;
			}
			int chType = Character.getType(ch);
			if (chType == Character.CONTROL || chType == Character.UNASSIGNED)
			{
				buffer.setCharAt(i, ' ');
			}
		}
		input = buffer.toString();
		buffer = new StringBuffer();
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(input, TOKENS, true);
		while (tokenizer.hasMoreTokens())
		{
			String nextToken = tokenizer.nextToken();
			length = nextToken.length();
			if (length > 1)
			{
				buffer.append(nextToken);
			}
			else if (length == 1)
			{
				char ch = nextToken.charAt(0);
				switch (ch)
				{
					case '&':
						buffer.append("&amp;");
						break;
					case '\'':
						buffer.append("&apos;");
						break;
					case '"':
						buffer.append("&quot;");
						break;
					case '<':
						buffer.append("&lt;");
						break;
					case '>':
						buffer.append("&gt;");
						break;
					case '\n':
					case '\r':
					case '\t':
					case '\f':
					case '\b':
					case '\\':
						buffer.append("&#");buffer.append((int)ch);buffer.append(";");
						break;
					default:
						buffer.append(nextToken);
				}
			}
		}
		return buffer.toString();
	}
}
