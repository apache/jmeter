package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * A null property.
 * 
 * @version $Revision$
 */
public class NullProperty extends AbstractProperty
{
    JMeterProperty tempValue;
    
    public NullProperty(String name)
    {
        super(name);
    }
    
    public NullProperty()
    {
        super();
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        if(tempValue != null)
        {
            return tempValue.getStringValue();
        }        
        return "";
    }
    
    public void setObjectValue(Object v)
    {
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return null;
    }

    /**
     * @see JMeterProperty#isRunningVersion()
     */
    public boolean isRunningVersion()
    {
        return false;
    }

    /**
     * @see JMeterProperty#isTemporary(TestElement)
     */
    public boolean isTemporary(TestElement owner)
    {
        return true;
    }

    /**
     * @see JMeterProperty#mergeIn(JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {
        tempValue = prop;
    }

    /**
     * @see Object#clone()
     */
    public Object clone()
    {
        return this;
    }

    /**
     * @see JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        return false;
    }

    /**
     * @see JMeterProperty#getDoubleValue()
     */
    public double getDoubleValue()
    {
        return 0;
    }

    /**
     * @see JMeterProperty#getFloatValue()
     */
    public float getFloatValue()
    {
        return 0;
    }

    /**
     * @see JMeterProperty#getIntValue()
     */
    public int getIntValue()
    {
        return 0;
    }

    /**
     * @see JMeterProperty#getLongValue()
     */
    public long getLongValue()
    {
        return 0;
    }

    /**
     * @see JMeterProperty#recoverRunningVersion(TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        tempValue = null;
    }

}
