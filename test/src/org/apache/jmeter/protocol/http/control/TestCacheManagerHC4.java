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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.control.CacheManager.CacheEntry;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link CacheManager} that uses HTTPHC4Impl
 */
public class TestCacheManagerHC4 extends JMeterTestCase {
 
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
        /* (non-Javadoc)
         * @see org.apache.http.message.AbstractHttpMessage#getAllHeaders()
         */
        @Override
        public org.apache.http.Header[] getAllHeaders() {
            return headers.toArray(new org.apache.http.Header[headers.size()]);
        }

        /* (non-Javadoc)
         * @see org.apache.http.message.AbstractHttpMessage#addHeader(org.apache.http.Header)
         */
        @Override
        public void addHeader(org.apache.http.Header header) {
            headers.add(header);
        }

        /* (non-Javadoc)
         * @see org.apache.http.message.AbstractHttpMessage#getFirstHeader(java.lang.String)
         */
        @Override
        public Header getFirstHeader(String headerName) {
            Header[] headers = getHeaders(headerName);
            if(headers.length > 0) {
                return headers[0];
            }
            return null;
        }
        
        /* (non-Javadoc)
         * @see org.apache.http.message.AbstractHttpMessage#getLastHeader(java.lang.String)
         */
        @Override
        public Header getLastHeader(String headerName) {
            Header[] headers = getHeaders(headerName);
            if(headers.length > 0) {
                return headers[headers.length-1];
            }
            return null;
        }
        
