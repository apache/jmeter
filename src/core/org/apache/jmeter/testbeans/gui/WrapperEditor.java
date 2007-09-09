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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import javax.swing.JOptionPane;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This is an implementation of a full-fledged property editor, providing both
 * object-text transformation and an editor GUI (a custom editor component),
 * from two simpler property editors providing only one of these functionalities
 * each, namely:
 * <dl>
 * <dt>typeEditor
 * <dt>
 * <dd>Provides suitable object-to-string and string-to-object transformation
 * for the property's type. That is: it's a simple editor that only need to
 * support the set/getAsText and set/getValue methods.</dd>
 * <dt>guiEditor</dt>
 * <dd>Provides a suitable GUI for the property, but works on [possibly null]
 * String values. That is: it supportsCustomEditor, but get/setAsText and
 * get/setValue are indentical.</dd>
 * </dl>
 * <p>
 * The resulting editor provides optional support for null values (you can
 * choose whether <b>null</b> is to be a valid property value). It also
 * provides optional support for JMeter 'expressions' (you can choose whether
 * they make valid property values).
 * 
 */
class WrapperEditor extends PropertyEditorSupport implements PropertyChangeListener {
	protected static Logger log = LoggingManager.getLoggerForClass();

	/**
	 * The type's property editor.
	 */
	PropertyEditor typeEditor;

	/**
	 * The gui property editor
	 */
	PropertyEditor guiEditor;

	/**
	 * Whether to allow <b>null</b> as a property value.
	 */
	boolean acceptsNull;

	/**
	 * Whether to allow JMeter 'expressions' as property values.
	 */
	boolean acceptsExpressions;

	/**
	 * Whether to allow any constant values different from the provided tags.
	 */
	boolean acceptsOther;

	/**
	 * Keep track of the last valid value in the editor, so that we can revert
	 * to it if the user enters an invalid value.
	 */
	private String lastValidValue = null;

	/**
	 * Constructor for use when a PropertyEditor is delegating to us.
	 */
	WrapperEditor(Object source, PropertyEditor typeEditor, PropertyEditor guiEditor, boolean acceptsNull,
			boolean acceptsExpressions, boolean acceptsOther, Object defaultValue) {
		super(source);
		initialize(typeEditor, guiEditor, acceptsNull, acceptsExpressions, acceptsOther, defaultValue);
	}

	/**
	 * Constructor for use for regular instantiation and by subclasses.
	 */
	WrapperEditor(PropertyEditor typeEditor, PropertyEditor guiEditor, boolean acceptsNull, boolean acceptsExpressions,
			boolean acceptsOther, Object defaultValue) {
		super();
		initialize(typeEditor, guiEditor, acceptsNull, acceptsExpressions, acceptsOther, defaultValue);
	}

	private void initialize(PropertyEditor _typeEditor, PropertyEditor _guiEditor, boolean _acceptsNull,
			boolean _acceptsExpressions, boolean _acceptsOther, Object defaultValue) {
		this.typeEditor = _typeEditor;
		this.guiEditor = _guiEditor;
		this.acceptsNull = _acceptsNull;
		this.acceptsExpressions = _acceptsExpressions;
		this.acceptsOther = _acceptsOther;

		setValue(defaultValue);
		lastValidValue = getAsText();

		if (_guiEditor instanceof ComboStringEditor) {
			String[] tags = ((ComboStringEditor) _guiEditor).getTags();

			// Provide an initial edit value if necessary -- this is an
			// heuristic that tries to provide the most convenient
			// initial edit value:

			String v;
			if (!_acceptsOther)
				v = "${}";
			else if (isValidValue(""))
				v = "";
			else if (_acceptsExpressions)
				v = "${}";
			else if (tags != null && tags.length > 0)
				v = tags[0];
			else
				v = getAsText();

			((ComboStringEditor) _guiEditor).setInitialEditValue(v);
		}

		_guiEditor.addPropertyChangeListener(this);
	}

	public boolean supportsCustomEditor() {
		return true;
	}

	public Component getCustomEditor() {
		return guiEditor.getCustomEditor();
	}

	public String[] getTags() {
		return guiEditor.getTags();
	}

	/**
	 * Determine wheter a string is one of the known tags.
	 * 
	 * @param text
	 * @return true iif text equals one of the getTags()
	 */
	private boolean isATag(String text) {
		String[] tags = getTags();
		if (tags == null)
			return false;
		for (int i = 0; i < tags.length; i++) {
			if (tags[i].equals(text))
				return true;
		}
		return false;
	}

	/**
	 * Determine whether a string is a valid value for the property.
	 * 
	 * @param text
	 *            the value to be checked
	 * @return true iif text is a valid value
	 */
	private boolean isValidValue(String text) {
		if (text == null)
			return acceptsNull;

		if (acceptsExpressions && isExpression(text))
			return true;

		// Not an expression (isn't or can't be), not null.

		// The known tags are assumed to be valid:
		if (isATag(text))
			return true;

		// Was not a tag, so if we can't accept other values...
		if (!acceptsOther)
			return false;

		// Delegate the final check to the typeEditor:
		try {
			typeEditor.setAsText(text);
		} catch (IllegalArgumentException e1) {
			// setAsText failed: not valid
			return false;
		}
		// setAsText succeeded: valid
		return true;
	}

