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
    
    public static String getCurrentName(String className)
    {
    	if (nameMap.containsKey(className))
    	{
			String newName= nameMap.getProperty(className);
    		log.info("Upgrading class "+className+" to "+newName);
    		return newName; 
    	}
        return className;
    }

	public static String getCurrentName(String propertyName, String className)
	{
		String key= className+"/"+propertyName;
		if (nameMap.containsKey(key))
		{
			String newName= nameMap.getProperty(key);
			log.info("Upgrading property "+propertyName+" to "+newName);
			return newName;
		}
		return propertyName;
	}

	public static String getCurrentName(String value, String propertyName, String className)
	{
		String key= className+"."+propertyName+"/"+value;
		if (nameMap.containsKey(key))
		{
			String newValue= nameMap.getProperty(key);
			log.info("Upgrading value "+value+" to "+newValue);
			return newValue;
		}
		return value;
	}

    /**
     * Private constructor to prevent instantiation.
     */
    private NameUpdater()
    {
    }
}
