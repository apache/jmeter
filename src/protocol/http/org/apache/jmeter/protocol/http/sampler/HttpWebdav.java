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

package org.apache.jmeter.protocol.http.sampler;

import java.net.URI;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * WebDav request
 *
 * @since 2.12
 */
public final class HttpWebdav extends HttpEntityEnclosingRequestBase {
    // A HTTP method can be a token as specified in
    // https://tools.ietf.org/html/rfc7230#section-3.2.6
    private static final Pattern WEBDAV_METHOD_PATTERN = Pattern.compile("(?i)[\\da-z!#$%&'*+\\-.^_`|~]+"); // only used in .matches(), no need for anchors
    
    private final String davMethod;

    /**
     * 
     * @param davMethod
     *            method to use (has to be a Webdav method as identified by
     *            {@link #isWebdavMethod(String)})
     * @param uri
     *            {@link URI} to use
     */
    public HttpWebdav(final String davMethod, final URI uri) {
        super();
        this.davMethod = davMethod;
        setURI(uri);
    }

    @Override
    public String getMethod() {
        return davMethod;
    }

    /**
     * @param method
     *            Http Method
     * @return <code>true</code> if method is a Webdav one
     */
    public static boolean isWebdavMethod(String method) {
        return method != null && WEBDAV_METHOD_PATTERN.matcher(method).matches();
    }
}
