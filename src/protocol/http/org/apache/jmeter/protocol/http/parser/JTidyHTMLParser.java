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

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * HtmlParser implementation using JTidy.
 */
class JTidyHTMLParser extends HTMLParser
{
    /** Used to store the Logger (used for debug and error messages). */
    transient private static Logger log = LoggingManager.getLoggerForClass();

    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.parser.HTMLParser#getEmbeddedResourceURLs(byte[], java.net.URL)
     */
    public Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl)
        throws HTMLParseException
    {
        LinkedHashSet uniqueURLs= new LinkedHashSet();
		Document dom = null;
		try
		{
			dom = (Document)getDOM(html);
		}
		catch(SAXException se)
		{
            throw new HTMLParseException(se);
		}
        
		// Now parse the DOM tree
		
		// TODO - check for <base> tag ??
        
		// look for images
		parseNodes(dom, "img", false, "src", uniqueURLs, baseUrl);
		// look for applets
        
		// This will only work with an Applet .class file.
		// Ideally, this should be upgraded to work with Objects (IE)
		// and archives (.jar and .zip) files as well.
        
		parseNodes(dom, "applet", false, "code", uniqueURLs, baseUrl);
		// look for input tags with image types
		parseNodes(dom, "input", true, "src", uniqueURLs, baseUrl);
		// look for background images
		parseNodes(dom, "body", false, "background", uniqueURLs, baseUrl);
        
		// look for table background images
		parseNodes(dom, "table", false, "background", uniqueURLs, baseUrl);

		//TODO look for TD, TR etc images

		return uniqueURLs.iterator();
	}


    /**
     * Parse the DOM tree looking for the specified HTML source tags,
     * and download the appropriate binary files matching these tags.
     *
     * @param html      the HTML document to parse
     * @param htmlTag   the HTML tag to parse for
     * @param type      indicates that we require 'type=image'
     * @param srcTag    the HTML tag that indicates the source URL
     * @param uniques   used to ensure that binary files are only downloaded
     *                  once
     * @param baseUrl   base URL
     * 
     * @param res       <code>SampleResult</code> to store sampling results
     */
    private static void parseNodes(Document html, String htmlTag, boolean type,
            String srcTag, Set uniques, URL baseUrl)
    {
        log.debug("Start : HTTPSamplerFull parseNodes");
        NodeList nodeList = html.getElementsByTagName(htmlTag);
        for(int i = 0; i < nodeList.getLength(); i++)
        {
            Node tempNode = nodeList.item(i);
            if(log.isDebugEnabled())
            {
                log.debug("'" + htmlTag + "' tag: " + tempNode);
            }

            // get the url of the Binary
            NamedNodeMap nnm = tempNode.getAttributes();
            Node namedItem = null;

            if(type)
            {
                // if type is set, we need 'type=image'
                namedItem = nnm.getNamedItem("type");
                if(namedItem == null)
                {
                    log.debug("namedItem 'null' - ignoring");
                    break;
                }
                String inputType = namedItem.getNodeValue();
                if(log.isDebugEnabled())
                {
                    log.debug("Input type - " + inputType);
                }
                if(inputType != null && inputType.equalsIgnoreCase("image"))
                {
                    // then we need to download the binary
                }
                else
                {
                    log.debug("type != 'image' - ignoring");
                    break;
                }
            }

            namedItem = nnm.getNamedItem(srcTag);
            if(namedItem == null)
            {
                continue;
            }
            String binUrlStr = namedItem.getNodeValue();
            try
            {
                uniques.add(new URL(baseUrl, binUrlStr));
            }
            catch(MalformedURLException mfue)
            {
                // Can't build the URL. May be a site error: return
                // the string.
                uniques.add(binUrlStr);
            }
        }
        log.debug("End   : HTTPSamplerFull parseNodes");
    }


    /**
     * Returns <code>tidy</code> as HTML parser.
     *
     * @return  a <code>tidy</code> HTML parser
     */
    private static Tidy getTidyParser()
    {
        log.debug("Start : getParser");
        Tidy tidy = new Tidy();
        tidy.setCharEncoding(org.w3c.tidy.Configuration.UTF8);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        if(log.isDebugEnabled())
        {
            log.debug("getParser : tidy parser created - " + tidy);
        }
        log.debug("End   : getParser");
        return tidy;
    }

    /**
     * Returns a node representing a whole xml given an xml document.
     *
     * @param text  an xml document (as a byte array)
     * @return      a node representing a whole xml
     *
     * @throws SAXException indicates an error parsing the xml document
     */
    private static Node getDOM(byte [] text) throws SAXException
    {
        log.debug("Start : getDOM");
        Node node = getTidyParser().parseDOM(new
          ByteArrayInputStream(text), null);
        if(log.isDebugEnabled())
        {
            log.debug("node : " + node);
        }
        log.debug("End   : getDOM");
        return node;
    }
    
    public static class Test extends TestCase
    {
        public Test() {
            super();
        }
        public void testParser() throws Exception {
            HTMLParserTest.testParser(new JTidyHTMLParser());
        }
    }
}
