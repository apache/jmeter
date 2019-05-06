package org.apache.jmeter;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AccessControlException;
import java.security.Permission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Before;
import org.junit.Test;

public class JMeterTest extends JMeterTestCase {
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
     * Versions of all libraries mentioned in build.properties (except
     * checkstyle-all)
     */
    private final Map<String, String> versions = new HashMap<>();
    /**
     * Names of library.version entries in build.properties, excluding jars not
     * bundled (used for docs only)
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
                final String jarprop = key.replace(".version", "");
                final String old = versions.put(jarprop, value);
                propNames.add(jarprop);
                if (old != null) {
                    fail("Already have entry for " + key);
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
        // remove optional hsqldb, jacoco and sonar jars (required for coverage
        // reporting, not required for jmeter)
        for (String optLib : Arrays.asList("jacocoant", "sonarqube-ant-task", "hsqldb", "activemq-all", "mina-core",
                "ftplet-api", "ftpserver-core")) {
            propNames.remove(optLib);
            versions.remove(optLib);
        }
        prop = buildProp;
        final File licencesDir = getFileFromHome("licenses/bin");
        liceFiles = Arrays.stream(licencesDir.list()).filter(name -> !name.equalsIgnoreCase("README.txt"))
                .filter(name -> !name.equals(".svn")) // Ignore old-style SVN workspaces
                .map(name -> name.replace(".txt", "")).collect(Collectors.toSet());
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission(Permission permission) {
                if (permission.getName().startsWith("exitVM.1")) {
                    throw new AccessControlException(permission.getName());
                }
            }
        };
        System.setSecurityManager(securityManager);
    }

    @Test
    public void testJmxDoesntExist() {
        JMeter jmeter = new JMeter();
        String command = "-n -t testPlan.jmx";
        String[] args = command.split(" ");
        try {
            jmeter.start(args);
        } catch (AccessControlException ex) {
            assertEquals("The jmx file does not exist, the system should exit with system.exit(1)",
                    ex.getMessage(), "exitVM.1");
        } 
    }

    @Test
    public void testJmxExist() throws IOException {
        File temp = File.createTempFile("testPlan", ".jmx");
        String testPlan = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.2-SNAPSHOT\">\n" + "  <hashTree>\n"
                + "    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"Test Plan\" enabled=\"true\">\n"
                + "      <stringProp name=\"TestPlan.comments\"></stringProp>\n"
                + "      <boolProp name=\"TestPlan.functional_mode\">false</boolProp>\n"
                + "      <boolProp name=\"TestPlan.tearDown_on_shutdown\">true</boolProp>\n"
                + "      <boolProp name=\"TestPlan.serialize_threadgroups\">false</boolProp>\n"
                + "      <elementProp name=\"TestPlan.user_defined_variables\" elementType=\"Arguments\" guiclass=\"ArgumentsPanel\" testclass=\"Arguments\" testname=\"User Defined Variables\" enabled=\"true\">\n"
                + "        <collectionProp name=\"Arguments.arguments\"/>\n" + "      </elementProp>\n"
                + "      <stringProp name=\"TestPlan.user_define_classpath\"></stringProp>\n" + "    </TestPlan>\n"
                + "    <hashTree/>\n" + "  </hashTree>\n" + "</jmeterTestPlan>\n" + "";
        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(testPlan);
        out.close();
        JMeter jmeter = new JMeter();
        String command = "-n -t " + temp.getAbsolutePath();
        String[] args = command.split(" ");
        jmeter.start(args);
    }

    @Test
    public void testPluginDoesntExist() throws IOException {
        File temp = File.createTempFile("testPlan", ".jmx");
        String testPlan = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<jmeterTestPlan version=\"1.2\" properties=\"5.0\" jmeter=\"5.2-SNAPSHOT.20190506\">\n"
                + "  <hashTree>\n"
                + "    <TestPlan guiclass=\"TestPlanGui\" testclass=\"TestPlan\" testname=\"Test Plan\" enabled=\"true\">\n"
                + "      <stringProp name=\"TestPlan.comments\"></stringProp>\n"
                + "      <boolProp name=\"TestPlan.functional_mode\">false</boolProp>\n"
                + "      <boolProp name=\"TestPlan.tearDown_on_shutdown\">true</boolProp>\n"
                + "      <boolProp name=\"TestPlan.serialize_threadgroups\">false</boolProp>\n"
                + "      <elementProp name=\"TestPlan.user_defined_variables\" elementType=\"Arguments\" guiclass=\"ArgumentsPanel\" testclass=\"Arguments\" testname=\"User Defined Variables\" enabled=\"true\">\n"
                + "        <collectionProp name=\"Arguments.arguments\"/>\n" + "      </elementProp>\n"
                + "      <stringProp name=\"TestPlan.user_define_classpath\"></stringProp>\n" + "    </TestPlan>\n"
                + "    <hashTree>\n" + "      <hashTree>\n"
                + "        <kg.apc.jmeter.samplers.DummySampler guiclass=\"kg.apc.jmeter.samplers.DummySamplerGui\" testclass=\"kg.apc.jmeter.samplers.DummySampler\" testname=\"jp@gc - Dummy Sampler\" enabled=\"true\">\n"
                + "          <boolProp name=\"WAITING\">true</boolProp>\n"
                + "          <boolProp name=\"SUCCESFULL\">true</boolProp>\n"
                + "          <stringProp name=\"RESPONSE_CODE\">200</stringProp>\n"
                + "          <stringProp name=\"RESPONSE_MESSAGE\">OK</stringProp>\n"
                + "          <stringProp name=\"REQUEST_DATA\">{&quot;email&quot;:&quot;user1&quot;, &quot;password&quot;:&quot;password1&quot;}；</stringProp>\n"
                + "          <stringProp name=\"RESPONSE_DATA\">{&quot;successful&quot;: true, &quot;account_id&quot;:&quot;0123456789&quot;}</stringProp>\n"
                + "          <stringProp name=\"RESPONSE_TIME\">${__Random(50,500)}</stringProp>\n"
                + "          <stringProp name=\"LATENCY\">${__Random(1,50)}</stringProp>\n"
                + "          <stringProp name=\"CONNECT\">${__Random(1,5)}</stringProp>\n"
                + "        </kg.apc.jmeter.samplers.DummySampler>\n" + "      </hashTree>\n" + "    </hashTree>\n"
                + "  </hashTree>\n" + "</jmeterTestPlan>\n" + "" + "    <hashTree/>\n" + "  </hashTree>\n"
                + "</jmeterTestPlan>\n" + "";
        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(testPlan);
        out.close();
        JMeter jmeter = new JMeter();
        String command = "-n -t " + temp.getAbsolutePath();
        String[] args = command.split(" ");
        try {
            jmeter.start(args);
        } catch (AccessControlException ex) {
            assertEquals("The plugin is used for jmx file which doesn't exist, the system should exit with system.exit(1)",
                    ex.getMessage(), "exitVM.1");
        }
    }
      
    
}
