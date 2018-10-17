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
import java.util.Arrays;

import javax.swing.JOptionPane;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of a full-fledged property editor, providing both
 * object-text transformation and an editor GUI (a custom editor component),
 * from two simpler property editors providing only one of these functions
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
 * get/setValue are identical.</dd>
 * </dl>
 * <p>
 * The resulting editor provides optional support for null values (you can
 * choose whether <strong>null</strong> is to be a valid property value). It also
 * provides optional support for JMeter 'expressions' (you can choose whether
 * they make valid property values).
 *
 */
class WrapperEditor extends PropertyEditorSupport implements PropertyChangeListener {
    private static final Logger log = LoggerFactory.getLogger(WrapperEditor.class);

    /** The type's property editor. */
    private final PropertyEditor typeEditor;

    /** The gui property editor */
    private final PropertyEditor guiEditor;

    /** Whether to allow <b>null</b> as a property value. */
    private final boolean acceptsNull;

    /** Whether to allow JMeter 'expressions' as property values. */
    private final boolean acceptsExpressions;

    /** Whether to allow any constant values different from the provided tags. */
    private final boolean acceptsOther;

    /** Default value to be used to (re-)initialise the field */
    private final Object defaultValue;

    /**
     * Keep track of the last valid value in the editor, so that we can revert
     * to it if the user enters an invalid value.
     */
    private String lastValidValue = null;

    /**
     * Constructor for use when a PropertyEditor is delegating to us.
     */
    WrapperEditor(
            Object source, PropertyEditor typeEditor, PropertyEditor guiEditor,
            boolean acceptsNull, boolean acceptsExpressions,
            boolean acceptsOther, Object defaultValue) {
        super();
        if (source != null) {
            super.setSource(source);
        }
        this.typeEditor = typeEditor;
        this.guiEditor = guiEditor;
        this.acceptsNull = acceptsNull;
        this.acceptsExpressions = acceptsExpressions;
        this.acceptsOther = acceptsOther;
        this.defaultValue = defaultValue;
        initialize();
    }

    /**
     * Constructor for use for regular instantiation and by subclasses.
     */
    WrapperEditor(
            PropertyEditor typeEditor, PropertyEditor guiEditor,
            boolean acceptsNull, boolean acceptsExpressions,
            boolean acceptsOther, Object defaultValue) {
        this(null, typeEditor, guiEditor, acceptsNull, acceptsExpressions,  acceptsOther, defaultValue);
    }

    final void resetValue() {
        setValue(defaultValue);
        lastValidValue = getAsText();        
    }

    private void initialize() {

        resetValue();

        if (guiEditor instanceof ComboStringEditor) {
            String[] tags = guiEditor.getTags();

            // Provide an initial edit value if necessary -- this is a heuristic
            // that tries to provide the most convenient initial edit value:

            String v;
            if (!acceptsOther) {
                v = "${}"; //$NON-NLS-1$
            } else if (isValidValue("")) { //$NON-NLS-1$
                v = ""; //$NON-NLS-1$
            } else if (acceptsExpressions) {
                v = "${}"; //$NON-NLS-1$
            } else if (tags != null && tags.length > 0) {
                v = tags[0];
            } else {
                v = getAsText();
            }

            ((ComboStringEditor) guiEditor).setInitialEditValue(v);
        }

        guiEditor.addPropertyChangeListener(this);
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        return guiEditor.getCustomEditor();
    }

    @Override
    public String[] getTags() {
        return guiEditor.getTags();
    }

