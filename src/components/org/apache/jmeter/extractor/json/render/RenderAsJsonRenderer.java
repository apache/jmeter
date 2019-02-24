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

package org.apache.jmeter.extractor.json.render;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.extractor.json.jsonpath.JSONManager;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.RenderAsJSON;
import org.apache.jmeter.visualizers.ResultRenderer;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implement ResultsRender for JSON Path tester
 * @since 3.0
 */
public class RenderAsJsonRenderer implements ResultRenderer, ActionListener {

    private static final Logger log = LoggerFactory.getLogger(RenderAsJsonRenderer.class);
    private static final String NO_MATCH = "NO MATCH"; //$NON-NLS-1$
    private static final String TAB_SEPARATOR = "    "; //$NON-NLS-1$
    
    private static final String JSONPATH_TESTER_COMMAND = "jsonpath_tester"; // $NON-NLS-1$

    private JPanel jsonWithJSonPathPanel;

    private JSyntaxTextArea jsonDataField;

    private JLabeledTextField jsonPathExpressionField;

    private JTextArea jsonPathResultField;

    private JTabbedPane rightSide;

    private SampleResult sampleResult;

    /** {@inheritDoc} */
    @Override
    public void clearData() {
        this.jsonDataField.setText(""); // $NON-NLS-1$
        // don't set empty to keep json path
        this.jsonPathResultField.setText(""); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        // Create the panels for the json tab
        jsonWithJSonPathPanel = createJSonPathExtractorPanel();
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
        if ((sampleResult != null) && (JSONPATH_TESTER_COMMAND.equals(command))) {
            String response = jsonDataField.getText();
            executeAndJSonPathTester(response);
        }
    }

    /**
     * Launch json path engine to parse a input text
     * @param textToParse
     */
    private void executeAndJSonPathTester(String textToParse) {
        if (textToParse != null && textToParse.length() > 0
                && this.jsonPathExpressionField.getText().length() > 0) {
            this.jsonPathResultField.setText(process(textToParse));
            this.jsonPathResultField.setCaretPosition(0); // go to first line
        }
    }

    private String process(String textToParse) {
        String jsonPathExpression = jsonPathExpressionField.getText();
        try {
            List<Object> matchStrings = extractWithJSonPath(textToParse, jsonPathExpression);
            if (matchStrings.isEmpty()) {
                return NO_MATCH; //$NON-NLS-1$
            } else {
                StringBuilder builder = new StringBuilder();
                int i = 0;
                for (Object obj : matchStrings) {
                    String objAsString =
                            obj != null ? obj.toString() : ""; //$NON-NLS-1$
                    builder.append("Result[").append(i++).append("]=").append(objAsString).append("\n"); //$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
                }

                return builder.toString();
            }
        } catch (Exception e) { // NOSONAR We handle it through return message
            log.debug("Exception extracting from '{}' with JSON Path expression '{}'", textToParse, jsonPathExpression);
            return "Exception: " + e.getMessage(); //$NON-NLS-1$
        }
    }
    
    private List<Object> extractWithJSonPath(String textToParse, String expression) throws ParseException {
        JSONManager jsonManager = new JSONManager();
        return jsonManager.extractWithJsonPath(textToParse, expression);
    }

    /*================= internal business =================*/

    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        jsonDataField.setText(response == null ? "" : RenderAsJSON.prettyJSON(response, TAB_SEPARATOR));  //$NON-NLS-1$
        jsonDataField.setCaretPosition(0);
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("jsonpath_renderer"); // $NON-NLS-1$
    }


    /** {@inheritDoc} */
    @Override
    public void setupTabPane() {
         // Add json-path tester pane
        if (rightSide.indexOfTab(JMeterUtils.getResString("jsonpath_tester_title")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("jsonpath_tester_title"), jsonWithJSonPathPanel); // $NON-NLS-1$
        }
        clearData();
    }

    /**
     * @return JSON PATH Tester panel
     */
    private JPanel createJSonPathExtractorPanel() {
        jsonDataField = JSyntaxTextArea.getInstance(50, 80, true);
        jsonDataField.setCodeFoldingEnabled(true);
        jsonDataField.setEditable(false);
        jsonDataField.setBracketMatchingEnabled(false);
        jsonDataField.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        jsonDataField.setLanguage(SyntaxConstants.SYNTAX_STYLE_JSON);
        jsonDataField.setLineWrap(true);
        jsonDataField.setWrapStyleWord(true);
        

        JScrollPane jsonDataPane = JTextScrollPane.getInstance(jsonDataField, true);
        jsonDataPane.setPreferredSize(new Dimension(100, 200));

        JPanel panel = new JPanel(new BorderLayout(0, 5));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                jsonDataPane, createJSonPathExtractorTasksPanel());
        mainSplit.setDividerLocation(0.6d);
        mainSplit.setOneTouchExpandable(true);
        panel.add(mainSplit, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create the JSON PATH task pane
     *
     * @return JSON PATH task pane
     */
    private JPanel createJSonPathExtractorTasksPanel() {
        JPanel jsonPathActionPanel = new JPanel();
        jsonPathActionPanel.setLayout(new BoxLayout(jsonPathActionPanel, BoxLayout.X_AXIS));
        Border margin = new EmptyBorder(5, 5, 0, 5);
        jsonPathActionPanel.setBorder(margin);
        jsonPathExpressionField = new JLabeledTextField(JMeterUtils.getResString("jsonpath_tester_field")); // $NON-NLS-1$
        jsonPathActionPanel.add(jsonPathExpressionField, BorderLayout.WEST);

        JButton jsonPathTester = new JButton(JMeterUtils.getResString("jsonpath_tester_button_test")); // $NON-NLS-1$
        jsonPathTester.setActionCommand(JSONPATH_TESTER_COMMAND);
        jsonPathTester.addActionListener(this);
        jsonPathActionPanel.add(jsonPathTester, BorderLayout.EAST);

        jsonPathResultField = new JTextArea();
        jsonPathResultField.setEditable(false);
        jsonPathResultField.setLineWrap(true);
        jsonPathResultField.setWrapStyleWord(true);
        jsonPathResultField.setMinimumSize(new Dimension(100, 150));

        JPanel jsonPathTasksPanel = new JPanel(new BorderLayout(0, 5));
        jsonPathTasksPanel.add(jsonPathActionPanel, BorderLayout.NORTH);
        jsonPathTasksPanel.add(GuiUtils.makeScrollPane(jsonPathResultField), BorderLayout.CENTER);

        return jsonPathTasksPanel;
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
        jsonDataField.setText(JMeterUtils.getResString("jsonpath_render_no_text")); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void setBackgroundColor(Color backGround) {
        // NOOP
    }

}
