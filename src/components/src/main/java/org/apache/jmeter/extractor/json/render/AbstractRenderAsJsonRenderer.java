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

package org.apache.jmeter.extractor.json.render;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

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


/**
 * Abstract base class for implementation of a ResultsRenderer for a JSON tester
 * @since 5.2
 */
abstract class AbstractRenderAsJsonRenderer implements ResultRenderer, ActionListener {

    protected static final String NO_MATCH = "NO MATCH"; //$NON-NLS-1$
    private static final String TAB_SEPARATOR = "    "; //$NON-NLS-1$

    private static final String TESTER_COMMAND = "TESTER_COMMAND"; // $NON-NLS-1$

    private JPanel jsonWithExtractorPanel;

    private JSyntaxTextArea jsonDataField;

    private JLabeledTextField expressionField;

    private JTextArea resultField;

    private JTabbedPane rightSide;

    private SampleResult sampleResult;

    /** {@inheritDoc} */
    @Override
    public void clearData() {
        this.jsonDataField.setText(""); // $NON-NLS-1$
        // don't set empty to keep json path
        this.resultField.setText(""); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        // Create the panels for the json tab
        jsonWithExtractorPanel = createExtractorPanel();
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
        if ((sampleResult != null) && TESTER_COMMAND.equals(command)) {
            String response = jsonDataField.getText();
            executeTester(response);
        }
    }

    /**
     * Launch JSON path engine to parse a input text
     * @param textToParse the text that will be parsed
     */
    protected void executeTester(String textToParse) {
        if (textToParse != null && textToParse.length() > 0
                && this.expressionField.getText().length() > 0) {
            this.resultField.setText(process(textToParse));
            this.resultField.setCaretPosition(0); // go to first line
        }
    }

    protected String getExpression() {
        return expressionField.getText();
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
    public void setupTabPane() {
         // Add json-path tester pane
        if (rightSide.indexOfTab(getTabLabel()) < 0) { // $NON-NLS-1$
            rightSide.addTab(getTabLabel(), jsonWithExtractorPanel); // $NON-NLS-1$
        }
        clearData();
    }

    /**
     * @return Extractor panel
     */
    private JPanel createExtractorPanel() {
        jsonDataField = JSyntaxTextArea.getInstance(50, 80, true);
        jsonDataField.setCodeFoldingEnabled(true);
        jsonDataField.setEditable(true);
        jsonDataField.setBracketMatchingEnabled(false);
        jsonDataField.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        jsonDataField.setLanguage(SyntaxConstants.SYNTAX_STYLE_JSON);
        jsonDataField.setLineWrap(true);
        jsonDataField.setWrapStyleWord(true);


        JScrollPane jsonDataPane = JTextScrollPane.getInstance(jsonDataField, true);
        jsonDataPane.setPreferredSize(new Dimension(100, 200));

        JPanel panel = new JPanel(new BorderLayout(0, 5));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                jsonDataPane, createTechnologyExtractorTasksPanel());
        mainSplit.setDividerLocation(0.6d);
        mainSplit.setOneTouchExpandable(true);
        panel.add(mainSplit, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create the extractor task pane
     *
     * @return extractor task pane
     */
    private JPanel createTechnologyExtractorTasksPanel() {
        JPanel jsonPathActionPanel = new JPanel();
        jsonPathActionPanel.setLayout(new BoxLayout(jsonPathActionPanel, BoxLayout.X_AXIS));
        Border margin = new EmptyBorder(5, 5, 0, 5);
        jsonPathActionPanel.setBorder(margin);
        expressionField = new JLabeledTextField(getExpressionLabel()); // $NON-NLS-1$
        jsonPathActionPanel.add(expressionField, BorderLayout.WEST);

        JButton testerButton = new JButton(getTestButtonLabel()); // $NON-NLS-1$
        testerButton.setActionCommand(TESTER_COMMAND);
        testerButton.addActionListener(this);
        jsonPathActionPanel.add(testerButton, BorderLayout.EAST);

        resultField = new JTextArea();
        resultField.setEditable(false);
        resultField.setLineWrap(true);
        resultField.setWrapStyleWord(true);
        resultField.setMinimumSize(new Dimension(100, 150));

        JPanel jsonPathTasksPanel = new JPanel(new BorderLayout(0, 5));
        jsonPathTasksPanel.add(jsonPathActionPanel, BorderLayout.NORTH);
        jsonPathTasksPanel.add(GuiUtils.makeScrollPane(resultField), BorderLayout.CENTER);

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
        jsonDataField.setText(JMeterUtils.getResString("render_no_text")); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void setBackgroundColor(Color backGround) {
        // NOOP
    }

    /**
     * @return Tab label
     */
    protected abstract String getTabLabel();

    /**
     * @return Test button label
     */
    protected abstract String getTestButtonLabel();

    /**
     * @return The label for the technology expression
     */
    protected abstract String getExpressionLabel();

    /**
     * @param textToParse String of the response to process
     * @return the extracted values using the technology
     */
    protected abstract String process(String textToParse);

    /**
     * @return the rightSide
     */
    protected synchronized JTabbedPane getRightSide() {
        return rightSide;
    }

    /**
     * @return the jsonWithExtractorPanel
     */
    protected JPanel getJsonWithExtractorPanel() {
        return jsonWithExtractorPanel;
    }

    /**
     * @return the jsonDataField
     */
    protected JSyntaxTextArea getJsonDataField() {
        return jsonDataField;
    }

    /**
     * @return the expressionField
     */
    protected JLabeledTextField getExpressionField() {
        return expressionField;
    }

    /**
     * @return the resultField
     */
    protected JTextArea getResultField() {
        return resultField;
    }
}
