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
        this.value = value;
    }

    public MapProperty()
    {
        super();
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
        prop.value = value;
        return value;
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
                value.remove(name);
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
