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

import org.apache.jorphan.locale.LocalizedString
import org.apache.jorphan.locale.ResourceLocalizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.event.KeyEvent
import javax.swing.JMenuItem
import javax.swing.JTextArea

/**
 * Behaviour test for the gutter-modified semantics on
 * [JEditableTextArea]. Mirrors [JEditableTextFieldGutterSemanticsTest]:
 * the multi-line variant must follow exactly the same rules
 * (explicit-set, reset, backspace-as-reset, popup wiring) so the two
 * editors stay interchangeable from the user's point of view.
 */
class JEditableTextAreaGutterSemanticsTest {
    private val identityLocalizer = ResourceLocalizer { it }

    private class ExplicitTextArea(
        private val defaultValue: String,
        localizer: ResourceLocalizer,
    ) : JEditableTextArea(
        Configuration(
            resetMode = ResetMode.Allow(LocalizedString("reset", localizer)),
        ),
    ) {
        private var suppressModifiedUpdate = false

        init {
            addPropertyChangeListener(VALUE_PROPERTY) {
                if (!suppressModifiedUpdate) {
                    isModified = true
                }
            }
        }

        public override fun resetToDefault() {
            suppressModifiedUpdate = true
            try {
                value = defaultValue
                isModified = false
            } finally {
                suppressModifiedUpdate = false
            }
        }

        fun loadFromElement(explicitValue: String?) {
            suppressModifiedUpdate = true
            try {
                value = explicitValue ?: defaultValue
                isModified = explicitValue != null
            } finally {
                suppressModifiedUpdate = false
            }
        }
    }

    private fun newArea(default: String = ""): ExplicitTextArea =
        ExplicitTextArea(default, identityLocalizer)

    // --- Initial state ---

    @Test
    fun `gutter is dark before any interaction`() {
        assertFalse(newArea().isModified)
    }

    // --- User-driven changes ---

    @Test
    fun `user-driven value change lights the gutter`() {
        val area = newArea(default = "")
        area.value = "line one\nline two"
        assertTrue(area.isModified)
    }

    @Test
    fun `user-driven change back to default still counts as modified`() {
        val area = newArea(default = "default text")
        area.loadFromElement(explicitValue = null)
        area.value = "custom"
        assertTrue(area.isModified)

        area.value = "default text"
        assertTrue(area.isModified, "typing the default value is still an explicit assignment")
    }

    @Test
    fun `clearing a non-empty default to empty string lights the gutter`() {
        val area = newArea(default = "default text")
        area.loadFromElement(explicitValue = null)
        area.value = ""
        assertTrue(area.isModified)
    }

    @Test
    fun `multi-line value is read back verbatim`() {
        // Sanity-check that the wrapper does not strip newlines or
        // otherwise tamper with multi-line content as it crosses the
        // gutter / value-listener boundary.
        val area = newArea(default = "")
        area.value = "first\nsecond\nthird"
        assertEquals("first\nsecond\nthird", area.value)
        assertTrue(area.isModified)
    }

    // --- Reset ---

    @Test
    fun `resetToDefault restores default value and clears the gutter`() {
        val area = newArea(default = "default text")
        area.value = "custom"
        assertTrue(area.isModified)

        area.resetToDefault()

        assertFalse(area.isModified)
        assertEquals("default text", area.value)
    }

    @Test
    fun `user can re-modify after reset`() {
        val area = newArea(default = "")
        area.value = "first"
        area.resetToDefault()
        assertFalse(area.isModified)

        area.value = "second"
        assertTrue(area.isModified)
    }

    // --- Loading from a TestElement-like source ---

    @Test
    fun `loadFromElement with absent property leaves the gutter dark`() {
        val area = newArea(default = "default text")
        area.loadFromElement(explicitValue = null)
        assertFalse(area.isModified)
        assertEquals("default text", area.value)
    }

