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

package org.apache.jmeter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Check the eclipse and Maven version definitions against build.properties
 * Drop this if we move to Maven the build process
 */
public class JMeterVersionTest extends JMeterTestCase {

    // Convert between eclipse jar name and build.properties name
    private static Map<String, String> JAR_TO_BUILD_PROP = new HashMap<>();
    static {
        JAR_TO_BUILD_PROP.put("bsf", "apache-bsf");
        JAR_TO_BUILD_PROP.put("bsh", "beanshell");
        JAR_TO_BUILD_PROP.put("geronimo-jms_1.1_spec", "jms");
        JAR_TO_BUILD_PROP.put("mail", "javamail");
        JAR_TO_BUILD_PROP.put("oro", "jakarta-oro");
        JAR_TO_BUILD_PROP.put("xercesImpl", "xerces");
        JAR_TO_BUILD_PROP.put("xpp3_min", "xpp3");
    }

    private static final File JMETER_HOME = new File(JMeterUtils.getJMeterHome());

    /**
     * Versions of all libraries mentioned in build.properties (except checkstyle-all)
     */
    private final Map<String, String> versions = new HashMap<>();

    /**
     * Names of library.version entries in build.properties, excluding jars not bundled (used for docs only)
     */
    private final Set<String> propNames = new HashSet<>();

    /** License file names found under license/bin (WITHOUT the .txt suffix) */
    private Set<String> liceFiles;

    private File getFileFromHome(String relativeFile) {
        return new File(JMETER_HOME, relativeFile);
    }

    private Properties prop;

    @Before
    public void setUp() throws IOException {
        final Properties buildProp = new Properties();
        final FileInputStream bp = new FileInputStream(getFileFromHome("build.properties"));
        buildProp.load(bp);
        bp.close();
        for (Entry<Object, Object> entry : buildProp.entrySet()) {
            final String key = (String) entry.getKey();
            if (key.endsWith(".version")) {
                final String value = (String) entry.getValue();
                final String jarprop = key.replace(".version","");
                final String old = versions.put(jarprop, value);
                propNames.add(jarprop);
                if (old != null) {
                    fail("Already have entry for "+key);
                }
            }
        }
        // remove docs-only jars
        propNames.remove("jdom");
        propNames.remove("velocity");
        propNames.remove("commons-lang"); // lang3 is bundled, lang2 is doc-only

        // Darcula is not a maven artifact
        propNames.remove("darcula"); // not needed in Maven
        buildProp.remove("darcula.loc"); // not a Maven download
        versions.remove("darcula");

        // remove optional checkstyle name
        propNames.remove("checkstyle-all"); // not needed in Maven
        buildProp.remove("checkstyle-all.loc"); // not a Maven download
        versions.remove("checkstyle-all");
        // remove option RAT jars
        propNames.remove("rat");
        versions.remove("rat");
        propNames.remove("rat-tasks");
        versions.remove("rat-tasks");
        // remove optional hsqldb, jacoco and sonar jars (required for coverage reporting, not required for jmeter)
        for (String optLib : Arrays.asList("jacocoant", "sonarqube-ant-task", "hsqldb", "activemq-all",
                "mina-core", "ftplet-api", "ftpserver-core")) {
            propNames.remove(optLib);
            versions.remove(optLib);
        }
        prop = buildProp;
        final File licencesDir = getFileFromHome("licenses/bin");
        liceFiles = Arrays.stream(licencesDir.list())
                .filter(name -> !name.equalsIgnoreCase("README.txt"))
                .filter(name -> !name.equals(".svn")) // Ignore old-style SVN workspaces
                .map(name -> name.replace(".txt", ""))
                .collect(Collectors.toSet());
    }

    /**
     * Check eclipse.classpath contains the jars declared in build.properties
     * @throws IOException if something fails
     */
    @Test
    public void testEclipse() throws IOException {
        final BufferedReader eclipse = new BufferedReader(
                new FileReader(getFileFromHome("eclipse.classpath"))); // assume default charset is OK here
//      <classpathentry kind="lib" path="lib/geronimo-jms_1.1_spec-1.1.1.jar"/>
//      <classpathentry kind="lib" path="lib/activation-1.1.1.jar"/>
//      <classpathentry kind="lib" path="lib/jtidy-r938.jar"/>
        final Pattern p = Pattern.compile("\\s+<classpathentry kind=\"lib\" path=\"lib/(?:api/)?(.+?)-([^-]+(-\\d*|-b\\d+|-BETA\\d)?)\\.jar\"/>");
        final Pattern versionPat = Pattern.compile("\\$\\{(.+)\\.version\\}");
        String line;
        final ArrayList<String> toRemove = new ArrayList<>();
        while((line=eclipse.readLine()) != null){
            final Matcher m = p.matcher(line);
            if (m.matches()) {
                String jar = m.group(1);
                String version = m.group(2);
                if (jar.endsWith("-jdk15on")) { // special handling
                    jar=jar.replace("-jdk15on","");
                } else if (jar.equals("commons-jexl") && version.startsWith("2")) { // special handling
                    jar = "commons-jexl2";
                } else if (jar.equals("spock-core-1.2-groovy")) { // special handling
                    jar = "spock-core";
                    version = "1.2-groovy-2.4";
                } else {
                    String tmp = JAR_TO_BUILD_PROP.get(jar);
                    if (tmp != null) {
                        jar = tmp;
                    }
                }
                String expected = versions.get(jar);
                if (expected == null) {
                    final String message =
                            "Didn't find version for jar name extracted by regexp, jar name extracted:"
                                    + jar + ", version extracted:" + version + ", current line:" + line;
                    System.err.println(message);
                    fail(message);
                }
                // Process ${xxx.version} references
                final Matcher mp = versionPat.matcher(expected);
                if (mp.matches()) {
                    String key = mp.group(1);
                    expected = versions.get(key);
                    toRemove.add(key); // in case it is not itself used we remove it later
                }
                propNames.remove(jar);
                if (expected == null) {
                    fail("Versions list does not contain: " + jar);
                } else if (!version.equals(expected)) {
                    assertEquals(jar,version,expected);
                }
            }
        }
        // remove any possibly unused references
        propNames.removeAll(toRemove);
        eclipse.close();
        if (propNames.size() > 0) {
            fail("Should have no names left: "
                    + Arrays.toString(propNames.toArray())
                    + ". Check eclipse.classpath");
        }
    }

