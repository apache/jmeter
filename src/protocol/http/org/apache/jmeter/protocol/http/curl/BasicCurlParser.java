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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.avalon.CLArgsParser;
import org.apache.commons.cli.avalon.CLOption;
import org.apache.commons.cli.avalon.CLOptionDescriptor;
import org.apache.commons.io.FileUtils;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic cURL command parser that handles: -X -H --compressed --data POST with
 * Body data
 * 
 * @since 5.1
 */
public class BasicCurlParser {
    private static final Logger log = LoggerFactory.getLogger(BasicCurlParser.class);
    private static final int METHOD_OPT = 'X';
    private static final int COMPRESSED_OPT = 'c';// $NON-NLS-1$
    private static final int HEADER_OPT = 'H';// $NON-NLS-1$
    private static final int DATA_OPT = 'd';// $NON-NLS-1$
    private static final int DATA_ASCII_OPT = "data-ascii".hashCode();// $NON-NLS-1$
    private static final int DATA_BINARY_OPT = "data-binary".hashCode();// $NON-NLS-1$
    private static final int DATA_URLENCODE_OPT = "data-urlencode".hashCode();// $NON-NLS-1$
    private static final int DATA_RAW_OPT = "data-raw".hashCode();// $NON-NLS-1$
    private static final int FORM_OPT = 'F';// $NON-NLS-1$
    private static final int FORM_STRING_OPT = "form".hashCode();// $NON-NLS-1$
    private static final int USER_AGENT_OPT = 'A';// $NON-NLS-1$
    private static final int CONNECT_TIMEOUT_OPT = "connect-timeout".hashCode();// $NON-NLS-1$
    private static final int COOKIE_OPT = 'b';// $NON-NLS-1$
    private static final int USER_OPT = 'u';// $NON-NLS-1$
    private static final int BASIC_OPT = "basic".hashCode();// $NON-NLS-1$
    private static final int DIGEST_OPT = "digest".hashCode();// $NON-NLS-1$
    private static final int CERT_OPT = 'E';// $NON-NLS-1$
    private static final int CAFILE_OPT = "cacert".hashCode();// $NON-NLS-1$
    private static final int CAPATH_OPT = "capath".hashCode();// $NON-NLS-1$
    private static final int CIPHERS_OPT = "ciphers".hashCode();// $NON-NLS
    private static final int CERT_STATUS_OPT = "cert-status".hashCode();// $NON-NLS-1$-1$
    private static final int CERT_TYPE_OPT = "cert-type".hashCode();// $NON-NLS-1$-1$
    private static final int GET_OPT = 'G';// $NON-NLS-1$
    private static final int DNS_OPT = "dns-servers".hashCode();// $NON-NLS-1$
    private static final int NO_KEEPALIVE_OPT = "no-keepalive".hashCode();// $NON-NLS-1$
    private static final int REFERER_OPT = 'e';// $NON-NLS-1$
    private static final int LOCATION_OPT = 'L';// $NON-NLS-1$
    private static final int INCLUDE_OPT = 'i';// $NON-NLS-1$
    private static final int HEAD_OPT = 'I';// $NON-NLS-1$
    private static final int PROXY_OPT = 'x';// $NON-NLS-1$
    private static final int PROXY_USER_OPT = 'U';// $NON-NLS-1$
    private static final int KEEPALIVETILE_OPT = "keepalive-time".hashCode();// $NON-NLS-1$
    private static final int MAX_TIME_OPT = 'm';// $NON-NLS-1$
    private static final List<Integer> AUTH_OPT = new ArrayList<>();// $NON-NLS-1$
    static {
        AUTH_OPT.add(BASIC_OPT);
        AUTH_OPT.add(DIGEST_OPT);
    }
    private static final List<Integer> SSL_OPT = new ArrayList<>();// $NON-NLS-1$
    static {
        SSL_OPT.add(CAFILE_OPT);
        SSL_OPT.add(CAPATH_OPT);
        SSL_OPT.add(CERT_OPT);
        SSL_OPT.add(CIPHERS_OPT);
        SSL_OPT.add(CERT_STATUS_OPT);
        SSL_OPT.add(CERT_TYPE_OPT);
    }
    private static final List<Integer> DATAS_OPT = new ArrayList<>();// $NON-NLS-1$
    static {
        DATAS_OPT.add(DATA_OPT);
        DATAS_OPT.add(DATA_ASCII_OPT);
        DATAS_OPT.add(DATA_BINARY_OPT);
        DATAS_OPT.add(DATA_URLENCODE_OPT);
        DATAS_OPT.add(DATA_RAW_OPT);
    }
    private static final List<Integer> FORMS_OPT = new ArrayList<>();// $NON-NLS-1$
    static {
        FORMS_OPT.add(FORM_OPT);
        FORMS_OPT.add(FORM_STRING_OPT);
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
        private String cacert = "";
        private Map<String, String> formData = new LinkedHashMap<>();
        private Map<String, String> formStringData = new LinkedHashMap<>();
        private List<String> dnsServers = new ArrayList<>();
        private boolean isKeepAlive = true;
        private String maxTime = null;

        public String getMaxTime() {
            return maxTime;
        }

        public void setMaxTime(String maxTime) {
            this.maxTime = maxTime;
        }

        public Request() {
            super();
        }


        private Map<String, String> proxyServer = new LinkedHashMap<>();

        public Map<String, String> getProxyServer() {
            return proxyServer;
        }

        public void setProxyServer(Map<String, String> proxyServer) {
            this.proxyServer = proxyServer;
        }

        public boolean isKeepAlive() {
            return isKeepAlive;
        }

        public void setKeepAlive(boolean isKeepAlive) {
            this.isKeepAlive = isKeepAlive;
        }

        public List<String> getDnsServers() {
            return dnsServers;
        }

        public void addDnsServers(String dnsServer) {
            this.dnsServers.add(dnsServer);
        }

        public Map<String, String> getFormStringData() {
            return formStringData;
        }

        public void addFormStringData(String key, String value) {
            formStringData.put(key, value);
        }

        public Map<String, String> getFormData() {
            return formData;
        }

        public void addFormData(String key, String value) {
            formData.put(key, value);
        }

        public String getConnectTimeout() {
            return connectTimeout;
        }

        public String getCacert() {
            return cacert;
        }

        public void setCacert(String cacert) {
            this.cacert = cacert;
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
    private static final CLOptionDescriptor D_DATA_ASCII_OPT = new CLOptionDescriptor("data-ascii",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DATA_ASCII_OPT, "HTTP POST ascii data ");
    private static final CLOptionDescriptor D_DATA_BINARY_OPT = new CLOptionDescriptor("data-binary",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DATA_BINARY_OPT, "HTTP POST binary data ");
    private static final CLOptionDescriptor D_DATA_URLENCODE_OPT = new CLOptionDescriptor("data-urlencode",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DATA_URLENCODE_OPT, "HTTP POST url encoding data ");
    private static final CLOptionDescriptor D_DATA_RAW_OPT = new CLOptionDescriptor("data-raw",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DATA_RAW_OPT, "HTTP POST url allowed '@' ");
    private static final CLOptionDescriptor D_FORM_OPT = new CLOptionDescriptor("form",
            CLOptionDescriptor.ARGUMENT_REQUIRED | CLOptionDescriptor.DUPLICATES_ALLOWED, FORM_OPT,
            "HTTP POST form data allowed '@' and ';Type='");
    private static final CLOptionDescriptor D_FORM_STRING_OPT = new CLOptionDescriptor("form-string",
            CLOptionDescriptor.ARGUMENT_REQUIRED | CLOptionDescriptor.DUPLICATES_ALLOWED, FORM_STRING_OPT,
            "HTTP POST form data  ");
    private static final CLOptionDescriptor D_USER_AGENT_OPT = new CLOptionDescriptor("user-agent",
            CLOptionDescriptor.ARGUMENT_REQUIRED, USER_AGENT_OPT, "The User-Agent string");
    private static final CLOptionDescriptor D_CONNECT_TIMEOUT_OPT = new CLOptionDescriptor("connect-timeout",
            CLOptionDescriptor.ARGUMENT_REQUIRED, CONNECT_TIMEOUT_OPT,
            "Maximum time in seconds that the connection to the server");
    private static final CLOptionDescriptor D_REFERER_OPT = new CLOptionDescriptor("referer",
            CLOptionDescriptor.ARGUMENT_REQUIRED, REFERER_OPT,
            "Sends the 'Referer Page' information to the HTTP server ");
    private static final CLOptionDescriptor D_COOKIE_OPT = new CLOptionDescriptor("cookie",
            CLOptionDescriptor.ARGUMENT_REQUIRED, COOKIE_OPT, "Pass the data to the HTTP server as a cookie");
    private static final CLOptionDescriptor D_USER_OPT = new CLOptionDescriptor("user",
            CLOptionDescriptor.ARGUMENT_REQUIRED, USER_OPT, "User and password to use for server authentication. ");
    private static final CLOptionDescriptor D_BASIC_OPT = new CLOptionDescriptor("basic",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, BASIC_OPT, "HTTP Basic authentication ");
    private static final CLOptionDescriptor D_DIGEST_OPT = new CLOptionDescriptor("digest",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, DIGEST_OPT, "HTTP digest authentication ");
    private static final CLOptionDescriptor D_CERT_OPT = new CLOptionDescriptor("cert",
            CLOptionDescriptor.ARGUMENT_REQUIRED, CERT_OPT, " The specified client certificate file for SSL");
    private static final CLOptionDescriptor D_CACERT_OPT = new CLOptionDescriptor("cacert",
            CLOptionDescriptor.ARGUMENT_REQUIRED, CAFILE_OPT,
            "Use the specified certificate file to verify the peer. ");
    private static final CLOptionDescriptor D_CAPATH_OPT = new CLOptionDescriptor("capath",
            CLOptionDescriptor.ARGUMENT_REQUIRED, CAPATH_OPT,
            "Use the specified certificate directory to verify the peer. ");
    private static final CLOptionDescriptor D_CIPHERS_OPT = new CLOptionDescriptor("ciphers",
            CLOptionDescriptor.ARGUMENT_REQUIRED, CIPHERS_OPT, "The ciphers to use in the connection. ");
    private static final CLOptionDescriptor D_CERT_STATUS_OPT = new CLOptionDescriptor("cert-status",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, CERT_STATUS_OPT, "Tells curl to verify the status of the server "
                    + "certificate by using the Certificate Status Request TLS extension. ");
    private static final CLOptionDescriptor D_CERT_TYPE_OPT = new CLOptionDescriptor("cert-type",
            CLOptionDescriptor.ARGUMENT_REQUIRED, CERT_TYPE_OPT, "Tells curl the type of certificate type of the "
                    + "provided certificate. PEM, DER and ENG are recognized types ");
    private static final CLOptionDescriptor D_GET_OPT = new CLOptionDescriptor("get",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, GET_OPT,
            "Put the post data in the url and use get to replace post. ");
    private static final CLOptionDescriptor D_DNS_OPT = new CLOptionDescriptor("dns-servers",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DNS_OPT, "Resolve host name over DOH. ");
    private static final CLOptionDescriptor D_NO_KEEPALIVE_OPT = new CLOptionDescriptor("no-keepalive",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, NO_KEEPALIVE_OPT, "Disabled keep-alive ");
    private static final CLOptionDescriptor D_LOCATION_OPT = new CLOptionDescriptor("location",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, LOCATION_OPT, "Follow Redirect ");
    private static final CLOptionDescriptor D_INCLUDE_OPT = new CLOptionDescriptor("include",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, INCLUDE_OPT, "Include the HTTP-header in the output ");
    private static final CLOptionDescriptor D_HEAD_OPT = new CLOptionDescriptor("head",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, HEAD_OPT, "Fetch the HTTP-header only");
    private static final CLOptionDescriptor D_PROXY_OPT = new CLOptionDescriptor("proxy",
            CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_OPT,
            "Use the specified HTTP proxy. If the port number" + " is not specified, it is assumed at port 1080.");
    private static final CLOptionDescriptor D_PROXY_USER_OPT = new CLOptionDescriptor("proxy-user",
            CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_USER_OPT,
            "Use the specified HTTP proxy. If the port number is not specified, it is assumed at port 1080.");
    private static final CLOptionDescriptor D_KEEPALIVETILE_OPT = new CLOptionDescriptor("keepalive-time",
            CLOptionDescriptor.ARGUMENT_REQUIRED, KEEPALIVETILE_OPT,
            " This option sets the  time  a  connection  needs  to  remain  idle  before  sending"
            + " keepalive  probes and the time between individual keepalive probes..");
    private static final CLOptionDescriptor D_MAX_TIME_OPT = new CLOptionDescriptor("max-time",
            CLOptionDescriptor.ARGUMENT_REQUIRED, MAX_TIME_OPT,"Maximum time in seconds that you allow the whole operation to take. ");
    private static final CLOptionDescriptor[] OPTIONS = new CLOptionDescriptor[] { D_COMPRESSED_OPT, D_HEADER_OPT,
            D_METHOD_OPT, D_DATA_OPT, D_DATA_ASCII_OPT, D_DATA_URLENCODE_OPT, D_DATA_RAW_OPT, D_DATA_BINARY_OPT,
            D_FORM_OPT, D_FORM_STRING_OPT, D_USER_AGENT_OPT, D_CONNECT_TIMEOUT_OPT, D_COOKIE_OPT, D_USER_OPT,
            D_BASIC_OPT, D_DIGEST_OPT, D_CACERT_OPT, D_CAPATH_OPT, D_CERT_OPT, D_CERT_STATUS_OPT, D_CERT_TYPE_OPT,
            D_CIPHERS_OPT, D_GET_OPT, D_DNS_OPT, D_NO_KEEPALIVE_OPT, D_REFERER_OPT, D_LOCATION_OPT, D_INCLUDE_OPT,
            D_HEAD_OPT, D_PROXY_OPT,D_PROXY_USER_OPT,D_KEEPALIVETILE_OPT,D_MAX_TIME_OPT};

    public BasicCurlParser() {
        super();
    }

    public Request parse(String commandLine) {
        String[] args = translateCommandline(commandLine);
        CLArgsParser parser = new CLArgsParser(args, OPTIONS);
        String error = parser.getErrorString();
        boolean isPostToGet = false;
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
                } else if (DATAS_OPT.contains(option.getDescriptor().getId())) {
                    String value = option.getArgument(0);
                    String dataOptionName = option.getDescriptor().getName();
                    value = getDifferentValueByOptionName(value, dataOptionName);
                    request.setMethod("POST");
                    request.setPostData(value);
                } else if (FORMS_OPT.contains(option.getDescriptor().getId())) {
                    String nameAndValue = option.getArgument(0);
                    int indexOfSemicolon = nameAndValue.indexOf('=');
                    String key = nameAndValue.substring(0, indexOfSemicolon).trim();
                    String value = nameAndValue.substring(indexOfSemicolon + 1).trim();
                    if (option.getDescriptor().getName().equals("form-string")) {
                        request.addFormStringData(key, value);
                    } else {
                        request.addFormData(key, value);
                    }
                } else if (option.getDescriptor().getId() == USER_AGENT_OPT) {
                    String name = "User-Agent";
                    String value = option.getArgument(0);
                    request.addHeader(name, value);
                } else if (option.getDescriptor().getId() == REFERER_OPT) {
                    String name = "Referer";
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
                } else if (SSL_OPT.contains(option.getDescriptor().getId())) {
                    request.setCacert(option.getDescriptor().getName());
                } else if (option.getDescriptor().getId() == GET_OPT) {
                    isPostToGet = true;
                } else if (option.getDescriptor().getId() == DNS_OPT) {
                    String value = option.getArgument(0);
                    String[] dnsServer = value.split(",");
                    for (String s : dnsServer) {
                        request.addDnsServers(s);
                    }
                } else if (option.getDescriptor().getId() == NO_KEEPALIVE_OPT) {
                    request.setKeepAlive(false);
                } else if (option.getDescriptor().getId() == PROXY_OPT) {
                    String value = option.getArgument(0);
                    setProxyServer(request, value);
                } else if (option.getDescriptor().getId() == PROXY_USER_OPT) {
                    String value = option.getArgument(0);
                    setProxyServerUserInfo(request, value);
                } else if (option.getDescriptor().getId() == KEEPALIVETILE_OPT) {
                    String value = option.getArgument(0);
                    request.addHeader("Keep-Alive", "timeout=" + value);
                } else if (option.getDescriptor().getId() == MAX_TIME_OPT) {
                    String value = option.getArgument(0);
                    request.setMaxTime(value);
                }
            }
            if (isPostToGet) {
                String url = request.getUrl();
                url += "?" + request.getPostData();
                request.setUrl(url);
                request.setPostData(null);
                request.setMethod("GET");
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
        case "basic":
            authorization.setMechanism(Mechanism.BASIC);
            break;
        case "digest":
            authorization.setMechanism(Mechanism.DIGEST);
            break;
        default:
            break;
        }
    }

