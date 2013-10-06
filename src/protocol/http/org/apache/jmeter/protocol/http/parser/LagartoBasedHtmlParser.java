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
import java.util.Collections;
import java.util.Iterator;

import jodd.lagarto.EmptyTagVisitor;
import jodd.lagarto.LagartoException;
import jodd.lagarto.LagartoParser;
import jodd.lagarto.Tag;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Parser based on Lagarto
 * @since 2.10
 */
public class LagartoBasedHtmlParser extends HTMLParser {
    private static final Logger log = LoggingManager.getLoggerForClass();

    /*
     * A dummy class to pass the pointer of URL.
     */
    private static class URLPointer {
        private URLPointer(URL newUrl) {
            url = newUrl;
        }
        private URL url;
    }
    
    private static final class JMeterTagVisitor extends EmptyTagVisitor {

        private URLCollection urls;
        private URLPointer baseUrl;

        /**
         * @param baseUrl 
         * @param urls 
         */
        public JMeterTagVisitor(final URLPointer baseUrl, URLCollection urls) {
            this.urls = urls;
            this.baseUrl = baseUrl;
        }

        private final void extractAttribute(Tag tag, String attributeName) {
            String url = tag.getAttributeValue(attributeName, false);
            if (!StringUtils.isEmpty(url)) {
                urls.addURL(url, baseUrl.url);
            }
        }
        /*
         * (non-Javadoc)
         * 
         * @see jodd.lagarto.EmptyTagVisitor#script(jodd.lagarto.Tag,
         * java.lang.CharSequence)
         */
        @Override
        public void script(Tag tag, CharSequence body) {
            extractAttribute(tag, ATT_SRC);
        }

        /*
         * (non-Javadoc)
         * 
         * @see jodd.lagarto.EmptyTagVisitor#tag(jodd.lagarto.Tag)
         */
        @Override
        public void tag(Tag tag) {

            String tagName = tag.getName().toLowerCase();
            if (tagName.equals(TAG_BODY)) {
                extractAttribute(tag, ATT_BACKGROUND);
            } else if (tagName.equals(TAG_BASE)) {
                String baseref = tag.getAttributeValue(ATT_HREF, false);
                try {
                    if (!StringUtils.isEmpty(baseref))// Bugzilla 30713
                    {
                        baseUrl.url = ConversionUtils.makeRelativeURL(baseUrl.url, baseref);
                    }
                } catch (MalformedURLException e1) {
                    throw new RuntimeException(e1);
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
                if (ATT_IS_IMAGE.equalsIgnoreCase(tag.getAttributeValue(ATT_TYPE, false))) {
                    // then we need to download the binary
                    extractAttribute(tag, ATT_SRC);
                }
            } else if (tagName.equals(TAG_SCRIPT)) {
                extractAttribute(tag, ATT_SRC);
                // Bug 51750
            } else if (tagName.equals(TAG_FRAME) || tagName.equals(TAG_IFRAME)) {
                extractAttribute(tag, ATT_SRC);
            } else if (tagName.equals(TAG_EMBED)) {
                extractAttribute(tag, ATT_SRC);
            } else if (tagName.equals(TAG_BGSOUND)){
                extractAttribute(tag, ATT_SRC);
            } else if (tagName.equals(TAG_LINK)) {
                // Putting the string first means it works even if the attribute is null
                if (STYLESHEET.equalsIgnoreCase(tag.getAttributeValue(ATT_REL, false))) {
                    extractAttribute(tag, ATT_HREF);
                }
            } else {
                extractAttribute(tag, ATT_BACKGROUND);
            }


            // Now look for URLs in the STYLE attribute
            String styleTagStr = tag.getAttributeValue(ATT_STYLE, false);
            if(styleTagStr != null) {
                HtmlParsingUtils.extractStyleURLs(baseUrl.url, urls, styleTagStr);
            }
        }
    }

    @Override
    public Iterator<URL> getEmbeddedResourceURLs(byte[] html, URL baseUrl,
            URLCollection coll, String encoding) throws HTMLParseException {
        try {
            String contents = new String(html,encoding); 
            LagartoParser lagartoParser = new LagartoParser(contents);
            JMeterTagVisitor tagVisitor = new JMeterTagVisitor(new URLPointer(baseUrl), coll);
            lagartoParser.parse(tagVisitor);
            return coll.iterator();
        } catch (LagartoException e) {
            // TODO is it the best way ? https://issues.apache.org/bugzilla/show_bug.cgi?id=55634
            if(log.isDebugEnabled()) {
                log.debug("Error extracting embedded resource URLs from:'"+baseUrl+"', probably not text content, message:"+e.getMessage());
            }
            return Collections.<URL>emptyList().iterator();
        } catch (Exception e) {
            throw new HTMLParseException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.parser.HTMLParser#isReusable()
     */
    @Override
    protected boolean isReusable() {
        return true;
    }
}
