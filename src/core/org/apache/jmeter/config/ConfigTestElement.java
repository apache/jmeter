package org.apache.jmeter.config;

import java.io.Serializable;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;

/**
 * @author    Michael Stover
 * @version   $Revision$
 */
public class ConfigTestElement
    extends AbstractTestElement
    implements Serializable
{
    public final static String USERNAME = "ConfigTestElement.username";
    public final static String PASSWORD = "ConfigTestElement.password";

    public ConfigTestElement()
    {
    }

    public void addTestElement(TestElement parm1)
    {
        if (parm1 instanceof ConfigTestElement)
        {
            mergeIn(parm1);
        }
    }
}
