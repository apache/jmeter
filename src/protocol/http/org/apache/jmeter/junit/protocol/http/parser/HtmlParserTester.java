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

package org.apache.jmeter.junit.protocol.http.parser;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.protocol.http.modifier.AnchorModifier;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPNullSampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;

/**
 * Created    June 14, 2001
 * @version    $Revision$ Last updated: $Date$
 */
public class HtmlParserTester extends JMeterTestCase
{
    AnchorModifier parser = new AnchorModifier();

    /**
     * Constructor for the HtmlParserTester object.
     */
    public HtmlParserTester(String name)
    {
        super(name);
    }
    
    private JMeterContext jmctx = null;
    
    public void setUp()
	{
    	jmctx = JMeterContextService.getContext();
    	parser.setThreadContext(jmctx);
    }

    /**
     * A unit test for JUnit.
     */
    public void testSimpleParse() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig(".*/index\\.html");
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<a href=\"index.html\">Goto index page</a></body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setSamplerData(context.toString());
        result.setURL(context.getUrl());
        jmctx.setPreviousResult(result);
        parser.process();
        assertEquals(
            "http://www.apache.org/subdir/index.html",
            config.getUrl().toString());
    }

    public void testSimpleParse2() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig("/index\\.html");
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<a href=\"/index.html\">Goto index page</a>"
                + "hfdfjiudfjdfjkjfkdjf"
                + "<b>bold text</b><a href=lowerdir/index.html>lower</a>"
                + "</body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        jmctx.setPreviousResult(result);
        parser.process();
        String newUrl = config.getUrl().toString();
        assertTrue(
            "http://www.apache.org/index.html".equals(newUrl)
                || "http://www.apache.org/subdir/lowerdir/index.html".equals(
                    newUrl));
    }

    public void testSimpleParse3() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig(".*index.*");
        config.getArguments().addArgument("param1", "value1");
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<a href=\"/home/index.html?param1=value1\">"
                + "Goto index page</a></body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        jmctx.setPreviousResult(result);
        parser.process();
        String newUrl = config.getUrl().toString();
        assertEquals(
            "http://www.apache.org/home/index.html?param1=value1",
            newUrl);
    }

    public void testSimpleParse4() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig("/subdir/index\\..*");
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<A HREF=\"index.html\">Goto index page</A></body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        jmctx.setPreviousResult(result);
        parser.process();
        String newUrl = config.getUrl().toString();
        assertEquals("http://www.apache.org/subdir/index.html", newUrl);
    }

    public void testSimpleParse5() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig("/subdir/index\\.h.*");
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/one/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<a href=\"../index.html\">Goto index page</a></body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        jmctx.setPreviousResult(result);
        parser.process();
        String newUrl = config.getUrl().toString();
        assertEquals("http://www.apache.org/subdir/index.html", newUrl);
    }

    public void testFailSimpleParse1() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig(".*index.*?param2=.+1");
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<a href=\"/home/index.html?param1=value1\">"
                + "Goto index page</a></body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        String newUrl = config.getUrl().toString();
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        jmctx.setPreviousResult(result);
        parser.process();
        assertEquals(newUrl, config.getUrl().toString());
    }

    public void testFailSimpleParse3() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig("/home/index.html");
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<a href=\"/home/index.html?param1=value1\">"
                + "Goto index page</a></body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        String newUrl = config.getUrl().toString();
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        jmctx.setPreviousResult(result);
        parser.process();
        assertEquals(newUrl + "?param1=value1", config.getUrl().toString());
    }

    public void testFailSimpleParse2() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig(".*login\\.html");
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<a href=\"/home/index.html?param1=value1\">"
                + "Goto index page</a></body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setPreviousResult(result);
        parser.process();
        String newUrl = config.getUrl().toString();
        assertTrue(
            !"http://www.apache.org/home/index.html?param1=value1".equals(
                newUrl));
        assertEquals(config.getUrl().toString(), newUrl);
    }

    /**
     * A unit test for JUnit.
     */
    public void testSimpleFormParse() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig(".*index.html");
        config.addArgument("test", "g.*");
        config.setMethod(HTTPSamplerBase.POST);
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<form action=\"index.html\" method=\"POST\">"
                + "<input type=\"checkbox\" name=\"test\""
                + " value=\"goto\">Goto index page</form></body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        jmctx.setPreviousResult(result);
        parser.process();
        assertEquals(
            "http://www.apache.org/subdir/index.html",
            config.getUrl().toString());
        assertEquals("test=goto", config.getQueryString());
    }

    /**
     * A unit test for JUnit.
     */
    public void testBadCharParse() throws Exception
    {
        HTTPSamplerBase config = makeUrlConfig(".*index.html");
        config.addArgument("te$st", "g.*");
        config.setMethod(HTTPSamplerBase.POST);
        HTTPSamplerBase context =
            makeContext("http://www.apache.org/subdir/previous.html");
        String responseText =
            "<html><head><title>Test page</title></head><body>"
                + "<form action=\"index.html\" method=\"POST\">"
                + "<input type=\"checkbox\" name=\"te$st\""
                + " value=\"goto\">Goto index page</form></body></html>";
        HTTPSampleResult result = new HTTPSampleResult();
        result.setResponseData(responseText.getBytes());
        result.setSampleLabel(context.toString());
        result.setURL(context.getUrl());
        jmctx.setCurrentSampler(context);
        jmctx.setCurrentSampler(config);
        jmctx.setPreviousResult(result);
        parser.process();
        assertEquals(
            "http://www.apache.org/subdir/index.html",
            config.getUrl().toString());
        assertEquals("te%24st=goto", config.getQueryString());
    }

    private HTTPSamplerBase makeContext(String url) throws MalformedURLException
    {
        URL u = new URL(url);
        HTTPSamplerBase context = new HTTPNullSampler();
        context.setDomain(u.getHost());
        context.setPath(u.getPath());
        context.setPort(u.getPort());
        context.setProtocol(u.getProtocol());
        context.parseArguments(u.getQuery());
        return context;
    }

    private HTTPSamplerBase makeUrlConfig(String path)
    {
        HTTPSamplerBase config = new HTTPNullSampler();
        config.setDomain("www.apache.org");
        config.setMethod(HTTPSamplerBase.GET);
        config.setPath(path);
        config.setPort(80);
        config.setProtocol("http");
        return config;
    }
}
