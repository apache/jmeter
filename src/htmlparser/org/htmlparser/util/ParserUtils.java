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

package org.htmlparser.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.NodeReader;
import org.htmlparser.scanners.TagScanner;
import org.htmlparser.tags.Tag;

public class ParserUtils
{
    public static boolean evaluateTag(
        TagScanner pTagScanner,
        String pTagString,
        String pTagName)
    {
        pTagString = TagScanner.absorbLeadingBlanks(pTagString);
        if (pTagString.toUpperCase().indexOf(pTagName) == 0)
            return true;
        else
            return false;
    }

    public static String toHTML(Tag tag)
    {
        StringBuffer htmlString = new StringBuffer();

        Hashtable attrs = tag.getAttributes();
        String pTagName = tag.getAttribute(Tag.TAGNAME);
        htmlString.append("<").append(pTagName);
        for (Enumeration e = attrs.keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            String value = (String) attrs.get(key);
            if (!key.equalsIgnoreCase(Tag.TAGNAME) && value.length() > 0)
                htmlString.append(" ").append(key).append("=\"").append(
                    value).append(
                    "\"");
        }
        htmlString.append(">");

        return htmlString.toString();
    }

    public static String toString(Tag tag)
    {
        String tagName = tag.getAttribute(Tag.TAGNAME);
        Hashtable attrs = tag.getAttributes();

        StringBuffer lString = new StringBuffer(tagName);
        lString.append(" TAG\n");
        lString.append("--------\n");

        for (Enumeration e = attrs.keys(); e.hasMoreElements();)
        {
            String key = (String) e.nextElement();
            String value = (String) attrs.get(key);
            if (!key.equalsIgnoreCase(Tag.TAGNAME) && value.length() > 0)
                lString.append(key).append(" : ").append(value).append("\n");
        }

        return lString.toString();
    }

    public static Map adjustScanners(NodeReader reader)
    {
        Map tempScanners = new Hashtable();
        tempScanners = reader.getParser().getScanners();
        // Remove all existing scanners
        reader.getParser().flushScanners();
        return tempScanners;
    }
    public static void restoreScanners(NodeReader reader, Map tempScanners)
    {
        // Flush the scanners
        reader.getParser().setScanners(tempScanners);
    }

    public static String removeChars(String s, char occur)
    {
        StringBuffer newString = new StringBuffer();
        char ch;
        for (int i = 0; i < s.length(); i++)
        {
            ch = s.charAt(i);
            if (ch != occur)
                newString.append(ch);
        }
        return newString.toString();
    }

    public static String removeEscapeCharacters(String inputString)
    {
        inputString = ParserUtils.removeChars(inputString, '\r');
        inputString = ParserUtils.removeChars(inputString, '\n');
        inputString = ParserUtils.removeChars(inputString, '\t');
        return inputString;
    }

    public static String removeLeadingBlanks(String plainText)
    {
        while (plainText.indexOf(' ') == 0)
            plainText = plainText.substring(1);
        return plainText;
    }

    public static String removeTrailingBlanks(String text)
    {
        char ch = ' ';
        while (ch == ' ')
        {
            ch = text.charAt(text.length() - 1);
            if (ch == ' ')
                text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    /**
     * Search given node and pick up any objects of given type, return
     * Node array.
     * @param node
     * @param type
     * @return Node[]
     */
    public static Node[] findTypeInNode(Node node, Class type)
    {
        NodeList nodeList = new NodeList();
        node.collectInto(nodeList, type);
        Node spans[] = nodeList.toNodeArray();
        return spans;
    }

}