/*
 * Created on May 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.engine.util;

import java.util.Map;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.property.FunctionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ReplaceStringWithFunctions extends AbstractTransformer
{
    
    public ReplaceStringWithFunctions(CompoundVariable masterFunction,Map variables)
        {
            super();
            setMasterFunction(masterFunction);
            setVariables(variables);
        }

    /* (non-Javadoc)
     * @see org.apache.jmeter.engine.util.ValueTransformer#transformValue(org.apache.jmeter.testelement.property.JMeterProperty)
     */
    public JMeterProperty transformValue(JMeterProperty prop) throws InvalidVariableException
    {
        JMeterProperty newValue = prop;
        getMasterFunction().clear();
        getMasterFunction().setParameters(prop.getStringValue());
        if (getMasterFunction().hasFunction())
        {
            newValue = new FunctionProperty(prop.getName(), getMasterFunction().getFunction());
        }
        else if (getMasterFunction().hasStatics())
        {
            newValue = new StringProperty(prop.getName(), getMasterFunction().getStaticSubstitution());
        }
        return newValue;
    }

}
