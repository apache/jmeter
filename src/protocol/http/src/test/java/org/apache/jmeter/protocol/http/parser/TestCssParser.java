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

import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.jmeter.junit.JMeterTestCase;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

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
        List<?> result = extractUrls(nonIgnoreParser, "..");
        assertThat(result.isEmpty(), CoreMatchers.is(true));
    }

    @Test
    public void testGetEmbeddedResourceURLsnOneUrl() throws Exception {
        List<?> result;
        result = extractUrls("@import url(http://example.com/abc.css);");
        assertThat(result.isEmpty(), CoreMatchers.is(false));
    }
    
    @Test(expected=LinkExtractorParseException.class)
    public void testExtractUrlsFromBrokenData() throws Exception {
        extractUrls(CSS_IN_ERROR);
    }

    @Test
    public void testIsReusable() {
        assertThat(parser.isReusable(), CoreMatchers.is(true));
    }

    private List<?> extractUrls(String css) throws LinkExtractorParseException,
            MalformedURLException {
        return extractUrls(parser, css);
    }

    private List<?> extractUrls(CssParser parser, String css)
            throws LinkExtractorParseException, MalformedURLException {
        List<?> result = IteratorUtils.toList(parser.getEmbeddedResourceURLs(
                "Mozilla", css.getBytes(StandardCharsets.UTF_8), new URL(
                        "http://example.org/"), StandardCharsets.UTF_8
                        .displayName()));
        return result;
    }
}

