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

package org.apache.jmeter.save;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.converters.BooleanPropertyConverter;
import org.apache.jmeter.save.converters.HashTreeConverter;
import org.apache.jmeter.save.converters.IntegerPropertyConverter;
import org.apache.jmeter.save.converters.LongPropertyConverter;
import org.apache.jmeter.save.converters.MultiPropertyConverter;
import org.apache.jmeter.save.converters.SampleResultConverter;
import org.apache.jmeter.save.converters.SampleSaveConfigurationConverter;
import org.apache.jmeter.save.converters.StringPropertyConverter;
import org.apache.jmeter.save.converters.TestElementConverter;
import org.apache.jmeter.save.converters.TestElementPropertyConverter;
import org.apache.jmeter.save.converters.TestResultWrapperConverter;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.alias.CannotResolveClassException;
import com.thoughtworks.xstream.alias.ClassMapper;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;

/**
 * author Mike Stover
 * author <a href="mailto:kcassell&#X0040;apache.org">Keith Cassell </a>
 */
public class SaveService {
    private static final XStream saver = new XStream();

	private static final Logger log = LoggingManager.getLoggerForClass();

    // Default file name
    private static final String SAVESERVICE_PROPERTIES_FILE = "/bin/saveservice.properties"; // $NON-NLS-1$

    // Property name used to define file name
    private static final String SAVESERVICE_PROPERTIES = "saveservice_properties"; // $NON-NLS-1$

    // Define file format property names
    private static final String FILE_FORMAT = "file_format"; // $NON-NLS-1$
    private static final String FILE_FORMAT_TESTPLAN = "file_format.testplan"; // $NON-NLS-1$
    private static final String FILE_FORMAT_TESTLOG = "file_format.testlog"; // $NON-NLS-1$

    // Define file format versions
	private static final String VERSION_2_0 = "2.0";  // $NON-NLS-1$
    //NOT USED private static final String VERSION_2_1 = "2.1";  // $NON-NLS-1$
    private static final String VERSION_2_2 = "2.2";  // $NON-NLS-1$

    // Default to overall format, and then to version 2.2
    public static final String TESTPLAN_FORMAT
        = JMeterUtils.getPropDefault(FILE_FORMAT_TESTPLAN
        , JMeterUtils.getPropDefault(FILE_FORMAT, VERSION_2_2));
    
    public static final String TESTLOG_FORMAT
        = JMeterUtils.getPropDefault(FILE_FORMAT_TESTLOG
        , JMeterUtils.getPropDefault(FILE_FORMAT, VERSION_2_2));

    private static final boolean IS_TESTPLAN_FORMAT_20
        = VERSION_2_0.equals(TESTPLAN_FORMAT);
    
    private static final boolean IS_TESTLOG_FORMAT_20
    = VERSION_2_0.equals(TESTLOG_FORMAT);

    private static final boolean IS_TESTPLAN_FORMAT_22
        = VERSION_2_2.equals(TESTPLAN_FORMAT);

    // Holds the mappings from the saveservice properties file
    private static final Properties aliasToClass = new Properties();

    // Holds the reverse mappings
    private static final Properties classToAlias = new Properties();
	
    // Version information for test plan header
    // This is written to JMX files by ScriptWrapperConverter
    // Also to JTL files by ResultCollector
	private static final String VERSION = "1.2"; // $NON-NLS-1$

    // This is written to JMX files by ScriptWrapperConverter
	private static String propertiesVersion = "";// read from properties file; written to JMX files
    private static final String PROPVERSION = "1.8";// Expected version $NON-NLS-1$

    // Internal information only
    private static String fileVersion = ""; // read from properties file// $NON-NLS-1$
	private static final String FILEVERSION = "408187";// Expected value $NON-NLS-1$

    static {
        log.info("Testplan (JMX) version: "+TESTPLAN_FORMAT+". Testlog (JTL) version: "+TESTLOG_FORMAT);
        initProps();
        checkVersions();
    }

	// Helper method to simplify alias creation from properties
	private static void makeAlias(String alias, String clazz) {
		try {
			saver.alias(alias, Class.forName(clazz));
            aliasToClass.setProperty(alias,clazz);
            Object oldval=classToAlias.setProperty(clazz,alias);
            if (oldval != null) {
                log.error("Duplicate alias detected for "+clazz+": "+alias+" & "+oldval);
            }
		} catch (ClassNotFoundException e) {
			log.warn("Could not set up alias " + alias + " " + e.toString());
		} catch (NoClassDefFoundError e) {
			log.warn("Could not set up alias " + alias + " " + e.toString());
		}
	}

