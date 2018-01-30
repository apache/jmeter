/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.extractor;

import java.util.List;

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.util.JOrphanUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * JSoup based CSS/JQuery extractor
 * see http://jsoup.org/cookbook/extracting-data/selector-syntax
 * @since 2.9
 */
public class JSoupExtractor implements Extractor {

    private static final long serialVersionUID = -6308012192067714191L;

    private static final String CACHE_KEY_PREFIX = JSoupExtractor.class.getName()+"_PARSED_BODY";

    public JSoupExtractor() {
        super();
    }

    /**
     * @see Extractor#extract(String, String, int, String, List, int, String)
     */
    @Override
    public int extract(String expression, String attribute, int matchNumber,
            String inputString, List<String> result, int found,
            String cacheKey) {
        Document document;
        if (cacheKey != null) {
            document = (Document) 
                    JMeterContextService.getContext().getSamplerContext().get(CACHE_KEY_PREFIX+cacheKey);
            if(document==null) {
                document = Jsoup.parse(inputString);
                JMeterContextService.getContext().getSamplerContext().put(CACHE_KEY_PREFIX+cacheKey, document);
            }
        } else {
            document = Jsoup.parse(inputString);
        }
        Elements elements = document.select(expression);
        for (Element element : elements) {
            if (matchNumber <= 0 || found != matchNumber) {
                result.add(extractValue(attribute, element));
                found++;
            } else {
                break;
            }
        }
        return found;
    }
    
    
    /**
     * 
     * @param attribute Attribute to extract
     * @param element Element
     * @return String value
     */
    private String extractValue(String attribute, Element element) {
        if (!JOrphanUtils.isBlank(attribute)) {
            return element.attr(attribute);
        } else {
            return element.text().trim();
        }
    }
}
