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
import java.util.function.Predicate;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.SourceType;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Base class for HTTP implementations used by the HTTPSamplerProxy sampler.
 */
public abstract class HTTPAbstractImpl implements Interruptible, HTTPConstantsInterface {
    private enum CachedResourceMode {
        RETURN_200_CACHE(),
        RETURN_NO_SAMPLE(),
        RETURN_CUSTOM_STATUS()
    }

    /**
     * Should we add to POST request content-type header if missing:
     * Content-Type: application/x-www-form-urlencoded
     */
    protected static final boolean ADD_CONTENT_TYPE_TO_POST_IF_MISSING = 
            JMeterUtils.getPropDefault("http.post_add_content_type_if_missing", //$NON-NLS-1$
                    false);

    /**
     * If true create a SampleResult with empty content and 204 response code 
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

    protected static final Predicate<String> ALL_EXCEPT_COOKIE = s -> !HTTPConstants.HEADER_COOKIE.equalsIgnoreCase(s);
    
    protected static final Predicate<String> ONLY_COOKIE = s -> HTTPConstants.HEADER_COOKIE.equalsIgnoreCase(s);

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
     * Populates the provided HTTPSampleResult with details from the Exception.
     * Does not create a new instance, so should not be used directly to add a
     * subsample.
     * <p>
     * See {@link HTTPSamplerBase#errorResult(Throwable, HTTPSampleResult)}
     * 
     * @param t
     *            Exception representing the error.
     * @param res
     *            SampleResult to be modified
     * @return the modified sampling result containing details of the Exception.
     *         Invokes
     */
    protected HTTPSampleResult errorResult(Throwable t, HTTPSampleResult res) {
        return testElement.errorResult(t, res);
    }

    /**
     * Invokes {@link HTTPSamplerBase#getArguments()}
     *
     * @return the arguments of the associated test element
     */
    protected Arguments getArguments() {
        return testElement.getArguments();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getAuthManager()}
     *
     * @return the {@link AuthManager} of the associated test element
     */
    protected AuthManager getAuthManager() {
        return testElement.getAuthManager();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getAutoRedirects()}
     *
     * @return flag whether to do auto redirects
     */
    protected boolean getAutoRedirects() {
        return testElement.getAutoRedirects();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getCacheManager()}
     *
     * @return the {@link CacheManager} of the associated test element
     */
    protected CacheManager getCacheManager() {
        return testElement.getCacheManager();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getConnectTimeout()}
     *
     * @return the connect timeout of the associated test element
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
     *
     * @return the {@link CookieManager} of the associated test element
     */
    protected CookieManager getCookieManager() {
        return testElement.getCookieManager();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getHeaderManager()}
     *
     * @return the {@link HeaderManager} of the associated test element
     */
    protected HeaderManager getHeaderManager() {
        return testElement.getHeaderManager();
    }

    /**
     * 
     * Get the collection of files as a list.
     * The list is built up from the filename/filefield/mimetype properties,
     * plus any additional entries saved in the FILE_ARGS property.
     * <p>
     * If there are no valid file entries, then an empty list is returned.
     * <p>
     * Invokes {@link HTTPSamplerBase#getHTTPFiles()}
     *
     * @return an array of file arguments (never <code>null</code>)
     */
    protected HTTPFileArg[] getHTTPFiles() {
        return testElement.getHTTPFiles();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getIpSource()}
     *
     * @return the configured ip source for the associated test element
     */
    protected String getIpSource() {
        return testElement.getIpSource();
    }

    /**
     * Gets the IP source address (IP spoofing) if one has been provided.
     * 
     * @return the IP source address to use (or <code>null</code>, if none provided or the device address could not be found)
     * @throws UnknownHostException if the hostname/ip for {@link #getIpSource()} could not be resolved or not interface was found for it
     * @throws SocketException if an I/O error occurs
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
     * Invokes {@link HTTPSamplerBase#getProxyScheme()}
     *
     * @return the configured host scheme to use for proxy
     */
    protected String getProxyScheme() {
        return testElement.getProxyScheme();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getProxyHost()}
     *
     * @return the configured host to use as a proxy
     */
    protected String getProxyHost() {
        return testElement.getProxyHost();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getProxyPass()}
     *
     * @return the configured password to use for the proxy
     */
    protected String getProxyPass() {
        return testElement.getProxyPass();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getProxyPortInt()}
     *
     * @return the configured port to use for the proxy
     */
    protected int getProxyPortInt() {
        return testElement.getProxyPortInt();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getProxyUser()}
     *
     * @return the configured user to use for the proxy
     */
    protected String getProxyUser() {
        return testElement.getProxyUser();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getResponseTimeout()}
     *
     * @return the configured timeout for responses
     */
    protected int getResponseTimeout() {
        return testElement.getResponseTimeout();
    }

