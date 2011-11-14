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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.control.CacheManager.CacheEntry;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import org.apache.jmeter.samplers.SampleResult;

public class TestCacheManager extends JMeterTestCase {
    
    private class URLConnectionStub extends URLConnection {
        
        protected URLConnectionStub(URL url) {
            super(url);
        }
        
        private URLConnectionStub(URLConnection urlConnection) {
            super(urlConnection.getURL());
        }
        
        @Override
        public void connect() throws IOException {
        }
        
        private String expires = null;
        private String cacheControl = null;
        
        @Override
        public String getHeaderField(String name) {
            if (HTTPConstantsInterface.LAST_MODIFIED.equals(name)) {
                return currentTimeInGMT;
            } else if (HTTPConstantsInterface.ETAG.equals(name)) {
                return EXPECTED_ETAG;
            } else if (HTTPConstantsInterface.EXPIRES.equals(name)){
                return expires;
            } else if (HTTPConstantsInterface.CACHE_CONTROL.equals(name)){
                return cacheControl;
            }
            return super.getHeaderField(name);
        }
        @Override
        public URL getURL() {
            return url;
        }
    }
    
    private class HttpMethodStub extends PostMethod {
        private Header lastModifiedHeader;
        private Header etagHeader;
        private String expires;
        private String cacheControl;
        
        HttpMethodStub() {
            this.lastModifiedHeader = new Header(HTTPConstantsInterface.LAST_MODIFIED, currentTimeInGMT);
            this.etagHeader = new Header(HTTPConstantsInterface.ETAG, EXPECTED_ETAG);
        }
        
        @Override
        public Header getResponseHeader(String headerName) {
            if (HTTPConstantsInterface.LAST_MODIFIED.equals(headerName)) {
                return this.lastModifiedHeader;
            } else if (HTTPConstantsInterface.ETAG.equals(headerName)) {
                return this.etagHeader;
            } else if (HTTPConstantsInterface.EXPIRES.equals(headerName)) {
                return expires == null ? null : new Header(HTTPConstantsInterface.EXPIRES, expires);
            } else if (HTTPConstantsInterface.CACHE_CONTROL.equals(headerName)) {
                return cacheControl == null ? null : new Header(HTTPConstantsInterface.CACHE_CONTROL, cacheControl);
            }
            return null;
        }

        @Override
        public URI getURI() throws URIException {
            return uri;
        }
    }
    
    private static class HttpURLConnectionStub extends HttpURLConnection {
        private Map<String, List<String>> properties;
        
        public HttpURLConnectionStub(HttpMethod method, URL url) {
            super(method, url);
            this.properties = new HashMap<String, List<String>>();
        }
        
        @Override
        public void addRequestProperty(String key, String value) {
            List<String> list = new ArrayList<String>();
            list.add(value);
            this.properties.put(key, list);
        }
        
        @Override
        public Map<String, List<String>> getRequestProperties() {
            return this.properties;
        }
        
    }
    
    private static final String LOCAL_HOST = "http://localhost/";
    private static final String EXPECTED_ETAG = "0xCAFEBABEDEADBEEF";
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private CacheManager cacheManager;
    private String currentTimeInGMT;
    private URL url;
    private URI uri;
    private URLConnection urlConnection;
    private HttpMethod httpMethod;
    private HttpURLConnection httpUrlConnection;
    private SampleResult sampleResultOK;

    public TestCacheManager(String name) {
        super(name);
    }

    private String makeDate(Date d){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        simpleDateFormat.setTimeZone(GMT);
        return simpleDateFormat.format(d);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.cacheManager = new CacheManager();
        this.currentTimeInGMT = makeDate(new Date());
        this.uri = new URI(LOCAL_HOST, false);
        this.url = new URL(LOCAL_HOST);
        this.urlConnection =  new URLConnectionStub(this.url.openConnection());
        this.httpMethod = new HttpMethodStub();
        this.httpUrlConnection = new HttpURLConnectionStub(this.httpMethod, this.url);
        this.sampleResultOK = getSampleResultWithSpecifiedResponseCode("200");
    }

    @Override
    protected void tearDown() throws Exception {
        this.httpUrlConnection = null;
        this.httpMethod = null;
        this.urlConnection = null;
        this.url = null;
        this.uri = null;
        this.cacheManager = null;
        this.currentTimeInGMT = null;
        this.sampleResultOK = null;
        super.tearDown();
    }

