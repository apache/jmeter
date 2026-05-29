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
import org.apache.jmeter.testelement.property.BooleanProperty
import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor
import org.apache.jorphan.gui.ExpressionMode
import org.apache.jorphan.gui.JEditableCheckBox
import org.apache.jorphan.gui.ResetMode
import org.apache.jorphan.locale.LocalizedString
import org.apache.jorphan.locale.PlainValue
import org.apache.jorphan.locale.ResourceLocalizer
import org.apiguardian.api.API
import org.jetbrains.annotations.NonNls

/**
 * Provides an editor component for boolean properties that accommodate both true/false and expression string.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class JBooleanPropertyEditor(
    private val propertyDescriptor: BooleanPropertyDescriptor<*>,
    label: @NonNls String,
    resourceLocalizer: ResourceLocalizer,
) : JEditableCheckBox(label, createConfiguration(resourceLocalizer), resourceLocalizer), Binding {
    private companion object {
        private fun createConfiguration(resourceLocalizer: ResourceLocalizer) =
            Configuration(
                expressionMode = ExpressionMode.Allow(
                    useExpression = LocalizedString("edit_as_expression_action", resourceLocalizer),
                    useExpressionTooltip = LocalizedString("edit_as_expression_tooltip", resourceLocalizer)
                ),
                trueValue = LocalizedString("editable_checkbox.true", resourceLocalizer),
                falseValue = LocalizedString("editable_checkbox.false", resourceLocalizer),
                extraValues = listOf(
                    PlainValue("\${__P(property_name)}"),
                    PlainValue("\${variable_name}"),
                ),
                resetMode = ResetMode.Allow(LocalizedString("reset", resourceLocalizer)),
            )
    }

    /**
     * Suppresses automatic [isModified] updates while the editor is being
     * driven programmatically (loading from a [TestElement] or being reset).
     * Without this flag the value-change listener below would lift the
     * "modified" indicator on every internal mutation.
     */
    private var suppressModifiedUpdate: Boolean = false

    init {
        // Any user-driven value change marks the editor as modified — once
        // the user touches the control, the value is considered explicit
        // and stays explicit until reset.
        addPropertyChangeListener(VALUE_PROPERTY) {
            if (!suppressModifiedUpdate) {
                isModified = true
            }
        }
    }

    public fun reset() {
        suppressModifiedUpdate = true
        try {
            value = Value.of(propertyDescriptor.defaultValue ?: false)
            isModified = false
        } finally {
            suppressModifiedUpdate = false
        }
    }

    override fun resetToDefault() {
        reset()
    }

    public override fun updateElement(testElement: TestElement) {
        if (!isModified) {
            // Not explicitly set — drop the property so the element falls
            // back to the descriptor's default. Symmetric with updateUi(),
            // which marks the editor modified iff the property is present.
            testElement.removeProperty(propertyDescriptor)
            return
        }
        when (val value = value) {
            is Value.Boolean -> testElement[propertyDescriptor] = value.value
            is Value.Text -> testElement[propertyDescriptor] = value.value
        }
    }

    public override fun updateUi(testElement: TestElement) {
        suppressModifiedUpdate = true
        try {
            val prop = testElement.getPropertyOrNull(propertyDescriptor)
            value = when (prop) {
                is BooleanProperty, null -> Value.of(prop?.booleanValue ?: propertyDescriptor.defaultValue ?: false)
                // TODO: should we rather fail in case we detect an unknown property?
                else -> Value.Text(prop.stringValue)
            }
            // Modified means "the property is stored explicitly on the element";
            // an absent property means "use the default" and should leave the
            // gutter dark.
            isModified = prop != null
        } finally {
            suppressModifiedUpdate = false
        }
    }
}
