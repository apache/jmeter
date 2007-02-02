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
import java.lang.reflect.Constructor;
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
 * @author <a href="bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision$ $Date$
 */
public abstract class SSLManager {
	private static final Logger log = LoggingManager.getLoggerForClass();

	private static final String SSL_TRUST_STORE = "javax.net.ssl.trustStore";// $NON-NLS-1$

	private static final String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword"; // $NON-NLS-1$

	public static final String JAVAX_NET_SSL_KEY_STORE = "javax.net.ssl.keyStore"; // $NON-NLS-1$

	private static final String PKCS12 = "pkcs12"; // $NON-NLS-1$

	/** Singleton instance of the manager */
	private static SSLManager manager;

	private static boolean isSSLSupported = true;

	private static Provider sslProvider = null;

	/** Cache the KeyStore instance */
	private JmeterKeyStore keyStore;

	/** Cache the TrustStore instance */
	private KeyStore trustStore;

	/** Have the password available */
	protected String defaultpw = JMeterUtils.getJMeterProperties()
	    .getProperty(KEY_STORE_PASSWORD); // $NON-NLS-1$

	/**
	 * Resets the SSLManager so that we can create a new one with a new keystore
	 */
	static public void reset() {
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
		String password = this.defaultpw;

		if (null == this.keyStore) {
			String defaultName = JMeterUtils.getJMeterProperties()
			    .getProperty("user.home")  // $NON-NLS-1$
			    + File.separator
				+ ".keystore"; // $NON-NLS-1$
			String fileName = JMeterUtils.getJMeterProperties()
			.getProperty(JAVAX_NET_SSL_KEY_STORE, defaultName);
			System.setProperty(JAVAX_NET_SSL_KEY_STORE, fileName); // $NON-NLS-1$

			try {
				if (fileName.endsWith(".p12") || fileName.endsWith(".P12")) { // $NON-NLS-1$ // $NON-NLS-2$
					this.keyStore = JmeterKeyStore.getInstance(PKCS12);
					log.info("KeyStore Type: PKCS 12");
					System.setProperty("javax.net.ssl.keyStoreType", PKCS12); // $NON-NLS-1$
				} else {
					this.keyStore = JmeterKeyStore.getInstance("JKS"); // $NON-NLS-1$
					log.info("KeyStore Type: JKS");
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(), e, JMeterUtils
						.getResString("ssl_error_title"), JOptionPane.ERROR_MESSAGE); // $NON-NLS-1$
				this.keyStore = null;
				throw new RuntimeException("KeyStore Problem");
			}

			if (null == password) {
				if (null == defaultpw) {
					this.defaultpw = JMeterUtils.getJMeterProperties().getProperty(KEY_STORE_PASSWORD);

					if (null == defaultpw) {
						synchronized (this) {
							this.defaultpw = JOptionPane.showInputDialog(GuiPackage.getInstance().getMainFrame(),
									JMeterUtils.getResString("ssl_pass_prompt"),  // $NON-NLS-1$
									JMeterUtils.getResString("ssl_pass_title"),  // $NON-NLS-1$
									JOptionPane.QUESTION_MESSAGE);
							JMeterUtils.getJMeterProperties().setProperty(KEY_STORE_PASSWORD,
									this.defaultpw);
						}
					}
				}

				password = this.defaultpw;
				System.setProperty(KEY_STORE_PASSWORD, password);
			}

            FileInputStream fileInputStream = null;
			try {
				File initStore = new File(fileName);

				if (initStore.exists()) {
					fileInputStream = new FileInputStream(initStore);
                    this.keyStore.load(fileInputStream, password);
				} else {
					log.warn("Keystore not found, creating empty keystore");
					this.keyStore.load(null, password);
				}
			} catch (Exception e) {
				log.warn("Problem loading keystore: " +e.getMessage()); // Does not seem to matter much
			} finally {
                JOrphanUtils.closeQuietly(fileInputStream);
            }

			log.info("JmeterKeyStore Location: " + fileName);
			log.info("JmeterKeyStore type: " + this.keyStore.getClass().toString());
		}

		return this.keyStore;
	}

	/**
	 * Opens and initializes the TrustStore.
	 */
	protected KeyStore getTrustStore() {
		if (null == this.trustStore) {
			String fileName = JMeterUtils.getPropDefault(SSL_TRUST_STORE, "");
			System.setProperty(SSL_TRUST_STORE, fileName);

			try {
				this.trustStore = KeyStore.getInstance("JKS");
				log.info("TrustStore Type: JKS");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(), e, JMeterUtils
						.getResString("ssl_error_title"),  // $NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
				this.trustStore = null;
				throw new RuntimeException("TrustStore Problem");
			}

            FileInputStream fileInputStream = null;
			try {
				File initStore = new File(fileName);

				if (initStore.exists()) {
					fileInputStream = new FileInputStream(initStore);
                    this.trustStore.load(fileInputStream, null);
				} else {
					this.trustStore.load(null, null);
				}
			} catch (Exception e) {
				throw new RuntimeException("Can't load TrustStore: " + e.toString());
            } finally {
                JOrphanUtils.closeQuietly(fileInputStream);
			}

			log.info("TrustStore Location: " + fileName);
			log.info("TrustStore type: " + this.keyStore.getClass().toString());
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
	public static final SSLManager getInstance() {
		if (null == SSLManager.manager) {
			if (SSLManager.isSSLSupported) {
				String classname = null;
				classname = "org.apache.jmeter.util.JsseSSLManager"; // $NON-NLS-1$

				try {
					Class clazz = Class.forName(classname);
					Constructor con = clazz.getConstructor(new Class[] { Provider.class });
					SSLManager.manager = (SSLManager) con.newInstance(new Object[] { SSLManager.sslProvider });
				} catch (Exception e) {
					log.error("", e); // $NON-NLS-1$
					SSLManager.isSSLSupported = false;
					return null;
				}
			}
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
