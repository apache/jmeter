/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.util;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParserFactory;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.test.UnitTestManager;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.xml.sax.XMLReader;
/**
 *  This class contains the static utility methods used by JMeter.
 *
 *@author     <a href="mailto://stefano@apache.org">Stefano Mazzocchi</a>
 *@created    June 28, 2001
 *@version    $Revision$ $Date$
 */
public class JMeterUtils implements UnitTestManager
{
        private static final String VERSION="1.9.RC20030611";
        private static PatternCacheLRU patternCache = new PatternCacheLRU(1000,new Perl5Compiler());

	transient private static Logger log =
		Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.util");
	private static LoggingManager logManager;
	private static String LOG_FILE = "log_file";
	private static String LOG_PRIORITY = "log_level";
	private static final SAXParserFactory xmlFactory;
	static {
		SAXParserFactory temp = null;
		try
		{
			temp = SAXParserFactory.newInstance();
		}
		catch (Exception e)
		{
			log.error("", e);
			temp = null;
		}
		xmlFactory = temp;
	}
	private static Properties appProperties;
	private static Collection localeChangeListeners = new HashSet();
	private static Locale locale;
	private static ResourceBundle resources;
    
    private static ThreadLocal localMatcher = new ThreadLocal()
        {
            protected Object initialValue()
            {
                return new Perl5Matcher();
            }
        };
   
   //Provide Random numbers to whomever wants one
   private static Random rand = new Random();
   
   /**
    * Gets Perl5Matcher for this thread.
    */
   public static Perl5Matcher getMatcher()
   {
       return (Perl5Matcher)localMatcher.get();
   }

	/**
	 *  This method is used by the init method to load the property file that may
	 *  even reside in the user space, or in the classpath under
	 *  org.apache.jmeter.jmeter.properties
	 *
	 *@param  file  Description of Parameter
	 *@return       The Properties value
	 */
	public static Properties getProperties(String file)
	{
		Properties p = new Properties(System.getProperties());
		try
		{
			File f = new File(file);
			p.load(new FileInputStream(f));
		}
		catch (Exception e)
		{
			try
			{
                p.load(
                    ClassLoader.getSystemResourceAsStream(
                        "org/apache/jmeter/jmeter.properties"));
			}
			catch (IOException ex)
			{
				//JMeter.fail("Could not read internal resource. Archive is broken.");
			}
		}
		appProperties = p;
		LoggingManager.initializeLogging(appProperties);
		log = LoggingManager.getLoggerFor(UTIL);
		String loc= appProperties.getProperty("language");
		if (loc!=null) setLocale(new Locale(loc, ""));
		else setLocale(Locale.getDefault());
		return p;
	}
    
    public static PatternCacheLRU getPatternCache()
    {
        return patternCache;
    }
    
	public void initializeProperties(String file)
	{
		System.out.println("Initializing Properties: " + file);
		getProperties(file);
		String home = file.substring(0,file.lastIndexOf("/"));
		home = new File(home + "/..").getAbsolutePath();
		System.out.println("Setting JMeter home: " + home);
		setJMeterHome(home);
	}
	public static String[] getSearchPaths()
	{
		return new String[] { getJMeterHome() + "/lib/ext" };
	}
   
   /**
    * Provide random numbers
    * @param loc
    */
   public static int getRandomInt(int r)
   {
      return rand.nextInt(r);
   }

	/**
	 * Changes the current locale: re-reads resource strings and notifies
	 * listeners.
	 *
	 * @author Oliver Rossmueller
	 * @param locale new locale
	 */
	public static void setLocale(Locale loc)
	{
	    locale= loc;
	    resources = ResourceBundle.getBundle(
		    "org.apache.jmeter.resources.messages", locale);
	    notifyLocaleChangeListeners();
	}

	/**
	 * Gets the current locale.
	 *
	 * @author Oliver Rossmueller
	 * @return current locale
	 */
	public static Locale getLocale()
	{
	    return locale;
	}

	/**
	 * @author Oliver Rossmueller
	 */
	public static void addLocaleChangeListener(
		LocaleChangeListener listener)
	{
	    localeChangeListeners.add(listener);
	}

	/**
	 * @author Oliver Rossmueller
	 */
	public static void removeLocaleChangeListener(
		LocaleChangeListener listener)
	{
	    localeChangeListeners.remove(listener);
	}

	/**
	 * Notify all listeners interested in locale changes.
	 *
	 * @author Oliver Rossmueller
	 */
	private static void notifyLocaleChangeListeners()
	{
	    LocaleChangeEvent event =
		new LocaleChangeEvent(JMeterUtils.class, locale);
	    Iterator iterator = localeChangeListeners.iterator();

	    while (iterator.hasNext()) {
		LocaleChangeListener listener =
			(LocaleChangeListener)iterator.next();
		listener.localeChanged(event);
	    }
	}

