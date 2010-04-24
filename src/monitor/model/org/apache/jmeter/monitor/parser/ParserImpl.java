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
 */
package org.apache.jmeter.monitor.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.jmeter.monitor.model.ObjectFactory;
import org.apache.jmeter.monitor.model.Status;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class ParserImpl implements Parser {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final SAXParser PARSER;

    private final MonitorHandler DOCHANDLER;

    private final ObjectFactory FACTORY;

    /**
     *
     */
    public ParserImpl(ObjectFactory factory) {
        super();
        this.FACTORY = factory;
        SAXParser parser = null;
        MonitorHandler handler = null;
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            parser = parserFactory.newSAXParser();
            handler = new MonitorHandler();
            handler.setObjectFactory(this.FACTORY);
        } catch (SAXException e) {
            log.error("Failed to create the parser",e);
        } catch (ParserConfigurationException e) {
            log.error("Failed to create the parser",e);
        }
        PARSER = parser;
        DOCHANDLER = handler;
    }

    /**
     * parse byte array and return Status object
     *
     * @param bytes
     * @return Status
     */
    public Status parseBytes(byte[] bytes) {
        try {
            InputSource is = new InputSource();
            is.setByteStream(new ByteArrayInputStream(bytes));
            PARSER.parse(is, DOCHANDLER);
            return DOCHANDLER.getContents();
        } catch (SAXException e) {
            log.error("Failed to parse the bytes",e);
            // let bad input fail silently
            return DOCHANDLER.getContents();
        } catch (IOException e) { // Should never happen
            log.error("Failed to read the bytes",e);
            // let bad input fail silently
            return DOCHANDLER.getContents();
        }
    }

    /**
     * @param content
     * @return Status
     */
    public Status parseString(String content) {
        try {
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(content));
            PARSER.parse(is, DOCHANDLER);
            return DOCHANDLER.getContents();
        } catch (SAXException e) {
            log.error("Failed to parse the String",e);
            // let bad input fail silently
            return DOCHANDLER.getContents();
        } catch (IOException e) { // Should never happen
            log.error("Failed to read the String",e);
            // let bad input fail silently
            return DOCHANDLER.getContents();
        }
    }

    /**
     * @param result
     * @return Status
     */
    public Status parseSampleResult(SampleResult result) {
        return parseBytes(result.getResponseData());
    }

}
