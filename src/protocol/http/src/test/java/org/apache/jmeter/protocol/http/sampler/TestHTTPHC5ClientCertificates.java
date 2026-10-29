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

package org.apache.jmeter.protocol.http.sampler;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.exec.KeyToolUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * The client certificate of the JMeter key store has to be presented by the HttpClient5 sampler
 * implementation over every transport. The asynchronous transport, which HTTP/2 uses, performs the TLS
 * handshake with an {@code SSLEngine} on its I/O threads, so the certificate has to be chosen there, with
 * the variables of the JMeter thread the connection is opened for.
 */
@Isolated("changes the JMeter key store and the javax.net.ssl system properties")
class TestHTTPHC5ClientCertificates {

    private static final String PASSWORD = "jmeter";

    private static final String ALIAS_VARIABLE = "certAlias";

    private static final List<String> KEY_STORE_PROPERTIES = List.of(SSLManager.JAVAX_NET_SSL_KEY_STORE,
            "javax.net.ssl.keyStorePassword", "javax.net.ssl.keyStoreType");

    private final Map<String, String> savedProperties = new HashMap<>();

    @BeforeAll
    static void requireKeytool() {
        assumeTrue(KeyToolUtils.haveKeytool(), "keytool is needed to create the client certificates");
    }

    @AfterEach
    void restoreKeyStore() {
        HTTPHC5Impl.closeClientsOfCurrentThread();
        savedProperties.forEach((name, value) -> {
            if (value == null) {
                System.clearProperty(name);
            } else {
                System.setProperty(name, value);
            }
        });
        SSLManager.reset();
        JMeterContextService.getContext().setVariables(null);
    }

    /**
     * The server only trusts the second of two client certificates, so the sample only succeeds when the
     * certificate is presented at all, and when it is the one the alias variable of the JMeter thread
     * selects.
     */
    @ParameterizedTest
    @ValueSource(strings = {"HTTP/1.1", "HTTP/2", "HTTP/2 Strict"})
    void presentsTheClientCertificateSelectedByTheAliasVariable(String httpVersion, @TempDir Path dir)
            throws Exception {
        Path keyStore = dir.resolve("client.p12");
        KeyToolUtils.genkeypair(keyStore.toFile(), "first", PASSWORD, 30, "cn=first", null);
        KeyToolUtils.genkeypair(keyStore.toFile(), "second", PASSWORD, 30, "cn=second", null);
        Path trustStore = trustOnly(keyStore, "second", dir.resolve("server-trust.p12"));
        useJMeterKeyStore(keyStore);
        JMeterVariables variables = new JMeterVariables();
        variables.put(ALIAS_VARIABLE, "second");
        JMeterContextService.getContext().setVariables(variables);

        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .httpDisabled(true)
                .dynamicHttpsPort()
                .http2TlsDisabled(false)
                .needClientAuth(true)
                .trustStorePath(trustStore.toString())
                .trustStorePassword(PASSWORD)
                .trustStoreType("PKCS12"));
        server.start();
        try {
            server.stubFor(get(urlEqualTo("/mtls")).willReturn(aResponse().withStatus(200)));
            HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance("HttpClient5");
            sampler.setHttpVersion(httpVersion);

            HTTPSampleResult result = sampler.sample(
                    new URL("https://localhost:" + server.httpsPort() + "/mtls"), HTTPConstants.GET, false, 1);

            assertEquals("200", result.getResponseCode(),
                    () -> result.getResponseMessage() + "\n" + result.getResponseDataAsString());
            String expectedProtocol = httpVersion.startsWith("HTTP/2") ? "HTTP/2" : "HTTP/1.1";
            assertEquals(expectedProtocol, result.getResponseHeaders().substring(0, expectedProtocol.length()));
        } finally {
            server.stop();
        }
    }

    private void useJMeterKeyStore(Path keyStore) {
        for (String name : KEY_STORE_PROPERTIES) {
            savedProperties.put(name, System.getProperty(name));
        }
        System.setProperty(SSLManager.JAVAX_NET_SSL_KEY_STORE, keyStore.toString());
        System.setProperty("javax.net.ssl.keyStorePassword", PASSWORD);
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
        // The clients of this thread were set up with the previous key store
        HTTPHC5Impl.closeClientsOfCurrentThread();
        SSLManager.reset();
        SSLManager.getInstance().configureKeystore(true, 0, -1, ALIAS_VARIABLE);
    }

    private static Path trustOnly(Path keyStore, String alias, Path trustStore) throws Exception {
        KeyStore keys = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(keyStore)) {
            keys.load(in, PASSWORD.toCharArray());
        }
        KeyStore trusted = KeyStore.getInstance("PKCS12");
        trusted.load(null, null);
        trusted.setCertificateEntry(alias, keys.getCertificate(alias));
        try (OutputStream out = Files.newOutputStream(trustStore)) {
            trusted.store(out, PASSWORD.toCharArray());
        }
        return trustStore;
    }
}
