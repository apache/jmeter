package org.apache.jmeter.testelement.property;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class FunctionProperty extends AbstractProperty
{
    CompoundVariable function;
    int testIteration = -1;
    String cacheValue;

    public FunctionProperty(String name, CompoundVariable func)
    {
        super(name);
        function = func;
    }

    public FunctionProperty()
    {
        super();
    }

    public void setObjectValue(Object v)
    {
        if (v instanceof CompoundVariable && !isRunningVersion())
        {
            function = (CompoundVariable) v;
        }
        else
        {
            cacheValue = v.toString();
        }
    }

    public boolean equals(Object o)
    {
        if (o instanceof FunctionProperty)
        {
            if (function != null)
            {
                return function.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    /**
     * Executes the function (and caches the value for the duration of the test
     * iteration) if the property is a running version.  Otherwise, the raw
     * string representation of the function is provided.
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        if (!isRunningVersion())
        {
            log.debug("Not running version, return raw function string");
            return function.getRawParameters();
        }
        else
        {
            log.debug("Running version, executing function");
            int iter = JMeterContextService.getContext().getVariables().getIteration();
            if (iter < testIteration)
            {
                testIteration = -1;
            }
            if (iter > testIteration || cacheValue == null)
            {
                testIteration = iter;
                cacheValue = function.execute();
            }
            return cacheValue;
        }
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return function;
    }

    public Object clone()
    {
        FunctionProperty prop = (FunctionProperty) super.clone();
        prop.cacheValue = cacheValue;
        prop.testIteration = testIteration;
        prop.function = function;
        return prop;
    }

    /**
     * @see org.apache.jmeter.testelement.property.JMeterProperty#recoverRunningVersion(org.apache.jmeter.testelement.TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        super.recoverRunningVersion(owner);
        cacheValue = null;
    }

}
