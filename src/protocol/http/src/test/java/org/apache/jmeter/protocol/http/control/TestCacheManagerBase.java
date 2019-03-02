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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class TestCacheManagerBase extends JMeterTestCase {
    protected static final String LOCAL_HOST = "http://localhost/";
    protected static final String EXPECTED_ETAG = "0xCAFEBABEDEADBEEF";
    protected static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    protected CacheManager cacheManager;
    protected String currentTimeInGMT;
    protected String vary = null;
    protected URL url;
    protected HTTPSampleResult sampleResultOK;

    protected String makeDate(Date d) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        simpleDateFormat.setTimeZone(GMT);
        return simpleDateFormat.format(d);
    }

    @Before
    public void setUp() throws Exception {
        this.cacheManager = new CacheManager();
        this.currentTimeInGMT = makeDate(new Date());
        this.url = new URL(LOCAL_HOST);

        this.sampleResultOK = getSampleResultWithSpecifiedResponseCode("200");
    }

    @After
    public void tearDown() throws Exception {
        this.url = null;
        this.cacheManager = null;
        this.currentTimeInGMT = null;
        this.sampleResultOK = null;
    }

    protected abstract void setExpires(String expires);

    protected abstract void setCacheControl(String cacheControl);

    protected abstract void cacheResult(HTTPSampleResult result) throws Exception;

    protected abstract void setLastModified(String lastModified);

    protected abstract void checkRequestHeader(String requestHeader, String expectedValue);

    protected abstract void addRequestHeader(String requestHeader, String value);

    protected abstract void setRequestHeaders();

    protected void sleepTill(long deadline) {
        while (System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // FIXME Doing this can lead to sleep not sleeping expected time and random errors
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Test
    public void testExpiresBug59962() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        long start = System.currentTimeMillis();
        setExpires(makeDate(new Date(start + 2000)));
        cacheResultWithGivenCode("304");
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry", this.cacheManager.inCache(url));
        sleepTill(start + 2010);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }
    
    @Test
    public void testExpires() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        long start = System.currentTimeMillis();
        setExpires(makeDate(new Date(start + 2000)));
        cacheResult(sampleResultOK);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry", this.cacheManager.inCache(url));
        sleepTill(start + 2010);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }

    @Test
    public void testNoExpires() throws Exception {
        this.cacheManager.setUseExpires(false);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        setExpires(makeDate(new Date(System.currentTimeMillis() + 2000)));
        cacheResult(sampleResultOK);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }

    @Test
    public void testCacheControl() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        long start = System.currentTimeMillis();
        setExpires(makeDate(new Date(start)));
        setCacheControl("public, max-age=1");
        cacheResult(sampleResultOK);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry", this.cacheManager.inCache(url));
        sleepTill(start + 1010);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }

    @Test
    public void testCacheVarySomething() throws Exception {
        String varyHeader = "Something";
        testCacheVary(varyHeader, new Header[] { new Header(varyHeader, "value") },
                new Header[] {
                        new Header(varyHeader, "something completely different") });
    }

    @Test
    public void testCacheVaryAcceptEncoding() throws Exception {
        String varyHeader = "Accept-Encoding";
        testCacheVary(varyHeader,
                new Header[] { new Header(varyHeader, "value") }, new Header[] {
                        new Header(varyHeader, "something completely different") });
    }

    @Test
    public void testCacheMultiValueVaryHeaders() throws Exception {
        String varyHeader = "Accept-Encoding";
        testCacheVary(varyHeader,
                new Header[] { new Header(varyHeader, "value"),
                        new Header(varyHeader, "another value") },
                new Header[] { new Header(varyHeader,
                        "something completely different") });
    }

    @Test
    public void testCacheMultipleVaryHeaders() throws Exception {
        String varyHeaderOne = "Accept-Encoding";
        String varyHeaderTwo = "Something";
        testCacheVary(varyHeaderOne + "," + varyHeaderTwo,
                new Header[] { new Header(varyHeaderOne, "first value"),
                        new Header(varyHeaderTwo, "another value") },
                new Header[] { new Header(varyHeaderOne,
                        "first") });
    }

    @Test
    public void testCacheMultipleMultiVaryHeaders() throws Exception {
        String varyHeaderOne = "Accept-Encoding";
        String varyHeaderTwo = "Something";
        testCacheVary(varyHeaderOne + "," + varyHeaderTwo,
                new Header[] { new Header(varyHeaderOne, "first value"),
                        new Header(varyHeaderOne, "second value"),
                        new Header(varyHeaderTwo, "another value") },
                new Header[] { new Header(varyHeaderOne, "first value"),
                        new Header(varyHeaderOne, "another value") });
    }

    private String asString(Header[] headers) {
        StringBuilder result = new StringBuilder();
        for (Header header: headers) {
            result.append(header.getName()).append(": ").append(header.getValue()).append("\n");
        }
        return result.toString();
    }

    private void testCacheVary(String vary, Header[] origHeaders, Header[] differentHeaders) throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url, origHeaders));
        setExpires(makeDate(new Date(System.currentTimeMillis())));
        setCacheControl("public, max-age=5");
        sampleResultOK.setRequestHeaders(asString(origHeaders));
        this.vary = vary;
        cacheResult(sampleResultOK);
        assertNotNull("Should find entry with vary header", getThreadCacheEntry(LOCAL_HOST).getVaryHeader());
        assertFalse("Should not find valid entry without headers", this.cacheManager.inCache(url));
        assertTrue("Should find valid entry with headers", this.cacheManager.inCache(url, origHeaders));
        assertFalse("Should not find valid entry with different header", this.cacheManager.inCache(url, differentHeaders));
        this.vary = null;
    }

    @Test
    public void testCacheHEAD() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        setExpires(makeDate(new Date(System.currentTimeMillis())));
        setCacheControl("public, max-age=5");
        HTTPSampleResult sampleResultHEAD = getSampleResultWithSpecifiedResponseCode("200");
        sampleResultHEAD.setHTTPMethod("HEAD");
        cacheResult(sampleResultHEAD);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }

    @Test
    public void testPrivateCache() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        long start = System.currentTimeMillis();
        setExpires(makeDate(new Date(start)));
        setCacheControl("private, max-age=1");
        cacheResult(sampleResultOK);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry", this.cacheManager.inCache(url));
        sleepTill(start + 1010);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }

    @Test
    public void testPrivateCacheNoMaxAgeNoExpire() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        setCacheControl("private");
        // Expires is not set, however RFC recommends to use
        // response_is_fresh = (freshness_lifetime > current_age)
        // We set "currentAge == X seconds", thus response will considered to
        // be fresh for the next 10% of X seconds == 0.1*X seconds
        long start = System.currentTimeMillis();
        long age = 30 * 1000; // 30 seconds
        setLastModified(makeDate(new Date(start - age)));
        cacheResult(sampleResultOK);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry", this.cacheManager.inCache(url));
        sleepTill(start + age / 10 + 10);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }

    @Test
    public void testPrivateCacheExpireNoMaxAge() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        long start = System.currentTimeMillis();
        setExpires(makeDate(new Date(start + 2000)));
        setCacheControl("private");
        cacheResult(sampleResultOK);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry", this.cacheManager.inCache(url));
        sleepTill(start + 2010);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }

    @Test
    public void testNoCache() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        setCacheControl("no-cache");
        cacheResult(sampleResultOK);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }

    @Test
    public void testNoStore() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        setCacheControl("no-store");
        cacheResult(sampleResultOK);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }

    @Test
    public void testCacheHttpClientBug51932() throws Exception {
        this.cacheManager.setUseExpires(true);
        this.cacheManager.testIterationStart(null);
        assertNull("Should not find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
        long start = System.currentTimeMillis();
        setExpires(makeDate(new Date(start)));
        setCacheControl("public, max-age=1, no-transform");
        cacheResult(sampleResultOK);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertTrue("Should find valid entry", this.cacheManager.inCache(url));
        sleepTill(start + 1010);
        assertNotNull("Should find entry", getThreadCacheEntry(LOCAL_HOST));
        assertFalse("Should not find valid entry", this.cacheManager.inCache(url));
    }
    
    @Test
    public void testGetClearEachIteration() throws Exception {
        assertFalse("Should default not to clear after each iteration.", this.cacheManager.getClearEachIteration());
        this.cacheManager.setClearEachIteration(true);
        assertTrue("Should be settable to clear after each iteration.", this.cacheManager.getClearEachIteration());
        this.cacheManager.setClearEachIteration(false);
        assertFalse("Should be settable not to clear after each iteration.", this.cacheManager.getClearEachIteration());
    }

    private void cacheResultWithGivenCode(String responseCode) throws Exception {
        HTTPSampleResult sampleResult = getSampleResultWithSpecifiedResponseCode(responseCode);
        cacheResult(sampleResult);
    }

    @Test
    public void testSaveDetailsWithEmptySampleResultGivesNoCacheEntry() throws Exception {
        cacheResultWithGivenCode("");
        assertTrue("Saving details with empty SampleResult should not make cache entry.", getThreadCache().isEmpty());
    }

    @Test
    public void testSaveDetailsHttpMethodWithSampleResultWithResponseCode200GivesCacheEntry() throws Exception {
        cacheResultWithGivenCode("200");
        CacheManager.CacheEntry cacheEntry = getThreadCacheEntry(LOCAL_HOST);
        assertNotNull("Saving SampleResult with HttpMethod & 200 response should make cache entry.", cacheEntry);
        assertEquals(
                "Saving details with SampleResult & HttpMethod with 200 response should make cache entry with no etag.",
                EXPECTED_ETAG, cacheEntry.getEtag());
        assertEquals(
                "Saving details with SampleResult & HttpMethod with 200 response should make cache entry with no last modified date.",
                this.currentTimeInGMT, cacheEntry.getLastModified());
    }

    @Test
    public void testSaveDetailsHttpMethodWithSampleResultWithResponseCode404GivesNoCacheEntry() throws Exception {
        cacheResultWithGivenCode("404");
        assertNull("Saving SampleResult with HttpMethod & 404 response should not make cache entry.", getThreadCacheEntry(LOCAL_HOST));
    }

    @Test
    public void testSetHeadersHttpMethodWithSampleResultWithResponseCode200GivesCacheEntry() throws Exception {
        addRequestHeader(HTTPConstants.IF_MODIFIED_SINCE, this.currentTimeInGMT);
        addRequestHeader(HTTPConstants.ETAG, EXPECTED_ETAG);
        cacheResultWithGivenCode("200");
        setRequestHeaders();
        checkRequestHeader(HTTPConstants.IF_NONE_MATCH, EXPECTED_ETAG);
        checkRequestHeader(HTTPConstants.IF_MODIFIED_SINCE, this.currentTimeInGMT);
    }

    @Test
    public void testSetHeadersHttpMethodWithSampleResultWithResponseCode404GivesNoCacheEntry() throws Exception {
        cacheResultWithGivenCode("404");
        setRequestHeaders();
        assertNull("Saving SampleResult with HttpMethod & 404 response should not make cache entry.", getThreadCacheEntry(LOCAL_HOST));
    }

    @Test
    public void testClearCache() throws Exception {
        assertTrue("ThreadCache should be empty initially.", getThreadCache().isEmpty());
        cacheResultWithGivenCode("200");
        assertFalse(
                "ThreadCache should be populated after saving details for HttpMethod with SampleResult with response code 200.",
                getThreadCache().isEmpty());
        this.cacheManager.clear();
        assertTrue("ThreadCache should be emptied by call to clear.", getThreadCache().isEmpty());
    }

    protected HTTPSampleResult getSampleResultWithSpecifiedResponseCode(String code) {
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
        ThreadLocal<Map<String, CacheManager.CacheEntry>> threadLocal = (ThreadLocal<Map<String, CacheManager.CacheEntry>>) threadLocalfield
                .get(this.cacheManager);
        return threadLocal.get();
    }

    protected CacheManager.CacheEntry getThreadCacheEntry(String url) throws Exception {
        return getThreadCache().get(url);
    }
    

}
