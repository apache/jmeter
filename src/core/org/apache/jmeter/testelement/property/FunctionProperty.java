package org.apache.jmeter.testelement.property;

import org.apache.jmeter.engine.util.CompoundVariable;
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

    /**
     * Executes the function (and caches the value for the duration of the test
     * iteration) if the property is a running version.  Otherwise, the raw
     * string representation of the function is provided.
     * @see org.apache.jmeter.testelement.property.JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        if(!isRunningVersion())
        {
            return function.getRawParameters();
        }
        else
        {
            int iter = JMeterContextService.getContext().getVariables().getIteration();
            if(iter < testIteration)
            {
                testIteration = -1;
            }
            if(iter > testIteration || cacheValue == null)
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
        FunctionProperty prop = (FunctionProperty)super.clone();
        prop.cacheValue = cacheValue;
        prop.testIteration = testIteration;
        prop.function = function;
        return prop;
    }

}
