package org.apache.jmeter.engine;

import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.config.Arguments;
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
            Map args= ((TestPlan)node).getUserDefinedVariables();
            replacer.setUserDefinedVariables(args);
            JMeterVariables vars= new JMeterVariables();
            vars.putAll(args);
            JMeterContextService.getContext().setVariables(vars);
        }
        else if (node instanceof TestElement)
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

        if (node instanceof Arguments)
        {
            Map args= ((Arguments)node).getArgumentsAsMap();
            for (Iterator a= args.entrySet().iterator(); a.hasNext(); )
            {
               Map.Entry e= (Map.Entry)a.next();
               replacer.addVariable((String)e.getKey(), (String)e.getValue());
            }
            JMeterContextService.getContext().getVariables().putAll(args);
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
