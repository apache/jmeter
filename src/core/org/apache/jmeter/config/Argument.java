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

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;

//Mark Walsh, 2002-08-03, add metadata attribute
// add constructor Argument(String name, Object value, Object metadata)
// add MetaData get and set methods
/****************************************
 * Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 * Apache Foundation
 *
 *@author    Michael Stover
 *@created   March 13, 2001
 *@version   1.0
 ***************************************/

public class Argument extends AbstractTestElement implements Serializable
{
	protected final static String NAME = "Argument.name";
	protected final static String VALUE = "Argument.value";
        private final static String METADATA = "Argument.metadata";
    
	/****************************************
	 * Constructor for the Argument object
	 *
	 *@param name   Description of Parameter
	 *@param value  Description of Parameter
	 *@param metadata Description of Parameter
	 ***************************************/
	public Argument(String name, Object value, Object metadata)
	{
		setProperty(NAME, name);
		setProperty(VALUE, value);
		setProperty(METADATA, metadata);
	}

	/****************************************
	 * Constructor for the Argument object
	 *
	 *@param name   Description of Parameter
	 *@param value  Description of Parameter
	 ***************************************/
	public Argument(String name, Object value)
	{
		setProperty(NAME, name);
		setProperty(VALUE, value);
	}

	/****************************************
	 * Constructor for the Argument object
	 ***************************************/
	public Argument() { }

	/****************************************
	 * !ToDo
	 *
	 *@param el  !ToDo
	 ***************************************/
	public void addTestElement(TestElement el) { }

	/****************************************
	 * Sets the Name attribute of the Argument object
	 *
	 *@param newName  The new Name value
	 ***************************************/
	public void setName(String newName)
	{
		setProperty(NAME, newName);
	}

	/****************************************
	 * Sets the Value attribute of the Argument object
	 *
	 *@param newValue  The new Value value
	 ***************************************/
	public void setValue(Object newValue)
	{
		setProperty(VALUE, newValue);
	}

	/****************************************
	 * Sets the Meta Data attribute of the Argument object
	 *
	 *@param newMetaData  The new Metadata value
	 ***************************************/
	public void setMetaData(Object newMetaData)
	{
		setProperty(METADATA, newMetaData);
	}

	/****************************************
	 * Gets the Name attribute of the Argument object
	 *
	 *@return   The Name value
	 ***************************************/
	public String getName()
	{
		return (String)getProperty(NAME);
	}

	/****************************************
	 * Gets the Value attribute of the Argument object
	 *
	 *@return   The Value value
	 ***************************************/
	public Object getValue()
	{
		return getProperty(VALUE);
	}

	/****************************************
	 * Gets the Meta Data attribute of the Argument object
	 *
	 *@return   The MetaData value
	 ***************************************/
	public Object getMetaData()
	{
		return getProperty(METADATA);
	}
}
