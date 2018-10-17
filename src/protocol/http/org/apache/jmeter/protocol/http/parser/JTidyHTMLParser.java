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

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * HtmlParser implementation using JTidy.
 */
class JTidyHTMLParser extends HTMLParser {
    private static final Logger log = LoggerFactory.getLogger(JTidyHTMLParser.class);

    protected JTidyHTMLParser() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<URL> getEmbeddedResourceURLs(String userAgent, byte[] html, URL baseUrl, URLCollection urls, String encoding) throws HTMLParseException {
        Document dom;
        try {
            dom = (Document) getDOM(html, encoding);
        } catch (SAXException se) {
            throw new HTMLParseException(se);
        }

        // Now parse the DOM tree

        scanNodes(dom, urls, baseUrl);

        return urls.iterator();
    }

    /**
     * Scan nodes recursively, looking for embedded resources
     *
     * @param node -
     *            initial node
     * @param urls -
     *            container for URLs
     * @param baseUrl -
     *            used to create absolute URLs
     *
     * @return new base URL
     */
    private URL scanNodes(Node node, URLCollection urls, URL baseUrl) throws HTMLParseException {
        if (node == null) {
            return baseUrl;
        }

        String name = node.getNodeName();

        int type = node.getNodeType();

        switch (type) {

        case Node.DOCUMENT_NODE:
            scanNodes(((Document) node).getDocumentElement(), urls, baseUrl);
            break;

        case Node.ELEMENT_NODE:

            NamedNodeMap attrs = node.getAttributes();
            if (name.equalsIgnoreCase(TAG_BASE)) {
                String tmp = getValue(attrs, ATT_HREF);
                if (tmp != null) {
                    try {
                        baseUrl = ConversionUtils.makeRelativeURL(baseUrl, tmp);
                    } catch (MalformedURLException e) {
                        throw new HTMLParseException(e);
                    }
                }
                break;
            }

            if (name.equalsIgnoreCase(TAG_IMAGE) || name.equalsIgnoreCase(TAG_EMBED)) {
                urls.addURL(getValue(attrs, ATT_SRC), baseUrl);
                break;
            }

            if (name.equalsIgnoreCase(TAG_APPLET)) {
                urls.addURL(getValue(attrs, "code"), baseUrl);
                break;
            }
            
            if (name.equalsIgnoreCase(TAG_OBJECT)) {
                String data = getValue(attrs, "codebase");
                if(!StringUtils.isEmpty(data)) {
                    urls.addURL(data, baseUrl);                    
                }
                
                data = getValue(attrs, "data");
                if(!StringUtils.isEmpty(data)) {
                    urls.addURL(data, baseUrl);                    
                }
                break;
            }
            
            if (name.equalsIgnoreCase(TAG_INPUT)) {
                String src = getValue(attrs, ATT_SRC);
                String typ = getValue(attrs, ATT_TYPE);
                if ((src != null) && ATT_IS_IMAGE.equalsIgnoreCase(typ)) {
                    urls.addURL(src, baseUrl);
                }
                break;
            }
            if (TAG_LINK.equalsIgnoreCase(name) && STYLESHEET.equalsIgnoreCase(getValue(attrs, ATT_REL))) {
                urls.addURL(getValue(attrs, ATT_HREF), baseUrl);
                break;
            }
            if (name.equalsIgnoreCase(TAG_SCRIPT)) {
                urls.addURL(getValue(attrs, ATT_SRC), baseUrl);
                break;
            }
            if (name.equalsIgnoreCase(TAG_FRAME)) {
                urls.addURL(getValue(attrs, ATT_SRC), baseUrl);
                break;
            }
            if (name.equalsIgnoreCase(TAG_IFRAME)) {
                urls.addURL(getValue(attrs, ATT_SRC), baseUrl);
                break;
            }
            String back = getValue(attrs, ATT_BACKGROUND);
            if (back != null) {
                urls.addURL(back, baseUrl);
            }
            if (name.equalsIgnoreCase(TAG_BGSOUND)) {
                urls.addURL(getValue(attrs, ATT_SRC), baseUrl);
                break;
            }

            String style = getValue(attrs, ATT_STYLE);
            if (style != null) {
                HtmlParsingUtils.extractStyleURLs(baseUrl, urls, style);
            }

            NodeList children = node.getChildNodes();
            if (children != null) {
                int len = children.getLength();
                for (int i = 0; i < len; i++) {
                    baseUrl = scanNodes(children.item(i), urls, baseUrl);
                }
            }

            break;

        default:
            // ignored
            break;
        }

        return baseUrl;

    }

    /*
     * Helper method to get an attribute value, if it exists @param attrs list
     * of attributes @param attname attribute name @return
     */
    private String getValue(NamedNodeMap attrs, String attname) {
        String v = null;
        Node n = attrs.getNamedItem(attname);
        if (n != null) {
            v = n.getNodeValue();
        }
        return v;
    }

    /**
     * Returns <code>tidy</code> as HTML parser.
     *
     * @return a <code>tidy</code> HTML parser
     */
    private static Tidy getTidyParser(String encoding) {
        log.debug("Start : getParser");
        Tidy tidy = new Tidy();
        tidy.setInputEncoding(encoding);
        tidy.setOutputEncoding(StandardCharsets.UTF_8.name());
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        if (log.isDebugEnabled()) {
            log.debug("getParser : tidy parser created - " + tidy);
        }
        log.debug("End   : getParser");
        return tidy;
    }

    /**
     * Returns a node representing a whole xml given an xml document.
     *
     * @param text
     *            an xml document (as a byte array)
     * @return a node representing a whole xml
     *
     * @throws SAXException
     *             indicates an error parsing the xml document
     */
    private static Node getDOM(byte[] text, String encoding) throws SAXException {
        log.debug("Start : getDOM");
        Node node = getTidyParser(encoding).parseDOM(new ByteArrayInputStream(text), null);
        if (log.isDebugEnabled()) {
            log.debug("node : " + node);
        }
        log.debug("End   : getDOM");
        return node;
    }
}
