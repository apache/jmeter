package org.apache.jmeter.engine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MultiProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */
public class ValueReplacer
{
    transient private static Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor(JMeterUtils.ENGINE);
    CompoundVariable masterFunction = new CompoundVariable();
    Map variables = new HashMap();
    TestPlan tp;

    public ValueReplacer()
    {
        tp = new TestPlan();
    }

    public ValueReplacer(TestPlan tp)
    {
        this.tp = tp;
        setUserDefinedVariables(tp.getUserDefinedVariables());
    }

    public void setUserDefinedVariables(Map variables)
    {
        this.variables = variables;
    }

    public void replaceValues(TestElement el) throws InvalidVariableException
    {
        Collection newProps =
            replaceValues(
                el.propertyIterator(),
                new ReplaceStringWithFunctions(masterFunction, variables));
        setProperties(el, newProps);
    }

    private void setProperties(TestElement el, Collection newProps)
    {
        Iterator iter = newProps.iterator();
        el.clear();
        while(iter.hasNext())
        {
            el.setProperty((JMeterProperty)iter.next());
        }
    }
    
    public void reverseReplace(TestElement el) throws InvalidVariableException
    {
        Collection newProps =
            replaceValues(
                el.propertyIterator(),
                new ReplaceFunctionsWithStrings(masterFunction, variables));
        setProperties(el, newProps);
    }
        
    public void undoReverseReplace(TestElement el)
        throws InvalidVariableException
    {
        Collection newProps =
            replaceValues(
                el.propertyIterator(),
                new UndoVariableReplacement(masterFunction, variables));
        setProperties(el, newProps);
    }

    public void addVariable(String name, String value)
    {
        tp.addParameter(name, value);
        setUserDefinedVariables(tp.getUserDefinedVariables());
    }
    
    private Collection replaceValues(
        PropertyIterator iter,
        ValueTransformer transform)
        throws InvalidVariableException
    {
        List props = new LinkedList();
        while(iter.hasNext())
        {
            JMeterProperty val = iter.next();
            if (val instanceof StringProperty)
            {
                val = transform.transformValue((StringProperty) val);
            }
            else if (val instanceof MultiProperty)
            {
                MultiProperty multiVal = (MultiProperty)val;
                Collection newValues =
                    replaceValues(multiVal.iterator(), transform);
                multiVal.clear();
                Iterator propIter = newValues.iterator();
                while(propIter.hasNext())
                {
                    multiVal.addProperty((JMeterProperty) propIter.next());
                }
            }
            props.add(val);
        }
        return props;
    }

    

    public static class Test extends TestCase
    {
        TestPlan variables;

        public Test(String name)
        {
            super(name);
        }

        public void setUp()
        {
            variables = new TestPlan();
            variables.addParameter("server", "jakarta.apache.org");
            variables.addParameter("username", "jack");
            variables.addParameter("password", "jacks_password");
            variables.addParameter("regex", ".*");
            JMeterVariables vars = new JMeterVariables();
            vars.put("server", "jakarta.apache.org");
            JMeterContextService.getContext().setVariables(vars);
            JMeterContextService.getContext().setSamplingStarted(true);
        }
        
        

        public void testReverseReplacement() throws Exception
        {
            ValueReplacer replacer = new ValueReplacer(variables);
            assertTrue(
                variables.getUserDefinedVariables().containsKey("server"));
            assertTrue(replacer.variables.containsKey("server"));
            TestElement element = new TestPlan();
            element.setProperty(
                new StringProperty("domain", "jakarta.apache.org"));
            List args = new ArrayList();
            args.add("username is jack");
            args.add("jacks_password");
            element.setProperty(new CollectionProperty("args", args));
            replacer.reverseReplace(element);
            assertEquals("${server}", element.getPropertyAsString("domain"));
            args = (List) element.getProperty("args").getObjectValue();
            assertEquals(
                "${password}",
                ((JMeterProperty) args.get(1)).getStringValue());
        }

        public void testReplace() throws Exception
        {
            ValueReplacer replacer = new ValueReplacer();
            replacer.setUserDefinedVariables(
                variables.getUserDefinedVariables());
            TestElement element = new ConfigTestElement();
            element.setProperty(new StringProperty("domain", "${server}"));
            replacer.replaceValues(element);
            log.debug("domain property = " + element.getProperty("domain"));
            element.setRunningVersion(true);
            assertEquals(
                "jakarta.apache.org",
                element.getPropertyAsString("domain"));
        }

        /* (non-Javadoc)
         * @see junit.framework.TestCase#tearDown()
         */
        protected void tearDown() throws Exception
        {
            JMeterContextService.getContext().setSamplingStarted(false);
        }
    }
}
