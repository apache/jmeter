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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.SourceType;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Base class for HTTP implementations used by the HTTPSamplerProxy sampler.
 */
public abstract class HTTPAbstractImpl implements Interruptible, HTTPConstantsInterface {
    private static enum CachedResourceMode {
        RETURN_200_CACHE(),
        RETURN_NO_SAMPLE(),
        RETURN_CUSTOM_STATUS();
    }

    /**
     * If true create a SampleResult with emply content and 204 response code 
     */
    private static final CachedResourceMode CACHED_RESOURCE_MODE = 
            CachedResourceMode.valueOf(
                    JMeterUtils.getPropDefault("cache_manager.cached_resource_mode", //$NON-NLS-1$
                    CachedResourceMode.RETURN_NO_SAMPLE.toString()));
    
    /**
     * SampleResult message when resource was in cache and mode is RETURN_200_CACHE
     */
    private static final String RETURN_200_CACHE_MESSAGE =
            JMeterUtils.getPropDefault("RETURN_200_CACHE.message","(ex cache)");//$NON-NLS-1$ $NON-NLS-2$

    /**
     * Custom response code for cached resource
     */
    private static final String RETURN_CUSTOM_STATUS_CODE = 
            JMeterUtils.getProperty("RETURN_CUSTOM_STATUS.code");//$NON-NLS-1$

    /**
     * Custom response message for cached resource
     */
    private static final String RETURN_CUSTOM_STATUS_MESSAGE = 
            JMeterUtils.getProperty("RETURN_CUSTOM_STATUS.message"); //$NON-NLS-1$

    protected final HTTPSamplerBase testElement;

    protected HTTPAbstractImpl(HTTPSamplerBase testElement){
        this.testElement = testElement;
    }

    protected abstract HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth);

    // Allows HTTPSamplerProxy to call threadFinished; subclasses can override if necessary
    protected void threadFinished() {
    }

    // Allows HTTPSamplerProxy to call notifyFirstSampleAfterLoopRestart; subclasses can override if necessary
    protected void notifyFirstSampleAfterLoopRestart() {
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
     * @return the encoding of the content, i.e. its charset name
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
     * Gets the IP source address (IP spoofing) if one has been provided.
     * 
     * @return the IP source address to use (or null, if none provided or the device address could not be found)
     * @throws UnknownHostException
     * @throws SocketException 
     */
    protected InetAddress getIpSourceAddress() throws UnknownHostException, SocketException {
        final String ipSource = getIpSource();
        if (ipSource.trim().length() > 0) {
            Class<? extends InetAddress> ipClass = null;
            final SourceType sourceType = HTTPSamplerBase.SourceType.values()[testElement.getIpSourceType()];
            switch (sourceType) {
            case DEVICE:
                ipClass = InetAddress.class;
                break;
            case DEVICE_IPV4:
                ipClass = Inet4Address.class;
                break;
            case DEVICE_IPV6:
                ipClass = Inet6Address.class;
                break;
            case HOSTNAME:
            default:
                return InetAddress.getByName(ipSource);
            }

            NetworkInterface net = NetworkInterface.getByName(ipSource);
            if (net != null) {
                for (InterfaceAddress ia : net.getInterfaceAddresses()) {
                    final InetAddress inetAddr = ia.getAddress();
                    if (ipClass.isInstance(inetAddr)) {
                        return inetAddr;
                    }
                }
                throw new UnknownHostException("Interface " + ipSource
                        + " does not have address of type " + ipClass.getSimpleName());
            }
            throw new UnknownHostException("Cannot find interface " + ipSource);
        }
        return null; // did not want to spoof the IP address
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
     * Determine whether to send a file as the entire body of an
     * entity enclosing request such as POST, PUT or PATCH.
     * 
     * Invokes {@link HTTPSamplerBase#getSendFileAsPostBody()}
     */
    protected boolean getSendFileAsPostBody() {
        return testElement.getSendFileAsPostBody();
    }

    /**
     * Determine whether to send concatenated parameters as the entire body of an
     * entity enclosing request such as POST, PUT or PATCH.
     * 
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
    
    /**
     * Update HTTPSampleResult for a resource in cache
     * @param res {@link HTTPSampleResult}
     * @return HTTPSampleResult
     */
    protected HTTPSampleResult updateSampleResultForResourceInCache(HTTPSampleResult res) {
        switch (CACHED_RESOURCE_MODE) {
            case RETURN_NO_SAMPLE:
                return null;
            case RETURN_200_CACHE:
                res.sampleEnd();
                res.setResponseCodeOK();
                res.setResponseMessage(RETURN_200_CACHE_MESSAGE);
                res.setSuccessful(true);
                return res;
            case RETURN_CUSTOM_STATUS:
                res.sampleEnd();
                res.setResponseCode(RETURN_CUSTOM_STATUS_CODE);
                res.setResponseMessage(RETURN_CUSTOM_STATUS_MESSAGE);
                res.setSuccessful(true);
                return res;
            default:
                // Cannot happen
                throw new IllegalStateException("Unknown CACHED_RESOURCE_MODE");
        }
    }
}
