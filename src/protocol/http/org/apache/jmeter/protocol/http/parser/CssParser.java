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

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * CSS Parser used to extract from CSS files external urls
 * @since 3.0
 */
public final class CssParser implements LinkExtractorParser {
    private static final Logger LOG = LoggingManager.getLoggerForClass();
    private static final Joiner ON_COMA = Joiner.on(',');
    private static final LoadingCache<Triple<String, URL, Charset>, URLCollection> CSS_URL_CACHE;

    static {
        final int cacheSize = JMeterUtils.getPropDefault("css.parser.cache.size", 400);
        CSS_URL_CACHE = CacheBuilder
                .<String, URLCollection> newBuilder()
                .maximumSize(cacheSize)
                .build(new CssParserCacheLoader());
    }

    @Override
    public Iterator<URL> getEmbeddedResourceURLs(
            final String userAgent,
            final byte[] data,
            final URL baseUrl,
            final String encoding) throws LinkExtractorParseException {
        try {
            final String cssContent = new String(data, encoding);
            final Charset charset = Charset.forName(encoding);
            final Triple<String, URL, Charset> triple = ImmutableTriple.of(cssContent, baseUrl, charset);
            final URLCollection urlCollection = CSS_URL_CACHE.get(triple);

            if(LOG.isDebugEnabled()) {
                LOG.debug("Parsed: " + baseUrl + ", got: " + ON_COMA.join(urlCollection.iterator()));
            }

            return urlCollection.iterator();
        } catch (final UnsupportedEncodingException | ExecutionException e) {
            throw new LinkExtractorParseException(e);
        }
    }

    @Override
    public boolean isReusable() {
        return true;
    }

}
