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
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * Parser class using JTidy to scan HTML documents fof images etc
 * 
 * @author TBA
 * @version $revision$ Last updated: $date$
 */
public class ParseJTidy
{
    /** Used to store the Logger (used for debug and error messages). */
    transient private static Logger log = LoggingManager.getLoggerForClass();

    /**
     * This is a singleton class
     */
    private ParseJTidy()
    {
        super();
    }

	protected static SampleResult parseForImages(SampleResult res,HTTPSampler sampler)
	{
		String displayName = res.getSampleLabel();
		Document html = null;
		URL baseUrl = null;
		try
		{
			baseUrl = sampler.getUrl();
			if(log.isDebugEnabled())
			{
				log.debug("baseUrl - " + baseUrl.toString());
			}
			html = (Document)getDOM(res.getResponseData());
		}
		catch(SAXException se)
		{
			log.error("Error parsing document - " + se);
			res.setResponseData(se.toString().getBytes());
			res.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
			res.setResponseMessage(HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
			res.setSuccessful(false);
			return res;
		}
		catch(MalformedURLException mfue)
		{
			log.error("Error creating URL '" + displayName + "'");
			log.error("MalformedURLException - " + mfue);
			res.setResponseData(mfue.toString().getBytes());
			res.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
			res.setResponseMessage(HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
			res.setSuccessful(false);
			return res;
		}
        
		// Now parse the DOM tree
		
		// TODO - check for <base> tag ??
        
		// This is used to ignore duplicated binary files.
		Set uniqueURLs = new HashSet();
        
		// look for images
		parseNodes(html, "img", false, "src", uniqueURLs, res,sampler,baseUrl);
		// look for applets
        
		// This will only work with an Applet .class file.
		// Ideally, this should be upgraded to work with Objects (IE)
		// and archives (.jar and .zip) files as well.
        
		parseNodes(html, "applet", false, "code", uniqueURLs, res,sampler,baseUrl);
		// look for input tags with image types
		parseNodes(html, "input", true, "src", uniqueURLs, res,sampler,baseUrl);
		// look for background images
		parseNodes(html, "body", false, "background", uniqueURLs, res,sampler,baseUrl);
        
		// look for table background images
		parseNodes(html, "table", false, "background", uniqueURLs, res,sampler,baseUrl);

		//TODO look for TD, TR etc images

		// Okay, we're all done now
		if(log.isDebugEnabled())
		{
			log.debug("Total time - " + res.getTime());
		}
		log.debug("End   : HTTPSamplerFull sample");
		return res;
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
            String srcTag, Set uniques, SampleResult res,HTTPSampler sampler,
            URL baseUrl)
    {
        log.debug("Start : HTTPSamplerFull parseNodes");
        NodeList nodeList = html.getElementsByTagName(htmlTag);
        boolean uniqueBinary;
        SampleResult binRes = null;
        for(int i = 0; i < nodeList.getLength(); i++)
        {
            uniqueBinary = true;
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

            binRes = new SampleResult();
            namedItem = nnm.getNamedItem(srcTag);
            if(namedItem == null)
            {
                continue;
            }
            String binUrlStr = namedItem.getNodeValue();
            // set the baseUrl and binUrl so that if error occurs
            // due to MalformedException then at least the values will be
            // visible to the user to aid correction
            binRes.setSampleLabel(baseUrl + "," + binUrlStr);
            // download the binary
            URL binUrl = null;
            try
            {
                binUrl = new URL(baseUrl, binUrlStr);
            }
            catch(MalformedURLException mfue)
            {
                log.error("Error creating URL '" + baseUrl +
                          " , " + binUrlStr + "'");
                log.error("MalformedURLException - " + mfue);
                binRes.setResponseData(mfue.toString().getBytes());
                binRes.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
                binRes.setResponseMessage(
                    HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
                binRes.setSuccessful(false);
                res.addSubResult(binRes);
                break;
            }
            if(log.isDebugEnabled())
            {
                log.debug("Binary url - " + binUrlStr);
                log.debug("Full Binary url - " + binUrl);
            }
            binRes.setSampleLabel(binUrl.toString());
            uniqueBinary = uniques.add(binUrl.toString());
            if (uniqueBinary)
            {
                // a browser should be smart enough to *not* download
                //   a binary file that it already has in its cache.
                try
                {
                    HTTPSamplerFull.loadBinary(binUrl, binRes,sampler);
                }
                catch(Exception ioe)
                {
                    log.error("Error reading from URL - " + ioe);
                    binRes.setResponseData(ioe.toString().getBytes());
                    binRes.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
                    binRes.setResponseMessage(
                        HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
                    binRes.setSuccessful(false);
                }
                log.debug("Adding result");
                res.addSubResult(binRes);
                res.setTime(res.getTime() + binRes.getTime());
            }
            else
            {
                if(log.isDebugEnabled())
                {
                    log.debug("Skipping duplicate - " + binUrl);
                }
            }
        }
        log.debug("End   : HTTPSamplerFull parseNodes");
    }


    /**
     * Returns <code>tidy</code> as HTML parser.
     *
     * @return  a <code>tidy</code> HTML parser
     */
    private static Tidy getParser()
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
        Node node = getParser().parseDOM(new
          ByteArrayInputStream(text), null);
        if(log.isDebugEnabled())
        {
            log.debug("node : " + node);
        }
        log.debug("End   : getDOM");
        return node;
    }

}
