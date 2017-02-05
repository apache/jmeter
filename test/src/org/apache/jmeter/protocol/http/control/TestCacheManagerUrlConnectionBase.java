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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.jmeter.protocol.http.util.HTTPConstants;

public abstract class TestCacheManagerUrlConnectionBase extends TestCacheManagerBase {
    protected class URLConnectionStub extends URLConnection {

        protected URLConnectionStub(URL url) {
            super(url);
        }

        private URLConnectionStub(URLConnection urlConnection) {
            super(urlConnection.getURL());
        }

        @Override
        public void connect() throws IOException {
        }

        protected String expires = null;
        protected String cacheControl = null;
        protected String lastModifiedHeader = currentTimeInGMT;

        @Override
        public String getHeaderField(String name) {
            if (HTTPConstants.LAST_MODIFIED.equals(name)) {
                return lastModifiedHeader;
            } else if (HTTPConstants.ETAG.equals(name)) {
                return EXPECTED_ETAG;
            } else if (HTTPConstants.EXPIRES.equals(name)) {
                return expires;
            } else if (HTTPConstants.CACHE_CONTROL.equals(name)) {
                return cacheControl;
            } else if (HTTPConstants.DATE.equals(name)) {
                return currentTimeInGMT;
            } else if (HTTPConstants.VARY.equals(name)) {
                return vary;
            }
            return super.getHeaderField(name);
        }

        @Override
        public URL getURL() {
            return url;
        }
    }

    protected class HttpMethodStub extends PostMethod {
        protected org.apache.commons.httpclient.Header lastModifiedHeader;
        protected org.apache.commons.httpclient.Header etagHeader;
        protected String expires;
        protected String cacheControl;
        protected org.apache.commons.httpclient.Header dateHeader;

        HttpMethodStub() {
            this.lastModifiedHeader = new org.apache.commons.httpclient.Header(HTTPConstants.LAST_MODIFIED, currentTimeInGMT);
            this.dateHeader = new org.apache.commons.httpclient.Header(HTTPConstants.DATE, currentTimeInGMT);
            this.etagHeader = new org.apache.commons.httpclient.Header(HTTPConstants.ETAG, EXPECTED_ETAG);
        }

        @Override
        public org.apache.commons.httpclient.Header getResponseHeader(String headerName) {
            if (HTTPConstants.LAST_MODIFIED.equals(headerName)) {
                return this.lastModifiedHeader;
            } else if (HTTPConstants.ETAG.equals(headerName)) {
                return this.etagHeader;
            } else if (HTTPConstants.EXPIRES.equals(headerName)) {
                return expires == null ? null : new org.apache.commons.httpclient.Header(HTTPConstants.EXPIRES, expires);
            } else if (HTTPConstants.CACHE_CONTROL.equals(headerName)) {
                return cacheControl == null ? null : new org.apache.commons.httpclient.Header(HTTPConstants.CACHE_CONTROL, cacheControl);
            } else if (HTTPConstants.DATE.equals(headerName)) {
                return this.dateHeader;
            } else if (HTTPConstants.VARY.equals(headerName)) {
                return vary == null ? null : new org.apache.commons.httpclient.Header(HTTPConstants.VARY, vary);
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
            this.properties = new HashMap<>();
        }

        @Override
        public void addRequestProperty(String key, String value) {
            List<String> list = new ArrayList<>();
            list.add(value);
            this.properties.put(key, list);
        }

        @Override
        public Map<String, List<String>> getRequestProperties() {
            return this.properties;
        }

    }

    private URI uri;
    protected URLConnection urlConnection;
    protected HttpMethod httpMethod;
    protected HttpURLConnection httpUrlConnection;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.uri = new URI(LOCAL_HOST, false);
        this.urlConnection = new URLConnectionStub(this.url.openConnection());
        this.httpMethod = new HttpMethodStub();
        this.httpUrlConnection = new HttpURLConnectionStub(this.httpMethod, this.url);
        this.httpMethod.setURI(this.uri);
    }

    @Override
    public void tearDown() throws Exception {
        this.httpUrlConnection = null;
        this.httpMethod = null;
        this.urlConnection = null;
        this.uri = null;
        super.tearDown();
    }
}
