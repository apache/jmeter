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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.CorrelationTableModel;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Correlation {

    private static final Logger log = LoggerFactory.getLogger(Correlation.class);

    // Initialize member variables
    private static Map<String, List<String>> candidatesMap = new HashMap<>();
    private static Set<HTTPSamplerProxy> samplerSet = new HashSet<>();

    private static final String HTTP_TEST_SAMPLE_GUI = "org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui";
    private static final String TEST_NAME = "TestElement.name";
    public static final String TEXT_HTML = "text/html";
    public static final String AMPERSAND = "&";
    public static final String UNDERSCORE = "_";
    public static final String EQUAL = "=";

    private Correlation() {
    }

    /**
     * Compare the list of api-request objects and prepare the map containing
     * the parameter list which are candidates for correlation.
     *
     * @param file JMX file whose parameters need to be extracted
     */
    public static void extractParameters(File file) {

        if (file == null) {
            log.error("Unable to load JMX file.");
        }
        List<HTTPSamplerBase> sampleRequests = new ArrayList<>();
        List<HTTPSamplerBase> currentGuiSampleRequests = new ArrayList<>();
        GuiPackage guiPackage = GuiPackage.getInstance();

        // get HashTree for loaded JMX file from GUI instance.
        JMeterTreeNode rootNode = (JMeterTreeNode) guiPackage.getTreeModel().getRoot();
        // load browsed JMX file and convert it into
        // HTTPSamplerProxy object list.
        try {
            HashTree tree = SaveService.loadTree(file);
            convertSubTree(tree);
            samplerSet.forEach(proxy ->
                sampleRequests.add((HTTPSamplerBase) proxy));
            samplerSet.clear();

            getSamplerObjectList(rootNode);
            samplerSet.forEach(proxy ->
                currentGuiSampleRequests.add((HTTPSamplerBase) proxy));
            samplerSet.clear();

        } catch (IOException e) {
            log.error("Unable to parse JMX file.");
        }

        // create the map for API requests.
        Map<String, String> firstJmxMap = createJmxObjectMap(sampleRequests);
        Map<String, String> secondJmxMap = createJmxObjectMap(currentGuiSampleRequests);

        // compare the maps of API requests to identify the
        // list of correlation candidates.
        extractCorrelationCandidates(firstJmxMap, secondJmxMap);
        log.info("List of parameters required for correlation created.");

    }

    /**
     * It is used to replace the correlation parameter value with variable
     * expression.
     *
     * @param node
     * @param bodyParameterMap
     * @param guiPackage
     */
    private static void getSamplerObjectList(JMeterTreeNode node) {

        Enumeration<?> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode) enumNode.nextElement();
            TestElement testElement = child.getTestElement();
            if (testElement.getPropertyAsString(TestElement.GUI_CLASS).equals(HTTP_TEST_SAMPLE_GUI)) {

                HTTPSamplerProxy sample = (HTTPSamplerProxy) testElement;
                samplerSet.add(sample);

            }
            getSamplerObjectList(child);
        }
    }

    /**
     * recursively call the method to find the HTTPSamplerProxy object and
     * prepare a list.
     *
     * @param tree
     */
    private static void convertSubTree(HashTree tree) {

        for (Object o : new LinkedList<>(tree.list())) {
            TestElement item = (TestElement) o;
            if (item instanceof HTTPSamplerProxy) {
                HTTPSamplerProxy proxyObj = (HTTPSamplerProxy) item;
                samplerSet.add(proxyObj);
            }
            convertSubTree(tree.getTree(item));
        }
    }

    /**
     * create the map of JMX object.
     *
     * @param jmxObjectList List of TestElements
     */
    public static Map<String, String> createJmxObjectMap(List<HTTPSamplerBase> jmxObjectList) {

        Map<String, String> jmxObjectMap = new HashMap<>();

        for (HTTPSamplerBase testElement : jmxObjectList) {
            String queryString = testElement.getQueryString();
            String[] httpArguments = null;
            if (!StringUtils.isBlank(queryString)) {

                if (queryString.indexOf(AMPERSAND) >= 0) {
                    httpArguments = queryString.split(AMPERSAND);
                } else {
                    httpArguments = new String[] { queryString };
                }
                for (int loopCount = 0; loopCount < httpArguments.length; loopCount++) {

                    String[] arguments = httpArguments[loopCount].split(EQUAL);
                    if (arguments.length > 1) {
                        if (jmxObjectMap.get(arguments[0]) == null) {
                            jmxObjectMap.put(arguments[0], arguments[1]);
                        } else {
                            jmxObjectMap.put("C_" + loopCount + UNDERSCORE + arguments[0], arguments[1]);
                        }
                    }
                }
            }
            // extract the query parameters
            String testName = testElement.getPropertyAsString(TEST_NAME);
            if (testName != null && !StringUtils.isBlank(testName) && testName.contains("?")) {
                String[] queryParameters = testName.split("\\?")[1].split(AMPERSAND);
                for (int loopCount = 0; loopCount < queryParameters.length; loopCount++) {

                    String[] queryParams = queryParameters[loopCount].split(EQUAL);
                    if (queryParams.length > 1) {
                        if (jmxObjectMap.get(queryParams[0]) == null) {
                            jmxObjectMap.put(queryParams[0], queryParams[1]);
                        } else {
                            jmxObjectMap.put("C_" + loopCount + UNDERSCORE + queryParams[0], queryParams[1]);
                        }
                    }
                }
            }
        }
        return jmxObjectMap;
    }

    /**
     * compare the maps to extract the correlation candidate parameters.
     *
     * @param firstJmxMap
     *            Map for first JMX script
     * @param secondJmxMap
     *            Map for second JMX script
     */
    public static void extractCorrelationCandidates(Map<String, String> firstJmxMap, Map<String, String> secondJmxMap) {

        Set<Entry<String, String>> entries = firstJmxMap.entrySet();
        int k = 1;
        for (Entry<String, String> entry : entries) {

            List<String> values = new ArrayList<>();
            values.add(entry.getValue());
            values.add(secondJmxMap.get(entry.getKey()));

            if (entry.getValue() != null && secondJmxMap.get(entry.getKey()) != null
                    && !entry.getValue().equals(secondJmxMap.get(entry.getKey()))) {

                if (candidatesMap.get(entry.getKey()) == null) {
                    candidatesMap.put(entry.getKey(), values);
                } else {
                    candidatesMap.put("C_" + k + UNDERSCORE + entry.getKey(), values);
                    k++;
                }
            }

        }
        // display data into table
        displayDataToTable(candidatesMap);
        candidatesMap.clear();
    }

    /**
     * display the correlation candidates into table.
     *
     * @param candidatesMap
     *            Map having list of parameters which are a candidate for
     *            correlation.
     */
    private static void displayDataToTable(Map<String, List<String>> candidatesMap) {

        // display correlation candidates into table
        Object[][] data = new Object[candidatesMap.size()][4];
        Set<Map.Entry<String, List<String>>> mapSet = candidatesMap.entrySet();
        int outerLoopCount = 0;
        for (Entry<String, List<String>> entry : mapSet) {

            data[outerLoopCount][0] = Boolean.FALSE;
            int innerLoopCount = 1;
            data[outerLoopCount][innerLoopCount++] = entry.getKey();
            List<String> values = entry.getValue();

            for (String string : values) {
                data[outerLoopCount][innerLoopCount++] = string;
            }
            outerLoopCount++;
        }
        // set the data into Table model
        CorrelationTableModel.rowData = data;

    }

    /**
     * @param regularExtractors
     * @param arguments
     * @param bodyParameterMap
     * @throws UnsupportedEncodingException
     */
    public static void updateJxmFileWithRegularExtractors(List<Map<String, String>> regularExtractors,
            List<String> arguments, Map<String, String> bodyParameterMap) throws UnsupportedEncodingException {

        GuiPackage guiPackage = GuiPackage.getInstance();

        for (Map<String, String> extractor : regularExtractors) {
            // if content type is text/html
            if (extractor.get("contentType") != null && extractor.get("contentType").contains(TEXT_HTML)) {
                addExtractor(extractor.get("testname"), "org.apache.jmeter.extractor.gui.HtmlExtractorGui", extractor);
            } else {
                addExtractor(extractor.get("testname"), "org.apache.jmeter.extractor.gui.RegexExtractorGui", extractor);
            }
        }
        replaceVariables((JMeterTreeNode) guiPackage.getTreeModel().getRoot(), bodyParameterMap, guiPackage);
    }

    public static void addExtractor(String testName, String extractorTypeClassName, Map<String, String> extractor) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        try {
            guiPackage.updateCurrentNode();

            // create testElement
            TestElement temp = CorrelationExtractor.createExtractor(guiPackage, extractorTypeClassName, extractor);

            // get the node in which we want to add the extractor
            JMeterTreeNode parentNode = guiPackage
                    .getNodeOf(traverseAndFind(testName, (JMeterTreeNode) guiPackage.getTreeModel().getRoot()));

            JMeterTreeNode node = guiPackage.getTreeModel().addComponent(temp, parentNode);
            guiPackage.getNamingPolicy().nameOnCreation(node);
            guiPackage.getMainFrame().getTree().setSelectionPath(new TreePath(node.getPath()));
        } catch (Exception err) {
            log.error("Exception while adding a component to tree.", err); // $NON-NLS-1$
            String msg = err.getMessage();
            if (msg == null) {
                msg = err.toString();
            }
            JMeterUtils.reportErrorToUser(msg);
        }
    }

    /**
     * traverse all the nodes and find the node against the testName
     *
     * @param testName
     * @param node
     * @return TestElement
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
     * It is used to replace the correlation parameter value with variable
     * expression.
     *
     * @param node
     * @param bodyParameterMap
     * @param guiPackage
     * @throws UnsupportedEncodingException
     */
    private static void replaceVariables(JMeterTreeNode node, Map<String, String> bodyParameterMap,
            GuiPackage guiPackage) throws UnsupportedEncodingException {

        guiPackage.updateCurrentNode();
        Enumeration<?> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode) enumNode.nextElement();
            TestElement testElement = child.getTestElement();
            if (testElement.getPropertyAsString(TestElement.GUI_CLASS).equals(HTTP_TEST_SAMPLE_GUI)) {

                HTTPSamplerProxy sample = (HTTPSamplerProxy) testElement;
                Set<Entry<String, String>> entrySet = sample.getArguments().getArgumentsAsMap().entrySet();
                for (Entry<String, String> entry : entrySet) {
                    String encodedValue = java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());

                    Optional<String> firstKey = bodyParameterMap.entrySet().stream()
                            .filter(en -> Objects.equals(entry.getValue(), en.getValue())
                                    || Objects.equals(encodedValue, en.getValue()))
                            .map(Map.Entry::getKey).findFirst();

                    if (firstKey.isPresent()) {
                        sample.getArguments().removeArgument(entry.getKey());
                        sample.getArguments()
                                .addArgument(new HTTPArgument(firstKey.get(), "${" + firstKey.get() + "}"));
                    }
                }
            }
            replaceVariables(child, bodyParameterMap, guiPackage);
        }
    }
}
