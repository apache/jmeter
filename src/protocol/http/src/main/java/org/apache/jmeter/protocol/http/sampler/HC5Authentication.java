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

import java.net.URL;
import java.util.List;

import org.apache.hc.client5.http.AuthenticationStrategy;
import org.apache.hc.client5.http.auth.AuthExchange;
import org.apache.hc.client5.http.auth.AuthSchemeFactory;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.AuthenticationException;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.auth.BasicSchemeFactory;
import org.apache.hc.client5.http.impl.auth.BearerSchemeFactory;
import org.apache.hc.client5.http.impl.auth.DigestSchemeFactory;
import org.apache.hc.client5.http.impl.auth.NTLMSchemeFactory;
import org.apache.hc.client5.http.impl.auth.ScramSchemeFactory;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jorphan.util.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication of {@link HTTPHC5Impl} with the credentials of the HTTP Authorization Manager and of the
 * proxy, set up the way {@link HTTPHC4Impl} does it:
 * <ul>
 * <li>NTLM is offered, with the domain of the Authorization Manager entry, or of the
 * {@code http.proxyDomain} property for the proxy. HttpClient 5 deprecates it, but still ships it.</li>
 * <li>The credentials of an entry with a realm only answer a challenge of that realm.</li>
 * <li>The credentials of an entry with the BASIC mechanism are sent with the first request, by HttpClient
 * rather than as a header of the request, so that a challenge for another scheme, like NTLM or Digest, can
 * still be answered.</li>
 * </ul>
 */
@SuppressWarnings("deprecation") // NTLM is deprecated in HttpClient 5, but HttpClient 4 supports it
final class HC5Authentication {

    private static final Logger log = LoggerFactory.getLogger(HC5Authentication.class);

    /**
     * Order in which the schemes a server or a proxy offers are tried: the one of HttpClient 5, with NTLM
     * ahead of Digest and Basic, like HttpClient 4 prefers it.
     */
    static final List<String> SCHEME_PRIORITY = List.of(StandardAuthScheme.BEARER, StandardAuthScheme.NTLM,
            StandardAuthScheme.DIGEST, StandardAuthScheme.BASIC);

    /** Selects the scheme to answer a challenge with, in the order of {@link #SCHEME_PRIORITY}. */
    static final AuthenticationStrategy STRATEGY = new DefaultAuthenticationStrategy() {
        @Override
        protected List<String> getSchemePriority() {
            return SCHEME_PRIORITY;
        }
    };

    private HC5Authentication() {
    }

    /**
     * Auth schemes HttpClient 5 registers by default, and NTLM.
     *
     * @return builder of the registry, so further schemes can be added
     */
    static RegistryBuilder<AuthSchemeFactory> authSchemes() {
        return RegistryBuilder.<AuthSchemeFactory>create()
                .register(StandardAuthScheme.BASIC, BasicSchemeFactory.INSTANCE)
                .register(StandardAuthScheme.DIGEST, DigestSchemeFactory.INSTANCE)
                .register(StandardAuthScheme.BEARER, BearerSchemeFactory.INSTANCE)
                .register(StandardAuthScheme.SCRAM_SHA_256, ScramSchemeFactory.INSTANCE)
                .register(StandardAuthScheme.NTLM, NTLMSchemeFactory.INSTANCE);
    }

    /**
     * Sets up the credentials of an Authorization Manager entry, which is not a Kerberos one, to answer the
     * challenges of the host of the URL.
     *
     * @param credentials   credentials of the request
     * @param url           URL the entry applies to
     * @param authorization the entry
     */
    static void setTargetCredentials(BasicCredentialsProvider credentials, URL url, Authorization authorization) {
        String realm = StringUtilities.isEmpty(authorization.getRealm()) ? null : authorization.getRealm();
        setCredentials(credentials, url.getHost(), getPort(url), realm,
                authorization.getUser(), authorization.getPass(), authorization.getDomain());
    }

    /**
     * Sets up the credentials of the proxy, with the domain of the {@code http.proxyDomain} property for NTLM.
     *
     * @param credentials credentials of the request
     * @param host        host of the proxy
     * @param port        port of the proxy
     * @param user        user to authenticate as
     * @param password    password of the user
     */
    static void setProxyCredentials(BasicCredentialsProvider credentials, String host, int port, String user,
            String password) {
        setCredentials(credentials, host, port, null, user, password, HTTPHCAbstractImpl.PROXY_DOMAIN);
    }

    /**
     * NTLM only accepts NT credentials, which the other schemes do not accept, so both are set up, and the
     * NT credentials are scoped to NTLM, which makes them the better match for an NTLM challenge.
     */
    private static void setCredentials(BasicCredentialsProvider credentials, String host, int port, String realm,
            String user, String password, String domain) {
        credentials.setCredentials(new AuthScope(null, host, port, realm, null),
                new UsernamePasswordCredentials(user, password.toCharArray()));
        credentials.setCredentials(new AuthScope(null, host, port, realm, StandardAuthScheme.NTLM),
                new NTCredentials(user, password.toCharArray(), HTTPHCAbstractImpl.LOCALHOST, domain));
    }

    /**
     * Has HttpClient send the credentials of an entry with the BASIC mechanism with the first request to the
     * host of the URL, unless it already authenticated with that host.
     *
     * @param context       context of the request
     * @param url           URL of the request
     * @param request       the request
     * @param authorization the entry
     * @return value of the {@code Authorization} header HttpClient sends, {@code null} if it sends none
     */
    static String preemptBasic(HttpClientContext context, URL url, HttpRequest request, Authorization authorization) {
        HttpHost target = new HttpHost(url.getProtocol(), url.getHost(), getPort(url));
        if (context.getAuthExchange(target).getState() != AuthExchange.State.UNCHALLENGED) {
            return null;
        }
        BasicScheme basic = new BasicScheme();
        basic.initPreemptive(new UsernamePasswordCredentials(authorization.getUser(),
                authorization.getPass().toCharArray()));
        String header;
        try {
            header = basic.generateAuthResponse(target, request, context);
        } catch (AuthenticationException e) {
            log.warn("Cannot send the credentials of {} as Basic: {}", authorization.getUser(), e.getMessage());
            return null;
        }
        context.resetAuthExchange(target, basic);
        return header;
    }

    private static int getPort(URL url) {
        return url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    }
}
