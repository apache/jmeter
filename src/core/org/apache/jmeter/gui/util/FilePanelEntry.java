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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.util.JMeterUtils;

public class FilePanelEntry extends HorizontalPanel implements ActionListener {
    private static final long serialVersionUID = 280L;

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font"); //$NON-NLS-1$

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8)); //$NON-NLS-1$

    private final JTextField filename = new JTextField(10);

    private final JLabel label;

    private final JButton browse = new JButton(JMeterUtils.getResString("browse")); //$NON-NLS-1$

    private static final String ACTION_BROWSE = "browse"; //$NON-NLS-1$

    private final List<ChangeListener> listeners = new LinkedList<>();

    private final String[] filetypes;
    
    private boolean onlyDirectories = false;

    // Mainly needed for unit test Serialisable tests
    public FilePanelEntry() {
        this(JMeterUtils.getResString("file_visualizer_filename")); //$NON-NLS-1$
    }

    public FilePanelEntry(String label) {
        this(label, (ChangeListener) null);
    }

    public FilePanelEntry(String label, String ... exts) {
        this(label, (ChangeListener) null, exts);
    }

    public FilePanelEntry(String label, boolean onlyDirectories, String ... exts) {
        this(label, onlyDirectories, (ChangeListener) null, exts);
    }

    public FilePanelEntry(String label, ChangeListener listener, String ... exts) {
        this(label, false, listener, exts);
    }
    
    public FilePanelEntry(String label, boolean onlyDirectories, ChangeListener listener, String ... exts) {
        this.label = new JLabel(label);
        if (listener != null) {
            listeners.add(listener);
        }
        if (exts != null && 
          !(exts.length == 1 && exts[0] == null) // String null is converted to String[]{null} NOSONAR it's not code
            ) {
            this.filetypes = new String[exts.length];
            System.arraycopy(exts, 0, this.filetypes, 0, exts.length);
        } else {
            this.filetypes = null;
        }
        this.onlyDirectories=onlyDirectories;
        init();
    }

    public final void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        add(label);
        add(filename);
        filename.addActionListener(this);
        browse.setFont(FONT_SMALL);
        add(browse);
        browse.setActionCommand(ACTION_BROWSE);
        browse.addActionListener(this);

    }

    public void clearGui(){
        filename.setText(""); // $NON-NLS-1$
    }

    /**
     * If the gui needs to enable/disable the FilePanel, call the method.
     *
     * @param enable The Flag whether the {@link FilePanel} should be enabled
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
        for (ChangeListener cl : listeners) {
            cl.stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ACTION_BROWSE)) {
            JFileChooser chooser;
            if(filetypes == null || filetypes.length == 0){
                chooser = FileDialoger.promptToOpenFile(filename.getText(),onlyDirectories);
            } else {
                chooser = FileDialoger.promptToOpenFile(filetypes, filename.getText(),onlyDirectories);
            }
            if (chooser != null && chooser.getSelectedFile() != null) {
                filename.setText(chooser.getSelectedFile().getPath());
                fireFileChanged();
            }
        } else {
            fireFileChanged();
        }
    }
}
