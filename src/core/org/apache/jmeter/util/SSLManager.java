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

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

import javax.swing.JOptionPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.keystore.JmeterKeyStore;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

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
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String SSL_TRUST_STORE = "javax.net.ssl.trustStore";// $NON-NLS-1$

    private static final String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword"; // $NON-NLS-1$

    public static final String JAVAX_NET_SSL_KEY_STORE = "javax.net.ssl.keyStore"; // $NON-NLS-1$

    private static final String PKCS12 = "pkcs12"; // $NON-NLS-1$

    /** Singleton instance of the manager */
    //@GuardedBy("this")
    private static SSLManager manager;

    private static final boolean isSSLSupported = true;

    private static final Provider sslProvider = null;

    /** Cache the KeyStore instance */
    private JmeterKeyStore keyStore;

    /** Cache the TrustStore instance - null if no truststore name was provided */
    private KeyStore trustStore = null;
    // Have we yet tried to load the truststore?
    private volatile boolean truststore_loaded=false;

    /** Have the password available */
    protected String defaultpw = System.getProperty(KEY_STORE_PASSWORD);

    /**
     * Resets the SSLManager so that we can create a new one with a new keystore
     */
    public static synchronized void reset() {
        SSLManager.manager = null;
    }

    public abstract void setContext(HttpURLConnection conn);

    /**
     * Default implementation of setting the Provider
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
     */
    protected JmeterKeyStore getKeyStore() {
        if (null == this.keyStore) {
            String defaultName = JMeterUtils.getJMeterProperties()
                .getProperty("user.home")  // $NON-NLS-1$
                + File.separator
                + ".keystore"; // $NON-NLS-1$
            String fileName = System.getProperty(JAVAX_NET_SSL_KEY_STORE, defaultName);
            log.info("JmeterKeyStore Location: " + fileName);
            try {
                if (fileName.endsWith(".p12") || fileName.endsWith(".P12")) { // $NON-NLS-1$ // $NON-NLS-2$
                    this.keyStore = JmeterKeyStore.getInstance(PKCS12);
                    log.info("KeyStore created OK, Type: PKCS 12");
                    System.setProperty("javax.net.ssl.keyStoreType", PKCS12); // $NON-NLS-1$
                } else {
                    this.keyStore = JmeterKeyStore.getInstance("JKS"); // $NON-NLS-1$
                    log.info("KeyStore created OK, Type: JKS");
                }
            } catch (Exception e) {
                this.keyStore = null;
                throw new RuntimeException("Could not create keystore: "+e.getMessage());
            }

            FileInputStream fileInputStream = null;
            try {
                File initStore = new File(fileName);

                if (initStore.exists()) {
                    fileInputStream = new FileInputStream(initStore);
                    this.keyStore.load(fileInputStream, getPassword());
                    log.info("Keystore loaded OK from file, found alias: "+keyStore.getAlias());
                } else {
                    log.warn("Keystore file not found, loading empty keystore");
                    this.defaultpw = ""; // Ensure not null
                    this.keyStore.load(null, "");
                }
            } catch (Exception e) {
                log.error("Problem loading keystore: " +e.getMessage());
            } finally {
                JOrphanUtils.closeQuietly(fileInputStream);
            }

            log.debug("JmeterKeyStore type: " + this.keyStore.getClass().toString());
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
            this.defaultpw = System.getProperty(KEY_STORE_PASSWORD);

            if (null == defaultpw) {
                synchronized (this) {
                    this.defaultpw = JOptionPane.showInputDialog(
                            GuiPackage.getInstance().getMainFrame(),
                            JMeterUtils.getResString("ssl_pass_prompt"),  // $NON-NLS-1$
                            JMeterUtils.getResString("ssl_pass_title"),  // $NON-NLS-1$
                            JOptionPane.QUESTION_MESSAGE);
                    System.setProperty(KEY_STORE_PASSWORD, this.defaultpw);
                }
            }

            password = this.defaultpw;
            System.setProperty(KEY_STORE_PASSWORD, password);
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
        if (!truststore_loaded) {

            truststore_loaded=true;// we've tried ...

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
                throw new RuntimeException("Problem creating truststore: "+e.getMessage());
            }

            FileInputStream fileInputStream = null;
            try {
                File initStore = new File(fileName);

                if (initStore.exists()) {
                    fileInputStream = new FileInputStream(initStore);
                    this.trustStore.load(fileInputStream, null);
                    log.info("Truststore loaded OK from file");
                } else {
                    log.info("Truststore file not found, loading empty truststore");
                    this.trustStore.load(null, null);
                }
            } catch (Exception e) {
                throw new RuntimeException("Can't load TrustStore: " + e.toString());
            } finally {
                JOrphanUtils.closeQuietly(fileInputStream);
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
     */
    public static final synchronized SSLManager getInstance() {
        if (null == SSLManager.manager) {
            SSLManager.manager = new JsseSSLManager(SSLManager.sslProvider);
//          if (SSLManager.isSSLSupported) {
//              String classname = null;
//              classname = "org.apache.jmeter.util.JsseSSLManager"; // $NON-NLS-1$
//
//              try {
//                  Class clazz = Class.forName(classname);
//                  Constructor con = clazz.getConstructor(new Class[] { Provider.class });
//                  SSLManager.manager = (SSLManager) con.newInstance(new Object[] { SSLManager.sslProvider });
//              } catch (Exception e) {
//                  log.error("Could not create SSLManager instance", e); // $NON-NLS-1$
//                  SSLManager.isSSLSupported = false;
//                  return null;
//              }
//          }
        }

        return SSLManager.manager;
    }

    /**
     * Test whether SSL is supported or not.
     */
    public static final boolean isSSLSupported() {
        return SSLManager.isSSLSupported;
    }
}
