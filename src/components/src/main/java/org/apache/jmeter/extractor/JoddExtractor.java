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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import jodd.csselly.CSSelly;
import jodd.csselly.CssSelector;
import jodd.lagarto.dom.LagartoDOMBuilder;
import jodd.lagarto.dom.Node;
import jodd.lagarto.dom.NodeSelector;
import jodd.log.LoggerFactory;
import jodd.log.impl.Slf4jLogger;

/**
 * Jodd-Lagerto based CSS/JQuery extractor
 * see http://jodd.org/doc/csselly/
 * @since 2.9
 */
public class JoddExtractor implements Extractor {

    private static final long serialVersionUID = -7235814605293262972L;

    private static final String CACHE_KEY_PREFIX = JoddExtractor.class.getName()+"_PARSED_BODY";
    private static final LoadingCache<String, List<List<CssSelector>>> CSS_SELECTOR_CACHE;
    static {
        LoggerFactory.setLoggerProvider(Slf4jLogger.PROVIDER);
        final int cacheSize = JMeterUtils.getPropDefault(
                    "cssselector.parser.cache.size", 400);
        CSS_SELECTOR_CACHE = Caffeine.newBuilder().maximumSize(cacheSize).build(CSSelly::parse);
    }

    public JoddExtractor() {
        super();
    }

    /**
     * @see org.apache.jmeter.extractor.Extractor#extract(String, String, int, String, List, int, String)
     */
    @Override
    public int extract(String expression, String attribute, int matchNumber,
            String inputString, List<String> result, int found,
            String cacheKey) {
        NodeSelector nodeSelector;
        if (cacheKey != null) {
            nodeSelector = (NodeSelector)
                    JMeterContextService.getContext().getSamplerContext().get(CACHE_KEY_PREFIX+cacheKey);
            if(nodeSelector==null) {
                LagartoDOMBuilder domBuilder = new LagartoDOMBuilder();
                jodd.lagarto.dom.Document doc = domBuilder.parse(inputString);
                nodeSelector = new NodeSelector(doc);
                JMeterContextService.getContext().getSamplerContext().put(CACHE_KEY_PREFIX+cacheKey, nodeSelector);
            }
        } else {
            LagartoDOMBuilder domBuilder = new LagartoDOMBuilder();
            jodd.lagarto.dom.Document doc = domBuilder.parse(inputString);
            nodeSelector = new NodeSelector(doc);
        }

        List<List<CssSelector>> cssSelectors = CSS_SELECTOR_CACHE.get(expression);
        List<Node> elements = nodeSelector.select(cssSelectors);
        for (Node element : elements) {
            if (matchNumber <= 0 || found != matchNumber) {
                result.add(extractValue(attribute, element));
                found++;
            } else {
                break;
            }
        }

        return found;
    }


    private String extractValue(String attribute, Node element) {
        if (!JOrphanUtils.isBlank(attribute)) {
            return element.getAttribute(attribute);
        } else {
            return element.getTextContent().trim();
        }
    }
}
