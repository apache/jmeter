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

import java.net.HttpURLConnection;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.jmeter.util.keystore.JmeterKeyStore;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The SSLManager handles the KeyStore information for JMeter. Basically, it
 * handles all the logic for loading and initializing all the JSSE parameters
 * and selecting the alias to authenticate against if it is available.
 * SSLManager will try to automatically select the client certificate for you,
 * but if it can't make a decision, it will pop open a dialog asking you for
 * more information.
 *
 * TODO: does not actually prompt
 *
 */
public class JsseSSLManager extends SSLManager {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String HTTPS = "https"; // $NON-NLS-1$

    // Temporary fix to allow default protocol to be changed
    private static final String DEFAULT_SSL_PROTOCOL =
        JMeterUtils.getPropDefault("https.default.protocol","TLS"); // $NON-NLS-1$ // $NON-NLS-2$

    // Allow reversion to original shared session context
    private static final boolean SHARED_SESSION_CONTEXT =
        JMeterUtils.getPropDefault("https.sessioncontext.shared",false); // $NON-NLS-1$

    private static final int cps;

    static {
        log.info("Using default SSL protocol: "+DEFAULT_SSL_PROTOCOL);
        log.info("SSL session context: "+(SHARED_SESSION_CONTEXT ? "shared" : "per-thread"));
        cps = JMeterUtils.getPropDefault("httpclient.socket.https.cps", 0); // $NON-NLS-1$

        if (cps > 0) {
            log.info("Setting up HTTPS SlowProtocol, cps="+cps);
        }

    }

    /**
     * Cache the SecureRandom instance because it takes a long time to create
     */
    private SecureRandom rand;

    private Provider pro = null;

    private SSLContext defaultContext; // If we are using a single session
    private ThreadLocal<SSLContext> threadlocal; // Otherwise

    /**
     * Create the SSLContext, and wrap all the X509KeyManagers with
     * our X509KeyManager so that we can choose our alias.
     *
     * @param provider
     *            Description of Parameter
     */
    public JsseSSLManager(Provider provider) {
        log.debug("ssl Provider =  " + provider);
        setProvider(provider);
        if (null == this.rand) { // Surely this is always null in the constructor?
            this.rand = new SecureRandom();
        }
        try {
            if (SHARED_SESSION_CONTEXT) {
                log.debug("Creating shared context");
                this.defaultContext = createContext();
            } else {
                this.threadlocal = new ThreadLocal<SSLContext>();
            }

            /*
             * Set up Java defaults.
             * N.B. does not allow SlowSocket - fails with:
             * java.lang.RuntimeException: Export restriction: this JSSE implementation is non-pluggable.
             */

            HttpsURLConnection.setDefaultSSLSocketFactory(new HttpSSLProtocolSocketFactory(this));
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            /*
             * Also set up HttpClient defaults
             */
            Protocol protocol = new Protocol(
                    JsseSSLManager.HTTPS,
                    (ProtocolSocketFactory) new HttpSSLProtocolSocketFactory(this, cps),
                    443);
            Protocol.registerProtocol(JsseSSLManager.HTTPS, protocol);
            log.debug("SSL stuff all set");
        } catch (GeneralSecurityException ex) {
            log.error("Could not set up SSLContext", ex);
        }
        log.debug("JsseSSLManager installed");
    }

    /**
     * Sets the Context attribute of the JsseSSLManager object
     *
     * @param conn
     *            The new Context value
     */
    @Override
    public void setContext(HttpURLConnection conn) {
        if (conn instanceof HttpsURLConnection) {
/*
 * No point doing this on a per-connection basis, as there is currently no way to configure it.
 * So we leave it to the defaults set up in the SSL Context
 *
 */
//          HttpsURLConnection secureConn = (HttpsURLConnection) conn;
//          secureConn.setSSLSocketFactory(this.getContext().getSocketFactory());
        } else {
            log.warn("Unexpected HttpURLConnection class: "+conn.getClass().getName());
        }
    }

    /**
     * Sets the Provider attribute of the JsseSSLManager object
     *
     * @param p
     *            The new Provider value
     */
    @Override
    protected final void setProvider(Provider p) {
        super.setProvider(p);
        if (null == this.pro) {
            this.pro = p;
        }
    }

    /**
     * Returns the SSLContext we are using.
     * This is either a context per thread,
     * or, for backwards compatibility, a single shared context.
     *
     * @return The Context value
     */
    public SSLContext getContext() throws GeneralSecurityException {
        if (SHARED_SESSION_CONTEXT) {
            if (log.isDebugEnabled()){
                log.debug("Using shared SSL context for: "+Thread.currentThread().getName());
            }
            return this.defaultContext;
        }

        SSLContext sslContext = this.threadlocal.get();
        if (sslContext == null) {
            if (log.isDebugEnabled()){
                log.debug("Creating threadLocal SSL context for: "+Thread.currentThread().getName());
            }
            sslContext = createContext();
            this.threadlocal.set(sslContext);
        }
        if (log.isDebugEnabled()){
            log.debug("Using threadLocal SSL context for: "+Thread.currentThread().getName());
        }
        return sslContext;
    }

