package org.apache.jmeter.engine;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.functions.CompoundFunction;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.ListedHashTree;
import org.apache.jmeter.util.ListedHashTreeVisitor;

/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class PreCompiler implements ListedHashTreeVisitor {
	
	private Map userDefinedVariables;
	private CompoundFunction masterFunction = new CompoundFunction();
	private boolean testValid = true;
	
	public PreCompiler()
	{
	}

	/**
	 * @see ListedHashTreeVisitor#addNode(Object, ListedHashTree)
	 */
	public void addNode(Object node, ListedHashTree subTree) {
		if(node instanceof TestPlan)
		{
			masterFunction.setUserDefinedVariables(((TestPlan)node).getUserDefinedVariables());
		}
		if(node instanceof TestElement)
		{
			replaceValues((TestElement)node);
		}
	}

	/**
	 * @see ListedHashTreeVisitor#subtractNode()
	 */
	public void subtractNode() {
	}

	/**
	 * @see ListedHashTreeVisitor#processPath()
	 */
	public void processPath() {
	}
	
	private void replaceValues(TestElement el)
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
	
	private Object getNewValue(String propValue)
	{
		Object newValue = propValue;
				masterFunction.clear();
				try {
					masterFunction.setParameters((String)propValue);
				} catch(InvalidVariableException e) {
					testValid = false;
				}
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
	
	

}
