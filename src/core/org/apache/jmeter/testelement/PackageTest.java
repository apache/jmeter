/*
 * Created on Jul 16, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.testelement;

import junit.framework.TestCase;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.LoginConfig;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PackageTest extends TestCase
{

    /**
     * @param arg0
     */
    public PackageTest(String arg0)
    {
        super(arg0);
        // TODO Auto-generated constructor stub
    }
    
    public void testRecovery() throws Exception
    {
        ConfigTestElement config = new ConfigTestElement();
        config.addProperty(new StringProperty("name","config"));
        config.setRunningVersion(true);
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setUsername("user1");
        loginConfig.setPassword("pass1");
        assertEquals(new NullProperty(),config.getProperty("login"));
        config.addProperty(new TestElementProperty("login",loginConfig));
        assertEquals(loginConfig.toString(),config.getPropertyAsString("login"));
        config.recoverRunningVersion();
        assertEquals(new NullProperty(),config.getProperty("login"));
    }
    
    public void testArguments() throws Exception
    {
        Arguments args = new Arguments();
        args.addArgument("arg1","val1","=");
        TestElementProperty prop = new TestElementProperty("args",args);
        ConfigTestElement te = new ConfigTestElement();
        te.addProperty(prop);
        te.setRunningVersion(true);
        Arguments config = new Arguments();
        config.addArgument("config1","configValue","=");
        TestElementProperty configProp = new TestElementProperty("args",config);
        ConfigTestElement te2 = new ConfigTestElement();
        te2.addProperty(configProp);
        te.addTestElement(te2);
        assertEquals(2,args.getArgumentCount());
        assertEquals("config1=configValue",args.getArgument(1).toString());
        te.recoverRunningVersion();
        te.addTestElement(te2);
        assertEquals(2,args.getArgumentCount());
        assertEquals("config1=configValue",args.getArgument(1).toString());
        
    }

}
