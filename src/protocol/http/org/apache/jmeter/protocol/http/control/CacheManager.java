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

// For unit tests @see TestCookieManager

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Handles HTTP Caching
 */
public class CacheManager extends ConfigTestElement implements TestListener, Serializable {

    private static final long serialVersionUID = 233L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static final String CLEAR = "clearEachIteration"; // $NON-NLS-1$

    private transient ThreadLocal threadCache;

    public CacheManager() {
        setProperty(new BooleanProperty(CLEAR, false));
        clearCache();
    }

    /*
     * Holder for storing cache details.
     * Perhaps add original response later?
     */
    private static class CacheEntry{
        private final String lastModified;
        private final String etag;
        public CacheEntry(String lastModified, String etag){
           this.lastModified = lastModified;
           this.etag = etag;
       }
        public String getLastModified() {
            return lastModified;
        }
        public String getEtag() {
            return etag;
        }
        @Override
        public String toString(){
            return lastModified+" "+etag;
        }
    }

    /**
     * Save the Last-Modified and Etag headers if the result is cacheable.
     *
     * @param conn connection
     * @param res result
     */
    public void saveDetails(URLConnection conn, SampleResult res){
        if (isCacheable(res)){
            String lastModified = conn.getHeaderField(HTTPConstantsInterface.LAST_MODIFIED);
            String etag = conn.getHeaderField(HTTPConstantsInterface.ETAG);
            String url = conn.getURL().toString();
            setCache(lastModified, etag, url);
        }
    }

    /**
     * Save the Last-Modified and Etag headers if the result is cacheable.
     *
     * @param method
     * @param res result
     */
    public void saveDetails(HttpMethod method, SampleResult res) throws URIException{
        if (isCacheable(res)){
            String lastModified = getHeader(method ,HTTPConstantsInterface.LAST_MODIFIED);
            String etag = getHeader(method ,HTTPConstantsInterface.ETAG);
            String url = method.getURI().toString();
            setCache(lastModified, etag, url);
        }
    }

    // helper method to save the cache entry
    private void setCache(String lastModified, String etag, String url) {
        if (log.isDebugEnabled()){
            log.debug("SET(both) "+url + " " + lastModified + " " + etag);
        }
        getCache().put(url, new CacheEntry(lastModified, etag));
    }

    // Helper method to deal with missing headers
    private String getHeader(HttpMethod method, String name){
        Header hdr = method.getResponseHeader(name);
        return hdr != null ? hdr.getValue() : null;
    }

    /*
     * Is the sample result OK to cache?
     * i.e is it in the 2xx range?
     */
    private boolean isCacheable(SampleResult res){
        final String responseCode = res.getResponseCode();
        return "200".compareTo(responseCode) <= 0  // $NON-NLS-1$
            && "299".compareTo(responseCode) >= 0; // $NON-NLS-1$
    }

    /**
     * Check the cache, and if there is a match, set the headers:<br/>
     * If-Modified-Since<br/>
     * If-None-Match<br/>
     * @param url URL to look up in cache
     * @param method where to set the headers
     */
    public void setHeaders(URL url, HttpMethod method) {
        CacheEntry entry = (CacheEntry) getCache().get(url.toString());
        if (log.isDebugEnabled()){
            log.debug(method.getName()+"(OAHC) "+url.toString()+" "+entry);
        }
        if (entry != null){
            final String lastModified = entry.getLastModified();
            if (lastModified != null){
                method.setRequestHeader(HTTPConstantsInterface.IF_MODIFIED_SINCE, lastModified);
            }
            final String etag = entry.getEtag();
            if (etag != null){
                method.setRequestHeader(HTTPConstantsInterface.IF_NONE_MATCH, etag);
            }
        }
    }

    /**
     * Check the cache, and if there is a match, set the headers:<br/>
     * If-Modified-Since<br/>
     * If-None-Match<br/>
     * @param url URL to look up in cache
     * @param conn where to set the headers
     */
    public void setHeaders(HttpURLConnection conn, URL url) {
        CacheEntry entry = (CacheEntry) getCache().get(url.toString());
        if (log.isDebugEnabled()){
            log.debug(conn.getRequestMethod()+"(Java) "+url.toString()+" "+entry);
        }
        if (entry != null){
            final String lastModified = entry.getLastModified();
            if (lastModified != null){
                conn.addRequestProperty(HTTPConstantsInterface.IF_MODIFIED_SINCE, lastModified);
            }
            final String etag = entry.getEtag();
            if (etag != null){
                conn.addRequestProperty(HTTPConstantsInterface.IF_NONE_MATCH, etag);
            }
        }
    }

    private Map getCache(){
        return (Map) threadCache.get();
    }

    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR, clear));
    }

    @Override
    public void clear(){
        super.clear();
        clearCache();
    }

    private void clearCache() {
        log.debug("Clear cache");
        threadCache = new ThreadLocal(){
            @Override
            protected Object initialValue(){
                return new HashMap();
            }
        };
    }

    public void testStarted() {
    }

    public void testEnded() {
    }

    public void testStarted(String host) {
    }

    public void testEnded(String host) {
    }

    public void testIterationStart(LoopIterationEvent event) {
        if (getClearEachIteration()) {
            clearCache();
        }
    }
}