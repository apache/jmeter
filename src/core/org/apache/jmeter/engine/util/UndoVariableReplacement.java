/*
 * Created on May 4, 2003
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
 * @version $Revision$
 */
public class UndoVariableReplacement extends AbstractTransformer
{
    public UndoVariableReplacement(
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
