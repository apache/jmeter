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
import org.htmlparser.Parser;
import org.htmlparser.tags.FormTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.LinkProcessor;
import org.htmlparser.util.ParserException;

/**
 * Scans for the Image Tag. This is a subclass of TagScanner, and is called using a
 * variant of the template method. If the evaluate() method returns true, that means the
 * given string contains an image tag. Extraction is done by the scan method thereafter
 * by the user of this class.
 */
public class FormScanner extends CompositeTagScanner
{
    private static final String[] MATCH_ID = { "FORM" };
    public static final String PREVIOUS_DIRTY_LINK_MESSAGE =
        "Encountered a form tag after an open link tag.\nThere should have been an end tag for the link before the form tag began.\nCorrecting this..";
    private boolean linkScannerAlreadyOpen = false;
    private static final String[] formTagEnders = { "HTML", "BODY" };
    /**
    * HTMLFormScanner constructor comment.
    */
    public FormScanner(Parser parser)
    {
        this("", parser);
    }
    /**
     * Overriding the constructor to accept the filter
     */
    public FormScanner(String filter, Parser parser)
    {
        super(filter, MATCH_ID, formTagEnders, false);
        parser.addScanner(new InputTagScanner("-i"));
        parser.addScanner(new TextareaTagScanner("-t"));
        parser.addScanner(new SelectTagScanner("-select"));
        parser.addScanner(new OptionTagScanner("-option"));
    }

    /**
     * Extract the location of the image, given the string to be parsed, and the url
     * of the html page in which this tag exists.
     * @param s String to be parsed
     * @param url URL of web page being parsed
     */
    public String extractFormLocn(Tag tag, String url) throws ParserException
    {
        try
        {
            String formURL = tag.getAttribute("ACTION");
            if (formURL == null)
                return "";
            else
                return (new LinkProcessor()).extract(formURL, url);
        }
        catch (Exception e)
        {
            String msg;
            if (tag != null)
                msg = tag.getText();
            else
                msg = "";
            throw new ParserException(
                "HTMLFormScanner.extractFormLocn() : Error in extracting form location, tag = "
                    + msg
                    + ", url = "
                    + url,
                e);
        }
    }

    public String extractFormName(Tag tag)
    {
        return tag.getAttribute("NAME");
    }

    public String extractFormMethod(Tag tag)
    {
        String method = tag.getAttribute("METHOD");
        if (method == null)
            method = FormTag.GET;
        return method.toUpperCase();

    }

    /**
     * Scan the tag and extract the information related to the <IMG> tag. The url of the
     * initiating scan has to be provided in case relative links are found. The initial
     * url is then prepended to it to give an absolute link.
     * The NodeReader is provided in order to do a lookahead operation. We assume that
     * the identification has already been performed using the evaluate() method.
     * @param tag HTML Tag to be scanned for identification
     * @param url The initiating url of the scan (Where the html page lies)
     * @param reader The reader object responsible for reading the html page
     * @param currentLine The current line (automatically provided by Tag)
     */
    //	public Tag scan(Tag tag,String url,NodeReader reader,String currentLine) throws ParserException
    //	{
    //		if (linkScannerAlreadyOpen) {
    //			String newLine = insertEndTagBeforeNode(tag, currentLine);
    //			reader.changeLine(newLine);
    //			return new EndTag(
    //				new TagData(
    //					tag.elementBegin(),
    //					tag.elementBegin()+3,
    //					"A",
    //					currentLine
    //				)
    //			);
    //		}
    //		return super.scan(tag,url,reader,currentLine);
    //	}

    /**
     * @see org.htmlparser.scanners.TagScanner#getID()
     */
    public String[] getID()
    {
        return MATCH_ID;
    }

    public boolean evaluate(String s, TagScanner previousOpenScanner)
    {
        if (previousOpenScanner instanceof LinkScanner)
        {
            linkScannerAlreadyOpen = true;
            StringBuffer msg = new StringBuffer();
            msg.append("<");
            msg.append(s);
            msg.append(">");
            msg.append(PREVIOUS_DIRTY_LINK_MESSAGE);
            feedback.warning(msg.toString());
            // This is dirty HTML. Assume the current tag is
            // not a new link tag - but an end tag. This is actually a really wild bug - 
            // Internet Explorer actually parses such tags.
            // So - we shall then proceed to fool the scanner into sending an endtag of type </A>
            // For this - set the dirty flag to true and return
        }
        else
            linkScannerAlreadyOpen = false;
        return super.evaluate(s, previousOpenScanner);
    }

    public Tag createTag(TagData tagData, CompositeTagData compositeTagData)
        throws ParserException
    {
        String formUrl =
            extractFormLocn(
                compositeTagData.getStartTag(),
                tagData.getUrlBeingParsed());
        if (formUrl != null && formUrl.length() > 0)
            compositeTagData.getStartTag().setAttribute("ACTION", formUrl);
        return new FormTag(tagData, compositeTagData);
    }

}
