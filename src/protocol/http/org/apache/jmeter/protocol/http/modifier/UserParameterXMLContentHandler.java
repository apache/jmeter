/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * @author     Mark Walsh
 * @version    $Revision$
 */
public class UserParameterXMLContentHandler implements ContentHandler
{
    //-------------------------------------------
    // Constants and Data Members
    //-------------------------------------------

    // Note UserParameterXML accesses this variable
    // to obtain the Set data via method getParsedParameters()
    private List userThreads = new LinkedList();

    private String paramname = "";
    private String paramvalue = "";
    private Map nameValuePair = new HashMap();

    /** Buffer for collecting data from the "characters" SAX event. */
    private CharArrayWriter contents = new CharArrayWriter();

    //-------------------------------------------
    // Methods
    //-------------------------------------------

    /*-------------------------------------------------------------------------
     * Methods implemented from org.xml.sax.ContentHandler
     *----------------------------------------------------------------------- */
    public void setDocumentLocator(Locator locator)
    {
    }

    public void startDocument() throws SAXException
    {
    }

    public void endDocument() throws SAXException
    {
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException
    {
    }

    public void endPrefixMapping(String prefix) throws SAXException
    {
    }

    public void startElement(
        String namespaceURL,
        String localName,
        String qName,
        Attributes atts)
        throws SAXException
    {

        contents.reset();

        // haven't got to reset paramname & paramvalue
        // but did it to keep the code looking correct
        if (qName.equals("parameter"))
        {
            paramname = "";
            paramvalue = "";
        }

        // must create a new object,
        // or else end up with a set full of the same Map object
        if (qName.equals("thread"))
        {
            nameValuePair = new HashMap();
        }

    }

    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException
    {
        if (qName.equals("paramname"))
        {
            paramname = contents.toString();
        }
        if (qName.equals("paramvalue"))
        {
            paramvalue = contents.toString();
        }
        if (qName.equals("parameter"))
        {
            nameValuePair.put(paramname, paramvalue);
        }
        if (qName.equals("thread"))
        {
            userThreads.add(nameValuePair);
        }
    }

    public void characters(char ch[], int start, int length)
        throws SAXException
    {
        contents.write(ch, start, length);
    }

    public void ignorableWhitespace(char ch[], int start, int length)
        throws SAXException
    {
    }

    public void processingInstruction(String target, String date)
        throws SAXException
    {
    }

    public void skippedEntity(String name) throws SAXException
    {
    }

    /*-------------------------------------------------------------------------
     * Methods (used by UserParameterXML to get XML parameters from XML file)
     *----------------------------------------------------------------------- */

    /**
     * results of parsing all user parameter data defined in XML file.
     * @return all users name value pairs obtained from XML file
     */
    public List getParsedParameters()
    {
        return userThreads;
    }
}
