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

package org.htmlparser;
public class RemarkNodeParser
{
    public final static int REMARK_NODE_BEFORE_PARSING_STATE = 0;
    public final static int REMARK_NODE_OPENING_ANGLE_BRACKET_STATE = 1;
    public final static int REMARK_NODE_EXCLAMATION_RECEIVED_STATE = 2;
    public final static int REMARK_NODE_FIRST_DASH_RECEIVED_STATE = 3;
    public final static int REMARK_NODE_ACCEPTING_STATE = 4;
    public final static int REMARK_NODE_CLOSING_FIRST_DASH_RECEIVED_STATE = 5;
    public final static int REMARK_NODE_CLOSING_SECOND_DASH_RECEIVED_STATE = 6;
    public final static int REMARK_NODE_ACCEPTED_STATE = 7;
    public final static int REMARK_NODE_ILLEGAL_STATE = 8;
    public final static int REMARK_NODE_FINISHED_PARSING_STATE = 2;

    /**
     * Locate the remark tag withing the input string, by parsing from the given position
     * @param reader HTML reader to be provided so as to allow reading of next line
     * @param input Input String
     * @param position Position to start parsing from
     */
    public RemarkNode find(NodeReader reader, String input, int position)
    {
        int state = REMARK_NODE_BEFORE_PARSING_STATE;
        StringBuffer tagContents = new StringBuffer();
        int tagBegin = 0;
        int tagEnd = 0;
        int i = position;
        int inputLen = input.length();
        char ch, prevChar = ' ';
        while (i < inputLen && state < REMARK_NODE_ACCEPTED_STATE)
        {
            ch = input.charAt(i);
            if (state == REMARK_NODE_CLOSING_SECOND_DASH_RECEIVED_STATE)
            {
                if (ch == '>')
                {
                    state = REMARK_NODE_ACCEPTED_STATE;
                    tagEnd = i;
                }
                else if (ch == '-')
                {
                    tagContents.append(prevChar);
                }
                else
                {
                    // Rollback last 2 characters (assumed same)
                    state = REMARK_NODE_ACCEPTING_STATE;
                    tagContents.append(prevChar);
                    tagContents.append(prevChar);
                }

            }

            if (state == REMARK_NODE_CLOSING_FIRST_DASH_RECEIVED_STATE)
            {
                if (ch == '-')
                {
                    state = REMARK_NODE_CLOSING_SECOND_DASH_RECEIVED_STATE;
                }
                else
                {
                    // Rollback
                    state = REMARK_NODE_ACCEPTING_STATE;
                    tagContents.append(prevChar);
                }
            }
            if (state == REMARK_NODE_ACCEPTING_STATE)
            {
                if (ch == '-')
                {
                    state = REMARK_NODE_CLOSING_FIRST_DASH_RECEIVED_STATE;
                } /*else
                				if (ch == '<')
                				{
                					state=REMARK_NODE_ILLEGAL_STATE;
                				} */
            }
            if (state == REMARK_NODE_ACCEPTING_STATE)
            {
                // We can append contents now		
                tagContents.append(ch);
            }

            if (state == REMARK_NODE_FIRST_DASH_RECEIVED_STATE)
            {
                if (ch == '-')
                {
                    state = REMARK_NODE_ACCEPTING_STATE;
                    // Do a lookahead and see if the next char is >
                    if (input.length() > i + 1 && input.charAt(i + 1) == '>')
                    {
                        state = REMARK_NODE_ACCEPTED_STATE;
                        tagEnd = i + 1;
                    }
                }
                else
                    state = REMARK_NODE_ILLEGAL_STATE;
            }
            if (state == REMARK_NODE_EXCLAMATION_RECEIVED_STATE)
            {
                if (ch == '-')
                    state = REMARK_NODE_FIRST_DASH_RECEIVED_STATE;
                else if (ch == '>')
                {
                    state = REMARK_NODE_ACCEPTED_STATE;
                    tagEnd = i;
                }
                else
                    state = REMARK_NODE_ILLEGAL_STATE;
            }
            if (state == REMARK_NODE_OPENING_ANGLE_BRACKET_STATE)
            {
                if (ch == '!')
                    state = REMARK_NODE_EXCLAMATION_RECEIVED_STATE;
                else
                    state = REMARK_NODE_ILLEGAL_STATE;
                // This is not a remark tag
            }
            if (state == REMARK_NODE_BEFORE_PARSING_STATE)
            {
                if (ch == '<')
                {
                    // Transition from State 0 to State 1 - Record data till > is encountered
                    tagBegin = i;
                    state = REMARK_NODE_OPENING_ANGLE_BRACKET_STATE;
                }
                else if (ch != ' ')
                {
                    // Its not a space, hence this is probably a string node, not a remark node
                    state = REMARK_NODE_ILLEGAL_STATE;
                }
            }
            //			if (state > REMARK_NODE_OPENING_ANGLE_BRACKET_STATE && state < REMARK_NODE_ACCEPTED_STATE && i == input.length() - 1)
            if (state >= REMARK_NODE_ACCEPTING_STATE
                && state < REMARK_NODE_ACCEPTED_STATE
                && i == input.length() - 1)
            {
                // We need to continue parsing to the next line
                //input = reader.getNextLine();
                tagContents.append(Node.getLineSeparator());
                do
                {
                    input = reader.getNextLine();
                }
                while (input != null && input.length() == 0);
                if (input != null)
                    inputLen = input.length();
                else
                    inputLen = -1;
                i = -1;
            }
            if (state == REMARK_NODE_ILLEGAL_STATE)
            {
                return null;
            }
            i++;
            prevChar = ch;
        }
        if (state == REMARK_NODE_ACCEPTED_STATE)
            return new RemarkNode(tagBegin, tagEnd, tagContents.toString());
        else
            return null;
    }
}
