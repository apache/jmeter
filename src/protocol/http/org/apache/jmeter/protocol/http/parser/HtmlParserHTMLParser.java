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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.tags.AppletTag;
import org.htmlparser.tags.BaseHrefTag;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.ObjectTag;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

/**
 * HtmlParser implementation using SourceForge's HtmlParser.
 *
 */
class HtmlParserHTMLParser extends HTMLParser {
    private static final Logger log = LoggingManager.getLoggerForClass();

    static{
        org.htmlparser.scanners.ScriptScanner.STRICT = false; // Try to ensure that more javascript code is processed OK ...
    }

    protected HtmlParserHTMLParser() {
        super();
        log.info("Using htmlparser version: "+Parser.getVersion());
    }

    @Override
    protected boolean isReusable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<URL> getEmbeddedResourceURLs(byte[] html, URL baseUrl, URLCollection urls, String encoding) throws HTMLParseException {

        if (log.isDebugEnabled()) {
            log.debug("Parsing html of: " + baseUrl);
        }

        Parser htmlParser = null;
        try {
            String contents = new String(html,encoding); 
            htmlParser = new Parser();
            htmlParser.setInputHTML(contents);
        } catch (Exception e) {
            throw new HTMLParseException(e);
        }

        // Now parse the DOM tree
        try {
            // we start to iterate through the elements
            parseNodes(htmlParser.elements(), new URLPointer(baseUrl), urls);
            log.debug("End   : parseNodes");
        } catch (ParserException e) {
            throw new HTMLParseException(e);
        }

        return urls.iterator();
    }

    /*
     * A dummy class to pass the pointer of URL.
     */
    private static class URLPointer {
        private URLPointer(URL newUrl) {
            url = newUrl;
        }
        private URL url;
    }

    /**
     * Recursively parse all nodes to pick up all URL s.
     * @see e the nodes to be parsed
     * @see baseUrl Base URL from which the HTML code was obtained
     * @see urls URLCollection
     */
    private void parseNodes(final NodeIterator e,
            final URLPointer baseUrl, final URLCollection urls)
        throws HTMLParseException, ParserException {
        while(e.hasMoreNodes()) {
            Node node = e.nextNode();
            // a url is always in a Tag.
            if (!(node instanceof Tag)) {
                continue;
            }
            Tag tag = (Tag) node;
            String tagname=tag.getTagName();
            String binUrlStr = null;

            // first we check to see if body tag has a
            // background set
            if (tag instanceof BodyTag) {
                binUrlStr = tag.getAttribute(ATT_BACKGROUND);
            } else if (tag instanceof BaseHrefTag) {
                BaseHrefTag baseHref = (BaseHrefTag) tag;
                String baseref = baseHref.getBaseUrl();
                try {
                    if (!baseref.equals(""))// Bugzilla 30713
                    {
                        baseUrl.url = ConversionUtils.makeRelativeURL(baseUrl.url, baseref);
                    }
                } catch (MalformedURLException e1) {
                    throw new HTMLParseException(e1);
                }
            } else if (tag instanceof ImageTag) {
                ImageTag image = (ImageTag) tag;
                binUrlStr = image.getImageURL();
            } else if (tag instanceof AppletTag) {
                // look for applets

                // This will only work with an Applet .class file.
                // Ideally, this should be upgraded to work with Objects (IE)
                // and archives (.jar and .zip) files as well.
                AppletTag applet = (AppletTag) tag;
                binUrlStr = applet.getAppletClass();
            } else if (tag instanceof ObjectTag) {
                // look for Objects
                ObjectTag applet = (ObjectTag) tag; 
                String data = applet.getAttribute(ATT_CODEBASE);
                if(!StringUtils.isEmpty(data)) {
                    binUrlStr = data;               
                }
                
                data = applet.getAttribute(ATT_DATA);
                if(!StringUtils.isEmpty(data)) {
                    binUrlStr = data;                    
                }
                
            } else if (tag instanceof InputTag) {
                // we check the input tag type for image
                if (ATT_IS_IMAGE.equalsIgnoreCase(tag.getAttribute(ATT_TYPE))) {
                    // then we need to download the binary
                    binUrlStr = tag.getAttribute(ATT_SRC);
                }
            } else if (tag instanceof ScriptTag) {
                binUrlStr = tag.getAttribute(ATT_SRC);
                // Bug 51750
            } else if (tag instanceof FrameTag || tagname.equalsIgnoreCase(TAG_IFRAME)) {
                binUrlStr = tag.getAttribute(ATT_SRC);
            } else if (tagname.equalsIgnoreCase(TAG_EMBED)
                || tagname.equalsIgnoreCase(TAG_BGSOUND)){
                binUrlStr = tag.getAttribute(ATT_SRC);
            } else if (tagname.equalsIgnoreCase(TAG_LINK)) {
                // Putting the string first means it works even if the attribute is null
                if (STYLESHEET.equalsIgnoreCase(tag.getAttribute(ATT_REL))) {
                    binUrlStr = tag.getAttribute(ATT_HREF);
                }
            } else {
                binUrlStr = tag.getAttribute(ATT_BACKGROUND);
            }

            if (binUrlStr != null) {
                urls.addURL(binUrlStr, baseUrl.url);
            }

            // Now look for URLs in the STYLE attribute
            String styleTagStr = tag.getAttribute(ATT_STYLE);
            if(styleTagStr != null) {
                HtmlParsingUtils.extractStyleURLs(baseUrl.url, urls, styleTagStr);
            }

            // second, if the tag was a composite tag,
            // recursively parse its children.
            if (tag instanceof CompositeTag) {
                CompositeTag composite = (CompositeTag) tag;
                parseNodes(composite.elements(), baseUrl, urls);
            }
        }
    }

}
