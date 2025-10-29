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

package org.apache.jmeter.protocol.http.parser;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.StringUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSExpressionMemberTermURI;
import com.helger.css.decl.CSSImportRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.ICSSTopLevelRule;
import com.helger.css.decl.visit.CSSVisitor;
import com.helger.css.decl.visit.DefaultCSSUrlVisitor;
import com.helger.css.reader.CSSReader;
import com.helger.css.reader.CSSReaderSettings;
import com.helger.css.reader.errorhandler.DoNothingCSSInterpretErrorHandler;
import com.helger.css.reader.errorhandler.LoggingCSSParseErrorHandler;

public class CssParserCacheLoader implements
        CacheLoader<CssParser.CssCacheKey, URLCollection> {

    private static final Logger LOG = LoggerFactory.getLogger(CssParserCacheLoader.class);
    private static final boolean IGNORE_ALL_CSS_ERRORS = JMeterUtils
            .getPropDefault("css.parser.ignore_all_css_errors", true);

    @Override
    public URLCollection load(CssParser.CssCacheKey triple)
            throws Exception {
        final String cssContent = triple.cssContents();
        final URL baseUrl = triple.baseUrl();
        final Charset charset = triple.charset();
        final CSSReaderSettings readerSettings = new CSSReaderSettings()
                .setBrowserCompliantMode(true)
                .setFallbackCharset(charset)
                .setCustomErrorHandler(new LoggingCSSParseErrorHandler())
                .setUseSourceLocation(false)
                .setCustomExceptionHandler(
                        new CSSParseExceptionCallback(baseUrl));
        if (IGNORE_ALL_CSS_ERRORS) {
            readerSettings
                    .setInterpretErrorHandler(new DoNothingCSSInterpretErrorHandler());
        }
        final CascadingStyleSheet aCSS = CSSReader.readFromStringReader(
                cssContent, readerSettings);

        final URLCollection urls = new URLCollection(new ArrayList<>());

        if (aCSS == null) {
            LOG.warn("Failed parsing CSS: {}, got null CascadingStyleSheet", baseUrl);
            return urls;
        }

        CSSVisitor.visitCSSUrl(aCSS, new DefaultCSSUrlVisitor() {
            @Override
            public void onImport(CSSImportRule rule) {
                final String location = rule.getLocationString();
                if (StringUtilities.isNotEmpty(location)) {
                    urls.addURL(location, baseUrl);
                }
            }

            // Call for URLs outside of URLs
            @Override
            public void onUrlDeclaration(final ICSSTopLevelRule aTopLevelRule,
                    final CSSDeclaration aDeclaration,
                    final CSSExpressionMemberTermURI aURITerm) {
                // NOOP
                // Browser fetch such urls only when CSS rule matches
                // so we disable this code
            }
        });

        return urls;
    }

}
