package org.apache.jmeter.testelement.property;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class IntegerProperty extends NumberProperty
{
    int value;

    public IntegerProperty(String name, int value)
    {
        super(name);
        this.value = value;
    }

    public IntegerProperty(String name)
    {
        super(name);
    }

    public IntegerProperty()
    {
        super();
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    protected void setNumberValue(Number n)
    {
        value = n.intValue();
    }

    protected void setNumberValue(String n) throws NumberFormatException
    {
        value = Integer.parseInt(n);
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return Integer.toString(value);
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return new Integer(value);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        IntegerProperty prop = (IntegerProperty) super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        return getIntValue() > 0 ? true : false;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getDoubleValue()
     */
    public double getDoubleValue()
    {
        return (double) value;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getFloatValue()
     */
    public float getFloatValue()
    {
        return (float) value;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getIntValue()
     */
    public int getIntValue()
    {
        return value;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getLongValue()
     */
    public long getLongValue()
    {
        return (long) value;
    }

}
