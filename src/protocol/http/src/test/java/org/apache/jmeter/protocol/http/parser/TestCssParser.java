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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jmeter.junit.JMeterTestCase;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

public class TestCssParser extends JMeterTestCase {

    private static final String CSS_IN_ERROR = "@-webkit-keyframes \"introjspulse\"{"
            + "0%{-webkit-transform:scale(0);opacity:.0}"
            + "25%{-webkit-transform:scale(0);opacity:.1}"
            + "50%{-webkit-transform:scale(0.1);opacity:.3}"
            + "75%{-webkit-transform:scale(0.5);opacity:.5}"
            + "100%{-webkit-transform:scale(1);opacity:.0}}";

    private final CssParser parser = new CssParser();

    @Test
    public void testGetEmbeddedResourceURLsNoUrls() throws Exception {
        CssParser nonIgnoreParser = new CssParser();
        List<URL> result = extractUrls(nonIgnoreParser, "..");
        assertThat(result, is(empty()));
    }

    @Test
    public void testGetEmbeddedResourceURLsnOneUrl() throws Exception {
        List<URL> result = extractUrls("@import url(http://example.com/abc.css);");
        assertThat(result, is(not(empty())));
    }

    @Test
    public void testExtractUrlsFromBrokenData() throws Exception {
        List<URL> result = extractUrls(CSS_IN_ERROR);
        assertThat(result, is(empty()));
    }

    @Test
    public void testIsReusable() {
        assertThat(parser.isReusable(), CoreMatchers.is(true));
    }

    private List<URL> extractUrls(String css) throws LinkExtractorParseException,
            MalformedURLException {
        return extractUrls(parser, css);
    }

    private List<URL> extractUrls(CssParser parser, String css)
            throws LinkExtractorParseException, MalformedURLException {
        List<URL> result = new ArrayList<>();
        Iterator<URL> urlIterator = parser.getEmbeddedResourceURLs(
                "Mozilla", css.getBytes(StandardCharsets.UTF_8), new URL(
                        "http://example.org/"), StandardCharsets.UTF_8
                        .displayName());
        urlIterator.forEachRemaining(result::add);
        return result;
    }
}
