/*
 * Copyright 2001-2004,2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * This class is a Cookie encapsulator.
 * 
 * @author <a href="mailto:sdowd@arcmail.com">Sean Dowd</a>
 */
public class Cookie extends AbstractTestElement implements Serializable {
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

	public void addConfigElement(ConfigElement config) {
	}

	public boolean expectsModification() {
		return false;
	}

	public String getClassLabel() {
		return "Cookie";
	}

	/**
	 * get the value for this object.
	 */
	public synchronized String getValue() {
		return getPropertyAsString(VALUE);
	}

	/**
	 * set the value for this object.
	 */
	public synchronized void setValue(String value) {
		this.setProperty(VALUE, value);
	}

	/**
	 * get the domain for this object.
	 */
	public synchronized String getDomain() {
		return getPropertyAsString(DOMAIN);
	}

	/**
	 * set the domain for this object.
	 */
	public synchronized void setDomain(String domain) {
		setProperty(DOMAIN, domain);
	}

	/**
	 * get the expiry time for the cookie
     * 
     * @return Expiry time in seconds since the Java epoch
	 */
	public synchronized long getExpires() {
		return getPropertyAsLong(EXPIRES);
	}

    /**
     * get the expiry time for the cookie
     * 
     * @return Expiry time in milli-seconds since the Java epoch, 
     * i.e. same as System.currentTimeMillis()
     */
    public synchronized long getExpiresMillis() {
        return getPropertyAsLong(EXPIRES)*1000;
    }

	/**
	 * set the expiry time for the cookie
     * @param expires - expiry time in seconds since the Java epoch
	 */
	public synchronized void setExpires(long expires) {
		setProperty(new LongProperty(EXPIRES, expires));
	}

	/**
	 * get the secure for this object.
	 */
	public synchronized boolean getSecure() {
		return getPropertyAsBoolean(SECURE);
	}

	/**
	 * set the secure for this object.
	 */
	public synchronized void setSecure(boolean secure) {
		setProperty(new BooleanProperty(SECURE, secure));
	}

	/**
	 * get the path for this object.
	 */
	public synchronized String getPath() {
		return getPropertyAsString(PATH);
	}

	/**
	 * set the path for this object.
	 */
	public synchronized void setPath(String path) {
		setProperty(PATH, path);
	}

	/**
	 * creates a string representation of this cookie
	 */
	public String toString() {
		return getDomain() + "\tTRUE\t" + getPath() + "\t" + JOrphanUtils.booleanToSTRING(getSecure()) + "\t"
				+ getExpires() + "\t" + getName() + "\t" + getValue();
	}
}
