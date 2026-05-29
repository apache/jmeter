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

package org.apache.jmeter.gui

import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apache.jorphan.gui.ExpressionMode
import org.apache.jorphan.gui.JEditableComboBox
import org.apache.jorphan.gui.ResetMode
import org.apache.jorphan.gui.UnsetMode
import org.apache.jorphan.locale.ComboBoxValue
import org.apache.jorphan.locale.LocalizedString
import org.apache.jorphan.locale.LocalizedValue
import org.apache.jorphan.locale.PlainValue
import org.apache.jorphan.locale.ResourceKeyed
import org.apache.jorphan.locale.ResourceLocalizer
import org.apiguardian.api.API
import org.jetbrains.annotations.NonNls

/**
 * Property editor for enum values that implements [ResourceKeyed].
 *
 * This editor provides a combo box that displays localized enum values and can also
 * accept custom expressions like `${__P(property)}` for dynamic configuration.
 *
 * The editor:
 * - Displays predefined enum values with localized text
 * - Stores enum resource keys (not enum names) in the test element property
 * - Allows switching to an editable text field for expressions
 * - Automatically detects and handles both enum values and custom expressions
 *
 * Example usage with factory method:
 * ```java
 * private final JEnumPropertyEditor<ResponseProcessingMode> modeEditor;
 *
 * modeEditor = JEnumPropertyEditor.create(
 *     schema.getResponseProcessingMode(),
 *     "response_mode_label",
 *     ResponseProcessingMode.class,
 *     JMeterUtils::getResString,
 *     UnsetMode.ALLOW,
 *     ExpressionMode.ALLOW
 * );
 * bindingGroup.add(modeEditor);
 * ```
 *
 * @param E the enum type that implements [ResourceKeyed]
 * @property propertyDescriptor the property descriptor for the enum property
 * @property configuration the configuration for the enum editor
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public class JEnumPropertyEditor<E>(
    private val propertyDescriptor: StringPropertyDescriptor<*>,
    private val configuration: Configuration<E>,
    label: @NonNls String,
    resourceLocalizer: ResourceLocalizer,
) : JEditableComboBox<E>(label, createConfiguration(configuration, resourceLocalizer), resourceLocalizer), Binding
    where E : Enum<E>, E : ResourceKeyed {

    /**
     * Configuration for [JEnumPropertyEditor].
     *
     * @property values the list of enum values to display
     * @property unsetMode whether to allow clearing/unsetting the value
     * @property expressionMode whether to allow editing expressions instead of selecting from the list
     * @property extraValues additional example values to show (e.g., expression templates)
     */
    public data class Configuration<E>(
        val values: List<E>,
        val unsetMode: UnsetMode,
        val expressionMode: ExpressionMode,
        val extraValues: List<ComboBoxValue> = emptyList(),
    ) where E : Enum<E>, E : ResourceKeyed

    public companion object {
        /**
         * Creates a [JEnumPropertyEditor] with the specified configuration options.
         *
         * This is a convenience factory method for common use cases where you want to
         * create an editor with enum mode flags rather than building a full Configuration object.
         *
         * @param propertyDescriptor the property descriptor for the enum property
         * @param label the label text to display next to the combo box
         * @param enumClass the class of the enum type
         * @param resourceLocalizer the resource localizer for text translation
         * @param unsetMode whether to allow clearing/unsetting the value (default: Allow with localized "Undefined")
         * @param expressionMode whether to allow editing expressions (default: Allow with standard action text)
         * @return a configured [JEnumPropertyEditor]
         */
        @JvmStatic
        @JvmOverloads
        public fun <E> create(
            propertyDescriptor: StringPropertyDescriptor<*>,
            label: @NonNls String,
            enumClass: Class<E>,
            resourceLocalizer: ResourceLocalizer,
            unsetMode: UnsetMode? = null,
            expressionMode: ExpressionMode? = null,
        ): JEnumPropertyEditor<E>
            where E : Enum<E>, E : ResourceKeyed {
            val config = Configuration(
                values = enumClass.enumConstants.toList(),
                unsetMode = unsetMode ?: UnsetMode.Allow(
                    unsetValue = LocalizedString("property_undefined", resourceLocalizer)
                ),
                expressionMode = expressionMode ?: ExpressionMode.Allow(
                    useExpression = LocalizedString("edit_as_expression_action", resourceLocalizer),
                    useExpressionTooltip = LocalizedString("edit_as_expression_tooltip", resourceLocalizer)
                ),
                extraValues = listOf(
                    PlainValue("\${__P(property_name)}"),
                    PlainValue("\${variable_name}"),
                )
            )
            return JEnumPropertyEditor(propertyDescriptor, config, label, resourceLocalizer)
        }

        private fun <E> createConfiguration(
            config: Configuration<E>,
            resourceLocalizer: ResourceLocalizer
        ): JEditableComboBox.Configuration<E>
            where E : Enum<E>, E : ResourceKeyed = Configuration(
            unsetMode = config.unsetMode,
            expressionMode = config.expressionMode,
            values = config.values.map {
                LocalizedValue(it, resourceLocalizer)
            },
            extraValues = config.extraValues,
            resourceLocalizer = resourceLocalizer,
            resetMode = ResetMode.Allow(LocalizedString("reset", resourceLocalizer)),
        )
    }

    /**
     * Suppresses automatic [isModified] updates while the editor is being
     * driven programmatically (loading from a [TestElement] or being reset).
     */
    private var suppressModifiedUpdate: Boolean = false

    init {
        // Any user-driven value change marks the editor as modified — once
        // the user picks something, the value is considered explicit and
        // stays explicit until reset.
        addPropertyChangeListener(VALUE_PROPERTY) {
            if (!suppressModifiedUpdate) {
                isModified = true
            }
        }
    }

    /**
     * Resets the editor to the default value specified in the property descriptor.
     */
    public fun reset() {
        suppressModifiedUpdate = true
        try {
            value = when (configuration.unsetMode) {
                is UnsetMode.Allow -> null
                UnsetMode.Forbid -> PlainValue(propertyDescriptor.defaultValue ?: "")
            }
            isModified = false
        } finally {
            suppressModifiedUpdate = false
        }
    }

    override fun resetToDefault() {
        reset()
    }

    /**
     * Updates the test element with the current value from the editor.
     *
     * The value is stored as-is (either a resource key or a custom expression).
     * If the editor is not modified (the user has not explicitly set a value),
     * the property is removed so the element falls back to the descriptor's
     * default. Symmetric with [updateUi], which marks the editor modified
     * iff the property is present on the element.
     */
    override fun updateElement(testElement: TestElement) {
        if (!isModified) {
            testElement.removeProperty(propertyDescriptor)
            return
        }
        val currentValue = value
        testElement[propertyDescriptor] = when (currentValue) {
            null -> null
            is ResourceKeyed -> currentValue.resourceKey
            else -> currentValue.toString()
        }
    }

    /**
     * Updates the editor UI from the test element's property value.
     *
     * Handles both enum resource keys and custom expression strings.
     */
    override fun updateUi(testElement: TestElement) {
        suppressModifiedUpdate = true
        try {
            val property = testElement.getPropertyOrNull(propertyDescriptor)
            value = when (property) {
                null -> null
                else -> PlainValue(property.stringValue)
            }
            // Modified means "the property is stored explicitly on the element";
            // an absent property means "use the default" and should leave the
            // gutter dark.
            isModified = property != null
        } finally {
            suppressModifiedUpdate = false
        }
    }
}
