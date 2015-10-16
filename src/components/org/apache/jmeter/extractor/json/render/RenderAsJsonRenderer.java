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
import java.util.ArrayList;
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
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.RenderAsJSON;
import org.apache.jmeter.visualizers.ResultRenderer;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


/**
 * Implement ResultsRender for JSON Path tester
 * @since 2.14
 */
public class RenderAsJsonRenderer implements ResultRenderer, ActionListener {

    private static final Logger LOGGER = LoggingManager.getLoggerForClass();

    private static final String JSONPATH_TESTER_COMMAND = "jsonpath_tester"; // $NON-NLS-1$

    private JPanel jsonWithJSonPathPane;

    private JTextArea jsonDataField;

    private JLabeledTextField jsonPathExpressionField;

    private JTextArea jsonPathResultField;

    private JTabbedPane rightSide;

    private SampleResult sampleResult = null;

    private JScrollPane jsonDataPane;


    /** {@inheritDoc} */
    public void clearData() {
        this.jsonDataField.setText(""); // $NON-NLS-1$
        // don't set empty to keep json path
        this.jsonPathResultField.setText(""); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    public void init() {
        // Create the panels for the json tab
        jsonWithJSonPathPane = createJSonPathExtractorPanel();
    }

    /**
     * Display the response as text or as rendered HTML. Change the text on the
     * button appropriate to the current display.
     *
     * @param e the ActionEvent being processed
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ((sampleResult != null) && (JSONPATH_TESTER_COMMAND.equals(command))) {
            String response = jsonDataField.getText();
            executeAndShowXPathTester(response);
        }
    }

    /**
     * Launch json path engine to parse a input text
     * @param textToParse
     */
    private void executeAndShowXPathTester(String textToParse) {
        if (textToParse != null && textToParse.length() > 0
                && this.jsonPathExpressionField.getText().length() > 0) {
            this.jsonPathResultField.setText(process(textToParse));
            this.jsonPathResultField.setCaretPosition(0); // go to first line
        }
    }

    private String process(String textToParse) {
        try {
            List<String> matchStrings = new ArrayList<String>();
            extractWithJSonPath(textToParse, jsonPathExpressionField.getText(), matchStrings);
            if(matchStrings.size()==0) {
                return "NO MATCH";
            }
            else {
                StringBuilder builder = new StringBuilder();
                int i=0;
                for (String text : matchStrings) {
                    builder.append("Result[").append(i++).append("]=").append(text).append("\n");
                }
                
                return builder.toString();
            }
        } catch (Exception e) {
            return "Exception:"+ e.getMessage();
        }
    }
    
    private void extractWithJSonPath(String textToParse, String expression,
            List<String> matchStrings) throws ParseException {
        JSONManager jsonManager = new JSONManager();
        List<String> list = jsonManager.extractWithJsonPath(textToParse, expression);
        matchStrings.addAll(list);
    }

    /*================= internal business =================*/



    /** {@inheritDoc} */
    public void renderResult(SampleResult sampleResult) {
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        try {
            jsonDataField.setText(response == null ? "" : RenderAsJSON.prettyJSON(response));
            jsonDataField.setCaretPosition(0);
        } catch (Exception e) {
            LOGGER.error("Exception converting to XML:"+response+ ", message:"+e.getMessage(),e);
            jsonDataField.setText("Exception converting to XML:"+response+ ", message:"+e.getMessage());
            jsonDataField.setCaretPosition(0);
        }
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("jsonpath_renderer"); // $NON-NLS-1$
    }


    /** {@inheritDoc} */
    public void setupTabPane() {
         // Add json-path tester pane
        if (rightSide.indexOfTab(JMeterUtils.getResString("jsonpath_tester_title")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("jsonpath_tester_title"), jsonWithJSonPathPane); // $NON-NLS-1$
        }
        clearData();
    }

    /**
     * @return RegExp Tester panel
     */
    private JPanel createJSonPathExtractorPanel() {
        
        jsonDataField = new JTextArea();
        jsonDataField.setEditable(false);
        jsonDataField.setLineWrap(true);
        jsonDataField.setWrapStyleWord(true);

        this.jsonDataPane = GuiUtils.makeScrollPane(jsonDataField);
        jsonDataPane.setMinimumSize(new Dimension(0, 400));

        JPanel pane = new JPanel(new BorderLayout(0, 5));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                jsonDataPane, createJSonPathExtractorTasksPanel());
        mainSplit.setDividerLocation(400);
        pane.add(mainSplit, BorderLayout.CENTER);
        return pane;
    }

    /**
     * Create the Regexp task pane
     *
     * @return Regexp task pane
     */
    private JPanel createJSonPathExtractorTasksPanel() {
        JPanel xpathActionPanel = new JPanel();
        xpathActionPanel.setLayout(new BoxLayout(xpathActionPanel, BoxLayout.X_AXIS));
        Border margin = new EmptyBorder(5, 5, 0, 5);
        xpathActionPanel.setBorder(margin);
        jsonPathExpressionField = new JLabeledTextField(JMeterUtils.getResString("jsonpath_tester_field")); // $NON-NLS-1$
        xpathActionPanel.add(jsonPathExpressionField, BorderLayout.WEST);

        JButton xpathTester = new JButton(JMeterUtils.getResString("jsonpath_tester_button_test")); // $NON-NLS-1$
        xpathTester.setActionCommand(JSONPATH_TESTER_COMMAND);
        xpathTester.addActionListener(this);
        xpathActionPanel.add(xpathTester, BorderLayout.EAST);

        jsonPathResultField = new JTextArea();
        jsonPathResultField.setEditable(false);
        jsonPathResultField.setLineWrap(true);
        jsonPathResultField.setWrapStyleWord(true);

        JPanel xpathTasksPanel = new JPanel(new BorderLayout(0, 5));
        xpathTasksPanel.add(xpathActionPanel, BorderLayout.NORTH);
        xpathTasksPanel.add(GuiUtils.makeScrollPane(jsonPathResultField), BorderLayout.CENTER);

        return xpathTasksPanel;
    }

    /** {@inheritDoc} */
    public synchronized void setRightSide(JTabbedPane side) {
        rightSide = side;
    }

    /** {@inheritDoc} */
    public synchronized void setSamplerResult(Object userObject) {
        if (userObject instanceof SampleResult) {
            sampleResult = (SampleResult) userObject;
        }
    }

    /** {@inheritDoc} */
    public void setLastSelectedTab(int index) {
        // nothing to do
    }

    /** {@inheritDoc} */
    public void renderImage(SampleResult sampleResult) {
        clearData();
        jsonDataField.setText(JMeterUtils.getResString("jsonpath_render_no_text")); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    public void setBackgroundColor(Color backGround) {
    }

}
