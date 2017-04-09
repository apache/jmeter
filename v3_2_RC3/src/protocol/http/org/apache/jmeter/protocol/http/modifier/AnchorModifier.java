/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.parser.HtmlParsingUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// For Unit tests, @see TestAnchorModifier

public class AnchorModifier extends AbstractTestElement implements PreProcessor, Serializable {
    private static final Logger log = LoggerFactory.getLogger(AnchorModifier.class);

    private static final long serialVersionUID = 240L;

    public AnchorModifier() {
    }

    /**
     * Modifies an Entry object based on HTML response text.
     */
    @Override
    public void process() {
        JMeterContext context = getThreadContext();
        Sampler sam = context.getCurrentSampler();
        SampleResult res = context.getPreviousResult();
        HTTPSamplerBase sampler;
        HTTPSampleResult result;
        if (!(sam instanceof HTTPSamplerBase) || !(res instanceof HTTPSampleResult)) {
            log.info("Can't apply HTML Link Parser when the previous" + " sampler run is not an HTTP Request.");
            return;
        } else {
            sampler = (HTTPSamplerBase) sam;
            result = (HTTPSampleResult) res;
        }
        List<HTTPSamplerBase> potentialLinks = new ArrayList<>();
        String responseText = result.getResponseDataAsString();
        int index = responseText.indexOf('<'); // $NON-NLS-1$
        if (index == -1) {
            index = 0;
        }
        if (log.isDebugEnabled()) {
            log.debug("Check for matches against: "+sampler.toString());
        }
        Document html = (Document) HtmlParsingUtils.getDOM(responseText.substring(index));
        addAnchorUrls(html, result, sampler, potentialLinks);
        addFormUrls(html, result, sampler, potentialLinks);
        addFramesetUrls(html, result, sampler, potentialLinks);
        if (!potentialLinks.isEmpty()) {
            HTTPSamplerBase url = potentialLinks.get(ThreadLocalRandom.current().nextInt(potentialLinks.size()));
            if (log.isDebugEnabled()) {
                log.debug("Selected: "+url.toString());
            }
            sampler.setDomain(url.getDomain());
            sampler.setPath(url.getPath());
            if (url.getMethod().equals(HTTPConstants.POST)) {
                for (JMeterProperty jMeterProperty : sampler.getArguments()) {
                    Argument arg = (Argument) jMeterProperty.getObjectValue();
                    modifyArgument(arg, url.getArguments());
                }
            } else {
                sampler.setArguments(url.getArguments());
                // config.parseArguments(url.getQueryString());
            }
            sampler.setProtocol(url.getProtocol());
        } else {
            log.debug("No matches found");
        }
    }

    private void modifyArgument(Argument arg, Arguments args) {
        if (log.isDebugEnabled()) {
            log.debug("Modifying argument: " + arg);
        }
        List<Argument> possibleReplacements = new ArrayList<>();
        PropertyIterator iter = args.iterator();
        Argument replacementArg;
        while (iter.hasNext()) {
            replacementArg = (Argument) iter.next().getObjectValue();
            try {
                if (HtmlParsingUtils.isArgumentMatched(replacementArg, arg)) {
                    possibleReplacements.add(replacementArg);
                }
            } catch (Exception ex) {
                log.error("Problem adding Argument", ex);
            }
        }

        if (!possibleReplacements.isEmpty()) {
            replacementArg = possibleReplacements.get(ThreadLocalRandom.current().nextInt(possibleReplacements.size()));
            arg.setName(replacementArg.getName());
            arg.setValue(replacementArg.getValue());
            if (log.isDebugEnabled()) {
                log.debug("Just set argument to values: " + arg.getName() + " = " + arg.getValue());
            }
            args.removeArgument(replacementArg);
        }
    }

    public void addConfigElement(ConfigElement config) {
    }

