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

package org.apache.jmeter.util;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;

import javax.swing.JOptionPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.keystore.JmeterKeyStore;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The SSLManager handles the KeyStore information for JMeter.  Basically, it
 * handles all the logic for loading and initializing all the JSSE parameters
 * and selecting the alias to authenticate against if it is available.
 * SSLManager will try to automatically select the client certificate for you,
 * but if it can't make a decision, it will pop open a dialog asking you for
 * more information.
 *
 * @author <a href="bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision$ $Date$
 */
public abstract class SSLManager
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    /** Singleton instance of the manager */
    private static SSLManager manager;
    private static boolean isIAIKProvider = false;
    private static boolean isSSLSupported = false;
    private static Provider sslProvider = null;

    /** Cache the KeyStore instance */
    private JmeterKeyStore keyStore;
    /** Cache the TrustStore instance */
    private KeyStore trustStore;
    /** Have the password available */
    protected String defaultpw =
        JMeterUtils.getJMeterProperties().getProperty(
            "javax.net.ssl.keyStorePassword");

    /**
     * Resets the SSLManager so that we can create a new one with a new keystore
     */
    static public void reset()
    {
        SSLManager.manager = null;
    }

    public abstract void setContext(HttpURLConnection conn);

    /**
     * Default implementation of setting the Provider
     */
    protected void setProvider(Provider provider)
    {
        if ( null != provider )
        {
            Security.addProvider( provider );
        }
    }


    /**
     * Opens and initializes the KeyStore.  If the password for the KeyStore is
     * not set, this method will prompt you to enter it.  Unfortunately, there
     * is no PasswordEntryField available from JOptionPane.
     */
    protected JmeterKeyStore getKeyStore()
    {
        String password = this.defaultpw;

        if (null == this.keyStore)
        {
            String defaultName =
                JMeterUtils.getJMeterProperties().getProperty("user.home")
                    + File.separator
                    + ".keystore";
            String fileName =
                JMeterUtils.getJMeterProperties().getProperty(
                    "javax.net.ssl.keyStore",
                    defaultName);
            System.setProperty("javax.net.ssl.keyStore", fileName);

            try
            {
                if (fileName.endsWith(".p12") || fileName.endsWith(".P12"))
                {
                    this.keyStore = JmeterKeyStore.getInstance("pkcs12");
                    log.info("KeyStore Type: PKCS 12");
                    System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
                }
                else
                {
                    this.keyStore = JmeterKeyStore.getInstance("JKS");
                    log.info("KeyStore Type: JKS");
                }
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(
                    GuiPackage.getInstance().getMainFrame(),
                    e,
                    JMeterUtils.getResString("ssl_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                this.keyStore = null;
                throw new RuntimeException("KeyStore Problem");
            }

            if (null == password)
            {
                if (null == defaultpw)
                {
                    this.defaultpw =
                        JMeterUtils.getJMeterProperties().getProperty(
                            "javax.net.ssl.keyStorePassword");

                    if (null == defaultpw)
                    {
                        synchronized (this)
                        {
                            this.defaultpw =
                                JOptionPane.showInputDialog(
                                    GuiPackage.getInstance().getMainFrame(),
                                    JMeterUtils.getResString("ssl_pass_prompt"),
                                    JMeterUtils.getResString("ssl_pass_title"),
                                    JOptionPane.QUESTION_MESSAGE);
                            JMeterUtils.getJMeterProperties().setProperty(
                                "javax.net.ssl.keyStorePassword",
                                this.defaultpw);
                        }
                    }
                }

                password = this.defaultpw;
                System.setProperty("javax.net.ssl.keyStorePassword", password);
            }

            try
            {
                File initStore = new File(fileName);

                if (initStore.exists())
                {
                    this.keyStore.load(new FileInputStream(initStore),password);
                }
                else
                {
                    this.keyStore.load(null, password);
                }
            }
            catch (Exception e)
            {
                log.error("Couldn't load keystore" ,e);
            }

        log.info("JmeterKeyStore Location: " + fileName);
        log.info("JmeterKeyStore type: " + this.keyStore.getClass().toString());
        }

        return this.keyStore;
    }

    /**
     * Opens and initializes the TrustStore.
     */
    protected KeyStore getTrustStore()
    {
        if (null == this.trustStore)
        {
            String fileName =
                JMeterUtils.getPropDefault("javax.net.ssl.trustStore", "");
            System.setProperty("javax.net.ssl.trustStore", fileName);

            try
            {
                if (fileName.endsWith(".iaik"))
                {
                    this.trustStore =
                        KeyStore.getInstance("IAIKKeyStore", "IAIK");
                }
                else
                {
                    this.trustStore = KeyStore.getInstance("JKS");
                    log.info("KeyStore Type: JKS");
                }
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(
                    GuiPackage.getInstance().getMainFrame(),
                    e,
                    JMeterUtils.getResString("ssl_error_title"),
                    JOptionPane.ERROR_MESSAGE);
                this.trustStore = null;
                throw new RuntimeException("TrustStore Problem");
            }

            try
            {
                File initStore = new File(fileName);

                if (initStore.exists())
                {
                    this.trustStore.load(new FileInputStream(initStore), null);
                }
                else
                {
                    this.trustStore.load(null, null);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(
                    "Can't load TrustStore: " + e.toString());
            }

            log.info("TrustStore Location: " + fileName);
            log.info("TrustStore type: " + this.keyStore.getClass().toString());
        }

        return this.trustStore;
    }

    /**
     * Protected Constructor to remove the possibility of directly instantiating
     * this object.  Create the SSLContext, and wrap all the X509KeyManagers
     * with our X509KeyManager so that we can choose our alias.
     */
    protected SSLManager() {}

    /**
     * Static accessor for the SSLManager object.  The SSLManager is a
     * singleton.
     */
    public static final SSLManager getInstance()
    {
        if (null == SSLManager.manager)
        {
            if (SSLManager.isSSLSupported)
            {
                String classname = null;
                if (SSLManager.isIAIKProvider)
                {
                    classname = "org.apache.jmeter.util.IaikSSLManager";
                }
                else
                {
                    classname = "org.apache.jmeter.util.JsseSSLManager";
                }

                try
                {
                    Class clazz = Class.forName(classname);
                    Constructor con =
                        clazz.getConstructor(new Class[] { Provider.class });
                    SSLManager.manager =
                        (SSLManager) con.newInstance(
                            new Object[] { SSLManager.sslProvider });
                }
                catch (Exception e)
                {
                    log.error("",e);
                    SSLManager.isSSLSupported = false;
                    return null;
                }
            }
        }

        return SSLManager.manager;
    }

    /**
     * Test wether SSL is supported or not.
     */
    public static final boolean isSSLSupported()
    {
        return SSLManager.isSSLSupported;
    }

    // Moved from SSLStaticProvider so all SSL specific management is done in
    // one place.
    static
    {
        SSLManager.isSSLSupported = true;
        SSLManager.sslProvider = null;
/*
        try {
            // Class.forName() was choking if the property wasn't set on the
            // line below..
            String strSSLProvider =
                JMeterUtils.getPropDefault("ssl.provider", null);
            if (strSSLProvider != null)
            {
                SSLManager.sslProvider =
                    (Provider) Class.forName(strSSLProvider).newInstance();
                SSLManager.isSSLSupported = true;
            }
        } catch (Exception noSSL) {
            log.error("",noSSL);
        }

        try {
            if(SSLManager.sslProvider != null) {
                log.info("SSL Provider is: " + SSLManager.sslProvider);
                Security.addProvider(SSLManager.sslProvider);
                // register jsse provider
            }
        } catch (Exception ssl) {
            // ignore
        }

        String protocol =
            JMeterUtils.getPropDefault(
                "ssl.pkgs",
                JMeterUtils.getPropDefault("java.protocol.handler.pkgs", null));
        SSLManager.sslProvider = null;
        // register https protocol handler.  JSSE needs a provider--but iSaSiLk
        // does not.
        if (null != protocol) {
            System.setProperty("java.protocol.handler.pkgs", protocol);
            if ("iaik.protocol".equals(protocol)) {
                SSLManager.isSSLSupported = true;
                SSLManager.isIAIKProvider = true;
            } else {
                // This is the case where a provider is set and
                // java.protocol.handler.pkgs is set
                SSLManager.isSSLSupported = true;
            }
        } else {
            SSLManager.isSSLSupported = true;
        }*/
    }
}
