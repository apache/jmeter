package org.apache.jmeter.testelement.property;

/**
 * @version $Revision$
 */
public class FloatProperty extends NumberProperty
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
    
    protected void setNumberValue(Number n)
    {
        value = n.floatValue();
    }
    
    protected void setNumberValue(String n) throws NumberFormatException
    {
        value = Float.parseFloat(n);
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return Float.toString(value);
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return new Float(value);
    }

    /**
     * @see Object#clone()
     */
    public Object clone() 
    {
        FloatProperty prop = (FloatProperty)super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * @see JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        return value > 0 ? true : false;
    }

    /**
     * @see JMeterProperty#getDoubleValue()
     */
    public double getDoubleValue()
    {
        return (double)value;
    }

    /**
     * @see JMeterProperty#getFloatValue()
     */
    public float getFloatValue()
    {
        return value;
    }

    /**
     * @see JMeterProperty#getIntValue()
     */
    public int getIntValue()
    {
        return (int)value;
    }

    /**
     * @see JMeterProperty#getLongValue()
     */
    public long getLongValue()
    {
        return (long)value;
    }
}
