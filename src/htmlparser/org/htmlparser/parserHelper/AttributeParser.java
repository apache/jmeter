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

package org.htmlparser.parserHelper;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.htmlparser.tags.Tag;


/**
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 * @author Somik Raha, Kaarle Kaila
 * @version 7 AUG 2001
 */
public class AttributeParser
{
    private final String delima = " \t\r\n\f=\"'>";
    private final String delimb = " \t\r\n\f\"'>";
    private final char doubleQuote = '\"';
    private final char singleQuote = '\'';
    private String delim;

    /**
    * Method to break the tag into pieces.
    * @param returns a Hastable with elements containing the
    * pieces of the tag. The tag-name has the value field set to
    * the constant Tag.TAGNAME. In addition the tag-name is
    * stored into the Hashtable with the name Tag.TAGNAME
    * where the value is the name of the tag.
    * Tag parameters without value
    * has the value "". Parameters with value are represented
    * in the Hastable by a name/value pair.
    * As html is case insensitive but Hastable is not are all
    * names converted into UPPERCASE to the Hastable
    * E.g extract the href values from A-tag's and print them
    * <pre>
    *
    *    Tag tag;
    *    Hashtable h;
    *    String tmp;
    *    try {
    *        NodeReader in = new NodeReader(new FileReader(path),2048);
    *        Parser p = new Parser(in);
    *        Enumeration en = p.elements();
    *        while (en.hasMoreElements()) {
    *            try {
    *                tag = (Tag)en.nextElement();
    *                h = tag.parseParameters();
    *                tmp = (String)h.get(tag.TAGNAME);
    *                if (tmp != null && tmp.equalsIgnoreCase("A")) {;
    *                    System.out.println("URL is :" + h.get("HREF"));
    *                }
    *            } catch (ClassCastException ce){}
    *        }
    *    }
    *    catch (IOException ie) {
    *        ie.printStackTrace();
    *    }
    * </pre>
    *
    */
    public Hashtable parseAttributes(Tag tag)
    {
        Hashtable h = new Hashtable();
        String element, name, value, nextPart = null;
        String empty = null;
        name = null;
        value = null;
        element = null;
        boolean waitingForEqual = false;
        delim = delima;
        StringTokenizer tokenizer =
            new StringTokenizer(tag.getText(), delim, true);
        while (true)
        {
            nextPart = getNextPart(tokenizer, delim);
            delim = delima;
            if (element == null && nextPart != null && !nextPart.equals("="))
            {
                element = nextPart;
                putDataIntoTable(h, element, null, true);
            }
            else
            {
                if (nextPart != null && (0 < nextPart.length()))
                {
                    if (name == null)
                    {
                        if (!nextPart.substring(0, 1).equals(" "))
                        {
                            name = nextPart;
                            waitingForEqual = true;
                        }
                    }
                    else
                    {
                        if (waitingForEqual)
                        {
                            if (nextPart.equals("="))
                            {
                                waitingForEqual = false;
                                delim = delimb;
                            }
                            else
                            {
                                putDataIntoTable(h, name, "", false);
                                name = nextPart;
                                value = null;
                            }
                        }
                        if (!waitingForEqual && !nextPart.equals("="))
                        {
                            value = nextPart;
                            putDataIntoTable(h, name, value, false);
                            name = null;
                            value = null;
                        }
                    }
                }
                else
                {
                    if (name != null)
                    {
                        if (name.equals("/"))
                        {
                            putDataIntoTable(h, Tag.EMPTYTAG, "", false);
                        }
                        else
                        {
                            putDataIntoTable(h, name, "", false);
                        }
                        name = null;
                        value = null;
                    }
                    break;
                }
            }
        }
        if (null == element) // handle no tag contents
            putDataIntoTable(h, "", null, true);
        return h;
    }

    private String getNextPart(StringTokenizer tokenizer, String deli)
    {
        String tokenAccumulator = null;
        boolean isDoubleQuote = false;
        boolean isSingleQuote = false;
        boolean isDataReady = false;
        String currentToken;
        while (isDataReady == false && tokenizer.hasMoreTokens())
        {
            currentToken = tokenizer.nextToken(deli);
            //
            // First let's combine tokens that are inside "" or ''
            //
            if (isDoubleQuote || isSingleQuote)
            {
                if (isDoubleQuote && currentToken.charAt(0) == doubleQuote)
                {
                    isDoubleQuote = false;
                    isDataReady = true;
                }
                else if (
                    isSingleQuote && currentToken.charAt(0) == singleQuote)
                {
                    isSingleQuote = false;
                    isDataReady = true;
                }
                else
                {
                    tokenAccumulator += currentToken;
                    continue;
                }
            }
            else if (currentToken.charAt(0) == doubleQuote)
            {
                isDoubleQuote = true;
                tokenAccumulator = "";
                continue;
            }
            else if (currentToken.charAt(0) == singleQuote)
            {
                isSingleQuote = true;
                tokenAccumulator = "";
                continue;
            }
            else
                tokenAccumulator = currentToken;

            if (tokenAccumulator.equals(currentToken))
            {

                if (delim.indexOf(tokenAccumulator) >= 0)
                {
                    if (tokenAccumulator.equals("="))
                    {
                        isDataReady = true;
                    }
                }
                else
                {

                    isDataReady = true;
                }
            }
            else
                isDataReady = true;

        }
        return tokenAccumulator;
    }

    private void putDataIntoTable(
        Hashtable h,
        String name,
        String value,
        boolean isName)
    {
        if (isName && value == null)
            value = Tag.TAGNAME;
        else if (value == null)
            value = ""; // Hashtable does not accept nulls
        if (isName)
        {
            // store tagname as tag.TAGNAME,tag
            h.put(value, name.toUpperCase());
        }
        else
        {
            // store tag parameters as NAME, value
            h.put(name.toUpperCase(), value);
        }
    }
}
