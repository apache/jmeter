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

import org.htmlparser.Node;
import org.htmlparser.NodeReader;
import org.htmlparser.StringNode;

public class StringParser
{
    private final static int BEFORE_PARSE_BEGINS_STATE = 0;
    private final static int PARSE_HAS_BEGUN_STATE = 1;
    private final static int PARSE_COMPLETED_STATE = 2;
    private final static int PARSE_IGNORE_STATE = 3;

    /**
     * Returns true if the text at <code>pos</code> in <code>line</code> should be scanned as a tag.
     * Basically an open angle followed by a known special character or a letter.
     * @param line The current line being parsed.
     * @param pos The position in the line to examine.
     * @return <code>true</code> if we think this is the start of a tag.
     */
    private boolean beginTag(String line, int pos)
    {
        char ch;
        boolean ret;

        ret = false;

        if (pos + 2 <= line.length())
            if ('<' == line.charAt(pos))
            {
                ch = line.charAt(pos + 1);
                // the order of these tests might be optimized for speed
                if ('/' == ch
                    || '%' == ch
                    || Character.isLetter(ch)
                    || '!' == ch)
                    ret = true;
            }

        return (ret);
    }

    /**
     * Locate the StringNode within the input string, by parsing from the given position
     * @param reader HTML reader to be provided so as to allow reading of next line
     * @param input Input String
     * @param position Position to start parsing from
     * @param balance_quotes If <code>true</code> enter ignoring state on
     * encountering quotes.
     */
    public Node find(
        NodeReader reader,
        String input,
        int position,
        boolean balance_quotes)
    {
        StringBuffer textBuffer = new StringBuffer();
        int state = BEFORE_PARSE_BEGINS_STATE;
        int textBegin = position;
        int textEnd = position;
        int inputLen = input.length();
        char ch;
        char ignore_ender = '\"';
        for (int i = position;
            (i < inputLen && state != PARSE_COMPLETED_STATE);
            i++)
        {
            ch = input.charAt(i);
            if (ch == '<' && state != PARSE_IGNORE_STATE)
            {
                if (beginTag(input, i))
                {
                    state = PARSE_COMPLETED_STATE;
                    textEnd = i - 1;
                }
            }
            if (balance_quotes && (ch == '\'' || ch == '"'))
            {
                if (state == PARSE_IGNORE_STATE)
                {
                    if (ch == ignore_ender)
                        state = PARSE_HAS_BEGUN_STATE;
                }
                else
                {
                    ignore_ender = ch;
                    state = PARSE_IGNORE_STATE;
                }
            }
            if (state == BEFORE_PARSE_BEGINS_STATE)
            {
                state = PARSE_HAS_BEGUN_STATE;
            }
            if (state == PARSE_HAS_BEGUN_STATE || state == PARSE_IGNORE_STATE)
            {
                textBuffer.append(input.charAt(i));
            }
            // Patch by Cedric Rosa
            if (state == BEFORE_PARSE_BEGINS_STATE && i == inputLen - 1)
                state = PARSE_HAS_BEGUN_STATE;
            if (state == PARSE_HAS_BEGUN_STATE && i == inputLen - 1)
            {
                do
                {
                    input = reader.getNextLine();
                    if (input != null && input.length() == 0)
                        textBuffer.append(Node.getLineSeparator());
                }
                while (input != null && input.length() == 0);

                if (input == null)
                {
                    textEnd = i;
                    state = PARSE_COMPLETED_STATE;

                }
                else
                {
                    textBuffer.append(Node.getLineSeparator());
                    inputLen = input.length();
                    i = -1;
                }

            }
        }
        return new StringNode(textBuffer, textBegin, textEnd);
    }
}
