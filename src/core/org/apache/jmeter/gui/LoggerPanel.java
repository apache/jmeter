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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.gui;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;
import org.apache.jmeter.gui.logging.GuiLogEventListener;
import org.apache.jmeter.gui.logging.LogEventObject;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.util.JMeterUtils;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * Panel that shows log events
 */
public class LoggerPanel extends JPanel implements GuiLogEventListener {

    private static final long serialVersionUID = 6911128494402594429L;

    private final JTextArea textArea;

    // Limit length of log content 
    // 0 means unlimited
    private static final int LOGGER_PANEL_MAX_LINES =
            JMeterUtils.getPropDefault("jmeter.loggerpanel.maxlength", 1000); // $NON-NLS-1$

    // Make panel handle event even if closed
    private static final boolean LOGGER_PANEL_RECEIVE_WHEN_CLOSED =
            JMeterUtils.getPropDefault("jmeter.loggerpanel.enable_when_closed", true); // $NON-NLS-1$

    private static final int LOGGER_PANEL_REFRESH_PERIOD =
            JMeterUtils.getPropDefault("jmeter.gui.refresh_period", 500); // $NON-NLS-1$

    private final Buffer events;

    private volatile boolean logChanged = false;

    /**
     * Pane for display JMeter log file
     */
    public LoggerPanel() {
        if (LOGGER_PANEL_MAX_LINES > 0) {
            events = new CircularFifoBuffer(LOGGER_PANEL_MAX_LINES);
        } else {
            events = new UnboundedFifoBuffer();
        }
        textArea = init();
    }

    private JTextArea init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.setLayout(new BorderLayout());
        final JScrollPane areaScrollPane;
        final JTextArea jTextArea;

        if (JMeterUtils.getPropDefault("loggerpanel.usejsyntaxtext", true)) {
            // JSyntax Text Area
            JSyntaxTextArea jSyntaxTextArea = JSyntaxTextArea.getInstance(15, 80, true);
            jSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            jSyntaxTextArea.setCodeFoldingEnabled(false);
            jSyntaxTextArea.setAntiAliasingEnabled(false);
            jSyntaxTextArea.setEditable(false);
            jSyntaxTextArea.setLineWrap(false);
            jSyntaxTextArea.setLanguage("text");
            jSyntaxTextArea.setMargin(new Insets(2, 2, 2, 2)); // space between borders and text
            areaScrollPane = JTextScrollPane.getInstance(jSyntaxTextArea);
            jTextArea = jSyntaxTextArea;
        } else {
            // Plain text area
            jTextArea =  new JTextArea(15, 80);
            areaScrollPane = new JScrollPane(jTextArea);
        }

        areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(areaScrollPane, BorderLayout.CENTER);

        initWorker();

        return jTextArea;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.logging.GuiLogEventListener#processLogEvent(org.apache.jmeter.gui.logging.LogEventObject)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void processLogEvent(final LogEventObject logEventObject) {
        if(!LOGGER_PANEL_RECEIVE_WHEN_CLOSED && !GuiPackage.getInstance().getMenuItemLoggerPanel().getModel().isSelected()) {
            return;
        }

        String logMessage = logEventObject.toString();
        synchronized (events) {
            events.add(logMessage);
        }

        logChanged = true;
    }

    private void initWorker() {
        Timer timer = new Timer(
            LOGGER_PANEL_REFRESH_PERIOD,
            e -> updateLogEntries());
        timer.start();
    }

    private void updateLogEntries() {
        if (!logChanged) {
            return;
        }
        logChanged = false;
        StringBuilder builder = new StringBuilder();
        synchronized (events) {
            for (Object line: events) {
                builder.append((String) line);
            }
        }
        String logText = builder.toString();
        synchronized (textArea) {
            if (LOGGER_PANEL_MAX_LINES > 0) {
                textArea.setText(logText);
            } else {
                textArea.append(logText);
            }
            textArea.setCaretPosition(textArea.getText().length());
        }
    }

    /**
     * Clear panel content
     */
    public void clear() {
        synchronized (events) {
            events.clear();
        }
        logChanged = true;
    }
}
