/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 
 */

// The developers of JMeter and Apache are greatful to the developers
// of HTMLParser for giving Apache Software Foundation a non-exclusive
// license. The performance benefits of HTMLParser are clear and the
// users of JMeter will benefit from the hard work the HTMLParser
// team. For detailed information about HTMLParser, the project is
// hosted on sourceforge at http://htmlparser.sourceforge.net/.
//
// HTMLParser was originally created by Somik Raha in 2000. Since then
// a healthy community of users has formed and helped refine the
// design so that it is able to tackle the difficult task of parsing
// dirty HTML. Derrick Oswald is the current lead developer and was kind
// enough to assist JMeter.

package org.htmlparser.scanners;


//////////////////
// Java Imports //
//////////////////
import java.util.Hashtable;

import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.ParserException;

/**
 * Scans for the Frame Tag. This is a subclass of TagScanner, and is called using a
 * variant of the template method. If the evaluate() method returns true, that means the
 * given string contains an image tag. Extraction is done by the scan method thereafter
 * by the user of this class.
 */
public class FrameScanner extends TagScanner
{
    /**
     * Overriding the default constructor
     */
    public FrameScanner()
    {
        super();
    }
    /**
     * Overriding the constructor to accept the filter
     */
    public FrameScanner(String filter)
    {
        super(filter);
    }
    /**
     * Extract the location of the image, given the string to be parsed, and the url
     * of the html page in which this tag exists.
     * @param s String to be parsed
     * @param url URL of web page being parsed
     */
    public String extractFrameLocn(Tag tag, String url) throws ParserException
    {
        try
        {
            Hashtable table = tag.getAttributes();
            String relativeFrame = (String) table.get("SRC");
            if (relativeFrame == null)
                return "";
            else
                return (new LinkProcessor()).extract(relativeFrame, url);
        }
        catch (Exception e)
        {
            String msg;
            if (tag != null)
                msg = tag.getText();
            else
                msg = "null";
            throw new ParserException(
                "HTMLFrameScanner.extractFrameLocn() : Error in extracting frame location from tag "
                    + msg,
                e);
        }
    }

    public String extractFrameName(Tag tag, String url)
    {
        return tag.getAttribute("NAME");
    }

    /**
     * @see org.htmlparser.scanners.TagScanner#getID()
     */
    public String[] getID()
    {
        String[] ids = new String[1];
        ids[0] = "FRAME";
        return ids;
    }

    protected Tag createTag(TagData tagData, Tag tag, String url)
        throws ParserException
    {
        String frameUrl = extractFrameLocn(tag, url);
        String frameName = extractFrameName(tag, url);

        return new FrameTag(tagData, frameUrl, frameName);
    }

}
