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

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles HTTP Caching.
 */
public class CacheManager extends ConfigTestElement implements TestStateListener, TestIterationListener, Serializable {

    private static final long serialVersionUID = 235L;

    private static final Logger log = LoggerFactory.getLogger(CacheManager.class);

    private static final Date EXPIRED_DATE = new Date(0L);
    private static final int DEFAULT_MAX_SIZE = 5000;
    private static final long ONE_YEAR_MS = 365*24*60*60*1000L;
    private static final String[] CACHEABLE_METHODS = JMeterUtils.getPropDefault("cacheable_methods", "GET").split("[ ,]");

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

    private transient InheritableThreadLocal<Map<String, CacheEntry>> threadCache;

    private transient boolean useExpires; // Cached value

    /**
     * used to share the cache between 2 cache managers
     * @see CacheManager#createCacheManagerProxy() 
     * @since 3.0 */
    private transient Map<String, CacheEntry> localCache;

    public CacheManager() {
        setProperty(new BooleanProperty(CLEAR, false));
        setProperty(new BooleanProperty(USE_EXPIRES, false));
        clearCache();
        useExpires = false;
    }
    
    CacheManager(Map<String, CacheEntry> localCache, boolean useExpires) {
        this.localCache = localCache;
        this.useExpires = useExpires;
    }