    @Test
    public void testMaven() throws IOException {
        final BufferedReader maven = new BufferedReader(
                new FileReader(getFileFromHome("res/maven/ApacheJMeter_parent.pom"))); // assume default charset is OK here
//      <apache-bsf.version>2.4.0</apache-bsf.version>
//      <log4j-1.2-api.version>2.7</log4j-1.2-api.version>
        final Pattern p = Pattern.compile("\\s+<([^\\<\\>]+)\\.version>([^<]+)<.*");

        String line;
        while((line=maven.readLine()) != null){
            final Matcher m = p.matcher(line);
            if (m.matches()) {
                String jar = m.group(1);
                String version = m.group(2);
                String expected = versions.get(jar);
                propNames.remove(jar);
                if (expected == null) {
                    fail("Versions list does not contain: " + jar);
                } else {
                    if (!version.equals(expected)) {
                        assertEquals(jar,expected,version);
                    }
                }
            }
        }
        maven.close();
        if (propNames.size() > 0) {
            fail("Should have no names left: "
                    + Arrays.toString(propNames.toArray())
                    + ". Check ApacheJMeter_parent.pom");
        }
    }

    @Test
    public void testLicences() {
        Set<String> liceNames = new HashSet<>();
        for (Map.Entry<String, String> me : versions.entrySet()) {
            final String key = me.getKey();
            liceNames.add(key + "-" + me.getValue());
        }
        assertTrue("Expected at least one license file", liceFiles.size() > 0);
        for(String l : liceFiles) {
            if (!liceNames.remove(l)) {
                fail("Mismatched version in license file " + l);
            }
        }
    }

    @Test
    public void testLICENSE() throws Exception {
        HashSet<String> buildOnly = new HashSet<>();
        buildOnly.addAll(Arrays.asList(new String[]{"bcprov","bcmail","bcpkix"}));
        // Build set of names expected to be mentioned in LICENSE
        final HashSet<String> binaryJarNames = new HashSet<>();
        for(Map.Entry<String, String> me : versions.entrySet()) {
            final String key = me.getKey();
            final String jarName = key + "-" + me.getValue();
            if (propNames.contains(key) && !buildOnly.contains(key)) {
                binaryJarNames.add(jarName);
            }
        }
        // Extract the jar names from LICENSE
        final BufferedReader license = new BufferedReader(
                new FileReader(getFileFromHome("LICENSE"))); // assume default charset is OK here
        final Pattern p = Pattern.compile("^\\* (\\S+?)\\.jar(.*)");

        final HashSet<String> namesInLicenseFile = new HashSet<>(); // names documented in LICENSE
        final HashSet<String> externalNamesinLicenseFile = new HashSet<>(); // names documented in LICENSE with licenses/bin entries

        String line;
        while((line=license.readLine()) != null){
            final Matcher m = p.matcher(line);
            if (m.matches()) {
                final String name = m.group(1);
                assertTrue("Duplicate jar in LICENSE file " + line, namesInLicenseFile.add(name));
                if (!binaryJarNames.contains(name) && !line.contains("darcula")) {
                    fail("Unexpected entry in LICENCE file: " + line);
                }
                final String comment = m.group(2);
                if (comment.length() > 0) { // must be in external list
                    externalNamesinLicenseFile.add(name);
                }
            }
        }
        license.close();

        // Check all build.properties entries are in LICENSE file
        for(String s : binaryJarNames) {
            if (!namesInLicenseFile.contains(s)) {
                fail("LICENSE does not contain entry for " + s);
            }
        }

        // Check that external license files are present
        for(String s : externalNamesinLicenseFile) {
            if (!liceFiles.contains(s)) {
                fail("bin/licenses does not contain a file for " + s);
            }
        }

        // Check that there are no license/bin files not mentioned in LICENSE
        for(String s : liceFiles) {
            if (!namesInLicenseFile.contains(s)) {
                fail("LICENSE does not contain entry for " + s);
            }
        }
    }

    /**
     * Check that all downloads use Maven Central
     */
    @Test
    public void testMavenDownload() {
        int fails = 0;
        for (Entry<Object, Object> entry : prop.entrySet()) {
            final String key = (String) entry.getKey();
            if (key.endsWith(".loc")) {
                final String value = (String) entry.getValue();
                if (! value.startsWith("${maven2.repo}")) {
                    fails++;
                    System.err.println("ERROR: non-Maven download detected\n" + key + "=" +value);
                }
            }
        }
        if (fails > 0) {
            // TODO replace with fail()
            System.err.println("ERROR: All files must be available from Maven Central; but " + fails + " use(s) a different download source");
        }
    }
}
