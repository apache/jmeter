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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures the URLConnection-based post writer output in a temporary file for the JDK HTTP/2 client.
 */
final class Http2CapturingHttpURLConnection extends HttpURLConnection {
    private final Map<String, List<String>> requestProperties = new LinkedHashMap<>();
    private SpillOutputStream spillOutput;

    Http2CapturingHttpURLConnection(URL url, String method) {
        super(url);
        this.method = method;
    }

    @Override
    public void setRequestProperty(String key, String value) {
        if (key == null) {
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(value);
        requestProperties.put(key, list);
    }

    @Override
    public void addRequestProperty(String key, String value) {
        if (key != null) {
            requestProperties.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    @Override
    public String getRequestProperty(String key) {
        if (key == null) {
            return null;
        }
        List<String> values = requestProperties.get(key);
        if (values == null || values.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : requestProperties.entrySet()) {
                if (key.equalsIgnoreCase(entry.getKey())) {
                    values = entry.getValue();
                    break;
                }
            }
        }
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        return Collections.unmodifiableMap(requestProperties);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (spillOutput == null) {
            spillOutput = new SpillOutputStream(256 * 1024, "jmeter-http2-request-", ".tmp");
        }
        return spillOutput;
    }

    /** Flushes and closes the capture buffer, so the captured body can be read back safely. */
    void finishCapture() throws IOException {
        if (spillOutput != null) {
            spillOutput.close();
        }
    }

    boolean isSpilled() {
        return spillOutput != null && spillOutput.isSpilled();
    }

    Path getCapturedBody() {
        return spillOutput != null ? spillOutput.getTempFile() : null;
    }

    byte[] getCapturedByteArray() {
        return spillOutput != null ? spillOutput.toByteArray() : null;
    }

    long getCapturedBodyLength() {
        return spillOutput == null ? -1 : spillOutput.getLength();
    }

    void deleteCapturedBody() {
        if (spillOutput != null) {
            spillOutput.releaseResources();
        }
    }

    @Override
    public void connect() {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public boolean usingProxy() {
        return false;
    }

    @Override
    public String getHeaderField(int n) {
        return null;
    }

    @Override
    public String getHeaderFieldKey(int n) {
        return null;
    }

    @Override
    public String getHeaderField(String name) {
        return null;
    }
}
