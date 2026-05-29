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
import java.awt.BorderLayout
import java.awt.Container
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action
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
        /** Controls whether custom expressions can be entered (contains useExpression and useExpressionTooltip if Allow). */
        val expressionMode: ExpressionMode,
        /** The title to be used for "true" value in the checkbox. */
        val trueValue: LocalizedString,
        /** The title to be used for "false" value in the checkbox. */
        val falseValue: LocalizedString,
        /** Extra values to be added for the combobox. */
        val extraValues: List<ComboBoxValue> = listOf(),
        /** Controls whether a "Reset to default" item is shown in the component popup menu. */
        val resetMode: ResetMode = ResetMode.Forbid,
    )

    private val cards = CardLayoutWithSizeOfCurrentVisibleElement()
    private val cardPanel: JPanel = JPanel(cards).apply { isOpaque = false }
    private val gutter: ModifiedGutter = ModifiedGutter(cardPanel)

    private val useExpressionAction = when (val mode = configuration.expressionMode) {
        is ExpressionMode.Allow -> object : AbstractAction(mode.useExpression.toString()) {
            init {
                putValue(Action.SHORT_DESCRIPTION, mode.useExpressionTooltip.toString())
            }
            override fun actionPerformed(e: ActionEvent?) {
                cards.next(cardPanel)
                comboBox.selectedItem = if (checkbox.isSelected) configuration.trueValue else configuration.falseValue
                comboBox.requestFocusInWindow()
            }
        }
        ExpressionMode.Forbid -> null
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

    private val checkbox: JCheckBox = JCheckBox(resourceLocalizer.localize(label)).apply {
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
        // Reset must remain reachable while in expression mode.
        if (resetAction != null) {
            componentPopupMenu = JPopupMenu().apply {
                add(resetAction)
            }
        }
    }

    private val textFieldLabel = JLabel(resourceLocalizer.localize(label)).apply {
        labelFor = comboBox
    }

    @Transient
    private var changeEvent: ChangeEvent? = null

    init {
        layout = BorderLayout()
        isOpaque = false
        if (resetAction != null) {
            // Keep the "Reset to default" menu item enabled only while the
            // editor is in the modified state. We listen to the gutter
            // because it is the canonical source of the modified flag.
            gutter.addPropertyChangeListener(ModifiedGutter.MODIFIED_PROPERTY) {
                resetAction.isEnabled = it.newValue == true
            }
        }
        cardPanel.add(
            // A dummy container ensures popup menu appears on top of the checkbox
            Container().apply {
                layout = FlowLayout(FlowLayout.LEADING, 0, 0)
                add(checkbox)
            },
            CHECKBOX_CARD
        )
        cardPanel.add(
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
        add(gutter, BorderLayout.CENTER)
    }

    private var oldValue = value

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        checkbox.isEnabled = enabled
        comboBox.isEnabled = enabled
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

    public var value: Value
        get() = when (cardPanel.components.indexOfFirst { it.isVisible }) {
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
                    cards.show(cardPanel, CHECKBOX_CARD)
                }

                is Value.Text -> {
                    comboBox.selectedItem = PlainValue(value.value)
                    cards.show(cardPanel, EDITABLE_CARD)
                }
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