    /*
     * Holder for storing cache details.
     * Perhaps add original response later?
     */
    // package-protected to allow access by unit-test cases
    static class CacheEntry {
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

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "CacheEntry [lastModified=" + lastModified + ", etag=" + etag + ", expires=" + expires
                    + ", varyHeader=" + varyHeader + "]";
        }
    }

    /**
     * Save the Last-Modified, Etag, and Expires headers if the result is cacheable.
     * Version for Java implementation.
     * @param conn connection
     * @param res result
     */
    public void saveDetails(URLConnection conn, HTTPSampleResult res){
        final String varyHeader = conn.getHeaderField(HTTPConstants.VARY);
        if (isCacheable(res, varyHeader)){
            String lastModified = conn.getHeaderField(HTTPConstants.LAST_MODIFIED);
            String expires = conn.getHeaderField(HTTPConstants.EXPIRES);
            String etag = conn.getHeaderField(HTTPConstants.ETAG);
            String url = conn.getURL().toString();
            String cacheControl = conn.getHeaderField(HTTPConstants.CACHE_CONTROL);
            String date = conn.getHeaderField(HTTPConstants.DATE);
            setCache(lastModified, cacheControl, expires, etag, url, date, getVaryHeader(varyHeader, asHeaders(res.getRequestHeaders())));
        }
    }

    private Pair<String, String> getVaryHeader(String headerName, Header[] reqHeaders) {
        if (headerName == null) {
            return null;
        }
        final Set<String> names = new HashSet<>(Arrays.asList(headerName.split(",\\s*")));
        final Map<String, List<String>> values = new HashMap<>();
        for (final String name: names) {
            values.put(name, new ArrayList<String>());
        }
        for (Header header: reqHeaders) {
            if (names.contains(header.getName())) {
                log.debug("Found vary value {} for {} in response", header, headerName);
                values.get(header.getName()).add(header.getValue());
            }
        }
        return new ImmutablePair<>(headerName, values.toString());
    }

    /**
     * Save the Last-Modified, Etag, and Expires headers if the result is
     * cacheable. Version for Apache HttpClient implementation.
     *
     * @param method
     *            {@link HttpResponse} to extract header information from
     * @param res
     *            result to decide if result is cacheable
     */
    public void saveDetails(HttpResponse method, HTTPSampleResult res) {
        final String varyHeader = getHeader(method, HTTPConstants.VARY);
        if (isCacheable(res, varyHeader)){
            String lastModified = getHeader(method ,HTTPConstants.LAST_MODIFIED);
            String expires = getHeader(method ,HTTPConstants.EXPIRES);
            String etag = getHeader(method ,HTTPConstants.ETAG);
            String cacheControl = getHeader(method, HTTPConstants.CACHE_CONTROL);
            String date = getHeader(method, HTTPConstants.DATE);
            setCache(lastModified, cacheControl, expires, etag,
                    res.getUrlAsString(), date, getVaryHeader(varyHeader,
                            asHeaders(res.getRequestHeaders()))); // TODO correct URL?
        }
    }

    // helper method to save the cache entry
    private void setCache(String lastModified, String cacheControl, String expires,
            String etag, String url, String date, Pair<String, String> varyHeader) {
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
            if(cacheControl != null && !cacheControl.contains("no-cache")) {
                expiresDate = extractExpiresDateFromCacheControl(lastModified,
                        cacheControl, expires, etag, url, date, maxAge, expiresDate);
                // else expiresDate computed in (expires!=null) condition is used
            }
        }
        if (varyHeader != null) {
            if (log.isDebugEnabled()) {
                log.debug("Set entry into cache for url {} and vary {} ({})", url,
                        varyHeader,
                        varyUrl(url, varyHeader.getLeft(), varyHeader.getRight()));
            }
            getCache().put(url, new CacheEntry(lastModified, expiresDate, etag, varyHeader.getLeft()));
            getCache().put(varyUrl(url, varyHeader.getLeft(), varyHeader.getRight()), new CacheEntry(lastModified, expiresDate, etag, null));
        } else {
            if (getCache().get(url) != null) {
                log.debug("Entry for {} already in cache.", url);
                return;
            }
            CacheEntry cacheEntry = new CacheEntry(lastModified, expiresDate, etag, null);
            log.debug("Set entry {} into cache for url {}", url, cacheEntry);
            getCache().put(url, cacheEntry);
        }
    }

    private Date extractExpiresDateFromExpires(String expires) {
        Date expiresDate;
        try {
            expiresDate = org.apache.http.client.utils.DateUtils
                    .parseDate(expires);
        } catch (IllegalArgumentException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to parse Expires: '{}' {}", expires, e.toString());
            }
            expiresDate = CacheManager.EXPIRED_DATE; // invalid dates must be
                                                     // treated as expired
        }
        return expiresDate;
    }

    private Date extractExpiresDateFromCacheControl(String lastModified,
            String cacheControl, String expires, String etag, String url,
            String date, final String maxAge, Date defaultExpiresDate) {
        // the max-age directive overrides the Expires header,
        if (cacheControl.contains(maxAge)) {
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

    private Date calcExpiresDate(String lastModified, String cacheControl,
            String expires, String etag, String url, String date) {
        if(!StringUtils.isEmpty(lastModified) && !StringUtils.isEmpty(date)) {
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

    // Apache HttpClient
    private String getHeader(HttpResponse method, String name) {
        org.apache.http.Header hdr = method.getLastHeader(name);
        return hdr != null ? hdr.getValue() : null;
    }

    /*
     * Is the sample result OK to cache?
     * i.e is it in the 2xx range or equal to 304, and is it a cacheable method?
     */
    private boolean isCacheable(HTTPSampleResult res, String varyHeader){
        if ("*".equals(varyHeader)) {
            return false;
        }
        final String responseCode = res.getResponseCode();
        return isCacheableMethod(res) 
                && (("200".compareTo(responseCode) <= 0  // $NON-NLS-1$
                    && "299".compareTo(responseCode) >= 0)  // $NON-NLS-1$
                    || "304".equals(responseCode));  // $NON-NLS-1$
    }

    private boolean isCacheableMethod(HTTPSampleResult res) {
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
     * Apache HttpClient version.
     * @param url {@link URL} to look up in cache
     * @param request where to set the headers
     */
    public void setHeaders(URL url, HttpRequestBase request) {
        CacheEntry entry = getEntry(url.toString(), request.getAllHeaders());
        if (log.isDebugEnabled()){
            log.debug("setHeaders for HTTP Method:{}(OAH) URL:{} Entry:{}", request.getMethod(), url.toString(), entry);
        }
        if (entry != null){
            final String lastModified = entry.getLastModified();
            if (lastModified != null){
                request.setHeader(HTTPConstants.IF_MODIFIED_SINCE, lastModified);
            }
            final String etag = entry.getEtag();
            if (etag != null){
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
     * @param url {@link URL} to look up in cache
     * @param headers Array of {@link org.apache.jmeter.protocol.http.control.Header}
     * @param conn where to set the headers
     */
    public void setHeaders(HttpURLConnection conn,
            org.apache.jmeter.protocol.http.control.Header[] headers, URL url) {
        CacheEntry entry = getEntry(url.toString(), 
                headers != null ? asHeaders(headers) : new Header[0]);
        if (log.isDebugEnabled()){
            log.debug("setHeaders HTTP Method{}(Java) url:{} entry:{}", conn.getRequestMethod(), url.toString(), entry);
        }
        if (entry != null){
            final String lastModified = entry.getLastModified();
            if (lastModified != null){
                conn.addRequestProperty(HTTPConstants.IF_MODIFIED_SINCE, lastModified);
            }
            final String etag = entry.getEtag();
            if (etag != null){
                conn.addRequestProperty(HTTPConstants.IF_NONE_MATCH, etag);
            }
        }
    }

    /**
     * Check the cache, if the entry has an expires header and the entry has not
     * expired, return <code>true</code><br>
     * 
     * @param url
     *            {@link URL} to look up in cache
     * @return <code>true</code> if entry has an expires header and the entry
     *         has not expired, else <code>false</code>
     * @deprecated use a version of {@link CacheManager#inCache(URL, Header[])}
     *             or
     *             {@link CacheManager#inCache(URL, org.apache.jmeter.protocol.http.control.Header[])}
     */
    @Deprecated
    public boolean inCache(URL url) {
        return entryStillValid(url, getEntry(url.toString(), null));
    }

    public boolean inCache(URL url, Header[] allHeaders) {
        return entryStillValid(url, getEntry(url.toString(), allHeaders));
    }

    public boolean inCache(URL url, org.apache.jmeter.protocol.http.control.Header[] allHeaders) {
        return entryStillValid(url, getEntry(url.toString(), asHeaders(allHeaders)));
    }

    private Header[] asHeaders(
            org.apache.jmeter.protocol.http.control.Header[] allHeaders) {
        final List<Header> result = new ArrayList<>(allHeaders.length);
        for (org.apache.jmeter.protocol.http.control.Header header: allHeaders) {
            result.add(new HeaderAdapter(header));
        }
        return result.toArray(new Header[result.size()]);
    }

    private Header[] asHeaders(String allHeaders) {
        List<Header> result = new ArrayList<>();
        for (String line: allHeaders.split("\\n")) {
            String[] splitted = line.split(": ", 2);
            if (splitted.length == 2) {
                result.add(new BasicHeader(splitted[0], splitted[1]));
            }
        }
        return result.toArray(new Header[result.size()]);
    }

    private static class HeaderAdapter implements Header {

        private final org.apache.jmeter.protocol.http.control.Header delegate;

        public HeaderAdapter(org.apache.jmeter.protocol.http.control.Header delegate) {
            this.delegate = delegate;
        }

        @Override
        public HeaderElement[] getElements() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getValue() {
            return delegate.getValue();
        }

    }

    private boolean entryStillValid(URL url, CacheEntry entry) {
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

    private CacheEntry getEntry(String url, Header[] headers) {
        CacheEntry entry = getCache().get(url);
        log.debug("getEntry url:{} entry:{} header:{}", url, entry, headers);
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
        Pair<String, String> varyPair = getVaryHeader(entry.getVaryHeader(), headers);
        if (varyPair != null) {
            if(log.isDebugEnabled()) {
                log.debug("Looking again for {} because of {} with vary: {} ({})", url, entry, entry.getVaryHeader(), varyPair);
            }
            return getEntry(varyUrl(url, entry.getVaryHeader(), varyPair.getRight()), null);
        }
        return null;
    }

    private String varyUrl(String url, String headerName, String headerValue) {
        return "vary-" + headerName + "-" + headerValue + "-" + url;
    }

    private Map<String, CacheEntry> getCache() {
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
        threadCache = new InheritableThreadLocal<Map<String, CacheEntry>>(){
            @Override
            protected Map<String, CacheEntry> initialValue(){
                // Bug 51942 - this map may be used from multiple threads
                @SuppressWarnings("unchecked") // LRUMap is not generic currently
                Map<String, CacheEntry> map = new LRUMap(getMaxSize());
                return Collections.synchronizedMap(map);
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
        if (getClearEachIteration()) {
            clearCache();
        }
        useExpires = getUseExpires(); // cache the value
    }

}
