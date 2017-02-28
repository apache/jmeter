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

package org.apache.jmeter.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.util.Locale;

import javax.swing.JOptionPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.keystore.JmeterKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SSLManager handles the KeyStore information for JMeter. Basically, it
 * handles all the logic for loading and initializing all the JSSE parameters
 * and selecting the alias to authenticate against if it is available.
 * SSLManager will try to automatically select the client certificate for you,
 * but if it can't make a decision, it will pop open a dialog asking you for
 * more information.
 *
 * TODO? - N.B. does not currently allow the selection of a client certificate.
 *
 */
public abstract class SSLManager {
    private static final Logger log = LoggerFactory.getLogger(SSLManager.class);

    private static final String SSL_TRUST_STORE = "javax.net.ssl.trustStore";// $NON-NLS-1$

    private static final String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword"; // $NON-NLS-1$ NOSONAR no hard coded password

    public static final String JAVAX_NET_SSL_KEY_STORE = "javax.net.ssl.keyStore"; // $NON-NLS-1$

    private static final String JAVAX_NET_SSL_KEY_STORE_TYPE = "javax.net.ssl.keyStoreType"; // $NON-NLS-1$

    private static final String PKCS12 = "pkcs12"; // $NON-NLS-1$

    /** Singleton instance of the manager */
    //@GuardedBy("this")
    private static SSLManager manager;

    private static final boolean IS_SSL_SUPPORTED = true;

    /** Cache the KeyStore instance */
    private volatile JmeterKeyStore keyStore;

    /** Cache the TrustStore instance - null if no truststore name was provided */
    private KeyStore trustStore = null;
    // Have we yet tried to load the truststore?
    private volatile boolean truststoreLoaded=false;

    /** Have the password available */
    protected String defaultpw = System.getProperty(KEY_STORE_PASSWORD);

    private int keystoreAliasStartIndex;

    private int keystoreAliasEndIndex;

    private String clientCertAliasVarName;

    /**
     * Resets the SSLManager so that we can create a new one with a new keystore
     */
    public static synchronized void reset() {
        SSLManager.manager = null;
    }

    public abstract void setContext(HttpURLConnection conn);

    /**
     * Default implementation of setting the Provider
     *
     * @param provider
     *            the provider to use
     */
    protected void setProvider(Provider provider) {
        if (null != provider) {
            Security.addProvider(provider);
        }
    }
    
    /**
     * Opens and initializes the KeyStore. If the password for the KeyStore is
     * not set, this method will prompt you to enter it. Unfortunately, there is
     * no PasswordEntryField available from JOptionPane.
     *
     * @return the configured {@link JmeterKeyStore}
     */
    protected synchronized JmeterKeyStore getKeyStore() {
        if (null == this.keyStore) {
            String fileName = System.getProperty(JAVAX_NET_SSL_KEY_STORE,""); // empty if not provided
            String fileType = System.getProperty(JAVAX_NET_SSL_KEY_STORE_TYPE, // use the system property to determine the type
                    fileName.toLowerCase(Locale.ENGLISH).endsWith(".p12") ? PKCS12 : "JKS"); // otherwise use the name
            log.info("JmeterKeyStore Location: {} type {}", fileName, fileType);
            try {
                this.keyStore = JmeterKeyStore.getInstance(fileType, keystoreAliasStartIndex, keystoreAliasEndIndex, clientCertAliasVarName);
                log.info("KeyStore created OK");
            } catch (Exception e) {
                this.keyStore = null;
                throw new RuntimeException("Could not create keystore: "+e.getMessage(), e);
            }

            try {
                File initStore = new File(fileName);

                if (fileName.length() >0 && initStore.exists()) {
                    try (InputStream fis = new FileInputStream(initStore);
                            InputStream fileInputStream = new BufferedInputStream(fis)) {
                        this.keyStore.load(fileInputStream, getPassword());
                        if (log.isInfoEnabled()) {
                            log.info(
                                    "Total of {} aliases loaded OK from keystore",
                                    keyStore.getAliasCount());
                        }
                    }
                } else {
                    log.warn("Keystore file not found, loading empty keystore");
                    this.defaultpw = ""; // Ensure not null
                    this.keyStore.load(null, "");
                }
            } catch (Exception e) {
                log.error("Problem loading keystore: {}", e.getMessage(), e);
            }

            if (log.isDebugEnabled()) {
                log.debug("JmeterKeyStore type: {}", this.keyStore.getClass());
            }
        }

        return this.keyStore;
    }