	/**
	 *  Gets the resource string for this key.
	 *  @param key the key in the resource file
	 *  @return the resource string if the key is found; otherwise, return an empty string
	 */
	public static String getResString(String key)
	{
		if (key == null)
		{
			return null;
		}
		key = key.replace(' ', '_');
		key = key.toLowerCase();
		String resString = null;
		try
		{
			resString = resources.getString(key);
		}
		catch (MissingResourceException mre)
		{
			log.warn("ERROR! Resource string not found: [" + key + "]");
			resString = "";
		}
		return resString;
	}
	/**
	 *  This gets the currently defined appProperties. It can only be called after
	 *  the getProperties(String file) method is called.
	 *
	 *@return    The JMeterProperties value
	 */
	public static Properties getJMeterProperties()
	{
		return appProperties;
	}
	/**
	 *  This looks for the requested image in the classpath under
	 *  org.apache.jmeter.images. <name>
	 *
	 *@param  name  Description of Parameter
	 *@return       The Image value
	 */
	public static ImageIcon getImage(String name)
	{
		try
		{
			return new ImageIcon(
				JMeterUtils.class.getClassLoader().getResource(
					"org/apache/jmeter/images/" + name.trim()));
		}
		catch (NullPointerException e)
		{
			log.warn("no icon for " + name);
			return null;
		}
	}
	public static String getResourceFileAsText(String name)
	{
		try
		{
			String lineEnd = System.getProperty("line.separator");
			BufferedReader fileReader =
				new BufferedReader(
					new InputStreamReader(
						JMeterUtils.class.getClassLoader().getResourceAsStream(name)));
			StringBuffer text = new StringBuffer();
			String line = "NOTNULL";
			while (line != null)
			{
				line = fileReader.readLine();
				if (line != null)
				{
					text.append(line);
					text.append(lineEnd);
				}
			}
			return text.toString();
		}
		catch (IOException e)
		{
			return "";
		}
	}
	/**
	 *  Creates the vector of Timers plugins.
	 *
	 *@param  properties  Description of Parameter
	 *@return             The Timers value
	 */
	public static Vector getTimers(Properties properties)
	{
		return instantiate(
			getVector(properties, "timer."),
			"org.apache.jmeter.timers.Timer");
	}
	/**
	 *  Creates the vector of visualizer plugins.
	 *
	 *@param  properties  Description of Parameter
	 *@return             The Visualizers value
	 */
	public static Vector getVisualizers(Properties properties)
	{
		return instantiate(
			getVector(properties, "visualizer."),
			"org.apache.jmeter.visualizers.Visualizer");
	}
	/**
	 *  Creates a vector of SampleController plugins.
	 *
	 *@param  properties  The properties with information about the samplers
	 *@return             The Controllers value
	 */
	public static Vector getControllers(Properties properties)
	{
		String name = "controller.";
		Vector v = new Vector();
		Enumeration names = properties.keys();
		while (names.hasMoreElements())
		{
			String prop = (String) names.nextElement();
			if (prop.startsWith(name))
			{
				Object o =
					instantiate(
						properties.getProperty(prop),
						"org.apache.jmeter.control.SamplerController");
				v.addElement(o);
			}
		}
		return v;
	}
	/**
	 *  Create a string of class names for a particular SamplerController
	 *
	 *@param  properties  The properties with info about the samples.
	 *@param  name        The name of the sampler controller.
	 *@return             The TestSamples value
	 */
	public static String[] getTestSamples(Properties properties, String name)
	{
		return (String[]) getVector(properties, name + ".testsample").toArray(
			new String[0]);
	}
	/**
	 *  Create an instance of an org.xml.sax.Parser
	 *
	 * @deprecated use the plain version instead.  We are using JAXP!
	 *@param  properties  The properties file containing the parser's class name
	 *@return             The XMLParser value
	 */
	public static XMLReader getXMLParser(Properties properties)
	{
		return getXMLParser();
	}
	/**
	 *  Create an instance of an org.xml.sax.Parser based on the default props.
	 *
	 *@return    The XMLParser value
	 */
	public static XMLReader getXMLParser()
	{
		XMLReader reader = null;
		try
		{
			reader =
				(XMLReader) instantiate(getPropDefault("xml.parser",
					"org.apache.xerces.parsers.SAXParser"),
					"org.xml.sax.XMLReader");
			//reader = xmlFactory.newSAXParser().getXMLReader();
		}
		catch (Exception e)
		{
			reader =
				(XMLReader) instantiate(getPropDefault("xml.parser",
					"org.apache.xerces.parsers.SAXParser"),
					"org.xml.sax.XMLReader");
		}
		return reader;
	}
	/**
	 *  Creates the vector of alias strings.
	 *
	 *@param  properties  Description of Parameter
	 *@return             The Alias value
	 */
	public static Hashtable getAlias(Properties properties)
	{
		return getHashtable(properties, "alias.");
	}
	/**
	 *  Creates a vector of strings for all the properties that start with a common
	 *  prefix.
	 *
	 *@param  properties  Description of Parameter
	 *@param  name        Description of Parameter
	 *@return             The Vector value
	 */
	public static Vector getVector(Properties properties, String name)
	{
		Vector v = new Vector();
		Enumeration names = properties.keys();
		while (names.hasMoreElements())
		{
			String prop = (String) names.nextElement();
			if (prop.startsWith(name))
			{
				v.addElement(properties.getProperty(prop));
			}
		}
		return v;
	}
	/**
	 *  Creates a vector of strings for all the properties that start with a common
	 *  prefix.
	 *
	 *@param  properties  Description of Parameter
	 *@param  name        Description of Parameter
	 *@return             The Hashtable value
	 */
	public static Hashtable getHashtable(Properties properties, String name)
	{
		Hashtable t = new Hashtable();
		Enumeration names = properties.keys();
		while (names.hasMoreElements())
		{
			String prop = (String) names.nextElement();
			if (prop.startsWith(name))
			{
				t.put(prop.substring(name.length()), properties.getProperty(prop));
			}
		}
		return t;
	}
	/**
	 *  Get a int value with default if not present.
	 *
	 *@param  propName    the name of the property.
	 *@param  defaultVal  the default value.
	 *@return             The PropDefault value
	 */
	public static int getPropDefault(String propName, int defaultVal)
	{
		int ans;
		try
		{
			ans =
				(Integer
					.valueOf(
						appProperties
							.getProperty(propName, Integer.toString(defaultVal))
							.trim()))
					.intValue();
		}
		catch (Exception e)
		{
			ans = defaultVal;
		}
		return ans;
	}
	/**
	 *  Get a boolean value with default if not present.
	 *
	 *@param  propName    the name of the property.
	 *@param  defaultVal  the default value.
	 *@return             The PropDefault value
	 */
	public static boolean getPropDefault(String propName, boolean defaultVal)
	{
		boolean ans;
		try
		{
			String strVal =
				appProperties
					.getProperty(propName, (new Boolean(defaultVal)).toString())
					.trim();
			if (strVal.equalsIgnoreCase("true") || strVal.equalsIgnoreCase("t"))
			{
				ans = true;
			}
			else if (
				strVal.equalsIgnoreCase("false") || strVal.equalsIgnoreCase("f"))
			{
				ans = false;
			}
			else
			{
				ans = ((Integer.valueOf(strVal)).intValue() == 1);
			}
		}
		catch (Exception e)
		{
			ans = defaultVal;
		}
		return ans;
	}
	/**
	 *  Get a long value with default if not present.
	 *
	 *@param  propName    the name of the property.
	 *@param  defaultVal  the default value.
	 *@return             The PropDefault value
	 */
	public static long getPropDefault(String propName, long defaultVal)
	{
		long ans;
		try
		{
			ans =
				(Long
					.valueOf(
						appProperties
							.getProperty(propName, Long.toString(defaultVal))
							.trim()))
					.longValue();
		}
		catch (Exception e)
		{
			ans = defaultVal;
		}
		return ans;
	}
	/**
	 *  Get a String value with default if not present.
	 *
	 *@param  propName    the name of the property.
	 *@param  defaultVal  the default value.
	 *@return             The PropDefault value
	 */
	public static String getPropDefault(String propName, String defaultVal)
	{
		String ans;
		try
		{
			ans = appProperties.getProperty(propName, defaultVal).trim();
		}
		catch (Exception e)
		{
			ans = defaultVal;
		}
		return ans;
	}
	/**
	 *  Sets the selection of the JComboBox to the Object 'name' from the list in
	 *  namVec.
	 *
	 *@param  properties  Description of Parameter
	 *@param  combo       Description of Parameter
	 *@param  namVec      Description of Parameter
	 *@param  name        Description of Parameter
	 */
	public static void selJComboBoxItem(
		Properties properties,
		JComboBox combo,
		Vector namVec,
		String name)
	{
		int idx = namVec.indexOf(name);
		combo.setSelectedIndex(idx);
		// Redisplay.
		combo.updateUI();
		return;
	}
	/**
	 *  Instatiate an object and guarantee its class.
	 *
	 *@param  className  The name of the class to instantiate.
	 *@param  impls      The name of the class it subclases.
	 *@return            Description of the Returned Value
	 */
	public static Object instantiate(String className, String impls)
	{
		if (className != null)
			className.trim();
		if (impls != null)
			className.trim();
		try
		{
			Class c = Class.forName(impls);
			try
			{
				Class o = Class.forName(className);
				Object res = o.newInstance();
				if (c.isInstance(res))
				{
					return res;
				}
				else
				{
					throw new IllegalArgumentException(
						className + " is not an instance of " + impls);
				}
			}
			catch (ClassNotFoundException e)
			{
				log.error("Error loading class " + className + ": class is not found");
			}
			catch (IllegalAccessException e)
			{
				log.error(
					"Error loading class " + className + ": does not have access");
			}
			catch (InstantiationException e)
			{
				log.error(
					"Error loading class " + className + ": could not instantiate");
			}
			catch (NoClassDefFoundError e)
			{
				log.error(
					"Error loading class "
						+ className
						+ ": couldn't find class "
						+ e.getMessage());
			}
		}
		catch (ClassNotFoundException e)
		{
			log.error("Error loading class " + impls + ": was not found.");
		}
		return null;
	}
	/**
	 *  Instantiate a vector of classes
	 *
	 *@param  v          Description of Parameter
	 *@param  className  Description of Parameter
	 *@return            Description of the Returned Value
	 */
	public static Vector instantiate(Vector v, String className)
	{
		Vector i = new Vector();
		try
		{
			Class c = Class.forName(className);
			Enumeration elements = v.elements();
			while (elements.hasMoreElements())
			{
				String name = (String) elements.nextElement();
				try
				{
					Object o = Class.forName(name).newInstance();
					if (c.isInstance(o))
					{
						i.addElement(o);
					}
				}
				catch (ClassNotFoundException e)
				{
					log.error("Error loading class " + name + ": class is not found");
				}
				catch (IllegalAccessException e)
				{
					log.error("Error loading class " + name + ": does not have access");
				}
				catch (InstantiationException e)
				{
					log.error("Error loading class " + name + ": could not instantiate");
				}
				catch (NoClassDefFoundError e)
				{
					log.error(
						"Error loading class "
							+ name
							+ ": couldn't find class "
							+ e.getMessage());
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			log.error("Error loading class " + className + ": class is not found");
		}
		return i;
	}
	/**
	 *  Tokenize a string into a vector of tokens
	 *
	 *@param  string     Description of Parameter
	 *@param  separator  Description of Parameter
	 *@return            Description of the Returned Value
	 */
	public static Vector tokenize(String string, String separator)
	{
		Vector v = new Vector();
		StringTokenizer s = new StringTokenizer(string, separator);
		while (s.hasMoreTokens())
		{
			v.addElement(s.nextToken());
		}
		return v;
	}
	/**
	 *  Create a button with the netscape style
	 *
	 *@param  name      Description of Parameter
	 *@param  listener  Description of Parameter
	 *@return           Description of the Returned Value
	 */
	public static JButton createButton(String name, ActionListener listener)
	{
		JButton button = new JButton(getImage(name + ".on.gif"));
		button.setDisabledIcon(getImage(name + ".off.gif"));
		button.setRolloverIcon(getImage(name + ".over.gif"));
		button.setPressedIcon(getImage(name + ".down.gif"));
		button.setActionCommand(name);
		button.addActionListener(listener);
		button.setRolloverEnabled(true);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setOpaque(false);
		button.setPreferredSize(new Dimension(24, 24));
		return button;
	}
	/**
	 *  Create a button with the netscape style
	 *
	 *@param  name      Description of Parameter
	 *@param  listener  Description of Parameter
	 *@return           Description of the Returned Value
	 */
	public static JButton createSimpleButton(
		String name,
		ActionListener listener)
	{
		JButton button = new JButton(getImage(name + ".gif"));
		button.setActionCommand(name);
		button.addActionListener(listener);
		button.setFocusPainted(false);
		button.setBorderPainted(false);
		button.setOpaque(false);
		button.setPreferredSize(new Dimension(25, 25));
		return button;
	}
	/**
	 *  Takes a String and a tokenizer character, and returns a new array of
	 *  strings of the string split by the tokenizer character.
	 *
	 *@param  splittee   String to be split
	 *@param  splitChar  Character to split the string on
	 *@param  def        Default value to place between two split chars that have
	 *      nothing between them
	 *@return            Array of all the tokens.
	 */
	public static String[] split(String splittee, String splitChar, String def)
	{
		if (splittee == null || splitChar == null)
		{
			return new String[0];
		}
		StringTokenizer tokens;
		String temp;
		int spot;
		while ((spot = splittee.indexOf(splitChar + splitChar)) != -1)
		{
			splittee =
				splittee.substring(0, spot + splitChar.length())
					+ def
					+ splittee.substring(spot + 1 * splitChar.length(), splittee.length());
		}
		Vector returns = new Vector();
		int start = 0;
		int length = splittee.length();
		spot = 0;
		while (start < length && (spot = splittee.indexOf(splitChar, start)) > -1)
		{
			if (spot > 0)
			{
				returns.addElement(splittee.substring(start, spot));
			}
			start = spot + splitChar.length();
		}
		if (start < length)
		{
			returns.add(splittee.substring(start));
		}
		String[] values = new String[returns.size()];
		returns.copyInto(values);
		return values;
	}
	/**
	 *  Report an error through a dialog box.
	 *
	 *@param  errorMsg  the error message.
	 */
	public static void reportErrorToUser(String errorMsg)
	{
		JOptionPane.showMessageDialog(
			GuiPackage.getInstance().getMainFrame(),
			errorMsg,
			"Error",
			JOptionPane.ERROR_MESSAGE);
	}
	/**
	 *  Finds a string in an array of strings and returns the
	 *
	 *@param  array  Array of strings.
	 *@param  value  String to compare to array values.
	 *@return        Index of value in array, or -1 if not in array.
	 */
	public static int findInArray(String[] array, String value)
	{
		int count = -1;
		int index = -1;
		if (array != null && value != null)
		{
			while (++count < array.length)
			{
				if (array[count] != null && array[count].equals(value))
				{
					index = count;
					break;
				}
			}
		}
		return index;
	}
	/**
	 *  Takes an array of strings and a tokenizer character, and returns a string
	 *  of all the strings concatenated with the tokenizer string in between each
	 *  one.
	 *
	 *@param  splittee   Array of Objects to be concatenated.
	 *@param  splitChar  Object to unsplit the strings with.
	 *@return            Array of all the tokens.
	 */
	public static String unsplit(Object[] splittee, Object splitChar)
	{
		StringBuffer retVal = new StringBuffer("");
		int count = -1;
		while (++count < splittee.length)
		{
			if (splittee[count] != null)
			{
				retVal.append(splittee[count]);
			}
			if (count + 1 < splittee.length && splittee[count + 1] != null)
			{
				retVal.append(splitChar);
			}
		}
		return retVal.toString();
	}
	// End Method
	/**
	 *  Takes an array of strings and a tokenizer character, and returns a string
	 *  of all the strings concatenated with the tokenizer string in between each
	 *  one.
	 *
	 *@param  splittee   Array of Objects to be concatenated.
	 *@param  splitChar  Object to unsplit the strings with.
	 *@param  def        Default value to replace null values in array.
	 *@return            Array of all the tokens.
	 */
	public static String unsplit(Object[] splittee, Object splitChar, String def)
	{
		StringBuffer retVal = new StringBuffer("");
		int count = -1;
		while (++count < splittee.length)
		{
			if (splittee[count] != null)
			{
				retVal.append(splittee[count]);
			}
			else
			{
				retVal.append(def);
			}
			if (count + 1 < splittee.length)
			{
				retVal.append(splitChar);
			}
		}
		return retVal.toString();
	}
	// End Method
	public static String getJMeterHome()
	{
		return jmDir;
	}
	public static void setJMeterHome(String home)
	{
		jmDir = home;
	}
	private static String jmDir;
	public static final String JMETER = "jmeter";
	public static final String ENGINE = "jmeter.engine";
	public static final String ELEMENTS = "jmeter.elements";
	public static final String GUI = "jmeter.gui";
	public static final String UTIL = "jmeter.util";
	public static final String CLASSFINDER = "jmeter.util.classfinder";
	public static final String TEST = "jmeter.test";
	public static final String HTTP = "jmeter.protocol.http";
	public static final String JDBC = "jmeter.protocol.jdbc";
	public static final String FTP = "jmeter.protocol.ftp";
	public static final String JAVA = "jmeter.protocol.java";
    public static final String PROPERTIES = "jmeter.elements.properties";
	/**
	 * Gets the JMeter Version.
	 * @returns the JMeter version.
	 */
	public static String getJMeterVersion()
	{
		return VERSION;
	}
}




















