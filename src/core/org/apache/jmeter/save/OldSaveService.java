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

//import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
//import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
//import org.apache.avalon.framework.configuration.DefaultConfigurationSerializer;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.samplers.SampleResult;
//import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.MapProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.NameUpdater;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.xml.sax.SAXException;

/**
 * This class restores the original Avalon XML format (not used by default).
 *
 * This may be removed in a future release.
 */
public final class OldSaveService {
    private static final Logger log = LoggingManager.getLoggerForClass();

    // ---------------------------------------------------------------------
    // XML RESULT FILE CONSTANTS AND FIELD NAME CONSTANTS
    // ---------------------------------------------------------------------

    // Shared with TestElementSaver
    static final String PRESERVE = "preserve"; // $NON-NLS-1$
    static final String XML_SPACE = "xml:space"; // $NON-NLS-1$

    private static final String ASSERTION_RESULT_TAG_NAME = "assertionResult"; // $NON-NLS-1$
    private static final String BINARY = "binary"; // $NON-NLS-1$
    private static final String DATA_TYPE = "dataType"; // $NON-NLS-1$
    private static final String ERROR = "error"; // $NON-NLS-1$
    private static final String FAILURE = "failure"; // $NON-NLS-1$
    private static final String FAILURE_MESSAGE = "failureMessage"; // $NON-NLS-1$
    private static final String LABEL = "label"; // $NON-NLS-1$
    private static final String RESPONSE_CODE = "responseCode"; // $NON-NLS-1$
    private static final String RESPONSE_MESSAGE = "responseMessage"; // $NON-NLS-1$
    private static final String SAMPLE_RESULT_TAG_NAME = "sampleResult"; // $NON-NLS-1$
    private static final String SUCCESSFUL = "success"; // $NON-NLS-1$
    private static final String THREAD_NAME = "threadName"; // $NON-NLS-1$
    private static final String TIME = "time"; // $NON-NLS-1$
    private static final String TIME_STAMP = "timeStamp"; // $NON-NLS-1$

    private static final DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();

    /**
     * Private constructor to prevent instantiation.
     */
    private OldSaveService() {
    }


//    public static void saveSubTree(HashTree subTree, OutputStream writer) throws IOException {
//        Configuration config = getConfigsFromTree(subTree).get(0);
//        DefaultConfigurationSerializer saver = new DefaultConfigurationSerializer();
//
//        saver.setIndent(true);
//        try {
//            saver.serialize(writer, config);
//        } catch (SAXException e) {
//            throw new IOException("SAX implementation problem");
//        } catch (ConfigurationException e) {
//            throw new IOException("Problem using Avalon Configuration tools");
//        }
//    }

