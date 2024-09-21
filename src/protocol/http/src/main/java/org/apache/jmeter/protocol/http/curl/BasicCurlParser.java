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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.avalon.CLArgsParser;
import org.apache.commons.cli.avalon.CLOption;
import org.apache.commons.cli.avalon.CLOptionDescriptor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic cURL command parser that handles:
 *
 * @since 5.1
 */
public class BasicCurlParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicCurlParser.class);

    private static final int METHOD_OPT                 = 'X';// $NON-NLS-1$
    private static final int COMPRESSED_OPT             = 'c';// $NON-NLS-1$
    private static final int HEADER_OPT                 = 'H';// $NON-NLS-1$
    private static final int DATA_OPT                   = 'd';// $NON-NLS-1$
    private static final int DATA_ASCII_OPT             = "data-ascii".hashCode();// $NON-NLS-1$
    private static final int DATA_BINARY_OPT            = "data-binary".hashCode();// NOSONAR
    private static final int DATA_URLENCODE_OPT         = "data-urlencode".hashCode();// NOSONAR
    private static final int DATA_RAW_OPT               = "data-raw".hashCode();// NOSONAR
    private static final int FORM_OPT                   = 'F';// $NON-NLS-1$
    private static final int FORM_STRING_OPT            = "form".hashCode();// $NON-NLS-1$
    private static final int USER_AGENT_OPT             = 'A';// $NON-NLS-1$
    private static final int CONNECT_TIMEOUT_OPT        = "connect-timeout".hashCode();// $NON-NLS-1$
    private static final int COOKIE_OPT                 = 'b';// $NON-NLS-1$
    private static final int USER_OPT                   = 'u';// $NON-NLS-1$
    private static final int BASIC_OPT                  = "basic".hashCode();// NOSONAR
    private static final int DIGEST_OPT                 = "digest".hashCode();// NOSONAR
    private static final int CERT_OPT                   = 'E';// $NON-NLS-1$
    private static final int CAFILE_OPT                 = "cacert".hashCode();// $NON-NLS-1$
    private static final int CAPATH_OPT                 = "capath".hashCode();// $NON-NLS-1$
    private static final int CIPHERS_OPT                = "ciphers".hashCode();// $NON-NLS
    private static final int CERT_STATUS_OPT            = "cert-status".hashCode();// $NON-NLS-1$-1$
    private static final int CERT_TYPE_OPT              = "cert-type".hashCode();// $NON-NLS-1$-1$
    private static final int KEY_OPT                    = "key".hashCode();// $NON-NLS-1$-1$
    private static final int KEY_TYPE_OPT               = "key-type".hashCode();// $NON-NLS-1$-1$
    private static final int GET_OPT                    = 'G';// $NON-NLS-1$
    private static final int DNS_OPT                    = "dns-servers".hashCode();// $NON-NLS-1$
    private static final int NO_KEEPALIVE_OPT           = "no-keepalive".hashCode();// $NON-NLS-1$
    private static final int REFERER_OPT                = 'e';// $NON-NLS-1$
    private static final int LOCATION_OPT               = 'L';// $NON-NLS-1$
    private static final int INCLUDE_OPT                = 'i';// $NON-NLS-1$
    private static final int HEAD_OPT                   = 'I';// $NON-NLS-1$
    private static final int PROXY_OPT                  = 'x';// $NON-NLS-1$
    private static final int PROXY_USER_OPT             = 'U';// $NON-NLS-1$
    private static final int PROXY_NTLM_OPT             = "proxy-ntlm".hashCode();// $NON-NLS-1$
    private static final int PROXY_NEGOTIATE_OPT        = "proxy-negotiate".hashCode();// $NON-NLS-1$
    private static final int KEEPALIVETILE_OPT          = "keepalive-time".hashCode();// $NON-NLS-1$
    private static final int MAX_TIME_OPT               = 'm';// $NON-NLS-1$
    private static final int OUTPUT_OPT                 = 'o';// $NON-NLS-1$
    private static final int CREATE_DIRS_OPT            = "create-dir".hashCode();// $NON-NLS-1$
    private static final int INSECURE_OPT               = 'k';// $NON-NLS-1$
    private static final int RAW_OPT                    = "raw".hashCode();// $NON-NLS-1$
    private static final int INTERFACE_OPT              = "interface".hashCode();// $NON-NLS-1$
    private static final int DNS_RESOLVER_OPT           = "resolve".hashCode();// $NON-NLS-1$
    private static final int LIMIT_RATE_OPT             = "limit-rate".hashCode();// $NON-NLS-1$
    private static final int MAX_REDIRS_OPT             = "max-redirs".hashCode();// $NON-NLS-1$
    private static final int NOPROXY_OPT                = "noproxy".hashCode();// $NON-NLS-1$
    private static final int URL_OPT                    = "url".hashCode(); // $NON-NLS-1$
    private static final int VERBOSE_OPT                = 'v';// $NON-NLS-1$
    private static final int SILENT_OPT                 = 's';// $NON-NLS-1$

    private static final List<Integer> AUTH_OPT              = Arrays.asList(BASIC_OPT, DIGEST_OPT);
    private static final List<Integer> SSL_OPT               = Arrays.asList(CAFILE_OPT, CAPATH_OPT, CERT_OPT, CIPHERS_OPT,
            CERT_STATUS_OPT, CERT_TYPE_OPT, KEY_OPT, KEY_TYPE_OPT);
    private static final List<Integer> DATAS_OPT             = Arrays.asList(DATA_OPT, DATA_ASCII_OPT, DATA_BINARY_OPT,
            DATA_URLENCODE_OPT, DATA_RAW_OPT);
    private static final List<Integer> FORMS_OPT             = Arrays.asList(FORM_OPT, FORM_STRING_OPT);
    private static final List<Integer> IGNORE_OPTIONS_OPT    = Arrays.asList(OUTPUT_OPT, CREATE_DIRS_OPT, RAW_OPT,
            INCLUDE_OPT, KEEPALIVETILE_OPT, VERBOSE_OPT, SILENT_OPT);
    private static final List<Integer> NOSUPPORT_OPTIONS_OPT = Arrays.asList(PROXY_NTLM_OPT, PROXY_NEGOTIATE_OPT);
    private static final List<Integer> PROPERTIES_OPT        = Arrays.asList(MAX_REDIRS_OPT);
    private static final List<String> DYNAMIC_COOKIES        = Arrays.asList("PHPSESSID", "JSESSIONID", "ASPSESSIONID",
            "connect.sid");// $NON-NLS-1$

    public static final class Request {
        private boolean compressed;
        private String url;
        private final List<Pair<String, String>> headers = new ArrayList<>();
        private String method = "GET";
        private String postData;
        private String interfaceName;
        private double connectTimeout = -1;
        private String cookies = "";
        private String cookieInHeaders = "";
        private String filepathCookie="";
        private final Authorization authorization = new Authorization();
        private String caCert = "";
        private final List<Pair<String, ArgumentHolder>> formData = new ArrayList<>();
        private final List<Pair<String, String>> formStringData = new ArrayList<>();
        private final Set<String> dnsServers = new HashSet<>();
        private boolean isKeepAlive = true;
        private double maxTime = -1;
        private final List<String> optionsIgnored = new ArrayList<>();
        private final List<String> optionsNoSupport = new ArrayList<>();
        private final List<String> optionsInProperties = new ArrayList<>();
        private final Map<String, String> proxyServer = new LinkedHashMap<>();
        private String dnsResolver;
        private int limitRate = 0;
        private String noproxy;
        private static final List<String> HEADERS_TO_IGNORE = Arrays.asList("Connection", "Host");// $NON-NLS-1$
        private static final List<String> UNIQUE_HEADERS = Arrays.asList("user-agent"); // $NON-NLS-1$
        private static final int ONE_KILOBYTE_IN_CPS = 1024;
        public Request() {
            super();
        }

        /**
         * @return the HTTP method
         */
        public String getMethod() {
            return method;
        }

        /**
         * @param method the HTTP method to set
         */
        public void setMethod(String method) {
            this.method = method;
        }

        /**
         * @param value the post data
         */
        public void setPostData(String value) {
            if (StringUtils.isBlank(this.postData)) {
                this.postData = value;
            } else {
                this.postData = this.postData + "&" + value;
            }
        }

        /**
         * @return the postData
         */
        public String getPostData() {
            return postData;
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

        /**
         * @param name the field of Header
         * @param value the value of Header
         */
        public void addHeader(String name, String value) {
            if ("COOKIE".equalsIgnoreCase(name)) {
                this.cookieInHeaders = value;
            } else if (HEADERS_TO_IGNORE.contains(name)) {
                return;
            } else {
                if (UNIQUE_HEADERS.contains(name.toLowerCase(Locale.US))) {
                    headers.removeIf(p -> p.getLeft().equalsIgnoreCase(name));
                }
                headers.add(Pair.of(name, value));
            }
        }

        /**
         * <em>Note that {@link #setCookieInHeaders(String)} will have to be called first to set the cookies from headers.</em>
         *
         * @param url to extract domain and port for the cookie from
         * @return the extracted cookies in the earlier set headers
         */
        public List<Cookie> getCookieInHeaders(String url) {
            return Collections.unmodifiableList(stringToCookie(cookieInHeaders, url));
        }

        /**
         * @param cookieInHeaders the cookieInHeaders to set
         */
        public void setCookieInHeaders(String cookieInHeaders) {
            this.cookieInHeaders = cookieInHeaders;
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
        public List<Pair<String, String>> getHeaders() {
            return Collections.unmodifiableList(this.headers);
        }

        /**
         * @return the list of options which are ignored
         */
        public List<String> getOptionsInProperties() {
            return Collections.unmodifiableList(this.optionsInProperties);
        }

        /**
         * @param option the option
         */
        public void addOptionsInProperties(String option) {
            this.optionsInProperties.add(option);
        }

        /**
         * @return the maximum transfer rate
         */
        public int getLimitRate() {
            return limitRate;
        }

        /**
         * Transform the bandwidth to cps value (byte/s), cps =
         * bandwidth*1024/8, the unit of bandwidth in JMeter is measured in kbit/s. And
         * the speed in Curl is measured in bytes/second, so the conversion formula is
         * cps=limitRate*1024
         * @param limitRate the maximum transfer rate
         */
        public void setLimitRate(String limitRate) {
            String unit = limitRate.substring(limitRate.length() - 1, limitRate.length()).toLowerCase(Locale.ROOT);
            int value = Integer.parseInt(limitRate.substring(0, limitRate.length() - 1).toLowerCase(Locale.ROOT));
            switch (unit) {
            case "k":
                this.limitRate = value * ONE_KILOBYTE_IN_CPS;
                break;
            case "m":
                this.limitRate = value * ONE_KILOBYTE_IN_CPS * 1000;
                break;
            case "g":
                this.limitRate = value * ONE_KILOBYTE_IN_CPS * 1000000;
                break;
            default:
                break;
            }
        }

        /**
         * @return this list of hosts which don't use proxy
         */
        public String getNoproxy() {
            return noproxy;
        }

        /**
         * Set the list of hosts which don't use proxy
         * @param noproxy list of hosts that should not be used through the proxy
         */
        public void setNoproxy(String noproxy) {
            this.noproxy = noproxy;
        }

        /**
         * @return the DNS resolver
         */
        public String getDnsResolver() {
            return dnsResolver;
        }

        /**
         * set DNS resolver
         * @param dnsResolver name of the DNS resolver to use
         */
        public void setDnsResolver(String dnsResolver) {
            this.dnsResolver = dnsResolver;
        }

        /**
         * @return the interface name to perform an operation
         */
        public String getInterfaceName() {
            return interfaceName;
        }

        /**
         * @param interfaceName the name of interface
         */
        public void setInterfaceName(String interfaceName) {
            this.interfaceName = interfaceName;
        }

        /**
         * @return the list of options which are ignored
         */
        public List<String> getOptionsIgnored() {
            return Collections.unmodifiableList(this.optionsIgnored);
        }

        /**
         * @param option option is ignored
         */
        public void addOptionsIgnored(String option) {
            this.optionsIgnored.add(option);
        }

        /**
         * @return the list of options which are not supported by JMeter
         */
        public List<String> getOptionsNoSupport() {
            return Collections.unmodifiableList(this.optionsNoSupport);
        }

        /**
         * @param option option is not supported
         */
        public void addOptionsNoSupport(String option) {
            this.optionsNoSupport.add(option);
        }
        /**
         * @return the map of proxy server
         */
        public Map<String, String> getProxyServer() {
            return Collections.unmodifiableMap(this.proxyServer);
        }

        /**
         * @param key key
         * @param value value
         */
        public void setProxyServer(String key, String value) {
            this.proxyServer.put(key, value);
        }

        /**
         * @return if the Http request keeps alive
         */
        public boolean isKeepAlive() {
            return isKeepAlive;
        }

        /**
         * @param isKeepAlive set if the Http request keeps alive
         */
        public void setKeepAlive(boolean isKeepAlive) {
            this.isKeepAlive = isKeepAlive;
        }

        /**
         * @return the list of DNS server
         */
        public Set<String> getDnsServers() {
            return Collections.unmodifiableSet(this.dnsServers);
        }

        /**
         * @param dnsServer set the list of DNS server
         */
        public void addDnsServers(String dnsServer) {
            this.dnsServers.add(dnsServer);
        }

        /**
         * @return the map of form data
         */
        public List<Pair<String,String>> getFormStringData() {
            return Collections.unmodifiableList(this.formStringData);
        }

        /**
         * @param key   the key of form data
         * @param value the value of form data
         */
        public void addFormStringData(String key, String value) {
            formStringData.add(Pair.of(key, value));
        }

        /**
         * @return the map of form data
         */
        public List<Pair<String,ArgumentHolder>> getFormData() {
            return Collections.unmodifiableList(this.formData);
        }

        /**
         * @param key   the key of form data
         * @param value the value of form data
         */
        public void addFormData(String key, ArgumentHolder value) {
            formData.add(Pair.of(key, value));
        }

        /**
         * @return the certificate of the CA
         */
        public String getCaCert() {
            return caCert;
        }

        /**
         * the options which work for SSL
         * @param caCert cert of the CA
         */
        public void setCaCert(String caCert) {
            this.caCert = caCert;
        }

        /**
         * @return the authorization
         */
        public Authorization getAuthorization() {
            return authorization;
        }

        /**
         * @return the connection time out
         */
        public double getConnectTimeout() {
            return connectTimeout;
        }
        /**
         * @param connectTimeout the connection time out
         */
        public void setConnectTimeout(double connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        /**
         * @return the max time of connection
         */
        public double getMaxTime() {
            return maxTime;
        }

        /**
         * @param maxTime max time of connection
         */
        public void setMaxTime(double maxTime) {
            this.maxTime = maxTime;
        }

        /**
         * @return the filepathCookie
         */
        public String getFilepathCookie() {
            return filepathCookie;
        }

        /**
         * @param filepathCookie the filepathCookie to set
         */
        public void setFilepathCookie(String filepathCookie) {
            this.filepathCookie = filepathCookie;
        }

        /**
         * <em>Note that {@link #setCookies(String)} will have to be called first to set the cookies</em>
         * @param url to extract domain and port from
         * @return the cookies
         */
        public List<Cookie> getCookies(String url) {
            return Collections.unmodifiableList(stringToCookie(cookies, url));
        }

        /**
         * @param cookies the cookies to set
         */
        public void setCookies(String cookies) {
            this.cookies = cookies;
        }

        /* (non-Javadoc)
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

    private static final CLOptionDescriptor D_COMPRESSED_OPT =
            new CLOptionDescriptor("compressed", CLOptionDescriptor.ARGUMENT_DISALLOWED, COMPRESSED_OPT,
                    "Request compressed response (using deflate or gzip)");
    private static final CLOptionDescriptor D_HEADER_OPT =
            new CLOptionDescriptor("header", CLOptionDescriptor.ARGUMENT_REQUIRED | CLOptionDescriptor.DUPLICATES_ALLOWED, HEADER_OPT,
                    "Pass custom header LINE to server");
    private static final CLOptionDescriptor D_METHOD_OPT =
            new CLOptionDescriptor("request", CLOptionDescriptor.ARGUMENT_REQUIRED, METHOD_OPT,
                    "Pass custom header LINE to server");
    private static final CLOptionDescriptor D_DATA_OPT =
            new CLOptionDescriptor("data", CLOptionDescriptor.ARGUMENT_REQUIRED | CLOptionDescriptor.DUPLICATES_ALLOWED, DATA_OPT,
                    "HTTP POST data");
    private static final CLOptionDescriptor D_DATA_ASCII_OPT = new CLOptionDescriptor("data-ascii",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DATA_ASCII_OPT, "HTTP POST ascii data ");
    private static final CLOptionDescriptor D_DATA_BINARY_OPT = new CLOptionDescriptor("data-binary",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DATA_BINARY_OPT, "HTTP POST binary data ");
    private static final CLOptionDescriptor D_DATA_URLENCODE_OPT = new CLOptionDescriptor("data-urlencode",
            CLOptionDescriptor.ARGUMENT_REQUIRED | CLOptionDescriptor.DUPLICATES_ALLOWED, DATA_URLENCODE_OPT, "HTTP POST url encoding data ");
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
    private static final CLOptionDescriptor D_URL_OPT = new CLOptionDescriptor("url",
            CLOptionDescriptor.ARGUMENT_REQUIRED, URL_OPT, "url");
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
    private static final CLOptionDescriptor D_KEY_OPT = new CLOptionDescriptor("key",
            CLOptionDescriptor.ARGUMENT_REQUIRED, KEY_OPT,
            "Private key file name. Allows you to provide your private key in this separate file. ");
    private static final CLOptionDescriptor D_KEY_TYPE_OPT = new CLOptionDescriptor("key-type",
            CLOptionDescriptor.ARGUMENT_REQUIRED, KEY_TYPE_OPT,
            "Private key file type. Specify which type your --key provided private key is.");
    private static final CLOptionDescriptor D_GET_OPT = new CLOptionDescriptor("get",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, GET_OPT,
            "Put the post data in the url and use get to replace post. ");
    private static final CLOptionDescriptor D_DNS_SERVERS_OPT = new CLOptionDescriptor("dns-servers",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DNS_OPT, "Resolve host name over DOH. ");
    private static final CLOptionDescriptor D_NO_KEEPALIVE_OPT = new CLOptionDescriptor("no-keepalive",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, NO_KEEPALIVE_OPT, "Disabled keep-alive ");
    private static final CLOptionDescriptor D_LOCATION_OPT = new CLOptionDescriptor("location",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, LOCATION_OPT, "Follow Redirect ");
    private static final CLOptionDescriptor D_INCLUDE_OPT = new CLOptionDescriptor("include",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, INCLUDE_OPT, "Include the HTTP-header in the output ");
    private static final CLOptionDescriptor D_HEAD_OPT = new CLOptionDescriptor("head",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, HEAD_OPT, "Fetch the HTTP-header only");
    private static final CLOptionDescriptor D_INSECURE_OPT = new CLOptionDescriptor("insecure",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, INSECURE_OPT,
            "Allows curl to perform insecure SSL connections and transfers");
    private static final CLOptionDescriptor D_PROXY_OPT = new CLOptionDescriptor("proxy",
            CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_OPT,
            "Use the specified HTTP proxy. If the port number" + " is not specified, it is assumed at port 1080.");
    private static final CLOptionDescriptor D_PROXY_USER_OPT = new CLOptionDescriptor("proxy-user",
            CLOptionDescriptor.ARGUMENT_REQUIRED, PROXY_USER_OPT,
            "Specify user and password to use for proxy authentication.");
    private static final CLOptionDescriptor D_PROXY_NTLM_OPT = new CLOptionDescriptor("proxy-ntlm",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, PROXY_NTLM_OPT,
            "Tells curl to use HTTP ntlm authentication when communicating with the given proxy. ");
    private static final CLOptionDescriptor D_PROXY_NEGOTIATE_OPT = new CLOptionDescriptor("proxy-negotiate",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, PROXY_NEGOTIATE_OPT,
            "Tells curl to use HTTP negotiate authentication when communicating with the given proxy. ");
    private static final CLOptionDescriptor D_KEEPALIVETILE_OPT = new CLOptionDescriptor("keepalive-time",
            CLOptionDescriptor.ARGUMENT_REQUIRED, KEEPALIVETILE_OPT,
            " This option sets the  time  a  connection  needs  to  remain  idle  before  sending"
                    + " keepalive  probes and the time between individual keepalive probes..");
    private static final CLOptionDescriptor D_MAX_TIME_OPT = new CLOptionDescriptor("max-time",
            CLOptionDescriptor.ARGUMENT_REQUIRED, MAX_TIME_OPT,
            "Maximum time in seconds that you allow the whole operation to take. ");
    private static final CLOptionDescriptor D_OUTPUT_OPT = new CLOptionDescriptor("output",
            CLOptionDescriptor.ARGUMENT_REQUIRED, OUTPUT_OPT, "Write result to a file");
    private static final CLOptionDescriptor D_CREATE_DIRS_OPT = new CLOptionDescriptor("create-dir",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, CREATE_DIRS_OPT,
            "Create the necessary local directory hierarchy as needed for output file");
    private static final CLOptionDescriptor D_RAW_OPT = new CLOptionDescriptor("raw",
            CLOptionDescriptor.ARGUMENT_DISALLOWED, RAW_OPT,
            "When used, it disables all internal HTTP decoding of content or transfer encodings "
                    + "and instead makes them passed on unaltered raw. ");
    private static final CLOptionDescriptor D_INTERFACE_OPT = new CLOptionDescriptor("interface",
            CLOptionDescriptor.ARGUMENT_REQUIRED, INTERFACE_OPT, "Perform an operation using a specified interface");
    private static final CLOptionDescriptor D_DNS_RESOLVER_OPT = new CLOptionDescriptor("resolve",
            CLOptionDescriptor.ARGUMENT_REQUIRED, DNS_RESOLVER_OPT,
            "Provide a custom address for a specific host and port pair");
    private static final CLOptionDescriptor D_LIMIT_RATE_OPT = new CLOptionDescriptor("limit-rate",
            CLOptionDescriptor.ARGUMENT_REQUIRED, LIMIT_RATE_OPT,
            "Specify the maximum transfer rate you want curl to use");
    private static final CLOptionDescriptor D_MAX_REDIRS = new CLOptionDescriptor("max-redirs",
            CLOptionDescriptor.ARGUMENT_REQUIRED, MAX_REDIRS_OPT, "Set maximum number of redirections");
    private static final CLOptionDescriptor D_NOPROXY = new CLOptionDescriptor("noproxy",
            CLOptionDescriptor.ARGUMENT_REQUIRED, NOPROXY_OPT,
            "Comma-separated list of hosts which do not use a proxy, if one is specified. ");
    private static final CLOptionDescriptor D_SILENT = new CLOptionDescriptor("silent",
            CLOptionDescriptor.ARGUMENT_OPTIONAL, SILENT_OPT, "silent mode");
    private static final CLOptionDescriptor D_VERBOSE = new CLOptionDescriptor("verbose",
            CLOptionDescriptor.ARGUMENT_OPTIONAL, VERBOSE_OPT, "verbose mode");
    private static final Pattern deleteLinePattern = Pattern.compile("\r|\n|\r\n");

    private static final CLOptionDescriptor[] OPTIONS = new CLOptionDescriptor[] {
            D_COMPRESSED_OPT,D_HEADER_OPT, D_METHOD_OPT,D_DATA_OPT, D_DATA_ASCII_OPT, D_DATA_URLENCODE_OPT, D_DATA_RAW_OPT, D_DATA_BINARY_OPT,
            D_FORM_OPT, D_FORM_STRING_OPT, D_USER_AGENT_OPT, D_CONNECT_TIMEOUT_OPT, D_COOKIE_OPT, D_URL_OPT, D_USER_OPT,
            D_BASIC_OPT, D_DIGEST_OPT, D_CACERT_OPT, D_CAPATH_OPT, D_CERT_OPT, D_CERT_STATUS_OPT, D_CERT_TYPE_OPT,
            D_CIPHERS_OPT, D_KEY_OPT, D_KEY_TYPE_OPT, D_GET_OPT, D_DNS_SERVERS_OPT, D_NO_KEEPALIVE_OPT, D_REFERER_OPT,
            D_LOCATION_OPT, D_INCLUDE_OPT, D_INSECURE_OPT, D_HEAD_OPT, D_PROXY_OPT, D_PROXY_USER_OPT, D_PROXY_NTLM_OPT,
            D_PROXY_NEGOTIATE_OPT, D_KEEPALIVETILE_OPT, D_MAX_TIME_OPT, D_OUTPUT_OPT, D_CREATE_DIRS_OPT, D_RAW_OPT,
            D_INTERFACE_OPT, D_DNS_RESOLVER_OPT, D_LIMIT_RATE_OPT, D_MAX_REDIRS ,D_NOPROXY, D_VERBOSE, D_SILENT
    };

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
                if (option.getDescriptor().getId() == URL_OPT) {
                    request.setUrl(option.getArgument());
                } else if (option.getDescriptor().getId() == COMPRESSED_OPT) {
                    request.setCompressed(true);
                } else if (option.getDescriptor().getId() == HEADER_OPT) {
                    String nameAndValue = option.getArgument(0);
                    int indexOfColon = nameAndValue.indexOf(':');
                    if (indexOfColon >= 0) {
                        String name = nameAndValue.substring(0, indexOfColon).trim();
                        String value = nameAndValue.substring(indexOfColon + 1).trim();
                        request.addHeader(name, value);
                    } else if (nameAndValue.endsWith(";")) {
                            request.addHeader(nameAndValue.substring(0, nameAndValue.length() - 1), "");
                    } else {
                        LOGGER.warn("Could not parse header argument [{}] as it didn't contain a colon nor ended with a semicolon", nameAndValue);
                    }
                } else if (option.getDescriptor().getId() == METHOD_OPT) {
                    String value = option.getArgument(0);
                    request.setMethod(value);
                } else if (DATAS_OPT.contains(option.getDescriptor().getId())) {
                    String value = option.getArgument(0);
                    String dataOptionName = option.getDescriptor().getName();
                    if (value == null) {
                        value = "";
                    }
                    value = getPostDataByDifferentOption(value.trim(), dataOptionName);
                    if ("GET".equals(request.getMethod())) {
                        request.setMethod("POST");
                    }
                    request.setPostData(value);
                } else if (FORMS_OPT.contains(option.getDescriptor().getId())) {
                    String nameAndValue = option.getArgument(0);
                    int indexOfEqual = nameAndValue.indexOf('=');
                    String key = nameAndValue.substring(0, indexOfEqual).trim();
                    String value = nameAndValue.substring(indexOfEqual + 1).trim();
                    if ("form-string".equals(option.getDescriptor().getName())) {
                        request.addFormStringData(key, unquote(value));
                    } else {
                        if (value.charAt(0) == '@') {
                            request.addFormData(key, FileArgumentHolder.of(unquote(value.substring(1))));
                        } else {
                            request.addFormData(key, StringArgumentHolder.of(unquote(value)));
                        }
                    }
                    request.setMethod("POST");
                } else if (option.getDescriptor().getId() == USER_AGENT_OPT) {
                    request.addHeader("User-Agent", option.getArgument(0));
                } else if (option.getDescriptor().getId() == REFERER_OPT) {
                    request.addHeader("Referer", option.getArgument(0));
                } else if (option.getDescriptor().getId() == CONNECT_TIMEOUT_OPT) {
                    String value = option.getArgument(0);
                    request.setConnectTimeout(Double.parseDouble(value) * 1000);
                } else if (option.getDescriptor().getId() == COOKIE_OPT) {
                    String value = option.getArgument(0);
                    if (isValidCookie(value)) {
                        request.setCookies(value);
                    } else {
                        request.setFilepathCookie(value);
                    }
                } else if (option.getDescriptor().getId() == USER_OPT) {
                    String value = option.getArgument(0);
                    setAuthUserPasswd(value, request.getUrl(), request.getAuthorization());
                } else if (AUTH_OPT.contains(option.getDescriptor().getId())) {
                    String authOption = option.getDescriptor().getName();
                    setAuthMechanism(authOption, request.getAuthorization());
                } else if (SSL_OPT.contains(option.getDescriptor().getId())) {
                    request.setCaCert(option.getDescriptor().getName());
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
                } else if (option.getDescriptor().getId() == MAX_TIME_OPT) {
                    String value = option.getArgument(0);
                    request.setMaxTime(Double.parseDouble(value) * 1000);
                } else if (option.getDescriptor().getId() == HEAD_OPT) {
                    request.setMethod("HEAD");
                } else if (option.getDescriptor().getId() == INTERFACE_OPT) {
                    String value = option.getArgument(0);
                    request.setInterfaceName(value);
                } else if (option.getDescriptor().getId() == DNS_RESOLVER_OPT) {
                    String value = option.getArgument(0);
                    request.setDnsResolver(value);
                } else if (option.getDescriptor().getId() == LIMIT_RATE_OPT) {
                    String value = option.getArgument(0);
                    request.setLimitRate(value);
                } else if (option.getDescriptor().getId() == NOPROXY_OPT) {
                    String value = option.getArgument(0);
                    request.setNoproxy(value);
                } else if (IGNORE_OPTIONS_OPT.contains(option.getDescriptor().getId())) {
                    request.addOptionsIgnored(option.getDescriptor().getName());
                } else if (NOSUPPORT_OPTIONS_OPT.contains(option.getDescriptor().getId())) {
                    request.addOptionsNoSupport(option.getDescriptor().getName());
                } else if (PROPERTIES_OPT.contains(option.getDescriptor().getId())) {
                    request.addOptionsInProperties(
                            "--" + option.getDescriptor().getName() + " is in 'httpsampler.max_redirects(1062 line)'");
                } else if (option.getDescriptor().getId() == CLOption.TEXT_ARGUMENT
                        && !"CURL".equalsIgnoreCase(option.getArgument())) {
                    try {
                        request.setUrl(new URL(option.getArgument()).toExternalForm());
                    } catch (MalformedURLException ex) {
                        LOGGER.warn("Unhandled option {}", option.getArgument());
                    }
                }
            }
            if (isPostToGet) {
                String url = request.getUrl() + "?" + request.getPostData();
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
     * @return the command line broken into strings.
     * An empty or null toProcess parameter results in a zero sized array.
     */
    public static String[] translateCommandline(String toProcess) {
        if (toProcess == null || toProcess.isEmpty()) {
            //no command? no string
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
                    current.append(nextTok.replaceAll("^\\\\[\\r\\n]", ""));
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

    /**
    * Set the username , password and baseurl of authorization
    *
    * @param authentication   the username and password of authorization
    * @param url              the baseurl of authorization
    * @param authorization    the object of authorization
    */
   public void setAuthUserPasswd(String authentication, String url, Authorization authorization) {
       String[] authorizationParameters = authentication.split(":", 2);
       authorization.setUser(authorizationParameters[0].trim());
       authorization.setPass(authorizationParameters[1].trim());
       authorization.setURL(url);
   }

   /**
    * Set the mechanism of authorization
    *
    * @param mechanism     the mechanism of authorization
    * @param authorization the object of authorization
    */
   private static void setAuthMechanism(String mechanism, Authorization authorization) {
       switch (mechanism.toLowerCase(Locale.ROOT)) {
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

   /**
    * Set the parameters of proxy server in http request advanced
    *
    * @param request                       http request
    * @param originalProxyServerParameters the parameters of proxy server
    */
   private static void setProxyServer(Request request, String originalProxyServerParameters) {
       String proxyServerParameters = originalProxyServerParameters;
       if (!proxyServerParameters.contains("://")) {
           proxyServerParameters = "http://" + proxyServerParameters;
       }
       try {
           URI uriProxy = new URI(proxyServerParameters);
           request.setProxyServer("scheme", uriProxy.getScheme());
           Optional<String> userInfoOptional = Optional.ofNullable(uriProxy.getUserInfo());
           userInfoOptional.ifPresent(s -> setProxyServerUserInfo(request, s));
           Optional<String> hostOptional = Optional.ofNullable(uriProxy.getHost());
           hostOptional.ifPresent(s -> request.setProxyServer("servername", s));
           if (uriProxy.getPort() != -1) {
               request.setProxyServer("port", String.valueOf(uriProxy.getPort()));
           } else {
               request.setProxyServer("port", "1080");
           }
       } catch (URISyntaxException e) {
           throw new IllegalArgumentException(proxyServerParameters + " cannot be converted to a URL", e);
       }
   }

   /**
    * Set the username and password of proxy server
    *
    * @param request               http request
    * @param authentication        the username and password of proxy server
    */
   private static void setProxyServerUserInfo(Request request, String authentication) {
       if (authentication.contains(":")) {
           String[] userInfo = authentication.split(":", 2);
           request.setProxyServer("username", userInfo[0]);
           request.setProxyServer("password", userInfo[1]);
       }
   }

   /**
    * Get post data by different type of data option
    *
    * @param originalPostdata the post data
    * @param dataOptionName the different option of "--data"
    * @return the post data
    */
   private String getPostDataByDifferentOption(final String originalPostdata, String dataOptionName) {
       String postdata = originalPostdata;
       if ("data-urlencode".equals(dataOptionName)) {
           postdata = encodePostdata(postdata);
       } else {
           if (postdata.charAt(0) == '@' && !"data-raw".equals(dataOptionName)) {
               postdata = unquote(postdata.substring(1, postdata.length()));
               postdata = readFromFile(postdata);
               if (!"data-binary".equals(dataOptionName)) {
                   postdata = deleteLineBreak(postdata);
               }
           }
       }
       return postdata;
   }

   private String unquote(String value) {
       LoggerFactory.getLogger(this.getClass()).debug("Unquote {}", value);
       if (value.charAt(0) == '"') {
           String result = value.substring(1, value.length() - 1);
           return result.replaceAll("\\\\(.)", "$1");
       }
       return value;
   }

   /**
    * Encode the post data
    *
    * @param postdata the post data
    * @return the result of encoding
    */
    private String encodePostdata(String postdata) {
        if (postdata.contains("@")) {
            String contentFile = null;
            String[] arr = postdata.split("@", 2);
            String dataToEncode = readFromFile(unquote(arr[1]));
            try {
                contentFile = URLEncoder.encode(dataToEncode, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException(dataToEncode + " cannot be encoded", e);
            }
            if (!arr[0].isEmpty()) {
                contentFile = arr[0] + "=" + contentFile;
            }
            return contentFile;
        } else {
            if (!postdata.contains("=")) {
                try {
                    return URLEncoder.encode(postdata, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(postdata + " cannot be encoded", e);
                }
            } else {
                int index = postdata.indexOf('=');
                try {
                    return postdata.substring(0, index + 1) + URLEncoder
                            .encode(postdata.substring(index + 1, postdata.length()), StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(
                            postdata.substring(index + 1, postdata.length()) + " cannot be encoded", e);
                }
            }
        }
    }

   /**
    * Read the postdata from file
    *
    * @param filePath
    * @return the content of file
    */
   private static String readFromFile(String filePath) {
       File file = new File(filePath.trim());
       if (file.isFile() && file.exists()) {
           try {
               return FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
           } catch (IOException e) {
               LOGGER.error("Failed to read from File {}", filePath, e);
               throw new IllegalArgumentException("Failed to read from File " + filePath);
           }
       } else {
           throw new IllegalArgumentException(filePath + " is a directory or does not exist");
       }
   }

    private static String deleteLineBreak(String postdata) {
        Matcher m = deleteLinePattern.matcher(postdata);
        return m.replaceAll("");
    }

   /**
    * Verify if the string is cookie or filename
    * @param str the cookie to check
    * @return Whether the format of the string is cookie
    */
    public static boolean isValidCookie(String str) {
        for (String r : str.split(";")) {
            if (!r.contains("=")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert string to cookie
     *
     * @param cookieStr the cookie as a string
     * @param url to extract domain and path for the cookie from
     * @return list of cookies
     */
   public static List<Cookie> stringToCookie(String cookieStr, String url) {
       List<Cookie> cookies = new ArrayList<>();
       final StringTokenizer tok = new StringTokenizer(cookieStr, "; ", true);
        while (tok.hasMoreTokens()) {
            String nextCookie = tok.nextToken();
            if (nextCookie.contains("=")) {
                String[] cookieParameters = nextCookie.split("=", 2);
                if (!DYNAMIC_COOKIES.contains(cookieParameters[0])) {
                    Cookie newCookie = new Cookie();
                    newCookie.setName(cookieParameters[0]);
                    newCookie.setValue(cookieParameters[1]);
                    URL newUrl;
                    try {
                        newUrl = new URL(url.trim());
                        newCookie.setDomain(newUrl.getHost());
                        newCookie.setPath(newUrl.getPath());
                        cookies.add(newCookie);
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException(
                                "unqualified url " + url.trim() + ", unable to create cookies.");
                    }
                }
            }
        }
       return cookies;
   }
}
