package org.apache.jmeter.testelement.property;

import org.apache.jmeter.testelement.TestElement;

/**
 * @version $Revision$
 */
public class TestElementProperty extends MultiProperty
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
                return value.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * #getStringValue()
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

    /* (non-Javadoc)
     * #getObjectValue()
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

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
        TestElementProperty prop = (TestElementProperty) super.clone();
        prop.value = (TestElement) value.clone();
        return prop;
    }

    /* (non-Javadoc)
     * #mergeIn(JMeterProperty)
     */
    public void mergeIn(JMeterProperty prop)
    {
        if (isEqualType(prop))
        {
            value.addTestElement((TestElement) prop.getObjectValue());
        }
    }

    /* (non-Javadoc)
     * #recoverRunningVersion(TestElement)
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

    /* (non-Javadoc)
     * #setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean runningVersion)
    {
        super.setRunningVersion(runningVersion);
        value.setRunningVersion(runningVersion);
    }

    /* (non-Javadoc)
     * @see MultiProperty#addProperty(JMeterProperty)
     */
    public void addProperty(JMeterProperty prop)
    {
        value.setProperty(prop);
    }

    /* (non-Javadoc)
     * @see MultiProperty#clear()
     */
    public void clear()
    {
        value.clear();

    }

    /* (non-Javadoc)
     * @see MultiProperty#iterator()
     */
    public PropertyIterator iterator()
    {
        return value.propertyIterator();
    }
}
