package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class StringProperty extends AbstractProperty
{
    String value;
    String savedValue;

    public StringProperty(String name, String value)
    {
        super(name);
        this.value = value;
    }

    public StringProperty()
    {
        super();
    }

    /**
     * @see JMeterProperty#setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean runningVersion)
    {
        super.setRunningVersion(runningVersion);
    }

    public void setObjectValue(Object v)
    {
        if (isRunningVersion())
        {
            savedValue = this.value;
        }
        value = v.toString();
    }

    /**
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value;
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return value;
    }

    /**
     * @see Object#clone()
     */
    public Object clone()
    {
        StringProperty prop = (StringProperty) super.clone();
        prop.value = value;
        return prop;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see JMeterProperty#recoverRunningVersion(TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        if (savedValue != null)
        {
            value = savedValue;
            savedValue = null;
        }
        super.recoverRunningVersion(owner);
    }
}
