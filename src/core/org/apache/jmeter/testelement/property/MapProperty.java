package org.apache.jmeter.testelement.property;

import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.testelement.TestElement;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class MapProperty extends AbstractProperty
{
    Map value;

    public MapProperty(String name, Map value)
    {
        super(name);
        this.value = normalizeMap(value);
    }

    public MapProperty()
    {
        super();
    }

    public void setObjectValue(Object v)
    {
        if (v instanceof Map)
        {
            value = normalizeMap((Map) v);
        }
    }
    
    public void addProperty(JMeterProperty prop)
    {
        if(value.size() == 0 || valueIterator().next().getClass().equals(prop.getClass()))
        {
            value.put(prop.getName(),prop);
        }
    }
    
    public JMeterProperty get(String key)
    {
        return (JMeterProperty)value.get(key);
    }

    /**
         * Figures out what kind of properties this collection is holding and
         * returns the class type.
         * @see org.apache.jmeter.testelement.property.AbstractProperty#getPropertyType()
         */
    protected Class getPropertyType()
    {
        if (value.size() > 0)
        {
            return valueIterator().next().getClass();
        }
        else
        {
            return NullProperty.class;
        }
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value.toString();
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return value;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        MapProperty prop = (MapProperty) super.clone();
        prop.value = cloneMap();
        return value;
    }

    private Map cloneMap()
    {
        try
        {
            Map newCol = (Map) value.getClass().newInstance();
            PropertyIterator iter = valueIterator();
            while (iter.hasNext())
            {
                JMeterProperty item = iter.next();
                newCol.put(item.getName(), item.clone());
            }
            return newCol;
        }
        catch (Exception e)
        {
            log.error("Couldn't clone map", e);
            return value;
        }
    }

    public PropertyIterator valueIterator()
    {
        return new PropertyIteratorImpl(value.values());
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#mergeIn(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {
        if (((MapProperty) prop).value == value)
        {
            return;
        }
        if (prop instanceof MapProperty)
        {
            PropertyIterator iter = ((MapProperty) prop).valueIterator();
            while (iter.hasNext())
            {
                JMeterProperty subProp = iter.next();
                if (!value.containsKey(subProp.getName()))
                {
                    value.put(subProp.getName(), subProp);
                }
            }
        }
        else
        {
            addProperty(prop.getName(), prop);
        }
    }

    public void addProperty(String name, JMeterProperty prop)
    {
        if (value.size() == 0 || value.values().iterator().next().getClass().equals(prop.getClass()))
        {
            value.put(name, prop);
        }
    }

    public void setMap(Map newMap)
    {
        value = newMap;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#recoverRunningVersion(org.apache.jmeter.testelement.TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        Iterator iter = value.keySet().iterator();
        while (iter.hasNext())
        {
            String name = (String) iter.next();
            JMeterProperty prop = (JMeterProperty) value.get(name);
            if (prop.isTemporary(owner))
            {
                iter.remove();
            }
            else
            {
                prop.recoverRunningVersion(owner);
            }
        }
    }

    public void setRunningVersion(boolean running)
    {
        super.setRunningVersion(running);
        PropertyIterator iter = valueIterator();
        while (iter.hasNext())
        {
            iter.next().setRunningVersion(running);
        }
    }

    /**
         * @see org.apache.jmeter.testelement.property.JMeterProperty#setTemporary(boolean, org.apache.jmeter.testelement.TestElement)
         */
    public void setTemporary(boolean temporary, TestElement owner)
    {
        super.setTemporary(temporary, owner);
        PropertyIterator iter = valueIterator();
        while (iter.hasNext())
        {
            iter.next().setTemporary(temporary, owner);
        }
    }

}
