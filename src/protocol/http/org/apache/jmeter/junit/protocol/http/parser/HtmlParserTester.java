package org.apache.jmeter.junit.protocol.http.parser;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.jmeter.protocol.http.modifier.AnchorModifier;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;

/************************************************************
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author
 *@created    June 14, 2001
 *@version    1.0
 ***********************************************************/

public class HtmlParserTester extends TestCase
{

	AnchorModifier parser = new AnchorModifier();

	/************************************************************
	 *  Constructor for the HtmlParserTester object
	 *
	 *@param  name  Description of Parameter
	 ***********************************************************/
	public HtmlParserTester(String name)
	{
		super(name);
	}
    

	/************************************************************
	 *  A unit test for JUnit
	 *
	 *@exception  Exception  Description of Exception
	 ***********************************************************/
	public void testSimpleParse() throws Exception
	{
		HTTPSampler config = makeUrlConfig(".*/index\\.html");
		HTTPSampler context = makeContext("http://www.apache.org/subdir/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<a href=\"index.html\">Goto index page</a></body></html>";
		SampleResult result = new SampleResult();
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setCurrentSampler(config);
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());
		result.setSamplerData(context.toString());
        JMeterContextService.getContext().setPreviousResult(result);
		parser.process();
		assertEquals("http://www.apache.org:80/subdir/index.html",
				config.getUrl().toString());
	}

