package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * A null property.
 */
public class NullProperty extends AbstractProperty
{
    
    public NullProperty(String name)
    {
        super(name);
    }
    
    public NullProperty()
    {
        super();
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return "";
    }
    
    public void setObjectValue(Object v)
    {
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return null;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#isRunningVersion()
     */
    public boolean isRunningVersion()
    {
        return false;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#isTemporary(org.apache.jmeter.testelement.TestElement)
     */
    public boolean isTemporary(TestElement owner)
    {
        return true;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#mergeIn(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        return this;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        return false;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getDoubleValue()
     */
    public double getDoubleValue()
    {
        return 0;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getFloatValue()
     */
    public float getFloatValue()
    {
        return 0;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getIntValue()
     */
    public int getIntValue()
    {
        return 0;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getLongValue()
     */
    public long getLongValue()
    {
        return 0;
    }

}
