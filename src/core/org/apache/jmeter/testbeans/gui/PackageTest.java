/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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
 * 
 * @author Sebastian Bazley &lt; sebb AT apache DOT org &gt;
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.testbeans.gui;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.log.Logger;

import junit.framework.Test;
//import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * Find all beans out there and check their resource property files:
 * - Check that non-default property files don't have any extra keys.
 * - Check all necessary properties are defined at least in the default property file,
 *   except for beans whose name contains "Experimental" or "Alpha".
 * TODO: - Check property files don't have duplicate keys (is this important)
 */
public class PackageTest extends JMeterTestCase
{
	private static Logger log = LoggingManager.getLoggerForClass();

	private ResourceBundle defaultBundle;

	private Class testBeanClass;

	private String language;

	private PackageTest(
		Class testBeanClass, String language, ResourceBundle defaultBundle)
	{
		super(testBeanClass.getName()+" - "+language);
		this.testBeanClass= testBeanClass;
		this.language= language;
		this.defaultBundle= defaultBundle;
	}
	
	BeanInfo beanInfo;
	ResourceBundle bundle;

	public void setUp()
	{
		JMeterUtils.setLocale(new Locale(language,""));
		try
		{
			beanInfo= Introspector.getBeanInfo(testBeanClass, TestBean.class);
			bundle= (ResourceBundle) beanInfo
				.getBeanDescriptor()
				.getValue(TestBeanGUI.RESOURCE_BUNDLE);
		}
		catch (IntrospectionException e)
		{
			log.error("Can't get beanInfo for "+testBeanClass.getName(),
				e);
			throw new Error(e.toString()); // Programming error. Don't continue.
		}
		if (bundle == null) throw new Error("This can't happen!");
	}
	
	public void runTest()
	{
		if (bundle == defaultBundle) checkAllNecessaryKeysPresent();
		else checkNoInventedKeys();
	}

	public void checkNoInventedKeys()
	{		
		// Check that all keys in the bundle are also in the default bundle:
		for (Enumeration keys= bundle.getKeys(); keys.hasMoreElements(); )
		{
			String key= (String)keys.nextElement();
			defaultBundle.getString(key);
				// Will throw MissingResourceException if key is not there.
		}
	}
	
	public void checkAllNecessaryKeysPresent()
	{
		// Check that all necessary keys are there:

		// displayName is always mandatory:
		String dn= defaultBundle.getString("displayName").toLowerCase();

		// Skip the rest of this test for alpha/experimental beans:
		if (dn.indexOf("alpha") != -1
			|| dn.indexOf("experimental") != -1) return;

		// Check for property- and group-related texts:
		PropertyDescriptor[] descriptors= beanInfo.getPropertyDescriptors();
		for (int i=0; i<descriptors.length; i++)
		{
			// Skip non-editable properties, that is:
			// Ignore hidden, read-only, and write-only properties
			if (descriptors[i].isHidden()
				|| descriptors[i].getReadMethod() == null 
				|| descriptors[i].getWriteMethod() == null) continue;
			// Ignore TestElement properties which don't have an explicit editor:
			if (TestElement.class.isAssignableFrom(
					descriptors[i].getPropertyType())
				&& descriptors[i].getPropertyEditorClass() == null) continue;
			// Done -- we're working with an editable property.

			String name= descriptors[i].getName();
				
			bundle.getString(name+".displayName");
			//bundle.getString(name+".shortDescription"); NOT MANDATORY

			String group= (String)descriptors[i].getValue(TestBeanGUI.GROUP);
			if (group != null) bundle.getString(group+".displayName");
		}
	}

	public static Test suite() throws Exception
	{
		TestSuite suite = new TestSuite("Bean Resource Test Suite");

		//ResourceBundle i18nEdit= ResourceBundle.getBundle("org.apache.jmeter.resources.i18nedit");
		String[] languages= new String[] { "de", "ja", "no" };
		String defaultLanguage= "en"; //i18nEdit.getString("locale.default");
			// TODO: find a clean way to get these from i18nedit.properties

		Iterator iter =
			ClassFinder
				.findClassesThatExtend(
					JMeterUtils.getSearchPaths(),
					new Class[] { TestBean.class })
				.iterator();

		while (iter.hasNext())
		{
			Class testBeanClass= Class.forName((String)iter.next());
			JMeterUtils.setLocale(new Locale(defaultLanguage,""));
			ResourceBundle defaultBundle;
			try
			{
				defaultBundle= (ResourceBundle)
					Introspector.getBeanInfo(testBeanClass, TestBean.class)
					.getBeanDescriptor()
					.getValue(TestBeanGUI.RESOURCE_BUNDLE);
			}
			catch (IntrospectionException e)
			{
				log.error("Can't get beanInfo for "+testBeanClass.getName(),
					e);
				throw new Error(e.toString()); // Programming error. Don't continue.
			}

			if (defaultBundle == null)
			{
				throw new Error("No default bundle for class "
						+testBeanClass.getName());
			}

			suite.addTest(new PackageTest(testBeanClass, defaultLanguage, defaultBundle));

			for (int i=0; i<languages.length; i++)
			{
				suite.addTest(new PackageTest(testBeanClass, languages[i], defaultBundle));
			}
		}

		return suite;
	}
}
