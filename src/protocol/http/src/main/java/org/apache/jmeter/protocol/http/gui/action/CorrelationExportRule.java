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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.gui.CorrelationTableModel;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.AbstractActionWithNoRunningTest;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.correlation.rule.BoundryExtractorCorrelationRule;
import org.apache.jmeter.protocol.http.correlation.rule.CorrelationRule;
import org.apache.jmeter.protocol.http.correlation.rule.HtmlExtractorCorrelationRule;
import org.apache.jmeter.protocol.http.correlation.rule.JsonExtractorCorrelationRule;
import org.apache.jmeter.protocol.http.correlation.rule.RegexExtractorCorrelationRule;
import org.apache.jmeter.protocol.http.correlation.rule.XPath2ExtractorCorrelationRule;
import org.apache.jmeter.protocol.http.gui.CorrelationExportRuleGui;
import org.apache.jmeter.util.CorrelationFunction;

public class CorrelationExportRule extends AbstractActionWithNoRunningTest {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.CORRELATION_EXPORT_RULE);
    }

    public CorrelationExportRule() {
        super();
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    protected void doActionAfterCheck(ActionEvent e) throws IllegalUserActionException {
        GuiPackage guiPackage = GuiPackage.getInstance();
        // get all the extractors from the gui (duplicates removed) and
        // prepare CorrelationRule objects for them
        Set<CorrelationRule> extractorRuleSet = new HashSet<>();
        extractorRuleSet.addAll(prepareHtmlExtractorRuleSet(guiPackage));
        extractorRuleSet.addAll(prepareXPath2ExtractorRuleSet(guiPackage));
        extractorRuleSet.addAll(prepareJsonPathExtractorRuleSet(guiPackage));
        extractorRuleSet.addAll(prepareRegexExtractorRuleSet(guiPackage));
        extractorRuleSet.addAll(prepareBoundaryExtractorRuleSet(guiPackage));
        if (extractorRuleSet.isEmpty()) {
            throw new IllegalUserActionException(
                    "No extractors found to export. Please check if the script is correlated and try again.");
        }
        // prepare table data for extractors to export and show on GUI
        Object[][] tableData = extractorRuleSet.stream()
                .map(rule -> new Object[] { Boolean.FALSE, rule.getName(), rule.getType() })
                .toArray(size -> new Object[size][1]);
        CorrelationTableModel.setRowData(tableData);
        CorrelationExportRuleGui.createCorrelationRuleFileGui(extractorRuleSet);
    }

    private Set<BoundryExtractorCorrelationRule> prepareBoundaryExtractorRuleSet(GuiPackage guiPackage) {
        // extract Boundary Extractor JMeterTreeNode
        List<JMeterTreeNode> boundaryExtractorNodes = guiPackage.getTreeModel().getNodesOfType(BoundaryExtractor.class);
        // Extract testElement from nodes and cast them to BoundaryExtractor
        List<BoundaryExtractor> boundaryExtractors = boundaryExtractorNodes.stream()
                .map(node -> (BoundaryExtractor) node.getTestElement()).collect(Collectors.toList());
        // if the correlated parameter name contains (1) or (2) with it then it is a repeated
        // parameter. Hence, set the refName to the actual parameter name.
        // e.g., case for two extractors: 1. refName: token; 2. refName: token(1)
        // In this case, it will create a duplicate entry in rule json
        // hence, convert the name to token from token(1) and push it into the set
        // Prepare the set of Correlation Rule object
        return boundaryExtractors.stream().map(boundaryExtractor -> {
            boundaryExtractor.setRefName(CorrelationFunction.extractVariable(boundaryExtractor.getRefName()));
            return new BoundryExtractorCorrelationRule(boundaryExtractor);
        }).collect(Collectors.toSet());
    }

    private Set<RegexExtractorCorrelationRule> prepareRegexExtractorRuleSet(GuiPackage guiPackage) {
        // see prepareBoundaryExtractorRuleSet for description
        return guiPackage.getTreeModel().getNodesOfType(RegexExtractor.class).stream()
                .map(node -> (RegexExtractor) node.getTestElement()).collect(Collectors.toList()).stream()
                .map(regexExtractor -> {
                    regexExtractor.setRefName(CorrelationFunction.extractVariable(regexExtractor.getRefName()));
                    return new RegexExtractorCorrelationRule(regexExtractor);
                }).collect(Collectors.toSet());
    }

    private Set<JsonExtractorCorrelationRule> prepareJsonPathExtractorRuleSet(GuiPackage guiPackage) {
        // see prepareBoundaryExtractorRuleSet for description
        return guiPackage.getTreeModel().getNodesOfType(JSONPostProcessor.class).stream()
                .map(node -> (JSONPostProcessor) node.getTestElement()).collect(Collectors.toList()).stream()
                .map(jsonPathExtractor -> {
                    jsonPathExtractor.setRefNames(CorrelationFunction.extractVariable(jsonPathExtractor.getRefNames()));
                    return new JsonExtractorCorrelationRule(jsonPathExtractor);
                }).collect(Collectors.toSet());
    }

    private Set<XPath2ExtractorCorrelationRule> prepareXPath2ExtractorRuleSet(GuiPackage guiPackage) {
        // see prepareBoundaryExtractorRuleSet for description
        List<JMeterTreeNode> xpathExtractorNodes = guiPackage.getTreeModel().getNodesOfType(XPath2Extractor.class);
        List<XPath2Extractor> xpathExtractors = xpathExtractorNodes.stream()
                .map(node -> (XPath2Extractor) node.getTestElement()).collect(Collectors.toList());
        return xpathExtractors.stream().map(xpathExtractor -> {
            xpathExtractor.setRefName(CorrelationFunction.extractVariable(xpathExtractor.getRefName()));
            return new XPath2ExtractorCorrelationRule(xpathExtractor);
        }).collect(Collectors.toSet());
    }

    private Set<HtmlExtractorCorrelationRule> prepareHtmlExtractorRuleSet(GuiPackage guiPackage) {
        // see prepareBoundaryExtractorRuleSet for description
        List<JMeterTreeNode> htmlExtractorNodes = guiPackage.getTreeModel().getNodesOfType(HtmlExtractor.class);
        List<HtmlExtractor> htmlExtractors = htmlExtractorNodes.stream()
                .map(node -> (HtmlExtractor) node.getTestElement()).collect(Collectors.toList());
        return htmlExtractors.stream().map(htmlExtractor -> {
            htmlExtractor.setRefName(CorrelationFunction.extractVariable(htmlExtractor.getRefName()));
            return new HtmlExtractorCorrelationRule(htmlExtractor);
        }).collect(Collectors.toSet());
     }
}
