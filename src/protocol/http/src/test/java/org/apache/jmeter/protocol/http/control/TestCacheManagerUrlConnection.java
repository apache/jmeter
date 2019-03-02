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
import static org.junit.Assert.assertNotNull;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;

public class TestCacheManagerUrlConnection extends TestCacheManagerUrlConnectionBase {

    @Override
    protected void cacheResult(HTTPSampleResult result) throws Exception {
        this.cacheManager.saveDetails(this.urlConnection, result);
    }

    @Override
    protected void setExpires(String expires) {
        ((URLConnectionStub) urlConnection).expires = expires;
    }

    @Override
    protected void setCacheControl(String cacheControl) {
        ((URLConnectionStub) urlConnection).cacheControl = cacheControl;
    }

    @Override
    protected void setLastModified(String lastModified) {
        ((URLConnectionStub) urlConnection).lastModifiedHeader = lastModified;
    }

    @Override
    protected void checkRequestHeader(String requestHeader, String expectedValue) {
        Map<String, List<String>> properties = this.urlConnection.getRequestProperties();
        checkProperty(properties, requestHeader, expectedValue);
    }

    @Override
    protected void addRequestHeader(String requestHeader, String value) {
        // no-op
    }

    private org.apache.jmeter.protocol.http.control.Header[] asHeaders(Map<String, List<String>> headers) {
        // Java Implementation returns a null header for URL
        return headers.entrySet().stream()
                .filter(header -> header.getKey() != null)
                .map(header -> new Header(header.getKey(), String.join(", ", header.getValue())))
                .toArray(Header[]::new);
    }

    @Override
    protected void setRequestHeaders() {
        this.cacheManager.setHeaders(
                (HttpURLConnection)this.urlConnection,
                asHeaders(urlConnection.getHeaderFields()),
                this.url);
    }

    private static void checkProperty(Map<String, List<String>> properties, String property, String expectedPropertyValue) {
        assertNotNull(
                "Properties should not be null. Expected to find within it property = "
                        + property + " with expected value = "
                        + expectedPropertyValue,
                properties);
        List<String> listOfPropertyValues = properties.get(property);
        assertNotNull("No property entry found for property " + property, listOfPropertyValues);
        assertEquals("Did not find single property for property " + property, 1, listOfPropertyValues.size());
        assertEquals("Unexpected value for property " + property, expectedPropertyValue, listOfPropertyValues.get(0));
    }

}
