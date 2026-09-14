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

package org.apache.jmeter.protocol.http.visualizers;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.jmeter.gui.util.JSyntaxSearchToolBar;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.http.curl.CurlCommandFormatter;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.RequestView;

import com.google.auto.service.AutoService;

/**
 * Panel that renders an HTTP request as a ready-to-run {@code curl} command,
 * so it can be copied and pasted into a console or shared with a developer.
 *
 * @see CurlCommandFormatter
 */
@AutoService(RequestView.class)
public class RequestViewCurl implements RequestView {

    // Used by Request Panel
    static final String KEY_LABEL = "view_results_table_request_tab_curl"; //$NON-NLS-1$

    private JSyntaxTextArea curlData;

    private JPanel panel;

    @Override
    public void init() {
        panel = new JPanel(new BorderLayout(0, 5));
        curlData = JSyntaxTextArea.getInstance(20, 80, true);
        curlData.setEditable(false);
        curlData.setLineWrap(true);
        curlData.setWrapStyleWord(true);
        panel.add(new JSyntaxSearchToolBar(curlData).getToolBar(), BorderLayout.NORTH);
        panel.add(JTextScrollPane.getInstance(curlData), BorderLayout.CENTER);
    }

    @Override
    public void clearData() {
        curlData.setInitialText(""); //$NON-NLS-1$
    }

    @Override
    public void setSamplerResult(Object objectResult) {
        if (objectResult instanceof HTTPSampleResult sampleResult) {
            curlData.setInitialText(CurlCommandFormatter.format(sampleResult));
            curlData.setCaretPosition(0);
        } else {
            // add a message when no http sample (ex. Java request)
            curlData.setInitialText(JMeterUtils.getResString("view_results_table_request_http_nohttp")); //$NON-NLS-1$
        }
    }

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public String getLabel() {
        return JMeterUtils.getResString(KEY_LABEL);
    }

}
