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

package org.htmlparser.tags.data;

public class TagData
{
    private int tagBegin;
    private int tagEnd;
    private int startLine;
    private int endLine;
    private String tagContents;
    private String tagLine;
    private String urlBeingParsed;
    private boolean isXmlEndTag;

    public TagData(
        int tagBegin,
        int tagEnd,
        String tagContents,
        String tagLine)
    {
        this(tagBegin, tagEnd, 0, 0, tagContents, tagLine, "", false);
    }

    public TagData(
        int tagBegin,
        int tagEnd,
        String tagContents,
        String tagLine,
        String urlBeingParsed)
    {
        this(
            tagBegin,
            tagEnd,
            0,
            0,
            tagContents,
            tagLine,
            urlBeingParsed,
            false);
    }

    public TagData(
        int tagBegin,
        int tagEnd,
        int startLine,
        int endLine,
        String tagContents,
        String tagLine,
        String urlBeingParsed,
        boolean isXmlEndTag)
    {
        this.tagBegin = tagBegin;
        this.tagEnd = tagEnd;
        this.startLine = startLine;
        this.endLine = endLine;
        this.tagContents = tagContents;
        this.tagLine = tagLine;
        this.urlBeingParsed = urlBeingParsed;
        this.isXmlEndTag = isXmlEndTag;
    }

    public int getTagBegin()
    {
        return tagBegin;
    }

    public String getTagContents()
    {
        return tagContents;
    }

    public int getTagEnd()
    {
        return tagEnd;
    }

    public String getTagLine()
    {
        return tagLine;
    }

    public void setTagContents(String tagContents)
    {
        this.tagContents = tagContents;
    }

    public String getUrlBeingParsed()
    {
        return urlBeingParsed;
    }

    public void setUrlBeingParsed(String baseUrl)
    {
        this.urlBeingParsed = baseUrl;
    }

    public boolean isEmptyXmlTag()
    {
        return isXmlEndTag;
    }

    /**
     * Returns the line number where the tag starts in the HTML. At the moment this
     * will only be valid for tags created with the
     * <code>CompositeTagScanner</code> or a subclass of it.
     */
    public int getStartLine()
    {
        return startLine;
    }

    /**
     * Returns the line number where the tag ends in the HTML. At the moment this
     * will only be valid for tags created with the
     * <code>CompositeTagScanner</code> or a subclass of it.
     */
    public int getEndLine()
    {
        return endLine;
    }

}
