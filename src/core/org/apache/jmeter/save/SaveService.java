package org.apache.jmeter.save;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.ListedHashTree;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.xml.sax.SAXException;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class SaveService
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.util");
	private static final String ASSERTION_RESULT_TAG_NAME = "assertionResult";
	private static final String SAMPLE_RESULT_TAG_NAME = "sampleResult";
	private static final String TIME = "time";
	private static final String LABEL = "label";
	private static final String RESPONSE_CODE = "responseCode";
	private static final String RESPONSE_MESSAGE = "responseMessage";
	private static final String THREAD_NAME = "threadName";
	private static final String DATA_TYPE = "dataType";
	private static final String TIME_STAMP = "timeStamp";
	private static final String BINARY = "binary";
	private static final String FAILURE_MESSAGE = "failureMessage";
	private static final String ERROR = "error";
	private static final String FAILURE = "failure";
	private static final String SUCCESSFUL = "success";
	
	private static DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

	public SaveService()
	{
	}

	public static void saveSubTree(ListedHashTree subTree,OutputStream writer) throws
			IOException
	{
		Configuration config = (Configuration)getConfigsFromTree(subTree).get(0);
		DefaultConfigurationSerializer saver = new DefaultConfigurationSerializer();
		saver.setIndent(true);
		try
		{
			saver.serialize(writer,config);
		}
		catch(SAXException e)
		{
			throw new IOException("SAX implementation problem");
		}
		catch(ConfigurationException e)
		{
			throw new IOException("Problem using Avalon Configuration tools");
		}
	}
	
	public static SampleResult getSampleResult(Configuration config)
	{
		SampleResult result = new SampleResult();
		result.setThreadName(config.getAttribute(THREAD_NAME,""));
		result.setDataType(config.getAttribute(DATA_TYPE,""));
		result.setResponseCode(config.getAttribute(RESPONSE_CODE,""));
		result.setResponseMessage(config.getAttribute(RESPONSE_MESSAGE,""));
		result.setTime(config.getAttributeAsLong(TIME,0L));
		result.setTimeStamp(config.getAttributeAsLong(TIME_STAMP,0L));
		result.setSuccessful(config.getAttributeAsBoolean(SUCCESSFUL,false));
		result.setSampleLabel(config.getAttribute(LABEL,""));
		result.setResponseData(getBinaryData(config.getChild(BINARY)));
		Configuration[] subResults = config.getChildren(SAMPLE_RESULT_TAG_NAME);
		for(int i = 0;i < subResults.length;i++)
		{
			result.addSubResult(getSampleResult(subResults[i]));
		}
		Configuration[] assResults = config.getChildren(ASSERTION_RESULT_TAG_NAME);
		for(int i = 0;i < assResults.length;i++)
		{
			result.addAssertionResult(getAssertionResult(assResults[i]));
		}
		return result;
	}

	private static List getConfigsFromTree(ListedHashTree subTree)
	{
		Iterator iter = subTree.list().iterator();
		List configs = new LinkedList();
		while (iter.hasNext())
		{
			TestElement item = (TestElement)iter.next();
			DefaultConfiguration config = new DefaultConfiguration("node","node");
			config.addChild(getConfigForTestElement(null,item));
			List configList = getConfigsFromTree(subTree.get(item));
			Iterator iter2 = configList.iterator();
			while(iter2.hasNext())
			{
				config.addChild((Configuration)iter2.next());
			}
			configs.add(config);
		}
		return configs;
	}
	
	public static Configuration getConfiguration(byte[] bin)
	{
		DefaultConfiguration config = new DefaultConfiguration(BINARY,"JMeter Save Service");
		try {
			config.setValue(new String(bin,"utf-8"));
		} catch(UnsupportedEncodingException e) {
			log.error("",e);
		}
		return config;
	}
	
	public static byte[] getBinaryData(Configuration config)
	{
		if(config == null)
		{
			return new byte[0];
		}
		try {
			return config.getValue("").getBytes("utf-8");
		} catch(UnsupportedEncodingException e) {
			return new byte[0];
		}
	}
	
	public static AssertionResult getAssertionResult(Configuration config)
	{
		AssertionResult result = new AssertionResult();
		result.setError(config.getAttributeAsBoolean(ERROR,false));
		result.setFailure(config.getAttributeAsBoolean(FAILURE,false));
		result.setFailureMessage(config.getAttribute(FAILURE_MESSAGE,""));
		return result;		
	}
	
	public static Configuration getConfiguration(AssertionResult assResult)
	{
		DefaultConfiguration config = new DefaultConfiguration(ASSERTION_RESULT_TAG_NAME,
				"JMeter Save Service");
		config.setAttribute(FAILURE_MESSAGE,assResult.getFailureMessage());
		config.setAttribute(ERROR,""+assResult.isError());
		config.setAttribute(FAILURE,""+assResult.isFailure());
		return config;		
	}
	
	public static Configuration getConfiguration(SampleResult result,boolean funcTest)
	{
		DefaultConfiguration config = new DefaultConfiguration(SAMPLE_RESULT_TAG_NAME,"JMeter Save Service");
		config.setAttribute(TIME,""+result.getTime());
		config.setAttribute(LABEL,result.getSampleLabel());
		config.setAttribute(RESPONSE_CODE,result.getResponseCode());
		config.setAttribute(RESPONSE_MESSAGE,result.getResponseMessage());
		config.setAttribute(THREAD_NAME,result.getThreadName());
		config.setAttribute(DATA_TYPE,result.getDataType());
		config.setAttribute(TIME_STAMP,""+result.getTimeStamp());
		config.setAttribute(SUCCESSFUL,new Boolean(result.isSuccessful()).toString());
		SampleResult[] subResults = result.getSubResults();
		for(int i = 0;i < subResults.length;i++)
		{
			config.addChild(getConfiguration(subResults[i],funcTest));
		}
		if(funcTest)
		{
			config.addChild(getConfigForTestElement(null,result.getSamplerData()));
			AssertionResult[] assResults = result.getAssertionResults();
			for(int i = 0;i < assResults.length;i++)
			{
				config.addChild(getConfiguration(assResults[i]));
			}
			config.addChild(getConfiguration(result.getResponseData()));
		}
		return config;		
	}

	public static Configuration getConfigForTestElement(String named,TestElement item)
	{
		DefaultConfiguration config = new DefaultConfiguration("testelement","testelement");
		if(named != null)
		{
			config.setAttribute("name",named);
		}
		if(item.getProperty(TestElement.TEST_CLASS) != null)
		{
			config.setAttribute("class",(String)item.getProperty(TestElement.TEST_CLASS));
		}
		else
		{
			config.setAttribute("class",item.getClass().getName());
		}
		Iterator iter = item.getPropertyNames().iterator();
		while (iter.hasNext())
		{
			String name = (String)iter.next();
			Object value = item.getProperty(name);
			if(value instanceof TestElement)
			{
				config.addChild(getConfigForTestElement(name,(TestElement)value));
			}
			else if(value instanceof Collection)
			{
				config.addChild(createConfigForCollection(name,(Collection)value));
			}
			else if(value != null)
			{
				config.addChild(createConfigForString(name,value.toString()));
			}
		}
		return config;
	}

	private static Configuration createConfigForCollection(String propertyName,Collection list)
	{
		DefaultConfiguration config = new DefaultConfiguration("collection","collection");
		if(propertyName != null)
		{
			config.setAttribute("name",propertyName);
		}
		config.setAttribute("class",list.getClass().getName());
		Iterator iter = list.iterator();
		while (iter.hasNext())
		{
			Object item = iter.next();
			if(item instanceof TestElement)
			{
				config.addChild(getConfigForTestElement(null,(TestElement)item));
			}
			else if(item instanceof Collection)
			{
				config.addChild(createConfigForCollection(null,(Collection)item));
			}
			else
			{
				config.addChild(createConfigForString(item.toString()));
			}
		}
		return config;
	}

	private static Configuration createConfigForString(String value)
	{
		DefaultConfiguration config = new DefaultConfiguration("string","string");
		config.setValue(value);
		return config;
	}

	private static Configuration createConfigForString(String name,String value)
	{
		if(value == null || value.equals(""))
		{
			value = " ";
		}
		DefaultConfiguration config = new DefaultConfiguration("property","property");
		config.setAttribute("name",name);
		config.setValue(value);
		return config;
	}

	public synchronized static ListedHashTree loadSubTree(InputStream in) throws IOException
	{
		try
		{
			Configuration config = builder.build(in);
			ListedHashTree loadedTree = generateNode(config);
			return loadedTree;
		}
		catch(ConfigurationException e)
		{
			throw new IOException("Problem loading using Avalon Configuration tools");
		}
		catch(SAXException e)
		{
			throw new IOException("Problem with SAX implementation");
		}
	}

	public static TestElement createTestElement(Configuration config) throws ConfigurationException,
			ClassNotFoundException, IllegalAccessException,InstantiationException
	{
		TestElement element = null;
		try
		{
			element = (TestElement)Class.forName((String)config.getAttribute("class")).newInstance();
		}
		catch(InstantiationException e)
		{
			//Assertion is now an interface, replaced with ResponseAssertion
			if(config.getAttribute("class").equals("org.apache.jmeter.assertions.Assertion"))
			{
				element = new ResponseAssertion();
			}
		}
		Configuration[] children = config.getChildren();
		for (int i = 0; i < children.length; i++)
		{
			if(children[i].getName().equals("property"))
			{
				try
				{
					element.setProperty(children[i].getAttribute("name"),
						children[i].getValue());
				}
				catch (Exception ex)
				{
					log.error("Problem loading property",ex);
					element.setProperty(children[i].getAttribute("name"),"");
				}
			}
			else if(children[i].getName().equals("testelement"))
			{
				element.setProperty(children[i].getAttribute("name"),
						createTestElement(children[i]));
			}
			else if(children[i].getName().equals("collection"))
			{
				element.setProperty(children[i].getAttribute("name"),
						createCollection(children[i]));
			}
		}
		return element;
	}

	private static Collection createCollection(Configuration config) throws ConfigurationException,
			ClassNotFoundException,IllegalAccessException,InstantiationException
	{
		Collection coll = (Collection)Class.forName((String)config.getAttribute("class")).newInstance();
		Configuration[] items = config.getChildren();
		for (int i = 0; i < items.length; i++)
		{
			if(items[i].getName().equals("property"))
			{
				coll.add(items[i].getValue(""));
			}
			else if(items[i].getName().equals("testelement"))
			{
				coll.add(createTestElement(items[i]));
			}
			else if(items[i].getName().equals("collection"))
			{
				coll.add(createCollection(items[i]));
			}
			else if(items[i].getName().equals("string"))
			{
				coll.add(items[i].getValue(""));
			}
		}
		return coll;
	}

	private static ListedHashTree generateNode(Configuration config)
	{
		TestElement element = null;
		try
		{
			element = createTestElement(config.getChild("testelement"));
		}
		catch(Exception e)
		{
			log.error("Problem loading part of file",e);
			return null;
		}
		ListedHashTree subTree = new ListedHashTree(element);
		Configuration[] subNodes = config.getChildren("node");
		for (int i = 0; i < subNodes.length; i++)
		{
			ListedHashTree t = generateNode(subNodes[i]);
			if(t != null)
			{
				subTree.add(element,t);
			}
		}
		return subTree;
	}
	
	public static class Test extends TestCase
	{
		private File assertionFile;
		public Test(String name)
		{
			super(name);
		}
		
		public void setUp() {
			assertionFile =
				new File(System.getProperty("user.dir") + "/testfiles", "assertion.jmx");
			
		}
		
		public void testLoadAssertion() throws Exception
		{
			Configuration config = new DefaultConfigurationBuilder().buildFromFile(assertionFile);
			ResponseAssertion testEl = (ResponseAssertion)createTestElement(config.getChild("testelement"));
			assertEquals("save this string \\d+",testEl.getTestStrings().get(0));
		}
	}
}