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
 package org.apache.jmeter.samplers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigElement;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/************************************************************
 *  Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 *  Apache Foundation
 *
 *@author     Michael Stover
 *@created    $Date$
 *@version    1.0
 ***********************************************************/

public class Entry
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");
	Map configSet;
	Set clonedSet;
	Class sampler;
	List assertions;

	/************************************************************
	 *  !ToDo (Constructor description)
	 ***********************************************************/
	public Entry()
	{
		configSet = new HashMap();
		clonedSet = new HashSet();
		assertions = new LinkedList();
	}

	public void addAssertion(Assertion assertion)
	{
		assertions.add(assertion);
	}

	public List getAssertions()
	{
		return assertions;
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  samplerClass  !ToDo (Parameter description)
	 ***********************************************************/
	public void setSamplerClass(Class samplerClass)
	{
		this.sampler = samplerClass;
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public Class getSamplerClass()
	{
		return this.sampler;
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@param  configClass  !ToDo (Parameter description)
	 *@return              !ToDo (Return description)
	 ***********************************************************/
	public ConfigElement getConfigElement(Class configClass)
	{
		return (ConfigElement)configSet.get(configClass);
	}

	/************************************************************
	 *  !ToDo
	 *
	 *@param  config  !ToDo
	 ***********************************************************/
	public void addConfigElement(ConfigElement config)
	{
		addConfigElement(config,config.getClass());
	}

	/**
	 * Add a config element as a specific class.  Usually this is done to add a
	 * subclass as one of it's parent classes.
	 */
	public void addConfigElement(ConfigElement config,Class asClass)
	{
		if (config != null)
		{
			ConfigElement current = (ConfigElement)configSet.get(asClass);
			if (current == null)
			{
				configSet.put(asClass, cloneIfNecessary(config));
			}
			else
			{
				current.addConfigElement(config);
			}
		}
	}

	private ConfigElement cloneIfNecessary(ConfigElement config)
	{
		if(config.expectsModification())
		{
			return config;
		}
		else
		{
			return (ConfigElement)config.clone();
		}
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (Exception ex)
		{
			log.error("",ex);
		}
		return null;
	}

}
