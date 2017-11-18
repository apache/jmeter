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

package org.apache.jmeter.util.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.threads.JMeterContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this Keystore for JMeter specific KeyStores.
 *
 */
public final class JmeterKeyStore {

    private static final Logger log = LoggerFactory.getLogger(JmeterKeyStore.class);

    private final KeyStore store;

    /** first index to consider for a key */
    private final int startIndex;

    /** last index to consider for a key */
    private final int endIndex;

    /** name of the default alias */
    private String clientCertAliasVarName;

    private String[] names = new String[0]; // default empty array to prevent NPEs
    private Map<String, PrivateKey> privateKeyByAlias = new HashMap<>();
    private Map<String, X509Certificate[]> certsByAlias = new HashMap<>();

    private int last_user;

    /**
     * @param type
     *            type of the {@link KeyStore}
     * @param startIndex which keys should be considered starting from <code>0</code>
     * @param endIndex which keys should be considered up to <code>count - 1</code>
     * @param clientCertAliasVarName name for the default key, if empty use the first key available
     * @throws KeyStoreException
     *             when the type of the keystore is not supported
     * @throws IllegalArgumentException
     *             when <code>startIndex</code> &lt; 0, <code>endIndex</code>
     *             &lt; 0 or <code>endIndex</code> &lt; </code>startIndex</code>
     */
    private JmeterKeyStore(String type, int startIndex, int endIndex, String clientCertAliasVarName) throws KeyStoreException {
        if (startIndex < 0 || endIndex < 0 || endIndex < startIndex) {
            throw new IllegalArgumentException("Invalid index(es). Start="+startIndex+", end="+endIndex);
        }
        this.store = KeyStore.getInstance(type);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.clientCertAliasVarName = clientCertAliasVarName;
    }

    /**
     * Process the input stream and try to read the keys from the store
     *
     * @param is
     *            {@link InputStream} from which the store should be loaded
     * @param pword
     *            the password used to check the integrity of the store
     * @throws IOException
     *             if there is a problem decoding or reading the store. A bad
     *             password might be the cause for this, or an empty store
     * @throws CertificateException
     *             if any of the certificated in the store can not be loaded
     * @throws NoSuchAlgorithmException
     *             if the algorithm to check the integrity of the store can not
     *             be found
     * @throws KeyStoreException
     *             if the store has not been initialized (should not happen
     *             here)
     * @throws UnrecoverableKeyException
     *             if the key can not be recovered from the store (should not
     *             happen here, either)
     */
    public void load(InputStream is, String pword) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, UnrecoverableKeyException {
        char[] pw = pword==null ? null : pword.toCharArray();
        store.load(is, pw);
    
        List<String> v_names = new ArrayList<>();
        this.privateKeyByAlias = new HashMap<>();
        this.certsByAlias = new HashMap<>();

        if (null != is){ // No point checking an empty keystore
            PrivateKey _key = null;
            int index = 0;
            Enumeration<String> aliases = store.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (store.isKeyEntry(alias)) {
                    if (index >= startIndex && index <= endIndex) {
                        _key = (PrivateKey) store.getKey(alias, pw);
                        if (null == _key) {
                            throw new IOException("No key found for alias: " + alias); // Should not happen
                        }
                        Certificate[] chain = store.getCertificateChain(alias);
                        if (null == chain) {
                            throw new IOException("No certificate chain found for alias: " + alias);
                        }
                        v_names.add(alias);
                        X509Certificate[] x509certs = new X509Certificate[chain.length];
                        for (int i = 0; i < x509certs.length; i++) {
                            x509certs[i] = (X509Certificate)chain[i];
                        }

                        privateKeyByAlias.put(alias, _key);
                        certsByAlias.put(alias, x509certs);
                    }
                    index++;
                }
            }
    
            if (null == _key) {
                throw new IOException("No key(s) found");
            }
            if (index <= endIndex-startIndex) {
                if (log.isWarnEnabled()) {
                    log.warn("Did not find all requested aliases. Start={}, end={}, found={}",
                            startIndex, endIndex, certsByAlias.size());
                }
            }
        }
    
