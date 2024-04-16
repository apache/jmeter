/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.threads.JMeterContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this Keystore for JMeter specific KeyStores.
 */
public final class JmeterKeyStore {

    private static final Logger log = LoggerFactory.getLogger(JmeterKeyStore.class);

    public static final String DEFAULT_ALIAS_VAR_NAME = "certAlias";

    private final KeyStore store;

    /** first index to consider for a key */
    private final int startIndex;

    /** last index to consider for a key */
    private final int endIndex;

    /** name of the default alias */
    private final String clientCertAliasVarName;

    private String[] names = new String[0];
    private Map<String, PrivateKey> privateKeyByAlias = new HashMap<>();
    private Map<String, X509Certificate[]> certsByAlias = new HashMap<>();

    private int lastAliasIndex;

    /**
     * @param type                   type of the {@link KeyStore}
     * @param startIndex             which keys should be considered, starting from <code>0</code>
     * @param endIndex               which keys should be considered, up to <code>count - 1</code>
     * @param clientCertAliasVarName name for the default key, if empty use the first key available
     * @throws KeyStoreException        when the type of the keystore is not supported
     * @throws IllegalArgumentException when <code>startIndex</code> &lt; 0, <code>endIndex</code>
     *                                  &lt; -1 or <code>endIndex</code> &lt; </code>startIndex</code>
     */
    private JmeterKeyStore(String type, int startIndex, int endIndex, String clientCertAliasVarName) throws KeyStoreException {
        if (startIndex < 0 || (endIndex != -1 && endIndex < startIndex)) {
            throw new IllegalArgumentException(
                    "Invalid index(es). Start=" + startIndex + ", end=" + endIndex);
        }
        this.store = KeyStore.getInstance(type);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.clientCertAliasVarName = clientCertAliasVarName;
    }

    /**
     * Process the input stream and try to read the keys from the store
     *
     * @param is    {@link InputStream} from which the store should be loaded
     * @param pword the password used to check the integrity of the store
     * @throws IOException               if there is a problem decoding or reading the store. A bad
     *                                   password might be the cause for this, or an empty store
     * @throws CertificateException      if any of the certificated in the store can not be loaded
     * @throws NoSuchAlgorithmException  if the algorithm to check the integrity of the store can not
     *                                   be found
     * @throws KeyStoreException         if the store has not been initialized (should not happen
     *                                   here)
     * @throws UnrecoverableKeyException if the key can not be recovered from the store (should not
     *                                   happen here, either)
     */
    @SuppressWarnings("JdkObsolete")
    public void load(InputStream is, String pword)
            throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, UnrecoverableKeyException {
        char[] pw = toCharArrayOrNull(pword);
        store.load(is, pw);

        List<String> aliasesList = new ArrayList<>();
        this.privateKeyByAlias = new HashMap<>();
        this.certsByAlias = new HashMap<>();

        PrivateKey privateKey = null;
        if (log.isDebugEnabled()) {
            logDetailsOnKeystore(store);
        }
        int index = 0;
        Enumeration<String> aliases = store.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (!store.isKeyEntry(alias)) {
                continue;
            }
            if (isIndexInConfiguredRange(index)) {
                privateKey = Objects.requireNonNull(
                        (PrivateKey) store.getKey(alias, pw),
                        "No key found for alias: " + alias);
                Certificate[] chain = Objects.requireNonNull(
                        store.getCertificateChain(alias),
                        "No certificate chain found for alias" + alias);
                aliasesList.add(alias);
                privateKeyByAlias.put(alias, privateKey);
                certsByAlias.put(alias, toX509Certificates(chain));
            }
            index++;
        }

        if (is != null) { // only check for keys, if we were given a file as inputstream
            Objects.requireNonNull(privateKey, "No key(s) found");
            if (endIndex != -1 && index <= endIndex - startIndex && log.isWarnEnabled()) {
                log.warn("Did not find as much aliases as configured in indexes Start={}, end={}, found={}", startIndex,
                        endIndex, certsByAlias.size());
            }
        }