    /**
     * Read sampleResult from Avalon XML file.
     *
     * @param config Avalon configuration
     * @return sample result
     */
    // Probably no point in converting this to return a SampleEvent
    private static SampleResult getSampleResult(Configuration config) {
        SampleResult result = new SampleResult(config.getAttributeAsLong(TIME_STAMP, 0L), config.getAttributeAsLong(
                TIME, 0L));

        result.setThreadName(config.getAttribute(THREAD_NAME, "")); // $NON-NLS-1$
        result.setDataType(config.getAttribute(DATA_TYPE, ""));
        result.setResponseCode(config.getAttribute(RESPONSE_CODE, "")); // $NON-NLS-1$
        result.setResponseMessage(config.getAttribute(RESPONSE_MESSAGE, "")); // $NON-NLS-1$
        result.setSuccessful(config.getAttributeAsBoolean(SUCCESSFUL, false));
        result.setSampleLabel(config.getAttribute(LABEL, "")); // $NON-NLS-1$
        result.setResponseData(getBinaryData(config.getChild(BINARY)));
        Configuration[] subResults = config.getChildren(SAMPLE_RESULT_TAG_NAME);

        for (int i = 0; i < subResults.length; i++) {
            result.storeSubResult(getSampleResult(subResults[i]));
        }
        Configuration[] assResults = config.getChildren(ASSERTION_RESULT_TAG_NAME);

        for (int i = 0; i < assResults.length; i++) {
            result.addAssertionResult(getAssertionResult(assResults[i]));
        }

        Configuration[] samplerData = config.getChildren("property"); // $NON-NLS-1$
        for (int i = 0; i < samplerData.length; i++) {
            result.setSamplerData(samplerData[i].getValue("")); // $NON-NLS-1$
        }
        return result;
    }

//    private static List<Configuration> getConfigsFromTree(HashTree subTree) {
//        Iterator<TestElement> iter = subTree.list().iterator();
//        List<Configuration> configs = new LinkedList<Configuration>();
//
//        while (iter.hasNext()) {
//            TestElement item = iter.next();
//            DefaultConfiguration config = new DefaultConfiguration("node", "node"); // $NON-NLS-1$ // $NON-NLS-2$
//
//            config.addChild(getConfigForTestElement(null, item));
//            List<Configuration> configList = getConfigsFromTree(subTree.getTree(item));
//            Iterator<Configuration> iter2 = configList.iterator();
//
//            while (iter2.hasNext()) {
//                config.addChild(iter2.next());
//            }
//            configs.add(config);
//        }
//        return configs;
//    }

//    private static Configuration getConfiguration(byte[] bin) {
//        DefaultConfiguration config = new DefaultConfiguration(BINARY, "JMeter Save Service"); // $NON-NLS-1$
//
//        try {
//            config.setValue(new String(bin, "UTF-8")); // $NON-NLS-1$
//        } catch (UnsupportedEncodingException e) {
//            log.error("", e); // $NON-NLS-1$
//        }
//        return config;
//    }

