package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
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
     * @see org.apache.jmeter.testelement.property.JMeterProperty#setRunningVersion(boolean)
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
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return value;
    }

    /**
     * @see java.lang.Object#clone()
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
     * @see org.apache.jmeter.testelement.property.JMeterProperty#recoverRunningVersion(org.apache.jmeter.testelement.TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        if (savedValue != null)
        {
            value = savedValue;
            savedValue = null;
        }
        // TODO Auto-generated method stub
        super.recoverRunningVersion(owner);
    }

}
