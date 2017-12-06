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

package org.apache.jmeter.protocol.http.visualizers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.TextBoxDialoger.TextBoxDoubleClick;
import org.apache.jmeter.protocol.http.config.MultipartUrlConfig;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.RequestView;
import org.apache.jmeter.visualizers.SamplerResultTab.RowResult;
import org.apache.jmeter.visualizers.SearchTextExtension;
import org.apache.jmeter.visualizers.SearchTextExtension.ISearchTextExtensionProvider;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.reflect.Functor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specializer panel to view a HTTP request parsed
 */
public class RequestViewHTTP implements RequestView {

    private static final Logger log = LoggerFactory.getLogger(RequestViewHTTP.class);

    private static final String KEY_LABEL = "view_results_table_request_tab_http"; //$NON-NLS-1$
    
    private static final String CHARSET_DECODE = StandardCharsets.ISO_8859_1.name();
    
    private static final String PARAM_CONCATENATE = "&"; //$NON-NLS-1$

    private JPanel paneParsed;

    private ObjectTableModel requestModel = null;

    private ObjectTableModel paramsModel = null;

    private ObjectTableModel headersModel = null;

    private static final String[] COLUMNS_REQUEST = new String[] {
            " ", // one space for blank header // $NON-NLS-1$ 
            " " }; // one space for blank header  // $NON-NLS-1$

    private static final String[] COLUMNS_PARAMS = new String[] {
            "view_results_table_request_params_key", // $NON-NLS-1$
            "view_results_table_request_params_value" }; // $NON-NLS-1$

    private static final String[] COLUMNS_HEADERS = new String[] {
            "view_results_table_request_headers_key", // $NON-NLS-1$
            "view_results_table_request_headers_value" }; // $NON-NLS-1$

    private JTable tableRequest = null;

    private JTable tableParams = null;

    private JTable tableHeaders = null;

    // Request headers column renderers
    private static final TableCellRenderer[] RENDERERS_REQUEST = new TableCellRenderer[] {
            null, // Key
            null, // Value
    };

    // Request headers column renderers
    private static final TableCellRenderer[] RENDERERS_PARAMS = new TableCellRenderer[] {
            null, // Key
            null, // Value
    };

    // Request headers column renderers
    private static final TableCellRenderer[] RENDERERS_HEADERS = new TableCellRenderer[] {
            null, // Key
            null, // Value
    };

    private SearchTextExtension searchTextExtension;

