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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
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

import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Implement ResultsRender for Boundary Extractor tester
 */
public class RenderAsBoundaryExtractor implements ResultRenderer, ActionListener {

    private static final String BOUNDARY_EXTRACTOR_TESTER_COMMAND = "boundary_extractor_tester"; // $NON-NLS-1$

    private JPanel boundaryExtractorPane;

    private JTextArea boundaryExtractorDataField;
    
    private JLabeledTextField boundaryExtractorFieldLeft;

    private JLabeledTextField boundaryExtractorFieldRight;

    private JTextArea boundaryExtractorResultField;

    private JTabbedPane rightSide;

    private SampleResult sampleResult = null;
    
    /**
     * Display the response as text or as rendered HTML. Change the text on the
     * button appropriate to the current display.
     *
     * @param e the ActionEvent being processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ((sampleResult != null) && (BOUNDARY_EXTRACTOR_TESTER_COMMAND.equals(command))) {
            String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
            executeAndShowBoundaryExtractorTester(response);
        }
    }
    
    /**
     * Launch boundaryExtractor engine to parse a input text
     * @param textToParse
     */
    private void executeAndShowBoundaryExtractorTester(String textToParse) {
        if (textToParse != null && textToParse.length() > 0
                && this.boundaryExtractorFieldLeft.getText().length() > 0
                && this.boundaryExtractorFieldRight.getText().length() > 0) {
            this.boundaryExtractorResultField.setText(process(textToParse));
            this.boundaryExtractorResultField.setCaretPosition(0); // go to first line
        }
    }
    
    
    private String process(String textToParse) {

        BoundaryExtractor extractor = new BoundaryExtractor();

        List<String> matches = extractor.extractAll(
                boundaryExtractorFieldLeft.getText(),
                boundaryExtractorFieldRight.getText(),
                textToParse);

        int nbFound = matches.size();
        // Construct a multi-line string with all matches
        StringBuilder sb = new StringBuilder();
        sb.append("Match count: ").append(nbFound).append("\n");
        for (int j = 0; j < nbFound; j++) {
            String match = matches.get(j);
            sb.append("Match[").append(j+1).append("]=").append(match).append("\n");
        }
        return sb.toString();

    }

    /** {@inheritDoc} */
    @Override
    public void clearData() {
        this.boundaryExtractorDataField.setText(""); // $NON-NLS-1$
        this.boundaryExtractorFieldLeft.setText(""); // $NON-NLS-1$
        this.boundaryExtractorFieldRight.setText(""); // $NON-NLS-1$
        this.boundaryExtractorResultField.setText(""); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        // Create the panels for the boundaryExtractor tab
        boundaryExtractorPane = createBoundaryExtractorPanel();
    }

    /**
     * @return boundaryExtractor Tester panel
     */
    private JPanel createBoundaryExtractorPanel() {
        boundaryExtractorDataField = new JTextArea();
        boundaryExtractorDataField.setEditable(false);
        boundaryExtractorDataField.setLineWrap(true);
        boundaryExtractorDataField.setWrapStyleWord(true);

        JScrollPane boundaryExtractorDataPane = GuiUtils.makeScrollPane(boundaryExtractorDataField);
        boundaryExtractorDataPane.setPreferredSize(new Dimension(0, 200));

        JPanel pane = new JPanel(new BorderLayout(0, 5));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                boundaryExtractorDataPane, createBoundaryExtractorTasksPanel());
        mainSplit.setDividerLocation(0.6d);
        mainSplit.setOneTouchExpandable(true);
        pane.add(mainSplit, BorderLayout.CENTER);
        return pane;
    }
    
    /**
     * Create the boundaryExtractor task pane
     *
     * @return boundaryExtractor task pane
     */
    private JPanel createBoundaryExtractorTasksPanel() {
        JPanel boundaryExtractorActionPanel = new JPanel();
        boundaryExtractorActionPanel.setLayout(new BoxLayout(boundaryExtractorActionPanel, BoxLayout.X_AXIS));
        Border margin = new EmptyBorder(5, 5, 0, 5);
        boundaryExtractorActionPanel.setBorder(margin);
        boundaryExtractorFieldLeft = new JLabeledTextField(JMeterUtils.getResString("boundaryextractor_leftboundary_field")); // $NON-NLS-1$
        boundaryExtractorActionPanel.add(boundaryExtractorFieldLeft, BorderLayout.WEST);
        boundaryExtractorFieldRight = new JLabeledTextField(JMeterUtils.getResString("boundaryextractor_rightboundary_field")); // $NON-NLS-1$
        boundaryExtractorActionPanel.add(boundaryExtractorFieldRight, BorderLayout.WEST);
        
        JButton boundaryExtractorTester = new JButton(JMeterUtils.getResString("boundaryextractor_tester_button_test")); // $NON-NLS-1$
        boundaryExtractorTester.setActionCommand(BOUNDARY_EXTRACTOR_TESTER_COMMAND);
        boundaryExtractorTester.addActionListener(this);
        boundaryExtractorActionPanel.add(boundaryExtractorTester, BorderLayout.EAST);

        boundaryExtractorResultField = new JTextArea();
        boundaryExtractorResultField.setEditable(false);
        boundaryExtractorResultField.setLineWrap(true);
        boundaryExtractorResultField.setWrapStyleWord(true);

        JPanel boundaryExtractorTasksPanel = new JPanel(new BorderLayout(0, 5));
        boundaryExtractorTasksPanel.add(boundaryExtractorActionPanel, BorderLayout.NORTH);
        boundaryExtractorTasksPanel.add(GuiUtils.makeScrollPane(boundaryExtractorResultField), BorderLayout.CENTER);

        return boundaryExtractorTasksPanel;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setupTabPane() {
         // Add boundaryExtractor tester pane
        if (rightSide.indexOfTab(JMeterUtils.getResString("boundaryextractor_tester_title")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("boundaryextractor_tester_title"), boundaryExtractorPane); // $NON-NLS-1$
        }
        clearData();
    }

    @Override
    public void setLastSelectedTab(int index) {
        // nothing to do    
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
    public void renderResult(SampleResult sampleResult) {
        clearData();
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        boundaryExtractorDataField.setText(response);
        boundaryExtractorDataField.setCaretPosition(0);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("boundaryextractor_tester_title"); // $NON-NLS-1$
    }
    
    /** {@inheritDoc} */
    @Override
    public void renderImage(SampleResult sampleResult) {
        clearData();
        boundaryExtractorDataField.setText(JMeterUtils.getResString("boundaryextractor_render_no_text")); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void setBackgroundColor(Color backGround) {
        // NOOP
    }

}
