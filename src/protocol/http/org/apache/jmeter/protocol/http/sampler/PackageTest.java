/*
 * Created on Jul 16, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.protocol.http.sampler;

import junit.framework.TestCase;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.util.HTTPArgument;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PackageTest extends TestCase
{ /**
 * @param arg0
 */
    public PackageTest(String arg0)
    {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public void testConfiguring() throws Exception
    {
        HTTPSampler sampler = (HTTPSampler) new HttpTestSampleGui().createTestElement();
        sampler.addArgument("arg1", "val1");
        ConfigTestElement config = (ConfigTestElement) new HttpDefaultsGui().createTestElement();
        ((Arguments) config.getProperty(HTTPSampler.ARGUMENTS).getObjectValue()).addArgument(new HTTPArgument("config1", "configValue"));
        config.setRunningVersion(true);
        sampler.setRunningVersion(true);
        sampler.setRunningVersion(true);
        sampler.addTestElement(config);
        assertEquals("config1=configValue", sampler.getArguments().getArgument(1).toString());
        sampler.recoverRunningVersion();
        config.recoverRunningVersion();
        assertEquals(1, sampler.getArguments().getArgumentCount());
        sampler.addTestElement(config);
        assertEquals("config1=configValue", sampler.getArguments().getArgument(1).toString());

    }

}
