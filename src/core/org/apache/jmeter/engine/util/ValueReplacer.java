package org.apache.jmeter.engine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
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
    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(JMeterUtils.ENGINE);
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
        masterFunction.setUserDefinedVariables(variables);
        this.variables = variables;
    }

    public void replaceValues(TestElement el) throws InvalidVariableException
    {
        Iterator iter = el.getPropertyNames().iterator();
        while (iter.hasNext())
        {
            String propName = (String) iter.next();
            Object propValue = el.getProperty(propName);
            if (propValue instanceof String)
            {
                Object newValue = getNewValue((String) propValue);
                el.setProperty(propName, newValue);
            }
            else if (propValue instanceof TestElement)
            {
                replaceValues((TestElement) propValue);
            }
            else if (propValue instanceof Collection)
            {
                el.setProperty(propName, replaceValues((Collection) propValue));
            }
        }
    }

    private Object getNewValue(String propValue) throws InvalidVariableException
    {
        Object newValue = propValue;
        masterFunction.clear();
        masterFunction.setParameters((String) propValue);
        log.debug("value replacer looking at: " + propValue);
        if (masterFunction.hasFunction())
        {
            newValue = masterFunction.getFunction();
            log.debug("replaced with: " + newValue);
        }
        else if (masterFunction.hasStatics())
        {
            newValue = masterFunction.getStaticSubstitution();
            log.debug("replaced with: " + newValue);
        }
        return newValue;
    }

    public void addVariable(String name, String value)
    {
        tp.addParameter(name, value);
        setUserDefinedVariables(tp.getUserDefinedVariables());
    }

    public Collection replaceValues(Collection values) throws InvalidVariableException
    {
        Collection newColl = null;
        try
        {
            newColl = (Collection) values.getClass().newInstance();
        }
        catch (Exception e)
        {
            log.error("", e);
            return values;
        }
        Iterator iter = values.iterator();
        while (iter.hasNext())
        {
            Object val = iter.next();
            if (val instanceof TestElement)
            {
                replaceValues((TestElement) val);
            }
            else if (val instanceof String)
            {
                val = getNewValue((String) val);
            }
            else if (val instanceof Collection)
            {
                val = replaceValues((Collection) val);
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
        try
        {
            newColl = (Collection) values.getClass().newInstance();
        }
        catch (Exception e)
        {
            log.error("", e);
            return values;
        }
        Iterator iter = values.iterator();
        while (iter.hasNext())
        {
            Object val = iter.next();
            if (val instanceof TestElement)
            {
                reverseReplace((TestElement) val);
            }
            else if (val instanceof String)
            {
                val = substituteValues((String) val);
            }
            else if (val instanceof Collection)
            {
                val = reverseReplace((Collection) val);
            }
            newColl.add(val);
        }
        return newColl;
    }

    /**
         * Remove variables references and replace with the raw string values.
         */
    public Collection undoReverseReplace(Collection values)
    {
        Collection newColl = null;
        try
        {
            newColl = (Collection) values.getClass().newInstance();
        }
        catch (Exception e)
        {
            log.error("", e);
            return values;
        }
        Iterator iter = values.iterator();
        while (iter.hasNext())
        {
            Object val = iter.next();
            if (val instanceof TestElement)
            {
                undoReverseReplace((TestElement) val);
            }
            else if (val instanceof String)
            {
                val = substituteReferences((String) val);
            }
            else if (val instanceof Collection)
            {
                val = undoReverseReplace((Collection) val);
            }
            newColl.add(val);
        }
        return newColl;
    }

    /**
     * Remove variables references and replace with the raw string values.
     * @param el
     */
    public void undoReverseReplace(TestElement el)
    {
        Iterator iter = el.getPropertyNames().iterator();
        while (iter.hasNext())
        {
            String propName = (String) iter.next();
            Object propValue = el.getProperty(propName);
            if (propValue instanceof String)
            {
                Object newValue = substituteReferences((String) propValue);
                el.setProperty(propName, newValue);
            }
            else if (propValue instanceof TestElement)
            {
                undoReverseReplace((TestElement) propValue);
            }
            else if (propValue instanceof Collection)
            {
                el.setProperty(propName, undoReverseReplace((Collection) propValue));
            }
        }
    }

    /**
     * Replaces raw values with user-defined variable names.
     */
    public void reverseReplace(TestElement el)
    {
        Iterator iter = el.getPropertyNames().iterator();
        while (iter.hasNext())
        {
            String propName = (String) iter.next();
            Object propValue = el.getProperty(propName);
            if (propValue instanceof String)
            {
                Object newValue = substituteValues((String) propValue);
                el.setProperty(propName, newValue);
            }
            else if (propValue instanceof TestElement)
            {
                reverseReplace((TestElement) propValue);
            }
            else if (propValue instanceof Collection)
            {
                el.setProperty(propName, reverseReplace((Collection) propValue));
            }
        }
    }

    private String substituteValues(String input)
    {
        Iterator iter = variables.keySet().iterator();
        while (iter.hasNext())
        {
            String key = (String) iter.next();
            String value = (String) variables.get(key);
            input = StringUtilities.substitute(input, value, "${" + key + "}");
        }
        return input;
    }

    private String substituteReferences(String input)
    {
        Iterator iter = variables.keySet().iterator();
        while (iter.hasNext())
        {
            String key = (String) iter.next();
            String value = (String) variables.get(key);
            input = StringUtilities.substitute(input, "${" + key + "}", value);
        }
        return input;
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
        }

        public void testReverseReplacement() throws Exception
        {
            ValueReplacer replacer = new ValueReplacer(variables);
            assertTrue(variables.getUserDefinedVariables().containsKey("server"));
            assertTrue(replacer.variables.containsKey("server"));
            TestElement element = new TestPlan();
            element.setProperty("domain", "jakarta.apache.org");
            List args = new ArrayList();
            args.add("username is jack");
            args.add("jacks_password");
            element.setProperty("args", args);
            replacer.reverseReplace(element);
            assertEquals("${server}", element.getProperty("domain"));
            args = (List) element.getProperty("args");
            assertEquals("${password}", args.get(1));
        }

        public void testReplace() throws Exception
        {
            ValueReplacer replacer = new ValueReplacer();
            replacer.setUserDefinedVariables(variables.getUserDefinedVariables());
            TestElement element = new ConfigTestElement();
            element.setProperty("domain", "${server}");
            replacer.replaceValues(element);
            assertEquals("jakarta.apache.org", ((CompoundVariable) element.getProperty("domain")).execute());
        }
    }
}
