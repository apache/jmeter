package org.apache.jmeter.testelement;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.PropertyIteratorImpl;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public abstract class AbstractTestElement implements TestElement,Serializable
{
    private Map testInfo = Collections.synchronizedMap(new HashMap());
    transient private static Logger log =
            Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.elements");
    private List temporaryMembers;
            
    private boolean runningVersion;

    /****************************************
     * !ToDo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public Object clone()
    {
        TestElementCloner cloner = new TestElementCloner();
        this.traverse(cloner);
        return cloner.getClonedElement();
    }
    
    public void clear()
    {
        testInfo.clear();
    }

    public void removeProperty(String key)
    {
        testInfo.remove(key);
    }

    public boolean equals(Object o)
    {
        if(o instanceof AbstractTestElement)
        {
            return ((AbstractTestElement)o).testInfo.equals(testInfo);
        }
        else
        {
            return false;
        }
    }

    /****************************************
     * !ToDo
     *
     *@param el  !ToDo
     ***************************************/
    public void addTestElement(TestElement el)
    {
        if(isRunningVersion())
        {
            Iterator iter = temporaryMembers.iterator();
            while (iter.hasNext())
            {
                TestElement item = (TestElement)iter.next();
                if(item.getClass().equals(el.getClass()))
                {
                    item.addTestElement(el);
                    return;
                }                
            }
            temporaryMembers.add(el);
        }        
        else if(el.getClass().equals(this.getClass()))
        {
            mergeIn(el);
        }
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param name  !ToDo (Parameter description)
     ***************************************/
    public void setName(String name)
    {
        setProperty(TestElement.NAME, name);
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public String getName()
    {
        return (String)getProperty(TestElement.NAME);
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param key   !ToDo (Parameter description)
     *@param prop  !ToDo (Parameter description)
     ***************************************/
    public void setProperty(String key, Object prop)
    {
        testInfo.put(key, prop);
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@param key  !ToDo (Parameter description)
     *@return     !ToDo (Return description)
     ***************************************/
    public Object getProperty(String key)
    {
        return testInfo.get(key);
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public Collection getPropertyNames()
    {
        return testInfo.keySet();
    }
    
    public void traverse(TestElementTraverser traverser)
    {
        Iterator iter = getPropertyNames().iterator();
        traverser.startTestElement(this);
        while (iter.hasNext())
        {
            String key = (String)iter.next();
            Object value = getProperty(key);
            traverseObject(traverser, key,value);
        }
        traverser.endTestElement(this);
    }

    protected void traverseObject(TestElementTraverser traverser, Object key,Object value)
    {
        traverser.startProperty(key);
        traverseObject(traverser, value);
        traverser.endProperty(key);
    }

    protected void traverseObject(TestElementTraverser traverser, Object value)
    {
        if(value instanceof TestElement)
        {
            ((TestElement)value).traverse(traverser);
        }
        else if(value instanceof Collection)
        {            
            traverseCollection((Collection)value,traverser);
        }
        else if(value instanceof Map)
        {
            traverseMap((Map)value,traverser);
        }
        else
        {
            traverser.simplePropertyValue(value);
        }
    }
    
    protected void traverseMap(Map map,TestElementTraverser traverser)
    {
        traverser.startMap(map);
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext())
        {
            Object key = iter.next();
            Object value = map.get(key);
            traverseObject(traverser,key,value);            
        }
        traverser.endMap(map);
    }
    
    protected void traverseCollection(Collection col,TestElementTraverser traverser)
    {
        traverser.startCollection(col);
        Iterator iter = col.iterator();
        while (iter.hasNext())
        {
            traverseObject(traverser,iter.next());           
        }
        traverser.endCollection(col);
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param newObject  !ToDo (Parameter description)
     ***************************************/
    protected void configureClone(TestElement newObject)
    {
        Iterator iter = getPropertyNames().iterator();
        while(iter.hasNext())
        {
            String key = (String)iter.next();
            Object value = getProperty(key);
            if(value instanceof TestElement)
            {
                newObject.setProperty(key, ((TestElement)value).clone());
            }
            else if(value instanceof Collection)
            {
                try
                {
                    newObject.setProperty(key,cloneCollection(value));
                }
                catch(Exception e)
                {
                    log.error("",e);
                }
            }
            else
            {
                newObject.setProperty(key, value);
            }
        }
    }

    protected Collection cloneCollection(Object value)
            throws InstantiationException,
                   IllegalAccessException,
                   ClassNotFoundException
    {
        Iterator collIter = ((Collection)value).iterator();
        Collection newColl = (Collection)value.getClass().newInstance();
        while(collIter.hasNext())
        {
            Object val = collIter.next();
            if(val instanceof TestElement)
            {
                val = ((TestElement)val).clone();
            }
            else if(val instanceof Collection)
            {
                try
                {
                    val = cloneCollection(val);
                }
                catch(Exception e)
                {
                    continue;
                }
            }
            newColl.add(val);
        }
        return newColl;
    }

    private long getLongValue(Object bound)
    {
        if (bound == null)
        {
            return (long)0;
        }
        else if (bound instanceof Long)
        {
            return ((Long) bound).longValue();
        }
        else
        {
            return Long.parseLong((String) bound);
        }
    }

    private float getFloatValue(Object bound)
    {
        if (bound == null)
        {
                return (float)0;
        }
        else if (bound instanceof Float)
        {
            return ((Float) bound).floatValue();
        }
        else
        {
            return Float.parseFloat((String) bound);
        }
    }

    private double getDoubleValue(Object bound)
    {
        if (bound == null)
        {
            return (double)0;
        }
        else if (bound instanceof Double)
        {
            return ((Double) bound).doubleValue();
        }
        else
        {
            return Double.parseDouble((String) bound);
        }
    }

    private String getStringValue(Object bound)
    {
        if (bound == null)
        {
            return "";
        }
        else {
            return bound.toString();
        }
    }
    
    private Collection getCollectionValue(Object value)
    {
        if(value == null)
        {
            return new LinkedList();
        }
        if(value instanceof Collection)
        {
            return (Collection)value;
        }
        if(value instanceof Object[])
        {
            return Arrays.asList((Object[])value);
        }
        List newList = new LinkedList();
        newList.add(value);
        return newList;
    }

    private int getIntValue(Object bound)
    {
        if (bound == null)
        {
            return (int)0;
        }
        else if (bound instanceof Integer)
        {
            return ((Integer) bound).intValue();
        }
        else
        {
            try
            {
                return Integer.parseInt((String) bound);
            }
            catch(NumberFormatException e)
            {
                return 0;
            }
        }
    }

    private boolean getBooleanValue(Object bound)
    {
        if (bound == null)
        {
            return false;
        }
        else if (bound instanceof Boolean)
        {
            return ((Boolean) bound).booleanValue();
        }
        else
        {
            return new Boolean((String) bound).booleanValue();
        }
    }

    public int getPropertyAsInt(String key)
    {
        return getIntValue(getProperty(key));
    }

    public boolean getPropertyAsBoolean(String key)
    {
        return getBooleanValue(getProperty(key));
    }

    public float getPropertyAsFloat(String key)
    {
        return getFloatValue(getProperty(key));
    }

    public long getPropertyAsLong(String key)
    {
        return getLongValue(getProperty(key));
    }

    public double getPropertyAsDouble(String key)
    {
        return getDoubleValue(getProperty(key));
    }

    public String getPropertyAsString(String key)
    {
        return getStringValue(getProperty(key));
    }
    
    public Collection getPropertyAsCollection(String key)
    {
        return getCollectionValue(getProperty(key));
    }
    
    public void addProperty(JMeterProperty property)
    {
    }
    
    public PropertyIterator propertyIterator()
    {
        return new PropertyIteratorImpl(testInfo.values());
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param element  !ToDo (Parameter description)
     ***************************************/
    protected void mergeIn(TestElement element)
    {
        Iterator iter = element.getPropertyNames().iterator();
        while(iter.hasNext())
        {
            String key = (String)iter.next();
            Object value = element.getProperty(key);
            if(getProperty(key) == null || getProperty(key).equals(""))
            {
                setProperty(key, value);
                continue;
            }
            if(value instanceof TestElement)
            {
                if(getProperty(key) == null)
                {
                    setProperty(key,value);
                }
                else if(getProperty(key) instanceof TestElement)
                {
                    ((TestElement)getProperty(key)).addTestElement((TestElement)value);
                }
            }
            else if(value instanceof Collection)
            {
                Collection localCollection = (Collection)getProperty(key);
                if(localCollection == null)
                {
                    setProperty(key,value);
                }
                else
                {
                    // Remove any repeated elements:
                    Iterator iter2 = ((Collection)value).iterator();
                    while(iter2.hasNext())
                    {
                        Object item = iter2.next();
                        if(!localCollection.contains(item))
                        {
                            localCollection.remove(item);
                        }
                    }
                    // Add all elements now:
                    iter2 = ((Collection)value).iterator();
                    while(iter2.hasNext())
                    {
                        localCollection.add(iter2.next());
                    }
                }
            }
        }
    }
    /**
     * Returns the runningVersion.
     * @return boolean
     */
    public boolean isRunningVersion()
    {
        return runningVersion;
    }

    /**
     * Sets the runningVersion.
     * @param runningVersion The runningVersion to set
     */
    public void setRunningVersion(boolean runningVersion)
    {
        if(runningVersion)
        {
            temporaryMembers = new LinkedList();
        }
        else
        {
            temporaryMembers = null;
        }
        this.runningVersion = runningVersion;
    }
    
    public void recoverRunningVersion()
    {
        temporaryMembers.clear();
    }
}
