/*
 * Created on 02-Oct-2003
 *
 * This class defines the JMeter version only (moved from JMeterUtils)
 * 
 * Version changes no longer change the JMeterUtils source file
 * - easier to spot when JMeterUtils really changes 
 * - much smaller to download when the version changes
 * 
 */
package org.apache.jmeter.util;

/**
 * Utility class to define the JMeter Version string
 * 
 * @author sebb AT apache.org
 * @version $revision$ $date$
 */
public class JMeterVersion
{

	/*
	 * The VERSION string is updated by the Ant build file, which looks for the
	 * pattern: VERSION = <quote>.*<quote>
	 * 
	 */
	static final String VERSION = "1.9.20031130";

    private JMeterVersion() // Not instantiable
    {
        super();
    }

}
