package org.apache.jmeter.engine;
import java.util.Map;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.functions.ValueReplacer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
/**
 * @author mstover
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class PreCompiler implements HashTreeTraverser
{
	transient private static Logger log =
		Hierarchy.getDefaultHierarchy().getLoggerFor(JMeterUtils.ENGINE);
	private Map userDefinedVariables;
	private boolean testValid = true;
	private ValueReplacer replacer;
	public PreCompiler()
	{
		replacer = new ValueReplacer();
	}
	/**
	 * @see ListedHashTreeVisitor#addNode(Object, ListedHashTree)
	 */
	public void addNode(Object node, HashTree subTree)
	{
		if (node instanceof TestPlan)
		{
			replacer.setUserDefinedVariables(
				((TestPlan) node).getUserDefinedVariables());
		}
		if (node instanceof TestElement)
		{
			try
			{
				replacer.replaceValues((TestElement) node);
			}
			catch (InvalidVariableException e)
			{
				log.error("invalid variables",e);
				testValid = false;
			}
		}
	}
	/**
	 * @see ListedHashTreeVisitor#subtractNode()
	 */
	public void subtractNode()
	{
	}
	/**
	 * @see ListedHashTreeVisitor#processPath()
	 */
	public void processPath()
	{
	}
}
