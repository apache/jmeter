package org.apache.jmeter.engine;

import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 * @version $Revision$
 */
public class PreCompiler implements HashTreeTraverser
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    private ValueReplacer replacer;

    public PreCompiler()
    {
        replacer = new ValueReplacer();
    }

    /* (non-Javadoc)
     * @see HashTreeTraverser#addNode(Object, HashTree)
     */
    public void addNode(Object node, HashTree subTree)
    {
        if (node instanceof TestPlan)
        {
            replacer.setUserDefinedVariables(
                ((TestPlan) node).getUserDefinedVariables());
            JMeterVariables vars = new JMeterVariables();
            vars.putAll(((TestPlan) node).getUserDefinedVariables());
            JMeterContextService.getContext().setVariables(vars);
        }
        if (node instanceof TestElement)
        {
            try
            {
                replacer.replaceValues((TestElement) node);
                ((TestElement)node).setRunningVersion(true);
            }
            catch (InvalidVariableException e)
            {
                log.error("invalid variables", e);
            }
        }
    }

    /* (non-Javadoc)
     * @see HashTreeTraverser#subtractNode()
     */
    public void subtractNode()
    {
    }

    /* (non-Javadoc)
     * @see HashTreeTraverser#processPath()
     */
    public void processPath()
    {
    }
}
