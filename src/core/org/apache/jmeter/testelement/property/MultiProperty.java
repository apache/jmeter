/*
 * Created on May 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * For JMeterProperties that hold multiple properties within, provides a simple interface
 * for retrieving a property iterator for the sub values.
 */
public abstract class MultiProperty extends AbstractProperty
{
    
    public MultiProperty(String name)
    {
        super(name);
    }
    
    public MultiProperty()
    {
        super();
    }
    /**
     * Get the property iterator to iterate through the sub-values of this JMeterProperty
     * @return
     */
    public abstract PropertyIterator iterator();
    
    /**
     * Add a property to the collection.
     * @param prop
     */
    public abstract void addProperty(JMeterProperty prop);
    
    /**
     * Clear away all values in the property.
     *
     */
    public abstract void clear();

    public void setTemporary(boolean temporary, TestElement owner)
    {
        super.setTemporary(temporary, owner);
        PropertyIterator iter = iterator();
        while (iter.hasNext())
        {
            iter.next().setTemporary(temporary, owner);
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

    protected void recoverRunningVersionOfSubElements(TestElement owner)
    {
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

    public void mergeIn(JMeterProperty prop)
    {
        if (prop.getObjectValue() == getObjectValue())
        {
            return;
        }
        if (prop instanceof MultiProperty)
        {
            PropertyIterator iter = ((MultiProperty) prop).iterator();
            while (iter.hasNext())
            {
                addProperty(iter.next());
            }
        }
        else
        {
            addProperty(prop);
        }
    }
}