        /*
         * Note: if is == null and no pkcs11 store is configured, the arrays will be empty
         */
        this.names = aliasesList.toArray(new String[aliasesList.size()]);
    }

    private static final Map<String, String> EXTENDED_KEY_USAGES = new HashMap<>();
    static {
        EXTENDED_KEY_USAGES.put("1.3.6.1.4.1.311.10.3.4", "Can use encrypted file systems (EFS) (EFS_CRYPTO)");
        EXTENDED_KEY_USAGES.put("1.3.6.1.4.1.311.10.3.4.1", "Can use encrypted file systems (EFS) (EFS_RECOVERY)");
        EXTENDED_KEY_USAGES.put("1.3.6.1.4.1.311.20.2.2", "Smartcard logon to Microsoft Windows");
        EXTENDED_KEY_USAGES.put("1.3.6.1.5.5.7.3.1",
                "Transport Layer Security (TLS) World Wide Web (WWW) server authentication");
        EXTENDED_KEY_USAGES.put("1.3.6.1.5.5.7.3.2",
                "Transport Layer Security (TLS) World Wide Web (WWW) client authentication");
        EXTENDED_KEY_USAGES.put("1.3.6.1.5.5.7.3.3", "Signing of downloadable executable code");
        EXTENDED_KEY_USAGES.put("1.3.6.1.5.5.7.3.4", "Email protection");
        EXTENDED_KEY_USAGES.put("1.3.6.1.5.5.7.3.5", "IP security end system");
        EXTENDED_KEY_USAGES.put("1.3.6.1.5.5.7.3.6", "IP security tunnel termination");
        EXTENDED_KEY_USAGES.put("1.3.6.1.5.5.7.3.7", "IP security user");
        EXTENDED_KEY_USAGES.put("1.3.6.1.5.5.7.3.8", "Time stamping");
        EXTENDED_KEY_USAGES.put("1.3.6.1.5.5.7.3.9", "Signing Online Certificate Status Protocol (OCSP) responses");
        EXTENDED_KEY_USAGES.put("2.5.29.37.0", "Any purpose");
    }

    private static final List<String> SAN_GENERAL_NAMES = Arrays.asList("otherName", "rfc822Name", "dNSName", "x400Address",
            "directoryName", "ediPartyName", "uniformResourceIdentifier", "iPAddress", "registeredID");

    @SuppressWarnings("JdkObsolete")
    private static void logDetailsOnKeystore(KeyStore keystore) {
        Enumeration<String> aliases;
        try {
            aliases = keystore.aliases();
        } catch (KeyStoreException e) {
            log.debug("Problem reading the aliases from the store {}", keystore, e);
            return;
        }
        int i = 1;
        while(aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            log.debug("Certificate at index {} with alias {}", i++, alias);
            X509Certificate cert;
            try {
                cert = (X509Certificate) keystore.getCertificate(alias);
            } catch (KeyStoreException e) {
                log.debug("Can't read certificate for alias {}", alias, e);
                continue;
            }
            log.debug("Subject DN: {}", cert.getSubjectX500Principal());
            log.debug("Issuer DN: {}", cert.getIssuerX500Principal());
            log.debug("Not valid before: {}", cert.getNotBefore().toInstant());
            log.debug("Not valid after: {}", cert.getNotAfter().toInstant());
            try {
                final Collection<List<?>> subjectAlternativeNames = cert.getSubjectAlternativeNames();
                if (!(subjectAlternativeNames == null || subjectAlternativeNames.isEmpty())) {
                    log.debug("SAN: {}", decodeSanList(subjectAlternativeNames));
                }
            } catch (CertificateParsingException e) {
                log.debug("Problem parsing SAN for alias {}", alias, e);
            }
            List<String> extendedKeyUsage;
            try {
                extendedKeyUsage = cert.getExtendedKeyUsage();
                if (extendedKeyUsage != null) {
                    for (String keyUsage : extendedKeyUsage) {
                        log.debug("EKU: {} ({})", EXTENDED_KEY_USAGES.getOrDefault(keyUsage, keyUsage),
                                keyUsage);
                    }
                }
            } catch (CertificateParsingException e) {
                log.debug("Can't get EKU for alias {}", alias, e);
            }
        }
    }

    private static String decodeSanList(Collection<? extends List<?>> subjectAlternativeNames) {
        List<Pair<String, String>> decodedEntries = new ArrayList<>();
        for (List<?> entry : subjectAlternativeNames) {
            Object indexData = entry.get(0);
            Object data = entry.get(1);
            if (indexData instanceof Integer) {
                Integer generalNameIndex = (Integer) indexData;
                String description = sanGeneralNameIndexToName(generalNameIndex);
                String valueString = sanDataToString(data);
                decodedEntries.add(Pair.of(description, valueString));
            }
        }
        return decodedEntries.stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
    }

    private static String sanDataToString(Object data) {
        if (data instanceof String) {
            return (String) data;
        }
        return Hex.encodeHexString((byte[]) data);
    }

    private static String sanGeneralNameIndexToName(Integer index) {
        String description;
        if (index < SAN_GENERAL_NAMES.size()) {
            description = SAN_GENERAL_NAMES.get(index);
        } else {
            description = "UNKNOWN_SAN_GENERAL_NAME";
        }
        return description;
    }

    private static X509Certificate[] toX509Certificates(Certificate[] chain) {
        X509Certificate[] x509certs = new X509Certificate[chain.length];
        for (int i = 0; i < x509certs.length; i++) {
            x509certs[i] = (X509Certificate) chain[i];
        }
        return x509certs;
    }

    private boolean isIndexInConfiguredRange(int index) {
        return index >= startIndex && (endIndex == -1 || index <= endIndex);
    }

    private static char[] toCharArrayOrNull(String pword) {
        if (pword == null) {
            return null; // NOSONAR the api used requires null for "no password used"
        }
        return pword.toCharArray();
    }

    /**
     * Get the ordered certificate chain for a specific alias.
     *
     * @param alias the alias for which the certificate chain should be given
     * @return the certificate chain for the alias or null if not found
     * @see javax.net.ssl.X509KeyManager#getCertificateChain(String)
     */
    public X509Certificate[] getCertificateChain(String alias) {
        X509Certificate[] result = this.certsByAlias.get(alias);
        if (result != null) {
            return result;
        }
        // API expects null not empty array.
        // See http://docs.oracle.com/javase/7/docs/api/javax/net/ssl/X509KeyManager.html
        // However, throwing here to provide a better error message to the user
        throw new IllegalArgumentException("No certificate found for alias:'" + alias + "'");
    }

    /**
     * Get the next or only alias.
     *
     * @return the next or only alias.
     * @throws IllegalArgumentException if {@link #clientCertAliasVarName}
     *                                  is not empty and no key for this alias could be found
     */
    public String getAlias() {
        if (StringUtils.isNotEmpty(clientCertAliasVarName)) {
            String aliasName = JMeterContextService.getContext().getVariables().get(clientCertAliasVarName);
            if (StringUtils.isEmpty(aliasName)) {
                log.error("No var called '{}' found", clientCertAliasVarName);
                throw new IllegalArgumentException("No var called '" + clientCertAliasVarName + "' found");
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
     * @param alias the name of the alias for the private key
     * @return the private key for the given <code>alias</code>
     * @throws IllegalArgumentException when no private key could be found
     */
    public PrivateKey getPrivateKey(String alias) {
        PrivateKey pk = this.privateKeyByAlias.get(alias);
        if (pk != null) {
            return pk;
        }
        throw new IllegalArgumentException("No PrivateKey found for alias:'" + alias + "'");
    }

    /**
     * Create a keystore which returns a range of aliases (if available)
     *
     * @param type                   store type (e.g. JKS)
     * @param startIndex             first index (from 0)
     * @param endIndex               last index (to count-1)
     * @param clientCertAliasVarName name of the default key to, if empty the first key will be
     *                               used as default key
     * @return the keystore
     * @throws KeyStoreException        when the type of the store is not supported
     * @throws IllegalArgumentException when <code>startIndex</code> &lt; 0, <code>endIndex</code>
     *                                  &lt; -1, or <code>endIndex</code> &lt; <code>startIndex</code>
     */
    public static JmeterKeyStore getInstance(String type, int startIndex, int endIndex, String clientCertAliasVarName) throws KeyStoreException {
        return new JmeterKeyStore(type, startIndex, endIndex, clientCertAliasVarName);
    }

    /**
     * Create a keystore which returns the first alias only.
     *
     * @param type of the store e.g. JKS
     * @return the keystore
     * @throws KeyStoreException when the type of the store is not supported
     */
    public static JmeterKeyStore getInstance(String type) throws KeyStoreException {
        return getInstance(type, 0, -1, DEFAULT_ALIAS_VAR_NAME);
    }

    /**
     * Gets current index and increment by rolling if index is equal to length
     *
     * @param length Number of keys to roll
     */
    private int getIndexAndIncrement(int length) {
        synchronized (this) {
            int result = lastAliasIndex++;
            if (lastAliasIndex >= length) {
                lastAliasIndex = 0;
            }
            return result;
        }
    }

    /**
     * Compiles the list of all client aliases with a private key.
     * TODO Currently, keyType and issuers are both ignored.
     *
     * @param keyType the key algorithm type name (RSA, DSA, etc.)
     * @param issuers the CA certificates we are narrowing our selection on.
     * @return the array of aliases; null if none.
     * @see javax.net.ssl.X509KeyManager#getClientAliases
     */
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        int count = this.names.length;
        if (count == 0) {
            return null;
        }
        return Arrays.copyOf(this.names, count);
    }

}
