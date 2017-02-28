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

package org.apache.jmeter.assertions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * {@link ErrorHandler} implementation that logs
 * @since 3.2
 */
public class LogErrorHandler implements ErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Override
    public void warning(SAXParseException exception) throws SAXException {
        logger.warn("Exception parsing document, message:{}", exception.getMessage());
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        logger.error("Exception parsing document, message:{}", exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        logger.error("Fatal Exception parsing document, message:{}", exception.getMessage());
    }
}
