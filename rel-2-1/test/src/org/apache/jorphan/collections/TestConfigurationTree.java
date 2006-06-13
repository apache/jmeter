/*
 * Created on Feb 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.jorphan.collections;

import java.io.FileReader;
import java.util.Properties;

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jorphan.io.TextFile;

/**
 * @author mike
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class TestConfigurationTree extends JMeterTestCase {
	ConfigurationTree config;

	/**
	 * @param name
	 */
	public TestConfigurationTree(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		config = new ConfigurationTree("jmeterConfig");
		config.add("param1/value1");
		config.add("param2/value2");
		config.add("param3/value3");
		config.add("group1/param6/value6");
		config.add("group1/param7/value7");
		config.add("group1/param8/value8");
		config.add("param4/value4");
		config.add("group2/param9/value9");
		config.add("group2/param10/value10");
		config.add("school/announcement", "This is a special announcement.\nToday is Thursday.");
		config.add("school/announcement/date", "Thursday");
		config.setProperty("school/announcement/title", "Way\n\tto\n\t\tgo");
		config.add("school/subjects/", new String[] { "art", "science", "math", "english" });
		config.setValue("group1", "EON Developers");
	}

	public void testToString() throws Exception {
		assertEquals("EON Developers", config.getValue("group1"));
		config.setProperty("website/url", "http://mydomain.com/homepage.jsp");
		String props = config.toString();
		TextFile tf = new TextFile(findTestFile("testfiles/configurationTest.xml"));
		tf.setText(config.toString());
		ConfigurationTree newTree = ConfigurationTree.fromXML(new FileReader(
				findTestFile("testfiles/configurationTest.xml")));
		assertEquals("EON Developers", newTree.getValue("group1"));
		assertEquals(props, newTree.toString());
		assertEquals("math", config.getPropertyNames("school/subjects")[2]);
		assertEquals("math", newTree.getPropertyNames("school/subjects")[2]);
		assertEquals("http://mydomain.com/homepage.jsp", config.remove("website/url"));
	}

	public void testManipulation1() throws Exception {
		config.put("db/driver", "oracle.jdbc.driver.OracleDriver");
		assertEquals("oracle.jdbc.driver.OracleDriver", config.getProperty("db/driver"));
		config.add("db/url/my db's url");
		assertEquals("my db's url", config.getProperty("db/url"));
		config.setProperty("website/url", "http://mydomain.com/homepage.jsp");
		assertEquals("http://mydomain.com/homepage.jsp", config.getProperty("website/url"));
		config.replace("db/driver", "resin_db/resin_driver");
		assertEquals("oracle.jdbc.driver.OracleDriver", config.getProperty("resin_db/resin_driver"));
		assertEquals("my db's url", config.getProperty("resin_db/url"));
		config.remove("resin_db/resin_driver");
		assertNull(config.getProperty("resin_db/resin_driver"));
	}

	public void testBigLoad() throws Exception {
		ConfigurationTree tree = new ConfigurationTree(new FileReader("testfiles/test_config.xml"));
		assertEquals("proxy.apache.org", tree.getValue("services/org.apache.service.DocumentService/proxy"));
		assertEquals("Manager Notification", tree
				.getPropertyNames("services/org.apache.service.PreferenceService/preferenceSql")[6]);
		assertEquals("JMeter", tree.getValue());
		assertEquals("getBuddyList.sql", tree
				.getProperty("services/org.apache.service.PreferenceService/preferenceSql/Buddy List/sql"));
		assertEquals("org.apache.service.dbObjects", tree
				.getPropertyNames("services/org.apache.service.sql.ObjectMappingService/packages")[0]);
		String expect = "The system could not find the resource you requested.  Please check your request and try again."
				+ System.getProperty("line.separator")
				+ "If the problem persists, report it to the system administrators.";
		String actual = tree
				.getProperty("services/org.apache.service.webaction.error.Redirector/exceptions/NoSuchObjectException/msg");
		assertEquals(expect.length(), actual.length());
		assertEquals(expect, actual);
	}

	public void testAsProperties() throws Exception {
		Properties props = config.getAsProperties();
		assertEquals("value4", props.getProperty("param4"));
	}

	public void testAddProps() throws Exception {
		Properties props = new Properties();
		props.setProperty("name", "Mike");
		props.setProperty("pets", "dog");
		config.add(props);
		assertEquals("dog", config.getProperty("pets"));
	}

	public void testDefault() throws Exception {
		assertEquals("default", config.getProperty("notThere", "default"));
	}
}
