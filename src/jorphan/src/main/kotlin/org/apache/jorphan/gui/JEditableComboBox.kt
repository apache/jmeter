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
import org.apache.jorphan.locale.LocalizedString
import org.apache.jorphan.locale.LocalizedValue
import org.apache.jorphan.locale.PlainValue
import org.apache.jorphan.locale.ResourceKeyed
import org.apache.jorphan.locale.ResourceLocalizer
import org.apiguardian.api.API
import org.jetbrains.annotations.NonNls
import java.awt.Container
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Box
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
     * @property useExpression Text for the menu item to switch to editable mode
     * @property values List of predefined resource keys (stored values)
     * @property extraValues Additional template values to show in editable mode (like expressions)
     */
    public data class Configuration<T : ResourceKeyed>(
        val useExpression: LocalizedString,
        val useExpressionTooltip: LocalizedString,
        val values: List<LocalizedValue<T>>,
        val extraValues: List<ComboBoxValue> = listOf()
    )

    private val cards = CardLayoutWithSizeOfCurrentVisibleElement()

    private val useExpressionAction = object : AbstractAction(configuration.useExpression.toString()) {
        override fun actionPerformed(e: ActionEvent?) {
            editableCombo.selectedItem = nonEditableCombo.selectedItem
            cards.show(this@JEditableComboBox, EDITABLE_CARD)
            editableCombo.requestFocusInWindow()
        }
    }

    private val nonEditableCombo: JComboBox<ComboBoxValue> = JComboBox<ComboBoxValue>().apply {
        isEditable = false
        componentPopupMenu = JPopupMenu().apply {
            add(useExpressionAction)
        }

        // Add predefined values
        configuration.values.forEach {
            addItem(it)
        }

        addActionListener {
            fireValueChanged()
        }
    }

    private val editableCombo: JComboBox<ComboBoxValue> = JComboBox<ComboBoxValue>().apply {
        isEditable = true

        // Add template expressions first, then predefined values
        configuration.extraValues.forEach {
            addItem(it)
        }
        configuration.values.forEach {
            addItem(it)
        }
    }

    private val comboLabel = JLabel(localizer.localize(label)).apply {
        labelFor = nonEditableCombo
    }

    private val editableLabel = JLabel(localizer.localize(label)).apply {
        labelFor = editableCombo
    }

    private val expressionButton = JEllipsisButton().apply {
        toolTipText = configuration.useExpressionTooltip.toString()
        addActionListener(useExpressionAction)
    }

    @Transient
    private var changeEvent: ChangeEvent? = null

    init {
        layout = cards
        add(
            Container().apply {
                layout = FlowLayout(FlowLayout.LEADING, 0, 0)
                add(comboLabel)
                add(Box.createHorizontalStrut(5))
                add(nonEditableCombo)
                add(Box.createHorizontalStrut(3))
                add(expressionButton)
            },
            COMBO_CARD
        )
        add(
            Container().apply {
                layout = FlowLayout(FlowLayout.LEADING, 0, 0)
                add(editableLabel)
                add(Box.createHorizontalStrut(5))
                add(editableCombo)
            },
            EDITABLE_CARD
        )
    }

    private var oldValue = value

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        nonEditableCombo.isEnabled = enabled
        editableCombo.isEnabled = enabled
        expressionButton.isEnabled = enabled
        useExpressionAction.isEnabled = enabled
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
        get() = when (components.indexOfFirst { it.isVisible }) {
            0 -> nonEditableCombo.selectedItem as? ComboBoxValue
            else -> when (val value = editableCombo.selectedItem) {
                is ComboBoxValue -> value
                is String -> PlainValue(value)
                else -> null
            }
        }
        set(value) {
            // The user might provide a free-text value which coincides with a resourceKey
            // For instance, it might be the case when loading a value from jmx test plan
            val knownValue = value?.let { findKnownValue(it) }
            if (knownValue != null) {
                // Predefined value - use non-editable combo
                nonEditableCombo.selectedItem = knownValue
                cards.show(this, COMBO_CARD)
            } else {
                // Custom expression - use editable combo
                editableCombo.selectedItem = value
                cards.show(this, EDITABLE_CARD)
            }
            fireValueChanged()
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
