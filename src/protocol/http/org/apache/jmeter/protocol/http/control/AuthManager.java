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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.security.auth.Subject;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

// For Unit tests, @see TestAuthManager

/**
 * This class provides a way to provide Authorization in jmeter requests. The
 * format of the authorization file is: URL user pass where URL is an HTTP URL,
 * user a username to use and pass the appropriate password.
 *
 */
public class AuthManager extends ConfigTestElement implements TestStateListener, TestIterationListener, Serializable {
    private static final long serialVersionUID = 234L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String CLEAR = "AuthManager.clearEachIteration";// $NON-NLS-1$

    private static final String AUTH_LIST = "AuthManager.auth_list"; //$NON-NLS-1$

    private static final String[] COLUMN_RESOURCE_NAMES = {
        "auth_base_url", //$NON-NLS-1$
        "username",      //$NON-NLS-1$
        "password",      //$NON-NLS-1$
        "domain",        //$NON-NLS-1$
        "realm",         //$NON-NLS-1$
        "mechanism",     //$NON-NLS-1$
        };

    // Column numbers - must agree with order above
    public static final int COL_URL = 0;
    public static final int COL_USERNAME = 1;
    public static final int COL_PASSWORD = 2;
    public static final int COL_DOMAIN = 3;
    public static final int COL_REALM = 4;
    public static final int COL_MECHANISM = 5;

    private static final int COLUMN_COUNT = COLUMN_RESOURCE_NAMES.length;

    private static final Credentials USE_JAAS_CREDENTIALS = new NullCredentials();

    private static final boolean DEFAULT_CLEAR_VALUE = false;

    public enum Mechanism {
        BASIC_DIGEST, KERBEROS;
    }

