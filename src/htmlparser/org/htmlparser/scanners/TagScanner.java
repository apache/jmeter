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
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.NodeReader;
import org.htmlparser.StringNode;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserFeedback;

/**
 * TagScanner is an abstract superclass which is subclassed to create specific 
 * scanners, that operate on a tag's strings, identify it, and can extract data from it.
 * <br>
 * If you wish to write your own scanner, then you must implement scan().
 * You MAY implement evaluate() as well, if your evaluation logic is not based on a simple text match.
 * You MUST implement getID() - which identifies your scanner uniquely in the hashtable of scanners.
 * 
 * <br>
 * Also, you have a feedback object provided to you, should you want to send log messages. This object is
 * instantiated by Parser when a scanner is added to its collection.
 * 
 */
public abstract class TagScanner implements Serializable
{
    /**
     * A filter which is used to associate this tag. The filter contains a string
     * that is used to match which tags are to be allowed to pass through. This can
     * be useful when one wishes to dynamically filter out all tags except one type
     * which may be programmed later than the parser. Is also useful for command line
     * implementations of the parser.
     */
    protected String filter;

    /**
     * HTMLParserFeedback object automatically initialized
     */
    protected ParserFeedback feedback;
    /**
     * Default Constructor, automatically registers the scanner into a static array of 
     * scanners inside Tag
     */
    public TagScanner()
    {
        this.filter = "";
    }
    /**
     * This constructor automatically registers the scanner, and sets the filter for this
     * tag. 
     * @param filter The filter which will allow this tag to pass through.
     */
    public TagScanner(String filter)
    {
        this.filter = filter;
    }
    /**
    * Insert the method's description here.
    * Creation date: (6/4/2001 11:44:09 AM)
    * @return java.lang.String
    * @param c char
    */
    public String absorb(String s, char c)
    {
        int index = s.indexOf(c);
        if (index != -1)
            s = s.substring(index + 1, s.length());
        return s;
    }

    /**
     * Remove whitespace from the front of the given string.
     * @param s The string to trim.
     * @return Either the same string or a string with whitespace chopped off.
     */
    public static String absorbLeadingBlanks(String s)
    {
        int length;
        int i;
        String ret;

        i = 0;
        length = s.length();
        while (i < length && Character.isWhitespace(s.charAt(i)))
            i++;
        if (0 == i)
            ret = s;
        else if (length == i)
            ret = "";
        else
            ret = s.substring(i);

        return (ret);
    }

    /**
     * This method is used to decide if this scanner can handle this tag type. If the
     * evaluation returns true, the calling side makes a call to scan().
     * <strong>This method has to be implemented meaningfully only if a first-word match with 
     * the scanner id does not imply a match (or extra processing needs to be done).
     * Default returns true</strong>
     * @param s The complete text contents of the Tag.
     * @param previousOpenScanner Indicates any previous scanner which hasnt completed, before the current
     * scan has begun, and hence allows us to write scanners that can work with dirty html
     */
    public boolean evaluate(String s, TagScanner previousOpenScanner)
    {
        return true;
    }
    public static String extractXMLData(
        Node node,
        String tagName,
        NodeReader reader)
        throws ParserException
    {
        try
        {
            String xmlData = "";

            boolean xmlTagFound = isXMLTagFound(node, tagName);
            if (xmlTagFound)
            {
                try
                {
                    do
                    {
                        node = reader.readElement();
                        if (node != null)
                        {
                            if (node instanceof StringNode)
                            {
                                StringNode stringNode = (StringNode) node;
                                if (xmlData.length() > 0)
                                    xmlData += " ";
                                xmlData += stringNode.getText();
                            }
                            else if (
                                !(node instanceof org.htmlparser.tags.EndTag))
                                xmlTagFound = false;
                        }
                    }
                    while (node instanceof StringNode);

                }

                catch (Exception e)
                {
                    throw new ParserException(
                        "HTMLTagScanner.extractXMLData() : error while trying to find xml tag",
                        e);
                }
            }
            if (xmlTagFound)
            {
                if (node != null)
                {
                    if (node instanceof org.htmlparser.tags.EndTag)
                    {
                        org.htmlparser.tags.EndTag endTag =
                            (org.htmlparser.tags.EndTag) node;
                        if (!endTag.getText().equals(tagName))
                            xmlTagFound = false;
                    }

                }

            }
            if (xmlTagFound)
                return xmlData;
            else
                return null;
        }
        catch (Exception e)
        {
            throw new ParserException(
                "HTMLTagScanner.extractXMLData() : Error occurred while trying to extract xml tag",
                e);
        }
    }

