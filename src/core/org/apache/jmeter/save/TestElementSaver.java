package org.apache.jmeter.save;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestElementTraverser;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class TestElementSaver implements TestElementTraverser, SaveServiceConstants
{
    String name;
    LinkedList stack = new LinkedList();

    DefaultConfiguration rootConfig = null;

    public TestElementSaver(String name)
    {
        this.name = name;
    }

    public Configuration getConfiguration()
    {
        return rootConfig;
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#startTestElement(org.apache.jmeter.testelement.TestElement)
     */
    public void startTestElement(TestElement el)
    {
        DefaultConfiguration config = new DefaultConfiguration("testelement", "testelement");
        config.setAttribute("class", el.getClass().getName());
        if (rootConfig == null)
        {
            rootConfig = config;
            if (name != null && name.length() > 0)
            {
                rootConfig.setAttribute("name", name);
            }
        }
        else
        {
            setConfigName(config);
        }
        stack.add(config);
    }

    public void setConfigName(DefaultConfiguration config)
    {
        if (!(stack.getLast() instanceof Configuration))
        {
            Object key = stack.removeLast();
            config.setAttribute("name", key.toString());
        }
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#endTestElement(org.apache.jmeter.testelement.TestElement)
     */
    public void endTestElement(TestElement el)
    {
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#simplePropertyValue(java.lang.Object)
     */
    public void simplePropertyValue(JMeterProperty value)
    {
        try
        {
            Object parent = stack.getLast();
            if (!(parent instanceof Configuration))
            {
                DefaultConfiguration config = new DefaultConfiguration("property", "property");
                config.setValue(value != null ? value.toString() : "");
                config.setAttribute("name", parent.toString());
                config.setAttribute(XML_SPACE, PRESERVE);
                stack.removeLast();
                stack.add(config);
            }
            if (parent instanceof DefaultConfiguration && value instanceof Configuration)
            {
                ((DefaultConfiguration) parent).addChild((Configuration) value);
            }
            else if (parent instanceof DefaultConfiguration && !(value instanceof Configuration))
            {
                DefaultConfiguration config = new DefaultConfiguration("string", "string");
                config.setValue(value.toString());
                config.setAttribute(XML_SPACE, PRESERVE);
                ((DefaultConfiguration) parent).addChild(config);
            }
        }
        catch (NoSuchElementException e)
        {}
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#startMap(java.util.Map)
     */
    public void startMap(MapProperty map)
    {
        DefaultConfiguration config = new DefaultConfiguration("map", "map");
        config.setAttribute("class", map.getObjectValue().getClass().getName());
        config.setAttribute("name",map.getName());
        config.setAttribute("propType",map.getClass().getName());
        stack.add(config);
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#endMap(java.util.Map)
     */
    public void endMap(MapProperty map)
    {
        finishConfig();
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#startCollection(java.util.Collection)
     */
    public void startCollection(CollectionProperty col)
    {
        DefaultConfiguration config = new DefaultConfiguration("collection", "collection");
        config.setAttribute("class", col.getObjectValue().getClass().getName());
        config.setAttribute("name",col.getName());
        config.setAttribute("propType",col.getClass().getName());
        stack.add(config);
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#endCollection(java.util.Collection)
     */
    public void endCollection(CollectionProperty col)
    {
        finishConfig();
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#endProperty(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void endProperty(JMeterProperty key)
    {
        finishConfig();
    }

    private void finishConfig()
    {
        if (stack.size() > 1)
        {
            Configuration config = (Configuration) stack.removeLast();
            ((DefaultConfiguration) stack.getLast()).addChild(config);
        }
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#startProperty(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void startProperty(JMeterProperty key)
    {
        if (key instanceof CollectionProperty)
        {
            startCollection((CollectionProperty) key);
        }
        else if (key instanceof MapProperty)
        {
            startMap((MapProperty) key);
        }
        else if (key instanceof TestElementProperty)
        {
            stack.addLast(key.getName());
        }
        else
        {
            DefaultConfiguration config = new DefaultConfiguration("property", "property");
            config.setValue(key.getStringValue());
            config.setAttribute("name", key.getName());
            config.setAttribute("propType", key.getClass().getName());
            config.setAttribute(XML_SPACE, PRESERVE);
            stack.addLast(config);
        }

    }

}
