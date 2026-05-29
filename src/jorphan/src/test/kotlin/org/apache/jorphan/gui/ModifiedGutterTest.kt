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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.BorderLayout
import java.beans.PropertyChangeEvent
import javax.swing.JCheckBox
import javax.swing.JLabel

class ModifiedGutterTest {
    @Test
    fun `target is placed in CENTER`() {
        val target = JLabel("hi")
        val gutter = ModifiedGutter(target)
        val layout = gutter.layout as BorderLayout
        assertSame(target, layout.getLayoutComponent(BorderLayout.CENTER))
        assertNotNull(layout.getLayoutComponent(BorderLayout.WEST), "gutter strip must occupy WEST")
        assertSame(target, gutter.target)
    }

    @Test
    fun `gutter slot is reserved even when not modified`() {
        // The whole point of the pattern is that toggling modified must not
        // shift other form fields, so the WEST slot must stay non-empty.
        val gutter = ModifiedGutter(JLabel("x"))
        assertFalse(gutter.isModified)
        val strip = (gutter.layout as BorderLayout).getLayoutComponent(BorderLayout.WEST)
        assertNotNull(strip)
        assertEquals(ModifiedGutter.GUTTER_WIDTH, strip!!.preferredSize.width)
    }

    @Test
    fun `setting modified fires a property change`() {
        val gutter = ModifiedGutter(JCheckBox())
        val events = mutableListOf<PropertyChangeEvent>()
        gutter.addPropertyChangeListener(ModifiedGutter.MODIFIED_PROPERTY) { events += it }

        gutter.isModified = true

        assertTrue(gutter.isModified)
        assertEquals(1, events.size, "exactly one event expected")
        assertEquals(false, events[0].oldValue)
        assertEquals(true, events[0].newValue)
    }

    @Test
    fun `setting modified to the same value is a no-op`() {
        val gutter = ModifiedGutter(JCheckBox())
        val events = mutableListOf<PropertyChangeEvent>()
        gutter.addPropertyChangeListener(ModifiedGutter.MODIFIED_PROPERTY) { events += it }

        gutter.isModified = false // already false
        gutter.isModified = true
        gutter.isModified = true // already true

        assertEquals(1, events.size, "redundant assignments must not fire events")
    }

    @Test
    fun `accessibility description reflects modified state`() {
        // Screen readers / colour-blind users rely on something other than colour
        // to learn the modified state; we expose it via accessibleDescription.
        val gutter = ModifiedGutter(JCheckBox())
        assertNull(gutter.accessibleContext?.accessibleDescription)

        gutter.isModified = true
        assertEquals("modified", gutter.accessibleContext?.accessibleDescription)

        gutter.isModified = false
        assertNull(gutter.accessibleContext?.accessibleDescription)
    }

    @Test
    fun `withModifiedGutter wraps the receiver`() {
        val checkbox = JCheckBox()
        val gutter = checkbox.withModifiedGutter()
        assertSame(checkbox, gutter.target)
        assertFalse(gutter.isModified)
    }
}
