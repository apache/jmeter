/*
 * Created on Apr 30, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.junit;

import junit.framework.TestCase;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public abstract class JMeterTestCase extends TestCase
{
    
    public JMeterTestCase(String name)
    {
        super(name);
    }
    
    protected static Logger testLog = 
LoggingManager.getLoggerFor(JMeterUtils.TEST);
}

