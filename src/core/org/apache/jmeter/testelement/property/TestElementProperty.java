package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class TestElementProperty extends AbstractProperty
{
    TestElement value;
    TestElement savedValue = null;

    public TestElementProperty(String name, TestElement value)
    {
        super(name);
        this.value = value;
    }

    public TestElementProperty()
    {
        super();
    }

    public boolean equals(Object o)
    {
        if (o instanceof TestElementProperty)
        {
            if (value != null)
            {
                return value.equals(((TestElementProperty) o).getObjectValue());
            }
        }
        return false;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        return value.toString();
    }

    public void setObjectValue(Object v)
    {
        if (v instanceof TestElement)
        {
            if (isRunningVersion())
            {
                savedValue = this.value;
            }
            value = (TestElement) v;
        }
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return value;
    }

    public TestElement getElement()
    {
        return value;
    }

    public void setElement(TestElement el)
    {
        value = el;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        TestElementProperty prop = (TestElementProperty) super.clone();
        prop.value = (TestElement) value.clone();
        return prop;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#merge(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {
        if (isEqualType(prop))
        {
            value.addTestElement((TestElement) prop.getObjectValue());
        }
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#recoverRunningVersion()
     */
    public void recoverRunningVersion(TestElement owner)
    {
        if (savedValue != null)
        {
            value = savedValue;
            savedValue = null;
        }
        super.recoverRunningVersion(null);
        value.recoverRunningVersion();
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean runningVersion)
    {
        super.setRunningVersion(runningVersion);
        value.setRunningVersion(runningVersion);
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#setTemporary(boolean, org.apache.jmeter.testelement.TestElement)
     */
    public void setTemporary(boolean temporary, TestElement owner)
    {
        super.setTemporary(temporary, owner);
        PropertyIterator iter = value.propertyIterator();
        while (iter.hasNext())
        {
            iter.next().setTemporary(temporary, owner);
        }

    }

}
