// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MonitorHandler extends DefaultHandler
{

    /**
     * 
     */
    public MonitorHandler()
    {
        super();
    }

	public void startDocument ()
	throws SAXException
	{
	}
	
	public void endDocument ()
	throws SAXException
	{
	}

	/**
	 * Receive notification of the start of an element.
	 *
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the start of
	 * each element (such as allocating a new tree node or writing
	 * output to a file).</p>
	 *
	 * @param uri
	 * @param localName The element type name.
	 * @param qName
	 * @param attributes The specified or defaulted attributes.
	 * @exception org.xml.sax.SAXException Any SAX exception, possibly
	 *            wrapping another exception.
	 * @see org.xml.sax.ContentHandler#startElement
	 */
	public void startElement (String uri, String localName,
				  String qName, Attributes attributes)
	throws SAXException
	{
	// no op
	}
    
    
	/**
	 * Receive notification of the end of an element.
	 *
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass to take specific actions at the end of
	 * each element (such as finalising a tree node or writing
	 * output to a file).</p>
	 *
	 * @param uri
	 * @param localName The element type name.
	 * @param qName
	 * @exception org.xml.sax.SAXException Any SAX exception, possibly
	 *            wrapping another exception.
	 * @see org.xml.sax.ContentHandler#endElement
	 */
	public void endElement (String uri, String localName, String qName)
	throws SAXException
	{
	}

	/**
	 * Receive notification of character data inside an element.
	 *
	 * <p>By default, do nothing.  Application writers may override this
	 * method to take specific actions for each chunk of character data
	 * (such as adding the data to a node or buffer, or printing it to
	 * a file).</p>
	 *
	 * @param ch The characters.
	 * @param start The start position in the character array.
	 * @param length The number of characters to use from the
	 *               character array.
	 * @exception org.xml.sax.SAXException Any SAX exception, possibly
	 *            wrapping another exception.
	 * @see org.xml.sax.ContentHandler#characters
	 */
	public void characters (char ch[], int start, int length)
	throws SAXException
	{
	}
    
}
