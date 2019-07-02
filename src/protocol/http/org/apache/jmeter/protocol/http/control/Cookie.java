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
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jorphan.util.JOrphanUtils;

/**
 * This class is a Cookie encapsulator.
 *
 */
public class Cookie extends AbstractTestElement implements Serializable {
    private static final long serialVersionUID = 240L;

    private static final char TAB = '\t';

    private static final String VALUE = "Cookie.value"; //$NON-NLS-1$

    private static final String DOMAIN = "Cookie.domain"; //$NON-NLS-1$

    private static final String EXPIRES = "Cookie.expires"; //$NON-NLS-1$

    private static final String SECURE = "Cookie.secure"; //$NON-NLS-1$

    private static final String PATH = "Cookie.path"; //$NON-NLS-1$

    private static final String PATH_SPECIFIED = "Cookie.path_specified"; //$NON-NLS-1$

    private static final String DOMAIN_SPECIFIED = "Cookie.domain_specified"; //$NON-NLS-1$

    private static final String VERSION = "Cookie.version"; //$NON-NLS-1$

    private static final int DEFAULT_VERSION = 1;

    /**
     * create the coookie
     */
    public Cookie() {
        this("","","","",false,0,false,false);
    }

    /**
     * create the coookie
     * @param name name of the cookie
     * @param value value of the cookie
     * @param domain domain for which the cookie is valid
     * @param path  path for which the cookie is valid
     * @param secure flag whether cookie is to be handled as 'secure'
     * @param expires - this is in seconds
     *
     */
    public Cookie(String name, String value, String domain, String path, boolean secure, long expires) {
        this(name,value,domain,path,secure,expires,true,true);
    }

    /**
     * create the coookie
     * @param name name of the cookie
     * @param value value of the cookie
     * @param domain domain for which the cookie is valid
     * @param path path for which the cookie is valid
     * @param secure flag whether cookie is to be handled as 'secure'
     * @param expires - this is in seconds
     * @param hasPath - was the path explicitly specified?
     * @param hasDomain - was the domain explicitly specified?
     *
     */
    public Cookie(String name, String value, String domain, String path,
            boolean secure, long expires, boolean hasPath, boolean hasDomain) {
        this(name, value, domain, path, secure, expires, hasPath, hasDomain, DEFAULT_VERSION);
    }

    /**
     * Create a JMeter Cookie.
     *
     * @param name name of the cookie
     * @param value value of the cookie
     * @param domain domain for which the cookie is valid
     * @param path path for which the cookie is valid
     * @param secure flag whether cookie is to be handled as 'secure'
     * @param expires - this is in seconds
     * @param hasPath - was the path explicitly specified?
     * @param hasDomain - was the domain explicitly specified?
     * @param version - cookie spec. version
     */
    public Cookie(String name, String value, String domain, String path,
            boolean secure, long expires, boolean hasPath, boolean hasDomain, int version) {
        this.setName(name);
        this.setValue(value);
        this.setDomain(domain);
        this.setPath(path);
        this.setSecure(secure);
        this.setExpires(expires);
        this.setPathSpecified(hasPath);
        this.setDomainSpecified(hasDomain);
        this.setVersion(version);
    }

    public void addConfigElement(ConfigElement config) {
    }

    /**
     * get the value for this object.
     *
     * @return the value of this cookie
     */
    public String getValue() {
        return getPropertyAsString(VALUE);
    }

    /**
     * set the value for this object.
     *
     * @param value the value of this cookie
     */
    public void setValue(String value) {
        this.setProperty(VALUE, value);
    }

    /**
     * get the domain for this object.
     *
     * @return the domain for which this cookie is valid
     */
    public String getDomain() {
        return getPropertyAsString(DOMAIN);
    }

    /**
     * set the domain for this object.
     *
     * @param domain the domain for which this cookie is valid
     */
    public void setDomain(String domain) {
        setProperty(DOMAIN, domain);
    }

    /**
     * get the expiry time for the cookie
     *
     * @return Expiry time in seconds since the Java epoch
     */
    public long getExpires() {
        return getPropertyAsLong(EXPIRES);
    }

    /**
     * get the expiry time for the cookie
     *
     * @return Expiry time in milli-seconds since the Java epoch,
     * i.e. same as System.currentTimeMillis()
     */
    public long getExpiresMillis() {
        return getPropertyAsLong(EXPIRES)*1000;
    }

    /**
     * set the expiry time for the cookie
     * @param expires - expiry time in seconds since the Java epoch
     */
    public void setExpires(long expires) {
        setProperty(new LongProperty(EXPIRES, expires));
    }

    /**
     * get the secure for this object.
     *
     * @return flag whether this cookie should be treated as a 'secure' cookie
     */
    public boolean getSecure() {
        return getPropertyAsBoolean(SECURE);
    }

    /**
     * set the secure for this object.
     *
     * @param secure flag whether this cookie should be treated as a 'secure' cookie
     */
    public void setSecure(boolean secure) {
        setProperty(new BooleanProperty(SECURE, secure));
    }

    /**
     * get the path for this object.
     *
     * @return the path for which this cookie is valid
     */
    public String getPath() {
        return getPropertyAsString(PATH);
    }

    /**
     * set the path for this object.
     *
     * @param path the path for which this cookie is valid
     */
    public void setPath(String path) {
        setProperty(PATH, path);
    }

    public void setPathSpecified(boolean b) {
        setProperty(PATH_SPECIFIED, b);
    }

    public boolean isPathSpecified(){
        return getPropertyAsBoolean(PATH_SPECIFIED);
    }

    public void setDomainSpecified(boolean b) {
        setProperty(DOMAIN_SPECIFIED, b);
    }

    public boolean isDomainSpecified(){
        return getPropertyAsBoolean(DOMAIN_SPECIFIED);
    }

    /**
     * creates a string representation of this cookie
     */
    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder(80);
        sb.append(getDomain());
        // flag - if all machines within a given domain can access the variable.
        //(from http://www.cookiecentral.com/faq/ 3.5)
        sb.append(TAB).append("TRUE");
        sb.append(TAB).append(getPath());
        sb.append(TAB).append(JOrphanUtils.booleanToSTRING(getSecure()));
        sb.append(TAB).append(getExpires());
        sb.append(TAB).append(getName());
        sb.append(TAB).append(getValue());
        return sb.toString();
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return getPropertyAsInt(VERSION, DEFAULT_VERSION);
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        setProperty(VERSION, version, DEFAULT_VERSION);
    }


}
