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

import org.apache.commons.httpclient.Header;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;

import static org.junit.Assert.assertEquals;

public class TestCacheManagerHttpMethod extends TestCacheManagerUrlConnectionBase {

    @Override
    protected void setExpires(String expires) {
        ((HttpMethodStub) httpMethod).expires = expires;
    }

    @Override
    protected void setCacheControl(String cacheControl) {
        ((HttpMethodStub) httpMethod).cacheControl = cacheControl;
    }

    @Override
    protected void setLastModified(String lastModified) {
        ((HttpMethodStub) httpMethod).lastModifiedHeader =
                new org.apache.commons.httpclient.Header(HTTPConstants.LAST_MODIFIED,
                        lastModified);
    }

    @Override
    protected void cacheResult(HTTPSampleResult result) throws Exception {
        this.cacheManager.saveDetails(this.httpMethod, result);
    }

    @Override
    protected void addRequestHeader(String requestHeader, String value) {
        this.httpMethod.addRequestHeader(new Header(requestHeader, value, false));
    }

    @Override
    protected void setRequestHeaders() {
        this.cacheManager.setHeaders(this.url, this.httpMethod);
    }

    @Override
    protected void checkRequestHeader(String requestHeader, String expectedValue) {
        org.apache.commons.httpclient.Header header = this.httpMethod.getRequestHeader(requestHeader);
        assertEquals("Wrong name in header for " + requestHeader, requestHeader, header.getName());
        assertEquals("Wrong value for header " + header, expectedValue, header.getValue());
    }

}