	public void testSimpleParse2() throws Exception
	{
		HTTPSampler config = makeUrlConfig("/index\\.html");
		HTTPSampler context = makeContext("http://www.apache.org/subdir/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<a href=\"/index.html\">Goto index page</a>hfdfjiudfjdfjkjfkdjf"+
				"<b>bold text</b><a href=lowerdir/index.html>lower</a></body></html>";
		SampleResult result = new SampleResult();
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setCurrentSampler(config);
        JMeterContextService.getContext().setPreviousResult(result);
        parser.process();
		String newUrl = config.getUrl().toString();
		assertTrue("http://www.apache.org:80/index.html".equals(newUrl)
				|| "http://www.apache.org:80/subdir/lowerdir/index.html".equals(newUrl));

	}

	public void testSimpleParse3() throws Exception
	{
		HTTPSampler config = makeUrlConfig(".*index.*");
		config.getArguments().addArgument("param1","value1");
		HTTPSampler context = makeContext("http://www.apache.org/subdir/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<a href=\"/home/index.html?param1=value1\">Goto index page</a></body></html>";
		SampleResult result = new SampleResult();
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setCurrentSampler(config);
        JMeterContextService.getContext().setPreviousResult(result);
        parser.process();
		String newUrl = config.getUrl().toString();
		assertEquals("http://www.apache.org:80/home/index.html?param1=value1",newUrl);
	}

	public void testSimpleParse4() throws Exception
	{
		HTTPSampler config = makeUrlConfig("/subdir/index\\..*");
		HTTPSampler context = makeContext("http://www.apache.org/subdir/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<A HREF=\"index.html\">Goto index page</A></body></html>";
		SampleResult result = new SampleResult();
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setCurrentSampler(config);
        JMeterContextService.getContext().setPreviousResult(result);
        parser.process();
		String newUrl = config.getUrl().toString();
		assertEquals("http://www.apache.org:80/subdir/index.html",newUrl);
	}

	public void testSimpleParse5() throws Exception
	{
		HTTPSampler config = makeUrlConfig("/subdir/index\\.h.*");
		HTTPSampler context = makeContext("http://www.apache.org/subdir/one/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<a href=\"../index.html\">Goto index page</a></body></html>";
		SampleResult result = new SampleResult();
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setCurrentSampler(config);
        JMeterContextService.getContext().setPreviousResult(result);
        parser.process();
		String newUrl = config.getUrl().toString();
		assertEquals("http://www.apache.org:80/subdir/index.html",newUrl);
	}

	public void testFailSimpleParse1() throws Exception
	{
		HTTPSampler config = makeUrlConfig(".*index.*?param2=.+1");
		HTTPSampler context = makeContext("http://www.apache.org/subdir/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<a href=\"/home/index.html?param1=value1\">Goto index page</a></body></html>";
		SampleResult result = new SampleResult();
		String newUrl = config.getUrl().toString();
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setCurrentSampler(config);
        JMeterContextService.getContext().setPreviousResult(result);
        parser.process();
		assertEquals(newUrl,config.getUrl().toString());
	}
	
	public void testFailSimpleParse3() throws Exception
	{
		HTTPSampler config = makeUrlConfig("/home/index.html");
		HTTPSampler context = makeContext("http://www.apache.org/subdir/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<a href=\"/home/index.html?param1=value1\">Goto index page</a></body></html>";
		SampleResult result = new SampleResult();
		String newUrl = config.getUrl().toString();
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setCurrentSampler(config);
        JMeterContextService.getContext().setPreviousResult(result);
        parser.process();
		assertEquals(newUrl+"?param1=value1",config.getUrl().toString());
	}

	public void testFailSimpleParse2() throws Exception
	{
		HTTPSampler config = makeUrlConfig(".*login\\.html");
		HTTPSampler context = makeContext("http://www.apache.org/subdir/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<a href=\"/home/index.html?param1=value1\">Goto index page</a></body></html>";
		SampleResult result = new SampleResult();
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setPreviousResult(result);
        parser.process();
		String newUrl = config.getUrl().toString();
		assertTrue(!"http://www.apache.org:80/home/index.html?param1=value1".equals(newUrl));
		assertEquals(config.getUrl().toString(),newUrl);
	}

	/************************************************************
	 *  A unit test for JUnit
	 *
	 *@exception  Exception  Description of Exception
	 ***********************************************************/
	public void testSimpleFormParse() throws Exception
	{
		HTTPSampler config = makeUrlConfig(".*index.html");
		config.addArgument("test","g.*");
		config.setMethod(HTTPSampler.POST);
		HTTPSampler context = makeContext("http://www.apache.org/subdir/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<form action=\"index.html\" method=\"POST\"><input type=\"checkbox\" name=\"test\""+
				" value=\"goto\">Goto index page</form></body></html>";
		SampleResult result = new SampleResult();
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setCurrentSampler(config);
        JMeterContextService.getContext().setPreviousResult(result);
        parser.process();
		assertEquals("http://www.apache.org:80/subdir/index.html",
				config.getUrl().toString());
		assertEquals("test=goto",config.getQueryString());
	}
	
	/************************************************************
	 *  A unit test for JUnit
	 *
	 *@exception  Exception  Description of Exception
	 ***********************************************************/
	public void testBadCharParse() throws Exception
	{
		HTTPSampler config = makeUrlConfig(".*index.html");
		config.addArgument("te$st","g.*");
		config.setMethod(HTTPSampler.POST);
		HTTPSampler context = makeContext("http://www.apache.org/subdir/previous.html");
		String responseText = "<html><head><title>Test page</title></head><body>" +
				"<form action=\"index.html\" method=\"POST\"><input type=\"checkbox\" name=\"te$st\""+
				" value=\"goto\">Goto index page</form></body></html>";
		SampleResult result = new SampleResult();
		result.setResponseData(responseText.getBytes());
		result.setSampleLabel(context.toString());        
        JMeterContextService.getContext().setCurrentSampler(context);
        JMeterContextService.getContext().setCurrentSampler(config);
        JMeterContextService.getContext().setPreviousResult(result);
        parser.process();
		assertEquals("http://www.apache.org:80/subdir/index.html",
				config.getUrl().toString());
		assertEquals("te%24st=goto",config.getQueryString());
	}
	
	private HTTPSampler makeContext(String url) throws MalformedURLException
	{
		URL u = new URL(url);
		HTTPSampler context = new HTTPSampler();
		context.setDomain(u.getHost());
		context.setPath(u.getPath());
		context.setPort(u.getPort());
		context.setProtocol(u.getProtocol());
		context.parseArguments(u.getQuery());
		return context;
	}

	private HTTPSampler makeUrlConfig(String path)
	{
		HTTPSampler config = new HTTPSampler();
		config.setDomain("www.apache.org");
		config.setMethod(config.GET);
		config.setPath(path);
		config.setPort(80);
		config.setProtocol("http");
		return config;
	}
}
