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

package org.apache.jmeter.testelement.property

import org.apache.jmeter.control.gui.TestPlanGui
import org.apache.jmeter.testelement.AbstractTestElement
import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.schema.PropertiesAccessor
import org.apache.jmeter.threads.ThreadGroupSchema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class JMeterElementSchemaTest {
    abstract class WarpDriveElementSchema : TestElementSchema() {
        companion object INSTANCE : WarpDriveElementSchema()

        val warpFactor by int("WarpDriveElement.warpFactor", default = 7)
    }

    open class WarpDriveElement : AbstractTestElement() {
        override val schema: WarpDriveElementSchema
            get() = WarpDriveElementSchema
        override val props: PropertiesAccessor<WarpDriveElement, WarpDriveElementSchema>
            get() = PropertiesAccessor(this, schema)
    }

    @Test
    fun `getPropertyOrNull returns null for unset props`() {
        val warpDrive = WarpDriveElement()
        assertNull(warpDrive.getPropertyOrNull(warpDrive.schema.warpFactor)) {
            "${WarpDriveElementSchema.warpFactor} should be null for newly created element, getPropertyOrNull(PropertyDescriptor)"
        }
        assertNull(warpDrive.getPropertyOrNull(warpDrive.schema.warpFactor.name)) {
            "${WarpDriveElementSchema.warpFactor} should be null for newly created element, getPropertyOrNull(String)"
        }
    }

    @Test
    fun `get returns default value`() {
        val warpDrive = WarpDriveElement()
        assertGetWarpFactor(7, warpDrive, "element is empty, so default value expected")
    }

    @Test
    fun `set modifies value`() {
        val warpDrive = WarpDriveElement()
        warpDrive[warpDrive.schema.warpFactor] = 8

        assertGetWarpFactor(8, warpDrive, "value was modified with [warpFactor] = 8")
    }

    @Test
    fun `props set modifies value`() {
        val warpDrive = WarpDriveElement()
        warpDrive.props {
            it[warpFactor] = 8
        }

        assertGetWarpFactor(8, warpDrive, "value was modified with props { it[warpFactor] = 8 }")
    }

    @Test
    fun `property descriptor equals`() {
        assertEquals(TestElementSchema.name, ThreadGroupSchema.name) {
            "TestElementSchema.name and ThreadGroupSchema.name should be equal"
        }

        assertNotEquals(TestElementSchema.name, TestElementSchema.comments) {
            "TestElementSchema.name and TestElementSchema.comments should NOT be equal"
        }
    }

    @Test
    fun `test string setter`() {
        val warpDrive = WarpDriveElement()
        warpDrive.props {
            it[warpFactor] = "\${hello}"
        }
        assertEquals("\${hello}", warpDrive.getString(warpDrive.schema.warpFactor)) {
            "Int property should support get and set with String value for expressions purposes"
        }
    }

    private fun assertGetWarpFactor(expected: Int, warpDrive: WarpDriveElement, message: String) {
        assertEquals(expected, warpDrive[warpDrive.schema.warpFactor]) {
            "get(warpFactor): ${WarpDriveElementSchema.warpFactor}, $message"
        }
        assertEquals(expected, warpDrive.props[ { warpDrive.schema.warpFactor }]) {
            "props.get[{warpFactor}]: ${WarpDriveElementSchema.warpFactor}, $message"
        }
        assertEquals(expected.toString(), warpDrive.getString(warpDrive.schema.warpFactor)) {
            "getString(warpFactor): ${WarpDriveElementSchema.warpFactor}, $message"
        }
    }

    @Suppress("UNUSED_VARIABLE", "ReplaceGetOrSet")
    fun `compilation succeeds`() {
        // Below code does not make much sense, and it tests different styles of using the properties
        lateinit var base: TestElement
        lateinit var warpDrive: WarpDriveElement

        base.props {
            it[name] = "test"
            // Does not compile
            // it[WarpDriveElementSchema.warpFactor] = ""
        }
        warpDrive.props[WarpDriveElementSchema.warpFactor] = 8
        warpDrive.props[WarpDriveElementSchema.name] = "true"
        warpDrive.props[ { name }] = "true"
        warpDrive.props.set({ warpFactor }, "true")

        warpDrive.props {
            it[warpFactor] = 5
            it[name] = "test"
        }

        warpDrive.props[WarpDriveElementSchema.guiClass] = TestPlanGui::class
        warpDrive.props[WarpDriveElementSchema.guiClass] = TestPlanGui::class.java
        val gc = warpDrive.props[WarpDriveElementSchema.guiClass]
        // ok: can't pass non-GUI class to guiClass property
        // warpDrive.props[WarpDriveElementSchema.guiClass] = WarpDriveElement::class
        val x: Int = warpDrive.props[WarpDriveElementSchema.warpFactor]
        val y: Int = warpDrive.props[ { warpFactor }]
        val z: String = warpDrive.props[WarpDriveElementSchema.name]

        WarpDriveElementSchema.guiClass.getString(warpDrive)
        val guiClassAsString1 = warpDrive.props.getString { guiClass }
        val guiClassAsString2 = warpDrive.props.getString(WarpDriveElementSchema.guiClass)

        // ok: Fails to compile since TestElement does not have WarpDriveElement::warpFactor property
        // base.props[WarpDriveElement.warpFactor] = "true"
        base.props[WarpDriveElementSchema.name] = "true"
    }
}
