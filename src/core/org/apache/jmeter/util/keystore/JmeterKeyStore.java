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

package org.apache.jmeter.util.keystore;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Use this Keystore for JMeter specific KeyStores.
 * 
 * @author <a href="bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision$ $Date$
 */
public abstract class JmeterKeyStore {

	/**
	 * Process the input stream
	 */
	public abstract void load(InputStream is, String password) throws Exception;

	/**
	 * Get the ordered certificate chain.
	 */
	public abstract X509Certificate[] getCertificateChain();

	public abstract String getAlias();

	/**
	 * Return the private Key
	 */
	public abstract PrivateKey getPrivateKey();

	public static final JmeterKeyStore getInstance(String type) throws Exception {
        
        // PKCS12 is now handled by JSSE (Java 1.4+)
        // The PKCS12KeyStore JMeter class depended on IsAsIlK, and has been removed 
//		if ("PKCS12".equalsIgnoreCase(type)) {
//			try {
//				Class PKCS12 = Class.forName("org.apache.jmeter.util.keystore.PKCS12KeyStore");
//				Constructor con = PKCS12.getConstructor(new Class[] { String.class });
//				return (JmeterKeyStore) con.newInstance(new Object[] { type });
//			} catch (Exception e) {
//			}
//		}

        return new DefaultKeyStore(type);
	}
}