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
import org.apache.jorphan.locale.PlainValue
import org.apache.jorphan.locale.ResourceKeyed
import org.apache.jorphan.locale.ResourceLocalizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Round-trip tests for [JEnumPropertyEditor]: the modified-gutter and
 * the underlying property must stay in sync across `updateElement` →
 * `updateUi`, mirroring what happens on save / open of a `.jmx`.
 */
class JEnumPropertyEditorTest {
    private val identityLocalizer = ResourceLocalizer { it }

    private enum class Mode(override val resourceKey: String) : ResourceKeyed {
        FAST("mode_fast"),
        SLOW("mode_slow"),
    }

    private class TestElementImpl : AbstractTestElement()

    private fun newElement(): AbstractTestElement = TestElementImpl()

    private fun newEditor(default: String? = null): JEnumPropertyEditor<Mode> {
        val descriptor = StringPropertyDescriptor<TestElementSchema>(
            shortName = "test",
            name = "test.mode",
            defaultValue = default,
        )
        return JEnumPropertyEditor.create(
            descriptor,
            "test_label",
            Mode::class.java,
            identityLocalizer,
        )
    }

    @Test
    fun `untouched editor leaves the element clean`() {
        val element = newElement()
        val editor = newEditor()
        editor.updateUi(element)
        editor.updateElement(element)

        assertFalse(editor.isModified)
        assertNull(element.getPropertyOrNull("test.mode"))
    }

    @Test
    fun `selected enum value round-trips`() {
        val element = newElement()
        val editor = newEditor()
        editor.updateUi(element)

        editor.value = PlainValue(Mode.SLOW.resourceKey)
        assertTrue(editor.isModified)
        editor.updateElement(element)

        val stored = element.getPropertyOrNull("test.mode")
        assertNotNull(stored)
        assertEquals(Mode.SLOW.resourceKey, stored!!.stringValue)

        val reloaded = newEditor()
        reloaded.updateUi(element)
        assertTrue(reloaded.isModified)
        assertEquals(Mode.SLOW.resourceKey, (reloaded.value as ResourceKeyed).resourceKey)
    }

    @Test
    fun `selected enum value round-trips even when it equals the default`() {
        // Round-trip preservation: a value the user has explicitly chosen
        // must survive save/reload as "modified" even when that value
        // happens to coincide with the descriptor's default.
        val element = newElement()
        val editor = newEditor(default = Mode.FAST.resourceKey)
        editor.updateUi(element)

        editor.value = PlainValue(Mode.FAST.resourceKey)
        assertTrue(editor.isModified)
        editor.updateElement(element)

        val stored = element.getPropertyOrNull("test.mode")
        assertNotNull(stored, "explicit selection equal to the default must be persisted")
        assertEquals(Mode.FAST.resourceKey, stored!!.stringValue)

        val reloaded = newEditor(default = Mode.FAST.resourceKey)
        reloaded.updateUi(element)
        assertTrue(reloaded.isModified, "reload must light the gutter for an explicit value, even == default")
    }

    @Test
    fun `reset removes the property on save`() {
        val element = newElement()
        val editor = newEditor()
        editor.updateUi(element)

        editor.value = PlainValue(Mode.SLOW.resourceKey)
        editor.updateElement(element)
        assertNotNull(element.getPropertyOrNull("test.mode"))

        editor.reset()
        assertFalse(editor.isModified)
        editor.updateElement(element)
        assertNull(element.getPropertyOrNull("test.mode"), "reset must drop the stored property")

        val reloaded = newEditor()
        reloaded.updateUi(element)
        assertFalse(reloaded.isModified)
    }
}
