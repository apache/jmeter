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
import org.apache.jmeter.testelement.property.FunctionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
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
        this.variables = variables;
    }

    public void replaceValues(TestElement el) throws InvalidVariableException
    {
        PropertyIterator iter = el.propertyIterator();
        List newProps = new LinkedList();
        while (iter.hasNext())
        {
            JMeterProperty prop = iter.next();
            if (prop instanceof TestElementProperty)
            {
                replaceValues((TestElement) prop.getObjectValue());
            }
            else if (prop instanceof CollectionProperty)
            {
                replaceValues((CollectionProperty) prop);
            }
            else if (prop instanceof MapProperty)
            {
                replaceValues((MapProperty) prop);
            }
            else if (prop instanceof StringProperty)
            {
                JMeterProperty newValue = getNewValue((StringProperty) prop);
                newProps.add(newValue);
            }
        }
        Iterator props = newProps.iterator();
        while (props.hasNext())
        {
            el.setProperty((JMeterProperty) props.next());
        }
    }

    private JMeterProperty getNewValue(StringProperty prop) throws InvalidVariableException
    {
        JMeterProperty newValue = prop;
        masterFunction.clear();
        masterFunction.setParameters(prop.getStringValue());
        log.debug("value replacer looking at: " + prop.getStringValue());
        if (masterFunction.hasFunction())
        {
            newValue = new FunctionProperty(prop.getName(), masterFunction.getFunction());
            log.debug("replaced with function: " + newValue.getStringValue());
        }
        else if (masterFunction.hasStatics())
        {
            newValue = new StringProperty(prop.getName(), masterFunction.getStaticSubstitution());
            log.debug("replaced with static string: " + newValue.getStringValue());
        }
        return newValue;
    }

    public void addVariable(String name, String value)
    {
        tp.addParameter(name, value);
        setUserDefinedVariables(tp.getUserDefinedVariables());
    }

    public void replaceValues(CollectionProperty values) throws InvalidVariableException
    {
        Collection newColl = null;
        try
        {
            newColl = (Collection) values.getObjectValue().getClass().newInstance();
        }
        catch (Exception e)
        {
            log.error("", e);
            return;
        }
        PropertyIterator iter = values.iterator();
        while (iter.hasNext())
        {
            JMeterProperty val = iter.next();
            if (val instanceof TestElementProperty)
            {
                replaceValues((TestElement) val.getObjectValue());
            }
            else if (val instanceof StringProperty)
            {
                val = getNewValue((StringProperty) val);
            }
            else if (val instanceof CollectionProperty)
            {
                replaceValues((CollectionProperty) val);
            }
            else if (val instanceof MapProperty)
            {
                replaceValues((MapProperty) val);
            }
            newColl.add(val);
        }
        values.setCollection(newColl);
    }

    private void replaceValues(MapProperty values) throws InvalidVariableException
    {
        Map newMap = null;
        try
        {
            newMap = (Map) values.getObjectValue().getClass().newInstance();
        }
        catch (Exception e)
        {
            log.error("", e);
            return;
        }
        PropertyIterator iter = values.valueIterator();
        while (iter.hasNext())
        {
            JMeterProperty val = iter.next();
            if (val instanceof TestElementProperty)
            {
                replaceValues((TestElement) val.getObjectValue());
            }
            else if (val instanceof StringProperty)
            {
                val = getNewValue((StringProperty) val);
            }
            else if (val instanceof CollectionProperty)
            {
                replaceValues((CollectionProperty) val);
            }
            else if (val instanceof MapProperty)
            {
                replaceValues((MapProperty) val);
            }
            newMap.put(val.getName(), val);
        }
        values.setMap(newMap);
    }

    /**
     * Replaces raw values with user-defined variable names.
     */
    public void reverseReplace(CollectionProperty prop)
    {
        Collection newColl = null;
        try
        {
            newColl = (Collection) prop.getObjectValue().getClass().newInstance();
        }
        catch (Exception e)
        {
            log.error("", e);
            return;
        }
        PropertyIterator iter = prop.iterator();
        while (iter.hasNext())
        {
            JMeterProperty val = iter.next();
            if (val instanceof TestElementProperty)
            {
                reverseReplace((TestElement) val.getObjectValue());
            }
            else if (val instanceof StringProperty)
            {
                val = substituteValues((StringProperty) val);
            }
            else if (val instanceof CollectionProperty)
            {
                reverseReplace((CollectionProperty) val);
            }
            else if (val instanceof MapProperty)
            {
                reverseReplace((MapProperty) val);
            }
            newColl.add(val);
        }
        prop.setCollection(newColl);
    }

    private void reverseReplace(MapProperty prop)
    {
        Map newMap = null;
        try
        {
            newMap = (Map) prop.getObjectValue().getClass().newInstance();
        }
        catch (Exception e)
        {
            log.error("", e);
            return;
        }
        PropertyIterator iter = prop.valueIterator();
        while (iter.hasNext())
        {
            JMeterProperty val = iter.next();
            if (val instanceof TestElementProperty)
            {
                reverseReplace((TestElement) val.getObjectValue());
            }
            else if (val instanceof StringProperty)
            {
                val = substituteValues((StringProperty) val);
            }
            else if (val instanceof CollectionProperty)
            {
                reverseReplace((CollectionProperty) val);
            }
            else if (val instanceof MapProperty)
            {
                reverseReplace((MapProperty) val);
            }
            newMap.put(val.getName(), val);
        }
        prop.setMap(newMap);
    }

    private void undoReverseReplace(MapProperty prop)
    {
        Map newMap = null;
        try
        {
            newMap = (Map) prop.getObjectValue().getClass().newInstance();
        }
        catch (Exception e)
        {
            log.error("", e);
            return;
        }
        PropertyIterator iter = prop.valueIterator();
        while (iter.hasNext())
        {
            JMeterProperty val = iter.next();
            if (val instanceof TestElementProperty)
            {
                undoReverseReplace((TestElement) val.getObjectValue());
            }
            else if (val instanceof StringProperty)
            {
                val = substituteReferences((StringProperty) val);
            }
            else if (val instanceof CollectionProperty)
            {
                undoReverseReplace((CollectionProperty) val);
            }
            else if (val instanceof MapProperty)
            {
                undoReverseReplace((MapProperty) val);
            }
            newMap.put(val.getName(), val);
        }
        prop.setMap(newMap);
    }

    /**
         * Remove variables references and replace with the raw string values.
         */
    public void undoReverseReplace(CollectionProperty prop)
    {
        Collection newColl = null;
        try
        {
            newColl = (Collection) prop.getClass().newInstance();
        }
        catch (Exception e)
        {
            log.error("", e);
            return;
        }
        PropertyIterator iter = prop.iterator();
        while (iter.hasNext())
        {
            JMeterProperty val = iter.next();
            if (val instanceof TestElementProperty)
            {
                undoReverseReplace((TestElement) val.getObjectValue());
            }
            else if (val instanceof StringProperty)
            {
                val = substituteReferences((StringProperty) val);
            }
            else if (val instanceof CollectionProperty)
            {
                undoReverseReplace((CollectionProperty) val);
            }
            else if (val instanceof MapProperty)
            {
                undoReverseReplace((MapProperty) val);
            }
            newColl.add(val);
        }
        prop.setCollection(newColl);
    }

    /**
     * Remove variables references and replace with the raw string values.
     * @param el
     */
    public void undoReverseReplace(TestElement el)
    {
        PropertyIterator iter = el.propertyIterator();
        List newProps = new LinkedList();
        while (iter.hasNext())
        {
            JMeterProperty prop = iter.next();
            if (prop instanceof StringProperty)
            {
                newProps.add(substituteReferences((StringProperty) prop));
            }
            else if (prop instanceof TestElementProperty)
            {
                undoReverseReplace((TestElement) prop.getObjectValue());
            }
            else if (prop instanceof CollectionProperty)
            {
                undoReverseReplace((CollectionProperty) prop);
            }
            else if (prop instanceof MapProperty)
            {
                undoReverseReplace((MapProperty) prop);
            }
        }
        Iterator props = newProps.iterator();
        while (props.hasNext())
        {
            el.setProperty((JMeterProperty) props.next());
        }
    }

    /**
     * Replaces raw values with user-defined variable names.
     */
    public void reverseReplace(TestElement el)
    {
        PropertyIterator iter = el.propertyIterator();
        List newProps = new LinkedList();
        while (iter.hasNext())
        {
            JMeterProperty prop = iter.next();
            if (prop instanceof StringProperty)
            {
                newProps.add(substituteValues((StringProperty) prop));
            }
            else if (prop instanceof TestElementProperty)
            {
                reverseReplace((TestElement) prop.getObjectValue());
            }
            else if (prop instanceof CollectionProperty)
            {
                reverseReplace((CollectionProperty) prop);
            }
            else if (prop instanceof MapProperty)
            {
                reverseReplace((MapProperty) prop);
            }
        }
        Iterator props = newProps.iterator();
        while (props.hasNext())
        {
            el.setProperty((JMeterProperty) props.next());

        }
    }

    private StringProperty substituteValues(StringProperty prop)
    {
        Iterator iter = variables.keySet().iterator();
        String input = prop.getStringValue();
        while (iter.hasNext())
        {
            String key = (String) iter.next();
            String value = (String) variables.get(key);
            input = StringUtilities.substitute(input, value, "${" + key + "}");
        }
        return new StringProperty(prop.getName(), input);
    }

    private StringProperty substituteReferences(StringProperty prop)
    {
        Iterator iter = variables.keySet().iterator();
        String input = prop.getStringValue();
        while (iter.hasNext())
        {
            String key = (String) iter.next();
            String value = (String) variables.get(key);
            input = StringUtilities.substitute(input, "${" + key + "}", value);
        }
        return new StringProperty(prop.getName(), input);
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
            element.setProperty(new StringProperty("domain", "jakarta.apache.org"));
            List args = new ArrayList();
            args.add("username is jack");
            args.add("jacks_password");
            element.setProperty(new CollectionProperty("args", args));
            replacer.reverseReplace(element);
            assertEquals("${server}", element.getPropertyAsString("domain"));
            args = (List) element.getProperty("args").getObjectValue();
            assertEquals("${password}", ((JMeterProperty) args.get(1)).getStringValue());
        }

        public void testReplace() throws Exception
        {
            ValueReplacer replacer = new ValueReplacer();
            replacer.setUserDefinedVariables(variables.getUserDefinedVariables());
            TestElement element = new ConfigTestElement();
            element.setProperty(new StringProperty("domain", "${server}"));
            replacer.replaceValues(element);
            log.debug("domain property = " + element.getProperty("domain"));
            element.setRunningVersion(true);
            assertEquals("jakarta.apache.org", element.getPropertyAsString("domain"));
        }
    }
}
