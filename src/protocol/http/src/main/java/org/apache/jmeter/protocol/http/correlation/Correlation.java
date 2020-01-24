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

package org.apache.jmeter.protocol.http.correlation;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.apache.jmeter.extractor.gui.BoundaryExtractorGui;
import org.apache.jmeter.extractor.gui.HtmlExtractorGui;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.extractor.gui.XPath2ExtractorGui;
import org.apache.jmeter.extractor.json.jsonpath.gui.JSONPostProcessorGui;
import org.apache.jmeter.gui.CorrelationTableModel;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.correlation.extractordata.BoundaryExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.ExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.HtmlExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.JsonPathExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.RegexExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.XPath2ExtractorData;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
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

    private static int addedExtractorCount = 0;

    public static int getAddedExtractorCount() {
        return addedExtractorCount;
    }

    public static void setAddedExtractorCount(int addedExtractorCount) {
        Correlation.addedExtractorCount = addedExtractorCount;
    }

    private static final String PARANTHESES_OPEN = "("; //$NON-NLS-1$
    private static final String PARANTHESES_CLOSED = ")"; //$NON-NLS-1$

    private static final String BEARER_AUTH = "Bearer"; // $NON-NLS-1$

    private Correlation() {}

    /**
     * Compare the list of HTTP-request objects from two TestPlans(one imported and
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
        // Load the imported JMX file
        HashTree tree = null;
        try {
            tree = SaveService.loadTree(file);
        } catch (IOException e) {
            throw new IllegalUserActionException("Could not load the JMX file. Please check the file and try again.",
                    e);
        }
        // Create a list of HTTP sample requests and HeaderManagers for imported JMX test plan
        List<HTTPSamplerBase> importedJmxSampleRequestList = new ArrayList<>();
        getHttpSampleRequests(tree, importedJmxSampleRequestList);
        if (importedJmxSampleRequestList.isEmpty()) {
            throw new IllegalUserActionException(
                    "Imported JMX file doesn't have any HTTP(S) Requests. Please check the file and try again.");
        }
        List<HeaderManager> importedJmxHeaderManagerList = new ArrayList<>();
        getHeaderManagers(tree, importedJmxHeaderManagerList);

        // Find the HTTPSamplerProxy Nodes in currently open JMX test plan
        GuiPackage guiPackage = GuiPackage.getInstance();
        List<JMeterTreeNode> sampleList = guiPackage.getTreeModel().getNodesOfType(HTTPSamplerProxy.class);
        if (sampleList.isEmpty()) {
            throw new IllegalUserActionException(
                    "Current GUI TestPlan doesn't have any HTTP(S) Requests. Please record a plan and try again.");
        }
        // Check if response buffer is empty
        if (CorrelationRecorder.getBuffer() == null || CorrelationRecorder.getBuffer().isEmpty()) {
            throw new IllegalUserActionException(
                    "No Response data found. Make sure you have recorded the script and not opened it.");
        }
        // Create List of HTTPSamplerBase and HeaderManager for currently open JMX test plan
        List<HTTPSamplerBase> currentGuiSampleRequestList = sampleList.stream()
                .map(node -> (HTTPSamplerBase) node.getTestElement()).collect(Collectors.toList());
        List<HeaderManager> currentGuiheaderList = guiPackage.getTreeModel().getNodesOfType(HeaderManager.class)
                .stream().map(node -> (HeaderManager) node.getTestElement()).collect(Collectors.toList());
        // Extract API parameters from HTTPSamplerProxy objects
        Map<String, String> importedJmxHttpParameters = createJmxParameterMap(importedJmxSampleRequestList,
                importedJmxHeaderManagerList);
        Map<String, String> currentGuiHttpParameters = createJmxParameterMap(currentGuiSampleRequestList,
                currentGuiheaderList);
        // Compare the maps of API requests to identify the list of correlation
        // candidates.
        Object[][] correlationCandidates = extractCorrelationCandidates(importedJmxHttpParameters,
                currentGuiHttpParameters);
        if (0 == correlationCandidates.length) {
            throw new IllegalUserActionException("No candidates for correlation found. Cannot Correlate Script");
        }
        log.info("List of parameters required for correlation is created.");
        // display data into Correlation table
        CorrelationTableModel.setRowData(correlationCandidates);
        // Set number of extractors added to zero
        setAddedExtractorCount(0);
    }

    /**
     * Prepare a list of HeaderManager objects from the browsed TestPlan Hashtree
     *
     * @param tree                         Hashtree containing all the TestElements
     * @param importedJmxHeaderManagerList List of HeaderManager elements
     */
    private static void getHeaderManagers(HashTree tree, List<HeaderManager> importedJmxHeaderManagerList) {
        // traverse the testplan Hashtree and add the HeaderManager elements to
        // importedJmxHeaderManagerList
        for (Object o : new LinkedList<>(tree.list())) {
            TestElement item = (TestElement) o;
            if (item instanceof HeaderManager) {
                HeaderManager header = (HeaderManager) item;
                importedJmxHeaderManagerList.add(header);
            }
            getHeaderManagers(tree.getTree(item), importedJmxHeaderManagerList);
        }
    }

    /**
     * Prepare a list of HTTPSamplerBase objects from the browsed TestPlan Hashtree
     *
     * @param tree        Hashtree containing all the TestElements
     * @param samplerList List of HTTPSamplerBase elements
     */
    private static void getHttpSampleRequests(HashTree tree, List<HTTPSamplerBase> samplerList) {
        // traverse the testplan Hashtree and add the samplerbase elements to
        // samplerList
        for (Object o : new LinkedList<>(tree.list())) {
            TestElement item = (TestElement) o;
            if (item instanceof HTTPSamplerProxy) {
                HTTPSamplerBase request = (HTTPSamplerBase) item;
                samplerList.add(request);
            }
            getHttpSampleRequests(tree.getTree(item), samplerList);
        }
    }

    /**
     * Create a map which contain all the parameters from the list of
     * HTTPSamplerBase test elements
     *
     * @param httpSamplerBaseList List of HTTPSamplerBase Test Elements
     * @param headerManagerList   List of HeaderManager Test Elements
     * @return Map with HTTP API parameters (name, value)
     */
    public static Map<String, String> createJmxParameterMap(List<HTTPSamplerBase> httpSamplerBaseList,
            List<HeaderManager> headerManagerList) {
        Map<String, String> jmxParameterMap = new HashMap<>();
        for (HTTPSamplerBase testElement : httpSamplerBaseList) {
            // extract the parameters from body
            extractParametersFromBody(testElement, jmxParameterMap);
            // extract the query parameters from API path
            extractParametersFromApiPath(testElement, jmxParameterMap);
        }
        for (HeaderManager header : headerManagerList) {
            // extract the parameters from Header
            extractParametersFromHeader(header, jmxParameterMap);
        }
        return jmxParameterMap;
    }

    private static void addCorrelatableParameterToMap(Map<String, String> jmxParameterMap, String key, String value) {
        // check if the map contains a parameter with the same key
        if (jmxParameterMap.get(key) == null) {
            jmxParameterMap.put(key, value);
        } else if (!jmxParameterMap.get(key).equals(value) && (jmxParameterMap.entrySet().stream()
                .filter(x -> x.getValue().equals(value)).collect(Collectors.toList())).isEmpty()) {
            // Change the parameter name if it exists in the map and doesn't have
            // the same value as existing parameter
            String modifiedName = changeCorrelatableParameterName(jmxParameterMap, key);
            jmxParameterMap.put(modifiedName, value);
        }
    }

    private static String changeCorrelatableParameterName(Map<String, String> jmxParameterMap, String name) {
        // count the number of parameters present in the map with the same name
        Integer keyCount = jmxParameterMap.keySet().stream().filter(s -> s.startsWith(name + PARANTHESES_OPEN))
                .collect(Collectors.toSet()).size();
        keyCount++;
        return name + PARANTHESES_OPEN + keyCount.toString() + PARANTHESES_CLOSED;
    }

    private static void extractParametersFromHeader(HeaderManager header, Map<String, String> jmxParameterMap) {
        PropertyIterator itr = header.getHeaders().iterator();
        while (itr.hasNext()) {
            Header headerObj = (Header) itr.next().getObjectValue();
            // Get the Authorization header
            if (headerObj.getName().equals(HTTPConstants.HEADER_AUTHORIZATION)
                    && headerObj.getValue().split(" ").length >= 2) {
                String authHeaderType = headerObj.getValue().trim().split(" ")[0];
                // Correlate the Bearer Type
                if (authHeaderType.equals(BEARER_AUTH)) {
                    addCorrelatableParameterToMap(jmxParameterMap, headerObj.getName(),
                            headerObj.getValue().trim().split(" ")[1]);
                }
                // check only authorization header and return
                return;
            }
        }
    }

    private static void extractParametersFromBody(HTTPSamplerBase testElement, Map<String, String> jmxParameterMap) {
        Arguments arguments = testElement.getArguments();
        PropertyIterator iter = arguments.iterator();
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
            addCorrelatableParameterToMap(jmxParameterMap, name, item.getValue());
        }
    }

    private static void extractParametersFromApiPath(HTTPSamplerBase testElement, Map<String, String> jmxParameterMap) {
        String path = testElement.getPath();
        // Use sampler's content encoding to decode the path
        // Ref: DefaultSamplerCreator#computePath
        String contentEncoding = testElement.getContentEncoding();
        Charset charset = getCharset(contentEncoding);
        List<NameValuePair> params = new ArrayList<>();
        try {
            params = URLEncodedUtils.parse(new URI(path), charset);
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
            return;
        }
        params.forEach(param -> addCorrelatableParameterToMap(jmxParameterMap, param.getName(), param.getValue()));
    }

    /**
     * Get the charset for the specified string. Default to UTF_8
     *
     * @param contentEncoding String name
     * @return Charset or UTF_8 charset
     */
    private static Charset getCharset(String contentEncoding) {
        // try to create Charset for contentEncoding
        // If failed, return UTF_8
        if (StringUtils.isBlank(contentEncoding)) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName(contentEncoding);
        } catch (IllegalArgumentException e) {
            return StandardCharsets.UTF_8;
        }
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
        // convert it to 2D array (as JTable takes it) and return
        return candidatesList.stream().map(u -> u.toArray(new Object[0])).toArray(Object[][]::new);
    }

    /**
     * Update the GUI with all the created extractors present in extractors
     *
     * @param extractors   list of extractors
     * @param parameterMap Map containing parameter name and values which are a
     *                     candidate for correlation
     */
    public static void updateJxmFileWithExtractors(List<ExtractorData> extractors, Map<String, String> parameterMap) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        extractors.forEach(extractor -> {
            // Add extractors in TestPlan GUI based on their Type
            if (extractor instanceof HtmlExtractorData) {
                addExtractor(extractor.getTestName(), HtmlExtractorGui.class.getName(), extractor);
            } else if (extractor instanceof XPath2ExtractorData) {
                addExtractor(extractor.getTestName(), XPath2ExtractorGui.class.getName(), extractor);
            } else if (extractor instanceof JsonPathExtractorData) {
                addExtractor(extractor.getTestName(), JSONPostProcessorGui.class.getName(), extractor);
            } else if (extractor instanceof RegexExtractorData) {
                addExtractor(extractor.getTestName(), RegexExtractorGui.class.getName(), extractor);
            } else if (extractor instanceof BoundaryExtractorData) {
                addExtractor(extractor.getTestName(), BoundaryExtractorGui.class.getName(), extractor);
            }
        });
        // Replace existing correlated parameter values by their correlated variable
        // alias
        replaceParameterValues((JMeterTreeNode) guiPackage.getTreeModel().getRoot(), parameterMap, guiPackage);
        // Show Success/Failure information
        int expectedExtractorsToBeAdded = parameterMap.size();
        // Case 1: There can be parameters which are identified as a correlatable
        // parameter but
        // are not found in response, in that case extractors list won't have extractors
        // for those
        // parameters but the parameters will be present in parameterMap
        // Check if one extractor for all the selected parameters has been created
        int identifiedExtractorCount = 0;
        for (Entry<String, String> entry : parameterMap.entrySet()) {
            if (extractors.stream().filter(extractor -> extractor.getRefname().equals(entry.getKey())).count() >= 1) {
                identifiedExtractorCount++;
            }
        }
        // Case 2: It is not necessary that all the identified extractors will be added
        // to GUI
        // Check if the created extractors are added
        // Show the user only the final number of extractors which were added
        if (expectedExtractorsToBeAdded != getAddedExtractorCount()) {
            JMeterUtils.reportInfoToUser(getAddedExtractorCount() + " extractors added.\n"
                    + (expectedExtractorsToBeAdded - getAddedExtractorCount()) + " extractors not added.\n"
                    + "Please check logs for more information.", "Partially Correlated Script");
        } else {
            JMeterUtils.reportInfoToUser("Correlation successful. Added " + getAddedExtractorCount() + " extractors.",
                    "Successful");
        }
        // print granular information in logs
        log.info("Expected Extractors to be added: {}", expectedExtractorsToBeAdded);
        log.info("Identified Extractors to be added: {}", identifiedExtractorCount);
        log.info("Actual Extractors added: {}", getAddedExtractorCount());
        // clear the extractors map
        CorrelationExtractor.getListOfExtractor().clear();
    }

    /**
     * Add extractor to current testplan GUI
     *
     * @param testName               Node's name where the extractor should be added
     * @param extractorTypeClassName Type of extractor to be added
     * @param extractor              ExtractorData object
     */
    public static void addExtractor(String testName, String extractorTypeClassName, ExtractorData extractor) {
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
        if (parentNode == null) {
            log.warn("Could not add Extractor for HTTP Sample: {}", testName);
            return;
        }
        try {
            JMeterTreeNode node = guiPackage.getTreeModel().addComponent(extractorElement, parentNode);
            guiPackage.getNamingPolicy().nameOnCreation(node);
            guiPackage.getMainFrame().getTree().setSelectionPath(new TreePath(node.getPath()));
        } catch (IllegalUserActionException err) {
            log.error("Exception while adding a component to tree. {}", err.getMessage());
            return;
        }
        setAddedExtractorCount(getAddedExtractorCount() + 1);
    }

    /**
     * Traverse all the nodes and find the node against the testName
     *
     * @param testName Node name to find
     * @param node     TestPlan Root node
     * @return TestElement node against testName
     */
    private static TestElement traverseAndFind(String testName, JMeterTreeNode node) {
        if (testName.equals(node.getName()) && HTTPSamplerProxy.class.isInstance(node.getUserObject())) {
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
     * @param node         TestPlan root node
     * @param parameterMap Map containing correlation candidates and their values
     * @param guiPackage   Current TestPlan GuiPackage object
     */
    private static void replaceParameterValues(JMeterTreeNode node, Map<String, String> parameterMap,
            GuiPackage guiPackage) {
        guiPackage.updateCurrentNode();
        Enumeration<?> enumNode = node.children();
        while (enumNode.hasMoreElements()) {
            JMeterTreeNode child = (JMeterTreeNode) enumNode.nextElement();
            TestElement testElement = child.getTestElement();
            if (HTTPSamplerProxy.class.isInstance(child.getUserObject())) {
                HTTPSamplerProxy sample = (HTTPSamplerProxy) testElement;
                // Replace parameters in request body
                replaceParameterValuesInBody(sample, parameterMap);
                // Replace query parameters in path
                replaceParameterValuesInPath(sample, parameterMap);
            }
            // Replace header parameters
            else if (HeaderManager.class.isInstance(child.getUserObject())) {
                replaceParameterValuesInHeader(child, parameterMap);
            }
            replaceParameterValues(child, parameterMap, guiPackage);
        }
    }

    private static void replaceParameterValuesInHeader(JMeterTreeNode child, Map<String, String> parameterMap) {
        HeaderManager headerManager = (HeaderManager) child.getTestElement();
        PropertyIterator itr = headerManager.getHeaders().iterator();
        while (itr.hasNext()) {
            Header headerObj = (Header) itr.next().getObjectValue();
            // Update the header with correlated variable alias
            parameterMap.forEach((key, value) -> {
                String headerValue = headerObj.getValue();
                String newValue = headerValue.contains(value) ? headerValue.replace(value, "${" + key + "}") //$NON-NLS-1$ //$NON-NLS-2$
                        : headerValue;
                headerObj.setValue(newValue);
            });
        }
    }

    private static void replaceParameterValuesInPath(HTTPSamplerProxy sample, Map<String, String> parameterMap) {
        String path = sample.getPath();
        if (StringUtils.isBlank(path)) {
            return;
        }
        String encoding = getCharset(sample.getContentEncoding()).name();
        // path contains URL encoded parameter values so encode the value and find
        Optional<String> keyToReplace = parameterMap.entrySet().stream().filter(en -> {
            try {
                return path.contains(en.getValue()) || path.contains(URLEncoder.encode(en.getValue(), encoding));
            } catch (UnsupportedEncodingException e) {
                return path.contains(en.getValue());
            }
        }).map(Map.Entry::getKey).findFirst();
        if (keyToReplace.isPresent()) {
            String valueToReplace = parameterMap.get(keyToReplace.get());
            String encodedValue = null;
            try {
                encodedValue = URLEncoder.encode(valueToReplace, encoding);
            } catch (UnsupportedEncodingException e) {
                encodedValue = valueToReplace;
            }
            sample.setPath(path.replace(path.contains(valueToReplace) ? valueToReplace : encodedValue,
                    "${" + keyToReplace.get() + "}")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static void replaceParameterValuesInBody(HTTPSamplerProxy sample, Map<String, String> parameterMap) {
        Set<Entry<String, String>> entrySet = sample.getArguments().getArgumentsAsMap().entrySet();
        String encoding = getCharset(sample.getContentEncoding()).name();
        for (Entry<String, String> entry : entrySet) {
            String encodedValue;
            try {
                encodedValue = URLEncoder.encode(entry.getValue(), encoding);
            } catch (UnsupportedEncodingException e) {
                log.error("Could not encode value {}", entry.getValue());
                continue;
            }
            // find the HTTPArgument whose value equals to the correlated parameter
            Optional<String> firstKey = parameterMap.entrySet().stream()
                    .filter(en -> Objects.equals(entry.getValue(), en.getValue())
                            || Objects.equals(encodedValue, en.getValue()))
                    .map(Map.Entry::getKey).findFirst();
            if (firstKey.isPresent()) {
                // Remove the existing HTTPArgument and replace its value with the correlated
                // parameter alias
                sample.getArguments().removeArgument(entry.getKey());
                sample.getArguments().addArgument(new HTTPArgument(entry.getKey(), "${" + firstKey.get() + "}")); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
}
