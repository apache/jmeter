package org.apache.jmeter.testelement.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class CollectionProperty extends MultiProperty
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
        value = new ArrayList();
    }

    public boolean equals(Object o)
    {
        if (o instanceof CollectionProperty)
        {
            if (value != null)
            {
                return value.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
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
            setCollection((Collection) v);
        }

    }

    public PropertyIterator iterator()
    {
        return new PropertyIteratorImpl(value);
    }

    /* (non-Javadoc)
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value.toString();
    }

    /* (non-Javadoc)
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return value;
    }

    public int size()
    {
        return value.size();
    }

    /* (non-Javadoc)
     * @see Object#clone()
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
        if (isRunningVersion())
        {
            savedValue = this.value;
        }
        value = normalizeList(coll);
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
        value.add(prop);
    }

    public void addItem(Object item)
    {
        addProperty(convertObject(item));
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
            return value.iterator().next().getClass();
        }
        else
        {
            return NullProperty.class;
        }
    }

    /* (non-Javadoc)
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
    
    public static class Test extends JMeterTestCase
    {
        public Test(String name)
        {
            super(name);
        }
        
        public void testAddingProperties() throws Exception
        {
            CollectionProperty coll = new CollectionProperty();
            coll.addItem("joe");
            coll.addProperty(new FunctionProperty());
            assertEquals("joe",coll.get(0).getName());
            assertEquals(
                "org.apache.jmeter.testelement.property.FunctionProperty",
                coll.get(1).getClass().getName());
        }
    }

}
