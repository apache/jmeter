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

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.protocol.http.parser.HTMLParseException;
import org.slf4j.LoggerFactory;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A parser for html, to find the form tags, and their accept-charset value
 */
// made public see Bug 49976
public class FormCharSetFinder {
    private static final Logger log = LoggerFactory.getLogger(FormCharSetFinder.class);

    public FormCharSetFinder() {
        super();
    }

    /**
     * Add form action urls and their corresponding encodings for all forms on the page
     *
     * @param html the html to parse for form encodings
     * @param formEncodings the Map where form encodings should be added
     * @param pageEncoding the encoding used for the whole page
     * @throws HTMLParseException when parsing the <code>html</code> fails
     */
    public void addFormActionsAndCharSet(String html, Map<String, String> formEncodings, String pageEncoding)
            throws HTMLParseException {
        if (log.isDebugEnabled()) {
            log.debug("Parsing html of: " + html);
        }

        Document document = Jsoup.parse(html);
        Elements forms = document.select("form");
        for (Element element : forms) {
            String action = element.attr("action");
            if( !(StringUtils.isEmpty(action)) ) {
                // We use the page encoding where the form resides, as the
                // default encoding for the form
                String formCharSet = pageEncoding;
                String acceptCharSet = element.attr("accept-charset");
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
    }
}
