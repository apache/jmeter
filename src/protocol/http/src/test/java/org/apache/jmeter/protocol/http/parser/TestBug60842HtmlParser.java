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

import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestBug60842HtmlParser {

    private static Stream<Arguments> params() {
        List<String[]> snippets = Arrays.asList(
                        new String[] { "<body background='abc.png'/>",
                                "http://example.org/abc.png" },
                        new String[] { "<link href='abc.css' rel='stylesheet'/>",
                                "http://example.org/abc.css" },
                        new String[] { "<img src='abc.png'/>",
                                "http://example.org/abc.png" },
                        new String[] { "<base href='http://another.org'/><img src='one.png'/>",
                                "http://another.org/one.png" },
                        new String[] { "<applet code='abc.jar'/>",
                                "http://example.org/abc.jar" },
                        new String[] { "<object codebase='abc.jar' data='something'/>",
                                "http://example.org/abc.jar,http://example.org/something" },
                        new String[] { "<object data='something'/>",
                                "http://example.org/something" },
                        new String[] { "<object codebase='abc.jar'/>",
                                "http://example.org/abc.jar" },
                        new String[] { "<input type='image' src='foo'/>",
                                "http://example.org/foo" },
                        new String[] { "<input type='text' src='foo'/>", "" },
                        new String[] { "<frameset><frame src='foo'/></frameset>",
                                "http://example.org/foo" },
                        new String[] { "<iframe src='foo'/>",
                                "http://example.org/foo" },
                        new String[] { "<embed src='foo'/>",
                                "http://example.org/foo" },
                        new String[] { "<bgsound src='foo'/>",
                                "http://example.org/foo" },
                        new String[] { "<anytag background='foo'/>",
                                "http://example.org/foo" },
                        new String[] { "<anytag style='foo: url(\"bar\")'/>",
                                "http://example.org/bar" },
                        new String[] { "<anytag style=\"foo: url('bar')'\"/>",
                                "http://example.org/bar" },
                        new String[] { "<link href='  abc\n.css  ' rel='stylesheet'/>",
                                "http://example.org/abc.css" },
                        new String[] { "<link href='  with spaces\n.css  ' rel='stylesheet'/>",
                                "http://example.org/with spaces.css" },
                        new String[] { "<link href='favicon.ico' rel='shortcut icon' type='image/vnd.microsoft.icon'/>",
                                "http://example.org/favicon.ico" },
                        new String[] { "<link href='favicon.ico' rel='icon' type='image/vnd.microsoft.icon'/>",
                                "http://example.org/favicon.ico" },
                        new String[] { "<applet codebase='/some/path' code='Application.class' />",
                                "http://example.org/some/path/Application.class" },
                        new String[] { "<applet codebase='/some/path' code='Application.class' archive='app.jar' />",
                                "http://example.org/some/path/app.jar" },
                        new String[] { "<embed src=''/>", "" },
                        new String[] { "<embed src='  '/>", "" });
        List<Arguments> result = new ArrayList<>();
        for (HTMLParser parserToTest : Arrays.asList(new LagartoBasedHtmlParser(), new JsoupBasedHtmlParser())) {
            for (String[] data : snippets) {
                String htmlData = data[0];
                String linksData = data[1];
                Collection<String> links;
                if (linksData.isEmpty()) {
                    links = Collections.emptyList();
                } else {
                    links = Arrays.asList(linksData.split(","));
                }
                result.add(Arguments.of(parserToTest, htmlData, links));
            }
        }
        return result.stream();
    }

    @ParameterizedTest
    @MethodSource("params")
    void testGetEmbeddedResourceURLsStringByteArrayURLURLCollectionString(HTMLParser parser, String html,
            Collection<String> links)
            throws Exception {
        final ArrayList<URLString> c = new ArrayList<>();
        parser.getEmbeddedResourceURLs("Mozilla",
                html.getBytes(StandardCharsets.UTF_8),
                new URL("http://example.org"), new URLCollection(c),
                StandardCharsets.UTF_8.name());
        List<String> urlNames = c.stream().map(u -> u.toString())
                .collect(Collectors.toList());
        assertThat(
                String.format("Parse with %s the page %s to get %s",
                        parser.getClass().getSimpleName(), html, links),
                urlNames, CoreMatchers.is(CoreMatchers.equalTo(links)));
    }

}
