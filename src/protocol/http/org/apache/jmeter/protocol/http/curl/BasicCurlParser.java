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
package org.apache.jmeter.protocol.http.curl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.cli.avalon.CLArgsParser;
import org.apache.commons.cli.avalon.CLOption;
import org.apache.commons.cli.avalon.CLOptionDescriptor;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.Cookie;

/**
 * Basic cURL command parser that handles: -X -H --compressed --data POST with
 * Body data
 * 
 * @since 5.1
 */
public class BasicCurlParser {
    private static final int METHOD_OPT = 'X';
    private static final int COMPRESSED_OPT = 'c';// $NON-NLS-1$
    private static final int HEADER_OPT = 'H';// $NON-NLS-1$
    private static final int DATA_OPT = 'd';// $NON-NLS-1$
    private static final int USER_AGENT_OPT = 'A';// $NON-NLS-1$
    private static final int CONNECT_TIMEOUT_OPT = "connect_timeout".hashCode();// $NON-NLS-1$
    private static final int COOKIE_OPT = 'b';// $NON-NLS-1$
    private static final int USER_OPT = 'u';// $NON-NLS-1$
    private static final int BASIC_OPT = "basic".hashCode();// $NON-NLS-1$
    private static final int DIGEST_OPT = "digest".hashCode();// $NON-NLS-1$
    private static final List<Integer> AUTH_OPT = new ArrayList<>();// $NON-NLS-1$
    static {
        AUTH_OPT.add(BASIC_OPT);
        AUTH_OPT.add(DIGEST_OPT);
    }

    public static final class Request {
        private boolean compressed;
        private String url;
        private Map<String, String> headers = new LinkedHashMap<>();
        private String method = "GET";
        private String postData;
        private String connectTimeout = "";
        private List<Cookie> cookies = new ArrayList<>();
        private Authorization autorization = new Authorization();

        public Request() {
            super();
        }

