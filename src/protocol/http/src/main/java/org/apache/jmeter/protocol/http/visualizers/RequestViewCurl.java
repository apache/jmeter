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

package org.apache.jmeter.protocol.http.visualizers;

import java.awt.BorderLayout;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.apache.jmeter.gui.util.JSyntaxSearchToolBar;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.RequestView;
import org.apache.jorphan.util.StringUtilities;

import com.google.auto.service.AutoService;

/**
 * Panel that renders an HTTP request as a ready-to-run {@code curl} command,
 * so it can be copied and pasted into a console or shared with a developer.
 */
@AutoService(RequestView.class)
public class RequestViewCurl implements RequestView {

    // Used by Request Panel
    static final String KEY_LABEL = "view_results_table_request_tab_curl"; //$NON-NLS-1$

    private static final String NEWLINE = " \\\n"; //$NON-NLS-1$

    /**
     * Headers that must not be reproduced in the curl command: curl generates
     * them itself, or they are connection-specific (hop-by-hop) headers that are
     * forbidden in HTTP/2 and would make the request fail with a protocol error.
     */
    private static final Set<String> SKIPPED_HEADERS = Set.of(
            "content-length", //$NON-NLS-1$
            "connection", //$NON-NLS-1$
            "keep-alive", //$NON-NLS-1$
            "proxy-connection", //$NON-NLS-1$
            "transfer-encoding", //$NON-NLS-1$
            "upgrade"); //$NON-NLS-1$

    private JSyntaxTextArea curlData;

    private JPanel panel;

    @Override
    public void init() {
        panel = new JPanel(new BorderLayout(0, 5));
        curlData = JSyntaxTextArea.getInstance(20, 80, true);
        curlData.setEditable(false);
        curlData.setLineWrap(true);
        curlData.setWrapStyleWord(true);
        panel.add(new JSyntaxSearchToolBar(curlData).getToolBar(), BorderLayout.NORTH);
        panel.add(JTextScrollPane.getInstance(curlData), BorderLayout.CENTER);
    }

    @Override
    public void clearData() {
        curlData.setInitialText(""); //$NON-NLS-1$
    }

    @Override
    public void setSamplerResult(Object objectResult) {
        if (objectResult instanceof HTTPSampleResult sampleResult) {
            curlData.setInitialText(buildCurlCommand(sampleResult));
            curlData.setCaretPosition(0);
        } else {
            // add a message when no http sample (ex. Java request)
            curlData.setInitialText(JMeterUtils.getResString("view_results_table_request_http_nohttp")); //$NON-NLS-1$
        }
    }

    /**
     * Build a {@code curl} command line that reproduces the given HTTP request.
     *
     * @param sampleResult the sampled HTTP request
     * @return the curl command as a string
     */
    static String buildCurlCommand(HTTPSampleResult sampleResult) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("curl"); //$NON-NLS-1$

        String method = sampleResult.getHTTPMethod();
        if (StringUtilities.isNotBlank(method)) {
            sb.append(" -X ").append(quote(method)); //$NON-NLS-1$
        }

        URL url = sampleResult.getURL();
        if (url != null) {
            sb.append(NEWLINE).append("  ").append(quote(url.toString()));
        }

        boolean hasCookieHeader = false;
        String requestHeaders = sampleResult.getRequestHeaders();
        if (StringUtilities.isNotEmpty(requestHeaders)) {
            LinkedHashMap<String, String> headers = JMeterUtils.parseHeaders(requestHeaders);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String name = entry.getKey();
                if (StringUtilities.isBlank(name) || SKIPPED_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                    continue;
                }
                if (HTTPConstants.HEADER_COOKIE.equalsIgnoreCase(name)) {
                    hasCookieHeader = true;
                }
                sb.append(NEWLINE).append("  -H ").append(quote(name + ": " + entry.getValue())); //$NON-NLS-1$
            }
        }

        // Cookies are tracked separately in JMeter; only add them if they were
        // not already emitted as a Cookie header above.
        String cookies = sampleResult.getCookies();
        if (!hasCookieHeader && StringUtilities.isNotEmpty(cookies)) {
            sb.append(NEWLINE).append("  -b ").append(quote(cookies)); //$NON-NLS-1$
        }

        String body = sampleResult.getQueryString();
        if (StringUtilities.isNotBlank(body)) {
            sb.append(NEWLINE).append("  --data-raw ").append(quote(body)); //$NON-NLS-1$
        }

        return sb.toString();
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

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public String getLabel() {
        return JMeterUtils.getResString(KEY_LABEL);
    }

}
