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

package org.apache.jmeter.protocol.http.parser;

import java.net.URL;

/**
 * Helper class to allow URLs to be stored in Collections without incurring the
 * cost of the hostname lookup performed by the URL methods equals() and
 * hashCode() URL is a final class, so cannot be extended ...
 *
 */
public class URLString implements Comparable<URLString> {

    private final URL url;

    private final String urlAsString;

    private final int hashCode;

    public URLString(URL u) {
        url = u;
        urlAsString = u.toExternalForm();
        /*
         * TODO improve string version to better match browser behaviour? e.g.
         * do browsers regard http://host/ and http://Host:80/ as the same? If
         * so, it would be better to reflect this in the string
         */

        hashCode = urlAsString.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return urlAsString;
    }

    public URL getURL() {
        return url;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(URLString o) {
        return urlAsString.compareTo(o.toString());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        return o instanceof URLString && urlAsString.equals(o.toString());
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return hashCode;
    }
}
