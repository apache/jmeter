// $Header$
/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.util.JMeterUtils;

/**
 * @author Peter Lin
 * @version $Revision$ Last updated: $Date$
 */
public class ReportFilePanel extends HorizontalPanel implements ActionListener {
	JTextField filename = new JTextField(20);

	JLabel label = new JLabel(JMeterUtils.getResString("file_visualizer_filename"));

	JButton browse = new JButton(JMeterUtils.getResString("browse"));

	List listeners = new LinkedList();

	String title;

	String filetype;

	/**
	 * Constructor for the FilePanel object.
	 */
	public ReportFilePanel() {
		title = "";
		init();
	}

	public ReportFilePanel(String title) {
		this.title = title;
		init();
	}

	public ReportFilePanel(String title, String filetype) {
		this(title);
		this.filetype = filetype;
	}

	/**
	 * Constructor for the FilePanel object.
	 */
	public ReportFilePanel(ChangeListener l, String title) {
		this.title = title;
		init();
		listeners.add(l);
	}

	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	private void init() {
		setBorder(BorderFactory.createTitledBorder(title));
		add(label);
		add(Box.createHorizontalStrut(5));
		add(filename);
		add(Box.createHorizontalStrut(5));
		filename.addActionListener(this);
		add(browse);
		browse.setActionCommand("browse");
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
		Iterator iter = listeners.iterator();
		while (iter.hasNext()) {
			((ChangeListener) iter.next()).stateChanged(new ChangeEvent(this));
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("browse")) {
			JFileChooser chooser = ReportFileDialoger.promptToOpenFile(new String[] { filetype });
			if (chooser != null && chooser.getSelectedFile() != null) {
				filename.setText(chooser.getSelectedFile().getPath());
				fireFileChanged();
			}
		} else {
			fireFileChanged();
		}
	}
}