    @Test
    fun `loadFromElement with explicit value lights the gutter`() {
        val area = newArea(default = "default text")
        area.loadFromElement(explicitValue = "custom")
        assertTrue(area.isModified)
        assertEquals("custom", area.value)
    }

    @Test
    fun `loadFromElement with explicit value equal to default still lights the gutter`() {
        val area = newArea(default = "default text")
        area.loadFromElement(explicitValue = "default text")
        assertTrue(area.isModified)
    }

    @Test
    fun `loadFromElement does not fire spurious modifications from the listener`() {
        val area = newArea(default = "default text")
        area.loadFromElement(explicitValue = null)
        area.value = "user typed"
        assertTrue(area.isModified)

        area.loadFromElement(explicitValue = null)
        assertFalse(area.isModified, "absent property must clear the gutter even if it was lit")
    }

    // --- Reset menu item ---

    @Test
    fun `reset menu item is disabled when not modified and enabled when modified`() {
        val area = newArea(default = "default text")
        val item = findResetMenuItem(area)
        assertFalse(item.isEnabled)

        area.loadFromElement(explicitValue = null)
        area.value = "custom"
        assertTrue(item.isEnabled)

        area.resetToDefault()
        assertFalse(item.isEnabled)
    }

    @Test
    fun `clicking the reset menu item resets value and gutter`() {
        val area = newArea(default = "default text")
        area.loadFromElement(explicitValue = null)
        area.value = "custom"
        val item = findResetMenuItem(area)
        item.doClick()

        assertFalse(area.isModified)
        assertEquals("default text", area.value)
    }

    // --- Backspace / Delete on an empty modified field acts as reset ---

    @Test
    fun `backspace on empty modified text area resets to default`() {
        val area = newArea(default = "default text")
        area.loadFromElement(explicitValue = null)
        area.value = ""
        assertTrue(area.isModified)

        sendBackspace(area)

        assertFalse(area.isModified)
        assertEquals("default text", area.value)
    }

    @Test
    fun `delete on empty modified text area also resets to default`() {
        val area = newArea(default = "default text")
        area.loadFromElement(explicitValue = null)
        area.value = ""
        assertTrue(area.isModified)

        sendKey(area, KeyEvent.VK_DELETE)

        assertFalse(area.isModified)
        assertEquals("default text", area.value)
    }

    @Test
    fun `backspace on empty unmodified text area is a no-op`() {
        val area = newArea(default = "")
        area.loadFromElement(explicitValue = null)
        assertFalse(area.isModified)

        val event = sendBackspace(area)
        assertFalse(event.isConsumed)
        assertFalse(area.isModified)
    }

    @Test
    fun `backspace on multi-line text is not consumed`() {
        // Backspace inside a non-empty multi-line text area must follow
        // the standard JTextArea behaviour (delete the previous character
        // or merge two lines), not trigger a reset.
        val area = newArea(default = "")
        area.loadFromElement(explicitValue = null)
        area.value = "line one\nline two"
        assertTrue(area.isModified)

        val event = sendBackspace(area)

        assertFalse(event.isConsumed)
        assertTrue(area.isModified)
    }

    // --- Helpers ---

    private fun findResetMenuItem(area: JEditableTextArea): JMenuItem {
        val popup = area.getInnerTextArea().componentPopupMenu
        assertNotNull(popup, "text area must have a popup menu when resetMode=Allow")
        return popup.subElements.map { it.component as JMenuItem }
            .first { it.text == "reset" }
    }

    private fun sendBackspace(area: JEditableTextArea): KeyEvent =
        sendKey(area, KeyEvent.VK_BACK_SPACE)

    private fun sendKey(area: JEditableTextArea, keyCode: Int): KeyEvent {
        val ta = area.getInnerTextArea()
        val event = keyPressEvent(ta, keyCode)
        ta.keyListeners.forEach { it.keyPressed(event) }
        return event
    }

    private fun keyPressEvent(ta: JTextArea, keyCode: Int): KeyEvent =
        KeyEvent(ta, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED)
}
