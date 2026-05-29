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

import org.apache.jmeter.testkit.Screenshots
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.GraphicsEnvironment
import java.awt.GridLayout
import java.nio.file.Files
import java.nio.file.Path
import javax.swing.BorderFactory
import javax.swing.JPanel

/**
 * Renders the name field with a lit modified gutter and writes a PNG to
 * `build/screenshots/`. Acts both as a smoke test for [Screenshots] and as
 * a way to eyeball the gutter without launching the full GUI. Skipped when
 * running head-less (no display).
 */
class GutterScreenshotDemoTest {
    @Test
    fun `render name panel with modified gutter`() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "needs a display to render")

        // Two name panels side by side on an opaque white background:
        // the top one is unmodified (gutter dark), the bottom one is
        // modified (gutter lit), so the strip is easy to compare.
        val unmodified = NamePanel().apply {
            setDefaultName("HTTP Request")
            name = "HTTP Request"
        }
        val modified = NamePanel().apply {
            setDefaultName("HTTP Request")
            name = "Login request"
        }

        val canvas = JPanel(GridLayout(2, 1, 0, 8)).apply {
            isOpaque = true
            background = Color.WHITE
            border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
            add(unmodified)
            add(modified)
        }
        canvas.size = canvas.preferredSize

        val out = Path.of("build", "screenshots", "name-gutter.png")
        Screenshots.save(canvas, out)

        assertTrue(Files.exists(out), "screenshot should be written to $out")
        assertTrue(Files.size(out) > 0, "screenshot should not be empty")
    }
}
