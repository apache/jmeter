/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implement ResultsRender for XPath tester
 */
public class RenderAsXPath2 implements ResultRenderer, ActionListener {

    private static final Logger log = LoggerFactory.getLogger(RenderAsXPath.class);

    private static final String XPATH_TESTER_COMMAND = "xpath_tester"; // $NON-NLS-1$
    
    private static final String XPATH_NAMESPACES_COMMAND = "xpath_namespaces"; // $NON-NLS-1$

    private JPanel xmlWithXPathPane;

    private JSyntaxTextArea xmlDataField;

    private JLabeledTextField xpathExpressionField;

    private JTextArea xpathResultField;

    private JTabbedPane rightSide;

    private SampleResult sampleResult = null;
    
    // Should we return fragment as text, rather than text of fragment?
    private final JCheckBox getFragment =
        new JCheckBox(JMeterUtils.getResString("xpath_tester_fragment"));//$NON-NLS-1$
    
    /** {@inheritDoc} */
    @Override
    public void clearData() {
        // N.B. don't set xpathExpressionField to empty to keep xpath
        this.xmlDataField.setText(""); // $NON-NLS-1$
        this.xpathResultField.setText(""); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        // Create the panels for the xpath tab
        xmlWithXPathPane = createXpathExtractorPanel();
    }

    /**
     * Display the response as text or as rendered HTML. Change the text on the
     * button appropriate to the current display.
     *
     * @param e the ActionEvent being processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ((sampleResult != null) && (XPATH_TESTER_COMMAND.equals(command))) {
            String response = xmlDataField.getText();
            XPath2Extractor extractor = new XPath2Extractor();
            extractor.setFragment(getFragment.isSelected());
            executeAndShowXPathTester(response, extractor);
        }
        else if ((sampleResult != null) && (XPATH_NAMESPACES_COMMAND.equals(command))) {
            String response = xmlDataField.getText();
            this.xpathResultField.setText(getDocumentNamespaces(response));
        }
    }

    /**
     * Launch xpath engine to parse a input text
     * @param textToParse
     */
    private void executeAndShowXPathTester(String textToParse, XPath2Extractor extractor) {
        if (textToParse != null && textToParse.length() > 0
                && this.xpathExpressionField.getText().length() > 0) {
            this.xpathResultField.setText(process(textToParse, extractor));
            this.xpathResultField.setCaretPosition(0); // go to first line
        }
    }

    private String process(String textToParse, XPath2Extractor extractor) {
        try {
            List<String> matchStrings = new ArrayList<>();
            XPathUtil.putValuesForXPathInListUsingSaxon(textToParse, xpathExpressionField.getText(),
                    matchStrings, extractor.getFragment(), -1, getDocumentNamespaces(textToParse));
            StringBuilder builder = new StringBuilder();
            int nbFound = matchStrings.size();
            builder.append("Match count: ").append(nbFound).append("\n");
            for (int i = 0; i < nbFound; i++) {
                builder.append("Match[").append(i+1).append("]=").append(matchStrings.get(i)).append("\n");
            }
            return builder.toString();
        } catch (Exception e) {
            return "Exception:"+ ExceptionUtils.getStackTrace(e);
        }
    }
    
    
    private String getDocumentNamespaces(String textToParse) {
        StringBuilder result = new StringBuilder();
        try {
            List<String[]> namespaces = XPathUtil.getNamespaces(textToParse);
            for (int i = 0;i<namespaces.size();i++) {
                result.append(namespaces.get(i)[0])
                .append('=') // $NON-NLS-1$
                .append(namespaces.get(i)[1])
                .append('\n'); // $NON-NLS-1$
            }
            return result.toString();
        } catch (Exception e) {
            return "Exception:"+ ExceptionUtils.getStackTrace(e);
        }
    }
    
