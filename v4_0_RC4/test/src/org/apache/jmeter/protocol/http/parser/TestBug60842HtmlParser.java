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

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestBug60842HtmlParser {

    private List<String> links;
    private String html;
    private HTMLParser parser;

    public TestBug60842HtmlParser(HTMLParser parser, String html,
            String links) {
        this.parser = parser;
        this.html = html;
        if (links.isEmpty()) {
            this.links = Collections.emptyList();
        } else {
            this.links = Arrays.asList(links.split(","));
        }
    }

    @Parameters()
    public static Collection<Object[]> params() {
        List<Object[]> result = new ArrayList<>();
        for (HTMLParser parserToTest : Arrays.asList(
                new LagartoBasedHtmlParser(), new JsoupBasedHtmlParser())) {

            for (Object[] data : Arrays.asList(
                    new Object[] { parserToTest, "<body background='abc.png'/>",
                            "http://example.org/abc.png" },
                    new Object[] { parserToTest,
                            "<link href='abc.css' rel='stylesheet'/>",
                            "http://example.org/abc.css" },
                    new Object[] { parserToTest, "<img src='abc.png'/>",
                            "http://example.org/abc.png" },
                    new Object[] { parserToTest,
                            "<base href='http://another.org'/><img src='one.png'/>",
                            "http://another.org/one.png" },
                    new Object[] { parserToTest, "<applet code='abc.jar'/>",
                            "http://example.org/abc.jar" },
                    new Object[] { parserToTest,
                            "<object codebase='abc.jar' data='something'/>",
                            "http://example.org/abc.jar,http://example.org/something" },
                    new Object[] { parserToTest, "<object data='something'/>",
                            "http://example.org/something" },
                    new Object[] { parserToTest, "<object codebase='abc.jar'/>",
                            "http://example.org/abc.jar" },
                    new Object[] { parserToTest,
                            "<input type='image' src='foo'/>",
                            "http://example.org/foo" },
                    new Object[] { parserToTest,
                            "<input type='text' src='foo'/>", "" },
                    new Object[] { parserToTest,
                            "<frameset><frame src='foo'/></frameset>",
                            "http://example.org/foo" },
                    new Object[] { parserToTest, "<iframe src='foo'/>",
                            "http://example.org/foo" },
                    new Object[] { parserToTest, "<embed src='foo'/>",
                            "http://example.org/foo" },
                    new Object[] { parserToTest, "<bgsound src='foo'/>",
                            "http://example.org/foo" },
                    new Object[] { parserToTest, "<anytag background='foo'/>",
                            "http://example.org/foo" },
                    new Object[] { parserToTest,
                            "<anytag style='foo: url(\"bar\")'/>",
                            "http://example.org/bar" },
                    new Object[] { parserToTest,
                            "<anytag style=\"foo: url('bar')'\"/>",
                            "http://example.org/bar" },
                    new Object[] { parserToTest,
                            "<link href='  abc\n.css  ' rel='stylesheet'/>",
                            "http://example.org/abc.css" },
                    new Object[] { parserToTest,
                            "<link href='  with spaces\n.css  ' rel='stylesheet'/>",
                            "http://example.org/with spaces.css" },
                    new Object[] { parserToTest, "<embed src=''/>", "" },
                    new Object[] { parserToTest, "<embed src='  '/>", "" })) {
                result.add(data);
            }
        }
        return result;
    }

    @Test
    public void testGetEmbeddedResourceURLsStringByteArrayURLURLCollectionString()
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
