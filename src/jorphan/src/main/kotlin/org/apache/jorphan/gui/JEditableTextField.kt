/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jorphan.gui

import org.apiguardian.api.API
import org.jetbrains.annotations.NonNls
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.AbstractAction
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * A text field with an attached [ModifiedGutter] and an optional
 * "Reset to default" action in its popup menu.
 *
 * Unlike [JEditableCheckBox] / [JEditableComboBox], this class has no
 * dedicated expression mode: free-form text already accepts JMeter
 * expressions like `${variable}` or `${__P(name)}`, so a separate card
 * for expression editing is unnecessary.
 *
 * Subclasses are expected to drive [isModified] from a property-change
 * listener on [VALUE_PROPERTY] and to override [resetToDefault] so that
 * the popup item works.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public open class JEditableTextField(
    private val configuration: Configuration = Configuration(),
) : JPanel(BorderLayout()) {
    public companion object {
        /** Property name fired when the text value changes. */
        @NonNls
        public const val VALUE_PROPERTY: String = "value"
    }

    /**
     * Configuration for [JEditableTextField].
     *
     * @property resetMode Controls whether a "Reset to default" item is
     *  shown in the component popup menu.
     */
    public data class Configuration(
        val resetMode: ResetMode = ResetMode.Forbid,
    )

    private val textField: JTextField = JTextField()
    private val gutter: ModifiedGutter = ModifiedGutter(textField)

    private val resetAction = when (val mode = configuration.resetMode) {
        is ResetMode.Allow -> object : AbstractAction(mode.label.toString()) {
            init {
                isEnabled = false
            }
            override fun actionPerformed(e: ActionEvent?) {
                resetToDefault()
            }
        }
        ResetMode.Forbid -> null
    }

    private var oldValue: String = ""

    init {
        isOpaque = false
        if (resetAction != null) {
            gutter.addPropertyChangeListener(ModifiedGutter.MODIFIED_PROPERTY) {
                resetAction.isEnabled = it.newValue == true
            }
            textField.componentPopupMenu = JPopupMenu().apply {
                add(resetAction)
            }
        }
        textField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = fireValueChanged()
            override fun removeUpdate(e: DocumentEvent?) = fireValueChanged()
            override fun changedUpdate(e: DocumentEvent?) = fireValueChanged()
        })
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                // Backspace / Delete on an already-empty modified field is
                // treated as a quick "reset to default" gesture: the user
                // cleared the input once (which lit the gutter) and pressing
                // the delete key again is the natural "undo my custom value"
                // action, saving a trip to the popup menu.
                if (e.keyCode == KeyEvent.VK_BACK_SPACE || e.keyCode == KeyEvent.VK_DELETE) {
                    if (textField.text.isNullOrEmpty() && isModified) {
                        e.consume()
                        resetToDefault()
                    }
                }
            }
        })
        add(gutter, BorderLayout.CENTER)
    }

    private fun fireValueChanged() {
        val newValue = value
        if (newValue != oldValue) {
            val old = oldValue
            oldValue = newValue
            firePropertyChange(VALUE_PROPERTY, old, newValue)
        }
    }

    /**
     * The current text value. Setting the same value as the current one is
     * a no-op (no [VALUE_PROPERTY] event is fired).
     */
    public var value: String
        get() = textField.text ?: ""
        set(v) {
            if (textField.text != v) {
                // The DocumentListener installed in init{} will pick up the
                // change and call fireValueChanged() on its own — no need
                // to fire it explicitly here.
                textField.text = v
            }
        }

    /**
     * Whether the editor's current value differs from the default
     * ("modified" state). Drives the [ModifiedGutter] strip rendered to
     * the left of the text field. Subclasses are responsible for updating
     * this flag whenever the value or the default value changes.
     */
    public var isModified: Boolean
        get() = gutter.isModified
        set(value) {
            gutter.isModified = value
        }

    /**
     * Reset the editor to its default value. The default implementation
     * is a no-op; subclasses that know about a default value should
     * override this method. Invoked by the "Reset to default" popup menu
     * item when [Configuration.resetMode] is [ResetMode.Allow].
     */
    protected open fun resetToDefault() {
        // Default no-op: the base class does not know what "default" means.
    }

    /**
     * Returns the inner [JTextField] so that callers can attach
     * `labelFor` from a sibling [javax.swing.JLabel] for accessibility.
     */
    public fun getInnerTextField(): JTextField = textField

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        textField.isEnabled = enabled
        // Forward to the gutter so its strip is painted in the muted
        // disabled colour rather than the accent colour.
        gutter.isEnabled = enabled
        resetAction?.isEnabled = enabled && isModified
    }
}
