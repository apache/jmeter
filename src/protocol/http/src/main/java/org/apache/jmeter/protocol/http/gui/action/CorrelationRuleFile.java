/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.gui.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.xml.stream.FactoryConfigurationError;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.extractor.Extractor;
import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONManager;
import org.apache.jmeter.gui.CorrelationTableModel;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.AbstractActionWithNoRunningTest;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.correlation.Correlation;
import org.apache.jmeter.protocol.http.correlation.CorrelationRule;
import org.apache.jmeter.protocol.http.correlation.extractordata.BoundaryExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.ExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.HtmlExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.JsonPathExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.RegexExtractorData;
import org.apache.jmeter.protocol.http.correlation.extractordata.XPath2ExtractorData;
import org.apache.jmeter.protocol.http.gui.CorrelationRuleFileGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jmeter.visualizers.CorrelationRecorder;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import net.sf.saxon.s9api.SaxonApiException;

public class CorrelationRuleFile extends AbstractActionWithNoRunningTest {

    private static final String[] exts = new String[] { ".json" }; //$NON-NLS-1$
    private static final Set<String> commands = new HashSet<>();

    private static final String ONE = "1";
    private static final String GROUP_NUMBER = "$1$"; //$NON-NLS-1$

    private static final String APPLICATION_JSON = "application/json"; //$NON-NLS-1$
    private static final String TEXT_HTML = "text/html"; //$NON-NLS-1$
    private static final String TEXT_XML = "text/xml"; //$NON-NLS-1$

    public static final String HTML_EXTRACTOR_TYPE = "css_extractor";
    public static final String XPATH2_EXTRACTOR_TYPE = "xpath2_extractor";
    public static final String JSON_EXTRACTOR_TYPE = "jsonpath_extractor";
    public static final String REGEX_EXTRACTOR_TYPE = "regex_extractor";
    public static final String REGEX_HEADER_TYPE = "regex_header";
    public static final String BOUNDARY_EXTRACTOR_TYPE = "boundary_extractor";

    private static final Logger log = LoggerFactory.getLogger(CorrelationRuleFile.class);

    static {
        commands.add(ActionNames.CORRELATION_IMPORT_RULE);
    }

