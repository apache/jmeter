/*
 * Created on Jun 13, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.apache.jmeter.util;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author ano ano
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class NameUpdater
{
    private static Properties nameMap;
    private static Logger log = LoggingManager.getLoggerForClass();
    
    static{
        nameMap = new Properties();
        try
        {
            nameMap.load(new FileInputStream(JMeterUtils.getJMeterHome() + JMeterUtils.getPropDefault("upgrade_properties","/bin/upgrade.properties")));
        }
        catch (Exception e)
        {
            log.error("Bad upgrade file",e);
        }
    }
    
    public static String getCurrentName(String guiName)
    {
        if(nameMap.containsKey(guiName))
        {
            return nameMap.getProperty(guiName);
        }
        return guiName;
    }
    

}
