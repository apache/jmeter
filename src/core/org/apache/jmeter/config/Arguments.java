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
package org.apache.jmeter.config;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;

// Mark Walsh, 2002-08-03 add method addArgument(String name, Object value, Object metadata)
// modify methods toString(), addEmptyArgument(), addArgument(String name, Object value)
/****************************************
 * Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 * Apache Foundation
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class Arguments extends ConfigTestElement implements Serializable
{
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	public static String[] COLUMN_NAMES = {
			JMeterUtils.getResString("name"),
			JMeterUtils.getResString("value"),
                        JMeterUtils.getResString("metadata")
			};

	public final static String ARGUMENTS = "Arguments.arguments";


	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public Arguments()
	{
		setProperty(new CollectionProperty(ARGUMENTS,new ArrayList()));
	}

	public CollectionProperty getArguments()
	{
		return (CollectionProperty)getProperty(ARGUMENTS);
	}
    
    public void clear()
    {
        super.clear();
        setProperty(new CollectionProperty(ARGUMENTS,new ArrayList()));
    }
	
	public void setArguments(List arguments)
	{
		setProperty(new CollectionProperty(ARGUMENTS,arguments));
	}
	
	public Map getArgumentsAsMap()
	{
		PropertyIterator iter = getArguments().iterator();
		Map argMap = new HashMap();
		while(iter.hasNext())
		{
			Argument arg = (Argument)iter.next().getObjectValue();
			argMap.put(arg.getName(),arg.getValue());
		}
		return argMap;
	}

	/****************************************
	 * !ToDo
	 *
	 *@param name   !ToDo
	 *@param value  !ToDo
	 ***************************************/
	public void addArgument(String name, String value)
	{
		addArgument(new Argument(name, value, null));
	}
	
	public void addArgument(Argument arg)
	{
        TestElementProperty newArg = new TestElementProperty(arg.getName(),arg);
        if(isRunningVersion())
        {
            newArg.setTemporary(true,this);
        }
        getArguments().addItem(newArg);
	}

	/****************************************
	 * !ToDo
	 *
	 *@param name   !ToDo
	 *@param value  !ToDo
	 *@param metadata  Hold addition information
	 ***************************************/
	public void addArgument(String name, String value, String metadata)
	{
		addArgument(new Argument(name, value, metadata));
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public PropertyIterator iterator()
	{
		return getArguments().iterator();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		PropertyIterator iter = getArguments().iterator();
		while(iter.hasNext())
		{
			Argument arg = (Argument)iter.next().getObjectValue();
			if (arg.getMetaData() == null) {
			    str.append(arg.getName() + "=" + arg.getValue());
			} else {
			    str.append(arg.getName() + arg.getMetaData() + arg.getValue());
			}
			if(iter.hasNext())
			{
				str.append("&");
			}
		}
		return str.toString();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param row  !ToDo (Parameter description)
	 ***************************************/
	public void removeArgument(int row)
	{
		if(row < getArguments().size())
		{
			getArguments().remove(row);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param arg  !ToDo (Parameter description)
	 ***************************************/
	public void removeArgument(Argument arg)
	{
		PropertyIterator iter = getArguments().iterator();
        while(iter.hasNext())
        {
            Argument item = (Argument)iter.next().getObjectValue();
            if(arg.equals(item))
            {
                iter.remove();
            }
        }
	}
	
	public void removeArgument(String argName)
	{
		PropertyIterator iter = getArguments().iterator();
		while(iter.hasNext())
		{
			Argument arg = (Argument)iter.next().getObjectValue();
			if(arg.getName().equals(argName))
			{
				iter.remove();
			}
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void removeAllArguments()
	{
		getArguments().clear();
	}

	/****************************************
	 * !ToDo
	 ***************************************/
	public void addEmptyArgument()
	{
		addArgument(new Argument("", "",null));
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int getArgumentCount()
	{
		return getArguments().size();
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param row  !ToDo (Parameter description)
	 *@return     !ToDo (Return description)
	 ***************************************/
	public Argument getArgument(int row)
	{
		Argument argument = null;

		if(row < getArguments().size())
		{
			argument = (Argument)getArguments().get(row);
		}

		return argument;
	}
}
