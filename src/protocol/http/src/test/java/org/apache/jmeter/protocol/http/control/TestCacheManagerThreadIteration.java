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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.benmanes.caffeine.cache.Cache;

/**
 * Test {@link CacheManager} that uses HTTPHC4Impl
 */
public class TestCacheManagerThreadIteration {
    private JMeterContext jmctx;
    private JMeterVariables jmvars;
    private static final String SAME_USER="__jmv_SAME_USER";
    protected static final String LOCAL_HOST = "http://localhost/";
    protected static final String EXPECTED_ETAG = "0xCAFEBABEDEADBEEF";
    protected static final ZoneId GMT = ZoneId.of("GMT");
    protected CacheManager cacheManager;
    protected String currentTimeInGMT;
    protected String vary = null;
    protected URL url;
    protected HTTPSampleResult sampleResultOK;

    private class HttpResponseStub extends AbstractHttpMessage implements HttpResponse {
        private org.apache.http.Header lastModifiedHeader;
        private org.apache.http.Header etagHeader;
        private String expires;
        private String cacheControl;
        private org.apache.http.Header dateHeader;
        private List<org.apache.http.Header> headers;

        public HttpResponseStub() {
            this.headers = new ArrayList<>();
            this.lastModifiedHeader = new BasicHeader(HTTPConstants.LAST_MODIFIED, currentTimeInGMT);
            this.dateHeader = new BasicHeader(HTTPConstants.DATE, currentTimeInGMT);
            this.etagHeader = new BasicHeader(HTTPConstants.ETAG, EXPECTED_ETAG);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.apache.http.message.AbstractHttpMessage#getAllHeaders()
         */
        @Override
        public org.apache.http.Header[] getAllHeaders() {
            return headers.toArray(new org.apache.http.Header[headers.size()]);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.apache.http.message.AbstractHttpMessage#addHeader(org.apache.http.Header)
         */
        @Override
        public void addHeader(org.apache.http.Header header) {
            headers.add(header);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.apache.http.message.AbstractHttpMessage#getFirstHeader(java.lang.String)
         */
        @Override
        public Header getFirstHeader(String headerName) {
            Header[] headers = getHeaders(headerName);
            if (headers.length > 0) {
                return headers[0];
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.apache.http.message.AbstractHttpMessage#getLastHeader(java.lang.String)
         */
        @Override
        public Header getLastHeader(String headerName) {
            Header[] headers = getHeaders(headerName);
            if (headers.length > 0) {
                return headers[headers.length - 1];
            }
            return null;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.apache.http.message.AbstractHttpMessage#getHeaders(java.lang.String)
         */
        @Override
        public Header[] getHeaders(String headerName) {
            org.apache.http.Header header = null;
            if (HTTPConstants.LAST_MODIFIED.equals(headerName)) {
                header = this.lastModifiedHeader;
            } else if (HTTPConstants.ETAG.equals(headerName)) {
                header = this.etagHeader;
            } else if (HTTPConstants.EXPIRES.equals(headerName)) {
                header = expires == null ? null : new BasicHeader(HTTPConstants.EXPIRES, expires);
            } else if (HTTPConstants.CACHE_CONTROL.equals(headerName)) {
                header = cacheControl == null ? null : new BasicHeader(HTTPConstants.CACHE_CONTROL, cacheControl);
            } else if (HTTPConstants.DATE.equals(headerName)) {
                header = this.dateHeader;
            } else if (HTTPConstants.VARY.equals(headerName)) {
                header = vary == null ? null : new BasicHeader(HTTPConstants.VARY, vary);
            }
            if (header != null) {
                return new org.apache.http.Header[] { header };
            } else {
                return super.getHeaders(headerName);
            }
        }

        @Override
        public ProtocolVersion getProtocolVersion() {
            return null;
        }

        @Override
        public StatusLine getStatusLine() {
            return null;
        }

        @Override
        public void setStatusLine(StatusLine statusline) {
        }

        @Override
        public void setStatusLine(ProtocolVersion ver, int code) {
        }

        @Override
        public void setStatusLine(ProtocolVersion ver, int code, String reason) {
        }

        @Override
        public void setStatusCode(int code) throws IllegalStateException {
        }

        @Override
        public void setReasonPhrase(String reason) throws IllegalStateException {
        }

        @Override
        public HttpEntity getEntity() {
            return null;
        }

        @Override
        public void setEntity(HttpEntity entity) {
        }

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        public void setLocale(Locale loc) {
        }
    }

    private class HttpPostStub extends HttpPost {
        HttpPostStub() {
        }

        @Override
        public java.net.URI getURI() {
            try {
                return url.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalStateException();
            }
        }
    }

    protected String makeDate(Instant d) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
                .withLocale(Locale.US)
                .withZone(GMT);
        return formatter.format(d);
    }

    protected HTTPSampleResult getSampleResultWithSpecifiedResponseCode(String code) {
        HTTPSampleResult sampleResult = new HTTPSampleResult();
        sampleResult.setResponseCode(code);
        sampleResult.setHTTPMethod("GET");
        sampleResult.setURL(url);
        return sampleResult;
    }

    private HttpRequestBase httpMethod;
    private HttpResponse httpResponse;

    @BeforeEach
    public void setUp() throws Exception {
        this.cacheManager = new CacheManager();
        this.currentTimeInGMT = makeDate(Instant.now());
        this.url = new URL(LOCAL_HOST);
        this.sampleResultOK = getSampleResultWithSpecifiedResponseCode("200");
        this.httpMethod = new HttpPostStub();
        this.httpResponse = new HttpResponseStub();
        this.httpMethod.setURI(this.url.toURI());
        jmctx = JMeterContextService.getContext();
        jmvars = new JMeterVariables();
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.url = null;
        this.httpMethod = null;
        this.httpResponse = null;
        this.cacheManager =  new CacheManager();
        this.currentTimeInGMT = null;
        this.sampleResultOK = null;
    }

    protected void setExpires(String expires) {
        ((HttpResponseStub) httpResponse).expires = expires;
    }

    protected void setCacheControl(String cacheControl) {
        ((HttpResponseStub) httpResponse).cacheControl = cacheControl;
    }

    protected void setLastModified(String lastModified) {
        ((HttpResponseStub) httpResponse).lastModifiedHeader = new BasicHeader(HTTPConstants.LAST_MODIFIED,
                lastModified);
    }

    protected void cacheResult(HTTPSampleResult result) {
        this.cacheManager.saveDetails(httpResponse, result);
    }

    protected void addRequestHeader(String requestHeader, String value) {
        this.httpMethod.addHeader(new BasicHeader(requestHeader, value));
    }

    protected void setRequestHeaders() {
        this.cacheManager.setHeaders(this.url, this.httpMethod);
    }

    private Cache<String, CacheManager.CacheEntry> getThreadCache() throws Exception {
        Field threadLocalfield = CacheManager.class.getDeclaredField("threadCache");
        threadLocalfield.setAccessible(true);
        @SuppressWarnings("unchecked")
        ThreadLocal<Cache<String, CacheManager.CacheEntry>> threadLocal = (ThreadLocal<Cache<String, CacheManager.CacheEntry>>) threadLocalfield
                .get(this.cacheManager);
        return threadLocal.get();
    }

    protected CacheManager.CacheEntry getThreadCacheEntry(String url) throws Exception {
        return getThreadCache().getIfPresent(url);
    }
    @Test
    public void testCacheControlCleared() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull(getThreadCacheEntry(LOCAL_HOST), "Should not find entry");
        Header[] headers = new Header[1];
        assertFalse(this.cacheManager.inCache(url, headers), "Should not find valid entry");
        long start = System.currentTimeMillis();
        setExpires(makeDate(Instant.ofEpochMilli(start)));
        setCacheControl("public, max-age=1");
        cacheResult(sampleResultOK);
        assertNotNull(getThreadCacheEntry(LOCAL_HOST), "Before iternation, should find entry");
        assertTrue(this.cacheManager.inCache(url, headers), "Before iternation, should find valid entry");
        this.cacheManager.setClearEachIteration(true);
        this.cacheManager.testIterationStart(null);
        assertNull(getThreadCacheEntry(LOCAL_HOST), "After iterantion, should not find entry");
        assertFalse(this.cacheManager.inCache(url, headers), "After iterantion, should not find valid entry");
    }

    @Test
    public void testJmeterVariableCacheWhenThreadIterationIsANewUser() {
        jmvars.putObject(SAME_USER, true);
        jmctx.setVariables(jmvars);
        HTTPSamplerBase sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
        cacheManager.setControlledByThread(true);
        sampler.setCacheManager(cacheManager);
        sampler.setThreadContext(jmctx);
        boolean res = (boolean) cacheManager.getThreadContext().getVariables().getObject(SAME_USER);
        assertTrue(res, "When test different user on the different iternation, the cache should be cleared");
    }

    @Test
    public void testJmeterVariableWhenThreadIterationIsSameUser() {
        jmvars.putObject(SAME_USER, false);
        jmctx.setVariables(jmvars);
        HTTPSamplerBase sampler = (HTTPSamplerBase) new HttpTestSampleGui().createTestElement();
        cacheManager.setControlledByThread(true);
        sampler.setCacheManager(cacheManager);
        sampler.setThreadContext(jmctx);
        boolean res = (boolean) cacheManager.getThreadContext().getVariables().getObject(SAME_USER);
        assertFalse(res, "When test different user on the different iternation, the cache shouldn't be cleared");
    }

    @Test
    public void testCacheManagerWhenThreadIterationIsANewUser() throws Exception {
        //Controlled by ThreadGroup
        jmvars.putObject(SAME_USER, false);
        jmctx.setVariables(jmvars);
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull(getThreadCacheEntry(LOCAL_HOST), "Should not find entry");
        Header[] headers = new Header[1];
        assertFalse(this.cacheManager.inCache(url, headers), "Should not find valid entry");
        long start = System.currentTimeMillis();
        setExpires(makeDate(Instant.ofEpochMilli(start)));
        setCacheControl("public, max-age=1");
        cacheResult(sampleResultOK);
        this.cacheManager.setThreadContext(jmctx);
        this.cacheManager.setControlledByThread(true);
        assertNotNull(getThreadCacheEntry(LOCAL_HOST), "Before iternation, should find entry");
        assertTrue(this.cacheManager.inCache(url, headers), "Before iternation, should find valid entry");
        this.cacheManager.testIterationStart(null);
        assertNull(getThreadCacheEntry(LOCAL_HOST), "After iterantion, should not find entry");
        assertFalse(this.cacheManager.inCache(url, headers), "After iterantion, should not find valid entry");

        //Controlled by cacheManager
        jmvars.putObject(SAME_USER, true);
        jmctx.setVariables(jmvars);
        this.cacheManager.setThreadContext(jmctx);
        start = System.currentTimeMillis();
        setExpires(makeDate(Instant.ofEpochMilli(start)));
        setCacheControl("public, max-age=1");
        cacheResult(sampleResultOK);
        assertNotNull(getThreadCacheEntry(LOCAL_HOST), "Before iternation, should find entry");
        assertTrue(this.cacheManager.inCache(url, headers), "Before iternation, should find valid entry");
        this.cacheManager.setControlledByThread(false);
        this.cacheManager.setClearEachIteration(true);
        this.cacheManager.testIterationStart(null);
        assertNull(getThreadCacheEntry(LOCAL_HOST), "After iterantion, should not find entry");
        assertFalse(this.cacheManager.inCache(url, headers), "After iterantion, should not find valid entry");
    }

    @Test
    public void testCacheManagerWhenThreadIterationIsSameUser() throws Exception {
        // Controlled by ThreadGroup
        jmvars.putObject(SAME_USER, true);
        jmctx.setVariables(jmvars);
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull(getThreadCacheEntry(LOCAL_HOST), "Should not find entry");
        Header[] headers = new Header[1];
        assertFalse(this.cacheManager.inCache(url, headers), "Should not find valid entry");
        long start = System.currentTimeMillis();
        setExpires(makeDate(Instant.ofEpochMilli(start)));
        setCacheControl("public, max-age=1");
        cacheResult(sampleResultOK);
        this.cacheManager.setThreadContext(jmctx);
        this.cacheManager.setControlledByThread(true);
        assertNotNull(getThreadCacheEntry(LOCAL_HOST), "Before iteration, should find entry");
        assertTrue(this.cacheManager.inCache(url, headers), "Before iteration, should find valid entry");
        this.cacheManager.testIterationStart(null);
        assertNotNull(getThreadCacheEntry(LOCAL_HOST), "After iteration, should find entry");
        assertTrue(this.cacheManager.inCache(url, headers), "After iteration, should find valid entry");
        // Controlled by cacheManager
        jmvars.putObject(SAME_USER, false);
        jmctx.setVariables(jmvars);
        this.cacheManager.setThreadContext(jmctx);
        start = System.currentTimeMillis();
        setExpires(makeDate(Instant.ofEpochMilli(start)));
        setCacheControl("public, max-age=1");
        cacheResult(sampleResultOK);
        assertNotNull(getThreadCacheEntry(LOCAL_HOST), "Before iteration, should find entry");
        assertTrue(this.cacheManager.inCache(url, headers), "Before iteration, should find valid entry");
        this.cacheManager.setControlledByThread(false);
        this.cacheManager.setClearEachIteration(false);
        this.cacheManager.testIterationStart(null);
        assertNotNull(getThreadCacheEntry(LOCAL_HOST), "After iteration, should find entry");
        assertTrue(this.cacheManager.inCache(url, headers), "After iteration, should find valid entry");
    }

}
