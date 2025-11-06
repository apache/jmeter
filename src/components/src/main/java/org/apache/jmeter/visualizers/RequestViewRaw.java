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

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.apache.jmeter.gui.util.JSyntaxSearchToolBar;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.util.StringUtilities;

import com.google.auto.service.AutoService;

/**
 * (historical) Panel to view request data
 *
 */
@AutoService(RequestView.class)
public class RequestViewRaw implements RequestView {

    // Used by Request Panel
    static final String KEY_LABEL = "view_results_table_request_tab_raw"; //$NON-NLS-1$

    private JSyntaxTextArea headerData;
    private JSyntaxTextArea sampleDataField;

    private JPanel paneRaw; /** request pane content */

    @Override
    public void init() {
        paneRaw = new JPanel(new BorderLayout(0, 5));

        sampleDataField = JSyntaxTextArea.getInstance(20, 80, true);
        sampleDataField.setEditable(false);
        sampleDataField.setLineWrap(true);
        sampleDataField.setWrapStyleWord(true);
        JPanel requestAndSearchPanel = new JPanel(new BorderLayout());
        requestAndSearchPanel.add(new JSyntaxSearchToolBar(sampleDataField).getToolBar(), BorderLayout.NORTH);
        requestAndSearchPanel.add(JTextScrollPane.getInstance(sampleDataField), BorderLayout.CENTER);

        headerData = JSyntaxTextArea.getInstance(20, 80, true);
        headerData.setEditable(false);
        headerData.setLineWrap(true);
        headerData.setWrapStyleWord(true);
        JPanel headerAndSearchPanel = new JPanel(new BorderLayout());
        headerAndSearchPanel.add(new JSyntaxSearchToolBar(headerData).getToolBar(), BorderLayout.NORTH);
        headerAndSearchPanel.add(JTextScrollPane.getInstance(headerData), BorderLayout.CENTER);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.addTab(JMeterUtils.getResString("view_results_request_body"), new JScrollPane(requestAndSearchPanel));
        tabbedPane.addTab(JMeterUtils.getResString("view_results_request_headers"), new JScrollPane(headerAndSearchPanel));
        paneRaw.add(GuiUtils.makeScrollPane(tabbedPane));

    }

    @Override
    public void clearData() {
        sampleDataField.setInitialText(""); //$NON-NLS-1$
        headerData.setInitialText(""); //$NON-NLS-1$
    }

    @Override
    public void setSamplerResult(Object objectResult) {
        if (objectResult instanceof SampleResult sampleResult) {
            // Don't display Request headers label if rh is null or empty
            String rh = sampleResult.getRequestHeaders();
            if (StringUtilities.isNotEmpty(rh)) {
                headerData.setInitialText(rh);
                sampleDataField.setCaretPosition(0);
            }
            String data = sampleResult.getSamplerData();
            if (StringUtilities.isNotEmpty(data)) {
                sampleDataField.setText(data);
                sampleDataField.setCaretPosition(0);
            } else {
                // add a message when no request data (ex. Java request)
                sampleDataField.setText(JMeterUtils
                        .getResString("view_results_table_request_raw_nodata")); //$NON-NLS-1$
            }
        }
    }

    @Override
    public JPanel getPanel() {
        return paneRaw;
    }

    @Override
    public String getLabel() {
        return JMeterUtils.getResString(KEY_LABEL);
    }

}
