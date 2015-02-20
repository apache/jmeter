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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.jmeter.protocol.http.util.HTTPConstants;

/**
 * WebDav request
 * @since 2.12
 */
public final class HttpWebdav extends HttpEntityEnclosingRequestBase {
    private static final Set<String> WEBDAV_METHODS = 
            new HashSet<String>(Arrays.asList(new String[] {
                    HTTPConstants.PROPFIND,
                    HTTPConstants.PROPPATCH,
                    HTTPConstants.MKCOL,
                    HTTPConstants.COPY,
                    HTTPConstants.MOVE,
                    HTTPConstants.LOCK,
                    HTTPConstants.UNLOCK,
                    HTTPConstants.REPORT,
                    HTTPConstants.MKCALENDAR
            }));
    
    private String davMethod;

    /**
     * 
     * @param davMethod method to use (has to be a Webdav method as identified by {@link #isWebdavMethod(String)})
     * @param uri {@link URI} to use
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
     * @param method Http Method
     * @return <code>true</code> if method is a Webdav one
     */
    public static boolean isWebdavMethod(String method) {
        return WEBDAV_METHODS.contains(method);
    }
}