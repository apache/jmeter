package org.apache.jmeter.testelement.property;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class DoubleProperty extends NumberProperty
{

     double value;
    
    public DoubleProperty(String name,double value)
    {
        super(name);
        this.value = value;
    }
    
    public DoubleProperty()
    {
    }
    
    public void setValue(float value)
    {
        this.value = value;
    }
    
    protected void setNumberValue(Number n)
        {
            value = n.doubleValue();
        }
    
        protected void setNumberValue(String n) throws NumberFormatException
        {
            value = Double.parseDouble(n);
        }
    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return Double.toString(value);
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return new Double(value);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() 
    {
        DoubleProperty prop = (DoubleProperty)super.clone();
        prop.value = value;
        return prop;
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
        return value;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getFloatValue()
     */
    public float getFloatValue()
    {
        return (float)value;
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
