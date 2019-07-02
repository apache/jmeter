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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

public class Document {

    private static final Logger log = LoggerFactory.getLogger(Document.class);

    // Maximum size to convert a document to text (default 10Mb)
    private static final int MAX_DOCUMENT_SIZE =
        JMeterUtils.getPropDefault("document.max_size", 10 * 1024 * 1024); // $NON-NLS-1$

    /**
     * Convert to text plain a lot of kind of document (like odt, ods, odp,
     * doc(x), xls(x), ppt(x), pdf, mp3, mp4, etc.) with Apache Tika
     *
     * @param document
     *            binary representation of the document
     * @return text from document without format
     */
    public static String getTextFromDocument(byte[] document) {
        String errMissingTika = JMeterUtils.getResString("view_results_response_missing_tika"); // $NON-NLS-1$
        String response = errMissingTika;
        Parser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler(MAX_DOCUMENT_SIZE > 0 ? MAX_DOCUMENT_SIZE : -1); // -1 to disable the write limit
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        InputStream stream = new ByteArrayInputStream(document); // open the stream
        try {
            parser.parse(stream, handler, metadata, context);
            response = handler.toString();
        } catch (Exception e) {
            response = e.toString();
            log.warn("Error document parsing.", e);
        } catch (NoClassDefFoundError e) {
            // put a warning if tika-app.jar missing (or some dependencies in only tika-core|parsers packages are using)
            if (!System.getProperty("java.class.path").contains("tika-app")) { // $NON-NLS-1$ $NON-NLS-2$
                log.warn(errMissingTika);
            } else {
                log.warn(errMissingTika, e);
            }
        } finally {
            try {
                stream.close(); // close the stream
            } catch (IOException ioe) {
                log.warn("Error closing document stream", ioe);// $NON-NLS-1$
            }
        }

        if (response.length() == 0 && document.length > 0) {
            log.warn("Probably: {}", errMissingTika);// $NON-NLS-1$
            response = errMissingTika;
        }
        return response;
    }
}
