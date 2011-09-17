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

package org.apache.jmeter.protocol.http.proxy;

import java.util.Map;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.jmeter.protocol.http.parser.HTMLParseException;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.FormTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

/**
 * A parser for html, to find the form tags, and their accept-charset value
 */
// made public see Bug 49976
public class FormCharSetFinder {
    private static final Logger log = LoggingManager.getLoggerForClass();

    static {
        log.info("Using htmlparser version: "+Parser.getVersion());
    }

    protected FormCharSetFinder() {
        super();
    }

    /**
     * Add form action urls and their corresponding encodings for all forms on the page
     *
     * @param html the html to parse for form encodings
     * @param formEncodings the Map where form encodings should be added
     * @param pageEncoding the encoding used for the whole page
     * @throws HTMLParseException
     */
    public void addFormActionsAndCharSet(String html, Map<String, String> formEncodings, String pageEncoding)
            throws HTMLParseException {
        if (log.isDebugEnabled()) {
            log.debug("Parsing html of: " + html);
        }

        Parser htmlParser = null;
        try {
            htmlParser = new Parser();
            htmlParser.setInputHTML(html);
        } catch (Exception e) {
            throw new HTMLParseException(e);
        }

        // Now parse the DOM tree
        try {
            // we start to iterate through the elements
            parseNodes(htmlParser.elements(), formEncodings, pageEncoding);
            log.debug("End   : parseNodes");
        } catch (ParserException e) {
            throw new HTMLParseException(e);
        }
    }

    /**
     * Recursively parse all nodes to pick up all form encodings
     *
     * @param e the nodes to be parsed
     * @param formEncodings the Map where we should add form encodings found
     * @param pageEncoding the encoding used for the page where the nodes are present
     */
    private void parseNodes(final NodeIterator e, Map<String, String> formEncodings, String pageEncoding)
        throws HTMLParseException, ParserException {
        while(e.hasMoreNodes()) {
            Node node = e.nextNode();
            // a url is always in a Tag.
            if (!(node instanceof Tag)) {
                continue;
            }
            Tag tag = (Tag) node;

            // Only check form tags
            if (tag instanceof FormTag) {
                // Find the action / form url
                String action = tag.getAttribute("action");
                String acceptCharSet = tag.getAttribute("accept-charset");
                if(action != null && action.length() > 0) {
                    // We use the page encoding where the form resides, as the
                    // default encoding for the form
                    String formCharSet = pageEncoding;
                    // Check if we found an accept-charset attribute on the form
                    if(acceptCharSet != null) {
                        String[] charSets = JOrphanUtils.split(acceptCharSet, ",");
                        // Just use the first one of the possible many charsets
                        if(charSets.length > 0) {
                            formCharSet = charSets[0].trim();
                            if(formCharSet.length() == 0) {
                                formCharSet = null;
                            }
                        }
                    }
                    if(formCharSet != null) {
                        synchronized (formEncodings) {
                            formEncodings.put(action, formCharSet);
                        }
                    }
                }
            }

            // second, if the tag was a composite tag,
            // recursively parse its children.
            if (tag instanceof CompositeTag) {
                CompositeTag composite = (CompositeTag) tag;
                parseNodes(composite.elements(), formEncodings, pageEncoding);
            }
        }
    }
}
