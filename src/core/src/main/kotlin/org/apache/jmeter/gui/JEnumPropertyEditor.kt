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
import org.apache.jmeter.testelement.property.StringProperty
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apache.jorphan.gui.JEditableComboBox
import org.apache.jorphan.locale.LocalizedString
import org.apache.jorphan.locale.LocalizedValue
import org.apache.jorphan.locale.PlainValue
import org.apache.jorphan.locale.ResourceKeyed
import org.apache.jorphan.locale.ResourceLocalizer
import org.apache.jorphan.util.enumValues
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
 * Example usage in a GUI class:
 * ```java
 * private final JEnumPropertyEditor<ResponseProcessingMode> modeEditor;
 *
 * modeEditor = new JEnumPropertyEditor<>(
 *     schema.getResponseProcessingMode(),
 *     "response_mode_label",
 *     ResponseProcessingMode.class,
 *     JMeterUtils::getResString
 * );
 * bindingGroup.add(modeEditor);
 * ```
 *
 * @param E the enum type that implements [ResourceKeyed]
 * @property propertyDescriptor the property descriptor for the enum property
 * @property label the label text to display next to the combo box
 * @property enumClass the class of the enum type
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public class JEnumPropertyEditor<E>(
    private val propertyDescriptor: StringPropertyDescriptor<*>,
    label: @NonNls String,
    enumClass: Class<E>,
    resourceLocalizer: ResourceLocalizer,
) : JEditableComboBox<E>(label, createConfiguration(enumClass, resourceLocalizer), resourceLocalizer), Binding
    where E : Enum<E>, E : ResourceKeyed {

    private companion object {
        @JvmStatic
        private fun <E> createConfiguration(
            enumClass: Class<E>,
            resourceLocalizer: ResourceLocalizer
        ): Configuration<E>
            where E : Enum<E>, E : ResourceKeyed {
            val resourceKeys = enumClass.enumValues.map {
                LocalizedValue(it, resourceLocalizer)
            }

            return Configuration(
                useExpression = LocalizedString("edit_as_expression_action", resourceLocalizer),
                useExpressionTooltip = LocalizedString("edit_as_expression_tooltip", resourceLocalizer),
                values = resourceKeys,
                extraValues = listOf(
                    PlainValue("\${__P(property_name)}"),
                    PlainValue("\${variable_name}"),
                )
            )
        }
    }

    /**
     * Resets the editor to the default value specified in the property descriptor.
     */
    public fun reset() {
        value = PlainValue(propertyDescriptor.defaultValue ?: "")
    }

    /**
     * Updates the test element with the current value from the editor.
     *
     * The value is stored as-is (either a resource key or a custom expression).
     */
    override fun updateElement(testElement: TestElement) {
        val currentValue = value
        if ((currentValue as? PlainValue)?.value.isNullOrBlank() && propertyDescriptor.defaultValue == null) {
            // Remove property if empty and no default
            testElement.removeProperty(propertyDescriptor.name)
        } else {
            testElement[propertyDescriptor] = when (currentValue) {
                is ResourceKeyed -> currentValue.resourceKey
                else -> currentValue.toString()
            }
        }
    }

    /**
     * Updates the editor UI from the test element's property value.
     *
     * Handles both enum resource keys and custom expression strings.
     */
    override fun updateUi(testElement: TestElement) {
        val property = testElement.getPropertyOrNull(propertyDescriptor)
        value = PlainValue(
            when (property) {
                is StringProperty -> property.stringValue
                null -> propertyDescriptor.defaultValue ?: ""
                else -> property.stringValue
            }
        )
    }
}
