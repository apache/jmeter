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
 */
package org.apache.jmeter.protocol.http.sampler;


import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.samplers.Interruptible;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 * This sampler uses HttpClient 3.1.
 *
 */
public class HTTPSampler2 extends HTTPSamplerBase implements Interruptible {

    private static final long serialVersionUID = 240L;

    private final transient HTTPHC3Impl hc;
    
    public HTTPSampler2(){
        hc = new HTTPHC3Impl(this);
    }

    public boolean interrupt() {
        return hc.interrupt();
    }

    @Override
    protected HTTPSampleResult sample(URL u, String method,
            boolean areFollowingRedirect, int depth) {
        return hc.sample(u, method, areFollowingRedirect, depth);
    }

    // Methods needed by subclasses to get access to the implementation
    protected HttpClient setupConnection(URL url, HttpMethodBase httpMethod, HTTPSampleResult res) 
        throws IOException {
        return hc.setupConnection(url, httpMethod, res);
    }

    protected void saveConnectionCookies(HttpMethod httpMethod, URL url,
            CookieManager cookieManager) {
        hc.saveConnectionCookies(httpMethod, url, cookieManager);
   }

    protected String getResponseHeaders(HttpMethod httpMethod) {
        return hc.getResponseHeaders(httpMethod);
    }

    protected String getConnectionHeaders(HttpMethod httpMethod) {
        return hc.getConnectionHeaders(httpMethod);
    }

    protected void setSavedClient(HttpClient savedClient) {
        hc.savedClient = savedClient;
    }

    /**
     * {@inheritDoc}
     * This implementation forwards to the implementation class.
     */
    @Override
    protected void notifySSLContextWasReset() {
        hc.notifySSLContextWasReset();
    }
}
