/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *  
 */

/*
 * Created on May 21, 2004
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyEditorSupport;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author mstover
 * 
 */
public class TextAreaEditor extends PropertyEditorSupport implements FocusListener {
	JTextArea textUI;

	JScrollPane scroller;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
        
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		firePropertyChange();

	}

	protected void init() {
		textUI = new JTextArea();
		textUI.addFocusListener(this);
		textUI.setWrapStyleWord(true);
		textUI.setLineWrap(true);
		scroller = new JScrollPane(textUI, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}

	/**
	 * 
	 */
	public TextAreaEditor() {
		super();
		init();

	}

	/**
	 * @param source
	 */
	public TextAreaEditor(Object source) {
		super(source);
		init();
		setValue(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#getAsText()
	 */
	public String getAsText() {
		return textUI.getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#getCustomEditor()
	 */
	public Component getCustomEditor() {
		return scroller;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#setAsText(java.lang.String)
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		textUI.setText(text);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#setValue(java.lang.Object)
	 */
	public void setValue(Object value) {
		if (value != null) {
			textUI.setText(value.toString());
		} else {
			textUI.setText("");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#getValue()
	 */
	public Object getValue() {
		return textUI.getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#supportsCustomEditor()
	 */
	public boolean supportsCustomEditor() {
		return true;
	}
}
