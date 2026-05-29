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
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JMenuItem

/**
 * Behaviour test for the gutter-modified semantics layered on top of
 * [JEditableCheckBox] by subclasses such as
 * [org.apache.jmeter.gui.JBooleanPropertyEditor].
 *
 * The user-facing contract is:
 *  1. Initially the gutter is dark — the value is implicit / default.
 *  2. Any user-driven value change lights the gutter and the editor stays
 *     "modified" thereafter, even if the user happens to land back on the
 *     default value (because the property is now explicitly stored).
 *  3. The reset action (popup menu / `resetToDefault`) clears the
 *     modified flag and restores the default value.
 *  4. Loading from a [TestElement]-like source lights the gutter only
 *     when the source actually holds an explicit value; loading "absent"
 *     reverts the editor to a clean state.
 *
 * The test reuses the suppress-flag pattern that lives in
 * `JBooleanPropertyEditor` so that the production wiring is exercised
 * (listener + reset path + simulated `updateUi`) rather than just
 * poking [JEditableCheckBox.isModified] directly.
 */
class JEditableCheckBoxGutterSemanticsTest {
    private val identityLocalizer = ResourceLocalizer { it }

    /**
     * Test double that mirrors the production "explicit-set" pattern used
     * by `JBooleanPropertyEditor` and friends, without dragging in the
     * core/testbeans dependency tree.
     */
    private class ExplicitCheckBox(
        private val defaultValue: Boolean,
        localizer: ResourceLocalizer,
    ) : JEditableCheckBox(
        label = "label",
        configuration = Configuration(
            expressionMode = ExpressionMode.Allow(
                useExpression = LocalizedString("use_expression", localizer),
                useExpressionTooltip = LocalizedString("use_expression_tooltip", localizer),
            ),
            trueValue = LocalizedString("true_label", localizer),
            falseValue = LocalizedString("false_label", localizer),
            resetMode = ResetMode.Allow(LocalizedString("reset", localizer)),
        ),
        resourceLocalizer = localizer,
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
                value = Value.of(defaultValue)
                isModified = false
            } finally {
                suppressModifiedUpdate = false
            }
        }

        /** Mimics what `JBooleanPropertyEditor.updateUi(testElement)` does. */
        fun loadFromElement(explicitValue: Boolean?) {
            suppressModifiedUpdate = true
            try {
                value = Value.of(explicitValue ?: defaultValue)
                isModified = explicitValue != null
            } finally {
                suppressModifiedUpdate = false
            }
        }
    }

    private fun newCheckbox(default: Boolean = false): ExplicitCheckBox =
        ExplicitCheckBox(default, identityLocalizer)

    // --- Initial state ---

    @Test
    fun `gutter is dark before any interaction`() {
        assertFalse(newCheckbox().isModified)
    }

    // --- User-driven changes ---

    @Test
    fun `user-driven value change lights the gutter`() {
        val box = newCheckbox(default = false)
        box.value = JEditableCheckBox.Value.Boolean.TRUE
        assertTrue(box.isModified, "first user change must light the gutter")
    }

    @Test
    fun `user-driven change to the default value still counts as modified`() {
        // The whole point of the new semantics: returning to the default
        // value via the UI is still an explicit user assignment and must
        // keep the gutter lit. The earlier "value != default" rule was wrong.
        val box = newCheckbox(default = false)
        box.value = JEditableCheckBox.Value.Boolean.TRUE
        assertTrue(box.isModified)

        box.value = JEditableCheckBox.Value.Boolean.FALSE // == default
        assertTrue(box.isModified, "an explicit assignment equal to default is still explicit")
    }

    @Test
    fun `gutter stays lit through several user-driven changes`() {
        val box = newCheckbox(default = true)
        box.value = JEditableCheckBox.Value.Boolean.FALSE
        box.value = JEditableCheckBox.Value.Boolean.TRUE
        box.value = JEditableCheckBox.Value.Boolean.FALSE
        assertTrue(box.isModified)
    }

    // --- Reset ---

    @Test
    fun `resetToDefault restores default value and clears the gutter`() {
        val box = newCheckbox(default = true)
        // Establish a clean baseline: editor reflects the default (true),
        // gutter is dark. Without this, the visible-state of a freshly
        // constructed JCheckBox is "unchecked", which would make a
        // subsequent `value = FALSE` a no-op (no PropertyChangeEvent fires).
        box.loadFromElement(explicitValue = null)
        assertFalse(box.isModified)

        box.value = JEditableCheckBox.Value.Boolean.FALSE
        assertTrue(box.isModified)

        box.resetToDefault()

        assertFalse(box.isModified, "reset must clear the modified flag")
        assertEquals(JEditableCheckBox.Value.Boolean.TRUE, box.value, "reset must restore the default value")
    }

    @Test
    fun `user can re-modify after reset`() {
        val box = newCheckbox(default = false)
        box.value = JEditableCheckBox.Value.Boolean.TRUE
        box.resetToDefault()
        assertFalse(box.isModified)

        box.value = JEditableCheckBox.Value.Boolean.TRUE
        assertTrue(box.isModified, "post-reset user change must light the gutter again")
    }

    // --- Loading from a TestElement (updateUi parallel) ---

    @Test
    fun `loadFromElement with absent property leaves the gutter dark`() {
        val box = newCheckbox(default = true)
        box.loadFromElement(explicitValue = null)
        assertFalse(box.isModified)
        assertEquals(JEditableCheckBox.Value.Boolean.TRUE, box.value)
    }

    @Test
    fun `loadFromElement with explicit property lights the gutter`() {
        val box = newCheckbox(default = true)
        box.loadFromElement(explicitValue = false)
        assertTrue(box.isModified)
        assertEquals(JEditableCheckBox.Value.Boolean.FALSE, box.value)
    }

    @Test
    fun `loadFromElement with explicit value equal to default still lights the gutter`() {
        // Mirrors a real .jmx that explicitly stores the default value.
        // The user's intent ("I want this stored explicitly") must survive
        // a save/reload round-trip.
        val box = newCheckbox(default = true)
        box.loadFromElement(explicitValue = true)
        assertTrue(box.isModified)
    }

    @Test
    fun `loadFromElement does not fire spurious modifications from the listener`() {
        // Regression guard: without the suppress flag, value-change events
        // fired during loadFromElement would set isModified=true even when
        // the property is absent.
        val box = newCheckbox(default = true)
        box.loadFromElement(explicitValue = null) // start with default visible
        box.value = JEditableCheckBox.Value.Boolean.FALSE // user toggle → modified
        assertTrue(box.isModified)

        box.loadFromElement(explicitValue = null) // another absent-property load
        assertFalse(box.isModified, "absent property must clear the gutter even if it was lit")
    }

    // --- Reset menu item ---

    @Test
    fun `reset menu item is disabled when not modified and enabled when modified`() {
        val box = newCheckbox(default = false)
        val item = findResetMenuItemOnCheckbox(box)
        assertFalse(item.isEnabled, "reset must start disabled — nothing to reset")

        box.value = JEditableCheckBox.Value.Boolean.TRUE
        assertTrue(item.isEnabled, "reset must enable when the editor becomes modified")

        box.resetToDefault()
        assertFalse(item.isEnabled, "reset must disable again after resetToDefault")
    }

    @Test
    fun `clicking the reset menu item resets value and gutter`() {
        val box = newCheckbox(default = false)
        box.value = JEditableCheckBox.Value.Boolean.TRUE
        assertTrue(box.isModified)

        val item = findResetMenuItemOnCheckbox(box)
        item.doClick()

        assertFalse(box.isModified, "click on reset must clear the gutter")
        assertEquals(JEditableCheckBox.Value.Boolean.FALSE, box.value, "click on reset must restore the default")
    }

    // --- Reset must be reachable from the expression card too ---

    @Test
    fun `reset menu item exists on the expression card editor`() {
        // Regression guard for the issue where switching to expression mode
        // hid the popup menu entirely, leaving the user no way to reset.
        val box = newCheckbox(default = false)
        val expressionCombo = findExpressionCombo(box)
        val popup = expressionCombo.componentPopupMenu
        assertNotNull(popup, "expression combo must carry a popup so reset stays reachable")
        val resetItem = popup.subElements
            .map { it.component as JMenuItem }
            .firstOrNull { it.text == "reset" }
        assertNotNull(resetItem, "popup on the expression combo must include the reset action")
    }

    // --- Helpers ---

    private fun findResetMenuItemOnCheckbox(box: JEditableCheckBox): JMenuItem {
        val checkbox = findChild(box) { it is JCheckBox } as JCheckBox
        val popup = checkbox.componentPopupMenu
        assertNotNull(popup, "checkbox must have a popup menu when resetMode=Allow")
        return popup.subElements.map { it.component as JMenuItem }
            .first { it.text == "reset" }
    }

    private fun findExpressionCombo(box: JEditableCheckBox): JComboBox<*> =
        findChild(box) { it is JComboBox<*> } as JComboBox<*>

    private fun findChild(root: java.awt.Container, match: (java.awt.Component) -> Boolean): java.awt.Component {
        val queue = ArrayDeque<java.awt.Component>()
        queue += root
        while (queue.isNotEmpty()) {
            val c = queue.removeFirst()
            if (c !== root && match(c)) return c
            if (c is java.awt.Container) queue += c.components
        }
        error("no matching child component found")
    }
}
