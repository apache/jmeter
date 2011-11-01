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

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Use this Keystore to wrap the normal KeyStore implementation.
 *
 */
public class DefaultKeyStore extends JmeterKeyStore {
    private X509Certificate[][] certChains;

    private PrivateKey[] keys;

    private String[] names;

    private final KeyStore store;

    //@GuardedBy("this")
    private int last_user;

    private static final String KEY_STORE_START_INDEX = "https.keyStoreStartIndex"; // $NON-NLS-1$
    private static final String KEY_STORE_END_INDEX   = "https.keyStoreEndIndex"; // $NON-NLS-1$

    private int startIndex;
    private int endIndex;

    public DefaultKeyStore(String type) throws Exception {
        this.store = KeyStore.getInstance(type);
        startIndex = JMeterUtils.getPropDefault(KEY_STORE_START_INDEX, 0);
        endIndex = JMeterUtils.getPropDefault(KEY_STORE_END_INDEX, 0);
    }

    /** {@inheritDoc} */
    @Override
    public void load(InputStream is, String pword) throws Exception {
        store.load(is, pword.toCharArray());

        ArrayList<String> v_names = new ArrayList<String>();
        ArrayList<PrivateKey> v_keys = new ArrayList<PrivateKey>();
        ArrayList<X509Certificate[]> v_certChains = new ArrayList<X509Certificate[]>();

        if (null != is){ // No point checking an empty keystore
            PrivateKey _key = null;
            int index = 0;
            Enumeration<String> aliases = store.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (store.isKeyEntry(alias)) {
                    if ((index >= startIndex && index <= endIndex)) {
                        _key = (PrivateKey) store.getKey(alias, pword.toCharArray());
                        if (null == _key) {
                            throw new Exception("No key found for alias: " + alias); // Should not happen
                        }
                        Certificate[] chain = store.getCertificateChain(alias);
                        if (null == chain) {
                            throw new Exception("No certificate chain found for alias: " + alias);
                        }
                        v_names.add(alias);
                        v_keys.add(_key);
                        X509Certificate[] x509certs = new X509Certificate[chain.length];
                        for (int i = 0; i < x509certs.length; i++) {
                            x509certs[i] = (X509Certificate)chain[i];
                        }
                        v_certChains.add(x509certs);
                    }
                }
                index++;
            }

            if (null == _key) {
                throw new Exception("No key(s) found");
            }
        }

        /*
         * Note: if is == null, the arrays will be empty
         */
        int v_size = v_names.size();

        this.names = new String[v_size];
        this.names = v_names.toArray(names);

        this.keys = new PrivateKey[v_size];
        this.keys = v_keys.toArray(keys);

        this.certChains = new X509Certificate[v_size][];
        this.certChains = v_certChains.toArray(certChains);
    }

    @Override
    public final X509Certificate[] getCertificateChain(String alias) {
        int entry = findAlias(alias);
        if (entry >=0) {
            return this.certChains[entry];
        }
        return null;
    }

    @Override
    public final PrivateKey getPrivateKey(String alias) {
        int entry = findAlias(alias);
        if (entry >=0) {
            return this.keys[entry];
        }
        return null;
    }

    @Override
    public final String getAlias() {
        int length = this.names.length;
        if (length == 0) { // i.e. is == null
            return null;
        }
        return this.names[getNextIndex(length)];
    }

    @Override
    public final String getAlias(int index) {
        int length = this.names.length;
        if (length == 0 && index == 0) { // i.e. is == null
            return null;
        }
        if (index >= length || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return this.names[index];
    }

    @Override
    public int getAliasCount() {
        return this.names.length;
    }

    private int findAlias(String alias) {
        for(int i = 0; i < names.length; i++) {
            if (alias.equals(names[i])){
                return i;
            }
        }
        return -1;
    }

    private int getNextIndex(int length) {
        synchronized(this) {
            last_user ++;
            if (last_user >= length) {
                last_user = 0;
            }
            return last_user;
        }
    }

    /**
     * @return the startIndex
     */
    public int getAliasStartIndex() {
        return startIndex;
    }

    /**
     * @param startIndex the startIndex to set
     */
    public void setAliasStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * @return the endIndex
     */
    public int getAliasEndIndex() {
        return endIndex;
    }

    /**
     * @param endIndex the endIndex to set
     */
    public void setAliasEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

}
