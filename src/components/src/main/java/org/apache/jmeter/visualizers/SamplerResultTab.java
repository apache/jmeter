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
import java.awt.GridLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.JSyntaxSearchToolBar;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.TextBoxDialoger.TextBoxDoubleClick;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SearchTextExtension.JEditorPaneSearchProvider;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.reflect.Functor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Right side in View Results Tree
 *
 */
public abstract class SamplerResultTab implements ResultRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenderAsText.class);
    // N.B. these are not multi-threaded, so don't make it static
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // ISO format $NON-NLS-1$

    private static final String NL = "\n"; // $NON-NLS-1$

    public static final Color SERVER_ERROR_COLOR = Color.red;

    public static final Color CLIENT_ERROR_COLOR = Color.blue;

    public static final Color REDIRECT_COLOR = Color.green;

    protected static final String TEXT_COMMAND = "text"; // $NON-NLS-1$

    protected static final String REQUEST_VIEW_COMMAND = "change_request_view"; // $NON-NLS-1$

    private static final String STYLE_SERVER_ERROR = "ServerError"; // $NON-NLS-1$

    private static final String STYLE_CLIENT_ERROR = "ClientError"; // $NON-NLS-1$

    private static final String STYLE_REDIRECT = "Redirect"; // $NON-NLS-1$

    private JTextPane stats;

    /** Response Data pane */
    private JPanel resultsPane;

    /** Contains results; contained in resultsPane */
    protected JScrollPane resultsScrollPane;

    private JSyntaxTextArea headerData;
    /** Response Data shown here */
    protected JEditorPane results;

    private JLabel imageLabel;

    /** request pane content */
    private RequestPanel requestPanel;

    /** holds the tabbed panes */
    protected JTabbedPane rightSide;

    private int lastSelectedTab;

    private Object userObject = null; // Could be SampleResult or AssertionResult

    private SampleResult sampleResult = null;

    private AssertionResult assertionResult = null;

    protected SearchTextExtension searchTextExtension;

    protected boolean activateSearchExtension = true; // most current subclasses can process text

    private Color backGround;

    private static final String[] COLUMNS_RESULT = new String[] {
            " ", // one space for blank header // $NON-NLS-1$
            " " }; // one space for blank header  // $NON-NLS-1$

    private static final String[] COLUMNS_HEADERS = new String[] {
            "view_results_table_headers_key", // $NON-NLS-1$
            "view_results_table_headers_value" }; // $NON-NLS-1$

    private static final String[] COLUMNS_FIELDS = new String[] {
            "view_results_table_fields_key", // $NON-NLS-1$
            "view_results_table_fields_value" }; // $NON-NLS-1$

    private final ObjectTableModel resultModel;

    private final ObjectTableModel resHeadersModel;

    private final ObjectTableModel resFieldsModel;

    private JTable tableResult = null;

    private JTable tableResHeaders = null;

    private JTable tableResFields = null;

    private JTabbedPane tabbedResult = null;

    private JScrollPane paneRaw = null;

    private JSplitPane paneParsed = null;

    // to save last select tab (raw/parsed)
    private int lastResultTabIndex= 0;

    // Result column renderers
    private static final TableCellRenderer[] RENDERERS_RESULT = new TableCellRenderer[] {
            null, // Key
            null, // Value
    };

    // Response headers column renderers
    private static final TableCellRenderer[] RENDERERS_HEADERS = new TableCellRenderer[] {
            null, // Key
            null, // Value
    };

    // Response fields column renderers
    private static final TableCellRenderer[] RENDERERS_FIELDS = new TableCellRenderer[] {
            null, // Key
            null, // Value
    };

    public SamplerResultTab() {
        // create tables
        resultModel = new ObjectTableModel(COLUMNS_RESULT, RowResult.class, // The object used for each row
                new Functor[] {
                        new Functor("getKey"), // $NON-NLS-1$
                        new Functor("getValue") }, // $NON-NLS-1$
                new Functor[] {
                        null, null }, new Class[] {
                        String.class, String.class }, false);
        resHeadersModel = new ObjectTableModel(COLUMNS_HEADERS,
                RowResult.class, // The object used for each row
                new Functor[] {
                        new Functor("getKey"), // $NON-NLS-1$
                        new Functor("getValue") }, // $NON-NLS-1$
                new Functor[] {
                        null, null }, new Class[] {
                        String.class, String.class }, false);
        resFieldsModel = new ObjectTableModel(COLUMNS_FIELDS, RowResult.class, // The object used for each row
                new Functor[] {
                        new Functor("getKey"), // $NON-NLS-1$
                        new Functor("getValue") }, // $NON-NLS-1$
                new Functor[] {
                        null, null }, new Class[] {
                        String.class, String.class }, false);
    }

    @Override
    public void clearData() {
        results.setText("");// Response Data // $NON-NLS-1$
        headerData.setInitialText(""); // $NON-NLS-1$
        requestPanel.clearData();// Request Data // $NON-NLS-1$
        stats.setText(""); // Sampler result // $NON-NLS-1$
        resultModel.clearData();
        resHeadersModel.clearData();
        resFieldsModel.clearData();
    }

    @Override
    public void init() {
        rightSide.addTab(
                JMeterUtils.getResString("view_results_tab_sampler"), createResponseMetadataPanel()); // $NON-NLS-1$
        // Create the panels for the other tabs
        requestPanel = new RequestPanel();
        resultsPane = createResponseDataPanel();
    }

    @Override
    @SuppressWarnings("boxing")
    public void setupTabPane() {
        // Clear all data before display a new
        this.clearData();
        StyledDocument statsDoc = stats.getStyledDocument();
        try {
            if (userObject instanceof SampleResult) {
                sampleResult = (SampleResult) userObject;
                // We are displaying a SampleResult
                setupTabPaneForSampleResult();
                requestPanel.setSamplerResult(sampleResult);

                final String samplerClass = sampleResult.getClass().getName();
                String typeResult = samplerClass.substring(1 + samplerClass.lastIndexOf('.'));

                StringBuilder statsBuff = new StringBuilder(200);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_thread_name")) //$NON-NLS-1$
                        .append(sampleResult.getThreadName()).append(NL);
                String startTime = dateFormat
                        .format(new Date(sampleResult.getStartTime()));
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_sample_start")) //$NON-NLS-1$
                        .append(startTime).append(NL);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_load_time")) //$NON-NLS-1$
                        .append(sampleResult.getTime()).append(NL);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_connect_time")) //$NON-NLS-1$
                        .append(sampleResult.getConnectTime()).append(NL);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_latency")) //$NON-NLS-1$
                        .append(sampleResult.getLatency()).append(NL);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_size_in_bytes")) //$NON-NLS-1$
                        .append(sampleResult.getBytesAsLong()).append(NL);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_sent_bytes")) //$NON-NLS-1$
                        .append(sampleResult.getSentBytes()).append(NL);
                statsBuff
                        .append(JMeterUtils.getResString(
                                "view_results_size_headers_in_bytes")) //$NON-NLS-1$
                        .append(sampleResult.getHeadersSize()).append(NL);
                statsBuff
                        .append(JMeterUtils.getResString(
                                "view_results_size_body_in_bytes")) //$NON-NLS-1$
                        .append(sampleResult.getBodySizeAsLong()).append(NL);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_sample_count")) //$NON-NLS-1$
                        .append(sampleResult.getSampleCount()).append(NL);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_error_count")) //$NON-NLS-1$
                        .append(sampleResult.getErrorCount()).append(NL);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_datatype")) //$NON-NLS-1$
                        .append(sampleResult.getDataType()).append(NL);
                statsDoc.insertString(statsDoc.getLength(),
                        statsBuff.toString(), null);
                statsBuff.setLength(0); // reset for reuse

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
                default: // quieten Findbugs
                    break; // default - do nothing
                }

                statsBuff.append(JMeterUtils.getResString("view_results_response_code")).append(responseCode).append(NL); //$NON-NLS-1$
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), style);
                statsBuff.setLength(0); // reset for reuse

                // response message label
                String responseMsgStr = sampleResult.getResponseMessage();

                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_response_message")) //$NON-NLS-1$
                        .append(responseMsgStr).append(NL);
                statsBuff.append(NL);
                statsBuff.append(NL);
                statsBuff
                        .append(typeResult + " " //$NON-NLS-1$
                                + JMeterUtils
                                        .getResString("view_results_fields"))
                        .append(NL); // $NON-NLS-2$
                statsBuff.append("ContentType: ") //$NON-NLS-1$
                        .append(sampleResult.getContentType()).append(NL);
                statsBuff.append("DataEncoding: ") //$NON-NLS-1$
                        .append(sampleResult.getDataEncodingNoDefault())
                        .append(NL);
                statsDoc.insertString(statsDoc.getLength(), statsBuff.toString(), null);
                statsBuff = null; // NOSONAR Help gc

                // Tabbed results: fill table
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel("view_results_thread_name"), //$NON-NLS-1$
                        sampleResult.getThreadName()));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel("view_results_sample_start"), //$NON-NLS-1$
                        startTime));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel("view_results_load_time"), //$NON-NLS-1$
                        sampleResult.getTime()));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel("view_results_connect_time"), //$NON-NLS-1$
                        sampleResult.getConnectTime()));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel("view_results_latency"), //$NON-NLS-1$
                        sampleResult.getLatency()));
                resultModel.addRow(new RowResult(
                        JMeterUtils
                                .getParsedLabel("view_results_size_in_bytes"), //$NON-NLS-1$
                        sampleResult.getBytesAsLong()));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel("view_results_sent_bytes"), //$NON-NLS-1$
                        sampleResult.getSentBytes()));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel(
                                "view_results_size_headers_in_bytes"), //$NON-NLS-1$
                        sampleResult.getHeadersSize()));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel(
                                "view_results_size_body_in_bytes"), //$NON-NLS-1$
                        sampleResult.getBodySizeAsLong()));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel("view_results_sample_count"), //$NON-NLS-1$
                        sampleResult.getSampleCount()));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel("view_results_error_count"), //$NON-NLS-1$
                        sampleResult.getErrorCount()));
                resultModel
                        .addRow(new RowResult(
                                JMeterUtils.getParsedLabel(
                                        "view_results_response_code"), //$NON-NLS-1$
                                responseCode));
                resultModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel(
                                "view_results_response_message"), //$NON-NLS-1$
                        responseMsgStr));

                // Parsed response headers
                LinkedHashMap<String, String> lhm = JMeterUtils.parseHeaders(sampleResult.getResponseHeaders());
                Set<Entry<String, String>> keySet = lhm.entrySet();
                for (Entry<String, String> entry : keySet) {
                    resHeadersModel.addRow(new RowResult(entry.getKey(), entry.getValue()));
                }

                headerData.setInitialText(sampleResult.getResponseHeaders());

                // Fields table
                resFieldsModel.addRow(new RowResult("Type Result ", typeResult)); //$NON-NLS-1$
                //not sure needs I18N?
                resFieldsModel.addRow(new RowResult("ContentType", sampleResult.getContentType())); //$NON-NLS-1$
                resFieldsModel.addRow(new RowResult("DataEncoding", sampleResult.getDataEncodingNoDefault())); //$NON-NLS-1$

                // Reset search
                if (activateSearchExtension) {
                    searchTextExtension.resetTextToFind();
                }

            } else if (userObject instanceof AssertionResult) {
                assertionResult = (AssertionResult) userObject;

                // We are displaying an AssertionResult
                setupTabPaneForAssertionResult();

                StringBuilder statsBuff = new StringBuilder(100);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_assertion_error")) //$NON-NLS-1$
                        .append(assertionResult.isError()).append(NL);
                statsBuff
                        .append(JMeterUtils
                                .getResString("view_results_assertion_failure")) //$NON-NLS-1$
                        .append(assertionResult.isFailure()).append(NL);
                statsBuff
                        .append(JMeterUtils.getResString(
                                "view_results_assertion_failure_message")) //$NON-NLS-1$
                        .append(assertionResult.getFailureMessage()).append(NL);
                statsDoc.insertString(statsDoc.getLength(),
                        statsBuff.toString(), null);
            }
            stats.setCaretPosition(1);
        } catch (BadLocationException exc) {
            stats.setText(exc.getLocalizedMessage());
        }
    }

    private void setupTabPaneForSampleResult() {
        // restore tabbed pane parsed if needed
        if (tabbedResult.getTabCount() < 2) {
            tabbedResult.insertTab(JMeterUtils.getResString("view_results_table_result_tab_parsed"), null, paneParsed, null, 1); //$NON-NLS-1$
            tabbedResult.setSelectedIndex(lastResultTabIndex); // select last tab
        }
        // Set the title for the first tab
        rightSide.setTitleAt(0, JMeterUtils.getResString("view_results_tab_sampler")); //$NON-NLS-1$
        // Add the other tabs if not present
        if(rightSide.indexOfTab(JMeterUtils.getResString("view_results_tab_request")) < 0) { // $NON-NLS-1$
            rightSide.addTab(JMeterUtils.getResString("view_results_tab_request"), requestPanel.getPanel()); // $NON-NLS-1$
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
        // Remove the other (parsed) tab if present
        if (tabbedResult.getTabCount() >= 2) {
            lastResultTabIndex = tabbedResult.getSelectedIndex();
            int parsedTabIndex = tabbedResult.indexOfTab(JMeterUtils.getResString("view_results_table_result_tab_parsed")); // $NON-NLS-1$
            if(parsedTabIndex >= 0) {
                tabbedResult.removeTabAt(parsedTabIndex);
            }
        }
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

        paneRaw = GuiUtils.makeScrollPane(stats);
        paneRaw.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        // Set up the 1st table Result with empty headers
        tableResult = new JTable(resultModel);
        JMeterUtils.applyHiDPI(tableResult);
        tableResult.setToolTipText(JMeterUtils.getResString("textbox_tooltip_cell")); // $NON-NLS-1$
        tableResult.addMouseListener(new TextBoxDoubleClick(tableResult));
        setFirstColumnPreferredSize(tableResult);
        RendererUtils.applyRenderers(tableResult, RENDERERS_RESULT);

        // Set up the 2nd table
        tableResHeaders = new JTable(resHeadersModel);
        JMeterUtils.applyHiDPI(tableResHeaders);
        tableResHeaders.setToolTipText(JMeterUtils.getResString("textbox_tooltip_cell")); // $NON-NLS-1$
        tableResHeaders.addMouseListener(new TextBoxDoubleClick(tableResHeaders));
        setFirstColumnPreferredSize(tableResHeaders);
        tableResHeaders.getTableHeader().setDefaultRenderer(
                new HeaderAsPropertyRenderer());
        RendererUtils.applyRenderers(tableResHeaders, RENDERERS_HEADERS);

        // Set up the 3rd table
        tableResFields = new JTable(resFieldsModel);
        JMeterUtils.applyHiDPI(tableResFields);
        tableResFields.setToolTipText(JMeterUtils.getResString("textbox_tooltip_cell")); // $NON-NLS-1$
        tableResFields.addMouseListener(new TextBoxDoubleClick(tableResFields));
        setFirstColumnPreferredSize(tableResFields);
        tableResFields.getTableHeader().setDefaultRenderer(
                new HeaderAsPropertyRenderer());
        RendererUtils.applyRenderers(tableResFields, RENDERERS_FIELDS);

        // Prepare the Results tabbed pane
        tabbedResult = new JTabbedPane(SwingConstants.BOTTOM);

        // Create the split pane
        JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                GuiUtils.makeScrollPane(tableResHeaders),
                GuiUtils.makeScrollPane(tableResFields));
        topSplit.setOneTouchExpandable(true);
        topSplit.setResizeWeight(0.80); // set split ratio
        topSplit.setBorder(null); // see bug jdk 4131528

        paneParsed = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                GuiUtils.makeScrollPane(tableResult), topSplit);
        paneParsed.setOneTouchExpandable(true);
        paneParsed.setResizeWeight(0.40); // set split ratio
        paneParsed.setBorder(null); // see bug jdk 4131528

        // setup bottom tabs, first Raw, second Parsed
        tabbedResult.addTab(JMeterUtils.getResString("view_results_table_result_tab_raw"), paneRaw); //$NON-NLS-1$
        tabbedResult.addTab(JMeterUtils.getResString("view_results_table_result_tab_parsed"), paneParsed); //$NON-NLS-1$

        // Hint to background color on bottom tabs (grey, not blue)
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(tabbedResult);
        return panel;
    }

    private JPanel createResponseDataPanel() {
        results = new JEditorPane();
        results.setEditable(false);

        headerData = JSyntaxTextArea.getInstance(20, 80, true);
        headerData.setEditable(false);
        headerData.setLineWrap(true);
        headerData.setWrapStyleWord(true);

        JPanel headersAndSearchPanel = new JPanel(new BorderLayout());
        headersAndSearchPanel.add(new JSyntaxSearchToolBar(headerData).getToolBar(), BorderLayout.NORTH);
        headersAndSearchPanel.add(JTextScrollPane.getInstance(headerData), BorderLayout.CENTER);

        resultsScrollPane = GuiUtils.makeScrollPane(results);
        imageLabel = new JLabel();

        JPanel resultAndSearchPanel = new JPanel(new BorderLayout());
        resultAndSearchPanel.add(resultsScrollPane, BorderLayout.CENTER);


        if (activateSearchExtension) {
            // Add search text extension
            searchTextExtension = new SearchTextExtension(new JEditorPaneSearchProvider(results));
            resultAndSearchPanel.add(searchTextExtension.getSearchToolBar(), BorderLayout.NORTH);
        }

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.addTab(JMeterUtils.getResString("view_results_response_body"), new JScrollPane(resultAndSearchPanel));
        tabbedPane.addTab(JMeterUtils.getResString("view_results_response_headers"), new JScrollPane(headersAndSearchPanel));

        JPanel gPanel = new JPanel(new GridLayout(1,1));
        gPanel.add(tabbedPane);
        return gPanel;
    }

    private void showImage(Icon image) {
        imageLabel.setIcon(image);
        resultsScrollPane.setViewportView(imageLabel);
    }

    @Override
    public synchronized void setSamplerResult(Object sample) {
        userObject = sample;
    }

    @Override
    public synchronized void setRightSide(JTabbedPane side) {
        rightSide = side;
    }

    @Override
    public void setLastSelectedTab(int index) {
        lastSelectedTab = index;
    }

    @Override
    public void renderImage(SampleResult sampleResult) {
        byte[] responseBytes = sampleResult.getResponseData();
        if (responseBytes != null) {
            showImage(new ImageIcon(responseBytes)); //TODO implement other non-text types
        }
    }

    @Override
    public void setBackgroundColor(Color backGround){
        this.backGround = backGround;
    }

    private void setFirstColumnPreferredSize(JTable table) {
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMaxWidth(300);
        column.setPreferredWidth(180);
    }

    /**
     * For model table
     */
    public static class RowResult {
        private String key;

        private Object value;

        public RowResult(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        /**
         * @return the key
         */
        public synchronized String getKey() {
            return key;
        }

        /**
         * @param key
         *            the key to set
         */
        public synchronized void setKey(String key) {
            this.key = key;
        }

        /**
         * @return the value
         */
        public synchronized Object getValue() {
            return value;
        }

        /**
         * @param value
         *            the value to set
         */
        public synchronized void setValue(Object value) {
            this.value = value;
        }
    }

    /**
     * Optimized way to set text based on :
     * http://javatechniques.com/blog/faster-jtextpane-text-insertion-part-i/
     * @param data String data
     */
    protected void setTextOptimized(String data) {
        Document document = results.getDocument();
        Document blank = new DefaultStyledDocument();
        results.setDocument(blank);
        try {
            document.insertString(0, data == null ? "" : data, null);
        } catch (BadLocationException ex) {
            LOGGER.error("Error inserting text", ex);
        }
        results.setDocument(document);
    }
}