    private void addFormUrls(Document html, HTTPSampleResult result, HTTPSamplerBase config, 
            List<HTTPSamplerBase> potentialLinks) {
        NodeList rootList = html.getChildNodes();
        List<HTTPSamplerBase> urls = new LinkedList<>();
        for (int x = 0; x < rootList.getLength(); x++) {
            urls.addAll(HtmlParsingUtils.createURLFromForm(rootList.item(x), result.getURL()));
        }
        for (HTTPSamplerBase newUrl : urls) {
            newUrl.setMethod(HTTPConstants.POST);
            if (log.isDebugEnabled()) {
                log.debug("Potential Form match: " + newUrl.toString());
            }
            if (HtmlParsingUtils.isAnchorMatched(newUrl, config)) {
                log.debug("Matched!");
                potentialLinks.add(newUrl);
            }
        }
    }

    private void addAnchorUrls(Document html, HTTPSampleResult result, HTTPSamplerBase config, 
            List<HTTPSamplerBase> potentialLinks) {
        String base = "";
        NodeList baseList = html.getElementsByTagName("base"); // $NON-NLS-1$
        if (baseList.getLength() > 0) {
            base = baseList.item(0).getAttributes().getNamedItem("href").getNodeValue(); // $NON-NLS-1$
        }
        NodeList nodeList = html.getElementsByTagName("a"); // $NON-NLS-1$
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node tempNode = nodeList.item(i);
            NamedNodeMap nnm = tempNode.getAttributes();
            Node namedItem = nnm.getNamedItem("href"); // $NON-NLS-1$
            if (namedItem == null) {
                continue;
            }
            String hrefStr = namedItem.getNodeValue();
            if (hrefStr.startsWith("javascript:")) { // $NON-NLS-1$
                continue; // No point trying these
            }
            try {
                HTTPSamplerBase newUrl = HtmlParsingUtils.createUrlFromAnchor(hrefStr, ConversionUtils.makeRelativeURL(result.getURL(), base));
                newUrl.setMethod(HTTPConstants.GET);
                if (log.isDebugEnabled()) {
                    log.debug("Potential <a href> match: " + newUrl);
                }
                if (HtmlParsingUtils.isAnchorMatched(newUrl, config)) {
                    log.debug("Matched!");
                    potentialLinks.add(newUrl);
                }
            } catch (MalformedURLException e) {
                log.warn("Bad URL "+e);
            }
        }
    }

    private void addFramesetUrls(Document html, HTTPSampleResult result,
       HTTPSamplerBase config, List<HTTPSamplerBase> potentialLinks) {
       String base = "";
       NodeList baseList = html.getElementsByTagName("base"); // $NON-NLS-1$
       if (baseList.getLength() > 0) {
           base = baseList.item(0).getAttributes().getNamedItem("href") // $NON-NLS-1$
                   .getNodeValue();
       }
       NodeList nodeList = html.getElementsByTagName("frame"); // $NON-NLS-1$
       for (int i = 0; i < nodeList.getLength(); i++) {
           Node tempNode = nodeList.item(i);
           NamedNodeMap nnm = tempNode.getAttributes();
           Node namedItem = nnm.getNamedItem("src"); // $NON-NLS-1$
           if (namedItem == null) {
               continue;
           }
           String hrefStr = namedItem.getNodeValue();
           try {
               HTTPSamplerBase newUrl = HtmlParsingUtils.createUrlFromAnchor(
                       hrefStr, ConversionUtils.makeRelativeURL(result.getURL(), base));
               newUrl.setMethod(HTTPConstants.GET);
               if (log.isDebugEnabled()) {
                   log.debug("Potential <frame src> match: " + newUrl);
               }
               if (HtmlParsingUtils.isAnchorMatched(newUrl, config)) {
                   log.debug("Matched!");
                   potentialLinks.add(newUrl);
               }
           } catch (MalformedURLException e) {
               log.warn("Bad URL "+e);
           }
       }
   }
}
