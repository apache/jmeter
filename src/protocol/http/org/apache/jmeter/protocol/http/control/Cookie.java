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

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * This class is a Cookie encapsulator.
 *
 * @author  <a href="mailto:sdowd@arcmail.com">Sean Dowd</a>
 */
public class Cookie extends AbstractTestElement implements Serializable
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");
	 private static String NAME = "Cookie.name";
	 private static String VALUE = "Cookie.value";
	 private static String DOMAIN = "Cookie.domain";
	 private static String EXPIRES = "Cookie.expires";
	 private static String SECURE = "Cookie.secure";
	 private static String PATH = "Cookie.path";

	 /**
	  * create the coookie
	  */
	 public Cookie() {
		  this.setName("");
		  this.setValue("");
		  this.setDomain("");
		  this.setPath("");
		  this.setSecure(false);
		  this.setExpires(0);
	 }

	 /**
	  * create the coookie
	  */
	 public Cookie(String name, String value, String domain, String path, boolean secure, long expires) {
		  this.setName(name);
		  this.setValue(value);
		  this.setDomain(domain);
		  this.setPath(path);
		  this.setSecure(secure);
		  this.setExpires(expires);
	 }

	public void addConfigElement(ConfigElement config)
	{
	}

	public boolean expectsModification()
	{
		return false;
	}

	 public String getClassLabel()
	 {
		return "Cookie";
	 }

	 /**
	  * get the value for this object.
	  */
	 public String getValue() {
		  return (String)this.getProperty(VALUE);
	 }

	 /**
	  * set the value for this object.
	  */
	 public synchronized void setValue(String value) {
		  this.setProperty(VALUE,value);
	 }

	 /**
	  * get the domain for this object.
	  */
	 public String getDomain() {
		  return (String)getProperty(DOMAIN);
	 }

	 /**
	  * set the domain for this object.
	  */
	 public synchronized void setDomain(String domain) {
		  setProperty(DOMAIN,domain);
	 }

	 /**
	  * get the expires for this object.
	  */
	 public long getExpires() {
		Object ret = getProperty(EXPIRES);
		if(ret == null)
		{
			return 0;
		}
		else if(ret instanceof Long)
		{
			return ((Long)ret).longValue();
		}
		else if(ret instanceof String)
		{
			try
			{
				return Long.parseLong((String)ret);
			}
			catch (Exception ex)
			{
			}
		}
		return 0;
	 }

	 /**
	  * set the expires for this object.
	  */
	 public synchronized void setExpires(long expires) {
		  setProperty(EXPIRES,new Long(expires));
	 }

	 /**
	  * get the secure for this object.
	  */
	 public boolean getSecure() {
	 	log.info("Secure = "+getProperty(SECURE));
		return this.getPropertyAsBoolean(SECURE);
	 }

	 /**
	  * set the secure for this object.
	  */
	 public synchronized void setSecure(boolean secure) {
		  setProperty(SECURE,new Boolean(secure));
	 }

	 /**
	  * get the path for this object.
	  */
	 public String getPath() {
		  return (String)getProperty(PATH);
	 }

	 /**
	  * set the path for this object.
	  */
	 public synchronized void setPath(String path) {
		  setProperty(PATH,path);
	 }

	 /**
	  * creates a string representation of this cookie
	  */
	 public String toString() {
	return getDomain() + "\tTRUE\t" + getPath() + "\t" +
							 new Boolean(getSecure()).toString().toUpperCase() + "\t" +
							 getExpires() + "\t" + getName() + "\t" + getValue();
	 }
}
