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

package org.apache.jmeter.protocol.jdbc.util;

import java.io.*;

public class DBKey implements Serializable{


  public DBKey()
  {
  }

  void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
  {
	 ois.defaultReadObject();
  }

  void writeObject(ObjectOutputStream oos) throws IOException
  {
	 oos.defaultWriteObject();
  }

  public void setUrl(String newUrl)
  {
	 url = newUrl;
  }

  public String getUrl()
  {
	 return url;
  }

  public void setUsername(String newUsername)
  {
	 username = newUsername;
  }

  public String getUsername()
  {
	 return username;
  }

  public void setPassword(String newPassword)
  {
	 password = newPassword;
  }

  public String getPassword()
  {
	 return password;
  }

  public void setDriver(String newDriver)
  {
	 driver = newDriver;
  }

  public String getDriver()
  {
	 return driver;
  }

  public void setMaxUsage(int newMaxUsage)
  {
	 maxUsage = newMaxUsage;
  }

  public int getMaxUsage()
  {
	 return maxUsage;
  }

  public void setMaxConnections(int newMaxConnections)
  {
	 maxConnections = newMaxConnections;
  }

  public int getMaxConnections()
  {
	 return maxConnections;
  }

/**************************************************************
  Determines if the two DBKey objects have the same property values.
@param key DBKey to compare with this one.
@return bool True if equal, false otherwise.
****************************************************************/
  public boolean equals(Object key)
  {
	 if(key instanceof DBKey)
		return url.equals(((DBKey)key).getUrl());
	 else
		return false;
  }

  public int hashCode()
  {
	 return url.hashCode()*11;
  }

  public String toString()
  {
	 StringBuffer ret = new StringBuffer();
	 ret.append("Class=DBKey("+"\n");
	 ret.append("driver="+driver+"\n");
	 ret.append("url="+url+"\n");
	 ret.append("username="+username+"\n");
	 ret.append("Number of connections="+maxConnections+"\n");
	 ret.append("Max times each connection used before renewing="+maxUsage+"\n");
	 ret.append(")");
	 return ret.toString();
  }

  private String url;
  private String username;
  private String password;
  private String driver;
  private int maxUsage;
  private int maxConnections;
}

