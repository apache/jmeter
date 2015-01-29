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
import javax.swing.SwingUtilities;

import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.format.PatternFormatter;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 * Panel that shows log events
 */
public class LoggerPanel extends JPanel implements LogTarget {

    private static final long serialVersionUID = 6911128494402594429L;

    private final JTextArea textArea;

    private final PatternFormatter format;

    // Limit length of log content
    private static final int LOGGER_PANEL_MAX_LENGTH =
            JMeterUtils.getPropDefault("jmeter.loggerpanel.maxlength", 80000); // $NON-NLS-1$
    
    // Make panel handle event even if closed
    private static final boolean LOGGER_PANEL_RECEIVE_WHEN_CLOSED =
            JMeterUtils.getPropDefault("jmeter.loggerpanel.enable_when_closed", true); // $NON-NLS-1$

    /**
     * Pane for display JMeter log file
     */
    public LoggerPanel() {
        textArea = init();
        format = new PatternFormatter(LoggingManager.DEFAULT_PATTERN + "\n"); // $NON-NLS-1$
    }

    private JTextArea init() {
        this.setLayout(new BorderLayout());
        final JScrollPane areaScrollPane;
        final JTextArea jTextArea;

        if (JMeterUtils.getPropDefault("loggerpanel.usejsyntaxtext", true)) {
            // JSyntax Text Area
            JSyntaxTextArea jSyntaxTextArea = new JSyntaxTextArea(15, 80, true);
            jSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
            jSyntaxTextArea.setCodeFoldingEnabled(false);
            jSyntaxTextArea.setAntiAliasingEnabled(false);
            jSyntaxTextArea.setEditable(false);
            jSyntaxTextArea.setLineWrap(false);
            jSyntaxTextArea.setLanguage("text");
            jSyntaxTextArea.setMargin(new Insets(2, 2, 2, 2)); // space between borders and text
            areaScrollPane = new JTextScrollPane(jSyntaxTextArea);
            jTextArea = jSyntaxTextArea;
        } else {
            // Plain text area
            jTextArea =  new JTextArea(15, 80);
            areaScrollPane = new JScrollPane(jTextArea);
        }

        areaScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(areaScrollPane, BorderLayout.CENTER);
        return jTextArea;
    }

    /* (non-Javadoc)
     * @see org.apache.log.LogTarget#processEvent(org.apache.log.LogEvent)
     */
    @Override
    public void processEvent(final LogEvent logEvent) {
        if(!LOGGER_PANEL_RECEIVE_WHEN_CLOSED && !GuiPackage.getInstance().getMenuItemLoggerPanel().getModel().isSelected()) {
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
