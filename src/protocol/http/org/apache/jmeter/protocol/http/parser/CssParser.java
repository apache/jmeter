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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * CSS Parser used to extract from CSS files external urls
 *
 * @since 3.0
 */
public class CssParser implements LinkExtractorParser {
    private static final URLCollection EMPTY_URL_COLLECTION = new URLCollection(Collections.emptyList());
    private static final Logger LOG = LoggerFactory.getLogger(CssParser.class);

    private static final LoadingCache<Triple<String, URL, Charset>, URLCollection> CSS_URL_CACHE;
    static {
        final int cacheSize = JMeterUtils.getPropDefault(
                "css.parser.cache.size", 400);
        CSS_URL_CACHE = Caffeine.newBuilder().maximumSize(cacheSize)
                .build(new CssParserCacheLoader());
    }

    /**
     * @see org.apache.jmeter.protocol.http.parser.LinkExtractorParser#getEmbeddedResourceURLs
     *      (java.lang.String, byte[], java.net.URL, java.lang.String)
     */
    @Override
    public Iterator<URL> getEmbeddedResourceURLs(String userAgent, byte[] data,
            final URL baseUrl, String encoding)
            throws LinkExtractorParseException {
        try {
            final String cssContent = new String(data, encoding);
            final Charset charset = Charset.forName(encoding);
            final Triple<String, URL, Charset> triple = ImmutableTriple.of(
                    cssContent, baseUrl, charset);
            final URLCollection urlCollection = orDefault(CSS_URL_CACHE.get(triple), EMPTY_URL_COLLECTION);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsed:" + baseUrl + ", got:" + StringUtils.join(urlCollection, ","));
            }

            return urlCollection.iterator();
        } catch (Exception e) {
            throw new LinkExtractorParseException(e);
        }
    }

    private URLCollection orDefault(URLCollection urlCollection,
            URLCollection defaultValue) {
        if (urlCollection == null) {
            return Validate.notNull(defaultValue);
        }
        return urlCollection;
    }

    @Override
    public boolean isReusable() {
        return true;
    }

}