    private void setProxyServer(Request request, String value) {
        if (!value.contains("://")) {
            value = "http://" + value;
        }
        URI uriProxy = null;
        try {
            uriProxy = new URI(value);
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
        request.getProxyServer().put("scheme", uriProxy.getScheme());
        Optional<String> userInfoOptional = Optional.ofNullable(uriProxy.getUserInfo());
        if (userInfoOptional.isPresent()) {
            String userinfo = userInfoOptional.get();
            if (userinfo.contains(":")) {
                String[] userInfo = userinfo.split(":");
                request.getProxyServer().put("username", userInfo[0]);
                request.getProxyServer().put("password", userInfo[1]);
            }
        }
        Optional<String> hostOptional = Optional.ofNullable(uriProxy.getHost());
        if (hostOptional.isPresent()) {
            request.getProxyServer().put("servername", hostOptional.get());
        }
        if (uriProxy.getPort() != -1) {
            request.getProxyServer().put("port", String.valueOf(uriProxy.getPort()));
        } else {
            request.getProxyServer().put("port", "1080");
        }
    }
    private void setProxyServerUserInfo(Request request, String value) {
        if (value.contains(":")) {
            String[] userInfo = value.split(":");
            request.getProxyServer().put("username", userInfo[0]);
            request.getProxyServer().put("password", userInfo[1]);
        }
        
    }

    private String getDifferentValueByOptionName(String value, String dataOptionName) {
        if (value.contains("@") && !dataOptionName.equals("data-raw")) {
            value = value.replace("@", "");
            value = readFromFile(value);
            if (!dataOptionName.equals("data-binary")) {
                value = replaceSpecialStr(value);
            }
        }
        if (dataOptionName.equals("data-urlencode")) {
            value = encodeUrl(value);
        }
        return value;
    }

    private String encodeUrl(String value) {
        if (!value.contains("=")) {
            try {
                value = URLEncoder.encode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage());
            }
            return value;
        } else {
            Map<String, String> res = urlRequest(value);
            StringBuilder urlAfterEncoding = new StringBuilder();
            for (Map.Entry<String, String> entry : res.entrySet()) {
                urlAfterEncoding.append(entry.getKey());
                urlAfterEncoding.append("=");
                try {
                    urlAfterEncoding.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    log.error(e.getMessage());
                }
                urlAfterEncoding.append("&");
            }
            return urlAfterEncoding.substring(0, urlAfterEncoding.length() - 1);
        }
    }

    public static Map<String, String> urlRequest(String strUrlParam) {
        Map<String, String> mapRequest = new HashMap<>();
        String[] arrSplit = null;
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");
            if (arrSplitEqual.length > 1) {
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            }
        }
        return mapRequest;
    }

    public static String readFromFile(String filePath) {
        String encoding = "UTF-8";
        String myValue = "";
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            try {
                myValue = FileUtils.readFileToString(file, encoding);
            } catch (IOException e) {
                log.error("Failed to read from File");
            }
        }
        return myValue;
    }

    public static String replaceSpecialStr(String str) {
        String repl = "";
        if (str != null) {
            Pattern p = Pattern.compile("\r|\n|\r\n");
            Matcher m = p.matcher(str);
            repl = m.replaceAll("");
        }
        return repl;
    }
}
