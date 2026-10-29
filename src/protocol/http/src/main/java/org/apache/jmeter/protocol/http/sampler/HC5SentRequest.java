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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jorphan.io.CountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reports the request of a sample of {@link HTTPHC5Impl} the way HttpClient sent it, like
 * {@link HTTPHC4Impl} does.
 * <p>
 * HttpClient sends a copy of the request JMeter built, to which its protocol interceptors add headers
 * like {@code Host}, or the {@code Authorization} of an authentication. When it follows redirects on its
 * own, it sends a request of its own for every hop, whose {@code Cookie} and {@code Authorization}
 * headers {@link AutoRedirectStrategy} sets for the URL of that hop. Both transports keep the request
 * they sent last in the context, so that is the one reported, which is what the server of the final
 * hop received.
 */
final class HC5SentRequest {

    private static final Logger log = LoggerFactory.getLogger(HC5SentRequest.class);

    private static final String CONTEXT_ATTRIBUTE_PREEMPTIVE_AUTHORIZATION = "__jmeter.PREEMPTIVE_AUTH__"; //$NON-NLS-1$

    private HC5SentRequest() {
        // utility class
    }

    /**
     * Remembers the {@code Authorization} header HttpClient is going to send pre-emptively, so that it
     * can be reported even if HttpClient fails before it sends the request.
     *
     * @param context context the request is executed with
     * @param header  value of the header
     */
    static void rememberPreemptiveAuthorization(HttpClientContext context, String header) {
        context.setAttribute(CONTEXT_ATTRIBUTE_PREEMPTIVE_AUTHORIZATION, header);
    }

    /**
     * Sets the request headers, the cookies and the sent bytes of the result to those of the request
     * HttpClient sent last. If it failed before it sent any, the request JMeter built is reported, with
     * the {@code Authorization} header HttpClient was going to add pre-emptively.
     *
     * @param request request JMeter built
     * @param context context the request was executed with, may be {@code null}
     * @param result  result of the sample
     */
    static void report(HttpUriRequestBase request, HttpClientContext context, HTTPSampleResult result) {
        HttpRequest sent = context == null ? null : context.getRequest();
        if (sent != null) {
            result.setRequestHeaders(headers(sent));
            result.setSentBytes(size(sent, entityOf(sent, request)));
            result.setCookies(HTTPHCAbstractImpl.getOnlyCookieFromHeaders(HTTPHC5Impl.headersOf(sent)));
            return;
        }
        String headers = headers(request);
        long size = size(request, request.getEntity());
        String authorization = context == null ? null
                : (String) context.getAttribute(CONTEXT_ATTRIBUTE_PREEMPTIVE_AUTHORIZATION);
        if (authorization != null) {
            headers += HttpHeaders.AUTHORIZATION + ": " + authorization + "\n";
            size += HTTPMessageSizes.headerLength(HttpHeaders.AUTHORIZATION, authorization);
        }
        result.setRequestHeaders(headers);
        result.setSentBytes(size);
    }

    /**
     * Body of the request HttpClient sent. The classic transport keeps it with the request, while the
     * HTTP/2 one streams the body of the request JMeter built, which a redirect only drops when it
     * changes the method to {@code GET}.
     */
    private static HttpEntity entityOf(HttpRequest sent, HttpUriRequestBase request) {
        if (sent instanceof ClassicHttpRequest classicRequest) {
            return classicRequest.getEntity();
        }
        return request.getMethod().equalsIgnoreCase(sent.getMethod()) ? request.getEntity() : null;
    }

    private static String headers(HttpRequest request) {
        return HTTPHCAbstractImpl.formatHeaders(HTTPHC5Impl.headersOf(request), HTTPAbstractImpl.ALL_EXCEPT_COOKIE);
    }

    private static long size(HttpRequest request, HttpEntity entity) {
        String uri = request.getRequestUri() != null ? request.getRequestUri() : "";
        ProtocolVersion version = request.getVersion();
        String versionStr = version != null ? version.toString() : HTTPConstants.HTTP_1_1;
        long sentBytes = HTTPMessageSizes.requestLineLength(request.getMethod(), uri, versionStr);

        for (Header header : request.getHeaders()) {
            sentBytes += HTTPMessageSizes.headerLength(header.getName(), header.getValue());
        }
        sentBytes += implicitHeaderLength(request, entity);
        sentBytes += HTTPMessageSizes.EMPTY_LINE;

        if (entity != null) {
            long contentLength = entity.getContentLength();
            if (contentLength >= 0) {
                sentBytes += contentLength;
            } else if (entity.isRepeatable()) {
                CountingOutputStream counter = new CountingOutputStream(OutputStream.nullOutputStream());
                try {
                    entity.writeTo(counter);
                    sentBytes += counter.getBytesWritten();
                } catch (IOException e) {
                    log.debug("Exception measuring entity length", e);
                }
            }
        }
        return sentBytes;
    }

    /**
     * Length of the headers the protocol layer adds when it writes the request, so that they are
     * accounted for although they are not part of the request object, like those the HTTP/2 transport
     * turns into pseudo-headers or leaves out.
     */
    private static long implicitHeaderLength(HttpRequest request, HttpEntity entity) {
        long length = 0;
        URIAuthority authority = request.getAuthority();
        if (authority != null && !request.containsHeader(HttpHeaders.HOST)) {
            length += HTTPMessageSizes.headerLength(HttpHeaders.HOST, authority.toString());
        }
        if (entity == null) {
            return length;
        }
        if (entity.getContentType() != null && !request.containsHeader(HttpHeaders.CONTENT_TYPE)) {
            length += HTTPMessageSizes.headerLength(HttpHeaders.CONTENT_TYPE, entity.getContentType());
        }
        if (entity.getContentEncoding() != null && !request.containsHeader(HttpHeaders.CONTENT_ENCODING)) {
            length += HTTPMessageSizes.headerLength(HttpHeaders.CONTENT_ENCODING, entity.getContentEncoding());
        }
        if (request.containsHeader(HttpHeaders.CONTENT_LENGTH) || request.containsHeader(HttpHeaders.TRANSFER_ENCODING)) {
            return length;
        }
        long contentLength = entity.getContentLength();
        if (contentLength >= 0) {
            length += HTTPMessageSizes.headerLength(HttpHeaders.CONTENT_LENGTH, Long.toString(contentLength));
        } else if (entity.isChunked()) {
            length += HTTPMessageSizes.headerLength(HttpHeaders.TRANSFER_ENCODING, "chunked");
        }
        return length;
    }
}
