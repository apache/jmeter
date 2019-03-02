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

import java.net.URL;

import org.apache.jmeter.testelement.property.CollectionProperty;

/**
 * Interface to be implemented by CookieHandler
 */
public interface CookieHandler {

    /**
     * Add cookie to CookieManager from cookieHeader and URL
     * @param cookieManager CookieManager on which cookies are added
     * @param checkCookies boolean to indicate if cookies must be validated against spec
     * @param cookieHeader String cookie Header
     * @param url URL
     */
    void addCookieFromHeader(CookieManager cookieManager, boolean checkCookies,
            String cookieHeader, URL url);

    /**
     * Find cookies applicable to the given URL and build the Cookie header from
     * them.
     * @param cookiesCP {@link CollectionProperty} of {@link Cookie}
     * @param url
     *            URL of the request to which the returned header will be added.
     * @param allowVariableCookie flag whether to allow jmeter variables in cookie values
     * @return the value string for the cookie header (goes after "Cookie: ") or null if no cookie matches
     */
    String getCookieHeaderForURL(CollectionProperty cookiesCP, URL url,
            boolean allowVariableCookie);

    /**
     * @return Cookie default policy name
     */
    String getDefaultPolicy();

    /**
     * @return Supported cookie policies
     */
    String[] getPolicies();

}