    /**
     * Determine whether to send a file as the entire body of an
     * entity enclosing request such as POST, PUT or PATCH.
     * 
     * Invokes {@link HTTPSamplerBase#getSendFileAsPostBody()}
     *
     * @return flag whether to send a file as POST, PUT or PATCH
     */
    protected boolean getSendFileAsPostBody() {
        return testElement.getSendFileAsPostBody();
    }

    /**
     * Determine whether to send concatenated parameters as the entire body of an
     * entity enclosing request such as POST, PUT or PATCH.
     * 
     * Invokes {@link HTTPSamplerBase#getSendParameterValuesAsPostBody()}
     *
     * @return flag whether to send concatenated parameters as the entire body
     */
    protected boolean getSendParameterValuesAsPostBody() {
        return testElement.getSendParameterValuesAsPostBody();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getUseKeepAlive()}
     *
     * @return flag whether to use keep-alive for requests
     */
    protected boolean getUseKeepAlive() {
        return testElement.getUseKeepAlive();
    }

    /**
     * Determine if we should use <code>multipart/form-data</code> or
     * <code>application/x-www-form-urlencoded</code> for the post
     * <p>
     * Invokes {@link HTTPSamplerBase#getUseMultipartForPost()}
     *
     * @return <code>true</code> if <code>multipart/form-data</code> should be
     *         used and method is POST
     * @deprecated Use {@link HTTPAbstractImpl#getUseMultipart()}
     */
    @Deprecated
    protected boolean getUseMultipartForPost() {
        return testElement.getUseMultipartForPost();
    }
    
    /**
     * Determine if we should use <code>multipart/form-data</code> or
     * <code>application/x-www-form-urlencoded</code> for the method
     * <p>
     * Invokes {@link HTTPSamplerBase#getUseMultipart()}
     *
     * @return <code>true</code> if <code>multipart/form-data</code> should be used 
     */
    protected boolean getUseMultipart() {
        return testElement.getUseMultipart();
    }

    /**
     * Invokes {@link HTTPSamplerBase#getDoBrowserCompatibleMultipart()}
     *
     * @return flag whether we should do browser compatible multiparts
     */
    protected boolean getDoBrowserCompatibleMultipart() {
        return testElement.getDoBrowserCompatibleMultipart();
    }

    /**
     * Invokes {@link HTTPSamplerBase#hasArguments()}
     *
     * @return flag whether we have arguments to send
     */
    protected boolean hasArguments() {
        return testElement.hasArguments();
    }

    /**
     * Invokes {@link HTTPSamplerBase#isMonitor()}
     *
     * @return flag whether monitor is enabled
     * @deprecated since 3.2 always return false
     */
    @Deprecated
    protected boolean isMonitor() {
        return false;
    }

    /**
     * Determine if the HTTP status code is successful or not i.e. in range 200
     * to 399 inclusive
     * <p>
     * Invokes {@link HTTPSamplerBase#isSuccessCode(int)}
     *
     * @param errorLevel
     *            status code to check
     * @return whether in range 200-399 or not
     */
    protected boolean isSuccessCode(int errorLevel) {
        return testElement.isSuccessCode(errorLevel);
    }

