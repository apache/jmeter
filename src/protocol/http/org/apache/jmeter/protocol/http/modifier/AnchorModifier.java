/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.protocol.http.modifier;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ResponseBasedModifier;
import org.apache.jmeter.protocol.http.parser.HtmlParser;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/************************************************************
 *  Title: Jakarta-JMeter Description: Copyright: Copyright (c) 2001 Company:
 *  Apache
 *
 *@author     Michael Stover
 *@created    $Date$
 *@version    1.0
 ***********************************************************/

public class AnchorModifier extends AbstractTestElement implements ResponseBasedModifier,
		Serializable
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");
	private static Random rand = new Random();

	/************************************************************
	 *  !ToDo (Constructor description)
	 ***********************************************************/
	public AnchorModifier()
	{
	}

	/************************************************************
	 *  Modifies an Entry object based on HTML response text.
	 *
	 *@param  entry   !ToDo (Parameter description)
	 *@param  result  !ToDo (Parameter description)
	 *@return         !ToDo (Return description)
	 ***********************************************************/
	public boolean modifyEntry(Sampler sam, SampleResult result)
	{
		HTTPSampler sampler = null;
		if(result == null || !(sam instanceof HTTPSampler))
		{
			return false;
		}
		else
		{
			sampler = (HTTPSampler)sam;
		}
		List potentialLinks = new ArrayList();
		String responseText = "";
		try {
			responseText = new String(result.getResponseData(),"8859_1");
		} catch(UnsupportedEncodingException e) {
		}
		Document html;
		try
		{
			int index = responseText.indexOf("<");
			if(index == -1)
			{
				index = 0;
			}
			html = (Document)HtmlParser.getDOM(responseText.substring(index));
		}
		catch (SAXException e)
		{
			return false;
		}
		addAnchorUrls(html, result, sampler, potentialLinks);
		addFormUrls(html,result,sampler,potentialLinks);
		if (potentialLinks.size() > 0)
		{
			HTTPSampler url = (HTTPSampler)potentialLinks.get(rand.nextInt(potentialLinks.size()));
			sampler.setDomain(url.getDomain());
			sampler.setPath(url.getPath());
			if(url.getMethod().equals(HTTPSampler.POST))
			{
				Iterator iter = sampler.getArguments().iterator();
				while(iter.hasNext())
				{
					Argument arg = (Argument)iter.next();
					modifyArgument(arg,url.getArguments());
				}
			}
			else
			{
				sampler.setArguments(url.getArguments());
				//config.parseArguments(url.getQueryString());
			}
			sampler.setProtocol(url.getProtocol());
			return true;
		}
		return false;
	}

	private void modifyArgument(Argument arg,Arguments args)
	{
		List possibleReplacements = new ArrayList();
		Iterator iter = args.iterator();
		Argument replacementArg;
		while (iter.hasNext())
		{
			replacementArg = (Argument)iter.next();
			try
			{
				if(HtmlParser.isArgumentMatched(replacementArg,arg))
				{
					possibleReplacements.add(replacementArg);
				}
			}
			catch (Exception ex) {
				log.error("",ex);
			}
		}
		if(possibleReplacements.size() > 0)
		{
			replacementArg = (Argument)possibleReplacements.get(rand.nextInt(possibleReplacements.size()));
			arg.setName(replacementArg.getName());
			arg.setValue(replacementArg.getValue());
			args.removeArgument(replacementArg);
		}
	}

	/************************************************************
	 *  !ToDo
	 *
	 *@param  config  !ToDo
	 ***********************************************************/
	public void addConfigElement(ConfigElement config)
	{
	}

	private void addFormUrls(Document html,SampleResult result,HTTPSampler config,
			List potentialLinks)
	{
		NodeList rootList = html.getChildNodes();
		List urls = new LinkedList();
		for(int x = 0;x < rootList.getLength();x++)
		{
			urls.addAll(HtmlParser.createURLFromForm(rootList.item(x),
					(HTTPSampler)result.getSamplerData()));
		}
		Iterator iter = urls.iterator();
		while (iter.hasNext())
		{
			HTTPSampler newUrl = (HTTPSampler)iter.next();
			try
			{
				newUrl.setMethod(HTTPSampler.POST);
				if(HtmlParser.isAnchorMatched(newUrl,config))
				{
					potentialLinks.add(newUrl);
				}
			}
			catch (org.apache.oro.text.regex.MalformedPatternException e)
			{
				log.error("Bad pattern",e);
			}
		}
	}

	private void addAnchorUrls(Document html, SampleResult result, HTTPSampler config, List potentialLinks)
	{
		NodeList nodeList = html.getElementsByTagName("a");
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			Node tempNode = nodeList.item(i);
			NamedNodeMap nnm = tempNode.getAttributes();
			Node namedItem = nnm.getNamedItem("href");
			if(namedItem == null)
			{
				continue;
			}
			String hrefStr = namedItem.getNodeValue();
			try
			{
				HTTPSampler newUrl = HtmlParser.createUrlFromAnchor(hrefStr, (HTTPSampler)result.getSamplerData());
				newUrl.setMethod(HTTPSampler.GET);
				if (HtmlParser.isAnchorMatched(newUrl, config))
				{
					potentialLinks.add(newUrl);
				}
			}
			catch (MalformedURLException e)
			{
			}
			catch (org.apache.oro.text.regex.MalformedPatternException e)
			{
				log.error("Bad pattern",e);
			}
		}
	}
}
