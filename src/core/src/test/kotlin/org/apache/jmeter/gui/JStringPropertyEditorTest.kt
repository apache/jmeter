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
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apache.jorphan.locale.ResourceLocalizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Round-trip tests for [JStringPropertyEditor]: the modified-gutter
 * indicator and the underlying test element must stay in sync across a
 * `updateElement` → `updateUi` cycle, which is what happens on Save and
 * Open of a `.jmx` test plan.
 */
class JStringPropertyEditorTest {
    private val identityLocalizer = ResourceLocalizer { it }

    private class TestElementImpl : AbstractTestElement()

    private fun newElement(): AbstractTestElement = TestElementImpl()

    private fun newEditor(default: String? = null): JStringPropertyEditor {
        val descriptor = StringPropertyDescriptor<TestElementSchema>(
            shortName = "test",
            name = "test.string",
            defaultValue = default,
        )
        return JStringPropertyEditor(descriptor, identityLocalizer)
    }

    @Test
    fun `untouched editor leaves the element clean`() {
        val element = newElement()
        val editor = newEditor()
        editor.updateUi(element)
        editor.updateElement(element)

        assertFalse(editor.isModified)
        assertNull(element.getPropertyOrNull("test.string"))
    }

    @Test
    fun `non-empty value round-trips`() {
        val element = newElement()
        val editor = newEditor()
        editor.updateUi(element)

        editor.value = "custom"
        assertTrue(editor.isModified)
        editor.updateElement(element)

        val stored = element.getPropertyOrNull("test.string")
        assertNotNull(stored)
        assertEquals("custom", stored!!.stringValue)

        val reloaded = newEditor()
        reloaded.updateUi(element)
        assertTrue(reloaded.isModified)
        assertEquals("custom", reloaded.value)
    }

    @Test
    fun `explicit empty string round-trips`() {
        // Regression guard: with the new "isModified-aware" updateElement,
        // an empty string the user has typed (gutter lit) must survive a
        // save/reload cycle as an explicit empty value rather than being
        // silently converted to "absent".
        val element = newElement()
        val editor = newEditor(default = "default-value")
        editor.updateUi(element)

        // Make the editor look modified, then clear it back to "".
        editor.value = "anything"
        editor.value = ""
        assertTrue(editor.isModified, "explicit empty string must keep the editor modified")
        editor.updateElement(element)

        val stored = element.getPropertyOrNull("test.string")
        assertNotNull(stored, "explicit empty string must be persisted, not dropped")
        assertEquals("", stored!!.stringValue)

        val reloaded = newEditor(default = "default-value")
        reloaded.updateUi(element)
        assertTrue(reloaded.isModified, "reloaded editor must keep the gutter lit for an explicit empty value")
        assertEquals("", reloaded.value)
    }

    @Test
    fun `reset removes the property on save`() {
        val element = newElement()
        val editor = newEditor()
        editor.updateUi(element)

        editor.value = "custom"
        editor.updateElement(element)
        assertNotNull(element.getPropertyOrNull("test.string"))

        editor.reset()
        assertFalse(editor.isModified)
        editor.updateElement(element)
        assertNull(element.getPropertyOrNull("test.string"), "reset must drop the stored property")

        val reloaded = newEditor()
        reloaded.updateUi(element)
        assertFalse(reloaded.isModified)
    }
}