    public void testExpiresJava() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((URLConnectionStub)urlConnection).expires=makeDate(new Date(System.currentTimeMillis()+2000));
        this.cacheManager.saveDetails(this.urlConnection, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
    }

    public void testNoExpiresJava() throws Exception{
        this.cacheManager.setUseExpires(false);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((URLConnectionStub)urlConnection).expires=makeDate(new Date(System.currentTimeMillis()+2000));
        this.cacheManager.saveDetails(this.urlConnection, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
    }

    public void testExpiresHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpMethodStub)httpMethod).expires=makeDate(new Date(System.currentTimeMillis()+2000));
        this.cacheManager.saveDetails(httpMethod, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
    }

    public void testCacheHttpClient() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpMethodStub)httpMethod).expires=makeDate(new Date(System.currentTimeMillis()));
        ((HttpMethodStub)httpMethod).cacheControl="public, max-age=10";
        this.cacheManager.saveDetails(httpMethod, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
    }

    public void testCacheHttpClientBug51932() throws Exception{
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry",getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry",this.cacheManager.inCache(url));
        ((HttpMethodStub)httpMethod).expires=makeDate(new Date(System.currentTimeMillis()));
        ((HttpMethodStub)httpMethod).cacheControl="public, max-age=10, no-transform";
        this.cacheManager.saveDetails(httpMethod, sampleResultOK);
        assertNotNull("Should find entry",getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry",this.cacheManager.inCache(url));
    }

    public void testGetClearEachIteration() throws Exception {
        assertFalse("Should default not to clear after each iteration.", this.cacheManager.getClearEachIteration());
        this.cacheManager.setClearEachIteration(true);
        assertTrue("Should be settable to clear after each iteration.", this.cacheManager.getClearEachIteration());
        this.cacheManager.setClearEachIteration(false);
        assertFalse("Should be settable not to clear after each iteration.", this.cacheManager.getClearEachIteration());
    }

    public void testSaveDetailsWithEmptySampleResultGivesNoCacheEntry() throws Exception {
        saveDetailsWithConnectionAndSampleResultWithResponseCode("");
        assertTrue("Saving details with empty SampleResult should not make cache entry.", getThreadCache().isEmpty());
    }

    public void testSaveDetailsURLConnectionWithSampleResultWithResponseCode200GivesCacheEntry() throws Exception {
        saveDetailsWithConnectionAndSampleResultWithResponseCode("200");
        CacheManager.CacheEntry cacheEntry = getThreadCacheEntry(this.url.toString());
        assertNotNull("Saving details with SampleResult & connection with 200 response should make cache entry.", cacheEntry);
        assertEquals("Saving details with SampleResult & connection with 200 response should make cache entry with an etag.", EXPECTED_ETAG, cacheEntry.getEtag());
        assertEquals("Saving details with SampleResult & connection with 200 response should make cache entry with last modified date.", this.currentTimeInGMT, cacheEntry.getLastModified());
    }

    public void testSaveDetailsHttpMethodWithSampleResultWithResponseCode200GivesCacheEntry() throws Exception {
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("200");
        CacheManager.CacheEntry cacheEntry = getThreadCacheEntry(this.httpMethod.getURI().toString());
        assertNotNull("Saving SampleResult with HttpMethod & 200 response should make cache entry.", cacheEntry);
        assertEquals("Saving details with SampleResult & HttpMethod with 200 response should make cache entry with no etag.", EXPECTED_ETAG, cacheEntry.getEtag());
        assertEquals("Saving details with SampleResult & HttpMethod with 200 response should make cache entry with no last modified date.", this.currentTimeInGMT, cacheEntry.getLastModified());
    }

    public void testSaveDetailsURLConnectionWithSampleResultWithResponseCode404GivesNoCacheEntry() throws Exception {
        saveDetailsWithConnectionAndSampleResultWithResponseCode("404");
        assertNull("Saving details with SampleResult & connection with 404 response should not make cache entry.", getThreadCacheEntry(url.toString()));
    }

    public void testSaveDetailsHttpMethodWithSampleResultWithResponseCode404GivesNoCacheEntry() throws Exception {
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("404");
        assertNull("Saving SampleResult with HttpMethod & 404 response should not make cache entry.", getThreadCacheEntry(this.httpMethod.getPath()));
    }

    public void testSetHeadersHttpMethodWithSampleResultWithResponseCode200GivesCacheEntry() throws Exception {
        this.httpMethod.setURI(this.uri);
        this.httpMethod.addRequestHeader(new Header(HTTPConstantsInterface.IF_MODIFIED_SINCE, this.currentTimeInGMT, false));
        this.httpMethod.addRequestHeader(new Header(HTTPConstantsInterface.ETAG, EXPECTED_ETAG, false));
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("200");
        setHeadersWithUrlAndHttpMethod();
        checkRequestHeader(HTTPConstantsInterface.IF_NONE_MATCH, EXPECTED_ETAG);
        checkRequestHeader(HTTPConstantsInterface.IF_MODIFIED_SINCE, this.currentTimeInGMT);
    }

    public void testSetHeadersHttpMethodWithSampleResultWithResponseCode404GivesNoCacheEntry() throws Exception {
        this.httpMethod.setURI(this.uri);
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("404");
        setHeadersWithUrlAndHttpMethod();
        assertNull("Saving SampleResult with HttpMethod & 404 response should not make cache entry.", getThreadCacheEntry(this.httpMethod.getPath()));
    }

    public void testSetHeadersHttpURLConnectionWithSampleResultWithResponseCode200GivesCacheEntry() throws Exception {
        saveDetailsWithConnectionAndSampleResultWithResponseCode("200");
        setHeadersWithHttpUrlConnectionAndUrl();
        Map<String, List<String>> properties = this.httpUrlConnection.getRequestProperties();
        checkProperty(properties, HTTPConstantsInterface.IF_NONE_MATCH, EXPECTED_ETAG);
        checkProperty(properties, HTTPConstantsInterface.IF_MODIFIED_SINCE, this.currentTimeInGMT);
    }

    public void testSetHeadersHttpURLConnectionWithSampleResultWithResponseCode404GivesNoCacheEntry() throws Exception {
        saveDetailsWithConnectionAndSampleResultWithResponseCode("404");
        setHeadersWithHttpUrlConnectionAndUrl();
        assertNull("Saving SampleResult with HttpMethod & 404 response should not make cache entry.", getThreadCacheEntry(this.url.toString()));
    }

    public void testClearCache() throws Exception {
        assertTrue("ThreadCache should be empty initially.", getThreadCache().isEmpty());
        saveDetailsWithHttpMethodAndSampleResultWithResponseCode("200");
        assertFalse("ThreadCache should be populated after saving details for HttpMethod with SampleResult with response code 200.", getThreadCache().isEmpty());
        this.cacheManager.clear();
        assertTrue("ThreadCache should be emptied by call to clear.", getThreadCache().isEmpty());
    }

    private void checkRequestHeader(String requestHeader, String expectedValue) {
        Header header = this.httpMethod.getRequestHeader(requestHeader);
        assertEquals("Wrong name in header for " + requestHeader, requestHeader, header.getName());
        assertEquals("Wrong value for header " + header, expectedValue, header.getValue());
    }

    private static void checkProperty(Map<String, List<String>> properties, String property, String expectedPropertyValue) {
        assertNotNull("Properties should not be null. Expected to find within it property = " + property + " with expected value = " + expectedPropertyValue, properties);
        List<String> listOfPropertyValues = properties.get(property);
        assertNotNull("No property entry found for property " + property, listOfPropertyValues);
        assertEquals("Did not find single property for property " + property, 1, listOfPropertyValues.size());
        assertEquals("Unexpected value for property " + property, expectedPropertyValue, listOfPropertyValues.get(0));
    }

    private SampleResult getSampleResultWithSpecifiedResponseCode(String code) {
        SampleResult sampleResult = new SampleResult();
        sampleResult.setResponseCode(code);
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
        SampleResult sampleResult = getSampleResultWithSpecifiedResponseCode(responseCode);
        this.cacheManager.saveDetails(this.httpMethod, sampleResult);
    }

    private void saveDetailsWithConnectionAndSampleResultWithResponseCode(String responseCode) {
        SampleResult sampleResult = getSampleResultWithSpecifiedResponseCode(responseCode);
        this.cacheManager.saveDetails(this.urlConnection, sampleResult);
    }

    private void setHeadersWithHttpUrlConnectionAndUrl() {
        this.cacheManager.setHeaders(this.httpUrlConnection, this.url);
    }

    private void setHeadersWithUrlAndHttpMethod() {
        this.cacheManager.setHeaders(this.url, this.httpMethod);
    }
}
