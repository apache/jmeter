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
 * @author TBA
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 * @version $Id$
 */
package org.apache.jmeter.protocol.http.parser;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import org.htmlparser.*;
import org.htmlparser.util.*;
import org.htmlparser.scanners.*;
import org.htmlparser.tags.*;

/**
 * HtmlParser implementation using SourceForge's HtmlParser.
 */
class HtmlParserHTMLParser extends HTMLParser
{
    /** Used to store the Logger (used for debug and error messages). */
    transient private static Logger log= LoggingManager.getLoggerForClass();

	protected HtmlParserHTMLParser(){
		super();
	}

	protected boolean isReusable()
	{
		return true;
	}

    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.parser.HtmlParser#getEmbeddedResourceURLs(byte[], java.net.URL)
     */
    public Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl, URLCollection urls)
        throws HTMLParseException
    {
        Parser htmlParser= null;
        try
        {
            String contents= new String(html);
            StringReader reader= new StringReader(contents);
            NodeReader nreader= new NodeReader(reader, contents.length());
            htmlParser= new Parser(nreader, new DefaultParserFeedback());
            addTagListeners(htmlParser);
        }
        catch (Exception e)
        {
            throw new HTMLParseException(e);
        }

        // Now parse the DOM tree

        // look for applets

        // This will only work with an Applet .class file.
        // Ideally, this should be upgraded to work with Objects (IE)
        //	and archives (.jar and .zip) files as well.

        try
        {
            // we start to iterate through the elements
            for (NodeIterator e= htmlParser.elements(); e.hasMoreNodes();)
            {
                Node node= e.nextNode();
                String binUrlStr= null;

                // first we check to see if body tag has a
                // background set and we set the NodeIterator
                // to the child elements inside the body
                if (node instanceof BodyTag)
                {
                    BodyTag body= (BodyTag)node;
                    binUrlStr= body.getAttribute("background");
                    // if the body tag exists, we get the elements
                    // within the body tag. if we don't we won't
                    // see the body of the page. The only catch
                    // with this is if there are images after the
                    // closing body tag, it won't get parsed. If
                    // someone puts it outside the body tag, it
                    // is probably a mistake. Plus it's bad to
                    // have important content after the closing
                    // body tag. Peter Lin 10-9-03
                    e= body.elements();
                }
                else if (node instanceof BaseHrefTag)
                {
                    BaseHrefTag baseHref= (BaseHrefTag)node;
                    try
                    {
                        baseUrl= new URL(baseUrl, baseHref.getBaseUrl()+"/");
                    }
                    catch (MalformedURLException e1)
                    {
                        throw new HTMLParseException(e1);
                    }
                }
                else if (node instanceof ImageTag)
                {
                    ImageTag image= (ImageTag)node;
                    binUrlStr= image.getImageURL();
                }
                else if (node instanceof AppletTag)
                {
                    AppletTag applet= (AppletTag)node;
                    binUrlStr= applet.getAppletClass();
                }
                else if (node instanceof InputTag)
                {
                    InputTag input= (InputTag)node;
                    // we check the input tag type for image
                    String strType= input.getAttribute("type");
                    if (strType != null && strType.equalsIgnoreCase("image"))
                    {
                        // then we need to download the binary
                        binUrlStr= input.getAttribute("src");
                    }
				} else if (node instanceof LinkTag){
					LinkTag link = (LinkTag)node;
					if (link.getChild(0) instanceof ImageTag){
						ImageTag img = (ImageTag)link.getChild(0);
						binUrlStr = img.getImageURL();
					}
				}

                if (binUrlStr == null)
                {
                    continue;
                }

                urls.addURL(binUrlStr,baseUrl);
            }
            log.debug("End   : parseNodes");
        }
        catch (ParserException e)
        {
            throw new HTMLParseException(e);
        }

        return urls.iterator();
    }

    /**
     * Returns a node representing a whole xml given an xml document.
     *
     * @param text	an xml document
     * @return	a node representing a whole xml
     *
     * @throws SAXException indicates an error parsing the xml document
     */
    private static void addTagListeners(Parser parser)
    {
        log.debug("Start : addTagListeners");
        // add body tag scanner
        parser.addScanner(new BodyScanner());
        // add BaseHRefTag scanner
        parser.addScanner(new BaseHrefScanner());
        // add ImageTag and BaseHrefTag scanners
        LinkScanner linkScanner= new LinkScanner(LinkTag.LINK_TAG_FILTER);
        // parser.addScanner(linkScanner);
        parser.addScanner(
            linkScanner.createImageScanner(ImageTag.IMAGE_TAG_FILTER));
        parser.addScanner(
            linkScanner.createBaseHREFScanner("-b"));
                            // Taken from org.htmlparser.Parser
        // add input tag scanner
        parser.addScanner(new InputTagScanner());
        // add applet tag scanner
        parser.addScanner(new AppletScanner());
    }
}
