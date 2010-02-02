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
import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;

public abstract class SamplerResultTab implements ResultRenderer {

    // N.B. these are not multi-threaded, so don't make it static
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // ISO format $NON-NLS-1$

    private static final String NL = "\n"; // $NON-NLS-1$

    public static final Color SERVER_ERROR_COLOR = Color.red;

    public static final Color CLIENT_ERROR_COLOR = Color.blue;

    public static final Color REDIRECT_COLOR = Color.green;

    protected static final String TEXT_COMMAND = "text"; // $NON-NLS-1$

    private static final String STYLE_SERVER_ERROR = "ServerError"; // $NON-NLS-1$

    private static final String STYLE_CLIENT_ERROR = "ClientError"; // $NON-NLS-1$

    private static final String STYLE_REDIRECT = "Redirect"; // $NON-NLS-1$

    private JTextPane stats;

    private JPanel resultsPane; /** Response Data pane */
    protected JScrollPane resultsScrollPane; /** Contains results; contained in resultsPane */
    protected JEditorPane results; /** Response Data shown here */

    private JLabel imageLabel;

    private JPanel requestPane;

    private JTextArea sampleDataField; /** request pane content */

    protected JTabbedPane rightSide; /** holds the tabbed panes */

    private int lastSelectedTab;

    private Object userObject = null; // Could be SampleResult or AssertionResult

    private SampleResult sampleResult = null;

    private AssertionResult assertionResult = null;

    protected SearchTextExtension searchTextExtension;

    private JPanel searchPanel = null;

    protected boolean activateSearchExtension = true; // most current subclasses can process text

    private Color backGround;

    public void clearData() {
        results.setText("");// Response Data // $NON-NLS-1$
        sampleDataField.setText("");// Request Data // $NON-NLS-1$
    }

    public void init() {
        rightSide.addTab(JMeterUtils.getResString("view_results_tab_sampler"), createResponseMetadataPanel()); // $NON-NLS-1$
        // Create the panels for the other tabs
        requestPane = createRequestPanel();
        resultsPane = createResponseDataPanel();
    }

