package org.apache.jmeter.testelement;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.PropertyIteratorImpl;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
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
    transient private static Logger log =
            Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.elements");
    
    private Map propMap = Collections.synchronizedMap(new HashMap());
            
    private boolean runningVersion;

    /****************************************
     * !ToDo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public Object clone()
    {
        TestElement clonedElement = null;
        try
        {
            clonedElement = (TestElement)this.getClass().newInstance();
        }
        catch(Exception e){}
        
        PropertyIterator iter = propertyIterator();
        while(iter.hasNext())
        {
            clonedElement.setProperty((JMeterProperty)iter.next().clone());
        }
        return clonedElement;
    }
    
    public void clear()
    {
        propMap.clear();
    }

    public void removeProperty(String key)
    {
        propMap.remove(key);
    }

    public boolean equals(Object o)
    {
        if(o instanceof AbstractTestElement)
        {
            return ((AbstractTestElement)o).propMap.equals(propMap);
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
        mergeIn(el);
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param name  !ToDo (Parameter description)
     ***************************************/
    public void setName(String name)
    {
        setProperty(new StringProperty(TestElement.NAME, name));
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    public String getName()
    {
        return getProperty(TestElement.NAME).getStringValue();
    }

    /****************************************
     * Get the named property.  If it doesn't
     * exist, a NullProperty object is
     * returned.
     * **************************************/
    public JMeterProperty getProperty(String key)
    {
        JMeterProperty prop = (JMeterProperty)propMap.get(key);
        if(prop == null)
        {
            prop = new NullProperty(key);
        }
        return prop;
    }
    
    public void traverse(TestElementTraverser traverser)
    {
        PropertyIterator iter = propertyIterator();
        traverser.startTestElement(this);
        while (iter.hasNext())
        {
            traverseProperty(traverser,iter.next());
        }
        traverser.endTestElement(this);
    }

    protected void traverseProperty(TestElementTraverser traverser, JMeterProperty value)
    {
        traverser.startProperty(value);
        if(value instanceof TestElementProperty)
        {
            ((TestElement)value.getObjectValue()).traverse(traverser);
        }
        else if(value instanceof CollectionProperty)
        {            
            traverseCollection((CollectionProperty)value,traverser);
        }
        else if(value instanceof MapProperty)
        {
            traverseMap((MapProperty)value,traverser);
        }
        traverser.endProperty(value);
    }
    
    protected void traverseMap(MapProperty map,TestElementTraverser traverser)
    {
        PropertyIterator iter = map.valueIterator();
        while (iter.hasNext())
        {
            traverseProperty(traverser,iter.next());            
        }
    }
    
    protected void traverseCollection(CollectionProperty col,TestElementTraverser traverser)
    {
        PropertyIterator iter = col.iterator();
        while (iter.hasNext())
        {
            traverseProperty(traverser,iter.next());           
        }
    }

    public int getPropertyAsInt(String key)
    {
        return getProperty(key).getIntValue();
    }

    public boolean getPropertyAsBoolean(String key)
    {
        return getProperty(key).getBooleanValue();
    }

    public float getPropertyAsFloat(String key)
    {
        return getProperty(key).getFloatValue();
    }

    public long getPropertyAsLong(String key)
    {
        return getProperty(key).getLongValue();
    }

    public double getPropertyAsDouble(String key)
    {
        return getProperty(key).getDoubleValue();
    }

    public String getPropertyAsString(String key)
    {
        return getProperty(key).getStringValue();
    }
    
    public void addProperty(JMeterProperty property)
    {
        if(isRunningVersion())
        {
            property.setTemporary(true,this);
        }
        else
        {
            property.clearTemporary(this);
        }
        JMeterProperty prop = getProperty(property.getName());
        if(!(prop instanceof NullProperty) && !prop.getStringValue().equals(""))
        {
            prop.mergeIn(property);
        }
        else
        {
            propMap.put(property.getName(),property);
        }
    }
    
    public void setProperty(JMeterProperty property)
    {
        if(isRunningVersion())
        {
            if(getProperty(property.getName()) instanceof NullProperty)
            {
                addProperty(property);
            }            
            return;
        }
        else
        {
            propMap.put(property.getName(),property);
        }
    }
    
    public void setProperty(String name,String value)
    {
        setProperty(new StringProperty(name,value));
    }
    
    public PropertyIterator propertyIterator()
    {
        return new PropertyIteratorImpl(propMap.values());
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param element  !ToDo (Parameter description)
     ***************************************/
    protected void mergeIn(TestElement element)
    {
       PropertyIterator iter = element.propertyIterator();
       while(iter.hasNext())
       {
           JMeterProperty prop = iter.next();
           addProperty(prop);            
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
        this.runningVersion = runningVersion;
        PropertyIterator iter = propertyIterator();
        while(iter.hasNext())
        {
            iter.next().setRunningVersion(true);
        }
    }
    
    public void recoverRunningVersion()
    {
        PropertyIterator iter = propertyIterator();
        while(iter.hasNext())
        {
            JMeterProperty prop = iter.next();
            if(prop.isTemporary(this))
            {
                iter.remove();
                prop.clearTemporary(this);
            }
            else
            {
                prop.recoverRunningVersion(this);
            }
        }
    }
}
