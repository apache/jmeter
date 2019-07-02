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

/**
 *
 */
package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.extractor.Extractor;
import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * Implement ResultsRender for CSS/JQuery tester
 * @since 2.10
 */
public class RenderAsCssJQuery implements ResultRenderer, ActionListener {

    private static final String CSSJQUEY_TESTER_COMMAND = "cssjquery_tester"; // $NON-NLS-1$

    private JPanel cssJqueryPane;

    private JSyntaxTextArea cssJqueryDataField;

    private JLabeledTextField cssJqueryField;

    private JTextArea cssJqueryResultField;

    private JLabeledTextField attributeField;

    private JTabbedPane rightSide;

    private JLabeledChoice cssJqueryLabeledChoice;

    private SampleResult sampleResult = null;

    /** {@inheritDoc} */
    @Override
    public void clearData() {
        this.cssJqueryDataField.setText(""); // $NON-NLS-1$
        this.cssJqueryResultField.setText(""); // $NON-NLS-1$
        // don't set cssJqueryField to empty to keep it
        // don't set attribute to empty to keep it
        // don't change impl
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        // Create the panels for the cssJquery tab
        cssJqueryPane = createCssJqueryPanel();
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
        if ((sampleResult != null) && (CSSJQUEY_TESTER_COMMAND.equals(command))) {
            String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
            executeAndShowCssJqueryTester(response);
        }
    }

    /**
     * Launch cssJquery engine to parse a input text
     * @param textToParse
     */
    private void executeAndShowCssJqueryTester(String textToParse) {
        if (textToParse != null && textToParse.length() > 0
                && this.cssJqueryField.getText().length() > 0) {
            this.cssJqueryResultField.setText(process(textToParse));
            this.cssJqueryResultField.setCaretPosition(0); // go to first line
        }
    }

    private String process(String textToParse) {
        try {
            List<String> result = new ArrayList<>();
            Extractor extractor = HtmlExtractor.getExtractorImpl(cssJqueryLabeledChoice.getText());
            final int nbFound = extractor.extract(
                    cssJqueryField.getText(), attributeField.getText(), -1, textToParse, result, 0, null);

            // Construct a multi-line string with all matches
            StringBuilder sb = new StringBuilder();
            sb.append("Match count: ").append(nbFound).append("\n");
            for (int j = 0; j < nbFound; j++) {
                String mr = result.get(j);
                sb.append("Match[").append(j+1).append("]=").append(mr).append("\n");
            }
            return sb.toString();
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder();
            String message = MessageFormat.format(
                    JMeterUtils.getResString("cssjquery_tester_error") // $NON-NLS-1$
                    , new Object[]{cssJqueryField.getText(), ex.getMessage()});
            sb.append(message);
            return sb.toString();
        }

    }
    /** {@inheritDoc} */
   @Override
   public void renderResult(SampleResult sampleResult) {
       clearData();
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        cssJqueryDataField.setText(response);
        cssJqueryDataField.setCaretPosition(0);
    }

    /** {@inheritDoc} */
    @Override
    public void setupTabPane() {
         // Add cssJquery tester pane
        if (rightSide.indexOfTab(JMeterUtils.getResString("cssjquery_tester_title")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("cssjquery_tester_title"), cssJqueryPane); // $NON-NLS-1$
        }
        clearData();
    }

    /**
     * @return RegExp Tester panel
     */
    private JPanel createCssJqueryPanel() {
        cssJqueryDataField = JSyntaxTextArea.getInstance(50, 80, true);
        cssJqueryDataField.setCodeFoldingEnabled(true);
        cssJqueryDataField.setEditable(false);
        cssJqueryDataField.setBracketMatchingEnabled(false);
        cssJqueryDataField.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
        cssJqueryDataField.setLanguage(SyntaxConstants.SYNTAX_STYLE_HTML);
        cssJqueryDataField.setLineWrap(true);
        cssJqueryDataField.setWrapStyleWord(true);

        JScrollPane cssJqueryDataPane = JTextScrollPane.getInstance(cssJqueryDataField, true);
        cssJqueryDataPane.setPreferredSize(new Dimension(0, 200));

        JPanel pane = new JPanel(new BorderLayout(0, 5));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                cssJqueryDataPane, createCssJqueryTasksPanel());
        mainSplit.setDividerLocation(0.6d);
        mainSplit.setOneTouchExpandable(true);
        pane.add(mainSplit, BorderLayout.CENTER);
        return pane;
    }

    private static String[] getImplementations() {
        return new String[]{
            HtmlExtractor.EXTRACTOR_JSOUP,
            HtmlExtractor.EXTRACTOR_JODD,
            HtmlExtractor.DEFAULT_EXTRACTOR
        };
    }
    /**
     * Create the CssJquery task pane
     *
     * @return CssJquery task pane
     */
    private JPanel createCssJqueryTasksPanel() {
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        JPanel cssJqueryActionPanel = new JPanel();
        cssJqueryActionPanel.setLayout(g);
        Border margin = new EmptyBorder(5, 5, 0, 5);
        cssJqueryActionPanel.setBorder(margin);
        cssJqueryField = new JLabeledTextField(JMeterUtils.getResString("cssjquery_tester_field")); // $NON-NLS-1$
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx=0;
        c.gridy=0;
        cssJqueryActionPanel.add(cssJqueryField, c);

        cssJqueryLabeledChoice = new JLabeledChoice(
                JMeterUtils.getResString("cssjquery_impl"), // $NON-NLS-1$
                getImplementations());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx=1;
        c.gridy=0;
        cssJqueryActionPanel.add(cssJqueryLabeledChoice, c);

        attributeField = new JLabeledTextField(JMeterUtils.getResString("cssjquery_attribute")); // $NON-NLS-1$
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx=0;
        c.gridy=1;
        cssJqueryActionPanel.add(attributeField, c);

        JButton cssJqueryTester = new JButton(JMeterUtils.getResString("cssjquery_tester_button_test")); // $NON-NLS-1$
        cssJqueryTester.setActionCommand(CSSJQUEY_TESTER_COMMAND);
        cssJqueryTester.addActionListener(this);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx=1;
        c.gridy=1;
        cssJqueryActionPanel.add(cssJqueryTester, c);


        cssJqueryResultField = new JTextArea();
        cssJqueryResultField.setEditable(false);
        cssJqueryResultField.setLineWrap(true);
        cssJqueryResultField.setWrapStyleWord(true);

        JPanel cssJqueryTasksPanel = new JPanel(new BorderLayout(0, 5));
        cssJqueryTasksPanel.add(cssJqueryActionPanel, BorderLayout.NORTH);
        cssJqueryTasksPanel.add(GuiUtils.makeScrollPane(cssJqueryResultField), BorderLayout.CENTER);

        return cssJqueryTasksPanel;
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
    public String toString() {
        return JMeterUtils.getResString("cssjquery_tester_title"); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void renderImage(SampleResult sampleResult) {
        clearData();
        cssJqueryDataField.setText(JMeterUtils.getResString("cssjquery_render_no_text")); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void setBackgroundColor(Color backGround) {
    }

}
