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

package org.apache.jmeter.protocol.http.curl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.protocol.http.config.MultipartUrlConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.StringUtilities;

/**
 * Renders an {@link HTTPSampleResult} as a ready-to-run {@code curl} command,
 * the reverse of what {@link BasicCurlParser} does.
 *
 * <p>The generated command targets a POSIX-compatible shell: arguments are
 * single-quoted and lines are continued with a trailing backslash. It is not
 * valid {@code cmd.exe} or PowerShell syntax.</p>
 *
 * <p>The class has no Swing dependency so it can be reused outside the
 * View Results Tree (for example by a future "Copy as cURL" sampler action).</p>
 */
public final class CurlCommandFormatter {

    /** Backslash line continuation followed by indentation, for a POSIX shell. */
    private static final String NEWLINE = " \\\n  "; //$NON-NLS-1$

    private static final String ACCEPT_ENCODING = "accept-encoding"; //$NON-NLS-1$

    private static final String BOUNDARY = "boundary="; //$NON-NLS-1$

    /**
     * Headers that must not be reproduced in the curl command: curl generates
     * them itself, they are connection-specific (hop-by-hop) headers that are
     * forbidden in HTTP/2 and would make the request fail with a protocol
     * error, or they are pseudo-headers JMeter adds only for reporting and that
     * never went on the wire (X-LocalAddress).
     */
    private static final Set<String> SKIPPED_HEADERS = Set.of(
            "content-length", //$NON-NLS-1$
            "connection", //$NON-NLS-1$
            "keep-alive", //$NON-NLS-1$
            "proxy-connection", //$NON-NLS-1$
            "transfer-encoding", //$NON-NLS-1$
            "upgrade", //$NON-NLS-1$
            HTTPConstants.HEADER_LOCAL_ADDRESS.toLowerCase(Locale.ROOT));

    /**
     * Markers JMeter writes into the rendered request body in place of content
     * it did not keep (a file sent as the body, or a non-repeatable entity).
     * When present, the body is not the real wire body and cannot be reproduced.
     *
     * @see org.apache.jmeter.protocol.http.sampler.PostWriter
     */
    private static final String[] BODY_PLACEHOLDERS = {
            "<actual file content, not shown here>", //$NON-NLS-1$
            "<Entity was not repeatable, cannot view what was sent>" //$NON-NLS-1$
    };

    private CurlCommandFormatter() {
    }

    /**
     * Build a {@code curl} command line that reproduces the given HTTP request.
     *
     * @param sampleResult the sampled HTTP request
     * @return the curl command as a string
     */
    public static String format(HTTPSampleResult sampleResult) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("curl"); //$NON-NLS-1$

        String method = sampleResult.getHTTPMethod();
        boolean isHead = HTTPConstants.HEAD.equalsIgnoreCase(method);
        boolean isGet = StringUtilities.isBlank(method) || HTTPConstants.GET.equalsIgnoreCase(method);

        // Split by line (not via JMeterUtils.parseHeaders) so repeated header
        // names such as several Accept values are all preserved.
        List<String[]> headers = new ArrayList<>();
        String contentType = null;
        boolean acceptsEncoding = false;
        String requestHeaders = sampleResult.getRequestHeaders();
        if (StringUtilities.isNotEmpty(requestHeaders)) {
            for (String header : requestHeaders.split("\n")) { //$NON-NLS-1$
                int colon = header.indexOf(':');
                if (colon <= 0) {
                    continue;
                }
                String name = header.substring(0, colon).trim();
                String value = header.substring(colon + 1).trim();
                String lower = name.toLowerCase(Locale.ROOT);
                if (SKIPPED_HEADERS.contains(lower)) {
                    continue;
                }
                if (HTTPConstants.HEADER_CONTENT_TYPE.equalsIgnoreCase(name)) {
                    contentType = value;
                }
                if (ACCEPT_ENCODING.equals(lower)) {
                    acceptsEncoding = true;
                }
                headers.add(new String[] { name, value });
            }
        }

        String body = isHead ? "" : sampleResult.getQueryString(); //$NON-NLS-1$
        boolean hasBody = StringUtilities.isNotEmpty(body);
        boolean isMultipart = contentType != null
                && contentType.toLowerCase(Locale.ROOT).startsWith(HTTPConstants.MULTIPART_FORM_DATA);
        // Multipart bodies are rebuilt as -F flags; curl then sets its own
        // Content-Type (with its own boundary), so the original one is dropped.
        List<String> formParts = hasBody && isMultipart ? parseMultipartForm(contentType, body) : List.of();
        boolean emitsForm = !formParts.isEmpty();
        boolean emitsDataRaw = hasBody && !isMultipart && !containsPlaceholder(body);