    public static Properties loadProperties() throws IOException{
        Properties nameMap = new Properties();
            nameMap.load(new FileInputStream(JMeterUtils.getJMeterHome()
                    + JMeterUtils.getPropDefault(SAVESERVICE_PROPERTIES, SAVESERVICE_PROPERTIES_FILE)));
            return nameMap;
    }
	private static void initProps() {
		// Load the alias properties
		Properties nameMap = new Properties();
		try {
            nameMap = loadProperties();
            // now create the aliases
			Iterator it = nameMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry me = (Map.Entry) it.next();
				String key = (String) me.getKey();
				String val = (String) me.getValue();
				if (!key.startsWith("_")) {
					makeAlias(key, val);
				} else {
					// process special keys
					if (key.equalsIgnoreCase("_version")) { // $NON-NLS-1$
                        propertiesVersion = val;
						log.info("Using SaveService properties version " + propertiesVersion);
                    } else if (key.equalsIgnoreCase("_file_version")) { // $NON-NLS-1$
                            fileVersion = extractVersion(val);
                            log.info("Using SaveService properties file version " + fileVersion);
					} else {
						key = key.substring(1);// Remove the leading "_"
						try {
							if (val.trim().equals("collection")) { // $NON-NLS-1$
								saver.registerConverter((Converter) Class.forName(key).getConstructor(
										new Class[] { ClassMapper.class }).newInstance(
										new Object[] { saver.getClassMapper() }));
							} else if (val.trim().equals("mapping")) { // $NON-NLS-1$
								saver.registerConverter((Converter) Class.forName(key).getConstructor(
										new Class[] { ClassMapper.class }).newInstance(
										new Object[] { saver.getClassMapper() }));
							} else {
								saver.registerConverter((Converter) Class.forName(key).newInstance());
							}
						} catch (IllegalAccessException e1) {
							log.warn("Can't register a converter: " + key, e1);
						} catch (InstantiationException e1) {
							log.warn("Can't register a converter: " + key, e1);
						} catch (ClassNotFoundException e1) {
							log.warn("Can't register a converter: " + key, e1);
						} catch (IllegalArgumentException e1) {
							log.warn("Can't register a converter: " + key, e1);
						} catch (SecurityException e1) {
							log.warn("Can't register a converter: " + key, e1);
						} catch (InvocationTargetException e1) {
							log.warn("Can't register a converter: " + key, e1);
						} catch (NoSuchMethodException e1) {
							log.warn("Can't register a converter: " + key, e1);
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("Bad saveservice properties file", e);
		}
	}

    // For converters to use
    public static String aliasToClass(String s){
        String r = aliasToClass.getProperty(s);
        return r == null ? s : r;
    }
    
    // For converters to use
    public static String classToAlias(String s){
        String r = classToAlias.getProperty(s);
        return r == null ? s : r;
    }
    
	public static void saveTree(HashTree tree, Writer writer) throws Exception {
		ScriptWrapper wrapper = new ScriptWrapper();
		wrapper.testPlan = tree;
		saver.toXML(wrapper, writer);
		writer.write('\n');// Ensure terminated properly
	}

	public static void saveElement(Object el, Writer writer) throws Exception {
		saver.toXML(el, writer);
	}

	public static Object loadElement(InputStream in) throws Exception {
		return saver.fromXML(new InputStreamReader(in));
	}

	public static Object loadElement(Reader in) throws Exception {
		return saver.fromXML(in);
	}

	public synchronized static void saveSampleResult(SampleResult res, Writer writer) throws Exception {
		saver.toXML(res, writer);
		writer.write('\n');
	}

	public synchronized static void saveTestElement(TestElement elem, Writer writer) throws Exception {
		saver.toXML(elem, writer);
		writer.write('\n');
	}

	private static boolean versionsOK = true;

	// Extract version digits from String of the form #Revision: n.mm #
	// (where # is actually $ above)
	private static final String REVPFX = "$Revision: ";
	private static final String REVSFX = " $"; // $NON-NLS-1$

	private static String extractVersion(String rev) {
		if (rev.length() > REVPFX.length() + REVSFX.length()) {
			return rev.substring(REVPFX.length(), rev.length() - REVSFX.length());
		}
		return rev;
	}

	private static void checkVersion(Class clazz, String expected) {

		String actual = "*NONE*"; // $NON-NLS-1$
		try {
			actual = (String) clazz.getMethod("getVersion", null).invoke(null, null);
			actual = extractVersion(actual);
		} catch (Exception ignored) {
			// Not needed
		}
		if (0 != actual.compareTo(expected)) {
			versionsOK = false;
			log.warn("Version mismatch: expected '" + expected + "' found '" + actual + "' in " + clazz.getName());
		}
	}

    // Routines for TestSaveService
    static boolean checkPropertyVersion(){
        return SaveService.PROPVERSION.equals(SaveService.propertiesVersion);
    }
    
    static boolean checkFileVersion(){
        return SaveService.FILEVERSION.equals(SaveService.fileVersion);
    }

    static boolean checkVersions() {
		versionsOK = true;
		checkVersion(BooleanPropertyConverter.class, "493779"); // $NON-NLS-1$
		checkVersion(HashTreeConverter.class, "493779"); // $NON-NLS-1$
		checkVersion(IntegerPropertyConverter.class, "493779"); // $NON-NLS-1$
		checkVersion(LongPropertyConverter.class, "493779"); // $NON-NLS-1$
		checkVersion(MultiPropertyConverter.class, "493779"); // $NON-NLS-1$
		checkVersion(SampleResultConverter.class, "501270"); // $NON-NLS-1$

        // Not built until later, so need to use this method:
        try {
            checkVersion(
                    Class.forName("org.apache.jmeter.protocol.http.util.HTTPResultConverter"), // $NON-NLS-1$
                    "493789"); // $NON-NLS-1$
        } catch (ClassNotFoundException e) {
            versionsOK = false;
            log.warn(e.getLocalizedMessage());
        }
		checkVersion(StringPropertyConverter.class, "493779"); // $NON-NLS-1$
		checkVersion(TestElementConverter.class, "493779"); // $NON-NLS-1$
		checkVersion(TestElementPropertyConverter.class, "493779"); // $NON-NLS-1$
		checkVersion(ScriptWrapperConverter.class, "493779"); // $NON-NLS-1$
		checkVersion(TestResultWrapperConverter.class, "493779"); // $NON-NLS-1$
        checkVersion(SampleSaveConfigurationConverter.class,"493779"); // $NON-NLS-1$

        if (!PROPVERSION.equalsIgnoreCase(propertiesVersion)) {
			log.warn("Bad _version - expected " + PROPVERSION + ", found " + propertiesVersion + ".");
		}
        if (!FILEVERSION.equalsIgnoreCase(fileVersion)) {
            log.warn("Bad _file_version - expected " + FILEVERSION + ", found " + fileVersion +".");
        }
		if (versionsOK) {
			log.info("All converter versions present and correct");
		}
        return versionsOK;
	}

	public static TestResultWrapper loadTestResults(InputStream reader) throws Exception {
		TestResultWrapper wrapper = (TestResultWrapper) saver.fromXML(new InputStreamReader(reader));
		return wrapper;
	}

	public static HashTree loadTree(InputStream reader) throws Exception {
		if (!reader.markSupported()) {
			reader = new BufferedInputStream(reader);
		}
		reader.mark(Integer.MAX_VALUE);
		ScriptWrapper wrapper = null;
		try {
			wrapper = (ScriptWrapper) saver.fromXML(new InputStreamReader(reader));
			return wrapper.testPlan;
		} catch (CannotResolveClassException e) {
			log.warn("Problem loading new style: " + e.getLocalizedMessage());
			reader.reset();
			return OldSaveService.loadSubTree(reader);
		} catch (NoClassDefFoundError e) {
			log.warn("Missing class ", e);
			return null;
		}
	}

	public static boolean isSaveTestPlanFormat20() {
		return IS_TESTPLAN_FORMAT_20;
	}

	public static boolean isSaveTestLogFormat20() {
		return IS_TESTLOG_FORMAT_20;
	}

    // New test format - more compressed class names
    public static boolean isSaveTestPlanFormat22() {
        return IS_TESTPLAN_FORMAT_22;
    }

    
//  Normal output
//  ---- Debugging information ----
//  required-type       : org.apache.jorphan.collections.ListedHashTree 
//  cause-message       : WebServiceSampler : WebServiceSampler 
//  class               : org.apache.jmeter.save.ScriptWrapper 
//  message             : WebServiceSampler : WebServiceSampler 
//  line number         : 929 
//  path                : /jmeterTestPlan/hashTree/hashTree/hashTree[4]/hashTree[5]/WebServiceSampler 
//  cause-exception     : com.thoughtworks.xstream.alias.CannotResolveClassException 
//  -------------------------------

    /**
     * Simplify getMessage() output from XStream ConversionException
     * @param ce - ConversionException to analyse
     * @return string with details of error
     */
    public static String CEtoString(ConversionException ce){
        String msg = 
            "XStream ConversionException at line: " + ce.get("line number")
            + "\n" + ce.get("message")
            + "\nPerhaps a missing jar? See log file.";
        return msg;
    }

    public static String getPropertiesVersion() {
        return propertiesVersion;
    }

    public static String getVERSION() {
        return VERSION;
    }
}