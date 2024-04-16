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

package org.apache.jmeter.gui.util

import org.apache.jmeter.junit.JMeterTestCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class MenuFactoryTest : JMeterTestCase() {

    @Test
    fun `ensure each menu has something in it`() {
        assertEquals(12, MenuFactory.getMenuMap().size, "MenuFactory.getMenuMap().size")
        MenuFactory.getMenuMap().forEach { (group, items) ->
            assertNotEquals(0, items.size, "MenuFactory.getMenuMap()[$group].size")
        }
    }

    @Test
    fun `default add menu has expected item count`() {
        assertEquals(6 + 3, MenuFactory.createDefaultAddMenu().itemCount, "items + separators")
    }
}
