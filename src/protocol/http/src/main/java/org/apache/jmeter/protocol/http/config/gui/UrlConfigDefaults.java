/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.config.gui;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;

/**
 * Default option value settings for {@link UrlConfigGui}.
 */
public class UrlConfigDefaults implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Available HTTP methods to be shown in the {@link UrlConfigGui}.
     */
    private List<String> validMethodList;

    /**
     * The default HTTP method to be selected in the {@link UrlConfigGui}.
     */
    private String defaultMethod = HTTPSamplerBase.DEFAULT_METHOD;

    /**
     * The default value to be set for the followRedirect checkbox in the {@link UrlConfigGui}.
     */
    private boolean followRedirects = true;

    /**
     * The default value to be set for the autoRedirects checkbox in the {@link UrlConfigGui}.
     */
    private boolean autoRedirects;

    /**
     * The default value to be set for the useKeepAlive checkbox in the {@link UrlConfigGui}.
     */
    private boolean useKeepAlive = true;

    /**
     * The default value to be set for the useMultipart checkbox in the {@link UrlConfigGui}.
     */
    private boolean useMultipart;

    /**
     * The default value to be set for the useBrowserCompatibleMultipartMode checkbox in the {@link UrlConfigGui}.
     */
    private boolean useBrowserCompatibleMultipartMode = HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT;

    /**
     * Flag whether to show the followRedirect checkbox in the {@link UrlConfigGui}.
     */
    private boolean followRedirectsVisible = true;

    /**
     * Flag whether to show the autoRedirectsVisible checkbox in the {@link UrlConfigGui}.
     */
    private boolean autoRedirectsVisible = true;

    /**
     * Flag whether to show the useKeepAliveVisible checkbox in the {@link UrlConfigGui}.
     */
    private boolean useKeepAliveVisible = true;

    /**
     * Flag whether to show the useMultipartVisible checkbox in the {@link UrlConfigGui}.
     */
    private boolean useMultipartVisible = true;

    /**
     * Flag whether to show the useBrowserCompatibleMultipartModeVisible checkbox in the {@link UrlConfigGui}.
     */
    private boolean useBrowserCompatibleMultipartModeVisible = true;

    /**
     * Return available HTTP methods to be shown in the {@link UrlConfigGui}, returning {@link HTTPSamplerBase#getValidMethodsAsArray()}
     * by default if not reset.
     * @return available HTTP methods to be shown in the {@link UrlConfigGui}
     */
    public String[] getValidMethods() {
        if (validMethodList != null) {
            return validMethodList.toArray(new String[validMethodList.size()]);
        }
        return HTTPSamplerBase.getValidMethodsAsArray();
    }

    /**
     * Set available HTTP methods to be shown in the {@link UrlConfigGui}.
     * @param validMethods available HTTP methods
     * @throws IllegalArgumentException if the input array is empty
     */
    public void setValidMethods(String[] validMethods) {
        if (validMethods == null || validMethods.length == 0) {
            throw new IllegalArgumentException("HTTP methods array is empty.");
        }
        this.validMethodList = Arrays.asList(validMethods);
    }

    /**
     * Return the default HTTP method to be selected in the {@link UrlConfigGui}.
     * @return the default HTTP method to be selected in the {@link UrlConfigGui}
     */
    public String getDefaultMethod() {
        return defaultMethod;
    }

    /**
     * Set the default HTTP method to be selected in the {@link UrlConfigGui}.
     * @param defaultMethod the default HTTP method to be selected in the {@link UrlConfigGui}
     */
    public void setDefaultMethod(String defaultMethod) {
        this.defaultMethod = defaultMethod;
    }

    /**
     * @return the default value to be set for the followRedirect checkbox in the {@link UrlConfigGui}.
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * Set the default value to be set for the followRedirect checkbox in the {@link UrlConfigGui}.
     * @param followRedirects flag whether redirects should be followed
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * @return the default value to be set for the autoRedirects checkbox in the {@link UrlConfigGui}.
     */
    public boolean isAutoRedirects() {
        return autoRedirects;
    }

    /**
     * Set the default value to be set for the autoRedirects checkbox in the {@link UrlConfigGui}.
     * @param autoRedirects flag whether redirects should be followed automatically
     */
    public void setAutoRedirects(boolean autoRedirects) {
        this.autoRedirects = autoRedirects;
    }

    /**
     * @return the default value to be set for the useKeepAlive checkbox in the {@link UrlConfigGui}.
     */
    public boolean isUseKeepAlive() {
        return useKeepAlive;
    }

    /**
     * Set the default value to be set for the useKeepAlive checkbox in the {@link UrlConfigGui}.
     * @param useKeepAlive flag whether to use keep-alive on HTTP requests
     */
    public void setUseKeepAlive(boolean useKeepAlive) {
        this.useKeepAlive = useKeepAlive;
    }

    /**
     * @return the default value to be set for the useMultipart checkbox in the {@link UrlConfigGui}.
     */
    public boolean isUseMultipart() {
        return useMultipart;
    }

    /**
     * Set the default value to be set for the useMultipart checkbox in the {@link UrlConfigGui}.
     * @param useMultipart flag whether request data should use multi-part feature
     */
    public void setUseMultipart(boolean useMultipart) {
        this.useMultipart = useMultipart;
    }

    /**
     * @return the default value to be set for the useBrowserCompatibleMultipartMode checkbox in the {@link UrlConfigGui}.
     */
    public boolean isUseBrowserCompatibleMultipartMode() {
        return useBrowserCompatibleMultipartMode;
    }

    /**
     * Set the default value to be set for the useBrowserCompatibleMultipartMode checkbox in the {@link UrlConfigGui}.
     * @param useBrowserCompatibleMultipartMode flag whether to use browser compatible multi-part mode
     */
    public void setUseBrowserCompatibleMultipartMode(boolean useBrowserCompatibleMultipartMode) {
        this.useBrowserCompatibleMultipartMode = useBrowserCompatibleMultipartMode;
    }

    /**
     * @return {@code true} if the followRedirect checkbox should be visible in the {@link UrlConfigGui}.
     */
    public boolean isFollowRedirectsVisible() {
        return followRedirectsVisible;
    }

    /**
     * Set the visibility of the followRedirect checkbox in the {@link UrlConfigGui}.
     * @param followRedirectsVisible flag to toggle visibility in GUI
     */
    public void setFollowRedirectsVisible(boolean followRedirectsVisible) {
        this.followRedirectsVisible = followRedirectsVisible;
    }

    /**
     * @return true if the autoRedirectsVisible checkbox should be visible in the {@link UrlConfigGui}.
     */
    public boolean isAutoRedirectsVisible() {
        return autoRedirectsVisible;
    }

    /**
     * Set the visibility of the autoRedirectsVisible checkbox in the {@link UrlConfigGui}.
     * @param autoRedirectsVisible flag to toggle visibility in GUI
     */
    public void setAutoRedirectsVisible(boolean autoRedirectsVisible) {
        this.autoRedirectsVisible = autoRedirectsVisible;
    }

    /**
     * @return {@code true} if the useKeepAliveVisible checkbox should be visible in the {@link UrlConfigGui}.
     */
    public boolean isUseKeepAliveVisible() {
        return useKeepAliveVisible;
    }

    /**
     * Set the visibility of the useKeepAliveVisible checkbox in the {@link UrlConfigGui}.
     * @param useKeepAliveVisible flag to toggle visibility in GUI
     */
    public void setUseKeepAliveVisible(boolean useKeepAliveVisible) {
        this.useKeepAliveVisible = useKeepAliveVisible;
    }

    /**
     * @return {@code true} if the useMultipartVisible checkbox should by default in the {@link UrlConfigGui}.
     */
    public boolean isUseMultipartVisible() {
        return useMultipartVisible;
    }

    /**
     * Set the visibility of the useMultipartVisible checkbox in the {@link UrlConfigGui}.
     * @param useMultipartVisible flag to toggle visibility in GUI
     */
    public void setUseMultipartVisible(boolean useMultipartVisible) {
        this.useMultipartVisible = useMultipartVisible;
    }

    /**
     * @return {@code true} if the useBrowserCompatibleMultipartModeVisible checkbox should be visible in the {@link UrlConfigGui}.
     */
    public boolean isUseBrowserCompatibleMultipartModeVisible() {
        return useBrowserCompatibleMultipartModeVisible;
    }

    /**
     * Set the visibility of the useBrowserCompatibleMultipartModeVisible checkbox in the {@link UrlConfigGui}.
     * @param useBrowserCompatibleMultipartModeVisible flag to toggle visibility in GUI
     */
    public void setUseBrowserCompatibleMultipartModeVisible(boolean useBrowserCompatibleMultipartModeVisible) {
        this.useBrowserCompatibleMultipartModeVisible = useBrowserCompatibleMultipartModeVisible;
    }
}
