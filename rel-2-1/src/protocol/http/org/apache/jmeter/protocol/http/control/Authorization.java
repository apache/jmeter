// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
import org.apache.jmeter.protocol.http.util.Base64Encoder;
import org.apache.jmeter.testelement.AbstractTestElement;

/**
 * This class is an Authorization encapsulator.
 * 
 * @author <a href="mailto:luta.raphael@networks.vivendi.net">Raphael Luta</a>
 * @version $Revision$
 */
public class Authorization extends AbstractTestElement implements Serializable {
	private static String URL = "Authorization.url";

	private static String USERNAME = "Authorization.username";

	private static String PASSWORD = "Authorization.password";

	/**
	 * create the authorization
	 */
	Authorization(String url, String user, String pass) {
		setURL(url);
		setUser(user);
		setPass(pass);
	}

	public boolean expectsModification() {
		return false;
	}

	public Authorization() {
		setURL("");
		setUser("");
		setPass("");
	}

	public String getClassLabel() {
		return "Authorization";
	}

	public void addConfigElement(ConfigElement config) {
	}

	public synchronized String getURL() {
		return getPropertyAsString(URL);
	}

	public synchronized void setURL(String url) {
		setProperty(URL, url);
	}

	public synchronized String getUser() {
		return getPropertyAsString(USERNAME);
	}

	public synchronized void setUser(String user) {
		setProperty(USERNAME, user);
	}

	public synchronized String getPass() {
		return getPropertyAsString(PASSWORD);
	}

	public synchronized void setPass(String pass) {
		setProperty(PASSWORD, pass);
	}

	public String toString() {
		return getURL() + "\t" + getUser() + "\t" + getPass();
	}
    
    public String toBasicHeader(){
        return "Basic " + Base64Encoder.encode(getUser() + ":" + getPass());
    }
}
