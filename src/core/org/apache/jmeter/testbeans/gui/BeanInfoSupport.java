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
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Support class for test bean beanInfo objects. It will help using the
 * introspector to get most of the information, to then modify it at will.
 * <p>
 * To use, subclass it, create a subclass with a parameter-less constructor
 * that:
 * <ol>
 * <li>Calls super(beanClass)
 * <li>Modifies the property descriptors, bean descriptor, etc. at will.
 * </ol>
 * <p>
 * Without any such modifications, the property descriptors will already
 * have localized display names and short descriptions, and the bean
 * descriptor will have a "resourceBoundle" attribute to be used for further
 * localization.
 */
public abstract class BeanInfoSupport implements BeanInfo {

	private static transient Logger log = LoggingManager.getLoggerForClass();

	/**
	 * The class for which we're providing the bean info.
	 */
	private Class beanClass;

	/**
	 * The BeanInfo for our class as obtained by the introspector.
	 */
	private BeanInfo rootBeanInfo;

	/**
	 * Construct a BeanInfo for the given class.
	 */
	protected BeanInfoSupport(Class beanClass) {
		
		this.beanClass= beanClass;
		
		try {
			rootBeanInfo= Introspector.getBeanInfo(
				beanClass,
				Introspector.IGNORE_IMMEDIATE_BEANINFO);
		} catch (IntrospectionException e) {
			log.error("Can't introspect.", e);
			throw new Error(e); // Programming error: bail out.
		}
	
		try{
			ResourceBundle resourceBundle= ResourceBundle.getBundle(
				beanClass.getName()+"Resources",
				JMeterUtils.getLocale()); 

			// Store the resource bundle as an attribute of the BeanDescriptor:
			getBeanDescriptor().setValue("resourceBundle", resourceBundle);

			// Localize the bean name
			try
			{
				getBeanDescriptor().setDisplayName(
					resourceBundle.getString("displayName"));
			}
			catch (MissingResourceException e)
			{
				log.debug(
					"Localized display name not available for bean "
					+beanClass.getName());
			}
			
			// Localize the property names and descriptions:
			PropertyDescriptor[] properties= getPropertyDescriptors();

			for (int i=0; i<properties.length; i++)
			{
				String name= properties[i].getName();
				String s;
			
				try
				{
					properties[i].setDisplayName(
						resourceBundle.getString(name+".displayName"));
				}
				catch (MissingResourceException e)
				{
					log.debug(
						"Localized display name not available for property "
						+name);
				}
			
				try
				{
					properties[i].setShortDescription(
						resourceBundle.getString(name+".shortDescription"));
				}
				catch (MissingResourceException e)
				{
					log.debug(
						"Localized short description not available for property "
						+name);
				}
			}
		}
		catch (MissingResourceException e)
		{
			log.warn("Localized strings not available for bean "+beanClass
							+" on locale "+JMeterUtils.getLocale());
		}
	}
	
	/**
	 * Get the property descriptor for the property of the given name.
	 * 
	 * @param name property name
	 * @return descriptor for a property of that name, or null if there's none
	 */
	protected PropertyDescriptor property(String name) {
		PropertyDescriptor[] properties= getPropertyDescriptors();
		for (int i=0; i<properties.length; i++)
		{
			if (properties[i].getName().equals(name)) {
				return properties[i];
			}
		}
		return null;
	}

	private int numCreatedGroups= 0;
	
	/**
	 * Utility method to group and order properties.
	 * <p>
	 * It will assing the given group name to each of the named properties,
	 * and set their order attribute so that they are shown in the given order.
	 * <p>
	 * The created groups will get order 1, 2, 3,... in the order in which they
	 * are created.
	 * 
	 * @param group name of the group
	 * @param names property names in the desired order
	 */
	protected void createPropertyGroup(String group, String[] names)
	{
		for (int i=0; i<names.length; i++)
		{
			PropertyDescriptor p= property(names[i]);
			p.setValue("group", group);
			p.setValue("order", new Integer(i));
		}
		numCreatedGroups++;
		getBeanDescriptor().setValue("group."+group+".order",
			new Integer(numCreatedGroups));
	}

	public BeanInfo[] getAdditionalBeanInfo() {
		return rootBeanInfo.getAdditionalBeanInfo();
	}

	public BeanDescriptor getBeanDescriptor() {
		return rootBeanInfo.getBeanDescriptor();
	}

	public int getDefaultEventIndex() {
		return rootBeanInfo.getDefaultEventIndex();
	}

	public int getDefaultPropertyIndex() {
		return rootBeanInfo.getDefaultPropertyIndex();
	}

	public EventSetDescriptor[] getEventSetDescriptors() {
		return rootBeanInfo.getEventSetDescriptors();
	}

	public Image getIcon(int iconKind) {
		return rootBeanInfo.getIcon(iconKind);
	}

	public MethodDescriptor[] getMethodDescriptors() {
		return rootBeanInfo.getMethodDescriptors();
	}

	public PropertyDescriptor[] getPropertyDescriptors() {
		return rootBeanInfo.getPropertyDescriptors();
	}
}
