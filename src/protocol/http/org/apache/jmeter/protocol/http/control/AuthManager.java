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
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

// For Unit tests, @see TestAuthManager

/**
 * This class provides a way to provide Authorization in jmeter requests. The
 * format of the authorization file is: URL user pass where URL is an HTTP URL,
 * user a username to use and pass the appropriate password.
 *
 */
public class AuthManager extends ConfigTestElement implements Serializable {
    private static final long serialVersionUID = 233L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final static String AUTH_LIST = "AuthManager.auth_list"; //$NON-NLS-1$

    private final static String[] COLUMN_RESOURCE_NAMES = {
        "auth_base_url", //$NON-NLS-1$
        "username",      //$NON-NLS-1$
        "password",      //$NON-NLS-1$
        "domain",        //$NON-NLS-1$
        "realm",         //$NON-NLS-1$
        };

    // Column numbers - must agree with order above
    public final static int COL_URL = 0;
    public final static int COL_USERNAME = 1;
    public final static int COL_PASSWORD = 2;
    public final static int COL_DOMAIN = 3;
    public final static int COL_REALM = 4;

    private final static int COLUMN_COUNT = COLUMN_RESOURCE_NAMES.length;

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
        setProperty(new CollectionProperty(AUTH_LIST, new ArrayList<Object>()));
    }

    /**
     * Update an authentication record.
     */
    public void set(int index, String url, String user, String pass, String domain, String realm) {
        Authorization auth = new Authorization(url, user, pass, domain, realm);
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
                int port = url.getProtocol().equalsIgnoreCase("http") ? 80 : 443;
                // only http and https are supported
                url2 = new URL(url.getProtocol(), url.getHost(), port, url.getPath());
            } else if ((url.getPort() == 80 && url.getProtocol().equalsIgnoreCase("http"))
                    || (url.getPort() == 443 && url.getProtocol().equalsIgnoreCase("https"))) {
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
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        writer.println("# JMeter generated Authorization file");
        for (int i = 0; i < getAuthObjects().size(); i++) {
            Authorization auth = (Authorization) getAuthObjects().get(i).getObjectValue();
            writer.println(auth.toString());
        }
        writer.flush();
        writer.close();
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
                    if (line.startsWith("#") || line.trim().length() == 0) { //$NON-NLS-1$
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
                    Authorization auth = new Authorization(url, user, pass,domain,realm);
                    getAuthObjects().addItem(auth);
                } catch (NoSuchElementException e) {
                    log.error("Error parsing auth line: '" + line + "'");
                    ok = false;
                }
            }
        } finally {
            IOUtils.closeQuietly(reader);
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
}