    /**
     * Pane to view HTTP request sample in view results tree
     */
    public RequestViewHTTP() {
        requestModel = new ObjectTableModel(COLUMNS_REQUEST, RowResult.class, // The object used for each row
                new Functor[] {
                        new Functor("getKey"), // $NON-NLS-1$
                        new Functor("getValue") }, // $NON-NLS-1$
                new Functor[] {
                        null, null }, new Class[] {
                        String.class, String.class }, false);
        paramsModel = new ObjectTableModel(COLUMNS_PARAMS, RowResult.class, // The object used for each row
                new Functor[] {
                        new Functor("getKey"), // $NON-NLS-1$
                        new Functor("getValue") }, // $NON-NLS-1$
                new Functor[] {
                        null, null }, new Class[] {
                        String.class, String.class }, false);
        headersModel = new ObjectTableModel(COLUMNS_HEADERS, RowResult.class, // The object used for each row
                new Functor[] {
                        new Functor("getKey"), // $NON-NLS-1$
                        new Functor("getValue") }, // $NON-NLS-1$
                new Functor[] {
                        null, null }, new Class[] {
                        String.class, String.class }, false);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.request.RequestView#init()
     */
    @Override
    public void init() {
        paneParsed = new JPanel(new BorderLayout(0, 5));
        paneParsed.add(createRequestPane());
        this.searchTextExtension = new SearchTextExtension();
        this.searchTextExtension.init(paneParsed);
        JPanel searchPanel = this.searchTextExtension.createSearchTextExtensionPane();
        searchPanel.setBorder(null);
        this.searchTextExtension.setSearchProvider(new RequestViewHttpSearchProvider());
        searchPanel.setVisible(true);
        paneParsed.add(searchPanel, BorderLayout.PAGE_END);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.request.RequestView#clearData()
     */
    @Override
    public void clearData() {
        requestModel.clearData();
        paramsModel.clearData();
        headersModel.clearData(); // clear results table before filling
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.request.RequestView#setSamplerResult(java.lang.Object)
     */
    @Override
    public void setSamplerResult(Object objectResult) {

        this.searchTextExtension.resetTextToFind();
        if (objectResult instanceof HTTPSampleResult) {
            HTTPSampleResult sampleResult = (HTTPSampleResult) objectResult;

            // Display with same order HTTP protocol
            requestModel.addRow(new RowResult(
                    JMeterUtils.getResString("view_results_table_request_http_method"), //$NON-NLS-1$
                    sampleResult.getHTTPMethod()));

            // Parsed request headers
            LinkedHashMap<String, String> lhm = JMeterUtils.parseHeaders(sampleResult.getRequestHeaders());
            for (Entry<String, String> entry : lhm.entrySet()) {
                headersModel.addRow(new RowResult(entry.getKey(), entry.getValue()));
            }

            URL hUrl = sampleResult.getURL();
            if (hUrl != null){ // can be null - e.g. if URL was invalid
                requestModel.addRow(new RowResult(JMeterUtils
                        .getResString("view_results_table_request_http_protocol"), //$NON-NLS-1$
                        hUrl.getProtocol()));
                requestModel.addRow(new RowResult(
                        JMeterUtils.getResString("view_results_table_request_http_host"), //$NON-NLS-1$
                        hUrl.getHost()));
                int port = hUrl.getPort() == -1 ? hUrl.getDefaultPort() : hUrl.getPort();
                requestModel.addRow(new RowResult(
                        JMeterUtils.getResString("view_results_table_request_http_port"), //$NON-NLS-1$
                        Integer.valueOf(port)));
                requestModel.addRow(new RowResult(
                        JMeterUtils.getResString("view_results_table_request_http_path"), //$NON-NLS-1$
                        hUrl.getPath()));
    
                String queryGet = hUrl.getQuery() == null ? "" : hUrl.getQuery(); //$NON-NLS-1$
                boolean isMultipart = isMultipart(lhm);

                // Concatenate query post if exists
                String queryPost = sampleResult.getQueryString();
                if (!isMultipart && StringUtils.isNotBlank(queryPost)) {
                    if (queryGet.length() > 0) {
                        queryGet += PARAM_CONCATENATE;
                    }
                    queryGet += queryPost;
                }
                
                if (StringUtils.isNotBlank(queryGet)) {
                    Set<Entry<String, String[]>> keys = RequestViewHTTP.getQueryMap(queryGet).entrySet();
                    for (Entry<String, String[]> entry : keys) {
                        for (String value : entry.getValue()) {
                            paramsModel.addRow(new RowResult(entry.getKey(), value));
                        }
                    }
                }

                if(isMultipart && StringUtils.isNotBlank(queryPost)) {
                    String contentType = lhm.get(HTTPConstants.HEADER_CONTENT_TYPE);
                    String boundaryString = extractBoundary(contentType);
                    MultipartUrlConfig urlconfig = new MultipartUrlConfig(boundaryString);
                    urlconfig.parseArguments(queryPost);
                    
                    for(JMeterProperty prop : urlconfig.getArguments()) {
                        Argument arg = (Argument) prop.getObjectValue();
                        paramsModel.addRow(new RowResult(arg.getName(), arg.getValue()));
                    }
                }
            }
            
            // Display cookie in headers table (same location on http protocol)
            String cookie = sampleResult.getCookies();
            if (cookie != null && cookie.length() > 0) {
                headersModel.addRow(new RowResult(
                        JMeterUtils.getParsedLabel("view_results_table_request_http_cookie"), //$NON-NLS-1$
                        sampleResult.getCookies()));
            }

        }
        else {
            // add a message when no http sample
            requestModel.addRow(new RowResult("", //$NON-NLS-1$
                    JMeterUtils.getResString("view_results_table_request_http_nohttp"))); //$NON-NLS-1$
        }
    }

    /**
     * Extract the multipart boundary
     * @param contentType the content type header
     * @return  the boundary string
     */
    private String extractBoundary(String contentType) {
        // Get the boundary string for the multiparts from the content type
        String boundaryString = contentType.substring(contentType.toLowerCase(java.util.Locale.ENGLISH).indexOf("boundary=") + "boundary=".length());
        //TODO check in the RFC if other char can be used as separator
        String[] split = boundaryString.split(";");
        if(split.length > 1) {
            boundaryString = split[0];
        }
        return boundaryString;
    }
    
    /**
     * check if the request is multipart
     * @param headers the http request headers
     * @return true if the request is multipart
     */
    private boolean isMultipart(LinkedHashMap<String, String> headers) {
        String contentType = headers.get(HTTPConstants.HEADER_CONTENT_TYPE);
        return contentType != null && contentType.startsWith(HTTPConstants.MULTIPART_FORM_DATA);
    }

    /**
     * @param query query to parse for param and value pairs
     * @return Map params and values
     */
    //TODO: move to utils class (JMeterUtils?)
    public static Map<String, String[]> getQueryMap(String query) {

        Map<String, String[]> map = new HashMap<>();
        String[] params = query.split(PARAM_CONCATENATE);
        for (String param : params) {
            String[] paramSplit = param.split("=");
            String name = decodeQuery(paramSplit[0]);

            // hack for SOAP request (generally)
            if (name.trim().startsWith("<?")) { // $NON-NLS-1$
                map.put(" ", new String[] {query}); //blank name // $NON-NLS-1$
                return map;
            }
            
            // the post payload is not key=value
            if((param.startsWith("=") && paramSplit.length == 1) || paramSplit.length > 2) {
                map.put(" ", new String[] {query}); //blank name // $NON-NLS-1$
                return map;
            }

            String value = "";
            if(paramSplit.length>1) {
                value = decodeQuery(paramSplit[1]);
            }
            
            String[] known = map.get(name);
            if(known == null) {
                known = new String[] {value};
            }
            else {
                String[] tmp = new String[known.length+1];
                tmp[tmp.length-1] = value;
                System.arraycopy(known, 0, tmp, 0, known.length);
                known = tmp;
            }
            map.put(name, known);
        }
        
        return map;
    }

    /**
     * Decode a query string
     * 
     * @param query
     *            to decode
     * @return the decoded query string, if it can be url-decoded. Otherwise the original
     *            query will be returned.
     */
    public static String decodeQuery(String query) {
        if (query != null && query.length() > 0) {
            try {
                return URLDecoder.decode(query, CHARSET_DECODE); // better  ISO-8859-1 than UTF-8
            } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                log.warn(
                        "Error decoding query, maybe your request parameters should be encoded:"
                                + query, e);
                return query;
            }
        }
        return "";
    }

    @Override
    public JPanel getPanel() {
        return paneParsed;
    }

    /**
     * Create a pane with three tables (request, params, headers)
     * 
     * @return Pane to display request data
     */
    private Component createRequestPane() {
        // Set up the 1st table Result with empty headers
        tableRequest = new JTable(requestModel);
        JMeterUtils.applyHiDPI(tableRequest);
        tableRequest.setToolTipText(JMeterUtils.getResString("textbox_tooltip_cell")); // $NON-NLS-1$
        tableRequest.addMouseListener(new TextBoxDoubleClick(tableRequest));
        
        setFirstColumnPreferredAndMaxWidth(tableRequest);
        RendererUtils.applyRenderers(tableRequest, RENDERERS_REQUEST);

        // Set up the 2nd table 
        tableParams = new JTable(paramsModel);
        JMeterUtils.applyHiDPI(tableParams);
        tableParams.setToolTipText(JMeterUtils.getResString("textbox_tooltip_cell")); // $NON-NLS-1$
        tableParams.addMouseListener(new TextBoxDoubleClick(tableParams));
        TableColumn column = tableParams.getColumnModel().getColumn(0);
        column.setPreferredWidth(160);
        tableParams.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        RendererUtils.applyRenderers(tableParams, RENDERERS_PARAMS);

        // Set up the 3rd table 
        tableHeaders = new JTable(headersModel);
        JMeterUtils.applyHiDPI(tableHeaders);
        tableHeaders.setToolTipText(JMeterUtils.getResString("textbox_tooltip_cell")); // $NON-NLS-1$
        tableHeaders.addMouseListener(new TextBoxDoubleClick(tableHeaders));
        setFirstColumnPreferredAndMaxWidth(tableHeaders);
        tableHeaders.getTableHeader().setDefaultRenderer(
                new HeaderAsPropertyRenderer());
        RendererUtils.applyRenderers(tableHeaders, RENDERERS_HEADERS);

        // Create the split pane
        JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                GuiUtils.makeScrollPane(tableParams),
                GuiUtils.makeScrollPane(tableHeaders));
        topSplit.setOneTouchExpandable(true);
        topSplit.setResizeWeight(0.50); // set split ratio
        topSplit.setBorder(null); // see bug jdk 4131528

        JSplitPane paneParsed = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                GuiUtils.makeScrollPane(tableRequest), topSplit);
        paneParsed.setOneTouchExpandable(true);
        paneParsed.setResizeWeight(0.25); // set split ratio (only 5 lines to display)
        paneParsed.setBorder(null); // see bug jdk 4131528

        // Hint to background color on bottom tabs (grey, not blue)
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(paneParsed);
        return panel;
    }