    /**
     * Determine whether a string is one of the known tags.
     *
     * @param text the value to be checked
     * @return true if text equals one of the getTags()
     */
    private boolean isATag(String text) {
        String[] tags = getTags();
        if (tags == null) {
            return false;
        }
        for (String tag : tags) {
            if (tag.equals(text)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether a string is a valid value for the property.
     *
     * @param text
     *            the value to be checked
     * @return true if text is a valid value
     */
    private boolean isValidValue(String text) {
        if (text == null) {
            return acceptsNull;
        }

        if (acceptsExpressions && isExpression(text)) {
            return true;
        }

        // Not an expression (isn't or can't be), not null.

        // The known tags are assumed to be valid:
        if (isATag(text)) {
            return true;
        }

        // Was not a tag, so if we can't accept other values...
        if (!acceptsOther) {
            return false;
        }

        // Delegate the final check to the typeEditor:
        try {
            typeEditor.setAsText(text);
        } catch (IllegalArgumentException e1) {
            return false; // setAsText failed: not valid
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
    private void shouldNeverHappen(String msg) {
        throw new Error(msg); // Programming error: bail out.
    }

    /**
     * Same as shouldNeverHappen(), but provide a source exception.
     *
     * @param e
     *            the exception that helped identify the problem
     * @throws Error
     *             always throws one.
     */
    private void shouldNeverHappen(Exception e) {
        throw new Error(e.toString()); // Programming error: bail out.
    }

    /**
     * Check if a string is a valid JMeter 'expression'.
     * <p>
     * The current implementation is very basic: it just accepts any string
     * containing "${" as a valid expression.
     * TODO: improve, but keep returning true for "${}".
     */
    private boolean isExpression(String text) {
        return text.contains("${");//$NON-NLS-1$
    }

    /**
     * Same as isExpression(String).
     *
     * @param text
     * @return true if text is a String and isExpression(text).
     */
    private boolean isExpression(Object text) {
        return text instanceof String && isExpression((String) text);
    }

    /**
     * @see java.beans.PropertyEditor#getValue()
     * @see org.apache.jmeter.testelement.property.JMeterProperty
     */
    @Override
    public Object getValue() {
        String text = (String) guiEditor.getValue();

        Object value;

        if (text == null) {
            if (!acceptsNull) {
                shouldNeverHappen("Text is null but null is not allowed");
            }
            value = null;
        } else {
            if (acceptsExpressions && isExpression(text)) {
                value = text;
            } else {
                // not an expression (isn't or can't be), not null.

                // a check, just in case:
                if (!acceptsOther && !isATag(text)) {
                    shouldNeverHappen("Text is not a tag but other entries are not allowed");
                }

                try {
                    // Bug 44314  Number field does not seem to accept ""
                    try {
                        typeEditor.setAsText(text);
                    } catch (NumberFormatException e) {
                        if (text.length() == 0){
                            text = "0";//$NON-NLS-1$
                            typeEditor.setAsText(text);
                        } else {
                            shouldNeverHappen(e);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    shouldNeverHappen(e);
                }
                value = typeEditor.getValue();
            }
        }

        if (log.isDebugEnabled()) {
            if (value == null) {
                log.debug("->NULL:null");
            } else {
                log.debug("->{}:{}", value.getClass().getName(), value);
            }
        }
        return value;
    }

    @Override
    public final void setValue(Object value) { /// final because called from ctor
        String text;

        if (log.isDebugEnabled()) {
            if (value == null) {
                log.debug("<-NULL:null");
            } else {
                log.debug("<-{}:{}", value.getClass().getName(), value);
            }
        }

        if (value == null) {
            if (!acceptsNull) {
                throw new IllegalArgumentException("Null is not allowed");
            }
            text = null;
        } else if (acceptsExpressions && isExpression(value)) {
            text = (String) value;
        } else {
            // Not an expression (isn't or can't be), not null.
            typeEditor.setValue(value); // may throw IllegalArgumentExc.
            text = fixGetAsTextBug(typeEditor.getAsText());

            if (!acceptsOther && !isATag(text)) {
                throw new IllegalArgumentException("Value not allowed: '" + text + "' is not in " + Arrays.toString(getTags()));
            }
        }

        guiEditor.setValue(text);
    }

    /*
     * Fix bug in JVMs that return true/false rather than True/False
     * from the type editor getAsText() method
     */
    private String fixGetAsTextBug(String asText) {
        if (asText == null){
            return null;
        }
        if (asText.equals("true")){
            log.debug("true=>True");// so we can detect it
            return "True";
        }
        if (asText.equals("false")){
            log.debug("false=>False");// so we can detect it
            return "False";
        }
        return asText;
    }

    @Override
    public String getAsText() {
        String text = fixGetAsTextBug(guiEditor.getAsText());

        if (text == null) {
            if (!acceptsNull) {
                shouldNeverHappen("Text is null, but null is not allowed");
            }
        } else if (!acceptsExpressions || !isExpression(text)) {
            // not an expression (can't be or isn't), not null.
            try {
                typeEditor.setAsText(text); // ensure value is propagated to editor
            } catch (IllegalArgumentException e) {
                shouldNeverHappen(e);
            }
            text = fixGetAsTextBug(typeEditor.getAsText());

            // a check, just in case:
            if (!acceptsOther && !isATag(text)) {
                shouldNeverHappen("Text is not a tag, but other values are not allowed");
            }
        }

        log.debug("->\"{}\"", text);
        return text;
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (log.isDebugEnabled()) {
            if (text == null) {
                log.debug("<-null");
            } else {
                log.debug("<-\"{}\"", text);
            }
        }

        String value;

        if (text == null) {
            if (!acceptsNull) {
                throw new IllegalArgumentException("Null parameter not allowed");
            }
            value = null;
        } else {
            if (acceptsExpressions && isExpression(text)) {
                value = text;
            } else {
                // Some editors do tiny transformations (e.g. "true" to
                // "True",...):
                typeEditor.setAsText(text); // may throw IllegalArgumentException
                value = typeEditor.getAsText();

                if (!acceptsOther && !isATag(text)) {
                    throw new IllegalArgumentException("Value not allowed: "+text);
                }
            }
        }

        guiEditor.setValue(value);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String text = fixGetAsTextBug(guiEditor.getAsText());
        if (isValidValue(text)) {
            lastValidValue = text;
            firePropertyChange();
        } else {
            if (GuiPackage.getInstance() == null){
                log.warn("Invalid value: {} {}", text, typeEditor);
            } else {
                JOptionPane.showMessageDialog(
                        guiEditor.getCustomEditor().getParent(),
                        JMeterUtils.getResString("property_editor.value_is_invalid_message"),//$NON-NLS-1$
                        JMeterUtils.getResString("property_editor.value_is_invalid_title"),  //$NON-NLS-1$
                        JOptionPane.WARNING_MESSAGE);
            }
            // Revert to the previous value:
            guiEditor.setAsText(lastValidValue);
        }
    }

    public void addChangeListener(PropertyChangeListener listener) {
        guiEditor.addPropertyChangeListener(listener);
    }
}
