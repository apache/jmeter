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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import javax.swing.JPopupMenu

class AbstractJMeterGuiComponentTest {
    @Test
    fun isEnabled() {
        val element = object : AbstractJMeterGuiComponent() {
            override fun getLabelResource(): String = "dummy_element_for_tests"

            override fun createTestElement(): TestElement = TODO()

            override fun modifyTestElement(element: TestElement?) = TODO()

            override fun createPopupMenu(): JPopupMenu = TODO()

            override fun getMenuCategories(): MutableCollection<String> = TODO()
        }

        assertEquals(true, element.isEnabled, "element.isEnabled after creation of the element")

        element.clearGui()
        assertEquals(true, element.isEnabled, "element.isEnabled after element.clearGui()")
    }
}
