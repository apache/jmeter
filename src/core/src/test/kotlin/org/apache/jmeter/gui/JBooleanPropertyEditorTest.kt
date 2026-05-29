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

import org.apache.jmeter.testelement.AbstractTestElement
import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor
import org.apache.jorphan.gui.JEditableCheckBox
import org.apache.jorphan.locale.ResourceLocalizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Round-trip tests for [JBooleanPropertyEditor]: the gutter (modified
 * indicator) and the underlying test element must stay in sync, in
 * particular the editor must:
 *
 *  - persist `false` as an explicit value when default is `false`
 *    (the previous "treat false as absent" workaround was a bug
 *    visible to users as a lost gutter on save/reload);
 *  - keep "absent" absent when the user did not touch the field;
 *  - restore the modified flag from the property's presence on load.
 */
class JBooleanPropertyEditorTest {
    private val identityLocalizer = ResourceLocalizer { it }

    private class TestElementImpl : AbstractTestElement()

    private fun newElement(): AbstractTestElement = TestElementImpl()

    private fun newEditor(default: Boolean? = false): JBooleanPropertyEditor {
        val descriptor = BooleanPropertyDescriptor<TestElementSchema>(
            shortName = "test",
            name = "test.bool",
            defaultValue = default,
        )
        return JBooleanPropertyEditor(descriptor, "test_label", identityLocalizer)
    }

    @Test
    fun `untouched editor leaves the element clean`() {
        val element = newElement()
        val editor = newEditor(default = false)
        editor.updateUi(element)
        // No user interaction → not modified → property stays absent.
        editor.updateElement(element)

        assertFalse(editor.isModified, "freshly loaded editor must not be modified")
        assertNull(element.getPropertyOrNull("test.bool"), "untouched value must not be persisted")
    }

    @Test
    fun `explicit true round-trips`() {
        val element = newElement()
        val editor = newEditor(default = false)
        editor.updateUi(element)

        editor.value = JEditableCheckBox.Value.Boolean.TRUE
        editor.updateElement(element)

        val reloaded = newEditor(default = false)
        reloaded.updateUi(element)

        assertTrue(reloaded.isModified)
        assertEquals(JEditableCheckBox.Value.Boolean.TRUE, reloaded.value)
    }

    @Test
    fun `explicit false round-trips even when default is false`() {
        // Regression guard: the old `takeIf { it || defaultValue == true }`
        // would drop an explicit `false` value when the descriptor's
        // default was `false`, so the gutter went dark after save/reload
        // even though the user had explicitly toggled the checkbox.
        val element = newElement()
        val editor = newEditor(default = false)
        editor.updateUi(element)

        // Switch to TRUE, then back to FALSE so the property-change listener
        // fires on a real transition and isModified flips to true.
        editor.value = JEditableCheckBox.Value.Boolean.TRUE
        editor.value = JEditableCheckBox.Value.Boolean.FALSE
        assertTrue(editor.isModified, "explicit FALSE must be marked modified before save")

        editor.updateElement(element)

        val stored = element.getPropertyOrNull("test.bool")
        assertNotNull(stored, "explicit FALSE must be persisted, not dropped")
        assertEquals(false, stored!!.booleanValue)

        val reloaded = newEditor(default = false)
        reloaded.updateUi(element)
        assertTrue(reloaded.isModified, "reload must preserve the modified state")
        assertEquals(JEditableCheckBox.Value.Boolean.FALSE, reloaded.value)
    }

    @Test
    fun `explicit false round-trips when default is true`() {
        // The complementary case: default=true, the user unchecks the box.
        val element = newElement()
        val editor = newEditor(default = true)
        editor.updateUi(element) // editor shows TRUE, not modified

        editor.value = JEditableCheckBox.Value.Boolean.FALSE
        assertTrue(editor.isModified)
        editor.updateElement(element)

        val reloaded = newEditor(default = true)
        reloaded.updateUi(element)
        assertTrue(reloaded.isModified)
        assertEquals(JEditableCheckBox.Value.Boolean.FALSE, reloaded.value)
    }

    @Test
    fun `reset removes the property on save`() {
        // After resetToDefault() the editor must persist as "absent" so
        // subsequent reloads keep the gutter dark.
        val element = newElement()
        val editor = newEditor(default = false)
        editor.updateUi(element)

        editor.value = JEditableCheckBox.Value.Boolean.TRUE
        editor.updateElement(element)
        assertNotNull(element.getPropertyOrNull("test.bool"))

        editor.reset()
        assertFalse(editor.isModified)
        editor.updateElement(element)
        assertNull(element.getPropertyOrNull("test.bool"), "reset must drop the stored property")

        val reloaded = newEditor(default = false)
        reloaded.updateUi(element)
        assertFalse(reloaded.isModified, "reloaded editor must not light the gutter for an absent property")
    }
}
