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

import java.util.Stack;

import org.htmlparser.tags.Bullet;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;
import org.htmlparser.util.ParserException;

/**
 * This scanner is created by BulletListScanner. It shares a stack to maintain the parent-child relationship
 * with BulletListScanner. The rules implemented are :<br>
 * [1] A &lt;ul&gt; can have &lt;li&gt; under it<br>
 * [2] A &lt;li&gt; can have &lt;ul&gt; under it<br>
 * [3] A &lt;li&gt; cannot have &lt;li&gt; under it<br> 
 * <p>
 * These rules are implemented easily through the shared stack. 
 */
public class BulletScanner extends CompositeTagScanner
{
    private static final String[] MATCH_STRING = { "LI" };
    private final static String ENDERS[] = { "BODY", "HTML" };
    private final static String END_TAG_ENDERS[] = { "UL" };
    private Stack ulli;

    public BulletScanner(Stack ulli)
    {
        this("", ulli);
    }

    public BulletScanner(String filter, Stack ulli)
    {
        super(filter, MATCH_STRING, ENDERS, END_TAG_ENDERS, false);
        this.ulli = ulli;
    }

    public Tag createTag(TagData tagData, CompositeTagData compositeTagData)
        throws ParserException
    {
        return new Bullet(tagData, compositeTagData);
    }

    public String[] getID()
    {
        return MATCH_STRING;
    }

    /**
     * This is the logic that decides when a bullet tag can be allowed
     */
    public boolean shouldCreateEndTagAndExit()
    {
        if (ulli.size() == 0)
            return false;
        CompositeTagScanner parentScanner = (CompositeTagScanner) ulli.peek();
        if (parentScanner == this)
        {
            ulli.pop();
            return true;
        }
        else
            return false;
    }

    public void beforeScanningStarts()
    {
        ulli.push(this);
    }

}
