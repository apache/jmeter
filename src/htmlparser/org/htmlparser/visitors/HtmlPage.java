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

package org.htmlparser.visitors;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.RemarkNode;
import org.htmlparser.StringNode;
import org.htmlparser.scanners.TableScanner;
import org.htmlparser.tags.EndTag;
import org.htmlparser.tags.TableTag;
import org.htmlparser.tags.Tag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

public class HtmlPage extends NodeVisitor
{
    private String title;
    private NodeList nodesInBody;
    private NodeList tables;
    private boolean bodyTagBegin;

    public HtmlPage(Parser parser)
    {
        super(false);
        parser.registerScanners();
        parser.addScanner(new TableScanner(parser));
        nodesInBody = new NodeList();
        tables = new NodeList();
        bodyTagBegin = false;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void visitTag(Tag tag)
    {
        addTagToBodyIfApplicable(tag);

        if (isTable(tag))
        {
            tables.add(tag);
        }
        else
        {
            if (isBodyTag(tag))
                bodyTagBegin = true;
        }
    }

    private boolean isTitle(Tag tag)
    {
        return tag instanceof TitleTag;
    }

    private boolean isTable(Tag tag)
    {
        return tag instanceof TableTag;
    }

    private void addTagToBodyIfApplicable(Node node)
    {
        if (bodyTagBegin)
            nodesInBody.add(node);
    }

    public void visitEndTag(EndTag endTag)
    {
        if (isBodyTag(endTag))
            bodyTagBegin = false;
        addTagToBodyIfApplicable(endTag);
    }

    public void visitRemarkNode(RemarkNode remarkNode)
    {
        addTagToBodyIfApplicable(remarkNode);
    }

    public void visitStringNode(StringNode stringNode)
    {
        addTagToBodyIfApplicable(stringNode);
    }

    private boolean isBodyTag(Tag tag)
    {
        return tag.getTagName().equals("BODY");
    }

    public NodeList getBody()
    {
        return nodesInBody;
    }

    public TableTag[] getTables()
    {
        TableTag[] tableArr = new TableTag[tables.size()];
        for (int i = 0; i < tables.size(); i++)
            tableArr[i] = (TableTag) tables.elementAt(i);
        return tableArr;
    }

    public void visitTitleTag(TitleTag titleTag)
    {
        title = titleTag.getTitle();
    }

}
