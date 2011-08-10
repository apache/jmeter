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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import org.apache.jmeter.gui.util.JMeterMenuBar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * Created on Nov 29, 2003
 * 
 * Test the composition of the messages*.properties files
 * - properties files exist
 * - properties files don't have duplicate keys
 * - non-default properties files don't have any extra keys.
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

public class PackageTest extends TestCase {

    private static final String MESSAGES = "messages";

    private static PropertyResourceBundle defaultPRB;

    private static final CharsetEncoder ASCII_ENCODER = 
        Charset.forName("US-ASCII").newEncoder(); // Ensure properties files don't use special characters
    
    private static boolean isPureAscii(String v) {
      return ASCII_ENCODER.canEncode(v);
    }

    // Read resource into ResourceBundle and store in List
    private PropertyResourceBundle getRAS(String res) throws Exception {
        InputStream ras = this.getClass().getResourceAsStream(res);
        if (ras == null){
            return null;
        }
        return new PropertyResourceBundle(ras);
    }

    private static final Object[] DUMMY_PARAMS = new Object[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" };

    // Read resource file saving the keys
    private int readRF(String res, List<String> l) throws Exception {
        int fails = 0;
        InputStream ras = this.getClass().getResourceAsStream(res);
        if (ras==null){
            if (MESSAGES.equals(resourcePrefix)|| lang.length() == 0 ){
                throw new IOException("Cannot open resource file "+res);
            } else {
                return 0;
            }
        }
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(ras));
        String s;
        while ((s = fileReader.readLine()) != null) {
            if (s.length() > 0 && !s.startsWith("#") && !s.startsWith("!")) {
                int equ = s.indexOf('=');
                String key = s.substring(0, equ);
                if (resourcePrefix.equals(MESSAGES)){// Only relevant for messages
                    /*
                     * JMeterUtils.getResString() converts space to _ and lowercases
                     * the key, so make sure all keys pass the test
                     */
                    if ((key.indexOf(' ') >= 0) || !key.toLowerCase(java.util.Locale.ENGLISH).equals(key)) {
                        System.out.println("Invalid key for JMeterUtils " + key);
                        fails++;
                    }
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
                        System.out.println("Incorrect message format ? (input/output) for: "+key);
                        System.out.println(val);
                        System.out.println(m);
                    }
                }

                if (!isPureAscii(val)) {
                    fails++;
                    System.out.println("Incorrect char value in: "+s);                    
                }
            }
        }
        return fails;
    }

    // Helper method to construct resource name
    private String getResName(String lang) {
        if (lang.length() == 0) {
            return resourcePrefix+".properties";
        } else {
            return resourcePrefix+"_" + lang + ".properties";
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
        ArrayList<String> alf = new ArrayList<String>(500);// holds keys from file
        String res = getResName(resname);
        subTestFailures += readRF(res, alf);
        Collections.sort(alf);

        // Look for duplicate keys in the file
        String last = "";
        for (int i = 0; i < alf.size(); i++) {
            String curr = alf.get(i);
            if (curr.equals(last)) {
                subTestFailures++;
                System.out.println("\nDuplicate key =" + curr + " in " + res);
            }
            last = curr;
        }

        if (resname.length() == 0) // Must be the default resource file
        {
            defaultPRB = getRAS(res);
            if (defaultPRB == null){
                throw new IOException("Could not find required file: "+res);
            }
        } else if (checkUnexpected) {
            // Check all the keys are in the default props file
            PropertyResourceBundle prb = getRAS(res); 
            if (prb == null){
                return;
            }
            final ArrayList<String> list = Collections.list(prb.getKeys());
            Collections.sort(list);
            Iterator<String> enumr = list.iterator();
            final boolean mainResourceFile = resname.startsWith("messages");
            while (enumr.hasNext()) {
                String key = enumr.next();
                try {
                    String val = defaultPRB.getString(key); // Also Check key is in default
                    if (mainResourceFile && val.equals(prb.getString(key))){
                        System.out.println("Duplicate value? "+key+"="+val+" in "+res);
                        subTestFailures++;
                    }
                } catch (MissingResourceException e) {
                    subTestFailures++;
                    System.out.println(resourcePrefix + "_" + resname + " has unexpected key: " + key);
                }
            }
        }

        if (subTestFailures > 0) {
            fail("One or more subtests failed");
        }
    }

    // TODO generate list by scanning for *Resources.properties
    private static final String[] prefixList={
        MESSAGES, // This is in the same package, so no need for full path name
        "/org/apache/jmeter/assertions/BSFAssertionResources",
        "/org/apache/jmeter/config/CSVDataSetResources",
        "/org/apache/jmeter/config/RandomVariableConfigResources",
        "/org/apache/jmeter/extractor/BeanShellPostProcessorResources",
        "/org/apache/jmeter/extractor/BSFPostProcessorResources",
        "/org/apache/jmeter/extractor/DebugPostProcessorResources",
        "/org/apache/jmeter/modifiers/BeanShellPreProcessorResources",
        "/org/apache/jmeter/modifiers/BSFPreProcessorResources",
        "/org/apache/jmeter/sampler/DebugSamplerResources",
        "/org/apache/jmeter/timers/BeanShellTimerResources",
        "/org/apache/jmeter/timers/ConstantThroughputTimerResources",
        "/org/apache/jmeter/timers/SyncTimerResources",
        "/org/apache/jmeter/visualizers/BeanShellListenerResources",
        "/org/apache/jmeter/visualizers/BSFListenerResources",
//        "/org/apache/jmeter/examples/testbeans/example2/Example2Resources", // examples are not built by default
        "/org/apache/jmeter/protocol/http/sampler/AccessLogSamplerResources",
        "/org/apache/jmeter/protocol/jdbc/config/DataSourceElementResources",
        "/org/apache/jmeter/protocol/jdbc/sampler/JDBCSamplerResources",
    };

    /*
     * Use a suite to ensure that the default is done first
    */
    public static Test suite() {
        TestSuite ts = new TestSuite("Resources PackageTest");
        for(int j=0; j < prefixList.length; j++){
            String prefix = prefixList[j];
            TestSuite pfx = new TestSuite(prefix) ;
            pfx.addTest(new PackageTest("testLang","", prefix)); // load the default resource
            String lang[] = JMeterMenuBar.getLanguages();
            for(int i=0; i < lang.length; i++ ){
                if (!"en".equals(lang[i])){ // Don't try to check the default language
                    pfx.addTest(new PackageTest("testLang", lang[i], prefix));
                }
            }
            ts.addTest(pfx);
        }

        return ts;
    }

    private int subTestFailures;

    private final String lang;
    
    private final String resourcePrefix; // e.g. "messages"

    public PackageTest(String testName, String _lang) {
        this(testName, _lang, MESSAGES);
    }

    public PackageTest(String testName, String _lang, String propName) {
        super(testName);
        lang=_lang;
        subTestFailures = 0;
        resourcePrefix = propName;
    }

    public void testLang() throws Exception{
        check(lang);
    }

}