    /*================= internal business =================*/
    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        try {
            xmlDataField.setText(response == null ? "" : response);
            xmlDataField.setCaretPosition(0);
        } catch (Exception e) {
            log.error("Exception converting to XML: {}, message: {}", response, e.getMessage(), e);
            xmlDataField.setText("Exception converting to XML:"+response+ ", message:"+e.getMessage());
            xmlDataField.setCaretPosition(0);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("xpath2_tester"); // $NON-NLS-1$
    }


    /** {@inheritDoc} */
    @Override
    public void setupTabPane() {
         // Add xpath tester pane
        if (rightSide.indexOfTab(JMeterUtils.getResString("xpath_tester_title")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("xpath_tester_title"), xmlWithXPathPane); // $NON-NLS-1$
        }
        clearData();
    }

    /**
     * @return XPath Tester panel
     */
    private JPanel createXpathExtractorPanel() {
        xmlDataField = JSyntaxTextArea.getInstance(50, 80, true);
        xmlDataField.setCodeFoldingEnabled(true);
        xmlDataField.setEditable(false);
        xmlDataField.setBracketMatchingEnabled(false);
        xmlDataField.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        xmlDataField.setLanguage(SyntaxConstants.SYNTAX_STYLE_XML);
        xmlDataField.setLineWrap(true);
        xmlDataField.setWrapStyleWord(true);

        JScrollPane xmlDataPane = JTextScrollPane.getInstance(xmlDataField, true);
        xmlDataPane.setPreferredSize(new Dimension(0, 200));

        JPanel pane = new JPanel(new BorderLayout(0, 5));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                xmlDataPane, createXpathExtractorTasksPanel());
        mainSplit.setDividerLocation(0.6d);
        mainSplit.setOneTouchExpandable(true);
        pane.add(mainSplit, BorderLayout.CENTER);
        return pane;
    }

    /**
     * Create the XPath task pane
     *
     * @return XPath task pane
     */
    private JPanel createXpathExtractorTasksPanel() {
        Box xpathActionPanel = Box.createVerticalBox();
        
        Box selectorAndButton = Box.createHorizontalBox();

        Border margin = new EmptyBorder(5, 5, 0, 5);
        xpathActionPanel.setBorder(margin);
        xpathExpressionField = new JLabeledTextField(JMeterUtils.getResString("xpath_tester_field")); // $NON-NLS-1$
        
        JButton xpathTester = new JButton(JMeterUtils.getResString("xpath_tester_button_test")); // $NON-NLS-1$
        xpathTester.setActionCommand(XPATH_TESTER_COMMAND);
        xpathTester.addActionListener(this);
        
        JButton xpathTesterNamespaces = new JButton(JMeterUtils.getResString("xpath_namespaces")); // $NON-NLS-1$
        xpathTesterNamespaces.setActionCommand(XPATH_NAMESPACES_COMMAND);
        xpathTesterNamespaces.addActionListener(this);
        
        selectorAndButton.add(xpathExpressionField);
        selectorAndButton.add(xpathTester);
        selectorAndButton.add(xpathTesterNamespaces);
        
        xpathActionPanel.add(selectorAndButton);
        xpathActionPanel.add(getFragment);
        
        xpathResultField = new JTextArea();
        xpathResultField.setEditable(false);
        xpathResultField.setLineWrap(true);
        xpathResultField.setWrapStyleWord(true);

        JPanel xpathTasksPanel = new JPanel(new BorderLayout(0, 5));
        xpathTasksPanel.add(xpathActionPanel, BorderLayout.NORTH);
        xpathTasksPanel.add(GuiUtils.makeScrollPane(xpathResultField), BorderLayout.CENTER);

        return xpathTasksPanel;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setRightSide(JTabbedPane side) {
        rightSide = side;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setSamplerResult(Object userObject) {
        if (userObject instanceof SampleResult) {
            sampleResult = (SampleResult) userObject;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setLastSelectedTab(int index) {
        // nothing to do
    }

    /** {@inheritDoc} */
    @Override
    public void renderImage(SampleResult sampleResult) {
        clearData();
        xmlDataField.setText(JMeterUtils.getResString("xpath_tester_no_text")); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void setBackgroundColor(Color backGround) {
        // NOOP
    }
}
