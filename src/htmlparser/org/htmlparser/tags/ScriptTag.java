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

import org.htmlparser.tags.data.CompositeTagData;
import org.htmlparser.tags.data.TagData;

/**
 * A HTMLScriptTag represents a JavaScript node
 */
public class ScriptTag extends CompositeTag
{
    private java.lang.String language;
    private java.lang.String type;
    private String scriptCode;
    /**
     * The HTMLScriptTag is constructed by providing the beginning posn, ending posn
     * and the tag contents.
     * @param nodeBegin beginning position of the tag
     * @param nodeEnd ending position of the tag
     * @param tagContents The contents of the Script Tag (should be kept the same as that of the original Tag contents)
     * @param scriptCode The Javascript code b/w the tags
     * @param language The language parameter
     * @param type The type parameter
     * @param tagLine The current line being parsed, where the tag was found	 
     */
    public ScriptTag(TagData tagData, CompositeTagData compositeTagData)
    {
        super(tagData, compositeTagData);
        this.scriptCode = getChildrenHTML();
        this.language = getAttribute("LANGUAGE");
        this.type = getAttribute("TYPE");
    }

    public java.lang.String getLanguage()
    {
        return language;
    }

    public java.lang.String getScriptCode()
    {
        return scriptCode;
    }

    public java.lang.String getType()
    {
        return type;
    }
    /**
     * Set the language of the javascript tag
     * @param newLanguage java.lang.String
     */
    public void setLanguage(java.lang.String newLanguage)
    {
        language = newLanguage;
    }
    /**
     * Set the type of the javascript node
     * @param newType java.lang.String
     */
    public void setType(java.lang.String newType)
    {
        type = newType;
    }

    /**
     * Print the contents of the javascript node
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Script Node : \n");
        if (language != null && type != null)
            if (language.length() != 0 || type.length() != 0)
            {
                sb.append("Properties -->\n");
                if (language.length() != 0)
                    sb.append("[Language : " + language + "]\n");
                if (type != null && type.length() != 0)
                    sb.append("[Type : " + type + "]\n");
            }
        sb.append("\n");
        sb.append("Code\n");
        sb.append("****\n");
        sb.append(getScriptCode() + "\n");
        return sb.toString();
    }
}
