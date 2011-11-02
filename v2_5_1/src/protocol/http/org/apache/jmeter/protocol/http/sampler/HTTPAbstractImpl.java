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
    
    /**
     * Invokes {@link HTTPSamplerBase#errorResult(Throwable, HTTPSampleResult)}
     */
    protected HTTPSampleResult errorResult(Throwable t, HTTPSampleResult res) {
        return testElement.errorResult(t, res);
    }

    /**
     * Invokes {@link HTTPSamplerBase#getArguments()}
     */
    protected Arguments getArguments() {
        return testElement.getArguments();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getAuthManager()}
     */
    protected AuthManager getAuthManager() {
        return testElement.getAuthManager();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getAutoRedirects()}
     */
    protected boolean getAutoRedirects() {
        return testElement.getAutoRedirects();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getCacheManager()}
     */
    protected CacheManager getCacheManager() {
        return testElement.getCacheManager();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getConnectTimeout()}
     */
    protected int getConnectTimeout() {
        return testElement.getConnectTimeout();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getContentEncoding()}
     */
    protected String getContentEncoding() {
        return testElement.getContentEncoding();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getCookieManager()}
     */
    protected CookieManager getCookieManager() {
        return testElement.getCookieManager();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getHeaderManager()}
     */
    protected HeaderManager getHeaderManager() {
        return testElement.getHeaderManager();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getHTTPFiles()}
     */
    protected HTTPFileArg[] getHTTPFiles() {
        return testElement.getHTTPFiles();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getIpSource()}
     */
    protected String getIpSource() {
        return testElement.getIpSource();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getProxyHost()}
     */
    protected String getProxyHost() {
        return testElement.getProxyHost();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getProxyPass()}
     */
    protected String getProxyPass() {
        return testElement.getProxyPass();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getProxyPortInt()}
     */
    protected int getProxyPortInt() {
        return testElement.getProxyPortInt();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getProxyUser()}
     */
    protected String getProxyUser() {
        return testElement.getProxyUser();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getResponseTimeout()}
     */
    protected int getResponseTimeout() {
        return testElement.getResponseTimeout();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getSendFileAsPostBody()}
     */
    protected boolean getSendFileAsPostBody() {
        return testElement.getSendFileAsPostBody();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getSendParameterValuesAsPostBody()}
     */
    protected boolean getSendParameterValuesAsPostBody() {
        return testElement.getSendParameterValuesAsPostBody();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getUseKeepAlive()}
     */
    protected boolean getUseKeepAlive() {
        return testElement.getUseKeepAlive();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getUseMultipartForPost()}
     */
    protected boolean getUseMultipartForPost() {
        return testElement.getUseMultipartForPost();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getDoBrowserCompatibleMultipart()}
     */
    protected boolean getDoBrowserCompatibleMultipart() {
        return testElement.getDoBrowserCompatibleMultipart();
    }

    /**
     * Invokes {@link HTTPSamplerBase#hasArguments()}
     */
    protected boolean hasArguments() {
        return testElement.hasArguments();
    }

    /**
     * Invokes {@link HTTPSamplerBase#isMonitor()}
     */
    protected boolean isMonitor() {
        return testElement.isMonitor();
    }

    /**
     * Invokes {@link HTTPSamplerBase#isSuccessCode(int)}
     */
    protected boolean isSuccessCode(int errorLevel) {
        return testElement.isSuccessCode(errorLevel);
    }

    /**
     * Invokes {@link HTTPSamplerBase#readResponse(SampleResult, InputStream, int)}
     */
    protected byte[] readResponse(SampleResult res, InputStream instream,
            int responseContentLength) throws IOException {
        return testElement.readResponse(res, instream, responseContentLength);
    }

    /**
     * Invokes {@link HTTPSamplerBase#readResponse(SampleResult, InputStream, int)}
     */
    protected byte[] readResponse(SampleResult res, BufferedInputStream in,
            int contentLength) throws IOException {
        return testElement.readResponse(res, in, contentLength);
    }

    /**
     * Invokes {@link HTTPSamplerBase#resultProcessing(boolean, int, HTTPSampleResult)}
     */
    protected HTTPSampleResult resultProcessing(boolean areFollowingRedirect,
            int frameDepth, HTTPSampleResult res) {
        return testElement.resultProcessing(areFollowingRedirect, frameDepth, res);
    }

    /**
     * Invokes {@link HTTPSamplerBase#setUseKeepAlive(boolean)}
     */
    protected void setUseKeepAlive(boolean b) {
        testElement.setUseKeepAlive(b);
    }

    /**
     * Called by testIterationStart if the SSL Context was reset.
     * 
     * This implementation does nothing.
     */
    protected void notifySSLContextWasReset() {
        // NOOP
    }
}
