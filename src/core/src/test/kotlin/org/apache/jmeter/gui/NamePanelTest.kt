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
import org.apache.jorphan.gui.JEditableTextField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Component
import java.awt.Container
import java.awt.event.KeyEvent
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JTextField

/**
 * Headless tests for the name-field modified gutter.
 *
 * These run without a display (Swing components can be instantiated and
 * their model inspected as long as nothing is painted), so they let us
 * verify the gutter logic and the layout wiring without launching the GUI.
 *
 * The "name editor is wired into the title panel" test in particular
 * guards against the regression where `makeTitlePanel` added the raw
 * inner `JTextField` instead of the gutter-aware editor, so the gutter
 * was never shown.
 */
class NamePanelTest {
    private fun editorOf(panel: NamePanel): JEditableTextField =
        panel.nameComponent as JEditableTextField

    @Test
    fun `gutter is dark when name equals the default`() {
        val panel = NamePanel()
        panel.setDefaultName("HTTP Request")
        panel.name = "HTTP Request"
        assertFalse(editorOf(panel).isModified, "name == default must keep the gutter dark")
    }

    @Test
    fun `gutter lights up when name differs from the default`() {
        val panel = NamePanel()
        panel.setDefaultName("HTTP Request")
        panel.name = "Login request"
        assertTrue(editorOf(panel).isModified, "a custom name must light the gutter")
    }

    @Test
    fun `gutter goes dark again when the name is typed back to the default`() {
        val panel = NamePanel()
        panel.setDefaultName("HTTP Request")
        panel.name = "Login request"
        assertTrue(editorOf(panel).isModified)

        panel.name = "HTTP Request"
        assertFalse(editorOf(panel).isModified, "restoring the default name must clear the gutter")
    }

    @Test
    fun `setDefaultName recomputes the gutter for the current name`() {
        // When the owning component reports its static label after the name
        // is already set, the gutter must reflect the comparison immediately.
        val panel = NamePanel()
        panel.name = "Login request"
        panel.setDefaultName("Login request")
        assertFalse(editorOf(panel).isModified)

        panel.setDefaultName("HTTP Request")
        assertTrue(editorOf(panel).isModified)
    }

    @Test
    fun `reset menu item restores the default name and clears the gutter`() {
        // Regression guard: the popup Reset must actually restore the default
        // name. (A previous bug had resetToDefault calling the inherited
        // Component.setName instead of NamePanel.setName, so Reset did nothing.)
        val panel = NamePanel()
        panel.setDefaultName("HTTP Request")
        panel.name = "Login request"
        assertTrue(editorOf(panel).isModified)

        resetMenuItem(panel).doClick()

        assertEquals("HTTP Request", panel.name, "Reset must restore the default name")
        assertFalse(editorOf(panel).isModified, "Reset must clear the gutter")
    }

    @Test
    fun `backspace on an empty modified name resets to default`() {
        val panel = NamePanel()
        panel.setDefaultName("HTTP Request")
        panel.name = "" // user cleared the field -> empty but modified
        assertTrue(editorOf(panel).isModified)

        val tf = editorOf(panel).getInnerTextField()
        val event = KeyEvent(tf, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED)
        tf.keyListeners.forEach { it.keyPressed(event) }

        assertEquals("HTTP Request", panel.name, "backspace on empty modified name must reset to default")
        assertFalse(editorOf(panel).isModified)
    }

    private fun resetMenuItem(panel: NamePanel): JMenuItem {
        val tf: JTextField = panel.nameField
        val popup = tf.componentPopupMenu
        val resetText = org.apache.jmeter.util.JMeterUtils.getResString("reset")
        return popup.subElements.map { it.component as JMenuItem }.first { it.text == resetText }
    }

    @Test
    fun `name editor with gutter is placed in the title panel`() {
        // Regression guard: makeTitlePanel must add the gutter-aware editor,
        // not the raw inner JTextField — otherwise the gutter never renders.
        val gui = object : AbstractJMeterGuiComponent() {
            override fun getLabelResource(): String = "dummy_element_for_tests"
            override fun createTestElement(): TestElement = TODO()
            override fun modifyTestElement(element: TestElement?) = TODO()
            override fun createPopupMenu(): JPopupMenu = TODO()
            override fun getMenuCategories(): MutableCollection<String> = TODO()
            fun titlePanelForTest(): Container = makeTitlePanel() as Container
        }

        val titlePanel = gui.titlePanelForTest()
        val nameField = gui.namePanel.nameField

        // Find the JEditableTextField that actually contains the name field.
        val editor = findDescendant(titlePanel) { it is JEditableTextField }
        assertTrue(editor != null) {
            "the title panel must contain a JEditableTextField wrapping the name field"
        }
        assertTrue(isAncestorOf(editor as Container, nameField)) {
            "the name field must live inside the gutter-aware editor that is in the title panel"
        }
        // And the raw name field must NOT be added to the title panel directly.
        assertSame(editor, findDescendant(titlePanel) { it is JEditableTextField })
    }

    private fun findDescendant(root: Container, match: (Component) -> Boolean): Component? {
        val queue = ArrayDeque<Component>()
        queue += root
        while (queue.isNotEmpty()) {
            val c = queue.removeFirst()
            if (c !== root && match(c)) return c
            if (c is Container) queue += c.components
        }
        return null
    }

    private fun isAncestorOf(ancestor: Container, descendant: Component): Boolean {
        var c: Component? = descendant
        while (c != null) {
            if (c === ancestor) return true
            c = c.parent
        }
        return false
    }
}