    public CorrelationRuleFile() {
        super();
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    protected void doActionAfterCheck(ActionEvent e) throws IllegalUserActionException {
        final JFileChooser chooser = FileDialoger.promptToOpenRuleFile(exts);
        if (chooser == null) {
            return;
        }
        int retVal = chooser.showDialog(null, JMeterUtils.getResString("correlation_import_rule")); //$NON-NLS-1$
        if (retVal == JFileChooser.APPROVE_OPTION) {
            // parse JSON rule file and prepare rules list
            JSONParser jsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            JSONObject jsonData = null;
            try {
                jsonData = (JSONObject) jsonParser.parse(FileUtils.openInputStream(chooser.getSelectedFile()));
            } catch (ParseException | IOException | ClassCastException e1) {
                throw new IllegalUserActionException("Unable to parse rule file. Please check the file and try again.",
                        e1);
            }
            JSONArray rules = (JSONArray) jsonData.get("rule");
            if (rules == null) {
                throw new IllegalUserActionException("Invalid rule file.");
            }
            List<CorrelationRule> ruleObjectList = rules.stream().map(CorrelationRule::new)
                    .collect(Collectors.toList());
            if (ruleObjectList.isEmpty()) {
                throw new IllegalUserActionException("No rule present in rule file.");
            }
            // Prepare a list of parameters of Test plan present in the GUI
            Map<String, String> currentGuiHttpParameters = createCurrentGuiHttpParameters();
            if (currentGuiHttpParameters.isEmpty()) {
                throw new IllegalUserActionException(
                        "Testplan is empty or it doesn't have any parameters which can be correlated.");
            }
            if (CorrelationRecorder.getBuffer().isEmpty()) {
                throw new IllegalUserActionException("Testplan isn't recorded, please record the plan and try again.");
            }
            // Process the rule on response data and prepare a map with all the valid extractors possible
            // Also, simultaneously filter the matched values which aren't used as a parameter, i.e., do
            // not exist in currentGuiHttpParameters
            Map<CorrelationRule, List<ExtractorData>> ruleExtractorDataMap = processRules(ruleObjectList,
                    currentGuiHttpParameters);
            if (ruleExtractorDataMap.isEmpty()) {
                throw new IllegalUserActionException("Could not find any parameters after applying the rule.");
            }
            // prepare table data and show it on GUI
            Map<String, String> valuesToShowOnGui = new HashMap<>();
            ruleExtractorDataMap.forEach((key, value) -> value.forEach(extractor -> valuesToShowOnGui
                    .put(extractor.getRefname(), currentGuiHttpParameters.get(extractor.getRefname()))));
            Object[][] tableData = valuesToShowOnGui.entrySet().stream()
                    .map(entry -> new Object[] { Boolean.FALSE, entry.getKey(), entry.getValue() })
                    .toArray(size -> new Object[size][1]);
            CorrelationTableModel.setRowData(tableData);
            CorrelationRuleFileGui.createCorrelationRuleFileGui(ruleExtractorDataMap);
        }
    }

    private Map<CorrelationRule, List<ExtractorData>> processRules(List<CorrelationRule> ruleObjectList,
            Map<String, String> currentGuiHttpParameters) {
        Map<CorrelationRule, List<ExtractorData>> ruleExtractorDataMap = new HashMap<>();
        ruleObjectList.forEach(rule -> {
            if (rule.getType().equals(HTML_EXTRACTOR_TYPE)) {
                ruleExtractorDataMap.putAll(processCssExtractor(rule, currentGuiHttpParameters));
            } else if (rule.getType().equals(XPATH2_EXTRACTOR_TYPE)) {
                ruleExtractorDataMap.putAll(processXPath2Extractor(rule, currentGuiHttpParameters));
            } else if (rule.getType().equals(JSON_EXTRACTOR_TYPE)) {
                ruleExtractorDataMap.putAll(processJsonPathExtractor(rule, currentGuiHttpParameters));
            } else if (rule.getType().equals(REGEX_EXTRACTOR_TYPE) || rule.getType().equals(REGEX_HEADER_TYPE)) {
                ruleExtractorDataMap.putAll(processRegexExtractor(rule, currentGuiHttpParameters));
            } else if (rule.getType().equals(BOUNDARY_EXTRACTOR_TYPE)) {
                ruleExtractorDataMap.putAll(processBoundaryExtractor(rule, currentGuiHttpParameters));
            }
        });
        return ruleExtractorDataMap;
    }

    private Map<CorrelationRule, List<ExtractorData>> processCssExtractor(CorrelationRule rule,
            Map<String, String> currentGuiHttpParameters) {
        List<ExtractorData> extractorsDataList = new ArrayList<>();
        Extractor htmlExtractor = HtmlExtractor.getExtractorImpl("");
        for (Object sample : CorrelationRecorder.getBuffer()) {
            SampleResult sampleResult = (SampleResult) sample;
            List<String> result = new ArrayList<>();
            htmlExtractor.extract(rule.getExpr(), rule.getAttribute(), 1, sampleResult.getResponseDataAsString(),
                    result, 0, null);
            if (result.size() == 1 && StringUtils.isNotBlank(result.get(0))
                    && currentGuiHttpParameters.containsValue(result.get(0))) {
                Optional<String> optional = currentGuiHttpParameters.entrySet().stream()
                        .filter(entry -> entry.getValue().contains(result.get(0))).map(Map.Entry::getKey).findFirst();
                String correlationVariableName = "";
                if (optional.isPresent()) {
                    correlationVariableName = optional.get();
                    extractorsDataList.add(new HtmlExtractorData(correlationVariableName, rule.getExpr(),
                            rule.getAttribute(), ONE, TEXT_HTML, sampleResult.getSampleLabel()));
                }
            }
        }
        Map<CorrelationRule, List<ExtractorData>> map = new HashMap<>();
        map.put(rule, extractorsDataList);
        return map;
    }

    private Map<CorrelationRule, List<ExtractorData>> processXPath2Extractor(CorrelationRule rule,
            Map<String, String> currentGuiHttpParameters) {
        List<ExtractorData> extractorsDataList = new ArrayList<>();
        XPath2Extractor extractor = new XPath2Extractor();
        extractor.setFragment(false);
        for (Object sample : CorrelationRecorder.getBuffer()) {
            SampleResult sampleResult = (SampleResult) sample;
            List<String> matchStrings = new ArrayList<>();
            try {
                XPathUtil.putValuesForXPathInListUsingSaxon(sampleResult.getResponseDataAsString(), rule.getExpr(),
                        matchStrings, extractor.getFragment(), 1, "");
            } catch (SaxonApiException | FactoryConfigurationError e) {
                log.error(e.getMessage());
                continue;
            }
            if (matchStrings.size() == 1 && StringUtils.isNotBlank(matchStrings.get(0))
                    && currentGuiHttpParameters.containsValue(matchStrings.get(0))) {
                Optional<String> optional = currentGuiHttpParameters.entrySet().stream()
                        .filter(entry -> entry.getValue().contains(matchStrings.get(0))).map(Map.Entry::getKey)
                        .findFirst();
                String correlationVariableName = "";
                if (optional.isPresent()) {
                    correlationVariableName = optional.get();
                    extractorsDataList.add(new XPath2ExtractorData(correlationVariableName, rule.getExpr(), ONE,
                            TEXT_XML, sampleResult.getSampleLabel()));
                }
            }
        }
        Map<CorrelationRule, List<ExtractorData>> map = new HashMap<>();
        map.put(rule, extractorsDataList);
        return map;
    }

    private Map<CorrelationRule, List<ExtractorData>> processJsonPathExtractor(CorrelationRule rule,
            Map<String, String> currentGuiHttpParameters) {
        List<ExtractorData> extractorsDataList = new ArrayList<>();
        JSONManager jsonManager = new JSONManager();
        for (Object sample : CorrelationRecorder.getBuffer()) {
            SampleResult sampleResult = (SampleResult) sample;
            List<Object> matches;
            try {
                matches = jsonManager.extractWithJsonPath(sampleResult.getResponseDataAsString(), rule.getExpr());
            } catch (java.text.ParseException e) {
                log.error(e.getMessage());
                continue;
            }
            if (!matches.isEmpty() && StringUtils.isNotBlank(matches.get(0).toString())
                    && currentGuiHttpParameters.containsValue(matches.get(0).toString())) {
                Optional<String> optional = currentGuiHttpParameters.entrySet().stream()
                        .filter(entry -> entry.getValue().contains(matches.get(0).toString())).map(Map.Entry::getKey)
                        .findFirst();
                String correlationVariableName = "";
                if (optional.isPresent()) {
                    correlationVariableName = optional.get();
                    extractorsDataList.add(new JsonPathExtractorData(correlationVariableName, rule.getExpr(), ONE,
                            APPLICATION_JSON, sampleResult.getSampleLabel()));
                }
            }
        }
        Map<CorrelationRule, List<ExtractorData>> map = new HashMap<>();
        map.put(rule, extractorsDataList);
        return map;
    }

    private Map<CorrelationRule, List<ExtractorData>> processBoundaryExtractor(CorrelationRule rule,
            Map<String, String> currentGuiHttpParameters) {
        List<ExtractorData> extractorsDataList = new ArrayList<>();
        BoundaryExtractor extractor = new BoundaryExtractor();
        for (Object sample : CorrelationRecorder.getBuffer()) {
            SampleResult sampleResult = (SampleResult) sample;
            List<String> matches = extractor.extractAll(rule.getlBoundary(), rule.getrBoundary(),
                    sampleResult.getResponseDataAsString());
            if (!matches.isEmpty() && StringUtils.isNotBlank(matches.get(0)) && currentGuiHttpParameters.containsValue(matches.get(0))) {
                Optional<String> optional = currentGuiHttpParameters.entrySet().stream()
                        .filter(entry -> entry.getValue().contains(matches.get(0))).map(Map.Entry::getKey).findFirst();
                String correlationVariableName = "";
                if (optional.isPresent()) {
                    correlationVariableName = optional.get();
                    extractorsDataList.add(new BoundaryExtractorData(correlationVariableName, rule.getlBoundary(),
                            rule.getrBoundary(), ONE, sampleResult.getSampleLabel()));
                }
            }
        }
        Map<CorrelationRule, List<ExtractorData>> map = new HashMap<>();
        map.put(rule, extractorsDataList);
        return map;
    }

    private Map<CorrelationRule, List<ExtractorData>> processRegexExtractor(CorrelationRule rule,
            Map<String, String> currentGuiHttpParameters) {
        List<ExtractorData> extractorsDataList = new ArrayList<>();

        Perl5Matcher matcher = new Perl5Matcher();
        PatternCacheLRU pcLRU = new PatternCacheLRU();
        Pattern pattern;
        try {
            pattern = pcLRU.getPattern(rule.getExpr(), Perl5Compiler.READ_ONLY_MASK);
        } catch (MalformedCachePatternException e) {
            log.error(e.toString());
            return new HashMap<>();
        }
        Boolean isRegexHeader = rule.getType().equals("regex_header");
        for (Object sample : CorrelationRecorder.getBuffer()) {
            SampleResult sampleResult = (SampleResult) sample;
            String response = isRegexHeader ? sampleResult.getResponseHeaders()
                    : sampleResult.getResponseDataAsString();
            PatternMatcherInput input = new PatternMatcherInput(response);
            List<MatchResult> matches = new LinkedList<>();
            while (matcher.contains(input, pattern)) {
                matches.add(matcher.getMatch());
            }
            if (!matches.isEmpty() && StringUtils.isNotBlank(matches.get(0).group(1))
                    && currentGuiHttpParameters.containsValue(matches.get(0).group(1))) {
                Optional<String> optional = currentGuiHttpParameters.entrySet().stream()
                        .filter(entry -> entry.getValue().contains(matches.get(0).group(1))).map(Map.Entry::getKey)
                        .findFirst();
                String correlationVariableName = "";
                if (optional.isPresent()) {
                    correlationVariableName = optional.get();
                    extractorsDataList.add(new RegexExtractorData(rule.getExpr(), correlationVariableName,
                            sampleResult.getSampleLabel(), ONE, GROUP_NUMBER, isRegexHeader));
                }
            }
        }
        Map<CorrelationRule, List<ExtractorData>> map = new HashMap<>();
        map.put(rule, extractorsDataList);
        return map;
    }

    private Map<String, String> createCurrentGuiHttpParameters() {
        GuiPackage guiPackage = GuiPackage.getInstance();
        List<JMeterTreeNode> sampleList = guiPackage.getTreeModel().getNodesOfType(HTTPSamplerProxy.class);
        List<HTTPSamplerBase> currentGuiSampleRequestList = sampleList.stream()
                .map(node -> (HTTPSamplerBase) node.getTestElement()).collect(Collectors.toList());
        List<HeaderManager> currentGuiheaderList = guiPackage.getTreeModel().getNodesOfType(HeaderManager.class)
                .stream().map(node -> (HeaderManager) node.getTestElement()).collect(Collectors.toList());
        return Correlation.createJmxParameterMap(currentGuiSampleRequestList, currentGuiheaderList);
    }
}
