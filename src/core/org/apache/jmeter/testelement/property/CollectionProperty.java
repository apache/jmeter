package org.apache.jmeter.testelement.property;

import java.util.Collection;
import java.util.List;

import org.apache.jmeter.testelement.TestElement;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class CollectionProperty extends AbstractProperty
{

    protected Collection value;
    private Collection savedValue;

    public CollectionProperty(String name, Collection value)
    {
        super(name);
        this.value = normalizeList(value);
    }

    public CollectionProperty()
    {
        super();
    }

    public void remove(String prop)
    {
        PropertyIterator iter = iterator();
        while (iter.hasNext())
        {
            if (iter.next().getName().equals(prop))
            {
                iter.remove();
            }
        }
    }

    public void set(int index, String prop)
    {
        if (value instanceof List)
        {
            ((List) value).set(index, new StringProperty(prop, prop));
        }
    }

    public void set(int index, JMeterProperty prop)
    {
        if (value instanceof List)
        {
            ((List) value).set(index, prop);
        }
    }

    public JMeterProperty get(int row)
    {
        if (value instanceof List)
        {
            return (JMeterProperty) ((List) value).get(row);
        }
        else
        {
            return null;
        }
    }

    public void remove(int index)
    {
        if (value instanceof List)
        {
            ((List) value).remove(index);
        }
    }

    public void setObjectValue(Object v)
    {
        if (v instanceof Collection)
        {
            if (isRunningVersion())
            {
                savedValue = this.value;
            }
            value = normalizeList((Collection) v);
        }
    }

    public PropertyIterator iterator()
    {
        return new PropertyIteratorImpl(value);
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

    public int size()
    {
        return value.size();
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        CollectionProperty prop = (CollectionProperty) super.clone();
        prop.value = cloneCollection();
        return prop;
    }

    private Collection cloneCollection()
    {
        try
        {
            Collection newCol = (Collection) value.getClass().newInstance();
            PropertyIterator iter = iterator();
            while (iter.hasNext())
            {
                newCol.add(iter.next().clone());
            }
            return newCol;
        }
        catch (Exception e)
        {
            log.error("Couldn't clone collection", e);
            return value;
        }
    }

    public void setCollection(Collection coll)
    {
        value = coll;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#mergeIn(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {
        if (((CollectionProperty) prop).value == this.value)
        {
            return;
        }
        if (prop instanceof CollectionProperty)
        {
            PropertyIterator iter = ((CollectionProperty) prop).iterator();
            while (iter.hasNext())
            {
                value.add(iter.next());
            }
        }
        else
        {
            addProperty(prop);
        }
    }

    public void clear()
    {
        value.clear();
    }

    /**
     * Easy way to add properties to the list.
     * @param prop
     */
    public void addProperty(JMeterProperty prop)
    {
        if (value.size() == 0 || value.iterator().next().getClass().equals(prop.getClass()))
        {
            value.add(prop);
        }
    }

    public void addItem(Object item)
    {
        addProperty(convertObject(item));
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
            return value.iterator().next().getClass();
        }
        else
        {
            return NullProperty.class;
        }
    }

    public void setRunningVersion(boolean running)
    {
        super.setRunningVersion(running);
        PropertyIterator iter = iterator();
        while (iter.hasNext())
        {
            iter.next().setRunningVersion(running);
        }
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#recoverRunningVersion()
     */
    public void recoverRunningVersion(TestElement owner)
    {
        if (savedValue != null)
        {
            value = savedValue;
            savedValue = null;
        }
        PropertyIterator iter = iterator();
        while (iter.hasNext())
        {
            JMeterProperty prop = iter.next();
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

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#setTemporary(boolean, org.apache.jmeter.testelement.TestElement)
     */
    public void setTemporary(boolean temporary, TestElement owner)
    {
        super.setTemporary(temporary, owner);
        PropertyIterator iter = iterator();
        while (iter.hasNext())
        {
            iter.next().setTemporary(temporary, owner);
        }
    }

}
