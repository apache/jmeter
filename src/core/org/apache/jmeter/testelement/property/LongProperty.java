package org.apache.jmeter.testelement.property;


/**
 * @version $Revision$
 */
public class LongProperty extends NumberProperty
{
    long value;

    public LongProperty(String name, long value)
    {
        super(name);
        this.value = value;
    }

    public LongProperty()
    {
        super();
    }

    public void setValue(int value)
    {
        this.value = value;
    }

    protected void setNumberValue(Number n)
    {
        value = n.longValue();
    }

    protected void setNumberValue(String n) throws NumberFormatException
    {
        value = Long.parseLong(n);
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return Long.toString(value);
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return new Long(value);
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        LongProperty prop = (LongProperty) super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * @see JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        return getLongValue() > 0 ? true : false;
    }

    /**
     * @see JMeterProperty#getDoubleValue()
     */
    public double getDoubleValue()
    {
        return (double) value;
    }

    /**
     * @see JMeterProperty#getFloatValue()
     */
    public float getFloatValue()
    {
        return (float) value;
    }

    /**
     * @see JMeterProperty#getIntValue()
     */
    public int getIntValue()
    {
        return (int) value;
    }

    /**
     * @see JMeterProperty#getLongValue()
     */
    public long getLongValue()
    {
        return value;
    }
}
