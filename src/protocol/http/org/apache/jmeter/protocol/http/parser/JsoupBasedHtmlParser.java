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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * Parser based on JSOUP
 * @since 2.10
 * TODO Factor out common code between {@link LagartoBasedHtmlParser} and this one (adapter pattern)
 */
public class JsoupBasedHtmlParser extends HTMLParser {

    /*
     * A dummy class to pass the pointer of URL.
     */
    private static class URLPointer {
        private URLPointer(URL newUrl) {
            url = newUrl;
        }
        private URL url;
    }

    private static final class JMeterNodeVisitor implements NodeVisitor {

        private URLCollection urls;
        private URLPointer baseUrl;

        /**
         * @param baseUrl base url to extract possibly missing information from urls found in <code>urls</code>
         * @param urls collection of urls to consider
         */
        public JMeterNodeVisitor(final URLPointer baseUrl, URLCollection urls) {
            this.urls = urls;
            this.baseUrl = baseUrl;
        }

        private void extractAttribute(Element tag, String attributeName) {
            String url = tag.attr(attributeName);
            String normalizedUrl = normalizeUrlValue(url);
            if(normalizedUrl != null) {
                urls.addURL(normalizedUrl, baseUrl.url);
            }
        }

        @Override
        public void head(Node node, int depth) {
            if (!(node instanceof Element)) {
                return;
            }
            Element tag = (Element) node;
            String tagName = tag.tagName().toLowerCase();
            if (tagName.equals(TAG_BODY)) {
                extractAttribute(tag, ATT_BACKGROUND);
            } else if (tagName.equals(TAG_SCRIPT)) {
                extractAttribute(tag, ATT_SRC);
            } else if (tagName.equals(TAG_BASE)) {
                String baseref = tag.attr(ATT_HREF);
                try {
                    if (!StringUtils.isEmpty(baseref))// Bugzilla 30713
                    {
                        baseUrl.url = ConversionUtils.makeRelativeURL(baseUrl.url, baseref);
                    }
                } catch (MalformedURLException e1) {
                    throw new IllegalArgumentException("Error creating relative url from " + baseref, e1);
                }
            } else if (tagName.equals(TAG_IMAGE)) {
                extractAttribute(tag, ATT_SRC);
            } else if (tagName.equals(TAG_APPLET)) {
                extractAttribute(tag, ATT_CODE);
            } else if (tagName.equals(TAG_OBJECT)) {
                extractAttribute(tag, ATT_CODEBASE);
                extractAttribute(tag, ATT_DATA);
            } else if (tagName.equals(TAG_INPUT)) {
                // we check the input tag type for image
                if (ATT_IS_IMAGE.equalsIgnoreCase(tag.attr(ATT_TYPE))) {
                    // then we need to download the binary
                    extractAttribute(tag, ATT_SRC);
                }
                // Bug 51750
            } else if (tagName.equals(TAG_FRAME) || tagName.equals(TAG_IFRAME)) {
                extractAttribute(tag, ATT_SRC);
            } else if (tagName.equals(TAG_EMBED)) {
                extractAttribute(tag, ATT_SRC);
            } else if (tagName.equals(TAG_BGSOUND)){
                extractAttribute(tag, ATT_SRC);
            } else if (tagName.equals(TAG_LINK)) {
                String relAttr = tag.attr(ATT_REL);
                // Putting the string first means it works even if the attribute is null
                if (STYLESHEET.equalsIgnoreCase(relAttr) || ICON.equalsIgnoreCase(relAttr)
                        || SHORTCUT_ICON.equalsIgnoreCase(relAttr)) {
                    extractAttribute(tag, ATT_HREF);
                }
            } else {
                extractAttribute(tag, ATT_BACKGROUND);
            }


            // Now look for URLs in the STYLE attribute
            String styleTagStr = tag.attr(ATT_STYLE);
            if(styleTagStr != null) {
                HtmlParsingUtils.extractStyleURLs(baseUrl.url, urls, styleTagStr);
            }
        }

        @Override
        public void tail(Node arg0, int arg1) {
            // Noop
        }
    }

    @Override
    public Iterator<URL> getEmbeddedResourceURLs(String userAgent, byte[] html, URL baseUrl,
            URLCollection coll, String encoding) throws HTMLParseException {
        try {
            // TODO Handle conditional comments for IE
            String contents = new String(html,encoding);
            Document doc = Jsoup.parse(contents);
            JMeterNodeVisitor nodeVisitor = new JMeterNodeVisitor(new URLPointer(baseUrl), coll);
            NodeTraversor.traverse(nodeVisitor, doc);
            return coll.iterator();
        } catch (Exception e) {
            throw new HTMLParseException(e);
        }
    }
}