    /**
     * Read response from the input stream, converting to MD5 digest if the
     * useMD5 property is set.
     * <p>
     * For the MD5 case, the result byte count is set to the size of the
     * original response.
     * <p>
     * Closes the inputStream
     * <p>
     * Invokes
     * {@link HTTPSamplerBase#readResponse(SampleResult, InputStream, long)}
     * 
     * @param res
     *            sample to store information about the response into
     * @param instream
     *            input stream from which to read the response
     * @param responseContentLength
     *            expected input length or zero
     * @return the response or the MD5 of the response
     * @throws IOException
     *             if reading the result fails
     */
    protected byte[] readResponse(SampleResult res, InputStream instream,
            int responseContentLength) throws IOException {
        return readResponse(res, instream, (long)responseContentLength);
    }
    /**
     * Read response from the input stream, converting to MD5 digest if the
     * useMD5 property is set.
     * <p>
     * For the MD5 case, the result byte count is set to the size of the
     * original response.
     * <p>
     * Closes the inputStream
     * <p>
     * Invokes
     * {@link HTTPSamplerBase#readResponse(SampleResult, InputStream, long)}
     * 
     * @param res
     *            sample to store information about the response into
     * @param instream
     *            input stream from which to read the response
     * @param responseContentLength
     *            expected input length or zero
     * @return the response or the MD5 of the response
     * @throws IOException
     *             if reading the result fails
     */
    protected byte[] readResponse(SampleResult res, InputStream instream,
            long responseContentLength) throws IOException {
        return testElement.readResponse(res, instream, responseContentLength);
    }

    /**
     * Read response from the input stream, converting to MD5 digest if the
     * useMD5 property is set.
     * <p>
     * For the MD5 case, the result byte count is set to the size of the
     * original response.
     * <p>
     * Closes the inputStream
     * <p>
     * Invokes {@link HTTPSamplerBase#readResponse(SampleResult, InputStream, long)}
     * 
     * @param res
     *            sample to store information about the response into
     * @param in
     *            input stream from which to read the response
     * @param contentLength
     *            expected input length or zero
     * @return the response or the MD5 of the response
     * @throws IOException
     *             when reading the result fails
     * @deprecated use {@link HTTPAbstractImpl#readResponse(SampleResult, BufferedInputStream, long)}
     */
    @Deprecated
    protected byte[] readResponse(SampleResult res, BufferedInputStream in,
            int contentLength) throws IOException {
        return testElement.readResponse(res, in, contentLength);
    }
    
    /**
     * Read response from the input stream, converting to MD5 digest if the
     * useMD5 property is set.
     * <p>
     * For the MD5 case, the result byte count is set to the size of the
     * original response.
     * <p>
     * Closes the inputStream
     * <p>
     * Invokes {@link HTTPSamplerBase#readResponse(SampleResult, InputStream, long)}
     * 
     * @param res
     *            sample to store information about the response into
     * @param in
     *            input stream from which to read the response
     * @param contentLength
     *            expected input length or zero
     * @return the response or the MD5 of the response
     * @throws IOException
     *             when reading the result fails
     */
    protected byte[] readResponse(SampleResult res, BufferedInputStream in,
            long contentLength) throws IOException {
        return testElement.readResponse(res, in, contentLength);
    }

    /**
     * Follow redirects and download page resources if appropriate. this works,
     * but the container stuff here is what's doing it. followRedirects() is
     * actually doing the work to make sure we have only one container to make
     * this work more naturally, I think this method - sample() - needs to take
     * an HTTPSamplerResult container parameter instead of a
     * boolean:areFollowingRedirect.
     * <p>
     * Invokes
     * {@link HTTPSamplerBase#resultProcessing(boolean, int, HTTPSampleResult)}
     *
     * @param areFollowingRedirect
     *            flag whether we are getting a redirect target
     * @param frameDepth
     *            Depth of this target in the frame structure. Used only to
     *            prevent infinite recursion.
     * @param res
     *            sample result to process
     * @return the sample result
     */
    protected HTTPSampleResult resultProcessing(boolean areFollowingRedirect,
            int frameDepth, HTTPSampleResult res) {
        return testElement.resultProcessing(areFollowingRedirect, frameDepth, res);
    }

    /**
     * Invokes {@link HTTPSamplerBase#setUseKeepAlive(boolean)}
     *
     * @param b flag whether to use keep-alive for requests
     */
    protected void setUseKeepAlive(boolean b) {
        testElement.setUseKeepAlive(b);
    }

    /**
     * Called by testIterationStart if the SSL Context was reset.
     * 
     * This implementation does nothing.
     * @deprecated ** unused since r1489189. **
     */
    @Deprecated
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