    /**
     * Resets the SSLContext if using per-thread contexts.
     *
     */
    public void resetContext() {
        if (!SHARED_SESSION_CONTEXT) {
            log.debug("Clearing session context for current thread");
            this.threadlocal.set(null);
        }
    }
    /*
     *
     * Creates new SSL context
     * @return SSL context
     * @throws GeneralSecurityException
     */
    private SSLContext createContext() throws GeneralSecurityException {
        SSLContext context;
        if (pro != null) {
            context = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL, pro); // $NON-NLS-1$
        } else {
            context = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL); // $NON-NLS-1$
        }
        KeyManagerFactory managerFactory =
            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        JmeterKeyStore keys = this.getKeyStore();
        managerFactory.init(null, defaultpw == null ? new char[]{} : defaultpw.toCharArray());
        KeyManager[] managers = managerFactory.getKeyManagers();
        log.debug(keys.getClass().toString());

        // Now wrap the default managers with our key manager
        for (int i = 0; i < managers.length; i++) {
            if (managers[i] instanceof X509KeyManager) {
                X509KeyManager manager = (X509KeyManager) managers[i];
                managers[i] = new WrappedX509KeyManager(manager, keys);
            }
        }

        // Get the default trust managers
        TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmfactory.init(this.getTrustStore());

        // Wrap the defaults in our custom trust manager
        TrustManager[] trustmanagers = tmfactory.getTrustManagers();
        for (int i = 0; i < trustmanagers.length; i++) {
            if (trustmanagers[i] instanceof X509TrustManager) {
                trustmanagers[i] = new CustomX509TrustManager(
                    (X509TrustManager)trustmanagers[i]);
            }
        }
        context.init(managers, trustmanagers, this.rand);
        if (log.isDebugEnabled()){
            String[] dCiphers = context.getSocketFactory().getDefaultCipherSuites();
            String[] sCiphers = context.getSocketFactory().getSupportedCipherSuites();
            int len = (dCiphers.length > sCiphers.length) ? dCiphers.length : sCiphers.length;
            for (int i = 0; i < len; i++) {
                if (i < dCiphers.length) {
                    log.debug("Default Cipher: " + dCiphers[i]);
                }
                if (i < sCiphers.length) {
                    log.debug("Supported Cipher: " + sCiphers[i]);
                }
            }
        }
        return context;
    }

    /**
     * This is the X509KeyManager we have defined for the sole purpose of
     * selecting the proper key and certificate based on the keystore available.
     *
     */
    private static class WrappedX509KeyManager implements X509KeyManager {
        /**
         * The parent X509KeyManager
         */
        private final X509KeyManager manager;

        /**
         * The KeyStore this KeyManager uses
         */
        private final JmeterKeyStore store;

        /**
         * Instantiate a new WrappedX509KeyManager.
         *
         * @param parent
         *            The parent X509KeyManager
         * @param ks
         *            The KeyStore we derive our client certs and keys from
         */
        public WrappedX509KeyManager(X509KeyManager parent, JmeterKeyStore ks) {
            this.manager = parent;
            this.store = ks;
        }

        /**
         * Compiles the list of all client aliases with a private key.
         * Currently, keyType and issuers are both ignored.
         *
         * @param keyType
         *            the type of private key the server expects (RSA, DSA,
         *            etc.)
         * @param issuers
         *            the CA certificates we are narrowing our selection on.
         * @return the ClientAliases value
         */
        public String[] getClientAliases(String keyType, Principal[] issuers) {
            log.debug("WrappedX509Manager: getClientAliases: ");
            log.debug(this.store.getAlias());
            return new String[] { this.store.getAlias() };
        }

        /**
         * Get the list of server aliases for the SSLServerSockets. This is not
         * used in JMeter.
         *
         * @param keyType
         *            the type of private key the server expects (RSA, DSA,
         *            etc.)
         * @param issuers
         *            the CA certificates we are narrowing our selection on.
         * @return the ServerAliases value
         */
        public String[] getServerAliases(String keyType, Principal[] issuers) {
            log.debug("WrappedX509Manager: getServerAliases: ");
            return this.manager.getServerAliases(keyType, issuers);
        }

        /**
         * Get the Certificate chain for a particular alias
         *
         * @param alias
         *            The client alias
         * @return The CertificateChain value
         */
        public X509Certificate[] getCertificateChain(String alias) {
            log.debug("WrappedX509Manager: getCertificateChain(" + alias + ")");
            return this.store.getCertificateChain();
        }

        /**
         * Get the Private Key for a particular alias
         *
         * @param alias
         *            The client alias
         * @return The PrivateKey value
         */
        public PrivateKey getPrivateKey(String alias) {
            log.debug("WrappedX509Manager: getPrivateKey: " + this.store.getPrivateKey());
            return this.store.getPrivateKey();
        }

        /**
         * Select the Alias we will authenticate as if Client authentication is
         * required by the server we are connecting to. We get the list of
         * aliases, and if there is only one alias we automatically select it.
         * If there are more than one alias that has a private key, we prompt
         * the user to choose which alias using a combo box. Otherwise, we
         * simply provide a text box, which may or may not work. The alias does
         * have to match one in the keystore.
         *
         * TODO? - does not actually allow the user to choose an alias at present
         *
         * @see javax.net.ssl.X509KeyManager#chooseClientAlias(String[], Principal[], Socket)
         */
        public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
            String alias = this.store.getAlias();
            log.debug("ClientAlias: " + alias);
            if (alias == null || alias.length() == 0) {
                log.debug("ClientAlias not found.");
            }
            return alias;
        }

        /**
         * Choose the server alias for the SSLServerSockets. This are not used
         * in JMeter.
         *
         * @see javax.net.ssl.X509KeyManager#chooseServerAlias(String, Principal[], Socket)
         */
        public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
            return this.manager.chooseServerAlias(arg0, arg1, arg2);
        }
    }
}
