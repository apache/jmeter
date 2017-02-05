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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
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

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Implement ResultsRender for Regexp tester
 */
public class RenderAsRegexp implements ResultRenderer, ActionListener {

    private static final String REGEXP_TESTER_COMMAND = "regexp_tester"; // $NON-NLS-1$

    private JPanel regexpPane;

    private JTextArea regexpDataField;

    private JLabeledTextField regexpField;

    private JTextArea regexpResultField;

    private JTabbedPane rightSide;

    private SampleResult sampleResult = null;

    /** {@inheritDoc} */
    @Override
    public void clearData() {
        this.regexpDataField.setText(""); // $NON-NLS-1$
        // don't set empty to keep regexp
        // regexpField.setText(""); // $NON-NLS-1$
        this.regexpResultField.setText(""); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void init() {
        // Create the panels for the regexp tab
        regexpPane = createRegexpPanel();
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
        if ((sampleResult != null) && (REGEXP_TESTER_COMMAND.equals(command))) {
            String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
            executeAndShowRegexpTester(response);
        }
    }

    /**
     * Launch regexp engine to parse a input text
     * @param textToParse
     */
    private void executeAndShowRegexpTester(String textToParse) {
        if (textToParse != null && textToParse.length() > 0
                && this.regexpField.getText().length() > 0) {
            this.regexpResultField.setText(process(textToParse));
            this.regexpResultField.setCaretPosition(0); // go to first line
        }
    }

    private String process(String textToParse) {

        Perl5Matcher matcher = new Perl5Matcher();
        PatternMatcherInput input = new PatternMatcherInput(textToParse);

        PatternCacheLRU pcLRU = new PatternCacheLRU();
        Pattern pattern;
        try {
            pattern = pcLRU.getPattern(regexpField.getText(), Perl5Compiler.READ_ONLY_MASK);
        } catch (MalformedCachePatternException e) {
            return e.toString();
        }
        List<MatchResult> matches = new LinkedList<>();
        while (matcher.contains(input, pattern)) {
            matches.add(matcher.getMatch());
        }
        // Construct a multi-line string with all matches
        StringBuilder sb = new StringBuilder();
        final int size = matches.size();
        sb.append("Match count: ").append(size).append("\n");
        for (int j = 0; j < size; j++) {
            MatchResult mr = matches.get(j);
            final int groups = mr.groups();
            for (int i = 0; i < groups; i++) {
                sb.append("Match[").append(j+1).append("][").append(i).append("]=").append(mr.group(i)).append("\n");
            }
        }
        return sb.toString();

    }

    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        clearData();
        String response = ViewResultsFullVisualizer.getResponseAsString(sampleResult);
        regexpDataField.setText(response);
        regexpDataField.setCaretPosition(0);
    }

    /** {@inheritDoc} */
    @Override
    public void setupTabPane() {
         // Add regexp tester pane
        if (rightSide.indexOfTab(JMeterUtils.getResString("regexp_tester_title")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("regexp_tester_title"), regexpPane); // $NON-NLS-1$
        }
        clearData();
    }

    /**
     * @return RegExp Tester panel
     */
    private JPanel createRegexpPanel() {
        regexpDataField = new JTextArea();
        regexpDataField.setEditable(false);
        regexpDataField.setLineWrap(true);
        regexpDataField.setWrapStyleWord(true);

        JScrollPane regexpDataPane = GuiUtils.makeScrollPane(regexpDataField);
        regexpDataPane.setPreferredSize(new Dimension(0, 200));

        JPanel pane = new JPanel(new BorderLayout(0, 5));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                regexpDataPane, createRegexpTasksPanel());
        mainSplit.setDividerLocation(0.6d);
        mainSplit.setOneTouchExpandable(true);
        pane.add(mainSplit, BorderLayout.CENTER);
        return pane;
    }

    /**
     * Create the Regexp task pane
     *
     * @return Regexp task pane
     */
    private JPanel createRegexpTasksPanel() {
        JPanel regexpActionPanel = new JPanel();
        regexpActionPanel.setLayout(new BoxLayout(regexpActionPanel, BoxLayout.X_AXIS));
        Border margin = new EmptyBorder(5, 5, 0, 5);
        regexpActionPanel.setBorder(margin);
        regexpField = new JLabeledTextField(JMeterUtils.getResString("regexp_tester_field")); // $NON-NLS-1$
        regexpActionPanel.add(regexpField, BorderLayout.WEST);

        JButton regexpTester = new JButton(JMeterUtils.getResString("regexp_tester_button_test")); // $NON-NLS-1$
        regexpTester.setActionCommand(REGEXP_TESTER_COMMAND);
        regexpTester.addActionListener(this);
        regexpActionPanel.add(regexpTester, BorderLayout.EAST);

        regexpResultField = new JTextArea();
        regexpResultField.setEditable(false);
        regexpResultField.setLineWrap(true);
        regexpResultField.setWrapStyleWord(true);

        JPanel regexpTasksPanel = new JPanel(new BorderLayout(0, 5));
        regexpTasksPanel.add(regexpActionPanel, BorderLayout.NORTH);
        regexpTasksPanel.add(GuiUtils.makeScrollPane(regexpResultField), BorderLayout.CENTER);

        return regexpTasksPanel;
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
        return JMeterUtils.getResString("regexp_tester_title"); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void renderImage(SampleResult sampleResult) {
        clearData();
        regexpDataField.setText(JMeterUtils.getResString("regexp_render_no_text")); // $NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public void setBackgroundColor(Color backGround) {
    }

}