    private void setFirstColumnPreferredAndMaxWidth(JTable table) {
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMaxWidth(300);
        column.setPreferredWidth(160);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.request.RequestView#getLabel()
     */
    @Override
    public String getLabel() {
        return JMeterUtils.getResString(KEY_LABEL);
    }
    
    /**
     * Search implementation for the http parameter table
     */
    private class RequestViewHttpSearchProvider implements ISearchTextExtensionProvider {

        private int lastPosition = -1;
        
        @Override
        public void resetTextToFind() {
            lastPosition = -1;
            if(tableParams != null) {
                tableParams.clearSelection();
            }
        }

        @Override
        public boolean executeAndShowTextFind(Pattern pattern) {
            boolean found =  false;
            if(tableParams != null) {
                tableParams.clearSelection();
                outerloop:
                for (int i = lastPosition+1; i < tableParams.getRowCount(); i++) {
                    for (int j = 0; j < COLUMNS_PARAMS.length; j++) {
                        Object o = tableParams.getModel().getValueAt(i, j);
                        if(o instanceof String) {
                            Matcher matcher = pattern.matcher((String) o);
                            if (matcher.find()) {
                                found =  true;
                                tableParams.setRowSelectionInterval(i, i);
                                tableParams.scrollRectToVisible(tableParams.getCellRect(i, 0, true));
                                lastPosition = i;
                                break outerloop;
                            }
                        }
                    }
                }
                
                if(!found) {
                    resetTextToFind();
                }
            }
            return found;
        }
        
    }
}
