package org.apache.jmeter.testelement.property;

import java.util.Map;

import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class MapProperty extends MultiProperty
{
    Map value;
    Map savedValue = null;

    public MapProperty(String name, Map value)
    {
        super(name);
        this.value = normalizeMap(value);
    }

    public MapProperty()
    {
        super();
    }

    public boolean equals(Object o)
    {
        if (o instanceof MapProperty)
        {
            if (value != null)
            {
                return value.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    public void setObjectValue(Object v)
    {
        if (v instanceof Map)
        {
            setMap((Map) v);
        }
    }

    public void addProperty(JMeterProperty prop)
    {
        addProperty(prop.getName(),prop);
    }

    public JMeterProperty get(String key)
    {
        return (JMeterProperty) value.get(key);
    }

    /**
     * Figures out what kind of properties this collection is holding and
     * returns the class type.
     * @see AbstractProperty#getPropertyType()
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
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value.toString();
    }

    /**
     * @see JMeterProperty#getObjectValue()
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
        return prop;
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

    public void addProperty(String name, JMeterProperty prop)
    {
        if (!value.containsKey(name))
       {
           value.put(name, prop);
       }
    }

    public void setMap(Map newMap)
    {
        if (isRunningVersion())
        {
            savedValue = this.value;
        }
        value = normalizeMap(newMap);
    }

    /**
     * @see JMeterProperty#recoverRunningVersion(TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        if (savedValue != null)
        {
            value = savedValue;
            savedValue = null;
        }
        recoverRunningVersionOfSubElements(owner);
    }

    public void clear()
    {
        value.clear();
    }

    /* (non-Javadoc)
     * @see MultiProperty#iterator()
     */
    public PropertyIterator iterator()
    {
        return valueIterator();
    }

}
