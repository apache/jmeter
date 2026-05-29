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
import javax.swing.JMenuItem

/**
 * Smoke test for the integration between [JEditableCheckBox] and the
 * embedded [ModifiedGutter]. Subclasses ([JBooleanPropertyEditor], etc.)
 * drive the gutter through the inherited `isModified` property; this
 * verifies the wiring exists and does not regress with the layout
 * refactor that moved the CardLayout into a child panel.
 */
class JEditableCheckBoxModifiedTest {
    private val identityLocalizer = ResourceLocalizer { it }

    private fun newCheckbox() = JEditableCheckBox(
        label = "label",
        configuration = JEditableCheckBox.Configuration(
            expressionMode = ExpressionMode.Forbid,
            trueValue = LocalizedString("true_label", identityLocalizer),
            falseValue = LocalizedString("false_label", identityLocalizer),
        ),
        resourceLocalizer = identityLocalizer,
    )

    @Test
    fun `isModified default is false`() {
        assertFalse(newCheckbox().isModified)
    }

    @Test
    fun `isModified setter is reflected by getter`() {
        val box = newCheckbox()
        box.isModified = true
        assertTrue(box.isModified)
        box.isModified = false
        assertFalse(box.isModified)
    }

    @Test
    fun `value setter still works after layout refactor`() {
        // Regression guard: when CardLayout moved off `this` onto an inner
        // child panel, value getter / setter must still locate the active
        // card via cardPanel.components rather than this.components.
        val box = newCheckbox()
        box.value = JEditableCheckBox.Value.Boolean.TRUE
        assertEquals(JEditableCheckBox.Value.Boolean.TRUE, box.value)
        box.value = JEditableCheckBox.Value.Boolean.FALSE
        assertEquals(JEditableCheckBox.Value.Boolean.FALSE, box.value)
    }

    private fun newCheckboxWithReset(onReset: () -> Unit = {}) =
        object : JEditableCheckBox(
            label = "label",
            configuration = Configuration(
                expressionMode = ExpressionMode.Forbid,
                trueValue = LocalizedString("true_label", identityLocalizer),
                falseValue = LocalizedString("false_label", identityLocalizer),
                resetMode = ResetMode.Allow(LocalizedString("reset", identityLocalizer)),
            ),
            resourceLocalizer = identityLocalizer,
        ) {
            override fun resetToDefault() = onReset()
        }

    private fun findResetMenuItem(box: JEditableCheckBox): JMenuItem {
        // The popup menu lives on the inner JCheckBox.
        val checkbox = findCheckbox(box)
        val menu = checkbox.componentPopupMenu
        assertNotNull(menu, "componentPopupMenu must exist when resetMode=Allow")
        return menu.subElements.map { it.component as JMenuItem }
            .first { it.text == "reset" }
    }

    private fun findCheckbox(box: JEditableCheckBox): JCheckBox {
        // Walk the component tree until we find the JCheckBox.
        val queue = ArrayDeque<java.awt.Component>()
        queue += box
        while (queue.isNotEmpty()) {
            val c = queue.removeFirst()
            if (c is JCheckBox) return c
            if (c is java.awt.Container) {
                queue += c.components
            }
        }
        error("inner JCheckBox not found")
    }

    @Test
    fun `reset menu item is disabled until modified`() {
        val box = newCheckboxWithReset()
        val item = findResetMenuItem(box)
        assertFalse(item.isEnabled, "must start disabled — nothing to reset")

        box.isModified = true
        assertTrue(item.isEnabled, "must become enabled when value diverges from default")

        box.isModified = false
        assertFalse(item.isEnabled, "must go back to disabled when reset to default")
    }

    @Test
    fun `reset menu item triggers resetToDefault`() {
        var called = 0
        val box = newCheckboxWithReset(onReset = { called++ })
        val item = findResetMenuItem(box)
        // Force-enable: doClick respects enabled state and we are testing the
        // action wiring, not the enable logic (covered above).
        item.isEnabled = true
        item.doClick()
        assertEquals(1, called)
    }
}
