package org.apache.jmeter.functions;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.ClassFinder;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CompoundFunction implements Function
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.elements");
	private JMeterVariables threadVars;
	
	static Map functions = new HashMap();
	private Map definedValues;
	private boolean hasFunction,hasStatics,hasUnknowns;
	private String staticSubstitution;
	private static Perl5Util util = new Perl5Util();
	
	static private PatternCompiler compiler = new Perl5Compiler();
	static private String variableSplitter = "/(\\${)/"; 
	
	LinkedList compiledComponents = new LinkedList();
	
	static
	{
		try
		{
			List classes = ClassFinder.findClassesThatExtend(new Class[]{Function.class},true);
			Iterator iter = classes.iterator();
			while(iter.hasNext())
			{
					Function tempFunc = (Function)Class.forName((String)iter.next()).newInstance();
					functions.put(tempFunc.getReferenceKey(),tempFunc.getClass());
			}
		}
		catch(Exception err)
		{
			log.error("",err);
		}
	}
	
	public CompoundFunction()
	{
		hasFunction = false;
		hasStatics = false;
		hasUnknowns = false;
		definedValues = new HashMap();
		staticSubstitution = "";
	}

	/**
	 * @see Function#execute(SampleResult)
	 */
	public String execute(SampleResult previousResult,Sampler currentSampler) {
		if(compiledComponents == null || compiledComponents.size() == 0)
		{
			return "";
		}
		StringBuffer results = new StringBuffer();
		Iterator iter = compiledComponents.iterator();
		while(iter.hasNext())
		{
			Object item = iter.next();
			if(item instanceof Function)
			{
				try {
					results.append(((Function)item).execute(previousResult,currentSampler));
				} catch(InvalidVariableException e) {
				}
			}
			else
			{
				results.append(item);
			}
		}
		return results.toString();
	}
	
	public CompoundFunction getFunction()
	{
		CompoundFunction func = new CompoundFunction();
		func.compiledComponents = (LinkedList)compiledComponents.clone();
		return func;
	}
	
	public List getArgumentDesc()
	{
		return new LinkedList();
	}
	
	public void clear()
	{
		hasFunction = false;
		hasStatics = false;
		compiledComponents.clear();
		staticSubstitution = "";
	}
	
	public void setJMeterVariables(JMeterVariables threadVars)
	{
		Iterator iter = compiledComponents.iterator();
		while(iter.hasNext())
		{
			Object item = iter.next();
			if(item instanceof Function)
			{
				((Function)item).setJMeterVariables(threadVars);
			}
		}
		this.threadVars = threadVars;
	}

	/**
	 * @see Function#setParameters(String)
	 */
	public void setParameters(String parameters) throws InvalidVariableException 
	{
		if(parameters == null || parameters.length() == 0)
		{
			return;
		}
		List components = new LinkedList();
		util.split(components,variableSplitter,parameters);
		Iterator iter = components.iterator();
		String previous = "";
		while(iter.hasNext())
		{
			String part = (String)iter.next();
			int index = getFunctionEndIndex(part);
			if(index > -1 && previous.equals("${"))
			{
				String function = part.substring(0,index);
				String functionName = parseFunctionName(function);
				if(definedValues.containsKey(functionName))
				{
					Object replacement = definedValues.get(functionName);
					if(replacement instanceof Class)
					{
						try {
							hasFunction = true;
							Function func = (Function)((Class)replacement).newInstance();
							func.setParameters(extractParams(function));
							compiledComponents.addLast(func);
						} catch(Exception e) {
							log.error("",e);
							throw new InvalidVariableException();
						} 
					}
					else
					{
						hasStatics = true;
						addStringToComponents(compiledComponents,(String)replacement);
					}
				}
				else
				{
					UnknownFunction unknown = new UnknownFunction(functionName);
					compiledComponents.addLast(unknown);
					hasFunction = true;
					hasUnknowns = true;
				}
				if((index+1) < part.length())
				{
					addStringToComponents(compiledComponents,part.substring(index+1));
				}
			}
			else if(previous.equals("${"))
			{
				addStringToComponents(compiledComponents,"${");
				addStringToComponents(compiledComponents,part);
			}
			else if(!part.equals("${"))
			{
				addStringToComponents(compiledComponents, part);
			}
			previous = part;
		}
		if(!hasFunction)
		{
			staticSubstitution = compiledComponents.getLast().toString();
			if(hasStatics())
			{
				compiledComponents.clear();
				hasStatics = false;
				setParameters(staticSubstitution);
				hasStatics = true;
			}
		}
		else if(hasStatics())
		{
			iter = new LinkedList(compiledComponents).iterator();
			while(iter.hasNext())
			{
				Object item = iter.next();
				if(item instanceof StringBuffer)
				{
					CompoundFunction nestedFunc = new CompoundFunction();
					nestedFunc.setUserDefinedVariables(new HashMap());
					nestedFunc.setParameters(item.toString());
					if(nestedFunc.hasFunction())
					{
						int index = compiledComponents.indexOf(item);
						compiledComponents.remove(index);
						compiledComponents.add(index,nestedFunc);
					}
				}
			} 
		}
	}

	private void addStringToComponents(LinkedList refinedComponents, String part) {
		if(part == null || part.length() == 0)
		{
			return;
		}
		if(refinedComponents.size() == 0)
		{
			refinedComponents.addLast(new StringBuffer(part));
		}
		else
		{
			if(refinedComponents.getLast() instanceof StringBuffer)
			{
				((StringBuffer)refinedComponents.getLast()).append(part);
			}
			else
			{
				refinedComponents.addLast(new StringBuffer(part));
			}
		}
	}
	
	private String extractParams(String function)
	{
		if(function.indexOf("(") > -1)
		{
			return function.substring(function.indexOf("(")+1,function.lastIndexOf(")"));
		}
		else
		{
			return "";
		}
	}

	private int getFunctionEndIndex(String part) {
		int index = part.indexOf("}");
		return index;
	}

	private String parseFunctionName(String function) {
		String functionName = function;
		int parenIndex = -1;
		if((parenIndex = function.indexOf("(")) > -1)
		{
			functionName = function.substring(0,parenIndex);
		}
		return functionName;
	}
	
	public boolean hasFunction()
	{
		return hasFunction;
	}
	
	public boolean hasStatics()
	{
		return hasStatics;
	}
	
	public String getStaticSubstitution()
	{
		return staticSubstitution;
	}
	
	public void setUserDefinedVariables(Map userVariables)
	{
		definedValues.clear();
		definedValues.putAll(functions);
		definedValues.putAll(userVariables);
	}

	/**
	 * @see Function#getReferenceKey()
	 */
	public String getReferenceKey() {
		return "";
	}
	
	public static class Test extends TestCase
	{
		CompoundFunction function;
		SampleResult result;
		
		public Test(String name)
		{
			super(name);
		}
		
		public void setUp()
		{
			Map userDefinedVariables = new HashMap();
			userDefinedVariables.put("my_regex",".*");
			userDefinedVariables.put("server","jakarta.apache.org");
			function = new CompoundFunction();
			function.setUserDefinedVariables(userDefinedVariables);
			result = new SampleResult();
			result.setResponseData("<html>hello world</html>".getBytes());
		}
		
		public void testParseExample1() throws Exception
		{
			function.setParameters("${__regexFunction(<html>(.*)</html>,$1$)}");
			function.setJMeterVariables(new JMeterVariables());
			assertEquals(1,function.compiledComponents.size());
			assertTrue(function.compiledComponents.getFirst() instanceof RegexFunction);
			assertTrue(function.hasFunction());
			assertTrue(!function.hasStatics());
			assertEquals("hello world",((Function)function.compiledComponents.getFirst()).execute(result,null));
			assertEquals("hello world",function.execute(result,null));
		}
		
		public void testParseExample2() throws Exception
		{
			function.setParameters("It should say:${${__regexFunction("+URLEncoder.encode("<html>(.*)</html>")+",$1$)}}");
			function.setJMeterVariables(new JMeterVariables());
			assertEquals(3,function.compiledComponents.size());
			assertEquals("It should say:${",function.compiledComponents.getFirst().toString());
			assertTrue(function.hasFunction());
			assertTrue(!function.hasStatics());
			assertEquals("hello world",((Function)function.compiledComponents.get(1)).execute(result,null));
			assertEquals("}",function.compiledComponents.get(2).toString());
			assertEquals("It should say:${hello world}",function.execute(result,null));
			assertEquals("It should say:${<html>(.*)</html>,$1$}",function.execute(null,null));
		}
		
		public void testParseExample3() throws Exception
		{
			function.setParameters("${__regexFunction(<html>(.*)</html>,$1$)}${__regexFunction(<html>(.*o)(.*o)(.*)</html>,$1$$3$)}");
			function.setJMeterVariables(new JMeterVariables());
			assertEquals(2,function.compiledComponents.size());
			assertTrue(function.hasFunction());
			assertTrue(!function.hasStatics());
			assertEquals("hello world",((Function)function.compiledComponents.get(0)).execute(result,null));
			assertEquals("hellorld",((Function)function.compiledComponents.get(1)).execute(result,null));
			assertEquals("hello worldhellorld",function.execute(result,null));
			assertEquals("<html>(.*)</html>,$1$<html>(.*o)(.*o)(.*)</html>,$1$$3$",
					function.execute(null,null));
		}
		
		public void testParseExample4() throws Exception
		{
			function.setParameters("${non-existing function}");
			function.setJMeterVariables(new JMeterVariables());
			assertEquals(1,function.compiledComponents.size());
			assertTrue(function.hasFunction());
			assertTrue(!function.hasStatics());
			assertEquals("${non-existing function}",function.execute(result,null));
			assertEquals("${non-existing function}",function.execute(null,null));
		}
		
		public void testParseExample6() throws Exception
		{
			function.setParameters("${server}");
			function.setJMeterVariables(new JMeterVariables());
			assertEquals(1,function.compiledComponents.size());
			assertTrue(!function.hasFunction());
			assertTrue(function.hasStatics());
			assertEquals("jakarta.apache.org",function.execute(null,null));
		}
		
		public void testParseExample5() throws Exception
		{
			function.setParameters("");
			function.setJMeterVariables(new JMeterVariables());
			assertEquals(0,function.compiledComponents.size());
			assertTrue(!function.hasFunction());
			assertTrue(!function.hasStatics());
		}
		
		public void testNestedExample1() throws Exception
		{
			function.setParameters("${__regexFunction(<html>(${my_regex})</html>,$1$)}${__regexFunction(<html>(.*o)(.*o)(.*)</html>,$1$$3$)}");
			function.setJMeterVariables(new JMeterVariables());
			assertEquals(2,function.compiledComponents.size());
			assertTrue(function.hasFunction());
			assertTrue(function.hasStatics());
			assertEquals("hello world",((Function)function.compiledComponents.get(0)).execute(result,null));
			assertEquals("hellorld",((Function)function.compiledComponents.get(1)).execute(result,null));
			assertEquals("hello worldhellorld",function.execute(result,null));
			assertEquals("<html>(.*)</html>,$1$<html>(.*o)(.*o)(.*)</html>,$1$$3$",
					function.execute(null,null));
		}
		
		public void testNestedExample2() throws Exception
		{
			function.setParameters("${__regexFunction(<html>(${my_regex})</html>,$1$)}");
			function.setJMeterVariables(new JMeterVariables());
			assertEquals(1,function.compiledComponents.size());
			assertTrue(function.compiledComponents.getFirst() instanceof RegexFunction);
			assertTrue(function.hasFunction());
			assertTrue(function.hasStatics());
			assertEquals("hello world",((Function)function.compiledComponents.getFirst()).execute(result,null));
			assertEquals("hello world",function.execute(result,null));
		}
	}

}
