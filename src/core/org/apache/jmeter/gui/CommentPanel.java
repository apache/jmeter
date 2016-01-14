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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.jmeter.util.JMeterUtils;

/**
 * Generic comment panel for Test Elements
 *
 */
public class CommentPanel extends JPanel {
    private static final long serialVersionUID = 240L;

    /** A text field containing the comment. */
    private JTextArea commentField;

    /**
     * Create a new NamePanel with the default name.
     */
    public CommentPanel() {
        init();
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(5, 0));

        commentField = new JTextArea();
        JLabel commentLabel = new JLabel(JMeterUtils.getResString("testplan_comments")); //$NON-NLS-1$
        commentLabel.setLabelFor(commentField);

        JPanel commentPanel = new JPanel();
        commentPanel.setLayout(new BorderLayout(0, 5));
        commentPanel.add(commentLabel,BorderLayout.WEST);
        commentPanel.add(commentField,BorderLayout.CENTER);
        add(commentPanel);
    }

    public void setText(String comment) {
        this.commentField.setText(comment);
    }

    public String getText() {
        return this.commentField.getText();
    }

    public void clearGui() {
        commentField.setText(""); // $NON-NLS-1$
    }
}
