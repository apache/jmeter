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
import java.awt.CardLayout
import java.awt.Container
import java.awt.Dimension
import java.awt.Insets

/**
 * An alternative [CardLayout] that computes the size based on the currently visible component only.
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public class CardLayoutWithSizeOfCurrentVisibleElement : CardLayout() {
    override fun preferredLayoutSize(parent: Container): Dimension {
        synchronized(parent.treeLock) {
            val current = parent.components.firstOrNull { it.isVisible } ?: return super.preferredLayoutSize(parent)
            return addInsets(current.preferredSize, parent.insets)
        }
    }

    override fun minimumLayoutSize(parent: Container): Dimension {
        synchronized(parent.treeLock) {
            val current = parent.components.firstOrNull { it.isVisible } ?: return super.minimumLayoutSize(parent)
            return addInsets(current.minimumSize, parent.insets)
        }
    }

    private fun addInsets(size: Dimension, insets: Insets) = Dimension(
        size.width + insets.left + insets.right + hgap * 2,
        size.height + insets.top + insets.bottom + vgap * 2
    )

    override fun next(parent: Container) {
        super.next(parent)
        // Force re-layout since the newly visible element might have different dimensions
        parent.revalidate()
    }

    override fun previous(parent: Container) {
        super.previous(parent)
        // Force re-layout since the newly visible element might have different dimensions
        parent.revalidate()
    }

    override fun first(parent: Container) {
        super.first(parent)
        // Force re-layout since the newly visible element might have different dimensions
        parent.revalidate()
    }

    override fun last(parent: Container) {
        super.last(parent)
        // Force re-layout since the newly visible element might have different dimensions
        parent.revalidate()
    }

    override fun show(parent: Container, name: String) {
        super.show(parent, name)
        // Force re-layout since the newly visible element might have different dimensions
        parent.revalidate()
    }
}
