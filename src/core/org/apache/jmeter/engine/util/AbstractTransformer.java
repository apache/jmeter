/*
 * Created on May 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.engine.util;

import java.util.Collection;
import java.util.Map;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
abstract class AbstractTransformer implements ValueTransformer
{


    protected CompoundVariable masterFunction;
    protected Map variables;
    /**
     * @param variable
     */
    public void setMasterFunction(CompoundVariable variable)
    {
        masterFunction = variable;
    }
    
    protected CompoundVariable getMasterFunction()
    {
        return masterFunction;
    }

    /**
     * @return
     */
    public Map getVariables()
    {
        return variables;
    }

    /**
     * @param map
     */
    public void setVariables(Map map)
    {
        variables = map;
    }

}