    private static byte[] getBinaryData(Configuration config) {
        if (config == null) {
            return new byte[0];
        }
        try {
            return config.getValue("").getBytes("UTF-8"); // $NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }

    private static AssertionResult getAssertionResult(Configuration config) {
        AssertionResult result = new AssertionResult(""); //TODO provide proper name?
        result.setError(config.getAttributeAsBoolean(ERROR, false));
        result.setFailure(config.getAttributeAsBoolean(FAILURE, false));
        result.setFailureMessage(config.getAttribute(FAILURE_MESSAGE, ""));
        return result;
    }

//    private static Configuration getConfiguration(AssertionResult assResult) {
//        DefaultConfiguration config = new DefaultConfiguration(ASSERTION_RESULT_TAG_NAME, "JMeter Save Service");
//
//        config.setAttribute(FAILURE_MESSAGE, assResult.getFailureMessage());
//        config.setAttribute(ERROR, "" + assResult.isError());
//        config.setAttribute(FAILURE, "" + assResult.isFailure());
//        return config;
//    }

//    /**
//     * This method determines the content of the result data that will be
//     * stored for the Avalon XML format.
//     *
//     * @param result
//     *            the object containing all of the data that has been collected.
//     * @param saveConfig
//     *            the configuration giving the data items to be saved.
//     * N.B. It is rather out of date, as many fields are not saved.
//     * However it is probably not worth updating, as no-one should be using the format.
//     */
//    public static Configuration getConfiguration(SampleResult result, SampleSaveConfiguration saveConfig) {
//        DefaultConfiguration config = new DefaultConfiguration(SAMPLE_RESULT_TAG_NAME, "JMeter Save Service"); // $NON-NLS-1$
//
//        if (saveConfig.saveTime()) {
//            config.setAttribute(TIME, String.valueOf(result.getTime()));
//        }
//        if (saveConfig.saveLabel()) {
//            config.setAttribute(LABEL, result.getSampleLabel());
//        }
//        if (saveConfig.saveCode()) {
//            config.setAttribute(RESPONSE_CODE, result.getResponseCode());
//        }
//        if (saveConfig.saveMessage()) {
//            config.setAttribute(RESPONSE_MESSAGE, result.getResponseMessage());
//        }
//        if (saveConfig.saveThreadName()) {
//            config.setAttribute(THREAD_NAME, result.getThreadName());
//        }
//        if (saveConfig.saveDataType()) {
//            config.setAttribute(DATA_TYPE, result.getDataType());
//        }
//
//        if (saveConfig.printMilliseconds()) {
//            config.setAttribute(TIME_STAMP, String.valueOf(result.getTimeStamp()));
//        } else if (saveConfig.formatter() != null) {
//            String stamp = saveConfig.formatter().format(new Date(result.getTimeStamp()));
//
//            config.setAttribute(TIME_STAMP, stamp);
//        }
//
//        if (saveConfig.saveSuccess()) {
//            config.setAttribute(SUCCESSFUL, Boolean.toString(result.isSuccessful()));
//        }
//
//        SampleResult[] subResults = result.getSubResults();
//
//        if (subResults != null) {
//            for (int i = 0; i < subResults.length; i++) {
//                config.addChild(getConfiguration(subResults[i], saveConfig));
//            }
//        }
//
//        AssertionResult[] assResults = result.getAssertionResults();
//
//        if (saveConfig.saveSamplerData(result)) {
//            config.addChild(createConfigForString("samplerData", result.getSamplerData())); // $NON-NLS-1$
//        }
//        if (saveConfig.saveAssertions() && assResults != null) {
//            for (int i = 0; i < assResults.length; i++) {
//                config.addChild(getConfiguration(assResults[i]));
//            }
//        }
//        if (saveConfig.saveResponseData(result)) {
//            config.addChild(getConfiguration(result.getResponseData()));
//        }
//        return config;
//    }

//    private static Configuration getConfigForTestElement(String named, TestElement item) {
//        TestElementSaver saver = new TestElementSaver(named);
//        item.traverse(saver);
//        Configuration config = saver.getConfiguration();
//        /*
//         * DefaultConfiguration config = new DefaultConfiguration("testelement",
//         * "testelement");
//         *
//         * if (named != null) { config.setAttribute("name", named); } if
//         * (item.getProperty(TestElement.TEST_CLASS) != null) {
//         * config.setAttribute("class", (String)
//         * item.getProperty(TestElement.TEST_CLASS)); } else {
//         * config.setAttribute("class", item.getClass().getName()); } Iterator
//         * iter = item.getPropertyNames().iterator();
//         *
//         * while (iter.hasNext()) { String name = (String) iter.next(); Object
//         * value = item.getProperty(name);
//         *
//         * if (value instanceof TestElement) {
//         * config.addChild(getConfigForTestElement(name, (TestElement) value)); }
//         * else if (value instanceof Collection) {
//         * config.addChild(createConfigForCollection(name, (Collection) value)); }
//         * else if (value != null) { config.addChild(createConfigForString(name,
//         * value.toString())); } }
//         */
//        return config;
//    }


//    private static Configuration createConfigForString(String name, String value) {
//        if (value == null) {
//            value = "";
//        }
//        DefaultConfiguration config = new DefaultConfiguration("property", "property");
//
//        config.setAttribute("name", name);
//        config.setValue(value);
//        config.setAttribute(XML_SPACE, PRESERVE);
//        return config;
//    }

    // Called by SaveService.loadTree(InputStream reader) if XStream loading fails
    public synchronized static HashTree loadSubTree(InputStream in) throws IOException {
        try {
            Configuration config = builder.build(in);
            HashTree loadedTree = generateNode(config);

            return loadedTree;
        } catch (ConfigurationException e) {
            String message = "Problem loading using Avalon Configuration tools";
            log.error(message, e);
            throw new IOException(message);
        } catch (SAXException e) {
            String message = "Problem with SAX implementation";
            log.error(message, e);
            throw new IOException(message);
        }
    }

    private static TestElement createTestElement(Configuration config) throws ConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException {
        TestElement element = null;

        String testClass = config.getAttribute("class"); // $NON-NLS-1$

        String gui_class=""; // $NON-NLS-1$
        Configuration[] children = config.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i].getName().equals("property")) { // $NON-NLS-1$
                if (children[i].getAttribute("name").equals(TestElement.GUI_CLASS)){ // $NON-NLS-1$
                    gui_class=children[i].getValue();
                }
            }
        }

