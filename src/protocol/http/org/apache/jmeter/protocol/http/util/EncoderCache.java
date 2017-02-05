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

package org.apache.jmeter.protocol.http.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.oro.util.Cache;
import org.apache.oro.util.CacheLRU;

public class EncoderCache {

    /** The encoding which should be usd for URLs, according to HTTP specification */
    public static final String URL_ARGUMENT_ENCODING = StandardCharsets.UTF_8.name();

    private Cache cache;

    public EncoderCache(int cacheSize) {
        cache = new CacheLRU(cacheSize);
    }


    /**
     * Get the specified value URL encoded using UTF-8 encoding
     *
     * @param k the value to encode
     * @return the value URL encoded using UTF-8
     */
    public String getEncoded(String k) {
        try {
            return getEncoded(k, URL_ARGUMENT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            // This can't happen (how should utf8 not be supported!?!),
            // so just throw an Error:
            throw new Error("Should not happen: " + e.toString());
        }
    }

    /**
     * Get the specified value URL encoded using the specified encoding
     *
     * @param k the value to encode
     * @param contentEncoding the encoding to use when URL encoding
     * @return the value URL encoded using the specified encoding
     * @throws UnsupportedEncodingException if the specified encoding is not supported
     */
    public String getEncoded(String k, String contentEncoding) throws UnsupportedEncodingException {
        String cacheKey = k + contentEncoding;
        // Check if we have it in the cache
        Object encodedValue = cache.getElement(cacheKey);
        if (encodedValue != null) {
            return (String) encodedValue;
        }
        // Perform the encoding
        encodedValue = URLEncoder.encode(k, contentEncoding);
        // Add to cache
        cache.addElement(cacheKey, encodedValue);
        return (String) encodedValue;
    }
}
