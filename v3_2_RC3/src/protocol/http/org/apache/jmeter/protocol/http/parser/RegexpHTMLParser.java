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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HtmlParser implementation using regular expressions.
 * <p>
 * This class will find RLs specified in the following ways (where <b>url</b>
 * represents the RL being found:
 * <ul>
 * <li>&lt;img src=<b>url</b> ... &gt;
 * <li>&lt;script src=<b>url</b> ... &gt;
 * <li>&lt;applet code=<b>url</b> ... &gt;
 * <li>&lt;input type=image src=<b>url</b> ... &gt;
 * <li>&lt;body background=<b>url</b> ... &gt;
 * <li>&lt;table background=<b>url</b> ... &gt;
 * <li>&lt;td background=<b>url</b> ... &gt;
 * <li>&lt;tr background=<b>url</b> ... &gt;
 * <li>&lt;applet ... codebase=<b>url</b> ... &gt;
 * <li>&lt;embed src=<b>url</b> ... &gt;
 * <li>&lt;embed codebase=<b>url</b> ... &gt;
 * <li>&lt;object codebase=<b>url</b> ... &gt;
 * <li>&lt;link rel=stylesheet href=<b>url</b>... gt;
 * <li>&lt;bgsound src=<b>url</b> ... &gt;
 * <li>&lt;frame src=<b>url</b> ... &gt;
 * </ul>
 *
 * <p>
 * This class will take into account the following construct:
 * <ul>
 * <li>&lt;base href=<b>url</b>&gt;
 * </ul>
 *
 * <p>
 * But not the following:
 * <ul>
 * <li>&lt; ... codebase=<b>url</b> ... &gt;
 * </ul>
 *
 */
class RegexpHTMLParser extends HTMLParser {
    private static final Logger log = LoggerFactory.getLogger(RegexpHTMLParser.class);

    /**
     * Regexp fragment matching a tag attribute's value (including the equals
     * sign and any spaces before it). Note it matches unquoted values, which to
     * my understanding, are not conformant to any of the HTML specifications,
     * but are still quite common in the web and all browsers seem to understand
     * them.
     */
    private static final String VALUE = "\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\"'\\s>\\\\][^\\s>]*)(?=[\\s>]))";

    // Note there's 3 capturing groups per value

    /**
     * Regexp fragment matching the separation between two tag attributes.
     */
    private static final String SEP = "\\s(?:[^>]*\\s)?";

    /**
     * Regular expression used against the HTML code to find the URIs of images,
     * etc.:
     */
    private static final String REGEXP =
              "<(?:" + "!--.*?-->"
            + "|BASE" + SEP + "HREF" + VALUE
            + "|(?:IMG|SCRIPT|FRAME|IFRAME|BGSOUND)" + SEP + "SRC" + VALUE
            + "|APPLET" + SEP + "CODE(?:BASE)?" + VALUE
            + "|(?:EMBED|OBJECT)" + SEP + "(?:SRC|CODEBASE|DATA)" + VALUE
            + "|(?:BODY|TABLE|TR|TD)" + SEP + "BACKGROUND" + VALUE
            + "|[^<]+?STYLE\\s*=['\"].*?URL\\(\\s*['\"](.+?)['\"]\\s*\\)"
            + "|INPUT(?:" + SEP + "(?:SRC" + VALUE
            + "|TYPE\\s*=\\s*(?:\"image\"|'image'|image(?=[\\s>])))){2,}"
            + "|LINK(?:" + SEP + "(?:HREF" + VALUE
            + "|REL\\s*=\\s*(?:\"stylesheet\"|'stylesheet'|stylesheet(?=[\\s>])))){2,}" + ")";

    // Number of capturing groups possibly containing Base HREFs:
    private static final int NUM_BASE_GROUPS = 3;

    /**
     * Thread-local input:
     */
    private static final ThreadLocal<PatternMatcherInput> localInput =
            ThreadLocal.withInitial(() -> new PatternMatcherInput(new char[0]));

    /**
     * Make sure to compile the regular expression upon instantiation:
     */
    protected RegexpHTMLParser() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<URL> getEmbeddedResourceURLs(String userAgent, byte[] html, URL baseUrl, URLCollection urls, String encoding) throws HTMLParseException {
        Pattern pattern= null;
        Perl5Matcher matcher = null;
        try {
            matcher = JMeterUtils.getMatcher();
            PatternMatcherInput input = localInput.get();
            // TODO: find a way to avoid the cost of creating a String here --
            // probably a new PatternMatcherInput working on a byte[] would do
            // better.
            input.setInput(new String(html, encoding)); 
            pattern=JMeterUtils.getPatternCache().getPattern(
                    REGEXP,
                    Perl5Compiler.CASE_INSENSITIVE_MASK
                    | Perl5Compiler.SINGLELINE_MASK
                    | Perl5Compiler.READ_ONLY_MASK);

            while (matcher.contains(input, pattern)) {
                MatchResult match = matcher.getMatch();
                String s;
                if (log.isDebugEnabled()) {
                    log.debug("match groups " + match.groups() + " " + match.toString());
                }
                // Check for a BASE HREF:
                for (int g = 1; g <= NUM_BASE_GROUPS && g <= match.groups(); g++) {
                    s = match.group(g);
                    if (s != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("new baseUrl: " + s + " - " + baseUrl.toString());
                        }
                        try {
                            baseUrl = ConversionUtils.makeRelativeURL(baseUrl, s);
                        } catch (MalformedURLException e) {
                            // Doesn't even look like a URL?
                            // Maybe it isn't: Ignore the exception.
                            if (log.isDebugEnabled()) {
                                log.debug("Can't build base URL from RL " + s + " in page " + baseUrl, e);
                            }
                        }
                    }
                }
                for (int g = NUM_BASE_GROUPS + 1; g <= match.groups(); g++) {
                    s = match.group(g);
                    if (s != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("group " + g + " - " + match.group(g));
                        }
                        urls.addURL(s, baseUrl);
                    }
                }
            }
            return urls.iterator();
        } catch (UnsupportedEncodingException
                | MalformedCachePatternException e) {
            throw new HTMLParseException(e.getMessage(), e);
        } finally {
            JMeterUtils.clearMatcherMemory(matcher, pattern);
        }
    }
}
