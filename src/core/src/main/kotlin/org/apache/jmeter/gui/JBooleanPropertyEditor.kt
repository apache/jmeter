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
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.gui.JEditableCheckBox
import org.apiguardian.api.API

/**
 * Provides editor component for boolean properties that accommodate both true/false and expression string.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class JBooleanPropertyEditor(
    private val propertyDescriptor: BooleanPropertyDescriptor<*>,
    label: String,
) : JEditableCheckBox(label, DEFAULT_CONFIGURATION) {
    private companion object {
        @JvmField
        val DEFAULT_CONFIGURATION: Configuration = Configuration(
            startEditing = JMeterUtils.getResString("editable_checkbox.use_expression"),
            trueValue = "true",
            falseValue = "false",
            extraValues = listOf(
                "\${__P(property_name)}",
                "\${variable_name}",
            )
        )
    }

    public fun reset() {
        value = Value.of(propertyDescriptor.defaultValue ?: false)
    }

    /**
     * Update [TestElement] based on the state of the UI.
     * TODO: we might better use PropertiesAccessor<TestElement, ElementSchema>
     *     However, it would require callers to pass element.getProps() which might allocate.
     * @param testElement element to update
     */
    public fun updateElement(testElement: TestElement) {
        when (val value = value) {
            is Value.Boolean -> testElement[propertyDescriptor] = value.value
            is Value.Text -> testElement[propertyDescriptor] = value.value
        }
    }

    /**
     * Update UI based on the state of the given [TestElement].
     * TODO: we might better use PropertiesAccessor<TestElement, ElementSchema>
     *     However, it would require callers to pass element.getProps() which might allocate.
     * @param testElement element to get the state from
     */
    public fun updateUi(testElement: TestElement) {
        value = when (val value = testElement.getPropertyOrNull(propertyDescriptor)) {
            is BooleanProperty, null -> Value.of(value?.booleanValue ?: propertyDescriptor.defaultValue ?: false)
            // TODO: should we rather fail in case we detect an unknown property?
            else -> Value.Text(value.stringValue)
        }
    }
}
