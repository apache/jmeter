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
 */
package org.apache.jmeter.testbeans.gui;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyEditorSupport;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;

import org.apache.jmeter.util.JMeterUtils;

/**
 * This class implements a property editor for possibly null String properties
 * that supports custom editing (i.e.: provides a GUI component) based on a
 * combo box.
 * <p>
 * The provided GUI is a combo box with:
 * <ul>
 * <li>An option for "undefined" (corresponding to the null value), unless the
 * <b>noUndefined</b> property is set.
 * <li>An option for each value in the <b>tags</b> property.
 * <li>The possibility to write your own value, unless the <b>noEdit</b>
 * property is set.
 * </ul>
 * 
 * @author <a href="mailto:jsalvata@apache.org">Jordi Salvat i Alabart</a>
 */
class ComboStringEditor extends PropertyEditorSupport implements ItemListener {

	/**
	 * The list of options to be offered by this editor.
	 */
	private String[] tags = new String[0];

	/**
	 * True iif the editor should not accept (nor produce) a null value.
	 */
	private boolean noUndefined = false;

	/**
	 * True iif the editor should not accept (nor produce) any non-null values
	 * different from the provided tags.
	 */
	private boolean noEdit = false;

	/**
	 * The edited property's default value.
	 */
	private String initialEditValue;

	private JComboBox combo;

	private DefaultComboBoxModel model;

	private boolean startingEdit = false;

	/*
	 * True iif we're currently processing an event triggered by the user
	 * selecting the "Edit" option. Used to prevent reverting the combo to
	 * non-editable during processing of secondary events.
	 */

	// Needs to be visible to test cases
	static final Object UNDEFINED = new UniqueObject(JMeterUtils.getResString("property_undefined")); //$NON-NLS-1$

	private static final Object EDIT = new UniqueObject(JMeterUtils.getResString("property_edit")); //$NON-NLS-1$

	ComboStringEditor() {
		// Create the combo box we will use to edit this property:

		model = new DefaultComboBoxModel();
		model.addElement(UNDEFINED);
		model.addElement(EDIT);

		combo = new JComboBox(model);
		combo.addItemListener(this);
		combo.setEditable(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#supportsCustomEditor()
	 */
	public boolean supportsCustomEditor() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#getCustomEditor()
	 */
	public Component getCustomEditor() {
		return combo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#getValue()
	 */
	public Object getValue() {
		return getAsText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#getAsText()
	 */
	public String getAsText() {
		Object value = combo.getSelectedItem();

		if (value == UNDEFINED)
			return null;
		else
			return (String) value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#setValue()
	 */
	public void setValue(Object value) {
		setAsText((String) value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyEditor#setAsText()
	 */
	public void setAsText(String value) {
		combo.setEditable(true);

		if (value == null)
			combo.setSelectedItem(UNDEFINED);
		else
			combo.setSelectedItem(value);

		if (!startingEdit && combo.getSelectedIndex() >= 0) {
			combo.setEditable(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getItem() == EDIT) {
				startingEdit = true;
				startEditing();
				startingEdit = false;
			} else {
				if (!startingEdit && combo.getSelectedIndex() >= 0) {
					combo.setEditable(false);
				}

				firePropertyChange();
			}
		}
	}

	private void startEditing() {
		JTextComponent textField = (JTextComponent) combo.getEditor().getEditorComponent();

		combo.setEditable(true);

		textField.requestFocus();

		String text = initialEditValue;
		if (initialEditValue == null)
			text = ""; // will revert to last valid value if invalid

		combo.setSelectedItem(text);

		int i = text.indexOf("${}");
		if (i != -1)
			textField.setCaretPosition(i + 2);
		else
			textField.selectAll();
	}

	/**
	 * @return
	 */
	public String getInitialEditValue() {
		return initialEditValue;
	}

	/**
	 * @return
	 */
	public boolean getNoEdit() {
		return noEdit;
	}

	/**
	 * @return
	 */
	public boolean getNoUndefined() {
		return noUndefined;
	}

	/**
	 * @return
	 */
	public String[] getTags() {
		return tags;
	}

	/**
	 * @param object
	 */
	public void setInitialEditValue(String object) {
		initialEditValue = object;
	}

	/**
	 * @param b
	 */
	public void setNoEdit(boolean b) {
		if (noEdit == b)
			return;
		noEdit = b;

		if (noEdit)
			model.removeElement(EDIT);
		else
			model.addElement(EDIT);
	}

	/**
	 * @param b
	 */
	public void setNoUndefined(boolean b) {
		if (noUndefined == b)
			return;
		noUndefined = b;

		if (noUndefined)
			model.removeElement(UNDEFINED);
		else
			model.insertElementAt(UNDEFINED, 0);
	}

	/**
	 * @param strings
	 */
	public void setTags(String[] strings) {
		if (Arrays.equals(tags,strings))
			return;

		for (int i = 0; i < tags.length; i++)
			model.removeElement(tags[i]);

		tags = strings == null ? new String[0] : strings;

		int b = noUndefined ? 0 : 1; // base index for tags
		for (int i = 0; i < tags.length; i++)
			model.insertElementAt(tags[i], b + i);
	}

	/**
	 * This is a funny hack: if you use a plain String, entering the text of the
	 * string in the editor will make the combo revert to that option -- which
	 * actually amounts to making that string 'reserved'. I preferred to avoid
	 * this by using a different type having a controlled .toString().
	 */
	private static class UniqueObject {
		private String s;

		UniqueObject(String s) {
			this.s = s;
		}

		public String toString() {
			return s;
		}
	}
}