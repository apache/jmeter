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

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.protocol.http.util.Base64Encoder;
import org.apache.jmeter.testelement.AbstractTestElement;

/**
 * This class is an Authorization encapsulator.
 *
 */
public class Authorization extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    private static final String URL = "Authorization.url"; // $NON-NLS-1$

    private static final String USERNAME = "Authorization.username"; // $NON-NLS-1$

    private static final String PASSWORD = "Authorization.password"; // $NON-NLS-1$

    private static final String DOMAIN = "Authorization.domain"; // $NON-NLS-1$

    private static final String REALM = "Authorization.realm"; // $NON-NLS-1$

    private static final String TAB = "\t"; // $NON-NLS-1$

    /**
     * create the authorization
     */
    Authorization(String url, String user, String pass, String domain, String realm) {
        setURL(url);
        setUser(user);
        setPass(pass);
        setDomain(domain);
        setRealm(realm);
    }

    public boolean expectsModification() {
        return false;
    }

    public Authorization() {
        this("","","","","");
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

    public synchronized String getDomain() {
        return getPropertyAsString(DOMAIN);
    }

    public synchronized void setDomain(String domain) {
        setProperty(DOMAIN, domain);
    }

    public synchronized String getRealm() {
        return getPropertyAsString(REALM);
    }

    public synchronized void setRealm(String realm) {
        setProperty(REALM, realm);
    }

    // Used for saving entries to a file
    @Override
    public String toString() {
        return getURL() + TAB + getUser() + TAB + getPass() + TAB + getDomain() + TAB + getRealm();
    }

    public String toBasicHeader(){
        return "Basic " + Base64Encoder.encode(getUser() + ":" + getPass());
    }
}