    /*
     * The password can be defined as a property; this dialogue is provided to allow it
     * to be entered at run-time.
     *
     * However, this does not gain much, as the dialogue does not (yet) support hidden input ...
     *
    */
    private String getPassword() {
        String password = this.defaultpw;
        if (null == password) {
            final GuiPackage guiInstance = GuiPackage.getInstance();
            if (guiInstance != null) {
                synchronized (this) { // TODO is sync really needed?
                    this.defaultpw = JOptionPane.showInputDialog(
                            guiInstance.getMainFrame(),
                            JMeterUtils.getResString("ssl_pass_prompt"),  // $NON-NLS-1$
                            JMeterUtils.getResString("ssl_pass_title"),  // $NON-NLS-1$
                            JOptionPane.QUESTION_MESSAGE);
                    System.setProperty(KEY_STORE_PASSWORD, this.defaultpw);
                    password = this.defaultpw;
                }
            } else {
                log.warn("No password provided, and no GUI present so cannot prompt");
            }
        }
        return password;
    }

    /**
     * Opens and initializes the TrustStore.
     *
     * There are 3 possibilities:
     * - no truststore name provided, in which case the default Java truststore should be used
     * - truststore name is provided, and loads OK
     * - truststore name is provided, but is not found or does not load OK, in which case an empty
     * truststore is created
     *
     * If the KeyStore object cannot be created, then this is currently treated the same
     * as if no truststore name was provided.
     *
     * @return truststore
     * - null: use Java truststore
     * - otherwise, the truststore, which may be empty if the file could not be loaded.
     *
     */
    protected KeyStore getTrustStore() {
        if (!truststoreLoaded) {

            truststoreLoaded=true;// we've tried ...

            String fileName = System.getProperty(SSL_TRUST_STORE);
            if (fileName == null) {
                return null;
            }
            log.info("TrustStore Location: " + fileName);

            try {
                this.trustStore = KeyStore.getInstance("JKS");
                log.info("TrustStore created OK, Type: JKS");
            } catch (Exception e) {
                this.trustStore = null;
                throw new RuntimeException("Problem creating truststore: "+e.getMessage(), e);
            }

            try {
                File initStore = new File(fileName);

                if (initStore.exists()) {
                    try (InputStream fis = new FileInputStream(initStore);
                            InputStream fileInputStream = new BufferedInputStream(fis)) {
                        this.trustStore.load(fileInputStream, null);
                        log.info("Truststore loaded OK from file");
                    }
                } else {
                    log.info("Truststore file not found, loading empty truststore");
                    this.trustStore.load(null, null);
                }
            } catch (Exception e) {
                throw new RuntimeException("Can't load TrustStore: " + e.getMessage(), e);
            }
        }

        return this.trustStore;
    }

    /**
     * Protected Constructor to remove the possibility of directly instantiating
     * this object. Create the SSLContext, and wrap all the X509KeyManagers with
     * our X509KeyManager so that we can choose our alias.
     */
    protected SSLManager() {
    }

    /**
     * Static accessor for the SSLManager object. The SSLManager is a singleton.
     *
     * @return the singleton {@link SSLManager}
     */
    public static synchronized SSLManager getInstance() {
        if (null == SSLManager.manager) {
            SSLManager.manager = new JsseSSLManager(null);
        }

        return SSLManager.manager;
    }

    /**
     * Test whether SSL is supported or not.
     *
     * @return flag whether SSL is supported
     */
    public static boolean isSSLSupported() {
        return SSLManager.IS_SSL_SUPPORTED;
    }

    /**
     * Configure Keystore
     * 
     * @param preload
     *            flag whether the keystore should be opened within this method,
     *            or the opening should be delayed
     * @param startIndex
     *            first index to consider for a key
     * @param endIndex
     *            last index to consider for a key
     * @param clientCertAliasVarName
     *            name of the default key, if empty the first key will be used
     *            as default key
     */
    public void configureKeystore(boolean preload, int startIndex, int endIndex, String clientCertAliasVarName) {
        this.keystoreAliasStartIndex = startIndex;
        this.keystoreAliasEndIndex = endIndex;
        this.clientCertAliasVarName = clientCertAliasVarName;
        if(preload) {
            keyStore = getKeyStore();
        }
    }

    /**
     * Destroy Keystore
     */
    public void destroyKeystore() {
        keyStore=null;
    }
}
