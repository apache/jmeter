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
	 * @param name The element type name.
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
	 * @param name The element type name.
	 * @param attributes The specified or defaulted attributes.
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
