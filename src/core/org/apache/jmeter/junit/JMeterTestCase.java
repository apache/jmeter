/*
 * Created on Apr 30, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.junit;

import java.io.File;

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
	public JMeterTestCase(){
		super();
	}
    
    public JMeterTestCase(String name)
    {
        super(name);
    }
    
    /*
     * If not running under AllTests.java, make sure that the properties
     * (and log file) are set up correctly.
     * 
     * N.B. In order for this to work correctly, the JUnit test must be started
     * in the bin directory, and all the JMeter jars (plus any others needed at
     * run-time) need to be on the classpath.
     * 
     */
    static {
    	if (JMeterUtils.getJMeterProperties() == null){
    		String file="jmetertest.properties";
			File f = new File(file);
			if (!f.canRead()){
				System.out.println("Can't find "+file+" - trying bin directory");
				file="bin/"+file;// JMeterUtils assumes Unix-style separators
				// Also need to set working directory so test files can be found
				System.setProperty("user.dir",System.getProperty("user.dir")+File.separatorChar+"bin");
				System.out.println("Setting user.dir="+System.getProperty("user.dir"));
			}
    		JMeterUtils jmu = new JMeterUtils();
    		jmu.initializeProperties(file);
    	}
    }
    
    protected static final Logger testLog = LoggingManager.getLoggerForClass();
}

