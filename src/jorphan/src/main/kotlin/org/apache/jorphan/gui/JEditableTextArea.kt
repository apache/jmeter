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
import javax.swing.JTextArea
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * A multi-line text area with an attached [ModifiedGutter] and an
 * optional "Reset to default" action in its popup menu.
 *
 * Multi-line counterpart of [JEditableTextField]: same gutter wiring,
 * same popup-menu wiring, same backspace-as-reset gesture (triggered
 * only when the entire text area is empty). Like the text field, this
 * class does not have a separate expression mode — free-form text
 * already accepts JMeter expressions.
 *
 * The text area is added directly to the gutter, without a
 * [javax.swing.JScrollPane], which suits short comment-style fields.
 * Callers that need scrolling should wrap the editor's inner
 * [JTextArea] (see [getInnerTextArea]) or the editor itself in a
 * scroll pane externally.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public open class JEditableTextArea(
    private val configuration: Configuration = Configuration(),
) : JPanel(BorderLayout()) {
    public companion object {
        /** Property name fired when the text value changes. */
        @NonNls
        public const val VALUE_PROPERTY: String = "value"
    }

    /**
     * Configuration for [JEditableTextArea].
     *
     * @property resetMode Controls whether a "Reset to default" item is
     *  shown in the component popup menu.
     */
    public data class Configuration(
        val resetMode: ResetMode = ResetMode.Forbid,
    )

    private val textArea: JTextArea = JTextArea()
    private val gutter: ModifiedGutter = ModifiedGutter(textArea)

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
            textArea.componentPopupMenu = JPopupMenu().apply {
                add(resetAction)
            }
        }
        textArea.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = fireValueChanged()
            override fun removeUpdate(e: DocumentEvent?) = fireValueChanged()
            override fun changedUpdate(e: DocumentEvent?) = fireValueChanged()
        })
        textArea.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                // Backspace / Delete on an already-empty modified text area
                // is treated as a quick "reset to default" gesture, mirroring
                // the JEditableTextField behaviour. The trigger condition
                // ("entire text empty") means the user has manually cleared
                // every character before this final keystroke fires reset.
                if (e.keyCode == KeyEvent.VK_BACK_SPACE || e.keyCode == KeyEvent.VK_DELETE) {
                    if (textArea.text.isNullOrEmpty() && isModified) {
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
        get() = textArea.text ?: ""
        set(v) {
            if (textArea.text != v) {
                textArea.text = v
            }
        }

    /**
     * Whether the editor's current value differs from the default
     * ("modified" state). Drives the [ModifiedGutter] strip rendered to
     * the left of the text area.
     */
    public var isModified: Boolean
        get() = gutter.isModified
        set(value) {
            gutter.isModified = value
        }

    /**
     * Reset the editor to its default value. The default implementation
     * is a no-op; subclasses that know about a default value should
     * override this method.
     */
    protected open fun resetToDefault() {
        // Default no-op: the base class does not know what "default" means.
    }

    /**
     * Returns the inner [JTextArea] so that callers can configure
     * presentation knobs (rows, columns, line wrap, font) and attach
     * `labelFor` from a sibling [javax.swing.JLabel] for accessibility.
     */
    public fun getInnerTextArea(): JTextArea = textArea

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        textArea.isEnabled = enabled
        // Forward to the gutter so its strip is painted in the muted
        // disabled colour rather than the accent colour.
        gutter.isEnabled = enabled
        resetAction?.isEnabled = enabled && isModified
    }
}