	/**
	 * This method is used to do some low-cost defensive programming: it is
	 * called when a condition that the program logic should prevent from
	 * happening occurs. I hope this will help early detection of logical bugs
	 * in property value handling.
	 * 
	 * @throws Error
	 *             always throws an error.
	 */
	private final void shouldNeverHappen() throws Error {
		throw new Error(); // Programming error: bail out.
	}

	/**
	 * Same as shouldNeverHappen(), but provide a source exception.
	 * 
	 * @param e
	 *            the exception that helped identify the problem
	 * @throws Error
	 *             always throws one.
	 */
	private final void shouldNeverHappen(Exception e) throws Error {
		throw new Error(e.toString()); // Programming error: bail out.
	}

	/**
	 * Check if a string is a valid JMeter 'expression'.
	 * <p>
	 * The current implementation is very basic: it just accepts any string
	 * containing "${" as a valid expression. TODO: improve, but keep returning
	 * true for "${}".
	 */
	private final boolean isExpression(String text) {
		return text.indexOf("${") != -1;
	}

	/**
	 * Same as isExpression(String).
	 * 
	 * @param text
	 * @return true iif text is a String and isExpression(text).
	 */
	private final boolean isExpression(Object text) {
		return text instanceof String && isExpression((String) text);
	}

	/**
	 * @see java.beans.PropertyEditor#getValue()
	 * @see org.apache.jmeter.testelement.property.JMeterProperty
	 */
	public Object getValue() {
		String text = (String) guiEditor.getValue();

		Object value;

		if (text == null) {
			if (!acceptsNull)
				shouldNeverHappen();
			value = null;
		} else {
			if (acceptsExpressions && isExpression(text)) {
				value = text;
			} else {
				// not an expression (isn't or can't be), not null.

				// a check, just in case:
				if (!acceptsOther && !isATag(text))
					shouldNeverHappen();

				try {
					typeEditor.setAsText(text);
				} catch (IllegalArgumentException e) {
					shouldNeverHappen(e);
				}
				value = typeEditor.getValue();
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("->" + (value != null ? value.getClass().getName() : "NULL") + ":" + value);
		}
		return value;
	}

	public void setValue(Object value) {
		String text;

		if (log.isDebugEnabled()) {
			log.debug("<-" + (value != null ? value.getClass().getName() : "NULL") + ":" + value);
		}

		if (value == null) {
			if (!acceptsNull)
				throw new IllegalArgumentException("Null is not allowed");
			text = null;
		} else if (acceptsExpressions && isExpression(value)) {
			text = (String) value;
		} else {
			// Not an expression (isn't or can't be), not null.
			typeEditor.setValue(value); // may throw IllegalArgumentExc.
			text = typeEditor.getAsText();

			if (!acceptsOther && !isATag(text))
				throw new IllegalArgumentException("Value not allowed: "+text);
		}

		guiEditor.setValue(text);
	}

	public String getAsText() {
		String text = guiEditor.getAsText();

		if (text == null) {
			if (!acceptsNull)
				shouldNeverHappen();
		} else if (!acceptsExpressions || !isExpression(text)) {
			// not an expression (can't be or isn't), not null.
			try {
				typeEditor.setAsText(text);
			} catch (IllegalArgumentException e) {
				shouldNeverHappen(e);
			}
			text = typeEditor.getAsText();

			// a check, just in case:
			if (!acceptsOther && !isATag(text))
				shouldNeverHappen();
		}

		if (log.isDebugEnabled()) {
			log.debug("->\"" + text + "\"");
		}
		return text;
	}

	public void setAsText(String text) throws IllegalArgumentException {
		if (log.isDebugEnabled()) {
			log.debug(text == null ? "<-null" : "<-\"" + text + "\"");
		}

		String value;

		if (text == null) {
			if (!acceptsNull)
				throw new IllegalArgumentException("Null parameter not allowed");
			value = null;
		} else {
			if (acceptsExpressions && isExpression(text)) {
				value = text;
			} else {
				// Some editors do tiny transformations (e.g. "true" to
				// "True",...):
				typeEditor.setAsText(text); // may throw IllegalArgumentException
				value = typeEditor.getAsText();

				if (!acceptsOther && !isATag(text))
					throw new IllegalArgumentException("Value not allowed: "+text);
			}
		}

		guiEditor.setValue(value);
	}

	public void propertyChange(PropertyChangeEvent event) {
		String text = guiEditor.getAsText();
		if (isValidValue(text)) {
			lastValidValue = text;
			firePropertyChange();
		} else {
			// TODO: how to bring the editor back in view & focus?
			JOptionPane.showMessageDialog(guiEditor.getCustomEditor().getParent(), JMeterUtils
					.getResString("property_editor.value_is_invalid_message"), JMeterUtils
					.getResString("property_editor.value_is_invalid_title"), JOptionPane.WARNING_MESSAGE);

			// Revert to the previous value:
			guiEditor.setAsText(lastValidValue);
		}
	}
}