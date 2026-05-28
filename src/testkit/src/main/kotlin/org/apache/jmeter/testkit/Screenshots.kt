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

package org.apache.jmeter.testkit

import java.awt.image.BufferedImage
import java.io.IOException
import java.io.UncheckedIOException
import java.lang.reflect.InvocationTargetException
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.io.path.createParentDirectories

/**
 * Renders a Swing component into an off-screen image so tests (and
 * documentation tooling) can capture a screenshot without a human driving
 * the GUI.
 *
 * The component is briefly attached to an undecorated, never-shown
 * [JFrame] so that it is realized (gets a peer) and laid out at its
 * preferred size, then painted with [JComponent.printAll]. Rendering always
 * runs on the Event Dispatch Thread.
 *
 * Requires a graphics environment: it throws [java.awt.HeadlessException]
 * when run head-less, so on CI run it under a virtual framebuffer such as
 * `Xvfb`. Set the desired Look and Feel before calling if a specific theme
 * is needed — otherwise the current default LaF is used.
 *
 * @since 6.0.0
 */
public object Screenshots {
    /**
     * Renders the given component into a new ARGB image at its preferred
     * size. The component is re-parented into a throwaway frame.
     */
    @JvmStatic
    public fun render(component: JComponent): BufferedImage {
        lateinit var image: BufferedImage
        runOnEventDispatchThread { image = renderOnEdt(component) }
        return image
    }

    /**
     * Renders the component and writes it to [target] as a PNG, creating
     * parent directories as needed.
     *
     * @return [target], for chaining
     */
    @JvmStatic
    public fun save(component: JComponent, target: Path): Path {
        val image = render(component)
        try {
            target.toAbsolutePath().createParentDirectories()
            ImageIO.write(image, "png", target.toFile())
        } catch (e: IOException) {
            throw UncheckedIOException("Unable to write screenshot to $target", e)
        }
        return target
    }

    private fun renderOnEdt(component: JComponent): BufferedImage {
        val frame = JFrame()
        frame.isUndecorated = true
        try {
            frame.contentPane.add(component)
            frame.pack()
            val width = component.width.coerceAtLeast(1)
            val height = component.height.coerceAtLeast(1)
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g = image.createGraphics()
            try {
                component.printAll(g)
            } finally {
                g.dispose()
            }
            return image
        } finally {
            frame.dispose()
        }
    }

    private fun runOnEventDispatchThread(block: Runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            block.run()
            return
        }
        try {
            SwingUtilities.invokeAndWait(block)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while rendering screenshot", e)
        } catch (e: InvocationTargetException) {
            throw IllegalStateException("Failed to render screenshot", e.cause)
        }
    }
}
