package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.LoggingManager;
import org.apache.jmeter.util.StringUtilities;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class ValueReplacer
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(LoggingManager.ELEMENTS);
	CompoundFunction masterFunction = new CompoundFunction();
	Map variables = new HashMap();
	
	public ValueReplacer()
	{
	}

	public ValueReplacer(Map variables)
	{
		setUserDefinedVariables(variables);
	}
	
	public void setUserDefinedVariables(Map variables)
	{
		masterFunction.setUserDefinedVariables(variables);
		this.variables = variables;
	}
	
	public void replaceValues(TestElement el) throws InvalidVariableException
	{
		Iterator iter = el.getPropertyNames().iterator();
		while(iter.hasNext())
		{
			String propName = (String)iter.next();
			Object propValue = el.getProperty(propName);
			if(propValue instanceof String)
			{
				Object newValue = getNewValue((String)propValue);
				el.setProperty(propName,newValue);
			}
			else if(propValue instanceof TestElement)
			{
				replaceValues((TestElement)propValue);
			}
			else if(propValue instanceof Collection)
			{
				el.setProperty(propName,replaceValues((Collection)propValue));
			}
		}
	}
	
	private Object getNewValue(String propValue) throws InvalidVariableException
	{
		Object newValue = propValue;
				masterFunction.clear();
				masterFunction.setParameters((String)propValue);
				if(masterFunction.hasFunction())
				{
					newValue = masterFunction.getFunction();
				}
				else if(masterFunction.hasStatics())
				{
					newValue = masterFunction.getStaticSubstitution();
				}
				return newValue;
	}
	
	public Collection replaceValues(Collection values) throws InvalidVariableException
	{
		Collection newColl = null;
		try {
			newColl = (Collection)values.getClass().newInstance();
		} catch(Exception e) {
			log.error("",e);
			return values;
		} 
		Iterator iter = values.iterator();
		while(iter.hasNext())
		{
			Object val = iter.next();
			if(val instanceof TestElement)
			{
				replaceValues((TestElement)val);
			}
			else if(val instanceof String)
			{
				val = getNewValue((String)val);
			}
			else if(val instanceof Collection)
			{
				val = replaceValues((Collection)val);
			}
			newColl.add(val);
		}
		return newColl;
	}
	
	/**
	 * Replaces raw values with user-defined variable names.
	 */
	public Collection reverseReplace(Collection values)
	{
		Collection newColl = null;
		try {
			newColl = (Collection)values.getClass().newInstance();
		} catch(Exception e) {
			log.error("",e);
			return values;
		} 
		Iterator iter = values.iterator();
		while(iter.hasNext())
		{
			Object val = iter.next();
			if(val instanceof TestElement)
			{
				reverseReplace((TestElement)val);
			}
			else if(val instanceof String)
			{
				val = substituteValues((String)val);
			}
			else if(val instanceof Collection)
			{
				val = reverseReplace((Collection)val);
			}
			newColl.add(val);
		}
		return newColl;
	} 
	
	/**
	 * Replaces raw values with user-defined variable names.
	 */
	public void reverseReplace(TestElement el)
	{
		Iterator iter = el.getPropertyNames().iterator();
		while(iter.hasNext())
		{
			String propName = (String)iter.next();
			Object propValue = el.getProperty(propName);
			if(propValue instanceof String)
			{
				Object newValue = substituteValues((String)propValue);
				el.setProperty(propName,newValue);
			}
			else if(propValue instanceof TestElement)
			{
				reverseReplace((TestElement)propValue);
			}
			else if(propValue instanceof Collection)
			{
				el.setProperty(propName,reverseReplace((Collection)propValue));
			}
		}
	}
	
	private String substituteValues(String input)
	{
		Iterator iter = variables.keySet().iterator();
		while(iter.hasNext())
		{
			String key = (String)iter.next();
			String value = (String)variables.get(key);
			input = StringUtilities.substitute(input,value,"${"+key+"}");
		}
		return input;
	}
			
			
	
	public static class Test extends TestCase
	{
		Map variables;
		
		public Test(String name)
		{
			super(name);
		}
		
		public void setUp()
		{
			variables = new HashMap();
			variables.put("server","jakarta.apache.org");
			variables.put("username","jack");
			variables.put("password","jacks_password");
			variables.put("regex",".*");
		}
		
		public void testReverseReplacement() throws Exception
		{
			ValueReplacer replacer = new ValueReplacer(variables);
			TestElement element = new TestPlan();
			element.setProperty("domain","jakarta.apache.org");
			List args = new LinkedList();
			args.add("username is jack");
			args.add("jacks_password");
			element.setProperty("args",args);
			replacer.reverseReplace(element);
			assertEquals("${server}",element.getProperty("domain"));
		}
	}
}
