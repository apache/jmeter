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

import org.apiguardian.api.API
import org.jetbrains.annotations.NonNls
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

/**
 * Wraps a target component and renders a thin coloured "gutter" on the left
 * to signal that the value differs from the default ("modified" indicator).
 *
 * The gutter pattern follows the convention used by IDEs (IntelliJ IDEA,
 * Visual Studio Code) where a vertical accent strip next to a setting denotes
 * that the user has overridden the default value. Reset/expression actions are
 * expected to live in the component's context menu, not as adjacent buttons,
 * to keep configuration forms visually quiet.
 *
 * The decorator is a plain [JPanel] (not a [javax.swing.border.Border]) because
 * applying a custom border to controls such as [javax.swing.JComboBox] or
 * [javax.swing.JCheckBox] interferes with the look-and-feel UI delegate.
 *
 * Typical usage:
 * ```kotlin
 * val editor: JEditableCheckBox = ...
 * val gutter = ModifiedGutter(editor)
 * editor.addPropertyChangeListener(JEditableCheckBox.VALUE_PROPERTY) {
 *     gutter.isModified = editor.value != defaultValue
 * }
 * formPanel.add(gutter)
 * ```
 *
 * Or, fluently:
 * ```kotlin
 * val gutter = editor.withModifiedGutter()
 * ```
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public class ModifiedGutter(
    public val target: JComponent,
) : JPanel(BorderLayout(GUTTER_GAP, 0)) {
    public companion object {
        /** Width of the accent strip in pixels. */
        public const val GUTTER_WIDTH: Int = 3

        /**
         * Horizontal gap between the gutter strip and the target component.
         * Kept very small (1 px) so that the strip visually belongs to the
         * target rather than reading as a separator between siblings.
         */
        public const val GUTTER_GAP: Int = 1

        /**
         * Empty space reserved to the left of the gutter strip, between the
         * preceding sibling and the start of the strip. Without it, a 3 px
         * strip with a 1 px gap to the target ends up roughly equidistant
         * between two adjacent controls, which makes it look like a divider.
         */
        public const val LEFT_INSET: Int = 4

        /** Property name fired by [setModified] when the modified flag changes. */
        @NonNls
        public const val MODIFIED_PROPERTY: String = "modified"

        /**
         * UIManager keys consulted (in order) when picking the gutter colour
         * for an enabled control. The first non-null match wins. Designed to
         * play well with FlatLaf (`Component.accentColor`, `Component.linkColor`)
         * and to fall back to standard Swing keys for other look-and-feels.
         */
        @NonNls
        private val GUTTER_COLOR_KEYS: List<String> = listOf(
            "ModifiedGutter.color",
            "Component.accentColor",
            "Component.linkColor",
            "Component.focusColor",
            "controlHighlight",
        )

        /**
         * UIManager keys consulted (in order) for the disabled gutter colour.
         * If none match, the enabled colour is dimmed via [DISABLED_ALPHA].
         */
        @NonNls
        private val DISABLED_GUTTER_COLOR_KEYS: List<String> = listOf(
            "ModifiedGutter.disabledColor",
            "Component.disabledBorderColor",
            "Component.disabledForeground",
            "controlShadow",
        )

        /** Fallback colour when the look-and-feel does not provide one. */
        private val FALLBACK_COLOR: Color = Color(0x4C84FF)

        /**
         * Alpha applied to the enabled colour when no LaF-provided disabled
         * colour is available. Roughly 38 % opacity — enough to read as
         * "muted" without disappearing.
         */
        private const val DISABLED_ALPHA: Int = 96
    }

    private var modifiedFlag: Boolean = false

    private val gutter: JComponent = object : JComponent() {
        init {
            preferredSize = Dimension(GUTTER_WIDTH, 0)
            minimumSize = Dimension(GUTTER_WIDTH, 0)
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            if (!modifiedFlag) {
                return
            }
            g.color = lookupGutterColor(this@ModifiedGutter.isEnabled)
            // Paint the strip only as tall as the target's preferred height
            // so it does not overshoot the wrapped control vertically when
            // the parent layout stretches the panel (e.g. inside a row of
            // taller siblings). The strip is centred on the target.
            val targetH = target.preferredSize.height.coerceAtLeast(0)
            val drawH = if (targetH in 1 until height) targetH else height
            val y = ((height - drawH) / 2).coerceAtLeast(0)
            g.fillRect(0, y, width, drawH)
        }

        override fun isOpaque(): Boolean = false
    }

    /**
     * Whether the wrapped component currently holds a value that differs
     * from the default. When `true`, the gutter is painted; when `false`,
     * the gutter slot is reserved but transparent (so toggling the flag
     * never causes a layout shift).
     *
     * Setting the same value twice is a no-op and does not fire any event.
     * A [java.beans.PropertyChangeEvent] with name [MODIFIED_PROPERTY] is
     * fired when the value actually changes.
     */
    public var isModified: Boolean
        get() = modifiedFlag
        set(value) {
            if (modifiedFlag == value) {
                return
            }
            val old = modifiedFlag
            modifiedFlag = value
            gutter.repaint()
            updateAccessibility()
            firePropertyChange(MODIFIED_PROPERTY, old, value)
        }

    init {
        isOpaque = false
        // The empty border on the left separates the strip from the
        // preceding sibling control, so the strip clearly belongs to the
        // target on the right rather than reading as a divider.
        border = BorderFactory.createEmptyBorder(0, LEFT_INSET, 0, 0)
        add(gutter, BorderLayout.WEST)
        add(target, BorderLayout.CENTER)
        updateAccessibility()
        // Repaint after look-and-feel changes so that the gutter colour
        // tracks the new theme. We can't rely on DynamicStyle.onLaFChange
        // here because that registers a global listener whose lifetime
        // outlives the panel; instead, repaint when this panel is shown
        // and trust the regular updateUI cascade for theme switches.
        addAncestorListener(object : AncestorListener {
            override fun ancestorAdded(event: AncestorEvent?) {
                gutter.repaint()
            }
            override fun ancestorRemoved(event: AncestorEvent?) = Unit
            override fun ancestorMoved(event: AncestorEvent?) = Unit
        })
    }

    override fun updateUI() {
        super.updateUI()
        // updateUI() is called from the JPanel super-constructor before our
        // own fields have been assigned, hence the null check.
        @Suppress("SENSELESS_COMPARISON")
        if (gutter != null) {
            // Triggered by the look-and-feel cascade; re-pick the gutter colour.
            gutter.repaint()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        if (enabled == isEnabled) return
        super.setEnabled(enabled)
        // Repaint to pick up the muted disabled colour. JComponent.setEnabled
        // already schedules a repaint on the panel itself, but the strip is
        // a child component whose paintComponent reads our enabled flag, so
        // we forward the repaint explicitly.
        gutter.repaint()
    }

    private fun lookupGutterColor(enabled: Boolean): Color {
        val base = lookupBaseGutterColor()
        if (enabled) {
            return base
        }
        for (key in DISABLED_GUTTER_COLOR_KEYS) {
            UIManager.getColor(key)?.let { return it }
        }
        // Fallback: same hue as the enabled colour, but semi-transparent so
        // the strip still reads as "modified" while clearly muted.
        return Color(base.red, base.green, base.blue, DISABLED_ALPHA)
    }

    private fun lookupBaseGutterColor(): Color {
        for (key in GUTTER_COLOR_KEYS) {
            UIManager.getColor(key)?.let { return it }
        }
        return FALLBACK_COLOR
    }

    private fun updateAccessibility() {
        // Screen readers can pick this up even when the colour is invisible
        // to colour-blind users, satisfying the "no information by colour
        // alone" accessibility requirement.
        accessibleContext?.accessibleDescription = if (modifiedFlag) {
            "modified"
        } else {
            null
        }
    }
}

/**
 * Wraps the receiver in a [ModifiedGutter]. The gutter is initially
 * not modified; callers must drive [ModifiedGutter.isModified] explicitly
 * (typically from a property-change listener on the wrapped component).
 *
 * @since 6.0.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public fun <T : JComponent> T.withModifiedGutter(): ModifiedGutter = ModifiedGutter(this)
