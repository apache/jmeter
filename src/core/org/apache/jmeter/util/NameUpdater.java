/*
 * Created on Jun 13, 2003
 */
package org.apache.jmeter.util;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author ano ano
 * @version $Revision$
 */
public final class NameUpdater
{
    private static Properties nameMap;
    private static Logger log = LoggingManager.getLoggerForClass();
    
    static {
        nameMap = new Properties();
        try
        {
            nameMap.load(
                new FileInputStream(
                    JMeterUtils.getJMeterHome()
                        + JMeterUtils.getPropDefault(
                            "upgrade_properties",
                            "/bin/upgrade.properties")));
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
    
    /**
     * Private constructor to prevent instantiation.
     */
    private NameUpdater()
    {
    }
}
