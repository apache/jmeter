package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class BooleanProperty extends AbstractProperty
{
    boolean value;
    boolean savedValue;
    
    public BooleanProperty(String name,boolean v)
    {
        super(name);
        value = v;
    }
    
    public BooleanProperty()
    {
        super();
    }
    
    public void setObjectValue(Object v)
    {
        if(v instanceof Boolean)
        {
            value = ((Boolean)v).booleanValue();
        }
        else
        {
            value = Boolean.valueOf(v.toString()).booleanValue();
        }
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value ? "true" : "false";
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return new Boolean(value);
    }

    /**
     * @see Object#clone()
     */
    public Object clone()
    {
        BooleanProperty prop = (BooleanProperty)super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * @see JMeterProperty#getBooleanValue()
     */
    public boolean getBooleanValue()
    {
        return value;
    }
    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.property.JMeterProperty#setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean runningVersion)
    {
        savedValue = value;
        super.setRunningVersion(runningVersion);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.testelement.property.JMeterProperty#recoverRunningVersion(org.apache.jmeter.testelement.TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        value = savedValue;
    }

}
