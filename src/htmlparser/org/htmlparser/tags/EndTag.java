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

package org.htmlparser.tags;

import org.htmlparser.Node;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.visitors.NodeVisitor;
/**
 * EndTag can identify closing tags, like &lt;/A&gt;, &lt;/FORM&gt;, etc.
 */
public class EndTag extends Tag
{
    public final static String TYPE = "END_TAG";
    public final static int ENDTAG_BEFORE_PARSING_STATE = 0;
    public final static int ENDTAG_WAIT_FOR_SLASH_STATE = 1;
    public final static int ENDTAG_BEGIN_PARSING_STATE = 2;
    public final static int ENDTAG_FINISHED_PARSING_STATE = 3;

    /**
     * Constructor takes 3 arguments to construct an EndTag object.
     * @param nodeBegin Beginning position of the end tag
     * @param nodeEnd Ending position of the end tag
     * @param tagContents Text contents of the tag
     */
    public EndTag(TagData tagData)
    {
        super(tagData);
    }
    /**
     * Locate the end tag withing the input string, by parsing from the given position
     * @param input Input String
     * @param position Position to start parsing from
     */
    public static Node find(String input, int position)
    {
        int state = ENDTAG_BEFORE_PARSING_STATE;
        StringBuffer tagContents = new StringBuffer();
        int tagBegin = 0;
        int tagEnd = 0;
        int inputLen = input.length();
        char ch;
        int i;
        for (i = position;
            (i < inputLen && state != ENDTAG_FINISHED_PARSING_STATE);
            i++)
        {
            ch = input.charAt(i);
            if (ch == '>' && state == ENDTAG_BEGIN_PARSING_STATE)
            {
                state = ENDTAG_FINISHED_PARSING_STATE;
                tagEnd = i;
            }
            if (state == ENDTAG_BEGIN_PARSING_STATE)
            {
                tagContents.append(ch);
            }
            if (state == ENDTAG_WAIT_FOR_SLASH_STATE)
            {
                if (ch == '/')
                {
                    state = ENDTAG_BEGIN_PARSING_STATE;
                }
                else
                    return null;
            }

            if (ch == '<')
            {
                if (state == ENDTAG_BEFORE_PARSING_STATE)
                {
                    // Transition from State 0 to State 1 - Record data till > is encountered
                    tagBegin = i;
                    state = ENDTAG_WAIT_FOR_SLASH_STATE;
                }
                else if (state == ENDTAG_BEGIN_PARSING_STATE)
                {
                    state = ENDTAG_FINISHED_PARSING_STATE;
                    tagEnd = i;
                }
            }
            else if (state == ENDTAG_BEFORE_PARSING_STATE)
                // text before the end tag
                return (null);
        }
        // If parsing did not complete, it might be possible to accept
        if (state == ENDTAG_BEGIN_PARSING_STATE)
        {
            tagEnd = i;
            state = ENDTAG_FINISHED_PARSING_STATE;
        }
        if (state == ENDTAG_FINISHED_PARSING_STATE)
            return new EndTag(
                new TagData(tagBegin, tagEnd, tagContents.toString(), input));
        else
            return null;
    }
    public String toPlainTextString()
    {
        return "";
    }
    public String toHtml()
    {
        return "</" + getTagName() + ">";
    }
    public String toString()
    {
        return "EndTag : "
            + tagContents
            + "; begins at : "
            + elementBegin()
            + "; ends at : "
            + elementEnd();
    }

    public void accept(NodeVisitor visitor)
    {
        visitor.visitEndTag(this);
    }

    public String getType()
    {
        return TYPE;
    }

}
