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
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.jmeter.protocol.http.util.HTTPConstants;

public abstract class TestCacheManagerUrlConnectionBase extends TestCacheManagerBase {
    protected class URLConnectionStub extends HttpURLConnection {

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

        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }
    }
    protected URLConnection urlConnection;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.urlConnection = new URLConnectionStub(this.url.openConnection());
    }

    @Override
    public void tearDown() throws Exception {
        this.urlConnection = null;
        super.tearDown();
    }
}