        String newClass = NameUpdater.getCurrentTestName(testClass,gui_class);

        element = (TestElement) Class.forName(newClass).newInstance();

        for (int i = 0; i < children.length; i++) {
            if (children[i].getName().equals("property")) { // $NON-NLS-1$
                try {
                    JMeterProperty prop = createProperty(children[i], newClass);
                    if (prop!=null) {
                        element.setProperty(prop);
                    }
                } catch (Exception ex) {
                    log.error("Problem loading property", ex);
                    element.setProperty(children[i].getAttribute("name"), ""); // $NON-NLS-1$ // $NON-NLS-2$
                }
            } else if (children[i].getName().equals("testelement")) { // $NON-NLS-1$
                element.setProperty(new TestElementProperty(children[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
                        createTestElement(children[i])));
            } else if (children[i].getName().equals("collection")) { // $NON-NLS-1$
                element.setProperty(new CollectionProperty(children[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
                        createCollection(children[i], newClass)));
            } else if (children[i].getName().equals("map")) { // $NON-NLS-1$
                element.setProperty(new MapProperty(children[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
                        createMap(children[i],newClass)));
            }
        }
        return element;
    }

    private static Collection<JMeterProperty> createCollection(Configuration config, String testClass) throws ConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException {
        @SuppressWarnings("unchecked") // OK
        Collection<JMeterProperty> coll = (Collection<JMeterProperty>) Class.forName(config.getAttribute("class")).newInstance(); // $NON-NLS-1$
        Configuration[] items = config.getChildren();

        for (int i = 0; i < items.length; i++) {
            if (items[i].getName().equals("property")) { // $NON-NLS-1$
                JMeterProperty prop = createProperty(items[i], testClass);
                if (prop!=null) {
                    coll.add(prop);
                }
            } else if (items[i].getName().equals("testelement")) { // $NON-NLS-1$
                coll.add(new TestElementProperty(items[i].getAttribute("name", ""), createTestElement(items[i]))); // $NON-NLS-1$ // $NON-NLS-2$
            } else if (items[i].getName().equals("collection")) { // $NON-NLS-1$
                coll.add(new CollectionProperty(items[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
                        createCollection(items[i], testClass)));
            } else if (items[i].getName().equals("string")) { // $NON-NLS-1$
                JMeterProperty prop = createProperty(items[i], testClass);
                if (prop!=null) {
                    coll.add(prop);
                }
            } else if (items[i].getName().equals("map")) { // $NON-NLS-1$
                coll.add(new MapProperty(items[i].getAttribute("name", ""), createMap(items[i], testClass))); // $NON-NLS-1$ // $NON-NLS-2$
            }
        }
        return coll;
    }

    private static JMeterProperty createProperty(Configuration config, String testClass) throws IllegalAccessException,
            ClassNotFoundException, InstantiationException {
        String value = config.getValue(""); // $NON-NLS-1$
        String name = config.getAttribute("name", value); // $NON-NLS-1$
        String oname = name;
        String type = config.getAttribute("propType", StringProperty.class.getName()); // $NON-NLS-1$

        // Do upgrade translation:
        name = NameUpdater.getCurrentName(name, testClass);
        if (TestElement.GUI_CLASS.equals(name)) {
            value = NameUpdater.getCurrentName(value);
        } else if (TestElement.TEST_CLASS.equals(name)) {
            value=testClass; // must always agree
        } else {
            value = NameUpdater.getCurrentName(value, name, testClass);
        }

        // Delete any properties whose name converts to the empty string
        if (oname.length() != 0 && name.length()==0) {
            return null;
        }

        // Create the property:
        JMeterProperty prop = (JMeterProperty) Class.forName(type).newInstance();
        prop.setName(name);
        prop.setObjectValue(value);

        return prop;
    }

    private static Map<String, JMeterProperty> createMap(Configuration config, String testClass) throws ConfigurationException,
            ClassNotFoundException, IllegalAccessException, InstantiationException {
        @SuppressWarnings("unchecked") // OK
        Map<String, JMeterProperty> map = (Map<String, JMeterProperty>) Class.forName(config.getAttribute("class")).newInstance();
        Configuration[] items = config.getChildren();

        for (int i = 0; i < items.length; i++) {
            if (items[i].getName().equals("property")) { // $NON-NLS-1$
                JMeterProperty prop = createProperty(items[i], testClass);
                if (prop!=null) {
                    map.put(prop.getName(), prop);
                }
            } else if (items[i].getName().equals("testelement")) { // $NON-NLS-1$
                map.put(items[i].getAttribute("name", ""), new TestElementProperty(items[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
                        createTestElement(items[i])));
            } else if (items[i].getName().equals("collection")) { // $NON-NLS-1$
                map.put(items[i].getAttribute("name"),  // $NON-NLS-1$
                        new CollectionProperty(items[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
                        createCollection(items[i], testClass)));
            } else if (items[i].getName().equals("map")) { // $NON-NLS-1$
                map.put(items[i].getAttribute("name", ""),  // $NON-NLS-1$ // $NON-NLS-2$
                        new MapProperty(items[i].getAttribute("name", ""), // $NON-NLS-1$ // $NON-NLS-2$
                        createMap(items[i], testClass)));
            }
        }
        return map;
    }

    private static HashTree generateNode(Configuration config) {
        TestElement element = null;

        try {
            element = createTestElement(config.getChild("testelement")); // $NON-NLS-1$
        } catch (Exception e) {
            log.error("Problem loading part of file", e);
            return null;
        }
        HashTree subTree = new ListedHashTree(element);
        Configuration[] subNodes = config.getChildren("node"); // $NON-NLS-1$

        for (int i = 0; i < subNodes.length; i++) {
            HashTree t = generateNode(subNodes[i]);

            if (t != null) {
                subTree.add(element, t);
            }
        }
        return subTree;
    }

    // Called by ResultCollector#loadExistingFile() if XStream loading fails
    public static void processSamples(String filename, Visualizer visualizer, ResultCollector rc)
    throws SAXException, IOException, ConfigurationException
    {
        DefaultConfigurationBuilder cfgbuilder = new DefaultConfigurationBuilder();
        Configuration savedSamples = cfgbuilder.buildFromFile(filename);
        Configuration[] samples = savedSamples.getChildren();
        final boolean errorsOnly = rc.isErrorLogging();
        final boolean successOnly = rc.isSuccessOnlyLogging();
        for (int i = 0; i < samples.length; i++) {
            SampleResult result = OldSaveService.getSampleResult(samples[i]);
            if (ResultCollector.isSampleWanted(result.isSuccessful(), errorsOnly, successOnly)) {
                visualizer.add(result);
            }
        }
    }

    // Called by ResultCollector#recordResult()
//    public static String getSerializedSampleResult(
//            SampleResult result, DefaultConfigurationSerializer slzr, SampleSaveConfiguration cfg)
//        throws SAXException, IOException,
//            ConfigurationException {
//        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
//
//        slzr.serialize(tempOut, OldSaveService.getConfiguration(result, cfg));
//        String serVer = tempOut.toString();
//        String lineSep=System.getProperty("line.separator"); // $NON-NLS-1$
//        /*
//         * Remove the <?xml ... ?> prefix.
//         * When using the x-jars (xakan etc) or Java 1.4, the serialised output has a
//         * newline after the prefix. However, when using Java 1.5 without the x-jars, the output
//         * has no newline at all.
//         */
//        int index = serVer.indexOf(lineSep); // Is there a new-line?
//        if (index > -1) {// Yes, assume it follows the prefix
//            return serVer.substring(index);
//        }
//        if (serVer.startsWith("<?xml")){ // $NON-NLS-1$
//            index=serVer.indexOf("?>");// must exist // $NON-NLS-1$
//            return lineSep + serVer.substring(index+2);// +2 for ?>
//        }
//        return serVer;
//    }
}
