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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test {@link CacheManager} through its HTTP client neutral API, the one used by the
 * HttpClient 5 and the Java HTTP client implementations.
 */
public class TestCacheManagerHeaderSource extends TestCacheManagerBase {

    private List<Map.Entry<String, String>> requestHeaders;
    private Map<String, String> setRequestHeaders;
    private String expires;
    private String cacheControl;
    private String lastModified;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.requestHeaders = new ArrayList<>();
        this.setRequestHeaders = new LinkedHashMap<>();
        this.lastModified = this.currentTimeInGMT;
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        this.requestHeaders = null;
        this.setRequestHeaders = null;
        this.expires = null;
        this.cacheControl = null;
        this.lastModified = null;
        super.tearDown();
    }

    @Override
    protected void setExpires(String expires) {
        this.expires = expires;
    }

    @Override
    protected void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    @Override
    protected void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    protected void cacheResult(HTTPSampleResult result) throws Exception {
        cacheResult(result, true);
    }

    @Override
    protected void cacheResult(HTTPSampleResult result, boolean hasCachingHeaders) throws Exception {
        this.cacheManager.saveDetails(responseHeaders(hasCachingHeaders)::get, result);
    }

    private Map<String, String> responseHeaders(boolean hasCachingHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put(HTTPConstants.DATE, this.currentTimeInGMT);
        if (hasCachingHeaders) {
            headers.put(HTTPConstants.LAST_MODIFIED, this.lastModified);
            headers.put(HTTPConstants.ETAG, EXPECTED_ETAG);
            headers.put(HTTPConstants.EXPIRES, this.expires);
            headers.put(HTTPConstants.CACHE_CONTROL, this.cacheControl);
        }
        headers.put(HTTPConstants.VARY, this.vary);
        return headers;
    }

    @Override
    protected void addRequestHeader(String requestHeader, String value) {
        this.requestHeaders.add(Map.entry(requestHeader, value));
    }

    @Override
    protected void setRequestHeaders() {
        this.cacheManager.setHeaders(this.url, CacheManager.requestHeaderSink(
                action -> this.requestHeaders.forEach(header -> action.accept(header.getKey(), header.getValue())),
                this.setRequestHeaders::put));
    }

    @Override
    protected void checkRequestHeader(String requestHeader, String expectedValue) {
        assertEquals(
                requestHeader + ": " + expectedValue,
                requestHeader + ": " + this.setRequestHeaders.get(requestHeader));
    }
}
