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

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.DateUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Handles HTTP Caching.
 */
public class CacheManager extends ConfigTestElement implements TestStateListener, TestIterationListener, Serializable {

    private static final long serialVersionUID = 236L;

    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);

    @SuppressWarnings("JavaUtilDate")
    private static final Date EXPIRED_DATE = new Date(0L);
    private static final int DEFAULT_MAX_SIZE = 5000;
    private static final long ONE_YEAR_MS = 365*24*60*60*1000L;
    private static final String[] CACHEABLE_METHODS = JMeterUtils.getPropDefault("cacheable_methods", "GET").split("[ ,]");
    private static final String CONTROLLED_BY_THREAD = "CacheManager.controlledByThread";// $NON-NLS-1$

    static {
        if (log.isInfoEnabled()) {
            log.info("Will only cache the following methods: {}", Arrays.toString(CACHEABLE_METHODS));
        }
    }
    //+ JMX attributes, do not change values
    public static final String CLEAR = "clearEachIteration"; // $NON-NLS-1$
    public static final String USE_EXPIRES = "useExpires"; // $NON-NLS-1$
    public static final String MAX_SIZE = "maxSize";  // $NON-NLS-1$
    //-

    private transient InheritableThreadLocal<Cache<String, CacheEntry>> threadCache;

    private transient boolean useExpires; // Cached value

    /**
     * used to share the cache between 2 cache managers
     * @see CacheManager#createCacheManagerProxy()
     * @since 3.0 */
    private transient Cache<String, CacheEntry> localCache;

    public CacheManager() {
        setProperty(new BooleanProperty(CLEAR, false));
        setProperty(new BooleanProperty(USE_EXPIRES, false));
        clearCache();
        useExpires = false;
    }

    CacheManager(Cache<String, CacheEntry> localCache, boolean useExpires) {
        this.localCache = localCache;
        this.useExpires = useExpires;
    }
    public boolean getControlledByThread() {
        return getPropertyAsBoolean(CONTROLLED_BY_THREAD);
    }

    public void setControlledByThread(boolean control) {
        setProperty(new BooleanProperty(CONTROLLED_BY_THREAD, control));
    }

    /**
     * Holder for storing cache details.
     * Perhaps add original response later?
     */
    static class CacheEntry {
        // The class is package-protected to allow access by unit-test cases
        private final String lastModified;
        private final String etag;
        private final Date expires;
        private final String varyHeader;

        /**
         * Deprecated Constructor for a CacheEntry
         * @param lastModified formatted string containing the last modification time of the http response
         * @param expires formatted string containing the expiration time of the http response
         * @param etag of the http response
         * @deprecated use {@link CacheEntry#CacheEntry(String lastModified, Date expires, String etag, String varyHeader)} instead
         */
        @Deprecated
        public CacheEntry(String lastModified, Date expires, String etag) {
            this.lastModified = lastModified;
            this.etag = etag;
            this.expires = expires;
            this.varyHeader = null;
        }

        /**
         * Constructor for a CacheEntry
         * @param lastModified formatted string containing the last modification time of the http response
         * @param expires formatted string containing the expiration time of the http response
         * @param etag of the http response
         * @param varyHeader formatted string containing the vary header entries
         */
        public CacheEntry(String lastModified, Date expires, String etag, String varyHeader) {
            this.lastModified = lastModified;
            this.etag = etag;
            this.expires = expires;
            this.varyHeader = varyHeader;
        }

        public String getLastModified() {
            return lastModified;
        }

        public String getEtag() {
            return etag;
        }

        public Date getExpires() {
            return expires;
        }

        public String getVaryHeader() {
            return varyHeader;
        }

        @Override
        public String toString() {
            return "CacheEntry [lastModified=" + lastModified + ", etag=" + etag + ", expires=" + expires
                    + ", varyHeader=" + varyHeader + "]";
        }
    }

    /**
     * Provides read access to the headers of an HTTP response, independently of the
     * HTTP client implementation that produced it.
     *
     * @since 6.0
     */
    @FunctionalInterface
    public interface ResponseHeaderSource {
        /**
         * @param name name of the header (case handling is up to the implementation)
         * @return value of the header or {@code null} if the response has no such header
         */
        String getHeader(String name);
    }

    /**
     * Provides read access to the headers of an HTTP request, independently of the
     * HTTP client implementation that will send it.
     *
     * @since 6.0
     */
    @FunctionalInterface
    public interface RequestHeaderSource {
        /**
         * Passes every header of the request, including repeated ones, to the given action.
         *
         * @param action receives the name and the value of each header
         */
        void forEachHeader(BiConsumer<String, String> action);
    }

    /**
     * An HTTP request whose headers can be read and to which conditional request headers
     * (like {@code If-Modified-Since}) can be added.
     *
     * @since 6.0
     */
    public interface RequestHeaderSink extends RequestHeaderSource {
        /**
         * Sets the given header on the request, replacing a previously set one.
         *
         * @param name name of the header
         * @param value value of the header
         */
        void setHeader(String name, String value);
    }

    /**
     * Combines a {@link RequestHeaderSource} and a header setter into a {@link RequestHeaderSink}.
     *
     * @param headers headers currently present on the request
     * @param headerSetter sets a header on the request
     * @return sink that reads from {@code headers} and writes through {@code headerSetter}
     * @since 6.0
     */
    public static RequestHeaderSink requestHeaderSink(RequestHeaderSource headers,
            BiConsumer<String, String> headerSetter) {
        return new RequestHeaderSink() {
            @Override
            public void forEachHeader(BiConsumer<String, String> action) {
                headers.forEachHeader(action);
            }

            @Override
            public void setHeader(String name, String value) {
                headerSetter.accept(name, value);
            }
        };
    }

    /**
     * Save the Last-Modified, Etag, and Expires headers if the result is cacheable.
     * Version for Java implementation.
     * @param conn connection
     * @param res result
     */
    public void saveDetails(URLConnection conn, HTTPSampleResult res){
        saveDetails(conn::getHeaderField, res, conn.getURL().toString());
    }

    /**
     * Save the Last-Modified, Etag, and Expires headers if the result is cacheable.
     *
     * @param response headers of the response to extract the cache information from
     * @param res result to decide if the result is cacheable
     * @since 6.0
     */
    public void saveDetails(ResponseHeaderSource response, HTTPSampleResult res) {
        saveDetails(response, res, res.getUrlAsString());
    }

    private void saveDetails(ResponseHeaderSource response, HTTPSampleResult res, String url) {
        final String varyHeader = response.getHeader(HTTPConstants.VARY);
        if (isCacheable(res, varyHeader)) {
            String lastModified = response.getHeader(HTTPConstants.LAST_MODIFIED);
            String expires = response.getHeader(HTTPConstants.EXPIRES);
            String etag = response.getHeader(HTTPConstants.ETAG);
            String cacheControl = response.getHeader(HTTPConstants.CACHE_CONTROL);
            String date = response.getHeader(HTTPConstants.DATE);
            if (anyNotBlank(lastModified, expires, etag, cacheControl)) {
                setCache(lastModified, cacheControl, expires, etag, url, date,
                        getVaryHeader(varyHeader, requestHeadersOf(res.getRequestHeaders())));
            }
        }
    }

    private static boolean anyNotBlank(String... values) {
        for (String value: values) {
            if (StringUtilities.isNotBlank(value)) {
                return true;
            }
        }
        return false;
    }

    private static Map.Entry<String, String> getVaryHeader(String headerName, RequestHeaderSource reqHeaders) {
        if (headerName == null) {
            return null;
        }
        final var names = new HashSet<>(Arrays.asList(headerName.split(",\\s*")));
        final var values = new HashMap<String, List<String>>();
        for (final String name: names) {
            values.put(name, new ArrayList<>());
        }
        reqHeaders.forEachHeader((name, value) -> {
            List<String> valuesForName = values.get(name);
            if (valuesForName != null) {
                log.debug("Found vary value {}: {} for {} in request", name, value, headerName);
                valuesForName.add(value);
            }
        });
        return new AbstractMap.SimpleEntry<>(headerName, values.toString());
    }

    /**
     * Save the Last-Modified, Etag, and Expires headers if the result is
     * cacheable. Version for Apache HttpClient 4 implementation.
     *
     * @param method
     *            {@link HttpResponse} to extract header information from
     * @param res
     *            result to decide if result is cacheable
     * @deprecated use {@link #saveDetails(ResponseHeaderSource, HTTPSampleResult)} instead
     */
    @Deprecated
    public void saveDetails(HttpResponse method, HTTPSampleResult res) {
        saveDetails(responseHeadersOf(method), res);
    }

    // helper method to save the cache entry
    private void setCache(String lastModified, String cacheControl, String expires,
            String etag, String url, String date, Map.Entry<String, String> varyHeader) {
        log.debug("setCache({}, {}, {}, {}, {}, {}, {})", lastModified,
                cacheControl, expires, etag, url, date, varyHeader);
        Date expiresDate = null; // i.e. not using Expires
        if (useExpires) {// Check that we are processing Expires/CacheControl
            final String maxAge = "max-age=";

            if(cacheControl != null && cacheControl.contains("no-store")) {
                // We must not store an CacheEntry, otherwise a
                // conditional request may be made
                return;
            }
            if (expires != null) {
                expiresDate = extractExpiresDateFromExpires(expires);
            }
            // if no-cache is present, ensure that expiresDate remains null, which forces revalidation
            if(cacheControl == null || !cacheControl.contains("no-cache")) {
                expiresDate = extractExpiresDateFromCacheControl(lastModified,
                        cacheControl, expires, etag, url, date, maxAge, expiresDate);
                // else expiresDate computed in (expires!=null) condition is used
            }
        }
        Cache<String, CacheEntry> cache = getCache();
        if (varyHeader != null) {
            if (log.isDebugEnabled()) {
                log.debug("Set entry into cache for url {} and vary {} ({})", url,
                        varyHeader,
                        varyUrl(url, varyHeader.getKey(), varyHeader.getValue()));
            }
            cache.put(url, new CacheEntry(lastModified, expiresDate, etag, varyHeader.getKey()));
            cache.put(varyUrl(url, varyHeader.getKey(), varyHeader.getValue()), new CacheEntry(lastModified, expiresDate, etag, null));
        } else if (cache.getIfPresent(url) == null) {
            CacheEntry cacheEntry = new CacheEntry(lastModified, expiresDate, etag, null);
            log.debug("Set entry {} into cache for url {}", url, cacheEntry);
            cache.put(url, cacheEntry);
        }
    }

    private static Date extractExpiresDateFromExpires(String expires) {
        Date expiresDate;
        try {
            expiresDate = org.apache.http.client.utils.DateUtils
                    .parseDate(expires);
        } catch (IllegalArgumentException e) { // Exception handled by return
            if (log.isDebugEnabled()) {
                log.debug("Unable to parse Expires: '{}', exception: {}", expires, e);
            }
            expiresDate = CacheManager.EXPIRED_DATE; // invalid dates must be
                                                     // treated as expired
        }
        return expiresDate;
    }

    @SuppressWarnings("JavaUtilDate")
    private static Date extractExpiresDateFromCacheControl(String lastModified,
            String cacheControl, String expires, String etag, String url,
            String date, final String maxAge, Date defaultExpiresDate) {
        // the max-age directive overrides the Expires header,
        if (cacheControl != null && cacheControl.contains(maxAge)) {
            long maxAgeInSecs = Long.parseLong(cacheControl
                    .substring(cacheControl.indexOf(maxAge) + maxAge.length())
                    .split("[, ]")[0] // Bug 51932 - allow for optional trailing
                                      // attributes
            );
            return new Date(System.currentTimeMillis() + maxAgeInSecs * 1000);

        } else if (expires == null) { // No max-age && No expires
            return calcExpiresDate(lastModified, cacheControl, expires, etag,
                    url, date);
        }
        return defaultExpiresDate;
    }

    @SuppressWarnings("JavaUtilDate")
    private static Date calcExpiresDate(String lastModified, String cacheControl,
            String expires, String etag, String url, String date) {
        if (StringUtilities.isNotEmpty(lastModified) && StringUtilities.isNotEmpty(date)) {
            try {
                Date responseDate = DateUtils.parseDate(date);
                Date lastModifiedAsDate = DateUtils.parseDate(lastModified);
                // see https://developer.mozilla.org/en/HTTP_Caching_FAQ
                // see http://www.ietf.org/rfc/rfc2616.txt#13.2.4
                return new Date(System.currentTimeMillis() + Math.round(
                        (responseDate.getTime() - lastModifiedAsDate.getTime())
                                * 0.1));
            } catch(IllegalArgumentException e) {
                // date or lastModified may be null or in bad format
                if(log.isWarnEnabled()) {
                    log.warn("Failed computing expiration date with following info:"
                        +lastModified + ","
                        + cacheControl + ","
                        + expires + ","
                        + etag + ","
                        + url + ","
                        + date);
                }
                // TODO Can't see anything in SPEC
                return new Date(System.currentTimeMillis() + ONE_YEAR_MS);
            }
        } else {
            // TODO Can't see anything in SPEC
            return new Date(System.currentTimeMillis() + ONE_YEAR_MS);
        }
    }

    // Apache HttpClient 4
    private static ResponseHeaderSource responseHeadersOf(HttpResponse response) {
        return name -> {
            org.apache.http.Header hdr = response.getLastHeader(name);
            return hdr != null ? hdr.getValue() : null;
        };
    }

    /**
     * Is the sample result OK to cache?
     * i.e is it in the 2xx range or equal to 304, and is it a cacheable method?
     */
    private static boolean isCacheable(HTTPSampleResult res, String varyHeader){
        if ("*".equals(varyHeader)) {
            return false;
        }
        final String responseCode = res.getResponseCode();
        return isCacheableMethod(res)
                && (("200".compareTo(responseCode) <= 0  // $NON-NLS-1$
                    && "299".compareTo(responseCode) >= 0)  // $NON-NLS-1$
                    || "304".equals(responseCode));  // $NON-NLS-1$
    }

    private static boolean isCacheableMethod(HTTPSampleResult res) {
        final String resMethod = res.getHTTPMethod();
        for(String method : CACHEABLE_METHODS) {
            if (method.equalsIgnoreCase(resMethod)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the cache, and if there is a match, set the headers:
     * <ul>
     * <li>If-Modified-Since</li>
     * <li>If-None-Match</li>
     * </ul>
     *
     * @param url {@link URL} to look up in cache
     * @param request request to read the headers from and to set the conditional headers on
     * @since 6.0
     */
    public void setHeaders(URL url, RequestHeaderSink request) {
        CacheEntry entry = getEntry(url.toString(), request);
        if (log.isDebugEnabled()) {
            log.debug("setHeaders for URL:{} Entry:{}", url, entry);
        }
        if (entry != null) {
            final String lastModified = entry.getLastModified();
            if (lastModified != null) {
                request.setHeader(HTTPConstants.IF_MODIFIED_SINCE, lastModified);
            }
            final String etag = entry.getEtag();
            if (etag != null) {
                request.setHeader(HTTPConstants.IF_NONE_MATCH, etag);
            }
        }
    }

    /**
     * Check the cache, and if there is a match, set the headers:
     * <ul>
     * <li>If-Modified-Since</li>
     * <li>If-None-Match</li>
     * </ul>
     * Apache HttpClient 4 version.
     * @param url {@link URL} to look up in cache
     * @param request where to set the headers
     * @deprecated use {@link #setHeaders(URL, RequestHeaderSink)} instead
     */
    @Deprecated
    public void setHeaders(URL url, HttpRequestBase request) {
        setHeaders(url, requestHeaderSink(requestHeadersOf(request.getAllHeaders()), request::setHeader));
    }

    /**
     * Check the cache, and if there is a match, set the headers:
     * <ul>
     * <li>If-Modified-Since</li>
     * <li>If-None-Match</li>
     * </ul>
     * @param url {@link URL} to look up in cache
     * @param headers Array of {@link org.apache.jmeter.protocol.http.control.Header}
     * @param conn where to set the headers
     */
    public void setHeaders(HttpURLConnection conn,
            org.apache.jmeter.protocol.http.control.Header[] headers, URL url) {
        if (log.isDebugEnabled()) {
            log.debug("setHeaders HTTP Method{}(Java) url:{}", conn.getRequestMethod(), url);
        }
        setHeaders(url, requestHeaderSink(requestHeadersOf(headers), conn::addRequestProperty));
    }

    /**
     * Check the cache, if the entry has an expires header and the entry has not
     * expired, return <code>true</code><br>
     *
     * @param url
     *            {@link URL} to look up in cache
     * @return <code>true</code> if entry has an expires header and the entry
     *         has not expired, else <code>false</code>
     * @deprecated use a version of {@link CacheManager#inCache(URL, RequestHeaderSource)}
     *             or
     *             {@link CacheManager#inCache(URL, org.apache.jmeter.protocol.http.control.Header[])}
     */
    @Deprecated
    public boolean inCache(URL url) {
        return entryStillValid(url, getEntry(url.toString(), null));
    }

    /**
     * Check whether the URL has a valid entry for the supplied request headers.
     *
     * @param url {@link URL} to look up in cache
     * @param allHeaders headers of the request that would be sent
     * @return {@code true} if the matching entry has not expired
     * @since 6.0
     */
    public boolean inCache(URL url, RequestHeaderSource allHeaders) {
        return entryStillValid(url, getEntry(url.toString(), allHeaders));
    }

    /**
     * Check whether the URL has a valid entry for the supplied HttpClient 4 request headers.
     *
     * @param url {@link URL} to look up in cache
     * @param allHeaders request headers
     * @return {@code true} if the matching entry has not expired
     * @deprecated use {@link #inCache(URL, RequestHeaderSource)} instead
     */
    @Deprecated
    public boolean inCache(URL url, org.apache.http.Header[] allHeaders) {
        return inCache(url, requestHeadersOf(allHeaders));
    }

    public boolean inCache(URL url, org.apache.jmeter.protocol.http.control.Header[] allHeaders) {
        return inCache(url, requestHeadersOf(allHeaders));
    }

    private static RequestHeaderSource requestHeadersOf(
            org.apache.jmeter.protocol.http.control.Header[] allHeaders) {
        if (allHeaders == null) {
            return action -> { /* no headers to iterate over */ };
        }
        return action -> {
            for (org.apache.jmeter.protocol.http.control.Header header : allHeaders) {
                action.accept(header.getName(), header.getValue());
            }
        };
    }

    private static RequestHeaderSource requestHeadersOf(String allHeaders) {
        return action -> {
            for (String line : allHeaders.split("\\n")) {
                String[] nameAndValue = line.split(": ", 2);
                if (nameAndValue.length == 2) {
                    action.accept(nameAndValue[0], nameAndValue[1]);
                }
            }
        };
    }

    // Apache HttpClient 4
    private static RequestHeaderSource requestHeadersOf(org.apache.http.Header[] allHeaders) {
        return action -> {
            for (org.apache.http.Header header : allHeaders) {
                action.accept(header.getName(), header.getValue());
            }
        };
    }

    @SuppressWarnings("JavaUtilDate")
    private static boolean entryStillValid(URL url, CacheEntry entry) {
        log.debug("Check if entry {} is still valid for url {}", entry, url);
        if (entry != null && entry.getVaryHeader() == null) {
            final Date expiresDate = entry.getExpires();
            if (expiresDate != null) {
                if (expiresDate.after(new Date())) {
                    log.debug("Expires= {} (Valid) for url {}", expiresDate, url);
                    return true;
                } else {
                    log.debug("Expires= {} (Expired) for url {}", expiresDate, url);
                }
            } else {
                log.debug("expiresDate is null for url {}", url);
            }
        }
        return false;
    }

    private CacheEntry getEntry(String url, RequestHeaderSource headers) {
        CacheEntry entry = getCache().getIfPresent(url);
        log.debug("getEntry url:{} entry:{}", url, entry);
        if (entry == null) {
            log.debug("No entry found for url {}", url);
            return null;
        }
        if (entry.getVaryHeader() == null) {
            log.debug("Entry {} with no vary found for url {}", entry, url);
            return entry;
        }
        if (headers == null) {
            if(log.isDebugEnabled()) {
                log.debug("Entry {} found, but it should depend on vary {} for url {}", entry, entry.getVaryHeader(), url);
            }
            return null;
        }
        Map.Entry<String, String> varyPair = getVaryHeader(entry.getVaryHeader(), headers);
        if (varyPair != null) {
            if(log.isDebugEnabled()) {
                log.debug("Looking again for {} because of {} with vary: {} ({})", url, entry, entry.getVaryHeader(), varyPair);
            }
            return getEntry(varyUrl(url, entry.getVaryHeader(), varyPair.getValue()), null);
        }
        return null;
    }

    private static String varyUrl(String url, String headerName, String headerValue) {
        return "vary-" + headerName + "-" + headerValue + "-" + url;
    }

    private Cache<String, CacheEntry> getCache() {
        return localCache != null ? localCache : threadCache.get();
    }

    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR, clear));
    }

    public boolean getUseExpires() {
        return getPropertyAsBoolean(USE_EXPIRES);
    }

    public void setUseExpires(boolean expires) {
        setProperty(new BooleanProperty(USE_EXPIRES, expires));
    }

    /**
     * @return int cache max size
     */
    public int getMaxSize() {
        return getPropertyAsInt(MAX_SIZE, DEFAULT_MAX_SIZE);
    }

    /**
     * @param size int cache max size
     */
    public void setMaxSize(int size) {
        setProperty(MAX_SIZE, size, DEFAULT_MAX_SIZE);
    }


    @Override
    public void clear(){
        super.clear();
        clearCache();
    }

    private void clearCache() {
        log.debug("Clear cache");
        // TODO: avoid re-creating the thread local every time, reset its contents instead
        threadCache = new InheritableThreadLocal<Cache<String, CacheEntry>>(){
            @Override
            protected Cache<String, CacheEntry> initialValue() {
                return Caffeine.newBuilder()
                        .maximumSize(getMaxSize())
                        .build();
            }
        };
    }

    /**
     * create a cache manager that share the underlying cache of the current one
     * it allows to use the same cache in different threads which does not inherit from each other
     * @return a cache manager that share the underlying cache of the current one
     * @since 3.0
     */
    public CacheManager createCacheManagerProxy() {
        return new CacheManager(getCache(), this.useExpires);
    }

    @Override
    public void testStarted() {
    }

    @Override
    public void testEnded() {
    }

    @Override
    public void testStarted(String host) {
    }

    @Override
    public void testEnded(String host) {
    }

    @Override
    public void testIterationStart(LoopIterationEvent event) {
        JMeterVariables jMeterVariables = JMeterContextService.getContext().getVariables();
        if ((getControlledByThread() && !jMeterVariables.isSameUserOnNextIteration())
                || (!getControlledByThread() && getClearEachIteration())) {
            clearCache();
        }
        useExpires = getUseExpires(); // cache the value
    }

}
