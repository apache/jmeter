/*
 * Created on May 5, 2003
 */
package org.apache.jmeter.testelement.property;

/**
 * @author ano ano
 * @version $Revision$
 */
public abstract class NumberProperty extends AbstractProperty
{
    public NumberProperty()
    {
        super();
    }

    public NumberProperty(String name)
    {
        super(name);
    }

    /**
     * Set the value of the property with a Number object.
     */
    protected abstract void setNumberValue(Number n);

    /**
     * Set the value of the property with a String object.
     */
    protected abstract void setNumberValue(String n)
        throws NumberFormatException;

    public void setObjectValue(Object v)
    {
        if (v instanceof Number)
        {
            setNumberValue((Number) v);
        }
        else
        {
            try
            {
                setNumberValue(v.toString());
            }
            catch (RuntimeException e)
            {
            }
        }
    }
    
    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object arg0)
    {
        if(arg0 instanceof JMeterProperty)
        {
            double compareValue =
                getDoubleValue() - ((JMeterProperty) arg0).getDoubleValue();

            if(compareValue < 0)
            {
                return -1;
            }
            else if(compareValue == 0)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        else
        {
            return -1;
        }
    }
}
