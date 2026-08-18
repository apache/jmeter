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

package org.apache.jmeter.protocol.http.sampler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.jorphan.io.CountingOutputStream;

/**
 * Delegating {@link HttpURLConnection} that counts the bytes actually written to the request body.
 * <p>
 * It is used to compute the sent bytes of a sample, since the {@code String} returned by
 * {@link PostWriter#sendPostData(java.net.URLConnection, HTTPSamplerBase)} is only a human readable
 * representation of the request body (e.g. file contents are replaced with a placeholder).
 */
final class CountingOutputHttpURLConnection extends HttpURLConnection {
    private final HttpURLConnection delegate;
    private CountingOutputStream countingOutput;

    CountingOutputHttpURLConnection(HttpURLConnection delegate) {
        super(delegate.getURL());
        this.delegate = delegate;
    }

    /**
     * @return the number of bytes written to the request body, or {@code -1} if the body was never opened
     */
    long getBytesWritten() {
        return countingOutput == null ? -1 : countingOutput.getBytesWritten();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (countingOutput == null) {
            countingOutput = new CountingOutputStream(delegate.getOutputStream());
        }
        return countingOutput;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @Override
    public void setRequestProperty(String key, String value) {
        delegate.setRequestProperty(key, value);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        delegate.addRequestProperty(key, value);
    }

    @Override
    public String getRequestProperty(String key) {
        return delegate.getRequestProperty(key);
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return delegate.getRequestProperties();
    }

    @Override
    public void setDoOutput(boolean doOutput) {
        delegate.setDoOutput(doOutput);
    }

    @Override
    public boolean getDoOutput() {
        return delegate.getDoOutput();
    }

    @Override
    public void setDoInput(boolean doInput) {
        delegate.setDoInput(doInput);
    }

    @Override
    public boolean getDoInput() {
        return delegate.getDoInput();
    }

    @Override
    public URL getURL() {
        return delegate.getURL();
    }

    @Override
    public String getRequestMethod() {
        return delegate.getRequestMethod();
    }

    @Override
    public String getHeaderField(String name) {
        return delegate.getHeaderField(name);
    }

    @Override
    public String getHeaderField(int n) {
        return delegate.getHeaderField(n);
    }

    @Override
    public String getHeaderFieldKey(int n) {
        return delegate.getHeaderFieldKey(n);
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return delegate.getHeaderFields();
    }

    @Override
    public void connect() throws IOException {
        delegate.connect();
    }

    @Override
    public void disconnect() {
        delegate.disconnect();
    }

    @Override
    public boolean usingProxy() {
        return delegate.usingProxy();
    }
}
