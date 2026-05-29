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

import org.apache.jorphan.locale.ComboBoxValue
import org.apache.jorphan.locale.LocalizedValue
import org.apache.jorphan.locale.PlainValue
import org.apache.jorphan.locale.ResourceKeyed
import org.apache.jorphan.locale.ResourceLocalizer
import org.apiguardian.api.API
import org.jetbrains.annotations.NonNls
import java.awt.BorderLayout
import java.awt.Container
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
import javax.swing.Box
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.event.ChangeEvent

/**
 * A combo box that can display predefined enum values (with localized text) or switch to
 * an editable text field for custom expressions like `${__P(property)}`.
 *
 * This component uses a CardLayout to switch between:
 * - A non-editable combo box showing predefined values with localized display text
 * - An editable combo box allowing custom text input (expressions)
 *
 * The component stores resource keys (non-localized) as values, but displays localized
 * text to the user via a cell renderer.
 *
 * Example usage:
 * ```kotlin
 * val config = JEditableComboBox.Configuration(
 *     startEditing = JMeterUtils.getResString("editable_combobox_use_expression"),
 *     values = listOf("option_key_1", "option_key_2"),
 *     extraValues = listOf("\${__P(my_property)}", "\${variable_name}")
 * )
 * val comboBox = JEditableComboBox("Label:", config)
 * ```
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public open class JEditableComboBox<T : ResourceKeyed>(
    label: @NonNls String,
    private val configuration: Configuration<T>,
    localizer: ResourceLocalizer,
) : JPanel() {
    public companion object {
        public const val COMBO_CARD: String = "combo"
        public const val EDITABLE_CARD: String = "editable"
        public const val VALUE_PROPERTY: String = "value"
    }

    /**
     * Configuration for the editable combo box.
     *
     * @property unsetMode whether to allow clearing/unsetting the value (contains unsetValue if Allow)
     * @property expressionMode whether to allow editing expressions (contains strings if Allow)
     * @property values List of predefined resource keys (stored values)
     * @property extraValues Additional template values to show in editable mode (like expressions)
     * @property resourceLocalizer Resource localizer for translating strings
     *
     * @since 6.0.0
     */
    @API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
    public data class Configuration<T : ResourceKeyed>(
        val unsetMode: UnsetMode,
        val expressionMode: ExpressionMode,
        val values: List<LocalizedValue<T>>,
        val extraValues: List<ComboBoxValue> = listOf(),
        val resourceLocalizer: ResourceLocalizer,
        /** Controls whether a "Reset to default" item is shown in the component popup menu. */
        val resetMode: ResetMode = ResetMode.Forbid,
    )

    private val cards = CardLayoutWithSizeOfCurrentVisibleElement()
    private val cardPanel: JPanel = JPanel(cards).apply { isOpaque = false }
    private val gutter: ModifiedGutter = ModifiedGutter(cardPanel)

    // Extract values from sealed classes for easier access
    private val unsetValue: ComboBoxValue? = when (val mode = configuration.unsetMode) {
        is UnsetMode.Forbid -> null
        is UnsetMode.Allow -> mode.unsetValue
    }

    private val useExpressionAction = when (val expressionMode = configuration.expressionMode) {
        is ExpressionMode.Allow -> object : AbstractAction(expressionMode.useExpression.toString()) {
            init {
                putValue(Action.SHORT_DESCRIPTION, expressionMode.useExpressionTooltip.toString())
            }
            override fun actionPerformed(e: ActionEvent?) {
                editableCombo.selectedItem = nonEditableCombo.selectedItem
                cards.show(cardPanel, EDITABLE_CARD)
                editableCombo.requestFocusInWindow()
            }
        }
        else -> null
    }

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

    private val nonEditableCombo: JComboBox<ComboBoxValue> = JComboBox<ComboBoxValue>().apply {
        isEditable = false
        if (useExpressionAction != null || resetAction != null) {
            componentPopupMenu = JPopupMenu().apply {
                if (resetAction != null) {
                    add(resetAction)
                }
                if (useExpressionAction != null) {
                    add(useExpressionAction)
                }
            }
        }

        if (unsetValue != null) {
            addItem(unsetValue)
        }

        // Add predefined values
        configuration.values.forEach {
            addItem(it)
        }

        // Custom renderer to show unset value in italics
        renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: javax.swing.JList<*>,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): java.awt.Component {
                val component = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
                ) as JLabel
                if (value === unsetValue) {
                    component.font = component.font.deriveFont(java.awt.Font.ITALIC)
                }
                return component
            }
        }

        addActionListener {
            fireValueChanged()
        }
    }

    private val editableCombo: JComboBox<ComboBoxValue> = JComboBox<ComboBoxValue>().apply {
        isEditable = true

        if (unsetValue != null) {
            addItem(unsetValue)
        }
        // Add template expressions first, then predefined values
        configuration.extraValues.forEach {
            addItem(it)
        }
        configuration.values.forEach {
            addItem(it)
        }

        // Custom renderer to show unset value in italics
        renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: javax.swing.JList<*>,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): java.awt.Component {
                val component = super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus
                ) as JLabel
                if (value === unsetValue) {
                    component.font = component.font.deriveFont(java.awt.Font.ITALIC)
                }
                return component
            }
        }

        // Reset must remain reachable while in expression mode.
        if (resetAction != null) {
            componentPopupMenu = JPopupMenu().apply {
                add(resetAction)
            }
        }
    }

    private val comboLabel = JLabel(localizer.localize(label)).apply {
        labelFor = nonEditableCombo
    }

    private val editableLabel = JLabel(localizer.localize(label)).apply {
        labelFor = editableCombo
    }

    @Transient
    private var changeEvent: ChangeEvent? = null

    init {
        layout = BorderLayout()
        isOpaque = false
        if (resetAction != null) {
            // Keep the "Reset to default" menu item enabled only while the
            // editor is in the modified state.
            gutter.addPropertyChangeListener(ModifiedGutter.MODIFIED_PROPERTY) {
                resetAction.isEnabled = it.newValue == true
            }
        }
        cardPanel.add(
            Container().apply {
                layout = FlowLayout(FlowLayout.LEADING, 0, 0)
                add(comboLabel)
                add(Box.createHorizontalStrut(5))
                add(nonEditableCombo)
            },
            COMBO_CARD
        )
        cardPanel.add(
            Container().apply {
                layout = FlowLayout(FlowLayout.LEADING, 0, 0)
                add(editableLabel)
                add(Box.createHorizontalStrut(5))
                add(editableCombo)
            },
            EDITABLE_CARD
        )
        add(gutter, BorderLayout.CENTER)
    }

    private var oldValue = value

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        nonEditableCombo.isEnabled = enabled
        editableCombo.isEnabled = enabled
        useExpressionAction?.isEnabled = enabled
        // Forward to the gutter so its strip is painted in the muted
        // disabled colour rather than the accent colour.
        gutter.isEnabled = enabled
    }

    private fun fireValueChanged() {
        val newValue = value
        if (value != oldValue) {
            firePropertyChange(VALUE_PROPERTY, oldValue, newValue)
            oldValue = newValue
        }
    }

    /**
     * Gets or sets the current value (resource key or custom expression).
     */
    public var value: ComboBoxValue?
        get() = when (cardPanel.components.indexOfFirst { it.isVisible }) {
            0 -> nonEditableCombo.selectedItem as? ComboBoxValue
            else -> when (val value = editableCombo.selectedItem) {
                is ComboBoxValue -> value
                is String -> PlainValue(value)
                else -> null
            }
        }.takeIf { it != unsetValue }
        set(value) {
            // The user might provide a free-text value which coincides with a resourceKey
            // For instance, it might be the case when loading a value from jmx test plan
            val knownValue = when (value) {
                null -> unsetValue
                else -> findKnownValue(value)
            }
            if (knownValue != null) {
                // Predefined value - use non-editable combo
                nonEditableCombo.selectedItem = knownValue
                cards.show(cardPanel, COMBO_CARD)
            } else {
                // Custom expression - use editable combo
                editableCombo.selectedItem = value
                cards.show(cardPanel, EDITABLE_CARD)
            }
            fireValueChanged()
        }

    /**
     * Whether the editor's current value differs from the default ("modified" state).
     * Drives the [ModifiedGutter] strip rendered to the left of the control.
     * Subclasses are responsible for updating this flag whenever the value or
     * the default value changes.
     *
     * @since 6.0.0
     */
    public var isModified: Boolean
        get() = gutter.isModified
        set(value) {
            gutter.isModified = value
        }

    /**
     * Reset the editor to its default value. The default implementation is
     * a no-op; subclasses that know about a default value should override
     * this method. Invoked by the "Reset to default" popup menu item when
     * [Configuration.resetMode] is [ResetMode.Allow].
     *
     * @since 6.0.0
     */
    protected open fun resetToDefault() {
        // Default no-op: the base class does not know what "default" means.
    }

    private fun findKnownValue(value: ComboBoxValue): ComboBoxValue? {
        return when (value) {
            is PlainValue -> {
                // Plain value might match with one of the known resource keys
                configuration.values.find { it.value.resourceKey == value.value }
            }
            else -> {
                configuration.values.find { it == value }
            }
        }
    }

    public fun makeSmall() {
        JFactory.small(comboLabel)
        JFactory.small(editableLabel)
        // Note: JFactory.small() doesn't support JComboBox
    }
}
