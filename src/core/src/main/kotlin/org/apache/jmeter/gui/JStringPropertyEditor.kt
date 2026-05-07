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
import org.apache.jorphan.gui.JEditableTextField
import org.apache.jorphan.gui.ResetMode
import org.apache.jorphan.locale.LocalizedString
import org.apache.jorphan.locale.ResourceLocalizer
import org.apiguardian.api.API

/**
 * Property editor for string properties with [JEditableTextField] as the
 * underlying control.
 *
 * The editor binds to a [StringPropertyDescriptor] and follows the same
 * "explicit-set" semantics as [JBooleanPropertyEditor] /
 * [JEnumPropertyEditor]:
 *  * the gutter is dark while the property is absent on the test element,
 *  * any user-driven change lights the gutter and keeps it lit until reset,
 *  * `resetToDefault` clears both the value and the modified flag.
 *
 * Empty input is treated as "use default" — i.e. when the user clears the
 * field, the property is removed from the test element. Storing an
 * explicit empty string is intentionally not supported in this iteration;
 * the rare case can be handled later via a dedicated popup action.
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public open class JStringPropertyEditor(
    private val propertyDescriptor: StringPropertyDescriptor<*>,
    resourceLocalizer: ResourceLocalizer,
) : JEditableTextField(createConfiguration(resourceLocalizer)), Binding {
    private companion object {
        private fun createConfiguration(resourceLocalizer: ResourceLocalizer): Configuration =
            Configuration(
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
        // the user touches the field, the value is considered explicit and
        // stays explicit until reset.
        addPropertyChangeListener(VALUE_PROPERTY) {
            if (!suppressModifiedUpdate) {
                isModified = true
            }
        }
    }

    public fun reset() {
        suppressModifiedUpdate = true
        try {
            value = propertyDescriptor.defaultValue ?: ""
            isModified = false
        } finally {
            suppressModifiedUpdate = false
        }
    }

    override fun resetToDefault() {
        reset()
    }

    override fun updateElement(testElement: TestElement) {
        if (!isModified) {
            // Not explicitly set — drop the property so the element falls
            // back to the descriptor's default. Symmetric with updateUi(),
            // which marks the editor modified iff the property is present.
            // This also makes "explicit empty string" survive a save/reload
            // round-trip: when the user has typed and then cleared the
            // field (gutter still lit), we now persist "" rather than null.
            testElement.removeProperty(propertyDescriptor)
            return
        }
        testElement[propertyDescriptor] = value
    }

    override fun updateUi(testElement: TestElement) {
        suppressModifiedUpdate = true
        try {
            val prop = testElement.getPropertyOrNull(propertyDescriptor)
            value = prop?.stringValue ?: (propertyDescriptor.defaultValue ?: "")
            // Modified means "the property is stored explicitly on the
            // element"; an absent property means "use the default" and
            // should leave the gutter dark.
            isModified = prop != null
        } finally {
            suppressModifiedUpdate = false
        }
    }
}
