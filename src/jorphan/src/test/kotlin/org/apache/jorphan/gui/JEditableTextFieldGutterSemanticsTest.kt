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
import javax.swing.JTextField

/**
 * Behaviour test for the gutter-modified semantics layered on top of
 * [JEditableTextField] by subclasses (`JStringPropertyEditor`).
 *
 * The contract mirrors [JEditableCheckBoxGutterSemanticsTest]:
 *  1. Initially the gutter is dark.
 *  2. Any user-driven value change lights the gutter and keeps it lit
 *     until reset, even when the user types back the default value.
 *  3. `resetToDefault` clears the modified flag and restores the default.
 *  4. `loadFromElement` lights the gutter only when an explicit value is
 *     stored on the test element.
 */
class JEditableTextFieldGutterSemanticsTest {
    private val identityLocalizer = ResourceLocalizer { it }

    private class ExplicitTextField(
        private val defaultValue: String,
        localizer: ResourceLocalizer,
    ) : JEditableTextField(
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

        /** Mimics what `JStringPropertyEditor.updateUi(testElement)` does. */
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

    private fun newField(default: String = ""): ExplicitTextField =
        ExplicitTextField(default, identityLocalizer)

    // --- Initial state ---

    @Test
    fun `gutter is dark before any interaction`() {
        assertFalse(newField().isModified)
    }

    // --- User-driven changes ---

    @Test
    fun `user-driven value change lights the gutter`() {
        val field = newField(default = "")
        field.value = "abc"
        assertTrue(field.isModified)
    }

    @Test
    fun `user-driven change back to the default still counts as modified`() {
        // Same key invariant as for checkbox: returning to the default via
        // typing is still an explicit user action.
        val field = newField(default = "default-value")
        field.loadFromElement(explicitValue = null) // start clean at default
        field.value = "custom"
        assertTrue(field.isModified)

        field.value = "default-value"
        assertTrue(field.isModified, "typing the default value is still an explicit assignment")
    }

    @Test
    fun `clearing a non-empty default to empty string lights the gutter`() {
        // The user's intent is "I want this stored as empty"; the explicit
        // empty case is intentionally captured as modified.
        val field = newField(default = "default-value")
        field.loadFromElement(explicitValue = null)
        field.value = ""
        assertTrue(field.isModified)
    }

    // --- Reset ---

    @Test
    fun `resetToDefault restores default value and clears the gutter`() {
        val field = newField(default = "default-value")
        field.value = "custom"
        assertTrue(field.isModified)

        field.resetToDefault()

        assertFalse(field.isModified)
        assertEquals("default-value", field.value)
    }

    @Test
    fun `user can re-modify after reset`() {
        val field = newField(default = "")
        field.value = "first"
        field.resetToDefault()
        assertFalse(field.isModified)

        field.value = "second"
        assertTrue(field.isModified)
    }

    // --- Loading from a TestElement-like source ---

    @Test
    fun `loadFromElement with absent property leaves the gutter dark`() {
        val field = newField(default = "default-value")
        field.loadFromElement(explicitValue = null)
        assertFalse(field.isModified)
        assertEquals("default-value", field.value)
    }

    @Test
    fun `loadFromElement with explicit value lights the gutter`() {
        val field = newField(default = "default-value")
        field.loadFromElement(explicitValue = "custom")
        assertTrue(field.isModified)
        assertEquals("custom", field.value)
    }

    @Test
    fun `loadFromElement with explicit value equal to default still lights the gutter`() {
        // Round-trip preservation: a .jmx that explicitly stores the
        // default value must light the gutter on reload.
        val field = newField(default = "default-value")
        field.loadFromElement(explicitValue = "default-value")
        assertTrue(field.isModified)
    }

    @Test
    fun `loadFromElement does not fire spurious modifications from the listener`() {
        val field = newField(default = "default-value")
        field.loadFromElement(explicitValue = null)
        field.value = "user typed"
        assertTrue(field.isModified)

        field.loadFromElement(explicitValue = null)
        assertFalse(field.isModified, "absent property must clear the gutter even if it was lit")
    }

    // --- Reset menu item ---

    @Test
    fun `reset menu item is disabled when not modified and enabled when modified`() {
        val field = newField(default = "default-value")
        val item = findResetMenuItem(field)
        assertFalse(item.isEnabled, "must start disabled")

        field.loadFromElement(explicitValue = null)
        field.value = "custom"
        assertTrue(item.isEnabled)

        field.resetToDefault()
        assertFalse(item.isEnabled)
    }

    @Test
    fun `clicking the reset menu item resets value and gutter`() {
        val field = newField(default = "default-value")
        field.loadFromElement(explicitValue = null)
        field.value = "custom"
        assertTrue(field.isModified)

        val item = findResetMenuItem(field)
        item.doClick()

        assertFalse(field.isModified)
        assertEquals("default-value", field.value)
    }

    // --- Backspace / Delete on an empty modified field acts as reset ---

    @Test
    fun `backspace on empty modified field resets to default`() {
        // Scenario: user typed "abc" (gutter lit), then deleted everything
        // by holding backspace (gutter still lit, text empty). Pressing
        // backspace once more on the empty modified field is interpreted
        // as "I want to roll this back to the default" — saves a popup trip.
        val field = newField(default = "default-value")
        field.loadFromElement(explicitValue = null)
        field.value = ""
        assertTrue(field.isModified)

        sendBackspace(field)

        assertFalse(field.isModified, "second backspace on empty must reset")
        assertEquals("default-value", field.value, "second backspace must restore the default")
    }

    @Test
    fun `delete on empty modified field also resets to default`() {
        // Forward-delete (Fn+Backspace on macOS, Delete on Windows/Linux)
        // is treated identically to backspace.
        val field = newField(default = "default-value")
        field.loadFromElement(explicitValue = null)
        field.value = ""
        assertTrue(field.isModified)

        sendKey(field, KeyEvent.VK_DELETE)

        assertFalse(field.isModified)
        assertEquals("default-value", field.value)
    }

    @Test
    fun `backspace on empty unmodified field is a no-op`() {
        // Initial / post-reset state. Backspace must not consume the event,
        // so the standard JTextField behaviour (typically a UI beep)
        // remains the platform default.
        val field = newField(default = "")
        field.loadFromElement(explicitValue = null)
        assertFalse(field.isModified)
        assertEquals("", field.value)

        val event = sendBackspace(field)

        assertFalse(event.isConsumed, "no reset to perform — event must pass through to the JTextField")
        assertFalse(field.isModified)
    }

    @Test
    fun `backspace on non-empty field is not consumed`() {
        // The custom handling kicks in only when text is empty;
        // otherwise the standard JTextField backspace behaviour stays.
        val field = newField(default = "")
        field.loadFromElement(explicitValue = null)
        field.value = "abc"
        assertTrue(field.isModified)

        val event = sendBackspace(field)

        assertFalse(event.isConsumed, "non-empty backspace must reach the standard text-field handler")
        assertTrue(field.isModified, "non-empty backspace must not lower the modified flag")
    }

    // --- Helpers ---

    private fun findResetMenuItem(field: JEditableTextField): JMenuItem {
        val popup = field.getInnerTextField().componentPopupMenu
        assertNotNull(popup, "text field must have a popup menu when resetMode=Allow")
        return popup.subElements.map { it.component as JMenuItem }
            .first { it.text == "reset" }
    }

    private fun sendBackspace(field: JEditableTextField): KeyEvent =
        sendKey(field, KeyEvent.VK_BACK_SPACE)

    /**
     * Delivers a key-press to the inner text field's KeyListeners directly.
     * `Component.dispatchEvent` is unreliable for synthetic KeyEvents in
     * headless test environments (the toolkit may swallow them before the
     * listeners are reached), so we invoke the listeners ourselves.
     */
    private fun sendKey(field: JEditableTextField, keyCode: Int): KeyEvent {
        val tf = field.getInnerTextField()
        val event = keyPressEvent(tf, keyCode)
        tf.keyListeners.forEach { it.keyPressed(event) }
        return event
    }

    private fun keyPressEvent(tf: JTextField, keyCode: Int): KeyEvent =
        KeyEvent(tf, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, keyCode, KeyEvent.CHAR_UNDEFINED)
}
