/*
 * Created on May 4, 2003
 */
package org.apache.jmeter.engine.util;

import java.util.Map;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.property.FunctionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;

/**
 * @author ano ano
 *
 * @version $Revision$
 */
public class ReplaceStringWithFunctions extends AbstractTransformer
{
    public ReplaceStringWithFunctions(
        CompoundVariable masterFunction,
        Map variables)
        {
            super();
            setMasterFunction(masterFunction);
            setVariables(variables);
        }

    /* (non-Javadoc)
     * @see ValueTransformer#transformValue(JMeterProperty)
     */
    public JMeterProperty transformValue(JMeterProperty prop)
        throws InvalidVariableException
    {
        JMeterProperty newValue = prop;
        getMasterFunction().clear();
        getMasterFunction().setParameters(prop.getStringValue());
        if (getMasterFunction().hasFunction())
        {
            newValue =
                new FunctionProperty(
                    prop.getName(),
                    getMasterFunction().getFunction());
        }
        return newValue;
    }

}