        // Method: --head for HEAD (plain "-X HEAD" makes curl wait for a body it
        // never gets); no -X for a plain GET; -X GET only when a GET carries a
        // raw body, otherwise curl would switch it to POST; -X for everything else.
        if (isHead) {
            sb.append(" --head"); //$NON-NLS-1$
        } else if (!isGet) {
            sb.append(" -X ").append(quote(method)); //$NON-NLS-1$
        } else if (emitsDataRaw) {
            sb.append(" -X ").append(quote(HTTPConstants.GET)); //$NON-NLS-1$
        }

        URL url = sampleResult.getURL();
        if (url != null) {
            sb.append(NEWLINE).append(quote(url.toString()));
        }

        for (String[] header : headers) {
            if (emitsForm && HTTPConstants.HEADER_CONTENT_TYPE.equalsIgnoreCase(header[0])) {
                continue;
            }
            sb.append(NEWLINE).append("-H ").append(quote(header[0] + ": " + header[1])); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // HttpClient disables automatic decompression, so Accept-Encoding is only
        // present when explicitly set; --compressed makes curl decode the response.
        if (acceptsEncoding) {
            sb.append(NEWLINE).append("--compressed"); //$NON-NLS-1$
        }

        String cookies = sampleResult.getCookies();
        if (StringUtilities.isNotEmpty(cookies)) {
            sb.append(NEWLINE).append("-b ").append(quote(cookies)); //$NON-NLS-1$
        }

        if (emitsForm) {
            for (String part : formParts) {
                sb.append(NEWLINE).append("-F ").append(quote(part)); //$NON-NLS-1$
            }
        } else if (emitsDataRaw) {
            sb.append(NEWLINE).append("--data-raw ").append(quote(body)); //$NON-NLS-1$
        } else if (hasBody) {
            // A file sent as the body or a non-repeatable entity: the bytes were
            // not kept, so emitting them would produce a silently-wrong command.
            sb.append('\n').append("# ") //$NON-NLS-1$
                    .append(JMeterUtils.getResString("view_results_table_request_tab_curl_body_omitted")); //$NON-NLS-1$
        }

        return sb.toString();
    }

    /**
     * Rebuild the {@code -F} form parts of a multipart request from its rendered
     * body. Regular fields become {@code name=value}; file parts become
     * {@code name=@filename;type=...}. JMeter does not keep the uploaded bytes,
     * so the file name is only a placeholder the user edits to a real path
     * before running the command.
     *
     * @return the form parts, or an empty list if the body cannot be parsed
     */
    private static List<String> parseMultipartForm(String contentType, String body) {
        String boundary = extractBoundary(contentType);
        if (StringUtilities.isBlank(boundary)) {
            return List.of();
        }
        MultipartUrlConfig multipart = new MultipartUrlConfig(boundary);
        try {
            multipart.parseArguments(body);
        } catch (RuntimeException e) { // NOSONAR malformed body: fall back to the omitted-body note
            return List.of();
        }
        List<String> parts = new ArrayList<>();
        for (JMeterProperty property : multipart.getArguments()) {
            Argument argument = (Argument) property.getObjectValue();
            parts.add(argument.getName() + "=" + argument.getValue()); //$NON-NLS-1$
        }
        for (HTTPFileArg file : multipart.getHTTPFileArgs().asArray()) {
            StringBuilder part = new StringBuilder();
            part.append(file.getParamName()).append("=@").append(file.getPath()); //$NON-NLS-1$
            if (StringUtilities.isNotEmpty(file.getMimeType())) {
                part.append(";type=").append(file.getMimeType()); //$NON-NLS-1$
            }
            parts.add(part.toString());
        }
        return parts;
    }

    /**
     * @return the {@code boundary} value of a multipart content type, or
     *         {@code null} if it is absent
     */
    private static String extractBoundary(String contentType) {
        int index = contentType.toLowerCase(Locale.ROOT).indexOf(BOUNDARY);
        if (index < 0) {
            return null;
        }
        String boundary = contentType.substring(index + BOUNDARY.length());
        int semicolon = boundary.indexOf(';');
        if (semicolon >= 0) {
            boundary = boundary.substring(0, semicolon);
        }
        return boundary.trim();
    }

    private static boolean containsPlaceholder(String body) {
        for (String placeholder : BODY_PLACEHOLDERS) {
            if (body.contains(placeholder)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wrap a value in single quotes for safe use in a POSIX shell, escaping any
     * embedded single quotes using the {@code '\''} idiom.
     *
     * @param value the value to quote
     * @return the shell-quoted value
     */
    private static String quote(String value) {
        return "'" + value.replace("'", "'\\''") + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

}
