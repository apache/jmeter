/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.junit;

import java.io.File;
import java.util.MissingResourceException;

import junit.framework.TestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/*
 * Extend JUnit TestCase to provide common setup
 */
public abstract class JMeterTestCase extends TestCase {
	// Used by findTestFile
	private static final String filePrefix;

	public JMeterTestCase() {
		super();
	}

	public JMeterTestCase(String name) {
		super(name);
	}

	/*
	 * If not running under AllTests.java, make sure that the properties (and
	 * log file) are set up correctly.
	 * 
	 * N.B. In order for this to work correctly, the JUnit test must be started
	 * in the bin directory, and all the JMeter jars (plus any others needed at
	 * run-time) need to be on the classpath.
	 * 
	 */
	static {
		if (JMeterUtils.getJMeterProperties() == null) {
			String file = "testfiles/jmetertest.properties";
			File f = new File(file);
			if (!f.canRead()) {
				System.out.println("Can't find " + file + " - trying bin directory");
				file = "bin/" + file;// JMeterUtils assumes Unix-style
										// separators
				// Also need to set working directory so test files can be found
				System.setProperty("user.dir", System.getProperty("user.dir") + File.separatorChar + "bin");
				System.out.println("Setting user.dir=" + System.getProperty("user.dir"));
				filePrefix = "bin/";
			} else {
				filePrefix = "";
			}
			JMeterUtils jmu = new JMeterUtils();
			try {
				jmu.initializeProperties(file);
			} catch (MissingResourceException e) {
				System.out.println("** Can't find resources - continuing anyway **");
			}
			logprop("java.version");
			logprop("java.vendor");
			logprop("java.home");
			logprop("user.home");
			logprop("user.dir");
			logprop("java.class.version");
			logprop("os.name");
			logprop("os.version");
			logprop("os.arch");
			logprop("java.class.path");
			// String cp = System.getProperty("java.class.path");
			// String cpe[]= JOrphanUtils.split(cp,File.pathSeparator);
			// System.out.println("java.class.path=");
			// for (int i=0;i<cpe.length;i++){
			// System.out.println(cpe[i]);
			// }
		} else {
			filePrefix = "";
		}
	}

	private static void logprop(String prop) {
		System.out.println(prop + "=" + System.getProperty(prop));
	}

	// Helper method to find a file
	protected static File findTestFile(String file) {
		File f = new File(file);
		if (filePrefix.length() > 0 && !f.isAbsolute()) {
			f = new File(filePrefix + file);// Add the offset
		}
		return f;
	}

	protected static final Logger testLog = LoggingManager.getLoggerForClass();
}