        /* (non-Javadoc)
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
            if(header != null) {
                return new org.apache.http.Header[] {header};
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
    
    private static final String LOCAL_HOST = "http://localhost/";
    private static final String EXPECTED_ETAG = "0xCAFEBABEDEADBEEF";
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private CacheManager cacheManager;
    private String currentTimeInGMT;
    private String vary = null;
    private URL url;
    private HttpRequestBase httpMethod;
    private HttpResponse httpResponse;
    private HTTPSampleResult sampleResultOK;

    private String makeDate(Date d){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        simpleDateFormat.setTimeZone(GMT);
        return simpleDateFormat.format(d);
    }

    @Before
    public void setUp() throws Exception {
        this.cacheManager = new CacheManager();
        this.currentTimeInGMT = makeDate(new Date());
        this.url = new URL(LOCAL_HOST);
        this.httpMethod = new HttpPostStub();
        httpResponse = new HttpResponseStub();
        
        this.sampleResultOK = getSampleResultWithSpecifiedResponseCode("200");
    }

    @After
    public void tearDown() throws Exception {
        //this.httpUrlConnection = null;
        this.httpMethod = null;
        this.url = null;
        this.cacheManager = null;
        this.currentTimeInGMT = null;
        this.sampleResultOK = null;
    }

    @Test
    public void testExpiresHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).expires=makeDate(new Date(System.currentTimeMillis()+2000));
        this.cacheManager.saveDetails(httpResponse, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
        Thread.sleep(2010);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }
    
    @Test
    public void testCacheHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).expires=makeDate(new Date(System.currentTimeMillis()));
        ((HttpResponseStub)httpResponse).cacheControl="public, max-age=5";
        this.cacheManager.saveDetails(httpResponse, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
        Thread.sleep(5010);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }

    @Test
    public void testCacheVaryHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).expires=makeDate(new Date(System.currentTimeMillis()));
        ((HttpResponseStub)httpResponse).cacheControl="public, max-age=5";
        this.vary = "Something";
        this.cacheManager.saveDetails(httpResponse, sampleResultOK);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        this.vary = null;
    }

    @Test
    public void testCacheHttpClientHEAD() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).expires=makeDate(new Date(System.currentTimeMillis()));
        ((HttpResponseStub)httpResponse).cacheControl="public, max-age=5";
        HTTPSampleResult sampleResultHEAD=getSampleResultWithSpecifiedResponseCode("200");
        sampleResultHEAD.setHTTPMethod("HEAD");
        this.cacheManager.saveDetails(httpResponse, sampleResultHEAD);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }
    
    @Test
    public void testPrivateCacheHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).expires=makeDate(new Date(System.currentTimeMillis()));
        ((HttpResponseStub)httpResponse).cacheControl="private, max-age=5";
        this.cacheManager.saveDetails(httpResponse, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
        Thread.sleep(5010);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }
    
    @Test
    public void testPrivateCacheNoMaxAgeNoExpireHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).cacheControl="private";
        ((HttpResponseStub)httpResponse).lastModifiedHeader=new BasicHeader(HTTPConstants.LAST_MODIFIED, 
                makeDate(new Date(System.currentTimeMillis()-(10*5*1000))));
        this.cacheManager.saveDetails(httpResponse, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
        Thread.sleep(5010);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }
    
    @Test
    public void testPrivateCacheExpireNoMaxAgeHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).expires=makeDate(new Date(System.currentTimeMillis()+2000));
        ((HttpResponseStub)httpResponse).cacheControl="private";
        this.cacheManager.saveDetails(httpResponse, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
        Thread.sleep(2010);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }

    @Test
    public void testNoCacheHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).cacheControl="no-cache";
        this.cacheManager.saveDetails(httpResponse, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }
    
    @Test
    public void testNoStoreHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).cacheControl="no-store";
        this.cacheManager.saveDetails(httpResponse, sampleResultOK);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }
    
    @Test
    public void testCacheHttpClientBug51932() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpResponseStub)httpResponse).expires=makeDate(new Date(System.currentTimeMillis()));
        ((HttpResponseStub)httpResponse).cacheControl="public, max-age=5, no-transform";
        this.cacheManager.saveDetails(httpResponse, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
        Thread.sleep(5010);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }

    @Test
    public void testGetClearEachIteration() throws Exception {
        assertFalse("Should default not to clear after each iteration.", this.cacheManager.getClearEachIteration());
        this.cacheManager.setClearEachIteration(true);
        assertTrue("Should be settable to clear after each iteration.", this.cacheManager.getClearEachIteration());
        this.cacheManager.setClearEachIteration(false);
        assertFalse("Should be settable not to clear after each iteration.", this.cacheManager.getClearEachIteration());
    }

    @Test
    public void testSaveDetailsHttpMethodWithSampleResultWithResponseCode200GivesCacheEntry() throws Exception {
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("200");
        CacheManager.CacheEntry cacheEntry = getThreadCacheEntry(this.httpMethod.getURI().toString());
        assertNotNull("Saving SampleResult with HttpMethod & 200 response should make cache entry.", cacheEntry);
        assertEquals("Saving details with SampleResult & HttpMethod with 200 response should make cache entry with no etag.", EXPECTED_ETAG, cacheEntry.getEtag());
        assertEquals("Saving details with SampleResult & HttpMethod with 200 response should make cache entry with no last modified date.", this.currentTimeInGMT, cacheEntry.getLastModified());
    }

    @Test
    public void testSaveDetailsHttpMethodWithSampleResultWithResponseCode404GivesNoCacheEntry() throws Exception {
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("404");
        assertNull("Saving SampleResult with HttpMethod & 404 response should not make cache entry.", getThreadCacheEntry(url.toString()));
    }

    @Test
    public void testSetHeadersHttpMethodWithSampleResultWithResponseCode200GivesCacheEntry() throws Exception {
        this.httpMethod.setURI(this.url.toURI());
        this.httpMethod.addHeader(new BasicHeader(HTTPConstants.IF_MODIFIED_SINCE, this.currentTimeInGMT));
        this.httpMethod.addHeader(new BasicHeader(HTTPConstants.ETAG, EXPECTED_ETAG));
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("200");
        setHeadersWithUrlAndHttpMethod();
        checkRequestHeader(HTTPConstants.IF_NONE_MATCH, EXPECTED_ETAG);
        checkRequestHeader(HTTPConstants.IF_MODIFIED_SINCE, this.currentTimeInGMT);
    }

    @Test
    public void testSetHeadersHttpMethodWithSampleResultWithResponseCode404GivesNoCacheEntry() throws Exception {
        this.httpMethod.setURI(this.url.toURI());
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("404");
        setHeadersWithUrlAndHttpMethod();
        assertNull("Saving SampleResult with HttpMethod & 404 response should not make cache entry.", getThreadCacheEntry(url.toString()));
    }

    @Test
    public void testClearCache() throws Exception {
        assertTrue("ThreadCache should be empty initially.", getThreadCache().isEmpty());
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("200");
        assertFalse("ThreadCache should be populated after saving details for HttpMethod with SampleResult with response code 200.", getThreadCache().isEmpty());
        this.cacheManager.clear();
        assertTrue("ThreadCache should be emptied by call to clear.", getThreadCache().isEmpty());
    }

    private void checkRequestHeader(String requestHeader, String expectedValue) {
        org.apache.http.Header header = this.httpMethod.getLastHeader(requestHeader);
        assertEquals("Wrong name in header for " + requestHeader, requestHeader, header.getName());
        assertEquals("Wrong value for header " + header, expectedValue, header.getValue());
    }
    
    private HTTPSampleResult getSampleResultWithSpecifiedResponseCode(String code) {
        HTTPSampleResult sampleResult = new HTTPSampleResult();
        sampleResult.setResponseCode(code);
        sampleResult.setHTTPMethod("GET");
        sampleResult.setURL(url);
        return sampleResult;
    }

    private Map<String, CacheManager.CacheEntry> getThreadCache() throws Exception {
        Field threadLocalfield = CacheManager.class.getDeclaredField("threadCache");
        threadLocalfield.setAccessible(true);
        @SuppressWarnings("unchecked")
        ThreadLocal<Map<String, CacheEntry>> threadLocal = (ThreadLocal<Map<String, CacheManager.CacheEntry>>) threadLocalfield.get(this.cacheManager);
        return threadLocal.get();
    }

    private CacheManager.CacheEntry getThreadCacheEntry(String url) throws Exception {
        return getThreadCache().get(url);
    }

    private void saveDetailsWithHttpMethodAndSampleResultWithResponseCode(String responseCode) throws Exception {
        HTTPSampleResult sampleResult = getSampleResultWithSpecifiedResponseCode(responseCode);
        this.cacheManager.saveDetails(this.httpResponse, sampleResult);
    }

    private void setHeadersWithUrlAndHttpMethod() {
        this.cacheManager.setHeaders(this.url, this.httpMethod);
    }
}