        /*
         * Note: if is == null, the arrays will be empty
         */
        this.names = v_names.toArray(new String[v_names.size()]);
    }


    /**
     * Get the ordered certificate chain for a specific alias.
     *
     * @param alias
     *            the alias for which the certificate chain should be given
     * @return the certificate chain for the alias
     * @throws IllegalArgumentException
     *             if no chain could be found for the alias
     */
    public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate[] result = this.certsByAlias.get(alias);
        if(result != null) {
            return result;
        }
        // API expects null not empty array, see http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/X509KeyManager.html
        throw new IllegalArgumentException("No certificate found for alias:'"+alias+"'");
    }

    /**
     * Get the next or only alias.
     * 
     * @return the next or only alias.
     * @throws IllegalArgumentException
     *             if {@link JmeterKeyStore#clientCertAliasVarName
     *             clientCertAliasVarName} is not empty and no key for this
     *             alias could be found
     */
    public String getAlias() {
        if(!StringUtils.isEmpty(clientCertAliasVarName)) {
            // We return even if result is null
            String aliasName = JMeterContextService.getContext().getVariables().get(clientCertAliasVarName);
            if(StringUtils.isEmpty(aliasName)) {
                log.error("No var called '{}' found", clientCertAliasVarName);
                throw new IllegalArgumentException("No var called '"+clientCertAliasVarName+"' found");
            }
            return aliasName;
        }
        int length = this.names.length;
        if (length == 0) { // i.e. is == null
            return null;
        }
        return this.names[getIndexAndIncrement(length)];
    }

    public int getAliasCount() {
        return this.names.length;
    }

    public String getAlias(int index) {
        int length = this.names.length;
        if (length == 0 && index == 0) { // i.e. is == null
            return null;
        }
        if (index >= length || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return this.names[index];
    }

    /**
     * Return the private Key for a specific alias
     *
     * @param alias
     *            the name of the alias for the private key
     * @return the private key for the given <code>alias</code>
     * @throws IllegalArgumentException
     *             when no private key could be found
     */
    public PrivateKey getPrivateKey(String alias) {
        PrivateKey pk = this.privateKeyByAlias.get(alias);
        if(pk != null) {
            return pk;
        }
        throw new IllegalArgumentException("No PrivateKey found for alias:'"+alias+"'");
    }

    /**
     * Create a keystore which returns a range of aliases (if available)
     * 
     * @param type
     *            store type (e.g. JKS)
     * @param startIndex
     *            first index (from 0)
     * @param endIndex
     *            last index (to count -1)
     * @param clientCertAliasVarName
     *            name of the default key to, if empty the first key will be
     *            used as default key
     * @return the keystore
     * @throws KeyStoreException
     *             when the type of the store is not supported
     * @throws IllegalArgumentException
     *             when <code>startIndex</code> &lt; 0, <code>endIndex</code>
     *             &lt; 0, or <code>endIndex</code> &lt; <code>startIndex</code>
     */
    public static JmeterKeyStore getInstance(String type, int startIndex, int endIndex, String clientCertAliasVarName) throws KeyStoreException  {
        return new JmeterKeyStore(type, startIndex, endIndex, clientCertAliasVarName);
    }

    /**
     * Create a keystore which returns the first alias only.
     * 
     * @param type
     *            of the store e.g. JKS
     * @return the keystore
     * @throws KeyStoreException
     *             when the type of the store is not supported
     */
    public static JmeterKeyStore getInstance(String type) throws KeyStoreException {
        return getInstance(type, 0, 0, null);
    }

    /**
     * Gets current index and increment by rolling if index is equal to length
     * @param length Number of keys to roll
     */
    private int getIndexAndIncrement(int length) {
        synchronized(this) {
            int result = last_user++;
            if (last_user >= length) {
                last_user = 0;
            }
            return result;
        }
    }

    /**
     * Compiles the list of all client aliases with a private key.
     * TODO Currently, keyType and issuers are both ignored.
     *
     * @param keyType the key algorithm type name (RSA, DSA, etc.)
     * @param issuers  the CA certificates we are narrowing our selection on.
     * 
     * @return the array of aliases; may be empty
     */
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        int count = getAliasCount();
        String[] aliases = new String[count];
        for(int i = 0; i < aliases.length; i++) {
//            if (keys[i].getAlgorithm().equals(keyType)){
//                
//            }
            aliases[i] = this.names[i];
        }
        if(aliases.length>0) {
            return aliases;
        } else {
            // API expects null not empty array, see http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/X509KeyManager.html
            return null;
        }
    }

}
