package org.apache.jmeter.testelement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class TestElementCloner implements TestElementTraverser
{
    LinkedList stack = new LinkedList();
    Object currentProperty;

    TestElement clonedRoot = null;
    
    public TestElement getClonedElement()
    {
        return clonedRoot;
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#startTestElement(org.apache.jmeter.testelement.TestElement)
     */
    public void startTestElement(TestElement el)
    {
        try
        {
            if (clonedRoot == null)
            {
                clonedRoot = (TestElement) el.getClass().newInstance();
                stack.add(clonedRoot);

            }
            else
            {
                stack.add(el.getClass().newInstance());
            }
        }
        catch (InstantiationException e)
        {}
        catch (IllegalAccessException e)
        {}
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#endTestElement(org.apache.jmeter.testelement.TestElement)
     */
    public void endTestElement(TestElement el)
    {
        simplePropertyValue(stack.removeLast());
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#startProperty(java.lang.Object)
     */
    public void startProperty(Object key)
    {
        stack.add(new TestElementProperty(key));
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#endProperty(java.lang.Object)
     */
    public void endProperty(Object key)
    {
        TestElementProperty prop = (TestElementProperty)stack.removeLast();
        addProperty(prop,stack.getLast());
    }
    
    private void addProperty(TestElementProperty prop,Object parent)
    {
        if(parent instanceof TestElement)
        {
            ((TestElement)parent).setProperty(prop.getKey().toString(),prop.getValue());
        }
        else if(parent instanceof Map)
        {
            ((Map)parent).put(prop.getKey(),prop.getValue());
        }
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#simplePropertyValue(java.lang.Object)
     */
    public void simplePropertyValue(Object value)
    {
        try
        {
            Object parent = stack.getLast();
            if(parent instanceof TestElementProperty)
            {
                ((TestElementProperty)parent).setValue(value);
            }
            else if(parent instanceof Collection)
            {
                ((Collection)parent).add(value);
            }
        }
        catch (NoSuchElementException e)
        {}
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#startMap(java.util.Map)
     */
    public void startMap(Map map)
    {
        try
        {
            stack.add(map.getClass().newInstance());
        }
        catch (InstantiationException e)
        {}
        catch (IllegalAccessException e)
        {}
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#endMap(java.util.Map)
     */
    public void endMap(Map map)
    {
        Map cloned = (Map)stack.removeLast();
        simplePropertyValue(cloned);        
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#startCollection(java.util.Collection)
     */
    public void startCollection(Collection col)
    {
        try
        {
            stack.add(col.getClass().newInstance());
        }
        catch (InstantiationException e)
        {}
        catch (IllegalAccessException e)
        {}
    }

    /**
     * @see org.apache.jmeter.testelement.TestElementTraverser#endCollection(java.util.Collection)
     */
    public void endCollection(Collection col)
    {
        Collection cloned = (Collection)stack.removeLast();
        simplePropertyValue(cloned);
    }

}

