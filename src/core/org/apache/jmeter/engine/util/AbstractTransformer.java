/*
 * Created on May 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.engine.util;

import java.util.Map;

/**
 * @author ano ano
 */
abstract class AbstractTransformer implements ValueTransformer
{


    protected CompoundVariable masterFunction;
    protected Map variables;

    public void setMasterFunction(CompoundVariable variable)
    {
        masterFunction = variable;
    }
    
    protected CompoundVariable getMasterFunction()
    {
        return masterFunction;
    }

    public Map getVariables()
    {
        return variables;
    }

    public void setVariables(Map map)
    {
        variables = map;
    }
}
