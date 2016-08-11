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

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;

/**
 * (historical) Panel to view request data 
 *
 */
public class RequestViewRaw implements RequestView {
    
    // Used by Request Panel
    static final String KEY_LABEL = "view_results_table_request_tab_raw"; //$NON-NLS-1$

    private JTextArea sampleDataField;

    private JPanel paneRaw; /** request pane content */

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.request.RequestView#init()
     */
    @Override
    public void init() {
        paneRaw = new JPanel(new BorderLayout(0, 5));
        sampleDataField = new JTextArea();
        sampleDataField.setEditable(false);
        sampleDataField.setLineWrap(true);
        sampleDataField.setWrapStyleWord(true);

        paneRaw.add(GuiUtils.makeScrollPane(sampleDataField));

    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.request.RequestView#clearData()
     */
    @Override
    public void clearData() {
        sampleDataField.setText(""); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.request.RequestView#setSamplerResult(java.lang.Object)
     */
    @Override
    public void setSamplerResult(Object objectResult) {

        if (objectResult instanceof SampleResult) {
            SampleResult sampleResult = (SampleResult) objectResult;
            String rh = sampleResult.getRequestHeaders();
            StringBuilder sb = new StringBuilder();
            String sd = sampleResult.getSamplerData();
            if (sd != null) {
                sb.append(sd);
                sb.append("\n"); //$NON-NLS-1$
            } 
            // Don't display Request headers label if rh is null or empty
            if (rh != null && rh.length() > 0) {
                sb.append(JMeterUtils.getResString("view_results_request_headers")); //$NON-NLS-1$
                sb.append("\n"); //$NON-NLS-1$
                sb.append(rh);
                sb.append("\n"); //$NON-NLS-1$
            }
            if (sb.length() > 0) {
                sampleDataField.setText(sb.toString());
                sampleDataField.setCaretPosition(1);
            } else {
                // add a message when no request data (ex. Java request)
                sampleDataField.setText(JMeterUtils
                        .getResString("view_results_table_request_raw_nodata")); //$NON-NLS-1$
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.request.RequestView#getPanel()
     */
    @Override
    public JPanel getPanel() {
        return paneRaw;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.request.RequestView#getLabel()
     */
    @Override
    public String getLabel() {
        return JMeterUtils.getResString(KEY_LABEL);
    }

}
