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
package org.apache.jmeter.ejb.jndi.config;

import java.lang.Character;

import org.apache.jmeter.ejb.jndi.config.MethodConfigUserObjectException;
import org.apache.log4j.Category;
/**
 * Given the class of the parameter and its string value this class will
 * attempt to create an appropriate object to represent it e.g. if given
 * a class of int and value 8, a Integer object with the 8 value will be
 * created.  Failing which a MethodConfigUserObjectException will be thrown.
 *
 * @author	Khor Soon Hin
 * @version	1.0
 * @created	2001 Jan 08
 * @modified	2001 Jan 08
 */
public class MethodConfigUserObject 
{
  private static Category catClass = Category.getInstance(
	MethodConfigUserObject.class.getName());

  protected static final String INTEGER = "int";
  protected static final String LONG = "long";
  protected static final String FLOAT = "float";
  protected static final String DOUBLE = "double";
  protected static final String BOOLEAN = "boolean";
  protected static final String CHAR = "char";
  protected static final String BYTE = "byte";
  protected static final String SHORT = "short";
  protected static final String STRING_CLASS = "java.lang.String";

  protected Object object = null;
  protected Class type = null;

  public MethodConfigUserObject(Class type, String value)
	throws MethodConfigUserObjectException
  {
    if(type == null || value == null)
    {
      throw new MethodConfigUserObjectException(
	"Parameters of MethodConfigUserObject constructor cannot be null");
    }
    this.type = type;
    // ensure that the class type is one of the 8 primitives
    try
    { 
      if(type.getName().equals(INTEGER))
      {
        object = new Integer(value);
      }
      else if(type.getName().equals(LONG))
      {
        object = new Long(value);
      }
      else if(type.getName().equals(FLOAT))
      {
        object = new Float(value);
      }
      else if(type.getName().equals(DOUBLE))
      {
        object = new Double(value);
      }
      else if(type.getName().equals(BOOLEAN))
      {
        object = new Boolean(value);
      }
      else if(type.getName().equals(CHAR))
      {
        if(value.length() == 1)
        {
          object = new Character(value.charAt(0));
        }
        else
        {
          throw new MethodConfigUserObjectException(
		"Value format not compatible with class");
        }
      }
      else if(type.getName().equals(BYTE))
      {
        object = new Byte(value);
      }
      else if(type.getName().equals(SHORT))
      {
        object = new Short(value);
      }
      else if(type.getName().equals(STRING_CLASS))
      {
        object = new String(value);
      }
    }
    catch(NumberFormatException e)
    { 
      throw new MethodConfigUserObjectException(
	"Value format not compatible with class");
    }
  }

  public Object getObject()
  {
    return object;
  }

  public Class getType()
  {
    return type;
  }

  public String toString()
  {
    StringBuffer strbuff = new StringBuffer();
    strbuff.append(type.getName());
    strbuff.append(" : ");
    strbuff.append(object);
    return strbuff.toString();
  }
}
