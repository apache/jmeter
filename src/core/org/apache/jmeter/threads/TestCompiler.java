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
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.PerSampleClonable;
import org.apache.jmeter.testelement.PerThreadClonable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.ListedHashTree;
import org.apache.jmeter.util.ListedHashTreeVisitor;

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
	LinkedList stack = new LinkedList();
	Map samplerConfigs = new HashMap();
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
		if(sampler instanceof PerSampleClonable)
		{
			clonedSampler = (Sampler)((PerSampleClonable)sampler).clone();
		}
		if(objectsWithFunctions.contains(sampler))
		{
			replaceValues(clonedSampler);
		}
		ret.setSampler(clonedSampler);
		ret.addSampleListener(this);
		Iterator iter = ((List)samplerConfigs.get(sampler)).iterator();
		while(iter.hasNext())
		{
			TestElement config = (TestElement)iter.next();
			layerElement(ret,config, clonedSampler);
		}
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
				if(item != sam)
				{
					configs.add(item);
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
		samplerConfigs.put(sam, configs);
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
			testing.add(controller, config1);
			testing.add(controller, sampler);
			testing.get(controller).add(sampler, args);
			TestCompiler.initialize();

			TestCompiler compiler = new TestCompiler(testing,new JMeterVariables());
			testing.traverse(compiler);
			sampler = (HTTPSampler)compiler.configureSampler(sampler).getSampler();
			assertEquals(config1.getProperty(HTTPSampler.DOMAIN), sampler.getDomain());
			assertEquals(args.getArgument(0).getName(), sampler.getArguments().getArgument(0).getName());
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

	private void layerElement(SamplePackage ret,TestElement config, Sampler clonedSampler)
	{
		boolean replace = objectsWithFunctions.contains(config);
		if(config instanceof PerSampleClonable)
		{
			config = (TestElement)((PerSampleClonable)config).clone();
		}
		if(config instanceof Modifier)
		{
			((Modifier)config).modifyEntry(clonedSampler);
		}
		if(config instanceof ResponseBasedModifier && previousResult != null)
		{
			((ResponseBasedModifier)config).modifyEntry(clonedSampler, previousResult);
		}
		if(config instanceof SampleListener)
		{
			ret.addSampleListener((SampleListener)config);
		}
		if(config instanceof Assertion)
		{
			ret.addAssertion((Assertion)config);
		}
		if(config instanceof Timer)
		{
			ret.addTimer((Timer)config);
		}
		if(replace && config instanceof PerSampleClonable)
		{
			replaceValues(config);
		}
		else if(replace)
		{
			config = (TestElement)config.clone();
			replaceValues(config);
		}
		clonedSampler.addTestElement(config);
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
			e.printStackTrace();
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
