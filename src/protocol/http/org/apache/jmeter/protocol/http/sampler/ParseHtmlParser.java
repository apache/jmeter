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

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import org.htmlparser.*;
import org.htmlparser.util.*;
import org.htmlparser.scanners.*;
import org.htmlparser.tags.*;

/**
 * Parser class using HtmlParser to scan HTML documents fof images etc
 * 
 * @author TBA
 * @version $revision$ Last updated: $date$
 */
public class ParseHtmlParser
{
	/** Used to store the Logger (used for debug and error messages). */
   transient private static Logger log = LoggingManager.getLoggerForClass();

    /**
     * This is a singleton class
     */
    private ParseHtmlParser()
    {
        super();
    }

	/**
	 * This method is called by HTTPSampler to get the images
	 * from the HTML and retrieve it from the server. It is
	 * the entry point for parsing the HTML.
	 * 
     * @param res - the current sample result
     * @param sampler - the HTTP sampler
     * @return the sample result, with possible additional sub results
	 */
	protected static SampleResult parseForImages(SampleResult res,HTTPSampler sampler)
	{
		Parser HtmlParser = null;
		URL baseUrl = null;
		String displayName = res.getSampleLabel();
		try {

			String contents = new String(res.getResponseData());
			StringReader reader = new StringReader(contents);
			NodeReader nreader = new NodeReader(reader,contents.length());
			HtmlParser = new Parser(nreader,new DefaultParserFeedback ());
			addTagListeners(HtmlParser);
		} catch (Exception e){
			log.error("IOException: had problems reading the InputStream");
		}

		try
		{
			baseUrl = sampler.getUrl();
			if(log.isDebugEnabled())
			{
				log.error("baseUrl - " + baseUrl.toString());
			}
		}
		catch(MalformedURLException mfue)
		{
			log.error("Error creating URL '" + displayName + "'");
			log.error("MalformedURLException - " + mfue);
			res.setResponseData(mfue.toString().getBytes());
			res.setResponseCode(HTTPSampler.NON_HTTP_RESPONSE_CODE);
			res.setResponseMessage(HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
			res.setSuccessful(false);
			mfue.printStackTrace();
			return res;
		}
		// System.out.println("got the baseURL");
		// Now parse the DOM tree
        
		// This is used to ignore duplicated binary files.
		Set uniqueURLs = new HashSet();
        
		// look for applets
        
		// This will only work with an Applet .class file.
		// Ideally, this should be upgraded to work with Objects (IE)
		//	and archives (.jar and .zip) files as well.
        
		parseNodes(HtmlParser, uniqueURLs, res,sampler, baseUrl);
        
		// Okay, we're all done now
		if(log.isDebugEnabled())
		{
			log.debug("Total time - " + res.getTime());
		}
		log.debug("End   : NewHTTPSamplerFull sample");
		return res;
	}

    /**
     * User HTMLParser to get the relevant nodes. This simplifies
     * the process and should provide significant performance
     * improvements.
     *
     * @param parser	HTMLParser
     * @param uniques	used to ensure that binary files are only downloaded once
     * @param res		<code>SampleResult</code> to store sampling results
     */
    private static void parseNodes(Parser parser, Set uniques,
    	SampleResult res,HTTPSampler sampler, URL baseUrl)
    {
		// Okay, we're all done now
		if(log.isDebugEnabled())
		{
			log.debug("Start	: NewHTTPSamplerFull");
		}
        boolean uniqueBinary;
        SampleResult binRes = null;

		try {
			// we start to iterate through the elements
			for(NodeIterator e = parser.elements(); e.hasMoreNodes();)
			{
				uniqueBinary = true;
				Node node = e.nextNode();
				String binUrlStr = null;

				// first we check to see if body tag has a
				// background set and we set the NodeIterator
				// to the child elements inside the body
				if (node instanceof BodyTag){
					BodyTag body = (BodyTag)node;
					binUrlStr = body.getAttribute("background");
					// if the body tag exists, we get the elements
					// within the body tag. if we don't we won't
					// see the body of the page. The only catch
					// with this is if there are images after the
					// closing body tag, it won't get parsed. If
					// someone puts it outside the body tag, it
					// is probably a mistake. Plus it's bad to
					// have important content after the closing
					// body tag. Peter Lin 10-9-03
					e = body.elements();
				} else if (node instanceof ImageTag){
					ImageTag image = (ImageTag)node;
					binUrlStr = image.getImageURL();
				} else if (node instanceof AppletTag){
					AppletTag applet = (AppletTag)node;
					binUrlStr = applet.getAppletClass();
				} else if (node instanceof InputTag){
					InputTag input = (InputTag)node;
					// we check the input tag type for image
					String strType = input.getAttribute("type");
					if(strType != null && strType.equalsIgnoreCase("image"))
					{
						// then we need to download the binary
						binUrlStr = input.getAttribute("src");
					}
				}

				binRes = new SampleResult();
				if(binUrlStr == null)
				{
					continue;
				}

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
					binRes.setResponseMessage(HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
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
						binRes.setResponseMessage(HTTPSampler.NON_HTTP_RESPONSE_MESSAGE);
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
			log.debug("End   : NewHTTPSamplerFull parseNodes");
		}
		catch (ParserException e){
		}
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
        // add ImageTag scanner
		LinkScanner linkScanner = new LinkScanner(LinkTag.LINK_TAG_FILTER);
		// parser.addScanner(linkScanner);
		parser.addScanner(linkScanner.createImageScanner(ImageTag.IMAGE_TAG_FILTER));
		// add input tag scanner
		parser.addScanner(new InputTagScanner());
		// add applet tag scanner
		parser.addScanner(new AppletScanner());	
    }

}

