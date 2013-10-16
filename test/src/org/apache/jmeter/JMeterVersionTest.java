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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Check the eclipse and Maven version definitions against build.properties
 */
public class JMeterVersionTest extends JMeterTestCase {

    // Convert between eclipse jar name and build.properties name
    private static Map<String, String> JAR_TO_BUILD_PROP = new HashMap<String, String>();
    static {
        JAR_TO_BUILD_PROP.put("bsf", "apache-bsf");
        JAR_TO_BUILD_PROP.put("bsh", "beanshell");
        JAR_TO_BUILD_PROP.put("geronimo-jms_1.1_spec", "jms");
        JAR_TO_BUILD_PROP.put("htmllexer", "htmlparser"); // two jars same version
        JAR_TO_BUILD_PROP.put("httpmime", "httpclient"); // two jars same version
        JAR_TO_BUILD_PROP.put("mail", "javamail");
        JAR_TO_BUILD_PROP.put("oro", "jakarta-oro");
        JAR_TO_BUILD_PROP.put("xercesImpl", "xerces");
        JAR_TO_BUILD_PROP.put("xpp3_min", "xpp3");
    }

    private static final File JMETER_HOME = new File(JMeterUtils.getJMeterHome());

    public JMeterVersionTest() {
        super();
    }

    public JMeterVersionTest(String arg0) {
        super(arg0);
    }

    private final Map<String, String> versions = new HashMap<String, String>();
    private final Set<String> propNames = new HashSet<String>();

    private File getFileFromHome(String relativeFile) {
        return new File(JMETER_HOME, relativeFile);
    }

    @Override
    protected void setUp() throws Exception {
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
        propNames.remove("velocity");
        propNames.remove("commons-lang");
    }

    public void testEclipse() throws Exception {
        final BufferedReader eclipse = new BufferedReader(
                new FileReader(getFileFromHome("eclipse.classpath"))); // assume default charset is OK here
//      <classpathentry kind="lib" path="lib/geronimo-jms_1.1_spec-1.1.1.jar"/>
//      <classpathentry kind="lib" path="lib/activation-1.1.1.jar"/>
//      <classpathentry kind="lib" path="lib/jtidy-r938.jar"/>
        final Pattern p = Pattern.compile("\\s+<classpathentry kind=\"lib\" path=\"lib/(?:api/)?(.+)-([^-]+)\\.jar\"/>");
        String line;
        while((line=eclipse.readLine()) != null){
            final Matcher m = p.matcher(line);
            if (m.matches()) {
                String jar = m.group(1);
                String version = m.group(2);
                if (jar.endsWith("-jdk15on")) { // special handling
                    jar=jar.replace("-jdk15on","");
                } else if (jar.equals("commons-jexl") && version.startsWith("2")) { // special handling
                    jar="commons-jexl2";
                } else {
                    String tmp = JAR_TO_BUILD_PROP.get(jar);
                    if (tmp != null) {
                        jar = tmp;
                    }
                }
                final String expected = versions.get(jar);
                propNames.remove(jar);
                if (expected == null) {
                    fail("Versions list does not contain: " + jar);
                } else {
                    if (!version.equals(expected)) {
                        assertEquals(jar,version,expected);
                    }
                }
            }
        }
        eclipse.close();
        if (propNames.size() > 0) {
            fail("Should have no names left: "+Arrays.toString(propNames.toArray()) + ". Check eclipse.classpath");
        }
    }

    public void testMaven() throws Exception {
        final BufferedReader maven = new BufferedReader(
                new FileReader(getFileFromHome("res/maven/ApacheJMeter_parent.pom"))); // assume default charset is OK here
//      <apache-bsf.version>2.4.0</apache-bsf.version>
        final Pattern p = Pattern.compile("\\s+<([^\\.]+)\\.version>([^<]+)<.*");

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
            fail("Should have no names left: "+Arrays.toString(propNames.toArray()) + ". Check ApacheJMeter_parent.pom");
        }
   }

}
