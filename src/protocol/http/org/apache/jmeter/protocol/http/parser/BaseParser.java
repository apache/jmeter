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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * BaseParser is the base class for {@link LinkExtractorParser}
 * It is advised to make subclasses reusable across parsing, so {@link BaseParser}{@link #isReusable()} returns true by default
 * @since 3.0
 */
public abstract class BaseParser implements LinkExtractorParser {
    private static final Logger LOG = LoggerFactory.getLogger(BaseParser.class);
    // Cache of parsers - parsers must be re-usable
    private static final ConcurrentMap<String, LinkExtractorParser> PARSERS = new ConcurrentHashMap<>(5);

    /**
     * Constructor for BaseParser
     */
    public BaseParser() {
    }

    /**
     * Factory method of parsers. Instances might get cached, when
     * {@link LinkExtractorParser#isReusable()} on the newly created instance
     * equals {@code true}.
     * 
     * @param parserClassName
     *            name of the class that should be used to create new parsers
     * @return a possibly cached instance of the wanted
     *         {@link LinkExtractorParser}
     * @throws LinkExtractorParseException
     *             when a new instance could not be instantiated
     */
    public static LinkExtractorParser getParser(String parserClassName) 
            throws LinkExtractorParseException {

        // Is there a cached parser?
        LinkExtractorParser parser = PARSERS.get(parserClassName);
        if (parser != null) {
            LOG.debug("Fetched " + parserClassName);
            return parser;
        }

        try {
            Object clazz = Class.forName(parserClassName).newInstance();
            if (clazz instanceof LinkExtractorParser) {
                parser = (LinkExtractorParser) clazz;
            } else {
                throw new LinkExtractorParseException(new ClassCastException(parserClassName));
            }
        } catch (InstantiationException | ClassNotFoundException
                | IllegalAccessException e) {
            throw new LinkExtractorParseException(e);
        }
        LOG.info("Created " + parserClassName);
        if (parser.isReusable()) {
            LinkExtractorParser currentParser = PARSERS.putIfAbsent(
                    parserClassName, parser);// cache the parser if not already
                                             // done by another thread
            if (currentParser != null) {
                return currentParser;
            }
        }

        return parser;
    }
    
    /**
     * Parsers should over-ride this method if the parser class is re-usable, in
     * which case the class will be cached for the next getParser() call.
     *
     * @return {@code true} if the Parser is reusable
     */
    @Override
    public boolean isReusable() {
        return true;
    }

}
