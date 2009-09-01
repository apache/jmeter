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

package org.apache.jmeter.protocol.http.modifier;

import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * The handler used to read in XML parameter data.
 *
 * @version $Revision$
 */
public class UserParameterXMLContentHandler implements ContentHandler {
    // -------------------------------------------
    // Constants and Data Members
    // -------------------------------------------

    // Note UserParameterXML accesses this variable
    // to obtain the Set data via method getParsedParameters()
    private List<Map<String, String>> userThreads = new LinkedList<Map<String, String>>();

    private String paramname = "";

    private String paramvalue = "";

    private Map<String, String> nameValuePair = new HashMap<String, String>();

    /** Buffer for collecting data from the "characters" SAX event. */
    private CharArrayWriter contents = new CharArrayWriter();

    // -------------------------------------------
    // Methods
    // -------------------------------------------

    /*-------------------------------------------------------------------------
     * Methods implemented from org.xml.sax.ContentHandler
     *----------------------------------------------------------------------- */
    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void startElement(String namespaceURL, String localName, String qName, Attributes atts) throws SAXException {

        contents.reset();

        // haven't got to reset paramname & paramvalue
        // but did it to keep the code looking correct
        if (qName.equals("parameter")) {
            paramname = "";
            paramvalue = "";
        }

        // must create a new object,
        // or else end up with a set full of the same Map object
        if (qName.equals("thread")) {
            nameValuePair = new HashMap<String, String>();
        }

    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (qName.equals("paramname")) {
            paramname = contents.toString();
        }
        if (qName.equals("paramvalue")) {
            paramvalue = contents.toString();
        }
        if (qName.equals("parameter")) {
            nameValuePair.put(paramname, paramvalue);
        }
        if (qName.equals("thread")) {
            userThreads.add(nameValuePair);
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        contents.write(ch, start, length);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
    }

    public void processingInstruction(String target, String date) throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    /*-------------------------------------------------------------------------
     * Methods (used by UserParameterXML to get XML parameters from XML file)
     *----------------------------------------------------------------------- */

    /**
     * results of parsing all user parameter data defined in XML file.
     *
     * @return all users name value pairs obtained from XML file
     */
    public List<Map<String, String>> getParsedParameters() {
        return userThreads;
    }
}
