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

package org.apache.jmeter.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * Created on Nov 29, 2003
 * 
 * Test the composition of the properties files - properties files exist
 * (default, DE, NO, JA) - properties files don't have duplicate keys -
 * non-default properties files don't have any extra keys.
 * 
 * N.B. If there is a default resource, ResourceBundle does not detect missing
 * resources, i.e. the presence of messages.properties means that the
 * ResourceBundle for Locale "XYZ" would still be found, and have the same keys
 * as the default. This makes it not very useful for checking properties files.
 * 
 * This is why the tests use Class.getResourceAsStream() etc
 * 
 * The tests don't quite follow the normal JUnit test strategy of one test per
 * possible failure. This was done in order to make it easier to report exactly
 * why the tests failed.
 */

/**
 * @version $Revision$ $Date$
 */
public class PackageTest extends TestCase {

	// private static List defaultList = null;
	private static PropertyResourceBundle defaultPRB;

	// Read resource into ResourceBundle and store in List
	private PropertyResourceBundle getRAS(String res) throws Exception {
		InputStream ras = this.getClass().getResourceAsStream(res);
		return new PropertyResourceBundle(ras);
	}

	private static Object[] DUMMY_PARAMS = new Object[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" };

	// Read resource file saving the keys
	private int readRF(String res, List l) throws Exception {
		int fails = 0;
		InputStream ras = this.getClass().getResourceAsStream(res);
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(ras));
		String s;
		while ((s = fileReader.readLine()) != null) {
			if (s.length() > 0 && !s.startsWith("#") && !s.startsWith("!")) {
				int equ = s.indexOf('=');
				String key = s.substring(0, equ);
				/*
				 * JMeterUtils.getResString() converts space to _ and lowercases
				 * the key, so make sure all keys pass the test
				 */
				if ((key.indexOf(' ') >= 0) || !key.toLowerCase().equals(key)) {
					System.out.println("Invalid key for JMeterUtils " + key);
					fails++;
				}
				String val = s.substring(equ + 1);
				l.add(key); // Store the key
				/*
				 * Now check for invalid message format: if string contains {0}
				 * and ' there may be a problem, so do a format with dummy
				 * parameters and check if there is a { in the output. A bit
				 * crude, but should be enough for now.
				 */
				if (val.indexOf("{0}") > 0 && val.indexOf("'") > 0) {
					String m = java.text.MessageFormat.format(val, DUMMY_PARAMS);
					if (m.indexOf("{") > 0) {
						fails++;
						System.out.println("Incorrect message format ? (input/output): ");
						System.out.println(val);
						System.out.println(m);
					}
				}

			}
		}
		return fails;
	}

	// Helper method to construct resource name
	private static String getResName(String lang) {
		if (lang.length() == 0) {
			return "messages.properties";
		} else {
			return "messages_" + lang + ".properties";
		}
	}

	private void check(String resname) throws Exception {
		check(resname, true);// check that there aren't any extra entries
	}

	/*
	 * perform the checks on the resources
	 * 
	 */
	private void check(String resname, boolean checkUnexpected) throws Exception {
		ArrayList alf = new ArrayList(500);// holds keys from file
		String res = getResName(resname);
		subTestFailures += readRF(res, alf);
		Collections.sort(alf);

		// Look for duplicate keys in the file
		String last = "";
		for (int i = 0; i < alf.size(); i++) {
			String curr = (String) alf.get(i);
			if (curr.equals(last)) {
				subTestFailures++;
				System.out.println("\nDuplicate key =" + curr + " in " + res);
			}
			last = curr;
		}

		if (resname.length() == 0) // Must be the default resource file
		{
			defaultPRB = getRAS(res);
		} else if (checkUnexpected) {
			// Check all the keys are in the default props file
            PropertyResourceBundle prb = getRAS(res); 
			Enumeration enumr = prb.getKeys();
			while (enumr.hasMoreElements()) {
				String key = null;
				try {
					key = (String) enumr.nextElement();
					String val =defaultPRB.getString(key);
                    if (val.equals(prb.getString(key))){
                        System.out.println("Possible duplicate value for "+key+" in "+res);
                        subTestFailures++;
                    }
				} catch (MissingResourceException e) {
					subTestFailures++;
					System.out.println("Locale: " + resname + " has unexpected key: " + key);
				}
			}
		}

		if (subTestFailures > 0) {
			fail("One or more subtests failed");
		}
	}

	/*
	 * Use a suite to ensure that the default is done first
	 */
	public static Test suite() {
		TestSuite ts = new TestSuite("Resources PackageTest");
		ts.addTest(new PackageTest("atestDefault"));
		ts.addTest(new PackageTest("atestDE"));
		ts.addTest(new PackageTest("atestNO"));
		ts.addTest(new PackageTest("atestJA"));
		ts.addTest(new PackageTest("atestzh_CN"));
		ts.addTest(new PackageTest("atestzh_TW"));
		ts.addTest(new PackageTest("atestFR"));
		ts.addTest(new PackageTest("atestES"));
		return ts;
	}

	private int subTestFailures;

	public PackageTest(String string) {
		super(string);
		subTestFailures = 0;
	}

	public void atestDE() throws Exception {
		check("de");
	}

	public void atestJA() throws Exception {
		check("ja");
	}

	public void atestzh_CN() throws Exception {
		check("zh_CN");
	}

	public void atestzh_TW() throws Exception {
		check("zh_TW");
	}

	public void atestNO() throws Exception {
		check("no");
	}

	public void atestFR() throws Exception {
		check("fr");
	}

	public void atestES() throws Exception {
		check("es");
	}

	public void atestDefault() throws Exception {
		check("");
	}
}
