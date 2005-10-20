/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.converters.BooleanPropertyConverter;
import org.apache.jmeter.save.converters.HashTreeConverter;
import org.apache.jmeter.save.converters.IntegerPropertyConverter;
import org.apache.jmeter.save.converters.LongPropertyConverter;
import org.apache.jmeter.save.converters.MultiPropertyConverter;
import org.apache.jmeter.save.converters.SampleResultConverter;
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
import com.thoughtworks.xstream.converters.Converter;

/**
 * @author Mike Stover
 * @author <a href="mailto:kcassell&#X0040;apache.org">Keith Cassell </a>
 */
public class SaveService {
	private static XStream saver = new XStream();

	private static Logger log = LoggingManager.getLoggerForClass();

	// Version information for test plan header
	public static final String version = "1.1";

	static String propertiesVersion = "";// read from properties file
    private static final String PROPVERSION = "1.7";

    // Internal information only
    private static String fileVersion = ""; // read from properties file
	private static final String FILEVERSION = "325411";

	// Helper method to simplify alias creation from properties
	private static void makeAlias(String alias, String clazz) {
		try {
			saver.alias(alias, Class.forName(clazz));
		} catch (ClassNotFoundException e) {
			log.warn("Could not set up alias " + alias + " " + e.toString());
		} catch (NoClassDefFoundError e) {
			log.warn("Could not set up alias " + alias + " " + e.toString());
		}
	}

	private static void initProps() {
		// Load the alias properties
		Properties nameMap = new Properties();
		try {
			nameMap.load(new FileInputStream(JMeterUtils.getJMeterHome()
					+ JMeterUtils.getPropDefault("saveservice_properties", "/bin/saveservice.properties")));
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
					if (key.equalsIgnoreCase("_version")) {
						log.info("Using SaveService properties file " + val);
						propertiesVersion = val;
                    } else if (key.equalsIgnoreCase("_file_version")) {
                            val = extractVersion(val);
                            log.info("Using SaveService properties file " + val);
                            fileVersion = val;
					} else {
						key = key.substring(1);
						try {
							if (val.trim().equals("collection")) {
								saver.registerConverter((Converter) Class.forName(key).getConstructor(
										new Class[] { ClassMapper.class, String.class }).newInstance(
										new Object[] { saver.getClassMapper(), "class" }));
							} else if (val.trim().equals("mapping")) {
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

	static {
		initProps();
		/*
		 * saver.registerConverter(new StringPropertyConverter());
		 * saver.registerConverter(new BooleanPropertyConverter());
		 * saver.registerConverter(new IntegerPropertyConverter());
		 * saver.registerConverter(new LongPropertyConverter());
		 * saver.registerConverter(new
		 * TestElementConverter(saver.getClassMapper(), "class"));
		 * saver.registerConverter(new MultiPropertyConverter(
		 * saver.getClassMapper(), "class")); saver.registerConverter(new
		 * TestElementPropertyConverter(saver .getClassMapper(), "class"));
		 * saver.registerConverter(new HashTreeConverter(saver.getClassMapper(),
		 * "class")); saver .registerConverter(new ScriptWrapperConverter(saver
		 * .getClassMapper())); saver.registerConverter(new
		 * SampleResultConverter(saver.getClassMapper(), "class"));
		 * saver.registerConverter(new TestResultWrapperConverter(saver
		 * .getClassMapper(), "class"));
		 */
		checkVersions();
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

	static boolean versionsOK = true;

	// Extract version digits from String of the form #Revision: n.mm #
	// (where # is actually $ above)
	private static final String REVPFX = "$Revision: ";

	private static final String REVSFX = " $";

	private static String extractVersion(String rev) {
		if (rev.length() > REVPFX.length() + REVSFX.length()) {
			return rev.substring(REVPFX.length(), rev.length() - REVSFX.length());
		} else {
			return rev;
		}
	}

	private static void checkVersion(Class clazz, String expected) {

		String actual = "*NONE*";
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

	private static void checkVersions() {
		versionsOK = true;
		checkVersion(BooleanPropertyConverter.class, "325542");
		checkVersion(HashTreeConverter.class, "325542");
		checkVersion(IntegerPropertyConverter.class, "325542");
		checkVersion(LongPropertyConverter.class, "325542");
		checkVersion(MultiPropertyConverter.class, "325542");
		checkVersion(SampleResultConverter.class, "325542");
		/*
		 * Should check this, but tricky to do, because not built until later.
		 * 
		 * checkVersion(HTTPResultConverter.class, "1.6");
		 * 
		 */
		checkVersion(StringPropertyConverter.class, "325542");
		checkVersion(TestElementConverter.class, "325542");
		checkVersion(TestElementPropertyConverter.class, "325542");
		checkVersion(ScriptWrapperConverter.class, "325542");
		checkVersion(TestResultWrapperConverter.class, "325542");
		if (!PROPVERSION.equalsIgnoreCase(propertiesVersion)) {
			log.warn("Bad _version - expected " + PROPVERSION + ", found " + propertiesVersion + ".");
		}
        if (!FILEVERSION.equalsIgnoreCase(fileVersion)) {
            log.warn("Bad _file_version - expected " + FILEVERSION + ", found " + fileVersion +".");
        }
		if (versionsOK) {
			log.info("All converter versions present and correct");
		}
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
		return JMeterUtils.getPropDefault("file_format.testplan", "2.1").equals("2.0")
				|| JMeterUtils.getPropDefault("file_format", "2.1").equals("2.0");
	}

	public static boolean isSaveTestLogFormat20() {
		return JMeterUtils.getPropDefault("file_format.testlog", "2.1").equals("2.0")
				|| JMeterUtils.getPropDefault("file_format", "2.1").equals("2.0");
	}

	public static class Test extends JMeterTestCase {
		public Test() {
			super();
		}

		public Test(String name) {
			super(name);
		}

        public void testPropfile() throws Exception {
            assertEquals("Property Version mismatch", PROPVERSION, propertiesVersion);            
            assertEquals("Property File Version mismatch", FILEVERSION, fileVersion);
        }
        
		public void testVersions() throws Exception {
			initProps();
			checkVersions();
			assertTrue("Unexpected version found", versionsOK);
		}
	}
}