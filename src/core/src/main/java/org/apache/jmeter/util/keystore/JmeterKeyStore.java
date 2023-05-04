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
import java.util.concurrent.atomic.AtomicInteger;
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

    private int aliasCount;

    private Map<String, PrivateKey> privateKeyByAlias = new HashMap<>();
    private Map<String, X509Certificate[]> certsByAlias = new HashMap<>();
    private Map<String, String[]> aliasesByKeyType = new HashMap<>();
    private Map<String, String> keyTypeByAlias = new HashMap<>();
    private Map<String, AtomicInteger> aliasIndexByKeyType = new HashMap<>();

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

        this.privateKeyByAlias = new HashMap<>();
        this.certsByAlias = new HashMap<>();
        this.aliasesByKeyType = new HashMap<>();
        this.keyTypeByAlias = new HashMap<>();
        this.aliasIndexByKeyType = new HashMap<>();

        PrivateKey privateKey = null;
        if (log.isDebugEnabled()) {
            logDetailsOnKeystore(store);
        }
        int index = 0;
        Enumeration<String> aliases = store.aliases();

        Map<String, Collection<String>> aliasesByKeyType = new HashMap<>();

        int count = 0;

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
                privateKeyByAlias.put(alias, privateKey);
                certsByAlias.put(alias, toX509Certificates(chain));
                String keyType = privateKey.getAlgorithm();
                aliasesByKeyType.compute(keyType, (k,v)->{
                    if (v == null) { v = new ArrayList<>(); }
                    v.add(alias);
                    return v;
                });
                keyTypeByAlias.put(alias, keyType);
                count++;
            }
            index++;
        }

        aliasCount = count;

        aliasesByKeyType.forEach((k,v)-> {
            this.aliasesByKeyType.put(k, v.toArray(new String[0]));
            aliasIndexByKeyType.put(k, new AtomicInteger());
        });

        if (is != null) { // only check for keys, if we were given a file as inputstream
            Objects.requireNonNull(privateKey, "No key(s) found");
            if (endIndex != -1 && index <= endIndex - startIndex && log.isWarnEnabled()) {
                log.warn("Did not find as much aliases as configured in indexes Start={}, end={}, found={}", startIndex,
                        endIndex, certsByAlias.size());
            }
        }

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
    private void logDetailsOnKeystore(KeyStore keystore) {
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

    private String decodeSanList(Collection<List<?>> subjectAlternativeNames) {
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

    private String sanDataToString(Object data) {
        if (data instanceof String) {
            return (String) data;
        }
        return Hex.encodeHexString((byte[]) data);
    }

    private String sanGeneralNameIndexToName(Integer index) {
        String description;
        if (index < SAN_GENERAL_NAMES.size()) {
            description = SAN_GENERAL_NAMES.get(index);
        } else {
            description = "UNKNOWN_SAN_GENERAL_NAME";
        }
        return description;
    }

    private X509Certificate[] toX509Certificates(Certificate[] chain) {
        X509Certificate[] x509certs = new X509Certificate[chain.length];
        for (int i = 0; i < x509certs.length; i++) {
            x509certs[i] = (X509Certificate) chain[i];
        }
        return x509certs;
    }

    private boolean isIndexInConfiguredRange(int index) {
        return index >= startIndex && (endIndex == -1 || index <= endIndex);
    }

    private char[] toCharArrayOrNull(String pword) {
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
    public String getAlias(String [] keyTypes) {

        for (String keyType : keyTypes) {

            String alias = getAlias(keyType);
            if (alias != null) { return alias; }

        }

        return null;

    }

    /**
     * Get the next, only or null alias for the specified key type.
     *
     * @param keyType key type that the key under the alias must have
     * @return the next, only, or null alias
     */
    public String getAlias(String keyType) {

        if (StringUtils.isNotEmpty(clientCertAliasVarName)) {
            String aliasName = JMeterContextService.getContext().getVariables().get(clientCertAliasVarName);
            if (StringUtils.isEmpty(aliasName)) {
                log.error("No var called '{}' found", clientCertAliasVarName);
                throw new IllegalArgumentException("No var called '" + clientCertAliasVarName + "' found");
            }
            if (Objects.equals(keyTypeByAlias.get(aliasName), keyType)) {
                return aliasName;
            }
            // NOTE: the getAlias() call will likely get repeated with a "right" key type eventually.
            log.debug("Key for alias '{}' is not of type '{}', returning null", aliasName, keyType);
            return null;
        }

        // TODO if we do have keys of multiple types, we won't iterate
        // through all of them, only through the preferred ones.

        String [] aliases = aliasesByKeyType.get(keyType);
        if (aliases == null) { return null; }

        int length = aliases.length;
        if (length == 0) { // i.e. is == null
            return null;
        }
        return aliases[getIndexAndIncrement(keyType, length)];

    }

    public int getAliasCount() {
        return aliasCount;
    }

    // NOTE: getAlias(int) has been removed as it's unused, and has unclear semantics.

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
    private int getIndexAndIncrement(String keyType, int length) {

        AtomicInteger index = aliasIndexByKeyType.get(keyType);
        return index.getAndUpdate(i->{
            if (++i >= length) {
                return 0;
            }
            return i;
        });

    }

    /**
     * Compiles the list of all client aliases with a private key.
     * TODO Currently, issuers are ignored.
     *
     * @param keyType the key algorithm type name (RSA, DSA, etc.)
     * @param issuers the CA certificates we are narrowing our selection on.
     * @return the array of aliases; null if none.
     * @see javax.net.ssl.X509KeyManager#getClientAliases
     */
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        String [] aliases = aliasesByKeyType.get(keyType);
        if (aliases == null) { return null; }
        int count = aliases.length;
        if (count == 0) {
            return null;
        }
        return Arrays.copyOf(aliases, count);
    }

}
