package org.apache.jmeter.testelement.property;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class LongProperty extends AbstractProperty
{
    long value;
    
        public LongProperty(String name,long value)
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
    
        public void setObjectValue(Object v)
            {
                if(v instanceof Number)
                {
                    value = ((Number)v).intValue();
                }
                else
                {
                    try
                    {
                        value = Integer.parseInt(v.toString());
                    }
                    catch (RuntimeException e)
                    {}
                }
            }

        /**
         * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
         */
        public String getStringValue()
        {
            return Long.toString(value);
        }

        /**
         * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
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
            LongProperty prop = (LongProperty)super.clone();
            prop.value = value;
            return prop;
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object arg0)
        {
            if(arg0 instanceof JMeterProperty)
            {
                long argValue = ((JMeterProperty)arg0).getLongValue();
                if(value < argValue)
                {
                    return -1;
                }
                else if(value == argValue)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
            else
            {
                return -1;
            }
        }

        /**
         * @see org.apache.jmeter.testelement.property.JMeterProperty#getBooleanValue()
         */
        public boolean getBooleanValue()
        {
            return getLongValue() > 0 ? true : false;
        }

        /**
         * @see org.apache.jmeter.testelement.property.JMeterProperty#getDoubleValue()
         */
        public double getDoubleValue()
        {
            return (double)value;
        }

        /**
         * @see org.apache.jmeter.testelement.property.JMeterProperty#getFloatValue()
         */
        public float getFloatValue()
        {
            return (float)value;
        }

        /**
         * @see org.apache.jmeter.testelement.property.JMeterProperty#getIntValue()
         */
        public int getIntValue()
        {
            return (int)value;
        }

        /**
         * @see org.apache.jmeter.testelement.property.JMeterProperty#getLongValue()
         */
        public long getLongValue()
        {
            return value;
        }


}
