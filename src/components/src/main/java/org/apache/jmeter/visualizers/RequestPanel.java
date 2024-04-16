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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.ServiceLoader;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.LogAndIgnoreServiceLoadExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manipulate all classes which implements request view panel interface
 * and return a super panel with a bottom tab list of this classes
 *
 */
public class RequestPanel {

    private static final Logger log = LoggerFactory.getLogger(RequestPanel.class);

    private final Deque<RequestView> listRequestView;

    private final JPanel panel;

    /**
     * Find and instantiate all classes that extend RequestView
     * and Create Request Panel
     */
    public RequestPanel() {
        listRequestView = new ArrayDeque<>();
        String rawTab = JMeterUtils.getResString(RequestViewRaw.KEY_LABEL); // $NON-NLS-1$
        RequestView rawObject = null;
        for (RequestView requestView : JMeterUtils.loadServicesAndScanJars(
                RequestView.class,
                ServiceLoader.load(RequestView.class),
                Thread.currentThread().getContextClassLoader(),
                new LogAndIgnoreServiceLoadExceptionHandler(log)
        )) {
            if (rawTab.equals(requestView.getLabel())) {
                rawObject = requestView; // use later
            } else {
                listRequestView.add(requestView);
            }
        }
        // place raw tab in first position (first tab)
        if (rawObject != null) {
            listRequestView.addFirst(rawObject);
        }

        // Prepare the Request tabbed pane
        JTabbedPane tabbedRequest = new JTabbedPane(SwingConstants.BOTTOM);
        for (RequestView requestView : listRequestView) {
            requestView.init();
            tabbedRequest.addTab(requestView.getLabel(), requestView.getPanel());
        }

        // Hint to background color on bottom tabs (grey, not blue)
        panel = new JPanel(new BorderLayout());
        panel.add(tabbedRequest);
    }

    /**
     * Clear data in all request view
     */
    public void clearData() {
        for (RequestView requestView : listRequestView) {
            requestView.clearData();
        }
    }

    /**
     * Put SamplerResult in all request view
     *
     * @param samplerResult The {@link SampleResult} to be put in all {@link RequestView}s
     */
    public void setSamplerResult(SampleResult samplerResult) {
        for (RequestView requestView : listRequestView) {
            requestView.setSamplerResult(samplerResult);
        }
    }

    /**
     * @return a tabbed panel for view request
     */
    public JPanel getPanel() {
        return panel;
    }

}