    private static final class NullCredentials implements Credentials {
        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }
    }
    
    private KerberosManager kerberosManager = new KerberosManager();

    /**
     * Default Constructor.
     */
    public AuthManager() {
        setProperty(new CollectionProperty(AUTH_LIST, new ArrayList<Object>()));
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {
        super.clear();
        kerberosManager.clearSubjects();
        setProperty(new CollectionProperty(AUTH_LIST, new ArrayList<Object>()));
    }

    /**
     * Update an authentication record.
     */
    public void set(int index, String url, String user, String pass, String domain, String realm, Mechanism mechanism) {
        Authorization auth = new Authorization(url, user, pass, domain, realm, mechanism);
        if (index >= 0) {
            getAuthObjects().set(index, new TestElementProperty(auth.getName(), auth));
        } else {
            getAuthObjects().addItem(auth);
        }
    }

    public CollectionProperty getAuthObjects() {
        return (CollectionProperty) getProperty(AUTH_LIST);
    }

    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    public String getColumnName(int column) {
        return COLUMN_RESOURCE_NAMES[column];
    }

    public Class<?> getColumnClass(int column) {
        return COLUMN_RESOURCE_NAMES[column].getClass();
    }

    public Authorization getAuthObjectAt(int row) {
        return (Authorization) getAuthObjects().get(row).getObjectValue();
    }

    public boolean isEditable() {
        return true;
    }

    /**
     * Return the record at index i
     */
    public Authorization get(int i) {
        return (Authorization) getAuthObjects().get(i).getObjectValue();
    }

    public String getAuthHeaderForURL(URL url) {
        Authorization auth = getAuthForURL(url);
        if (auth == null) {
            return null;
        }
        return auth.toBasicHeader();
    }

    public Authorization getAuthForURL(URL url) {
        if (!isSupportedProtocol(url)) {
            return null;
        }

        // TODO: replace all this url2 mess with a proper method
        // "areEquivalent(url1, url2)" that
        // would also ignore case in protocol and host names, etc. -- use that
        // method in the CookieManager too.

        URL url2 = null;

        try {
            if (url.getPort() == -1) {
                // Obtain another URL with an explicit port:
                int port = url.getProtocol().equalsIgnoreCase("http") ? HTTPConstants.DEFAULT_HTTP_PORT : HTTPConstants.DEFAULT_HTTPS_PORT;
                // only http and https are supported
                url2 = new URL(url.getProtocol(), url.getHost(), port, url.getPath());
            } else if ((url.getPort() == HTTPConstants.DEFAULT_HTTP_PORT && url.getProtocol().equalsIgnoreCase("http"))
                    || (url.getPort() == HTTPConstants.DEFAULT_HTTPS_PORT && url.getProtocol().equalsIgnoreCase("https"))) {
                url2 = new URL(url.getProtocol(), url.getHost(), url.getPath());
            }
        } catch (MalformedURLException e) {
            log.error("Internal error!", e); // this should never happen
            // anyway, we'll continue with url2 set to null.
        }

        String s1 = url.toString();
        String s2 = null;
        if (url2 != null) {
            s2 = url2.toString();
        }

            log.debug("Target URL strings to match against: "+s1+" and "+s2);
        // TODO should really return most specific (i.e. longest) match.
        for (PropertyIterator iter = getAuthObjects().iterator(); iter.hasNext();) {
            Authorization auth = (Authorization) iter.next().getObjectValue();

            String uRL = auth.getURL();
            log.debug("Checking match against auth'n entry: "+uRL);
            if (s1.startsWith(uRL) || s2 != null && s2.startsWith(uRL)) {
                log.debug("Matched");
                return auth;
            }
            log.debug("Did not match");
        }
        return null;
    }

    /**
     * @return boolean true if an authorization is setup for url
     */
    public boolean hasAuthForURL(URL url) {
        return getAuthForURL(url) != null;
    }
    
    /**
     * @return Subject if Auth Scheme uses Subject and an authorization is setup for url
     */
    public Subject getSubjectForUrl(URL url) {
        Authorization authorization = getAuthForURL(url);
        if (authorization != null && Mechanism.KERBEROS.equals(authorization.getMechanism())) {
            return kerberosManager.getSubjectForUser(
                    authorization.getUser(), authorization.getPass());
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void addConfigElement(ConfigElement config) {
    }

    public void addAuth(Authorization auth) {
        getAuthObjects().addItem(auth);
    }

    public void addAuth() {
        getAuthObjects().addItem(new Authorization());
    }

    /** {@inheritDoc} */
    @Override
    public boolean expectsModification() {
        return false;
    }

    /**
     * Save the authentication data to a file.
     */
    public void save(String authFile) throws IOException {
        File file = new File(authFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir"),authFile);
        }
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(file));
            writer.println("# JMeter generated Authorization file");
            for (int i = 0; i < getAuthObjects().size(); i++) {
                Authorization auth = (Authorization) getAuthObjects().get(i).getObjectValue();
                writer.println(auth.toString());
            }
            writer.flush();
            writer.close();
        } finally {
            JOrphanUtils.closeQuietly(writer);
        }
    }

    /**
     * Add authentication data from a file.
     */
    public void addFile(String authFile) throws IOException {
        File file = new File(authFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir") + File.separator + authFile);
        }
        if (!file.canRead()) {
            throw new IOException("The file you specified cannot be read.");
        }

        BufferedReader reader = null;
        boolean ok = true;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    if (line.startsWith("#") || JOrphanUtils.isBlank(line)) { //$NON-NLS-1$
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(line, "\t"); //$NON-NLS-1$
                    String url = st.nextToken();
                    String user = st.nextToken();
                    String pass = st.nextToken();
                    String domain = "";
                    String realm = "";
                    if (st.hasMoreTokens()){// Allow for old format file without the extra columnns
                        domain = st.nextToken();
                        realm = st.nextToken();
                    }
                    Mechanism mechanism = Mechanism.BASIC_DIGEST;
                    if (st.hasMoreTokens()){// Allow for old format file without mechanism support
                        mechanism = Mechanism.valueOf(st.nextToken());
                    }
                    Authorization auth = new Authorization(url, user, pass, domain, realm, mechanism);
                    getAuthObjects().addItem(auth);
                } catch (NoSuchElementException e) {
                    log.error("Error parsing auth line: '" + line + "'");
                    ok = false;
                }
            }
        } finally {
            JOrphanUtils.closeQuietly(reader);
        }
        if (!ok){
            JMeterUtils.reportErrorToUser("One or more errors found when reading the Auth file - see the log file");
        }
    }

    /**
     * Remove an authentication record.
     */
    public void remove(int index) {
        getAuthObjects().remove(index);
    }

    /**
     *
     * @return true if kerberos auth must be cleared on each mail loop iteration 
     */
    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR, DEFAULT_CLEAR_VALUE);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(CLEAR, clear, DEFAULT_CLEAR_VALUE);
    }

    /**
     * Return the number of records.
     */
    public int getAuthCount() {
        return getAuthObjects().size();
    }

    // Needs to be package protected for Unit test
    static boolean isSupportedProtocol(URL url) {
        String protocol = url.getProtocol().toLowerCase(java.util.Locale.ENGLISH);
        return protocol.equals(HTTPConstants.PROTOCOL_HTTP) || protocol.equals(HTTPConstants.PROTOCOL_HTTPS);
    }    

    /**
     * Configure credentials and auth scheme on client if an authorization is 
     * available for url
     * @param client {@link HttpClient}
     * @param url URL to test 
     * @param credentialsProvider {@link CredentialsProvider}
     * @param localHost host running JMeter
     */
    public void setupCredentials(HttpClient client, URL url,
            CredentialsProvider credentialsProvider, String localHost) {
        Authorization auth = getAuthForURL(url);
        if (auth != null) {
            String username = auth.getUser();
            String realm = auth.getRealm();
            String domain = auth.getDomain();
            if (log.isDebugEnabled()){
                log.debug(username + " > D="+domain+" R="+realm + " M="+auth.getMechanism());
            }
            if (Mechanism.KERBEROS.equals(auth.getMechanism())) {
                ((AbstractHttpClient) client).getAuthSchemes().register(AuthPolicy.SPNEGO, new SPNegoSchemeFactory(true));
                credentialsProvider.setCredentials(new AuthScope(null, -1, null), USE_JAAS_CREDENTIALS);
            } else {
                credentialsProvider.setCredentials(
                        new AuthScope(url.getHost(), url.getPort(), realm.length()==0 ? null : realm),
                        new NTCredentials(username, auth.getPass(), localHost, domain));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void testStarted() {
        kerberosManager.clearSubjects();
    }
    
    /** {@inheritDoc} */
    @Override
    public void testEnded() {
    }

    /** {@inheritDoc} */
    @Override
    public void testStarted(String host) {
        testStarted();
    }

    /** {@inheritDoc} */
    @Override
    public void testEnded(String host) {
    }

    /** {@inheritDoc} */
    @Override
    public void testIterationStart(LoopIterationEvent event) {
        if (getClearEachIteration()) {
            kerberosManager.clearSubjects();
        }
    }
}
