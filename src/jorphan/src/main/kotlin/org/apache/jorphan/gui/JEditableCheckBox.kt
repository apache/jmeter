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
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.event.ChangeEvent

/**
 * A Checkbox that can be converted to an editable field and back.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public open class JEditableCheckBox(
    label: @NonNls String,
    private val configuration: Configuration,
    resourceLocalizer: ResourceLocalizer,
) : JPanel() {
    public companion object {
        public const val CHECKBOX_CARD: String = "checkbox"
        public const val EDITABLE_CARD: String = "editable"
        public const val VALUE_PROPERTY: String = "value"
    }

    /**
     * The representation of the state.
     */
    public sealed interface Value {
        public companion object {
            @JvmStatic
            public fun of(value: kotlin.Boolean): Boolean =
                if (value) Boolean.TRUE else Boolean.FALSE
        }

        /**
         * The value is a checkbox.
         */
        public sealed interface Boolean : Value {
            public val value: kotlin.Boolean

            public object TRUE : Boolean {
                override val value: kotlin.Boolean
                    get() = true
            }

            public object FALSE : Boolean {
                override val value: kotlin.Boolean
                    get() = false
            }
        }

        /**
         * The value is a free text.
         */
        public data class Text(val value: String) : Value
    }

    /**
     * Supplies the parameters to [JEditableCheckBox].
     */
    @API(status = API.Status.EXPERIMENTAL, since = "5.6.0")
    public data class Configuration(
        /** Menu item title to "start editing" the checkbox value. */
        val useExpression: LocalizedString,
        /** Tooltip for "start editing" button. */
        val useExpressionTooltip: LocalizedString,
        /** The title to be used for "true" value in the checkbox. */
        val trueValue: LocalizedString,
        /** The title to be used for "false" value in the checkbox. */
        val falseValue: LocalizedString,
        /** Extra values to be added for the combobox. */
        val extraValues: List<ComboBoxValue> = listOf(),
    )

    private val cards = CardLayoutWithSizeOfCurrentVisibleElement()

    private val useExpressionAction = object : AbstractAction(configuration.useExpression.toString()) {
        override fun actionPerformed(e: ActionEvent?) {
            cards.next(this@JEditableCheckBox)
            comboBox.selectedItem = if (checkbox.isSelected) configuration.trueValue else configuration.falseValue
            comboBox.requestFocusInWindow()
        }
    }

    private val checkbox: JCheckBox = JCheckBox(resourceLocalizer.localize(label)).apply {
        val cb = this
        componentPopupMenu = JPopupMenu().apply {
            add(useExpressionAction)
        }
        addItemListener {
            fireValueChanged()
        }
    }

    private val comboBox: JComboBox<ComboBoxValue> = JComboBox<ComboBoxValue>().apply {
        isEditable = true
        configuration.extraValues.forEach {
            addItem(it)
        }
        addItem(configuration.trueValue)
        addItem(configuration.falseValue)
    }

    private val textFieldLabel = JLabel(resourceLocalizer.localize(label)).apply {
        labelFor = comboBox
    }

    private val expressionButton = JEllipsisButton().apply {
        // Tooltip will be set via configuration or use default
        toolTipText = configuration.useExpressionTooltip.toString()
        addActionListener(useExpressionAction)
    }

    @Transient
    private var changeEvent: ChangeEvent? = null

    init {
        layout = cards
        add(
            // A dummy container ensures popup menu appears on top of the checkbox
            Container().apply {
                layout = FlowLayout(FlowLayout.LEADING, 0, 0)
                add(checkbox)
                add(expressionButton)
            },
            CHECKBOX_CARD
        )
        add(
            Container().apply {
                // FlowLayout adds horizontal gap before the first element, so we set zero gaps
                // and add the gap between the components manually.
                layout = FlowLayout(FlowLayout.LEADING, 0, 0)
                add(comboBox)
                add(Box.createHorizontalStrut(5))
                add(textFieldLabel)
            },
            EDITABLE_CARD
        )
    }

    private var oldValue = value

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        checkbox.isEnabled = enabled
        comboBox.isEnabled = enabled
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

    public var value: Value
        get() = when (components.indexOfFirst { it.isVisible }) {
            0 -> if (checkbox.isSelected) Value.Boolean.TRUE else Value.Boolean.FALSE
            else ->
                when (val value = comboBox.selectedItem) {
                    is ResourceKeyed ->
                        when (value.resourceKey) {
                            configuration.trueValue.resourceKey -> Value.Boolean.TRUE
                            configuration.falseValue.resourceKey -> Value.Boolean.FALSE
                            else -> Value.Text(value.resourceKey)
                        }
                    else -> Value.Text(value?.toString() ?: "")
                }
        }
        set(value) {
            when (value) {
                is Value.Boolean -> {
                    checkbox.isSelected = value.value
                    cards.show(this, CHECKBOX_CARD)
                }

                is Value.Text -> {
                    comboBox.selectedItem = PlainValue(value.value)
                    cards.show(this, EDITABLE_CARD)
                }
            }
            fireValueChanged()
        }

    @get:JvmSynthetic
    public var booleanValue: Boolean
        @Deprecated(message = "write-only property", level = DeprecationLevel.HIDDEN)
        get() = TODO()
        set(value) {
            this.value = Value.of(value)
        }

    @get:JvmSynthetic
    public var stringValue: String
        @Deprecated(message = "write-only property", level = DeprecationLevel.HIDDEN)
        get() = TODO()
        set(value) {
            this.value = Value.Text(value)
        }

    public fun makeSmall() {
        JFactory.small(checkbox)
        JFactory.small(expressionButton)
        // We do not make combobox small as the expression migh be hard to read
    }
}