    public String getFilter()
    {
        return filter;
    }

    public static boolean isXMLTagFound(Node node, String tagName)
    {
        boolean xmlTagFound = false;
        if (node instanceof Tag)
        {
            Tag tag = (Tag) node;
            if (tag.getText().toUpperCase().indexOf(tagName) == 0)
            {
                xmlTagFound = true;
            }
        }
        return xmlTagFound;
    }

    public final Tag createScannedNode(
        Tag tag,
        String url,
        NodeReader reader,
        String currLine)
        throws ParserException
    {
        Tag thisTag = scan(tag, url, reader, currLine);
        thisTag.setThisScanner(this);
        thisTag.setAttributes(tag.getAttributes());
        return thisTag;
    }

    /** 
     * Scan the tag and extract the information related to this type. The url of the 
     * initiating scan has to be provided in case relative links are found. The initial 
     * url is then prepended to it to give an absolute link.
     * The NodeReader is provided in order to do a lookahead operation. We assume that
     * the identification has already been performed using the evaluate() method.
     * @param tag HTML Tag to be scanned for identification
     * @param url The initiating url of the scan (Where the html page lies)
     * @param reader The reader object responsible for reading the html page
     */
    public Tag scan(Tag tag, String url, NodeReader reader, String currLine)
        throws ParserException
    {
        return createTag(
            new TagData(
                tag.elementBegin(),
                tag.elementEnd(),
                tag.getText(),
                currLine),
            tag,
            url);
    }

    public String removeChars(String s, String occur)
    {
        StringBuffer newString = new StringBuffer();
        char ch;
        int index;
        do
        {
            index = s.indexOf(occur);
            if (index != -1)
            {
                newString.append(s.substring(0, index));
                s = s.substring(index + occur.length());
            }
        }
        while (index != -1);
        newString.append(s);
        return newString.toString();
    }

    public abstract String[] getID();

    public final void setFeedback(ParserFeedback feedback)
    {
        this.feedback = feedback;
    }

    public static Map adjustScanners(NodeReader reader)
    {
        Map tempScanners = new Hashtable();
        tempScanners = reader.getParser().getScanners();
        // Remove all existing scanners
        reader.getParser().flushScanners();
        return tempScanners;
    }

    public static void restoreScanners(
        NodeReader pReader,
        Hashtable tempScanners)
    {
        // Flush the scanners
        pReader.getParser().setScanners(tempScanners);
    }

    /**
     * Insert an EndTag in the currentLine, just before the occurence of the provided tag
     */
    public String insertEndTagBeforeNode(Node node, String currentLine)
    {
        String newLine = currentLine.substring(0, node.elementBegin());
        newLine += "</A>";
        newLine
            += currentLine.substring(node.elementBegin(), currentLine.length());
        return newLine;
    }

    /**
     * Override this method to create your own tag type
     * @param tagData
     * @param tag
     * @param url
     * @return Tag
     * @throws ParserException
     */
    protected Tag createTag(TagData tagData, Tag tag, String url)
        throws ParserException
    {
        return null;
    }

    protected Tag getReplacedEndTag(
        Tag tag,
        NodeReader reader,
        String currentLine)
    {
        // Replace tag - it was a <A> tag - replace with </a>
        String newLine = replaceFaultyTagWithEndTag(tag, currentLine);
        reader.changeLine(newLine);
        return new EndTag(
            new TagData(
                tag.elementBegin(),
                tag.elementBegin() + 3,
                tag.getTagName(),
                currentLine));
    }

    public String replaceFaultyTagWithEndTag(Tag tag, String currentLine)
    {
        String newLine = currentLine.substring(0, tag.elementBegin());
        newLine += "</" + tag.getTagName() + ">";
        newLine
            += currentLine.substring(tag.elementEnd() + 1, currentLine.length());

        return newLine;
    }

    protected Tag getInsertedEndTag(
        Tag tag,
        NodeReader reader,
        String currentLine)
    {
        // Insert end tag
        String newLine = insertEndTagBeforeNode(tag, currentLine);
        reader.changeLine(newLine);
        return new EndTag(
            new TagData(
                tag.elementBegin(),
                tag.elementBegin() + 3,
                tag.getTagName(),
                currentLine));
    }

}
