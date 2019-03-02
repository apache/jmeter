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
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manipulate all classes which implements request view panel interface
 * and return a super panel with a bottom tab list of this classes
 *
 */
public class RequestPanel {

    private static final Logger log = LoggerFactory.getLogger(RequestPanel.class);

    private final LinkedList<RequestView> listRequestView;

    private final JPanel panel;

    /**
     * Find and instantiate all classes that extend RequestView
     * and Create Request Panel
     */
    public RequestPanel() {
        listRequestView = new LinkedList<>();
        List<String> classesToAdd = Collections.<String> emptyList();
        try {
            classesToAdd = JMeterUtils.findClassesThatExtend(RequestView.class);
        } catch (IOException e1) {
            // ignored
        }
        String rawTab = JMeterUtils.getResString(RequestViewRaw.KEY_LABEL); // $NON-NLS-1$
        Object rawObject = null;
        for (String clazz : classesToAdd) {
            try {
                // Instantiate requestview classes
                final RequestView requestView = (RequestView) Class.forName(clazz).getDeclaredConstructor().newInstance();
                if (rawTab.equals(requestView.getLabel())) {
                    rawObject = requestView; // use later
                } else {
                    listRequestView.add(requestView);
                }
            } catch (Exception e) {
                log.warn("Error in load result render: {}", clazz, e); // $NON-NLS-1$
            }
        }
        // place raw tab in first position (first tab)
        if (rawObject != null) {
            listRequestView.addFirst((RequestView) rawObject);
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
