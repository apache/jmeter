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

package org.apache.jmeter.gui.util;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.util.JMeterUtils;

public class DirectoryPanel extends HorizontalPanel implements ActionListener {

    private static final long serialVersionUID = 240L;

    private static final String ACTION_BROWSE = "browse"; // $NON-NSL-1$

    private final JTextField filename = new JTextField(20);

    private final JButton browse = new JButton(JMeterUtils.getResString("browse")); // $NON-NLS-1$

    private final List<ChangeListener> listeners = new LinkedList<ChangeListener>();

    private final String title;
    
    private final Color background;

    /**
     * Constructor for the FilePanel object.
     */
    public DirectoryPanel() {
        this("", null); // $NON-NLS-1$
    }

    public DirectoryPanel(String title) {
        this(title, null);
    }

    public DirectoryPanel(String title, Color bk) {
        this.title = title;
        this.background = bk;
        init();
    }
    /**
     * Constructor for the FilePanel object.
     */
    public DirectoryPanel(ChangeListener l, String title) {
        this(title);
        listeners.add(l);
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    private void init() {
        setBackground(this.background);
        setBorder(BorderFactory.createTitledBorder(title));
        add(Box.createHorizontalStrut(5));
        add(filename);
        add(Box.createHorizontalStrut(5));
        filename.addActionListener(this);
        add(browse);
        browse.setActionCommand(ACTION_BROWSE);
        browse.addActionListener(this);
    }

    /**
     * If the gui needs to enable/disable the FilePanel, call the method.
     *
     * @param enable
     */
    public void enableFile(boolean enable) {
        browse.setEnabled(enable);
        filename.setEnabled(enable);
    }

    /**
     * Gets the filename attribute of the FilePanel object.
     *
     * @return the filename value
     */
    public String getFilename() {
        return filename.getText();
    }

    /**
     * Sets the filename attribute of the FilePanel object.
     *
     * @param f
     *            the new filename value
     */
    public void setFilename(String f) {
        filename.setText(f);
    }

    private void fireFileChanged() {
        Iterator<ChangeListener> iter = listeners.iterator();
        while (iter.hasNext()) {
            iter.next().stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ACTION_BROWSE)) {
            JFileChooser chooser = DirectoryDialoger.promptToOpenFile();
            if (chooser.getSelectedFile() != null) {
                filename.setText(chooser.getSelectedFile().getPath());
                fireFileChanged();
            }
        } else {
            fireFileChanged();
        }
    }
}
