// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

package org.apache.jmeter.protocol.http.modifier;

import java.io.FileInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.parser.HtmlParsingUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.io.TextFile;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author     Michael Stover
 * @version    $Revision$
 */
public class AnchorModifier
    extends AbstractTestElement
    implements PreProcessor, Serializable
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    private static Random rand = new Random();

    public AnchorModifier()
    {}

    /**
     * Modifies an Entry object based on HTML response text.
     */
    public void process()
    {
    	JMeterContext context = getThreadContext();
        Sampler sam = context.getCurrentSampler();
        SampleResult res = context.getPreviousResult();
        HTTPSamplerBase sampler = null;
        HTTPSampleResult result = null;
        if (res == null 
            || !(sam instanceof HTTPSamplerBase)
            || !(res instanceof HTTPSampleResult))
        {
            log.info("Can't apply HTML Link Parser when the previous"
                     +" sampler run is not an HTTP Request.");
            return;
        }
        else
        {
            sampler = (HTTPSamplerBase) sam;
            result = (HTTPSampleResult) res;
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
            html = (Document) HtmlParsingUtils.getDOM(responseText.substring(index));
        }
        catch (SAXException e)
        {
            return;
        }
        addAnchorUrls(html, result, sampler, potentialLinks);
        addFormUrls(html, result, sampler, potentialLinks);
        if (potentialLinks.size() > 0)
        {
            HTTPSamplerBase url =
                (HTTPSamplerBase) potentialLinks.get(
                    rand.nextInt(potentialLinks.size()));
            sampler.setDomain(url.getDomain());
            sampler.setPath(url.getPath());
            if (url.getMethod().equals(HTTPSamplerBase.POST))
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
                if (HtmlParsingUtils.isArgumentMatched(replacementArg, arg))
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
            replacementArg =
                (Argument) possibleReplacements.get(
                    rand.nextInt(possibleReplacements.size()));
            arg.setName(replacementArg.getName());
            arg.setValue(replacementArg.getValue());
            log.debug(
                "Just set argument to values: "
                    + arg.getName()
                    + " = "
                    + arg.getValue());
            args.removeArgument(replacementArg);
        }
    }

    public void addConfigElement(ConfigElement config)
    {}

    private void addFormUrls(
        Document html,
        HTTPSampleResult result,
        HTTPSamplerBase config,
        List potentialLinks)
    {
        NodeList rootList = html.getChildNodes();
        List urls = new LinkedList();
        for (int x = 0; x < rootList.getLength(); x++)
        {
            urls.addAll(
                HtmlParsingUtils.createURLFromForm(
                    rootList.item(x),
                    result.getURL()));
        }
        Iterator iter = urls.iterator();
        while (iter.hasNext())
        {
            HTTPSamplerBase newUrl = (HTTPSamplerBase) iter.next();
            try
            {
                newUrl.setMethod(HTTPSamplerBase.POST);
                if (HtmlParsingUtils.isAnchorMatched(newUrl, config))
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

    private void addAnchorUrls(
        Document html,
        HTTPSampleResult result,
        HTTPSamplerBase config,
        List potentialLinks)
    {
    	String base="";
    	NodeList baseList = html.getElementsByTagName("base");
    	if (baseList.getLength()>0){
    		base=baseList.item(0).getAttributes().getNamedItem("href").getNodeValue();
    	}
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
                HTTPSamplerBase newUrl =
                    HtmlParsingUtils.createUrlFromAnchor(
                        hrefStr, new URL(result.getURL(),base));
                newUrl.setMethod(HTTPSamplerBase.GET);
                log.debug("possible match: " + newUrl);
                if (HtmlParsingUtils.isAnchorMatched(newUrl, config))
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

    public static class Test extends JMeterTestCase
    {
        public Test(String name)
        {
            super(name);
        }
        private JMeterContext jmctx = null;
        
        public void setUp()
    	{
        	jmctx = JMeterContextService.getContext();
        }

        public void testProcessingHTMLFile(String HTMLFileName) throws Exception
        {
            HTTPSamplerBase config =
                (HTTPSamplerBase) SaveService
                    .loadSubTree(
                        new FileInputStream(
                            System.getProperty("user.dir")
                                + "/testfiles/load_bug_list.jmx"))
                    .getArray()[0];
            config.setRunningVersion(true);
            HTTPSampleResult result = new HTTPSampleResult();
            HTTPSamplerBase context =
                (HTTPSamplerBase) SaveService
                    .loadSubTree(
                        new FileInputStream(
                            System.getProperty("user.dir")
                                + "/testfiles/Load_JMeter_Page.jmx"))
                    .getArray()[0];
            jmctx.setCurrentSampler(context);
            jmctx.setCurrentSampler(config);
            result.setResponseData(
                new TextFile(
                    System.getProperty("user.dir")
                        + HTMLFileName)
                    .getText()
                    .getBytes());
            result.setSampleLabel(context.toString());
            result.setSamplerData(context.toString());
            result.setURL(new URL("http://nagoya.apache.org/fakepage.html"));
            jmctx.setPreviousResult(result);
            AnchorModifier modifier = new AnchorModifier();
            modifier.setThreadContext(jmctx);
            modifier.process();
            assertEquals(
                "http://nagoya.apache.org/bugzilla/buglist.cgi?"
                    + "bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED"
                    + "&email1=&emailtype1=substring&emailassigned_to1=1"
                    + "&email2=&emailtype2=substring&emailreporter2=1"
                    + "&bugidtype=include&bug_id=&changedin=&votes="
                    + "&chfieldfrom=&chfieldto=Now&chfieldvalue="
                    + "&product=JMeter&short_desc=&short_desc_type=substring"
                    + "&long_desc=&long_desc_type=substring&bug_file_loc="
                    + "&bug_file_loc_type=substring&keywords="
                    + "&keywords_type=anywords"
                    + "&field0-0-0=noop&type0-0-0=noop&value0-0-0="
                    + "&cmdtype=doit&order=Reuse+same+sort+as+last+time",
                config.toString());
            config.recoverRunningVersion();
            assertEquals(
                "http://nagoya.apache.org/bugzilla/buglist.cgi?"
                    + "bug_status=.*&bug_status=.*&bug_status=.*&email1="
                    + "&emailtype1=substring&emailassigned_to1=1&email2="
                    + "&emailtype2=substring&emailreporter2=1"
                    + "&bugidtype=include&bug_id=&changedin=&votes="
                    + "&chfieldfrom=&chfieldto=Now&chfieldvalue="
                    + "&product=JMeter&short_desc=&short_desc_type=substring"
                    + "&long_desc=&long_desc_type=substring&bug_file_loc="
                    + "&bug_file_loc_type=substring&keywords="
                    + "&keywords_type=anywords&field0-0-0=noop"
                    + "&type0-0-0=noop&value0-0-0=&cmdtype=doit"
                    + "&order=Reuse+same+sort+as+last+time",
                config.toString());
        }

        public void testModifySampler() throws Exception
        {
            testProcessingHTMLFile(
                "/testfiles/jmeter_home_page.html");
        }
        
        public void testModifySamplerWithRelativeLink() throws Exception
        {
            testProcessingHTMLFile(
                "/testfiles/jmeter_home_page_with_relative_links.html");
        }
//* Feature not yet implemented. TODO: implement it.
        public void testModifySamplerWithBaseHRef() throws Exception
        {
            testProcessingHTMLFile(
                "/testfiles/jmeter_home_page_with_base_href.html");
        }
//*/
    }
}
