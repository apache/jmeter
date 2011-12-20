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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.apache.jorphan.util.JOrphanUtils;

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
    private static final String basedir = new File(System.getProperty("user.dir")).getParent();

	private static final File srcFiledir = new File(basedir,"src");

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
        BufferedReader fileReader = null;
        try {
        	fileReader = new BufferedReader(new InputStreamReader(ras));
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
        finally {
        	JOrphanUtils.closeQuietly(fileReader);
        }
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
            final boolean mainResourceFile = resname.startsWith("messages");
            for (String key : list) {
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

    private static final String[] prefixList = getResources(srcFiledir);

    /**
     * Find I18N resources in classpath
     * @param srcFiledir
     * @return list of properties files subject to I18N
     */
    public static final String[] getResources(File srcFiledir) {
    	Set<String> set = new TreeSet<String>();
		findFile(srcFiledir, set, new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory() 
						|| (
								name.equals("messages.properties") ||
								(name.endsWith("Resources.properties")
								&& !name.matches("Example\\d+Resources\\.properties")));
			}
		});
		return set.toArray(new String[set.size()]);
	}
    
    /**
     * Find resources matching filenamefiler and adds them to set removing everything before "/org"
     * @param file
     * @param set
     * @param filenameFilter
     */
    private static void findFile(File file, Set<String> set,
			FilenameFilter filenameFilter) {
    	File[] foundFiles = file.listFiles(filenameFilter);
    	for (File file2 : foundFiles) {
			if(file2.isDirectory()) {
				findFile(file2, set, filenameFilter);
			} else {
				String absPath2 = file2.getAbsolutePath().replace('\\', '/'); // Fix up Windows paths
                int indexOfOrg = absPath2.indexOf("/org");
				int lastIndex = absPath2.lastIndexOf(".");
				set.add(absPath2.substring(indexOfOrg, lastIndex));
			}
		}
    	
	}
    
    /*
     * Use a suite to ensure that the default is done first
    */
    public static Test suite() {
        TestSuite ts = new TestSuite("Resources PackageTest");
        String languages[] = JMeterMenuBar.getLanguages();
        for(String prefix : prefixList){
            TestSuite pfx = new TestSuite(prefix) ;
            pfx.addTest(new PackageTest("testLang","", prefix)); // load the default resource
            for(String language : languages){
                if (!"en".equals(language)){ // Don't try to check the default language
                    pfx.addTest(new PackageTest("testLang", language, prefix));
                }
            }
            ts.addTest(pfx);
        }
        ts.addTest(new PackageTest("checkI18n", "fr"));
        // TODO Add these some day
//        ts.addTest(new PackageTest("checkI18n", "es"));
//        ts.addTest(new PackageTest("checkI18n", "pl"));
//        ts.addTest(new PackageTest("checkI18n", "pt_BR"));
//        ts.addTest(new PackageTest("checkI18n", "tr"));
//        ts.addTest(new PackageTest("checkI18n", Locale.JAPANESE.toString()));
//        ts.addTest(new PackageTest("checkI18n", Locale.SIMPLIFIED_CHINESE.toString()));
//        ts.addTest(new PackageTest("checkI18n", Locale.TRADITIONAL_CHINESE.toString()));
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

    /**
     * Check all messages are available in one language
     * @throws Exception
     */
    public void checkI18n() throws Exception {
    	Map<String, Map<String,String>> missingLabelsPerBundle = new HashMap<String, Map<String,String>>();
    	for (String prefix : prefixList) {
        	Properties messages = new Properties();
        	messages.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(prefix.substring(1)+".properties"));
        	checkMessagesForLanguage( missingLabelsPerBundle , missingLabelsPerBundle, messages,prefix.substring(1), lang);
		}
    	
    	assertEquals(missingLabelsPerBundle.size()+" missing labels, labels missing:"+printLabels(missingLabelsPerBundle), 0, missingLabelsPerBundle.size());
    }

	/**
	 * Check messages are available in language
	 * @param missingLabelsPerBundle2 
	 * @param missingLabelsPerBundle 
	 * @param messages Properties messages in english
	 * @param language Language 
	 * @throws IOException
	 */
	private void checkMessagesForLanguage(Map<String, Map<String, String>> missingLabelsPerBundle, Map<String, Map<String, String>> missingLabelsPerBundle2, Properties messages, String bundlePath,String language)
			throws IOException {
		Properties messagesFr = new Properties();
		String languageBundle = bundlePath+"_"+language+ ".properties";
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(languageBundle);
		if(inputStream == null) {
			Map<String, String> messagesAsProperties = new HashMap<String, String>();
			for (Iterator<Map.Entry<Object, Object>> iterator = messages.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<Object, Object> entry = iterator.next();
				messagesAsProperties.put((String) entry.getKey(), (String) entry.getValue()); 
			}
			missingLabelsPerBundle.put(languageBundle, messagesAsProperties);
			return;
		}
    	messagesFr.load(inputStream);
    
    	Map<String, String> missingLabels = new TreeMap<String,String>();
    	for (Iterator<Map.Entry<Object,Object>> iterator =  messages.entrySet().iterator(); iterator.hasNext();) {
    		Map.Entry<Object,Object> entry = iterator.next();
			String key = (String)entry.getKey();
			if(!messagesFr.containsKey(key)) {
				missingLabels.put(key,(String) entry.getValue());
			}
		}
    	if(!missingLabels.isEmpty()) {
    		missingLabelsPerBundle.put(languageBundle, missingLabels);
    	}
	}

	/**
	 * Build message with misssing labels per bundle
	 * @param missingLabelsPerBundle
	 * @return String
	 */
    private String printLabels(Map<String, Map<String, String>> missingLabelsPerBundle) {
    	StringBuilder builder = new StringBuilder();
    	for (Iterator<Map.Entry<String,Map<String, String>>> iterator =  missingLabelsPerBundle.entrySet().iterator(); iterator.hasNext();) {
    		Map.Entry<String,Map<String, String>> entry = iterator.next();
    		builder.append("Missing labels in bundle:"+entry.getKey()+"\r\n");
        	for (Iterator<Map.Entry<String,String>> it2 =  entry.getValue().entrySet().iterator(); it2.hasNext();) {
        		Map.Entry<String,String> entry2 = it2.next();
    			builder.append(entry2.getKey()+"="+entry2.getValue()+"\r\n");
    		}
    		builder.append("======================================================\r\n");
		}
    	return builder.toString();
	}
}
