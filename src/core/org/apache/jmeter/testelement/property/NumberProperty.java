/*
 * Created on May 5, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.testelement.property;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class NumberProperty extends AbstractProperty
{

    public NumberProperty(String name)
    {
        super(name);
    }

    public NumberProperty()
    {
        super();
    }

    /**
     * Set the value of the property with a Number object.
     * @param n
     */
    protected abstract void setNumberValue(Number n);

    /**
     * Set the value of the property with a String object.
     * @param n
     * @throws NumberFormatException
     */
    protected abstract void setNumberValue(String n) throws NumberFormatException;

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
            {}
        }
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0)
    {
        if(arg0 instanceof JMeterProperty)
        {
            double compareValue = getDoubleValue() - ((JMeterProperty)arg0).getDoubleValue();
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