        public String getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(String connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public List<Cookie> getCookies() {
            return cookies;
        }

        public void setCookies(List<Cookie> cookies) {
            this.cookies = cookies;
        }

        public Authorization getAutorization() {
            return autorization;
        }

        /**
         * @return the compressed
         */
        public boolean isCompressed() {
            return compressed;
        }

        /**
         * @param compressed the compressed to set
         */
        public void setCompressed(boolean compressed) {
            this.compressed = compressed;
        }

        public void addHeader(String name, String value) {
            headers.put(name, value);
        }

        /**
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * @param url the url to set
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * @return the headers
         */
        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getMethod() {
            return method;
        }

        /**
         * @param method the method to set
         */
        public void setMethod(String method) {
            this.method = method;
        }

        public void setPostData(String value) {
            this.postData = value;
        }

        /**
         * @return the postData
         */
        public String getPostData() {
            return postData;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Request [compressed=");
            builder.append(compressed);
            builder.append(", url=");
            builder.append(url);
            builder.append(", method=");
            builder.append(method);
            builder.append(", headers=");
            builder.append(headers);
            builder.append("]");
            return builder.toString();
        }
    }

    private static final CLOptionDescriptor D_COMPRESSED_OPT = new CLOptionDescriptor("compressed",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, COMPRESSED_OPT,
            "Request compressed response (using deflate or gzip)");
    private static final CLOptionDescriptor D_HEADER_OPT = new CLOptionDescriptor("header",
            CLOptionDescriptor.ARGUMENT_REQUIRED | CLOptionDescriptor.DUPLICATES_ALLOWED, HEADER_OPT,
            "Pass custom header LINE to server");
    private static final CLOptionDescriptor D_METHOD_OPT = new CLOptionDescriptor("command",
            CLOptionDescriptor.ARGUMENT_REQUIRED, METHOD_OPT, "Pass custom header LINE to server");
    private static final CLOptionDescriptor D_DATA_OPT = new CLOptionDescriptor("data",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DATA_OPT, "HTTP POST data");
    private static final CLOptionDescriptor D_USER_AGENT_OPT = new CLOptionDescriptor("user-agent",
            CLOptionDescriptor.ARGUMENT_REQUIRED, USER_AGENT_OPT, "The User-Agent string");
    private static final CLOptionDescriptor D_CONNECT_TIMEOUT_OPT = new CLOptionDescriptor("connect-timeout",
            CLOptionDescriptor.ARGUMENT_REQUIRED, CONNECT_TIMEOUT_OPT,
            "Maximum time in seconds that the connection to the server");
    private static final CLOptionDescriptor D_COOKIE_OPT = new CLOptionDescriptor("cookie",
            CLOptionDescriptor.ARGUMENT_REQUIRED, COOKIE_OPT, "Pass the data to the HTTP server as a cookie");
    private static final CLOptionDescriptor D_USER_OPT = new CLOptionDescriptor("user",
            CLOptionDescriptor.ARGUMENT_REQUIRED, USER_OPT, "User and password to use for server authentication. ");
    private static final CLOptionDescriptor D_BASIC_OPT = new CLOptionDescriptor("basic",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, BASIC_OPT, "HTTP Basic authentication ");
    private static final CLOptionDescriptor D_DIGEST_OPT = new CLOptionDescriptor("digest",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, DIGEST_OPT, "HTTP digest authentication ");
    private static final CLOptionDescriptor[] OPTIONS = new CLOptionDescriptor[] { D_COMPRESSED_OPT, D_HEADER_OPT,
            D_METHOD_OPT, D_DATA_OPT, D_USER_AGENT_OPT, D_CONNECT_TIMEOUT_OPT, D_COOKIE_OPT, D_USER_OPT, D_BASIC_OPT,
            D_DIGEST_OPT };

    public BasicCurlParser() {
        super();
    }

    public Request parse(String commandLine) {
        String[] args = translateCommandline(commandLine);
        CLArgsParser parser = new CLArgsParser(args, OPTIONS);
        String error = parser.getErrorString();
        if (error == null) {
            List<CLOption> clOptions = parser.getArguments();
            Request request = new Request();
            for (CLOption option : clOptions) {
                if (option.getDescriptor().getId() == CLOption.TEXT_ARGUMENT) {
                    // Curl or URL
                    if (!"CURL".equalsIgnoreCase(option.getArgument())) {
                        request.setUrl(option.getArgument());
                        continue;
                    }
                } else if (option.getDescriptor().getId() == COMPRESSED_OPT) {
                    request.setCompressed(true);
                } else if (option.getDescriptor().getId() == HEADER_OPT) {
                    String nameAndValue = option.getArgument(0);
                    int indexOfSemicolon = nameAndValue.indexOf(':');
                    String name = nameAndValue.substring(0, indexOfSemicolon).trim();
                    String value = nameAndValue.substring(indexOfSemicolon + 1).trim();
                    request.addHeader(name, value);
                } else if (option.getDescriptor().getId() == METHOD_OPT) {
                    String value = option.getArgument(0);
                    request.setMethod(value);
                } else if (option.getDescriptor().getId() == DATA_OPT) {
                    String value = option.getArgument(0);
                    request.setMethod("POST");
                    request.setPostData(value);
                } else if (option.getDescriptor().getId() == USER_AGENT_OPT) {
                    String name = "User-Agent";
                    String value = option.getArgument(0);
                    request.addHeader(name, value);
                } else if (option.getDescriptor().getId() == CONNECT_TIMEOUT_OPT) {
                    String value = option.getArgument(0);
                    request.setConnectTimeout(value);
                } else if (option.getDescriptor().getId() == COOKIE_OPT) {
                    String value = option.getArgument(0);
                    request.setCookies(this.stringToCookie(value, request.getUrl()));
                } else if (option.getDescriptor().getId() == USER_OPT) {
                    String value = option.getArgument(0);
                    setAuthUserPasswd(value, request.getUrl(), request.getAutorization());
                } else if (AUTH_OPT.contains(option.getDescriptor().getId())) {
                    String authOption = option.getDescriptor().getName();
                    setAuthMechanism(authOption, request.getAutorization());
                }
            }
            return request;
        } else {
            throw new IllegalArgumentException(
                    "Unexpected format for command line:" + commandLine + ", error:" + error);
        }
    }

    /**
     * Crack a command line.
     * 
     * @param toProcess the command line to process.
     * @return the command line broken into strings. An empty or null toProcess
     *         parameter results in a zero sized array.
     */
    public static String[] translateCommandline(String toProcess) {
        if (toProcess == null || toProcess.isEmpty()) {
            // no command? no string
            return new String[0];
        }
        // parse with a simple finite state machine
        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        final ArrayList<String> result = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;
        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
            case inQuote:
                if ("\'".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            case inDoubleQuote:
                if ("\"".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            default:
                if ("\'".equals(nextTok)) {
                    state = inQuote;
                } else if ("\"".equals(nextTok)) {
                    state = inDoubleQuote;
                } else if (" ".equals(nextTok)) {
                    if (lastTokenHasBeenQuoted || current.length() > 0) {
                        result.add(current.toString());
                        current.setLength(0);
                    }
                } else {
                    current.append(nextTok);
                }
                lastTokenHasBeenQuoted = false;
                break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() > 0) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new IllegalArgumentException("unbalanced quotes in " + toProcess);
        }
        return result.toArray(new String[result.size()]);
    }

    public List<Cookie> stringToCookie(String cookieStr, String url) {
        List<Cookie> cookies = new ArrayList<>();
        final StringTokenizer tok = new StringTokenizer(cookieStr, "; ", true);
        while (tok.hasMoreTokens()) {
            String nextCookie = tok.nextToken();
            if (nextCookie.contains("=")) {
                String[] cookieParameters = nextCookie.split("=");
                Cookie newCookie = new Cookie();
                newCookie.setName(cookieParameters[0]);
                newCookie.setValue(cookieParameters[1]);
                URL newUrl;
                try {
                    newUrl = new URL(url);
                    newCookie.setDomain(newUrl.getHost());
                    newCookie.setPath(newUrl.getPath());
                    cookies.add(newCookie);
                } catch (MalformedURLException e) {
                    throw new IllegalArgumentException("unqualified url");
                }
            }
        }
        return cookies;
    }

    public void setAuthUserPasswd(String autorizationStr, String url, Authorization authorization) {
        String[] autorizationParameters = autorizationStr.split(":");
        authorization.setUser(autorizationParameters[0].trim());
        authorization.setPass(autorizationParameters[1].trim());
        authorization.setURL(url);
    }

    private void setAuthMechanism(String authOption, Authorization authorization) {
        switch (authOption) {
        case ("basic"):
            authorization.setMechanism(Mechanism.BASIC);
            break;
        case ("digest"):
            authorization.setMechanism(Mechanism.DIGEST);
            break;
        default:
            break;
        }
    }
}
