/*
 * Created on May 4, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.engine.util;

import java.util.Iterator;
import java.util.Map;

import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.StringUtilities;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UndoVariableReplacement extends AbstractTransformer
{
    
    public UndoVariableReplacement(CompoundVariable masterFunction,Map variables)
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
        Iterator iter = getVariables().keySet().iterator();
         String input = prop.getStringValue();
         while (iter.hasNext())
         {
             String key = (String) iter.next();
             String value = (String) getVariables().get(key);
             input = StringUtilities.substitute(input, "${" + key + "}", value);
         }
        StringProperty newProp = new StringProperty(prop.getName(), input);
        return newProp;
    }

}
