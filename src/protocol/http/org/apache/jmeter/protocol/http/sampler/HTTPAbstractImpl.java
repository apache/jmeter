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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Base class for HTTP implementations used by the HTTPSamplerProxy sampler.
 */
public abstract class HTTPAbstractImpl implements Interruptible, HTTPConstantsInterface {

    protected final HTTPSamplerBase testElement;

    protected HTTPAbstractImpl(HTTPSamplerBase testElement){
        this.testElement = testElement;
    }

    protected abstract HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth);

    // Allows HTTPSamplerProxy to call threadFinished; subclasses can override if necessary
    protected void threadFinished() {
    }

    // Provide access to HTTPSamplerBase methods
    
    protected HeaderManager getHeaderManager() {
        return testElement.getHeaderManager();
    }

    protected HTTPFileArg[] getHTTPFiles() {
        return testElement.getHTTPFiles();
    }

    protected AuthManager getAuthManager() {
        return testElement.getAuthManager();
    }

    protected Arguments getArguments() {
        return testElement.getArguments();
    }

    protected CookieManager getCookieManager() {
        return testElement.getCookieManager();
    }

    protected HTTPSampleResult errorResult(IOException iex, HTTPSampleResult res) {
        return testElement.errorResult(iex, res);
    }

    protected byte[] readResponse(SampleResult res, BufferedInputStream in,
            int contentLength) throws IOException {
        return testElement.readResponse(res, in, contentLength);
    }

    protected CacheManager getCacheManager() {
        return testElement.getCacheManager();
    }

    protected boolean getUseKeepAlive() {
        return testElement.getUseKeepAlive();
    }

    protected int getResponseTimeout() {
        return testElement.getResponseTimeout();
    }

    protected int getConnectTimeout() {
        return testElement.getConnectTimeout();
    }

    protected boolean getAutoRedirects() {
        return testElement.getAutoRedirects();
    }

    protected int getProxyPortInt() {
        return testElement.getProxyPortInt();
    }

    protected String getProxyHost() {
        return testElement.getProxyHost();
    }

    protected HTTPSampleResult resultProcessing(boolean areFollowingRedirect,
            int frameDepth, HTTPSampleResult res) {
        return testElement.resultProcessing(areFollowingRedirect, frameDepth, res);
    }

    protected boolean isSuccessCode(int errorLevel) {
        return testElement.isSuccessCode(errorLevel);
    }

    protected void setUseKeepAlive(boolean b) {
        testElement.setUseKeepAlive(b);
    }

    protected boolean isMonitor() {
        return testElement.isMonitor();
    }
    protected boolean getSendParameterValuesAsPostBody() {
        return testElement.getSendParameterValuesAsPostBody();
    }

    protected boolean getSendFileAsPostBody() {
        return testElement.getSendFileAsPostBody();
    }

    protected boolean hasArguments() {
        return testElement.hasArguments();
    }

    protected String getContentEncoding() {
        return testElement.getContentEncoding();
    }

    protected boolean getUseMultipartForPost() {
        return testElement.getUseMultipartForPost();
    }

    protected String getProxyPass() {
        return testElement.getProxyPass();
    }

    protected String getProxyUser() {
        return testElement.getProxyUser();
    }

    protected String getIpSource() {
        return testElement.getIpSource();
    }

    protected HTTPSampleResult errorResult(IllegalArgumentException e,
            HTTPSampleResult res) {
        return testElement.errorResult(e, res);
    }

    protected byte[] readResponse(HTTPSampleResult res, InputStream instream,
            int responseContentLength) throws IOException {
        return testElement.readResponse(res, instream, responseContentLength);
    }

}
