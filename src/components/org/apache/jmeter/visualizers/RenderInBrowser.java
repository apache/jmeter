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
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * {@link ResultRenderer} implementation that uses JAVAFX WebEngine to render as browser do
 * @since 3.2
 */
public class RenderInBrowser extends SamplerResultTab implements ResultRenderer {

    private JFXPanel jfxPanel;
    private WebEngine engine;
    private final JLabel lblStatus = new JLabel();
    private final JProgressBar progressBar = new JProgressBar();

    private JPanel browserPanel;

    /** {@inheritDoc} */
    @Override
    public void renderResult(SampleResult sampleResult) {
        String response = ViewResultsFullVisualizer
                .getResponseAsString(sampleResult);
        showRenderedResponse(response, sampleResult);
    }

    protected void showRenderedResponse(String response, SampleResult res) {
        if (response == null) {
            results.setText("");
            return;
        }

        int htmlIndex = response.indexOf("<HTML"); // could be <HTML lang=""> //
                                                   // $NON-NLS-1$

        // Look for a case variation
        if (htmlIndex < 0) {
            htmlIndex = response.indexOf("<html"); // ditto // $NON-NLS-1$
        }

        // If we still can't find it, just try using all of the text
        if (htmlIndex < 0) {
            htmlIndex = 0;
        }

        final String html = response.substring(htmlIndex);

        if (browserPanel == null) {
            browserPanel = initComponents(html);
        } 
        browserPanel.setVisible(true);
        resultsScrollPane.setViewportView(browserPanel);
        Platform.runLater(() -> engine.loadContent(html));
    }

    private JPanel initComponents(String htmlContent) {
        // Create it only in AWT Thread
        jfxPanel = new JFXPanel();
        createScene(htmlContent);
        progressBar.setPreferredSize(new Dimension(150, 18));
        progressBar.setStringPainted(true);

        JPanel statusBar = new JPanel(new BorderLayout(5, 0));
        statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        statusBar.add(lblStatus, BorderLayout.CENTER);
        statusBar.add(progressBar, BorderLayout.WEST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(1024, 600));
        panel.add(statusBar, BorderLayout.NORTH);
        panel.add(jfxPanel, BorderLayout.CENTER);
        return panel;
    }

    private void createScene(final String htmlContent) {
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {

                WebView view = new WebView();
                engine = view.getEngine();

                engine.setOnStatusChanged(event -> SwingUtilities.invokeLater(() -> lblStatus.setText(event.getData())));

                engine.getLoadWorker().workDoneProperty()
                        .addListener((ChangeListener<Number>) (observableValue, oldValue, newValue) -> SwingUtilities
                                .invokeLater(() -> progressBar.setValue(newValue.intValue())));

                engine.getLoadWorker().exceptionProperty()
                        .addListener((ObservableValue<? extends Throwable> o,
                                Throwable old, final Throwable value) -> {
                                if (engine.getLoadWorker().getState() == State.FAILED) {
                                    SwingUtilities.invokeLater(() -> JOptionPane
                                            .showMessageDialog(
                                                    resultsScrollPane,
                                                    (value != null) ? engine
                                                            .getLocation()
                                                            + "\n"
                                                            + value.getMessage()
                                                            : engine.getLocation()
                                                                    + "\nUnexpected error.",
                                                    "Loading error...",
                                                    JOptionPane.ERROR_MESSAGE));
                                }
                        });
                jfxPanel.setScene(new Scene(view));
        });
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return JMeterUtils.getResString("view_results_render_browser"); // $NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.visualizers.SamplerResultTab#clearData()
     */
    @Override
    public void clearData() {
        super.clearData();
        if (browserPanel == null) {
            browserPanel = initComponents("");
        }
        Platform.runLater(() -> engine.loadContent(""));
    }
}
