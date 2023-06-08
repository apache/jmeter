/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.jmeter.gui.util.JMeterMenuBar;
import org.junit.runner.Describable;
import org.junit.runner.Description;

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

public class PackageTest extends TestCase implements Describable {
    // We assume the test starts in "src/core" directory (which is true for Gradle and IDEs)
    private static final File resourceFiledir = new File("src/main/resources");

    private static final String MESSAGES = "messages";

    private static PropertyResourceBundle defaultPRB; // current default language properties file

    // Read resource into ResourceBundle and store in List
    private PropertyResourceBundle getRAS(String res) throws Exception {
        InputStream ras = this.getClass().getResourceAsStream(res);
        if (ras == null) {
            return null;
        }
        return new PropertyResourceBundle(ras);
    }

    private static final Object[] DUMMY_PARAMS =
            new Object[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" };

    // Read resource file saving the keys
    private void readRF(String res, List<String> l) throws Exception {
        InputStream ras = this.getClass().getResourceAsStream(res);
        if (ras == null){
            if (MESSAGES.equals(resourcePrefix)|| lang.isEmpty()) {
                throw new IOException("Cannot open resource file "+res);
            } else {
                return;
            }
        }
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(ras, StandardCharsets.UTF_8))) {
            String s;
            while ((s = fileReader.readLine()) != null) {
                if (!s.isEmpty() && !s.startsWith("#") && !s.startsWith("!")) {
                    int equ = s.indexOf('=');
                    String key = s.substring(0, equ);
                    if (resourcePrefix.equals(MESSAGES)){// Only relevant for messages
                        /*
                         * JMeterUtils.getResString() converts space to _ and lowercases
                         * the key, so make sure all keys pass the test
                         */
                        if (key.contains(" ") || !key.toLowerCase(java.util.Locale.ENGLISH).equals(key)) {
                            failures.add("Invalid key for JMeterUtils " + key);
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
                    if (val.contains("{0}") && val.contains("'")) {
                        String m = java.text.MessageFormat.format(val, DUMMY_PARAMS);
                        if (m.contains("{")) {
                            failures.add("Incorrect message format ? (input/output) for: " + key +
                                    ". Output contains {, it seems not all paratemeters were replaced." +
                                    "Format: " + val + ", message with dummy parameters: " + m);
                        }
                    }

                    // We don't need to verify ASCII as build system ensures the final properties will be in ASCII
                    // The proper test would be to get value of a well-known resource and validate it
                    //if (!isPureAscii(val)) {
                    //    failures.add("Message format should be pure ASCII. Actual format is " + val);
                    //}
                }
            }
        }
    }

    // Helper method to construct resource name
    private String getResName(String lang) {
        if (lang.isEmpty()) {
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
        ArrayList<String> alf = new ArrayList<>(500);// holds keys from file
        String res = getResName(resname);
        readRF(res, alf);
        Collections.sort(alf);

        // Look for duplicate keys in the file
        String last = "";
        for (String curr : alf) {
            if (curr.equals(last)) {
                failures.add("Duplicate key=" + curr + " in " + res);
            }
            last = curr;
        }

        if (resname.isEmpty()) // Must be the default resource file
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
                        failures.add("Duplicate value? "+key+"="+val+" in "+res);
                    }
                } catch (MissingResourceException e) {
                    failures.add(resourcePrefix + "_" + resname + " has unexpected key: " + key);
                }
            }
        }

        if (failures.isEmpty()) {
            return;
        }
        fail(String.join("\n", failures));
    }

    private static final String[] prefixList = getResources(resourceFiledir);

    /**
     * Find I18N resources in classpath
     * @param srcFileDir directory in which the files reside
     * @return list of properties files subject to I18N
     */
    public static String[] getResources(File srcFileDir) {
        if (!srcFileDir.exists() && "resources".equals(srcFileDir.getName())) {
            // Allow non-existing resources directory
            return new String[0];
        }
        Set<String> set = new TreeSet<>();
        findFile(srcFileDir, set, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals("messages.properties") ||
                        name.endsWith("Resources.properties")
                                && !name.matches("Example\\d+Resources\\.properties")
                        || new File(dir, name).isDirectory();
            }
        });
        return set.toArray(new String[set.size()]);
    }

    /**
     * Find resources matching filenameFiler and adds them to set removing
     * everything before "/org"
     *
     * @param file
     *            directory in which the files reside
     * @param set
     *            container into which the names of the files should be added
     * @param filenameFilter
     *            filter that the files must satisfy to be included into
     *            <code>set</code>
     */
    static void findFile(File file, Set<String> set,
            FilenameFilter filenameFilter) {
        File[] foundFiles = file.listFiles(filenameFilter);
        assertNotNull("Not a directory: "+file, foundFiles);
        for (File file2 : foundFiles) {
            if (file2.isDirectory()) {
                findFile(file2, set, filenameFilter);
            } else {
                String absPath2 = file2.getAbsolutePath().replace('\\', '/'); // Fix up Windows paths
                int indexOfOrg = absPath2.indexOf("/org");
                int lastIndex = absPath2.lastIndexOf('.');
                set.add(absPath2.substring(indexOfOrg, lastIndex));
            }
        }
    }

    /*
     * Use a suite to ensure that the default is done first
    */
    public static Test suite() {
        TestSuite ts = new TestSuite("Resources PackageTest");
        String[] languages = JMeterMenuBar.getLanguages();
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

    private List<String> failures = new ArrayList<>();

    private final String lang;

    private final String resourcePrefix; // e.g. "/org/apache/jmeter/resources/messages"

    public PackageTest(String testName, String _lang) {
        this(testName, _lang, MESSAGES);
    }

    public PackageTest(String testName, String _lang, String propName) {
        super(testName);
        lang=_lang;
        resourcePrefix = propName;
    }

    @Override
    public Description getDescription() {
        return Description.createTestDescription(getClass(), getName() + " " + lang + ": " + resourcePrefix);
    }

    public void testLang() throws Exception{
        check(lang);
    }

    /**
     * Check all messages are available in one language
     * @throws Exception if something fails
     */
    public void checkI18n() throws Exception {
        Map<String, Map<String,String>> missingLabelsPerBundle = new HashMap<>();
        for (String prefix : prefixList) {
            Properties messages = new Properties();
            messages.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(prefix.substring(1)+".properties"));
            checkMessagesForLanguage( missingLabelsPerBundle , messages,prefix.substring(1), lang);
        }

        assertEquals(missingLabelsPerBundle.size()+" missing labels, labels missing:"+printLabels(missingLabelsPerBundle), 0, missingLabelsPerBundle.size());
    }

    private void checkMessagesForLanguage(Map<String, Map<String, String>> missingLabelsPerBundle,
                                          Properties messages, String bundlePath, String language)
            throws IOException {
        Properties messagesFr = new Properties();
        String languageBundle = bundlePath+"_"+language+ ".properties";
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(languageBundle);
        if(inputStream == null) {
            Map<String, String> messagesAsProperties = new HashMap<>();
            for (Map.Entry<Object, Object> entry : messages.entrySet()) {
                messagesAsProperties.put((String) entry.getKey(), (String) entry.getValue());
            }
            missingLabelsPerBundle.put(languageBundle, messagesAsProperties);
            return;
        }
        messagesFr.load(inputStream);

        Map<String, String> missingLabels = new TreeMap<>();
        for (Map.Entry<Object, Object> entry : messages.entrySet()) {
            String key = (String) entry.getKey();
            final String I18NString = "[\\d% ]+";// numeric, space and % don't need translation
            if (!messagesFr.containsKey(key)) {
                String value = (String) entry.getValue();
                // TODO improve check of values that don't need translation
                if (value.matches(I18NString)) {
                    System.out.println("Ignoring missing " + key + "=" + value + " in " + languageBundle); // TODO convert to list and display at end
                } else {
                    missingLabels.put(key, (String) entry.getValue());
                }
            } else {
                String value = (String) entry.getValue();
                if (value.matches(I18NString)) {
                    System.out.println("Unnecessary entry " + key + "=" + value + " in " + languageBundle);
                }
            }
        }
        if (!missingLabels.isEmpty()) {
            missingLabelsPerBundle.put(languageBundle, missingLabels);
        }
    }

    private String printLabels(Map<String, Map<String, String>> missingLabelsPerBundle) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Map<String, String>> entry : missingLabelsPerBundle.entrySet()) {
            builder.append("Missing labels in bundle:")
                    .append(entry.getKey())
                    .append("\r\n");
            for (Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
                builder.append(entry2.getKey())
                        .append("=")
                        .append(entry2.getValue())
                        .append("\r\n");
            }
            builder.append("======================================================\r\n");
        }
        return builder.toString();
    }
}
