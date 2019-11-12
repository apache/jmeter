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

package org.apache.jmeter.protocol.http.gui.action;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.functions.CorrelationFunction;
import org.apache.jmeter.gui.CorrelationTableModel;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.CorrelationRecorder;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Correlation {

    private static final Logger log = LoggerFactory.getLogger(Correlation.class);

    private static Set<HTTPSamplerProxy> samplerSet = new LinkedHashSet<>();

    private static int count = 0;

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        Correlation.count = count;
    }

    private static final String PARANTHESES_OPEN = "("; //$NON-NLS-1$
    private static final String PARANTHESES_CLOSED = ")"; //$NON-NLS-1$

    private static final String CONTENT_TYPE = "contentType"; //$NON-NLS-1$
    private static final String HTTP_TEST_SAMPLE_GUI = "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui"; //$NON-NLS-1$
    private static final String TESTNAME = "testname"; //$NON-NLS-1$

    private static final String APPLICATION_XML = "application/xml"; //$NON-NLS-1$
    private static final String APPLICATION_JSON = "application/json"; //$NON-NLS-1$
    private static final String TEXT_HTML = "text/html"; //$NON-NLS-1$
    private static final String TEXT_XML = "text/xml"; //$NON-NLS-1$

    private static final String HTML_EXTRACTOR_GUI = "org.apache.jmeter.extractor.gui.HtmlExtractorGui"; //$NON-NLS-1$
    private static final String XPATH2_EXTRACTOR_GUI = "org.apache.jmeter.extractor.gui.XPath2ExtractorGui"; //$NON-NLS-1$
    private static final String JSONPATH_EXTRACTOR_GUI = "org.apache.jmeter.extractor.json.jsonpath.gui.JSONPostProcessorGui"; //$NON-NLS-1$
    private static final String REGEX_EXTRACTOR_GUI = "org.apache.jmeter.extractor.gui.RegexExtractorGui"; //$NON-NLS-1$

    private Correlation() {}

    /**
     * Compare the list of http-request objects from two TestPlans(one imported and
     * one currently in GUI) and prepare the map containing the parameter list which
     * are candidates for correlation.
     *
     * @param file JMX file whose parameters need to be extracted
     * @throws IllegalUserActionException when cannot correlate script
     */
    public static void extractParameters(File file) throws IllegalUserActionException {
        if (null == file) {
            throw new NullPointerException("JMX file is null. Please check the file and try again"); //$NON-NLS-1$
        }
        // Check if response buffer is empty
        if(CorrelationRecorder.buffer == null || CorrelationRecorder.buffer.isEmpty()) {
            throw new IllegalUserActionException("No Response data found. Make sure you have recorded the script and not opened it.");
        }
        // Load the imported JMX file and create a list of HTTP sample requests
        List<HTTPSamplerBase> importedJmxSampleRequestList = new ArrayList<>();
        HashTree tree = null;
        try {
            tree = SaveService.loadTree(file);
        } catch (IOException e) {
            throw new IllegalUserActionException("Could not load the JMX file. Please check the file and try again.",
                    e);
        }
        getHttpSampleRequests(tree);
        if (samplerSet.isEmpty()) {
            throw new IllegalUserActionException(
                    "Imported JMX file doesn't have any HTTP(S) Requests. Please check the file and try again.");
                    "Imported JMX file doesn't have any HTTP Requests. Please check the file and try again.");
        }
        samplerSet.forEach(proxy -> importedJmxSampleRequestList.add((HTTPSamplerBase) proxy));
        samplerSet.clear();

        // Create a list of HTTP sample requests from currently open JMX TestPlan
        List<HTTPSamplerBase> currentGuiSampleRequestList = new ArrayList<>();
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeNode rootNode = (JMeterTreeNode) guiPackage.getTreeModel().getRoot();
        getHttpSampleRequests(rootNode);
        if (samplerSet.isEmpty()) {
            throw new IllegalUserActionException(
                    "Current GUI TestPlan doesn't have any HTTP(S) Requests. Please record a plan and try again.");
                    "Current GUI TestPlan doesn't have any HTTP Requests. Please create/record a plan and try again.");
        }
        samplerSet.forEach(proxy -> currentGuiSampleRequestList.add((HTTPSamplerBase) proxy));
        samplerSet.clear();

        // Extract API parameters from HTTPSamplerProxy objects
        // TODO: Add parameters which are used in header
        Map<String, String> importedJmxHttpParameters = createJmxParameterMap(importedJmxSampleRequestList);
        Map<String, String> currentGuiHttpParameters = createJmxParameterMap(currentGuiSampleRequestList);

        // Compare the maps of API requests to identify the list of correlation candidates.
        Object[][] correlationCandidates = extractCorrelationCandidates(importedJmxHttpParameters, currentGuiHttpParameters);
        if (0 == correlationCandidates.length) {
            throw new IllegalUserActionException("No candidates for correlation found. Cannot Correlate Script");
        }
        log.info("List of parameters required for correlation is created.");
        // display data into Correlation table
        CorrelationTableModel.setRowData(correlationCandidates);
        // Set number of extractors added to zero
        setCount(0);
    }

    /**
     * Prepare a list of HTTPSamplerProxy objects from current TestPlan GUI opened
     * in JMeter
     *
     * @param node Root Node of the TestPlan tree
     */
    private static void getHttpSampleRequests(JMeterTreeNode node) {
        // traverse the testplan and add the sampler proxy elements to samplerSet
        Enumeration<?> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode) enumNode.nextElement();
            TestElement testElement = child.getTestElement();
            if (testElement.getPropertyAsString(TestElement.GUI_CLASS).equals(HTTP_TEST_SAMPLE_GUI)) {
                HTTPSamplerProxy sample = (HTTPSamplerProxy) testElement;
                samplerSet.add(sample);
            }
            getHttpSampleRequests(child);
        }
    }

    /**
     * Prepare a list of HTTPSamplerProxy objects from the browsed TestPlan Hashtree
     *
     * @param tree Hashtree containing all the TestElements
     */
    private static void getHttpSampleRequests(HashTree tree) {
        // traverse the testplan Hashtree and add the sampler proxy elements to samplerSet
        for (Object o : new LinkedList<>(tree.list())) {
            TestElement item = (TestElement) o;
            if (item instanceof HTTPSamplerProxy) {
                HTTPSamplerProxy proxyObj = (HTTPSamplerProxy) item;
                samplerSet.add(proxyObj);
            }
            getHttpSampleRequests(tree.getTree(item));
        }
    }

    /**
     * Create a map which contain all the parameters from the list of HTTP sampler
     * test elements
     *
     * @param httpSamplerBaseList List of HTTP Sampler Test Elements
     * @return Map with HTTP API parameters (name, value)
     */
    public static Map<String, String> createJmxParameterMap(List<HTTPSamplerBase> httpSamplerBaseList) {
        Map<String, String> jmxParameterMap = new HashMap<>();
        for (HTTPSamplerBase testElement : httpSamplerBaseList) {
            // extract the parameters from body
            extractParametersFromBody(testElement)
                    .forEach((key, value) -> addCorrelatableParamaterToMap(jmxParameterMap, key, value));
            // extract the query parameters from API path
            extractParametersFromApiPath(testElement)
                    .forEach((key, value) -> addCorrelatableParamaterToMap(jmxParameterMap, key, value));
        }
        return jmxParameterMap;
    }

    private static void addCorrelatableParamaterToMap(Map<String, String> jmxParameterMap, String key, String value) {
        // check if the map contains a parameter with the same key
        if (jmxParameterMap.get(key) == null) {
            jmxParameterMap.put(key, value);
        } else if (!jmxParameterMap.get(key).equals(value)) {
            // Change the parameter name if it already exists in the map and doesn't have
            // the same value as the already present parameter
            String modifiedName = changeCorrelatableParameterName(jmxParameterMap, key);
            jmxParameterMap.put(modifiedName, value);
        }
    }

    private static String changeCorrelatableParameterName(Map<String, String> jmxParameterMap, String name) {
        // count the number of parameters already present in the map with the same name
        Integer keyCount = jmxParameterMap.keySet().stream().filter(s -> s.startsWith(name + PARANTHESES_OPEN))
                .collect(Collectors.toSet()).size();
        keyCount++;
        return name + PARANTHESES_OPEN + keyCount.toString() + PARANTHESES_CLOSED;
    }

    private static Map<String, String> extractParametersFromBody(HTTPSamplerBase testElement) {
        Arguments arguments = testElement.getArguments();
        PropertyIterator iter = arguments.iterator();
        Map<String, String> jmxParameterMap = new HashMap<>();
        while (iter.hasNext()) {
            HTTPArgument item = null;
            Object objectValue = iter.next().getObjectValue();
            try {
                item = (HTTPArgument) objectValue;
            } catch (ClassCastException e) { // NOSONAR
                log.warn("Unexpected argument type: {} cannot be cast to HTTPArgument",
                        objectValue.getClass().getName());
                item = new HTTPArgument((Argument) objectValue);
            }
            final String name = item.getName();
            if (name.isEmpty()) {
                continue; // Skip parameters with a blank name (allows use of optional variables in
                          // parameter lists)
            }
            addCorrelatableParamaterToMap(jmxParameterMap, name, item.getValue());
        }
        return jmxParameterMap;
    }

    private static Map<String, String> extractParametersFromApiPath(HTTPSamplerBase testElement) {
        String path = testElement.getPath();
        List<NameValuePair> params = new ArrayList<>();
        Map<String, String> jmxParameterMap = new HashMap<>();
        try {
            params = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
            // return empty map
            return jmxParameterMap;
        }
        params.forEach(param -> addCorrelatableParamaterToMap(jmxParameterMap, param.getName(), param.getValue()));
        return jmxParameterMap;
    }

    /**
     * Compare the maps to extract the correlation candidate parameters.
     *
     * @param firstJmxMap  Map for first JMX script
     * @param secondJmxMap Map for second JMX script
     * @return 2D array of object containing parameter names and values
     */
    public static Object[][] extractCorrelationCandidates(Map<String, String> firstJmxMap,
            Map<String, String> secondJmxMap) {
        List<List<Object>> candidatesList = new ArrayList<>();
        // Compare the parameters from both maps
        // and if the keys are equal and values are different,
        // add them to candidates map
        firstJmxMap.forEach((key, value) -> {
            if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(secondJmxMap.get(key))
                    && !value.equals(secondJmxMap.get(key))) {
                candidatesList.add(Arrays.asList(Boolean.FALSE, key, value, secondJmxMap.get(key)));
            }
        });
        return candidatesList.stream().map(u -> u.toArray(new Object[0])).toArray(Object[][]::new);
    }

    /**
     * Update the GUI with all the created extractors present in extractors
     *
     * @param extractors       list of extractors
     * @param parameterMap Map containing parameter name and values which are a
     *                         candidate for correlation
     * @throws UnsupportedEncodingException when variable replacement failed
     */
    public static void updateJxmFileWithExtractors(List<Map<String, String>> extractors,
            Map<String, String> parameterMap) throws UnsupportedEncodingException {
        GuiPackage guiPackage = GuiPackage.getInstance();
        extractors.forEach(extractor -> {
            // Add extractors in TestPlan GUI based on content Type
            if (extractor.get(CONTENT_TYPE) != null) {
                if (extractor.get(CONTENT_TYPE).contains(TEXT_HTML)) {
                    addExtractor(extractor.get(TESTNAME), HTML_EXTRACTOR_GUI, extractor);
                } else if (extractor.get(CONTENT_TYPE).contains(APPLICATION_XML)
                        || extractor.get(CONTENT_TYPE).contains(TEXT_XML)) {
                    addExtractor(extractor.get(TESTNAME), XPATH2_EXTRACTOR_GUI, extractor);
                } else if (extractor.get(CONTENT_TYPE).contains(APPLICATION_JSON)) {
                    addExtractor(extractor.get(TESTNAME), JSONPATH_EXTRACTOR_GUI, extractor);
                }
            } else {
                addExtractor(extractor.get(TESTNAME), REGEX_EXTRACTOR_GUI, extractor);
            }
        });
        // Replace existing correlated parameter values by their correlated variable
        // alias
        replaceParameterValues((JMeterTreeNode) guiPackage.getTreeModel().getRoot(), parameterMap, guiPackage);
        JMeterUtils.reportInfoToUser("Correlation successful. Added " + count + " extractors.", "Successful");
        // clear the extractors map
        CorrelationExtractor.getListOfMap().clear();
    }

    /**
     * Add extractor to current testplan GUI
     *
     * @param testName               Node's name where the extractor should be added
     * @param extractorTypeClassName Type of extractor to be added
     * @param extractor              Map containing data to create Extractor
     */
    public static void addExtractor(String testName, String extractorTypeClassName, Map<String, String> extractor) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        guiPackage.updateCurrentNode();
        // create testElement
        TestElement extractorElement = CorrelationExtractor.createExtractorTestElement(guiPackage,
                extractorTypeClassName, extractor);
        if (extractorElement == null) {
            log.warn("Could not create extractor Test Element for {}", extractorTypeClassName);
            return;
        }
        // get the node in which the extractor will be added
        JMeterTreeNode parentNode = guiPackage
                .getNodeOf(traverseAndFind(testName, (JMeterTreeNode) guiPackage.getTreeModel().getRoot()));
        if(parentNode == null) {
            log.warn("Could not add Extractor for HTTP Sample: {}", testName);
            return;
        }
        try {
            JMeterTreeNode node = guiPackage.getTreeModel().addComponent(extractorElement, parentNode);
            guiPackage.getNamingPolicy().nameOnCreation(node);
            guiPackage.getMainFrame().getTree().setSelectionPath(new TreePath(node.getPath()));
        } catch (IllegalUserActionException err) {
            log.error("Exception while adding a component to tree. {}", err.getMessage());
        }
        setCount(getCount() + 1);
    }

    /**
     * Traverse all the nodes and find the node against the testName
     *
     * @param testName Node name to find
     * @param node     TestPlan Root node
     * @return TestElement node against testName
     */
    private static TestElement traverseAndFind(String testName, JMeterTreeNode node) {
        if (testName.equals(node.getName())
                && node.getTestElement().getPropertyAsString(TestElement.GUI_CLASS).equals(HTTP_TEST_SAMPLE_GUI)) {
            return node.getTestElement();
        }
        Enumeration<?> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode) enumNode.nextElement();
            TestElement result = traverseAndFind(testName, child);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Replace the correlation parameter value with variable expression. e.g token =
     * 6aBct12gs replaced with ${token}
     *
     * @param node             TestPlan root node
     * @param parameterMap Map containing correlation candidates and their
     *                         values
     * @param guiPackage       Current TestPlan GuiPackage object
     * @throws UnsupportedEncodingException when decoding value failed
     */
    private static void replaceParameterValues(JMeterTreeNode node, Map<String, String> parameterMap, GuiPackage guiPackage)
            throws UnsupportedEncodingException {
        guiPackage.updateCurrentNode();
        Enumeration<?> enumNode = node.children();
        // find all http requests
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode) enumNode.nextElement();
            TestElement testElement = child.getTestElement();
            if (testElement.getPropertyAsString(TestElement.GUI_CLASS).equals(HTTP_TEST_SAMPLE_GUI)) {
                HTTPSamplerProxy sample = (HTTPSamplerProxy) testElement;
                Set<Entry<String, String>> entrySet = sample.getArguments().getArgumentsAsMap().entrySet();
                // traverse all http arguments
                for (Entry<String, String> entry : entrySet) {
                    String encodedValue = java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                    // find the http argument whose value equals to the correlated parameter
                    Optional<String> firstKey = parameterMap.entrySet().stream()
                            .filter(en -> Objects.equals(entry.getValue(), en.getValue())
                                    || Objects.equals(encodedValue, en.getValue()))
                            .map(Map.Entry::getKey).findFirst();
                    if (firstKey.isPresent()) {
                        // Remove the existing http argument and replace its value with the correlated
                        // parameter alias
                        sample.getArguments().removeArgument(entry.getKey());
                        sample.getArguments().addArgument(new HTTPArgument(
                                CorrelationFunction.extractVariable(firstKey.get()), "${" + firstKey.get() + "}")); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                // replace query parameters in path
                String path = sample.getPath();
                if (StringUtils.isBlank(path)) {
                    continue;
                }
                Optional<String> keyToReplace = parameterMap.entrySet().stream()
                        .filter(en -> path.contains(en.getValue())).map(Map.Entry::getKey).findFirst();
                if (keyToReplace.isPresent()) {
                    sample.setPath(path.replace(parameterMap.get(keyToReplace.get()), "${" + keyToReplace.get() + "}")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            replaceParameterValues(child, parameterMap, guiPackage);
        }
    }
}
