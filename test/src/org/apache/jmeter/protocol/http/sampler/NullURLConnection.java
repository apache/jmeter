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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

/**
 * Dummy URLConnection class for use by classes that need an
 * URLConnection for junit tests.
 *
 */
public final class NullURLConnection extends URLConnection {

    private final Properties data = new Properties();

    public NullURLConnection() throws MalformedURLException {
        this(new URL("http://localhost"));
    }

    public NullURLConnection(URL url) {
        super(url);
    }

    @Override
    public void connect() {
    }

    @Override
    public void setRequestProperty(String name, String value) {
        data.put(name, value);
    }

    @Override
    public String getRequestProperty(String name) {
        return (String) data.get(name);
    }
}
