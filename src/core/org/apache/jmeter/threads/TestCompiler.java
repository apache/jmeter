package org.apache.jmeter.threads;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.Modifier;
import org.apache.jmeter.config.ResponseBasedModifier;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.PerSampleClonable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.ListedHashTree;
import org.apache.jmeter.util.ListedHashTreeVisitor;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * <p>
 *
 * Title: </p> <p>
 *
 * Description: </p> <p>
 *
 * Copyright: Copyright (c) 2001</p> <p>
 *
 * Company: </p>
 *
 *@author    unascribed
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class TestCompiler implements ListedHashTreeVisitor, SampleListener
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.engine");
	LinkedList stack = new LinkedList();
	Map samplerConfigMap = new HashMap();
	Set objectsWithFunctions = new HashSet();
	ListedHashTree testTree;
	SampleResult previousResult;
	Sampler currentSampler;
	JMeterVariables threadVars;
	private static Set pairing = new HashSet();

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param testTree  !ToDo (Parameter description)
	 ***************************************/
	public TestCompiler(ListedHashTree testTree,
			JMeterVariables vars)
	{
		threadVars = vars;
		this.testTree = testTree;
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public static void initialize()
	{
		pairing.clear();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void sampleOccurred(SampleEvent e)
	{
		previousResult = e.getResult();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void sampleStarted(SampleEvent e) { }

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void sampleStopped(SampleEvent e) { }

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param sampler  !ToDo (Parameter description)
	 *@return         !ToDo (Return description)
	 ***************************************/
	public SamplePackage configureSampler(Sampler sampler)
	{
		currentSampler = sampler;
		SamplePackage ret = new SamplePackage();
		Sampler clonedSampler = sampler;
		SamplerConfigs configs = (SamplerConfigs)samplerConfigMap.get(sampler);
		if(sampler instanceof PerSampleClonable)
		{
			clonedSampler = (Sampler)sampler.clone();
		}
		if(objectsWithFunctions.contains(sampler))
		{
			replaceValues(clonedSampler);
		}
		ret.setSampler(clonedSampler);
		configureWithConfigElements(clonedSampler,configs.getConfigs());
		configureWithResponseModifiers(clonedSampler,configs.getResponseModifiers());
		configureWithModifiers(clonedSampler,configs.getModifiers());
		configureSamplerPackage(ret,configs);
		//replaceStatics(ret);
		return ret;
	}

	/****************************************
	 * !ToDo
	 *
	 *@param node     !ToDo
	 *@param subTree  !ToDo
	 ***************************************/
	public void addNode(Object node, ListedHashTree subTree)
	{
		stack.addLast(node);
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void subtractNode()
	{
		TestElement child = (TestElement)stack.getLast();
		if(child instanceof Sampler)
		{
			saveSamplerConfigs((Sampler)child);
		}
		stack.removeLast();
		if(stack.size() > 0)
		{
			ObjectPair pair = new ObjectPair(child, stack.getLast());
			if(!pairing.contains(pair))
			{
				((TestElement)stack.getLast()).addTestElement(child);
				pairing.add(pair);
			}
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void processPath() { }

	private void saveSamplerConfigs(Sampler sam)
	{
		List configs = new LinkedList();
		List modifiers = new LinkedList();
		List responseModifiers = new LinkedList();
		List listeners = new LinkedList();
		List timers = new LinkedList();
		List assertions = new LinkedList();
		for(int i = stack.size(); i > 0; i--)
		{
			Iterator iter = testTree.list(stack.subList(0, i)).iterator();
			while(iter.hasNext())
			{
				TestElement item = (TestElement)iter.next();
				synchronized(item)
				{
					if(hasFunctions(item))
					{
						objectsWithFunctions.add(item);
					}
				}
				if((item instanceof ConfigTestElement))
				{
					configs.add(item);
				}
				if(item instanceof Modifier)
				{
					modifiers.add(item);
				}
				if(item instanceof ResponseBasedModifier)
				{
					responseModifiers.add(item);
				}
				if(item instanceof SampleListener)
				{
					listeners.add(item);
				}
				if(item instanceof Timer)
				{
					timers.add(item);
				}
				if(item instanceof Assertion)
				{
					assertions.add(item);
				}			
			}
		}
		synchronized(sam)
		{
			if(hasFunctions(sam))
			{
				objectsWithFunctions.add(sam);
			}
		}
		SamplerConfigs samplerConfigs = new SamplerConfigs(configs,modifiers,responseModifiers,
				listeners,timers,assertions);
		samplerConfigMap.put(sam, samplerConfigs);
	}
	
	private class SamplerConfigs
	{
		List configs;
		List modifiers;
		List listeners;
		List assertions;
		List timers;
		List responseModifiers;
		
		public SamplerConfigs(List configs,List modifiers,List responseModifiers,
				List listeners,List timers,List assertions)
		{
			this.configs = configs;
			this.modifiers = modifiers;
			this.responseModifiers = responseModifiers;
			this.listeners = listeners;
			this.timers = timers;
			this.assertions = assertions;
		}
		
		public List getConfigs()
		{
			return configs;
		}
		
		public List getModifiers()
		{
			return modifiers;
		}
		
		public List getResponseModifiers()
		{
			return responseModifiers;
		}
		
		public List getListeners()
		{
			return listeners;
		}
		
		public List getAssertions()
		{
			return assertions;
		}
		
		public List getTimers()
		{
			return timers;
		}
	}

	/****************************************
	 * !ToDo (Class description)
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	public static class Test extends junit.framework.TestCase
	{
		/****************************************
		 * !ToDo (Constructor description)
		 *
		 *@param name  !ToDo (Parameter description)
		 ***************************************/
		public Test(String name)
		{
			super(name);
		}

		/****************************************
		 * !ToDo
		 *
		 *@exception Exception  !ToDo (Exception description)
		 ***************************************/
		public void testConfigGathering() throws Exception
		{
			ListedHashTree testing = new ListedHashTree();
			GenericController controller = new GenericController();
			ConfigTestElement config1 = new ConfigTestElement();
			config1.setName("config1");
			config1.setProperty(HTTPSampler.DOMAIN, "www.jarkarta.org");
			HTTPSampler sampler = new HTTPSampler();
			sampler.setName("sampler");
			Arguments args = new Arguments();
			args.addArgument("param1", "value1");
			HTTPSampler sampler2 = new HTTPSampler();
			sampler.setName("sampler2");
			Arguments args2 = new Arguments();
			args2.addArgument(new HTTPArgument("xml","<data>1234</data>"));
			sampler2.setArguments(args2);
			testing.add(controller, config1);
			testing.add(controller, sampler);
			testing.add(controller,sampler2);
			testing.get(controller).add(sampler, args);
			TestCompiler.initialize();

			TestCompiler compiler = new TestCompiler(testing,new JMeterVariables());
			testing.traverse(compiler);
			sampler = (HTTPSampler)compiler.configureSampler(sampler).getSampler();
			assertEquals(config1.getProperty(HTTPSampler.DOMAIN), sampler.getDomain());
			assertEquals(args.getArgument(0).getName(), sampler.getArguments().getArgument(0).getName());
			assertEquals(1,sampler.getArguments().getArguments().size());
		}
	}

	/****************************************
	 * !ToDo (Class description)
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	private class ObjectPair
	{
		Object one, two;

		/****************************************
		 * !ToDo (Constructor description)
		 *
		 *@param one  !ToDo (Parameter description)
		 *@param two  !ToDo (Parameter description)
		 ***************************************/
		public ObjectPair(Object one, Object two)
		{
			this.one = one;
			this.two = two;
		}

		/****************************************
		 * !ToDo (Method description)
		 *
		 *@return   !ToDo (Return description)
		 ***************************************/
		public int hashCode()
		{
			return one.hashCode() + two.hashCode();
		}

		/****************************************
		 * !ToDo (Method description)
		 *
		 *@param o  !ToDo (Parameter description)
		 *@return   !ToDo (Return description)
		 ***************************************/
		public boolean equals(Object o)
		{
			if(o instanceof ObjectPair)
			{
				return one == ((ObjectPair)o).one && two == ((ObjectPair)o).two;
			}
			return false;
		}
	}
	
	private void configureWithConfigElements(Sampler sam,List configs)
	{
		Iterator iter = configs.iterator();
		while(iter.hasNext())
		{
			ConfigTestElement config = (ConfigTestElement)iter.next();
			TestElement clonedConfig = (TestElement)cloneIfNecessary(config);
			if(objectsWithFunctions.contains(config))
			{
				replaceValues(clonedConfig);
			}
			sam.addTestElement(clonedConfig);
		}
	}
	
	private void configureWithModifiers(Sampler sam,List modifiers)
	{
		Iterator iter = modifiers.iterator();
		while(iter.hasNext())
		{
			Modifier mod = (Modifier)iter.next();
			TestElement cloned = (TestElement)cloneIfNecessary(mod);
			if(objectsWithFunctions.contains(mod))
			{
				replaceValues(cloned);
			}
			((Modifier)cloned).modifyEntry(sam);
		}
	}
	
	private void configureWithResponseModifiers(Sampler sam,List responseModifiers)
	{
		Iterator iter = responseModifiers.iterator();
		while(iter.hasNext())
		{
			ResponseBasedModifier mod = (ResponseBasedModifier)iter.next();
			TestElement cloned = (TestElement)cloneIfNecessary(mod);
			if(objectsWithFunctions.contains(mod))
			{
				replaceValues(cloned);
			}
			if(previousResult != null)
			{
				((ResponseBasedModifier)cloned).modifyEntry(sam,previousResult);
			}
		}
	}
	
	private Object cloneIfNecessary(Object el)
	{
		if(el instanceof PerSampleClonable || objectsWithFunctions.contains(el))
		{
			return ((TestElement)el).clone();
		}
		else
		{
			return el;
		}
	}

	private void configureSamplerPackage(SamplePackage ret,SamplerConfigs configs)
	{
		Iterator iter = configs.getAssertions().iterator();
		while(iter.hasNext())
		{
			Assertion assertion = (Assertion)iter.next();
			TestElement cloned = (TestElement)cloneIfNecessary(assertion);
			if(objectsWithFunctions.contains(assertion))
			{
				replaceValues(cloned);
			}
			ret.addAssertion((Assertion)cloned);
		}
		iter = configs.getTimers().iterator();
		while(iter.hasNext())
		{
			Timer timer = (Timer)iter.next();
			TestElement cloned = (TestElement)cloneIfNecessary(timer);
			if(objectsWithFunctions.contains(timer))
			{
				replaceValues(cloned);
			}
			ret.addTimer((Timer)cloned);
		}
		
		iter = configs.getListeners().iterator();
		while(iter.hasNext())
		{
			SampleListener lis = (SampleListener)iter.next();
			TestElement cloned = (TestElement)cloneIfNecessary(lis);
			if(objectsWithFunctions.contains(lis))
			{
				replaceValues(cloned);
			}
			ret.addSampleListener((SampleListener)cloned);
		}
	}
	
	private boolean hasFunctions(TestElement el)
	{
		boolean hasFunctions = false;
		Iterator iter = el.getPropertyNames().iterator();
		while(iter.hasNext())
		{
			String propName = (String)iter.next();
			Object propValue = el.getProperty(propName);
			if(propValue instanceof Function)
			{
				((Function)propValue).setJMeterVariables(threadVars);
				hasFunctions = true;
			}
			else if(propValue instanceof TestElement)
			{
				if(hasFunctions((TestElement)propValue))
				{
					hasFunctions = true;
				}
			}
			else if(propValue instanceof Collection)
			{
				if(hasFunctions((Collection)propValue))
				{
					hasFunctions = true;
				}
			}
		}
		return hasFunctions;
	}
	
	private boolean hasFunctions(Collection values)
	{
		Iterator iter = new LinkedList(values).iterator();
		boolean hasFunctions = false;
		while(iter.hasNext())
		{
			Object val = iter.next();
			if(val instanceof TestElement)
			{
				if(hasFunctions((TestElement)val))
				{
					hasFunctions = true;
				}
			}
			else if(val instanceof Function)
			{
				((Function)val).setJMeterVariables(threadVars);
				hasFunctions = true;
			}
			else if(val instanceof Collection)
			{
				if(hasFunctions((Collection)val))
				{
					hasFunctions = true;
				}
			}
		}
		return hasFunctions;
	}	
	
	private void replaceValues(TestElement el)
	{
		Iterator iter = el.getPropertyNames().iterator();
		while(iter.hasNext())
		{
			String propName = (String)iter.next();
			Object propValue = el.getProperty(propName);
			if(propValue instanceof Function)
			{
				try
				{
					el.setProperty(propName,((Function)propValue).execute(previousResult,currentSampler));
				}
				catch(InvalidVariableException e)
				{}
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
	
	private Collection replaceValues(Collection values)
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
			else if(val instanceof Function)
			{
				try
				{
					val = ((Function)val).execute(previousResult,currentSampler);
				}
				catch(InvalidVariableException e)
				{}
			}
			else if(val instanceof Collection)
			{
				val = replaceValues((Collection)val);
			}
			newColl.add(val);
		}
		return newColl;
	}
}
