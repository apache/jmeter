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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.format.PatternFormatter;

/**
 * Panel that shows log events
 */
public class LoggerPanel extends JPanel implements LogTarget {

    private static final long serialVersionUID = 6911128494402594429L;

    private JSyntaxTextArea textArea;

    private final PatternFormatter format;

    // Limit length of log content
    private static final int LOGGER_PANEL_MAX_LENGTH =
            JMeterUtils.getPropDefault("jmeter.loggerpanel.maxlength", 80000); // $NON-NLS-1$

    /**
     * Pane for display JMeter log file
     */
    public LoggerPanel() {
        init();
        format = new PatternFormatter(LoggingManager.DEFAULT_PATTERN + "\n"); // $NON-NLS-1$
    }

    private void init() {
        this.setLayout(new BorderLayout());

        // TEXTAREA
        textArea = new JSyntaxTextArea(15, 50, true);
        textArea.setEditable(false);
        textArea.setLineWrap(false);
        textArea.setLanguage("text");
        textArea.setMargin(new Insets(2, 2, 2, 2)); // space between borders and text
        JScrollPane areaScrollPane = new JTextScrollPane(textArea);

        areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(areaScrollPane, BorderLayout.CENTER); 
    }

    /* (non-Javadoc)
     * @see org.apache.log.LogTarget#processEvent(org.apache.log.LogEvent)
     */
    @Override
    public void processEvent(final LogEvent logEvent) {
        if(!GuiPackage.getInstance().getMenuItemLoggerPanel().getModel().isSelected()) {
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                synchronized (textArea) {
                    textArea.append(format.format(logEvent));
                    int currentLength = textArea.getText().length();
                    // If LOGGER_PANEL_MAX_LENGTH is 0, it means all log events are kept
                    if(LOGGER_PANEL_MAX_LENGTH != 0 && currentLength> LOGGER_PANEL_MAX_LENGTH) {
                        textArea.setText(textArea.getText().substring(Math.max(0, currentLength-LOGGER_PANEL_MAX_LENGTH), 
                                currentLength));
                    }
                    textArea.setCaretPosition(textArea.getText().length());
                }
            }
        });
    }

    /**
     * Clear panel content
     */
    public void clear() {
        this.textArea.setText(""); // $NON-NLS-1$
    }
}