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
import java.util.Collection;
import java.util.Iterator;

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

	/** Stores the singleton parser to be used */
	private static HTMLParser myParser = new JTidyHTMLParser();

    /**
     * This is a singleton class
     */
    //TODO make private 
    JTidyHTMLParser()
    {
        super();
    }


    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.parser.HTMLParser#getEmbeddedResourceURLs(byte[], java.net.URL)
     */
    public Iterator getEmbeddedResourceURLs(byte[] html, URL baseUrl, Collection urls)
        throws HTMLParseException
    {
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
		
		scanNodes(dom,urls, baseUrl);

		return urls.iterator();
	}

    /** 
	 * Scan nodes recursively, looking for embedded resources
	 * @param node - initial node
	 * @param urls - container for URLs
	 * @param baseUrl - used to create absolute URLs
     * 
     * @return new base URL
	 */
	private URL scanNodes(Node node, Collection urls, URL baseUrl) throws HTMLParseException
	{
		if ( node == null ) {
		  return baseUrl;
	    }

	    String name = node.getNodeName();

	    int type = node.getNodeType();

	    switch ( type ) {

	    case Node.DOCUMENT_NODE:
		  scanNodes(((Document)node).getDocumentElement(),urls,baseUrl);
		  break;

	    case Node.ELEMENT_NODE:
	   
		  NamedNodeMap attrs = node.getAttributes();
		  if (name.equalsIgnoreCase("base"))
		  {
		  	String tmp=getValue(attrs,"href");
		  	if (tmp!=null) try
            {
                baseUrl= new URL(baseUrl, tmp);
            }
            catch (MalformedURLException e)
            {
            	throw new HTMLParseException(e);
            }
		  	break;
		  }
		  
		  if (name.equalsIgnoreCase("img"))
		  {
		  	addURL(urls,getValue(attrs,"src"),baseUrl);
			break;
          }
          
		  if (name.equalsIgnoreCase("applet"))
		  {
		  	addURL(urls,getValue(attrs,"code"),baseUrl);
			  break;
			}
			if (name.equalsIgnoreCase("input"))
			{
				String src=getValue(attrs,"src");
				String typ=getValue(attrs,"type");
				if ((src!=null) &&(typ.equalsIgnoreCase("image")) ){ 
					addURL(urls,src,baseUrl);
				}
			  break;
			}
			if (name.equalsIgnoreCase("link"))
			{
				addURL(urls,getValue(attrs,"href"),baseUrl);
			  break;
			}
			String back=getValue(attrs,"background");
			if (back != null){
				addURL(urls,back,baseUrl);
				break;
			}

		  NodeList children = node.getChildNodes();
		  if ( children != null ) {
			 int len = children.getLength();
			 for ( int i = 0; i < len; i++ ) {
				baseUrl= scanNodes(children.item(i),urls,baseUrl);
			 }
		  }
		  break;

//	   case Node.TEXT_NODE:
//		  break;

	   }
       
       return baseUrl;

	}

    /*
     * Helper method to get an attribute value, if it exists
     * @param attrs list of attributs
     * @param attname attribute name
     * @return
     */
    private String getValue(NamedNodeMap attrs, String attname)
    {
    	String v=null;
    	Node n = attrs.getNamedItem(attname);
    	if (n != null) v=n.getNodeValue();
        return v;
    }

    /*
     * Helper method to create and add a URL, if non-null
     * @param urls - set
     * @param url - may be null
     * @param baseUrl
     */
    private void addURL(Collection urls, String url, URL baseUrl)
    {
    	if (url == null) return;
    	boolean b=false;
		try
		{
			b=urls.add(new URL(baseUrl, url));
		}
		catch(MalformedURLException mfue)
		{
			// Can't build the URL. May be a site error: return
			// the string.
			b=urls.add(url);
		}
		if (b) {
			log.debug("Added   "+url);
		} else { 
			log.debug("Skipped "+url);
		}
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
    
    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.parser.HTMLParser#getParserInstance()
     */
    public static HTMLParser getParserInstance()
    {
		return myParser;
    }
    
	public static boolean isParserReusable(){
		return true;
	}
}
