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

import java.lang.Exception;

import org.apache.log4j.Category;
/**
 * Exception thrown when creating MethodConfigUserObject if the value
 * is not compatible for the class.
 *
 * @author	Khor Soon Hin
 * @version	$Revision$ Last Updated: $Date$
 * Created	2002 Jan 08
 */
public class MethodConfigUserObjectException extends Exception
{
  private static Category catClass = Category.getInstance(
	MethodConfigUserObjectException.class.getName());

  public MethodConfigUserObjectException(String string)
  {
    super(string);
  }
}
