package org.apache.jmeter.testelement.property;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class FloatProperty extends AbstractProperty
{
    float value;
    
    public FloatProperty(String name,float value)
    {
        super(name);
        this.value = value;
    }
    
    public FloatProperty()
    {
    }
    
    public void setValue(float value)
    {
        this.value = value;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return Float.toString(value);
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return new Float(value);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() 
    {
        FloatProperty prop = (FloatProperty)super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0)
    {
        if(arg0 instanceof JMeterProperty)
        {
            int argValue = ((JMeterProperty)arg0).getIntValue();
            if(value < argValue)
            {
                return -1;
            }
            else if(value == argValue)
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

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        return value > 0 ? true : false;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getDoubleValue()
     */
    public double getDoubleValue()
    {
        return (double)value;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getFloatValue()
     */
    public float getFloatValue()
    {
        return value;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getIntValue()
     */
    public int getIntValue()
    {
        return (int)value;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getLongValue()
     */
    public long getLongValue()
    {
        return (long)value;
    }

}