    public void setupTabPane() {
        StyledDocument statsDoc = stats.getStyledDocument();
        try {
            statsDoc.remove(0, statsDoc.getLength());
            sampleDataField.setText(""); // $NON-NLS-1$
            results.setText(""); // $NON-NLS-1$
            if (userObject instanceof SampleResult) {
                sampleResult = (SampleResult) userObject;
                // We are displaying a SampleResult
                setupTabPaneForSampleResult();

                // load time label
                String sd = sampleResult.getSamplerData();
                if (sd != null) {
                    String rh = sampleResult.getRequestHeaders();
                    if (rh != null) {
                        StringBuffer sb = new StringBuffer(sd.length() + rh.length() + 20);
                        sb.append(sd);
                        sb.append("\n"); //$NON-NLS-1$
                        sb.append(JMeterUtils.getResString("view_results_request_headers")); //$NON-NLS-1$
                        sb.append("\n"); //$NON-NLS-1$
                        sb.append(rh);
                        sd = sb.toString();
                    }
                    sampleDataField.setText(sd);
                }

                StringBuffer statsBuff = new StringBuffer(200);
                statsBuff.append(JMeterUtils.getResString("view_results_thread_name")).append(sampleResult.getThreadName()).append(NL); //$NON-NLS-1$
                String startTime = dateFormat.format(new Date(sampleResult.getStartTime()));
                statsBuff.append(JMeterUtils.getResString("view_results_sample_start")).append(startTime).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_load_time")).append(sampleResult.getTime()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_latency")).append(sampleResult.getLatency()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_size_in_bytes")).append(sampleResult.getBytes()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_sample_count")).append(sampleResult.getSampleCount()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_error_count")).append(sampleResult.getErrorCount()).append(NL); //$NON-NLS-1$
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), null);
                statsBuff = new StringBuffer(); // reset for reuse

                String responseCode = sampleResult.getResponseCode();

                int responseLevel = 0;
                if (responseCode != null) {
                    try {
                        responseLevel = Integer.parseInt(responseCode) / 100;
                    } catch (NumberFormatException numberFormatException) {
                        // no need to change the foreground color
                    }
                }

                Style style = null;
                switch (responseLevel) {
                case 3:
                    style = statsDoc.getStyle(STYLE_REDIRECT);
                    break;
                case 4:
                    style = statsDoc.getStyle(STYLE_CLIENT_ERROR);
                    break;
                case 5:
                    style = statsDoc.getStyle(STYLE_SERVER_ERROR);
                    break;
                }

                statsBuff.append(JMeterUtils.getResString("view_results_response_code")).append(responseCode).append(NL); //$NON-NLS-1$
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), style);
                statsBuff = new StringBuffer(100); // reset for reuse

                // response message label
                String responseMsgStr = sampleResult.getResponseMessage();

                statsBuff.append(JMeterUtils.getResString("view_results_response_message")).append(responseMsgStr).append(NL); //$NON-NLS-1$

                statsBuff.append(NL);
                statsBuff.append(JMeterUtils.getResString("view_results_response_headers")).append(NL); //$NON-NLS-1$
                statsBuff.append(sampleResult.getResponseHeaders()).append(NL);
                statsBuff.append(NL);
                final String samplerClass = sampleResult.getClass().getName();
                statsBuff.append(samplerClass.substring(1 + samplerClass.lastIndexOf('.'))).append(" "+ JMeterUtils.getResString("view_results_fields")).append(NL); //$NON-NLS-1$
                statsBuff.append("ContentType: ").append(sampleResult.getContentType()).append(NL);
                statsBuff.append("DataEncoding: ").append(sampleResult.getDataEncodingNoDefault()).append(NL);
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), null);
                statsBuff = null; // Done

                // Reset search
                if (activateSearchExtension) {
                    searchTextExtension.resetTextToFind();
                }

            } else if (userObject instanceof AssertionResult) {
                assertionResult = (AssertionResult) userObject;

                // We are displaying an AssertionResult
                setupTabPaneForAssertionResult();

                StringBuffer statsBuff = new StringBuffer(100);
                statsBuff.append(JMeterUtils.getResString("view_results_assertion_error")).append(assertionResult.isError()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_assertion_failure")).append(assertionResult.isFailure()).append(NL); //$NON-NLS-1$
                statsBuff.append(JMeterUtils.getResString("view_results_assertion_failure_message")).append(assertionResult.getFailureMessage()).append(NL); //$NON-NLS-1$
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), null);
                statsBuff = null;
            }
        } catch (BadLocationException exc) {
            stats.setText(exc.getLocalizedMessage());
        }
    }

    private void setupTabPaneForSampleResult() {
        // Set the title for the first tab
        rightSide.setTitleAt(0, JMeterUtils.getResString("view_results_tab_sampler")); //$NON-NLS-1$
        // Add the other tabs if not present
        if(rightSide.indexOfTab(JMeterUtils.getResString("view_results_tab_request")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("view_results_tab_request"), requestPane); // $NON-NLS-1$
        }
        if(rightSide.indexOfTab(JMeterUtils.getResString("view_results_tab_response")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("view_results_tab_response"), resultsPane); // $NON-NLS-1$
        }
        // restore last selected tab
        if (lastSelectedTab < rightSide.getTabCount()) {
            rightSide.setSelectedIndex(lastSelectedTab);
        }
    }

    private void setupTabPaneForAssertionResult() {
        // Set the title for the first tab
        rightSide.setTitleAt(0, JMeterUtils.getResString("view_results_tab_assertion")); //$NON-NLS-1$
        // Remove the other tabs if present
        int requestTabIndex = rightSide.indexOfTab(JMeterUtils.getResString("view_results_tab_request")); // $NON-NLS-1$
        if(requestTabIndex >= 0) {
            rightSide.removeTabAt(requestTabIndex);
        }
        int responseTabIndex = rightSide.indexOfTab(JMeterUtils.getResString("view_results_tab_response")); // $NON-NLS-1$
        if(responseTabIndex >= 0) {
            rightSide.removeTabAt(responseTabIndex);
        }
    }

    private Component createResponseMetadataPanel() {
        stats = new JTextPane();
        stats.setEditable(false);
        stats.setBackground(backGround);

        // Add styles to use for different types of status messages
        StyledDocument doc = (StyledDocument) stats.getDocument();

        Style style = doc.addStyle(STYLE_REDIRECT, null);
        StyleConstants.setForeground(style, REDIRECT_COLOR);

        style = doc.addStyle(STYLE_CLIENT_ERROR, null);
        StyleConstants.setForeground(style, CLIENT_ERROR_COLOR);

        style = doc.addStyle(STYLE_SERVER_ERROR, null);
        StyleConstants.setForeground(style, SERVER_ERROR_COLOR);

        JScrollPane pane = GuiUtils.makeScrollPane(stats);
        pane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return pane;
    }

    private JPanel createRequestPanel() {
        sampleDataField = new JTextArea();
        sampleDataField.setEditable(false);
        sampleDataField.setLineWrap(true);
        sampleDataField.setWrapStyleWord(true);

        JPanel pane = new JPanel(new BorderLayout(0, 5));
        pane.add(GuiUtils.makeScrollPane(sampleDataField));
        return pane;
    }

    private JPanel createResponseDataPanel() {
        results = new JEditorPane();
        results.setEditable(false);

        resultsScrollPane = GuiUtils.makeScrollPane(results);
        imageLabel = new JLabel();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(resultsScrollPane, BorderLayout.CENTER);

        if (activateSearchExtension) {
            // Add search text extension
            searchTextExtension = new SearchTextExtension();
            searchTextExtension.init(panel);
            searchPanel = searchTextExtension.createSearchTextExtensionPane();
            searchTextExtension.setResults(results);
            searchPanel.setVisible(true);
            panel.add(searchPanel, BorderLayout.PAGE_END);
        }

        return panel;
    }

    private void showImage(Icon image) {
        imageLabel.setIcon(image);
        resultsScrollPane.setViewportView(imageLabel);
    }

    public synchronized void setSamplerResult(Object sample) {
        userObject = sample;
    }

    public synchronized void setRightSide(JTabbedPane side) {
        rightSide = side;
    }

    public void setLastSelectedTab(int index) {
        lastSelectedTab = index;
    }

    public void renderImage(SampleResult sampleResult) {
        byte[] responseBytes = sampleResult.getResponseData();
        if (responseBytes != null) {
            showImage(new ImageIcon(responseBytes)); //TODO implement other non-text types
        }
    }

    public void setBackgroundColor(Color backGround){
        this.backGround = backGround;
    }
}