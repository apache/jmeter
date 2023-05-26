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
import java.awt.Container
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.event.ChangeEvent

/**
 * A Checkbox that can be converted to an editable field and back.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public open class JEditableCheckBox(
    label: String,
    private val configuration: Configuration
) : JPanel() {
    public companion object {
        public const val CHECKBOX_CARD: String = "checkbox"
        public const val EDITABLE_CARD: String = "editable"
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
    public data class Configuration(
        /** Menu item title to "start editing" the checkbox value. */
        val startEditing: String = "Use Expression",
        /** The title to be used for "true" value in the checkbox. */
        val trueValue: String = "true",
        /** The title to be used for "false" value in the checkbox. */
        val falseValue: String = "false",
        /** Extra values to be added for the combobox. */
        val extraValues: List<String> = listOf(),
    )

    private val cards = CardLayoutWithSizeOfCurrentVisibleElement()

    private val checkbox: JCheckBox = JCheckBox(label).apply {
        componentPopupMenu = JPopupMenu().apply {
            add(configuration.startEditing).apply {
                addActionListener {
                    cards.next(this@JEditableCheckBox)
                    comboBox.requestFocusInWindow()
                }
            }
        }
    }

    private val comboBox: JComboBox<String> = JComboBox<String>().apply {
        isEditable = true
        configuration.extraValues.forEach {
            addItem(it)
        }
        addItem(configuration.trueValue)
        addItem(configuration.falseValue)
        addActionListener {
            val jComboBox = it.source as JComboBox<*>
            SwingUtilities.invokeLater {
                if (jComboBox.isPopupVisible) {
                    return@invokeLater
                }
                when (val value = jComboBox.selectedItem as String) {
                    configuration.trueValue, configuration.falseValue -> {
                        checkbox.isSelected = value == configuration.trueValue
                        cards.show(this@JEditableCheckBox, CHECKBOX_CARD)
                        checkbox.requestFocusInWindow()
                    }
                }
            }
        }
    }

    private val textFieldLabel = JLabel(label).apply {
        labelFor = comboBox
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

    public var value: Value
        get() = when (components.indexOfFirst { it.isVisible }) {
            0 -> if (checkbox.isSelected) Value.Boolean.TRUE else Value.Boolean.FALSE
            else -> Value.Text(comboBox.selectedItem as String)
        }
        set(value) {
            when (value) {
                is Value.Boolean -> {
                    comboBox.selectedItem = ""
                    checkbox.isSelected = value.value
                    cards.show(this, CHECKBOX_CARD)
                }

                is Value.Text -> {
                    checkbox.isSelected = false
                    comboBox.selectedItem = value.value
                    cards.show(this, EDITABLE_CARD)
                }
            }
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
        // We do not make combobox small as the expression migh be hard to read
    }
}
