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

import java.io.FileInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.parser.HtmlParser;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.io.TextFile;
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

public class AnchorModifier extends AbstractTestElement implements PreProcessor, Serializable
{
    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.http");
    private static Random rand = new Random();

    /************************************************************
     *  !ToDo (Constructor description)
     ***********************************************************/
    public AnchorModifier()
    {}

    /************************************************************
     *  Modifies an Entry object based on HTML response text.
     *
     *@param  entry   !ToDo (Parameter description)
     *@param  result  !ToDo (Parameter description)
     *@return         !ToDo (Return description)
     ***********************************************************/
    public void process()
    {
        SampleResult result = JMeterContextService.getContext().getPreviousResult();
        Sampler sam = JMeterContextService.getContext().getCurrentSampler();
        HTTPSampler sampler = null;
        if (result == null || !(sam instanceof HTTPSampler))
        {
            return;
        }
        else
        {
            sampler = (HTTPSampler) sam;
        }
        List potentialLinks = new ArrayList();
        String responseText = "";
        try
        {
            responseText = new String(result.getResponseData(), "8859_1");
        }
        catch (UnsupportedEncodingException e)
        {}
        Document html;
        try
        {
            int index = responseText.indexOf("<");
            if (index == -1)
            {
                index = 0;
            }
            html = (Document) HtmlParser.getDOM(responseText.substring(index));
        }
        catch (SAXException e)
        {
            return;
        }
        addAnchorUrls(html, result, sampler, potentialLinks);
        addFormUrls(html, result, sampler, potentialLinks);
        if (potentialLinks.size() > 0)
        {
            HTTPSampler url = (HTTPSampler) potentialLinks.get(rand.nextInt(potentialLinks.size()));
            sampler.setDomain(url.getDomain());
            sampler.setPath(url.getPath());
            if (url.getMethod().equals(HTTPSampler.POST))
            {
                PropertyIterator iter = sampler.getArguments().iterator();
                while (iter.hasNext())
                {
                    Argument arg = (Argument) iter.next().getObjectValue();
                    modifyArgument(arg, url.getArguments());
                }
            }
            else
            {
                sampler.setArguments(url.getArguments());
                //config.parseArguments(url.getQueryString());
            }
            sampler.setProtocol(url.getProtocol());
            return;
        }
        return;
    }

    private void modifyArgument(Argument arg, Arguments args)
    {
        log.debug("Modifying argument: " + arg);
        List possibleReplacements = new ArrayList();
        PropertyIterator iter = args.iterator();
        Argument replacementArg;
        while (iter.hasNext())
        {
            replacementArg = (Argument) iter.next().getObjectValue();
            try
            {
                if (HtmlParser.isArgumentMatched(replacementArg, arg))
                {
                    possibleReplacements.add(replacementArg);
                }
            }
            catch (Exception ex)
            {
                log.error("", ex);
            }
        }
        if (possibleReplacements.size() > 0)
        {
            replacementArg = (Argument) possibleReplacements.get(rand.nextInt(possibleReplacements.size()));
            arg.setName(replacementArg.getName());
            arg.setValue(replacementArg.getValue());
            log.debug("Just set argument to values: " + arg.getName() + " = " + arg.getValue());
            args.removeArgument(replacementArg);
        }
    }

    /************************************************************
     *  !ToDo
     *
     *@param  config  !ToDo
     ***********************************************************/
    public void addConfigElement(ConfigElement config)
    {}

    private void addFormUrls(Document html, SampleResult result, HTTPSampler config, List potentialLinks)
    {
        NodeList rootList = html.getChildNodes();
        List urls = new LinkedList();
        for (int x = 0; x < rootList.getLength(); x++)
        {
            urls.addAll(HtmlParser.createURLFromForm(rootList.item(x), (HTTPSampler) JMeterContextService.getContext().getPreviousSampler()));
        }
        Iterator iter = urls.iterator();
        while (iter.hasNext())
        {
            HTTPSampler newUrl = (HTTPSampler) iter.next();
            try
            {
                newUrl.setMethod(HTTPSampler.POST);
                if (HtmlParser.isAnchorMatched(newUrl, config))
                {
                    potentialLinks.add(newUrl);
                }
            }
            catch (org.apache.oro.text.regex.MalformedPatternException e)
            {
                log.error("Bad pattern", e);
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
            if (namedItem == null)
            {
                continue;
            }
            String hrefStr = namedItem.getNodeValue();
            try
            {
                HTTPSampler newUrl = HtmlParser.createUrlFromAnchor(hrefStr, (HTTPSampler) JMeterContextService.getContext().getPreviousSampler());
                newUrl.setMethod(HTTPSampler.GET);
                log.debug("possible match: " + newUrl);
                if (HtmlParser.isAnchorMatched(newUrl, config))
                {
                    log.debug("Is a match! " + newUrl);
                    potentialLinks.add(newUrl);
                }
            }
            catch (MalformedURLException e)
            {}
            catch (org.apache.oro.text.regex.MalformedPatternException e)
            {
                log.error("Bad pattern", e);
            }
        }
    }

    public static class Test extends TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testModifySampler() throws Exception
        {
            HTTPSampler config = (HTTPSampler) SaveService.loadSubTree(new FileInputStream(System.getProperty("user.dir") + "/testfiles/load_bug_list.jmx")).getArray()[0];
            config.setRunningVersion(true);
            SampleResult result = new SampleResult();
            HTTPSampler context = (HTTPSampler) SaveService.loadSubTree(new FileInputStream(System.getProperty("user.dir") + "/testfiles/Load_JMeter_Page.jmx")).getArray()[0];
            JMeterContextService.getContext().setCurrentSampler(context);
            JMeterContextService.getContext().setCurrentSampler(config);
            result.setResponseData(new TextFile(System.getProperty("user.dir") + "/testfiles/jmeter_home_page.html").getText().getBytes());
            result.setSampleLabel(context.toString());
            result.setSamplerData(context.toString());
            JMeterContextService.getContext().setPreviousResult(result);
            AnchorModifier modifier = new AnchorModifier();
            modifier.process();
            assertEquals(
                "http://nagoya.apache.org/bugzilla/buglist.cgi?bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&email1=&emailtype1=substring&emailassigned_to1=1&email2=&emailtype2=substring&emailreporter2=1&bugidtype=include&bug_id=&changedin=&votes=&chfieldfrom=&chfieldto=Now&chfieldvalue=&product=JMeter&short_desc=&short_desc_type=substring&long_desc=&long_desc_type=substring&bug_file_loc=&bug_file_loc_type=substring&keywords=&keywords_type=anywords&field0-0-0=noop&type0-0-0=noop&value0-0-0=&cmdtype=doit&order=Reuse+same+sort+as+last+time",
                config.toString());
            config.recoverRunningVersion();
            assertEquals("http://nagoya.apache.org/bugzilla/buglist.cgi?bug_status=.*&bug_status=.*&bug_status=.*&email1=&emailtype1=substring&emailassigned_to1=1&email2=&emailtype2=substring&emailreporter2=1&bugidtype=include&bug_id=&changedin=&votes=&chfieldfrom=&chfieldto=Now&chfieldvalue=&product=JMeter&short_desc=&short_desc_type=substring&long_desc=&long_desc_type=substring&bug_file_loc=&bug_file_loc_type=substring&keywords=&keywords_type=anywords&field0-0-0=noop&type0-0-0=noop&value0-0-0=&cmdtype=doit&order=Reuse+same+sort+as+last+time",
            config.toString());
        }

    }
}
