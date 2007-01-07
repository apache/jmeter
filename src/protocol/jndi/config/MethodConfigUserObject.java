/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
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
 * @version $Revision$ Last Updated: $Date$
 * Created	2001 Jan 08
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
        object = Boolean.valueOf(value);
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
