package org.apache.jmeter.testelement.property;

import java.util.Collection;

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

    public CollectionProperty(String name, Collection value)
    {
        super(name);
        this.value = value;
    }

    public CollectionProperty()
    {
        super();
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
        prop.value = value;
        return prop;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#mergeIn(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {
        if (prop instanceof CollectionProperty)
        {
            PropertyIterator iter = ((CollectionProperty) prop).iterator();
            while (iter.hasNext())
            {
                value.add(iter.next());
            }
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
        PropertyIterator iter = iterator();
        while (iter.hasNext())
        {
            if (iter.next().isTemporary(owner))
            {
                iter.remove();
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
