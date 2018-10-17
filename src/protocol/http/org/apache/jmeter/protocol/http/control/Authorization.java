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
import java.nio.charset.Charset;
import java.util.Base64;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.testelement.AbstractTestElement;

/**
 * This class is an Authorization encapsulator.
 *
 */
public class Authorization extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 241L;

    private static final String URL = "Authorization.url"; // $NON-NLS-1$

    private static final String USERNAME = "Authorization.username"; // $NON-NLS-1$

    private static final String PASSWORD = "Authorization.password"; // $NON-NLS-1$ NOSONAR no hard coded password

    private static final String DOMAIN = "Authorization.domain"; // $NON-NLS-1$

    private static final String REALM = "Authorization.realm"; // $NON-NLS-1$

    private static final String MECHANISM = "Authorization.mechanism"; // $NON-NLS-1$

    private static final String TAB = "\t"; // $NON-NLS-1$

    /**
     * create the authorization
     * @param url url for which the authorization should be considered
     * @param user name of the user
     * @param pass password for the user
     * @param domain authorization domain (used for NTML-authentication)
     * @param realm authorization realm
     * @param mechanism authorization mechanism, that should be used
     */
    Authorization(String url, String user, String pass, String domain, String realm, Mechanism mechanism) {
        setURL(url);
        setUser(user);
        setPass(pass);
        setDomain(domain);
        setRealm(realm);
        setMechanism(mechanism);
    }

    public boolean expectsModification() {
        return false;
    }

    public Authorization() {
        this("","","","","", Mechanism.BASIC);
    }

    public void addConfigElement(ConfigElement config) {
        // NOOP
    }

    public String getURL() {
        return getPropertyAsString(URL);
    }

    public void setURL(String url) {
        setProperty(URL, url);
    }

    public String getUser() {
        return getPropertyAsString(USERNAME);
    }

    public void setUser(String user) {
        setProperty(USERNAME, user);
    }

    public String getPass() {
        return getPropertyAsString(PASSWORD);
    }

    public void setPass(String pass) {
        setProperty(PASSWORD, pass);
    }

    public String getDomain() {
        return getPropertyAsString(DOMAIN);
    }

    public void setDomain(String domain) {
        setProperty(DOMAIN, domain);
    }

    public String getRealm() {
        return getPropertyAsString(REALM);
    }

    public void setRealm(String realm) {
        setProperty(REALM, realm);
    }

    public Mechanism getMechanism() {
        return Mechanism.valueOf(getPropertyAsString(MECHANISM, Mechanism.BASIC.name()));
    }

    public void setMechanism(Mechanism mechanism) {
        setProperty(MECHANISM, mechanism.name(), Mechanism.BASIC.name());
    }

    // Used for saving entries to a file
    @Override
    public String toString() {
        return getURL() + TAB + getUser() + TAB + getPass() + TAB + getDomain() + TAB + getRealm() + TAB + getMechanism();
    }

    public String toBasicHeader(){
        return "Basic " + new String(Base64.getEncoder().encode((getUser() + ":" + getPass()).
                getBytes(Charset.defaultCharset())), Charset.defaultCharset());
    }
}
