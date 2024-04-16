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
    val warpDrive = WarpDriveElement()

    abstract class WarpDriveElementSchema : TestElementSchema() {
        companion object INSTANCE : WarpDriveElementSchema()

        val warpFactor by int("WarpDriveElement.warpFactor", default = 7)
        val turbo by boolean("WarpDriveElement.turbo")
        val description by string("WarpDriveElement.description")
    }

    open class WarpDriveElement : AbstractTestElement() {
        override val schema: WarpDriveElementSchema
            get() = WarpDriveElementSchema
        override val props: PropertiesAccessor<WarpDriveElement, WarpDriveElementSchema>
            get() = PropertiesAccessor(this, schema)
    }

    @Test
    fun `getPropertyOrNull returns null for unset props`() {
        assertGetWarpDescription(
            null,
            warpDrive,
            "${WarpDriveElementSchema.warpFactor} should be null for newly created element"
        )
        assertGetWarpTurbo(
            null,
            warpDrive,
            "${WarpDriveElementSchema.warpFactor} should be null for newly created element"
        )
        assertNull(warpDrive.getPropertyOrNull(warpDrive.schema.warpFactor)) {
            "${WarpDriveElementSchema.warpFactor} should be null for newly created element, getPropertyOrNull(PropertyDescriptor)"
        }
        assertNull(warpDrive.getPropertyOrNull(warpDrive.schema.warpFactor.name)) {
            "${WarpDriveElementSchema.warpFactor} should be null for newly created element, getPropertyOrNull(String)"
        }
    }

    @Test
    fun `get int returns default value`() {
        assertGetWarpFactor(7, warpDrive, "element is empty, so default value expected")
    }

    @Test
    fun `set int modifies value`() {
        warpDrive[warpDrive.schema.warpFactor] = 8

        assertGetWarpFactor(8, warpDrive, "value was modified with [warpFactor] = 8")
    }

    @Test
    fun `props set int modifies value`() {
        warpDrive.props {
            it[warpFactor] = 8
        }

        assertGetWarpFactor(8, warpDrive, "value was modified with props { it[warpFactor] = 8 }")
    }

    @Test
    fun `set string modifies value`() {
        var value = "new description"
        warpDrive[warpDrive.schema.description] = value
        assertGetWarpDescription(value, warpDrive, "value was modified with [description] = \"$value\"")

        value = ""
        warpDrive[warpDrive.schema.description] = value
        assertGetWarpDescription(value, warpDrive, "value was modified with [description] = \"$value\"")

        warpDrive[warpDrive.schema.description] = null
        assertGetWarpDescription(null, warpDrive, "value should be removed after [description] = null")
    }

    @Test
    fun `props set string modifies value`() {
        var value = "new description"
        warpDrive.props {
            it[description] = value
        }
        assertGetWarpDescription(value, warpDrive, "value was modified with props { it[description] = \"$value\" }")

        value = ""
        warpDrive.props {
            it[description] = value
        }
        assertGetWarpDescription(value, warpDrive, "value was modified with props { it[description] = \"$value\" }")

        warpDrive.props {
            it[description] = null
        }
        assertGetWarpDescription(null, warpDrive, "value should be removed after props { it[description] = null }")
    }

    @Test
    fun `set boolean modifies value`() {
        var value = true
        warpDrive[warpDrive.schema.turbo] = value
        assertGetWarpTurbo(value, warpDrive, "value was modified with [turbo] = \"$value\"")

        value = false
        warpDrive[warpDrive.schema.turbo] = value
        assertGetWarpTurbo(value, warpDrive, "value was modified with [turbo] = \"$value\"")

        warpDrive[warpDrive.schema.turbo] = null as Boolean?
        assertGetWarpTurbo(null, warpDrive, "value should be removed after [turbo] = null")
    }

    @Test
    fun `props set boolean modifies value`() {
        var value = true
        warpDrive.props {
            it[turbo] = value
        }
        assertGetWarpTurbo(value, warpDrive, "value was modified with props { it[turbo] = \"$value\" }")

        value = false
        warpDrive.props {
            it[turbo] = value
        }
        assertGetWarpTurbo(value, warpDrive, "value was modified with props { it[turbo] = \"$value\" }")

        warpDrive.props {
            it[turbo] = null as Boolean?
        }
        assertGetWarpTurbo(null, warpDrive, "value should be removed after props { it[turbo] = null }")
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
        assertEquals(expected, warpDrive.props[ { warpFactor }]) {
            "props.get[{warpFactor}]: ${WarpDriveElementSchema.warpFactor}, $message"
        }
        assertEquals(expected.toString(), warpDrive.getString(warpDrive.schema.warpFactor)) {
            "getString(warpFactor): ${WarpDriveElementSchema.warpFactor}, $message"
        }
    }

    private fun assertGetWarpDescription(expected: String?, warpDrive: WarpDriveElement, message: String) {
        assertEquals(expected ?: "", warpDrive[warpDrive.schema.description]) {
            "get(description): ${WarpDriveElementSchema.description}, $message"
        }
        assertEquals(expected ?: "", warpDrive.props[ { description }]) {
            "props.get[{description}]: ${WarpDriveElementSchema.description}, $message"
        }
        assertEquals(expected ?: "", warpDrive.getString(warpDrive.schema.description)) {
            "getString(description): ${WarpDriveElementSchema.description}, $message"
        }
        assertEquals(expected ?: "", warpDrive.getPropertyAsString(warpDrive.schema.description.name)) {
            "getPropertyAsString(description): ${WarpDriveElementSchema.description}, $message"
        }
        if (expected == null) {
            assertNull(warpDrive.getPropertyOrNull(warpDrive.schema.description)) {
                "getPropertyOrNull(description) should return null for absent property, ${WarpDriveElementSchema.description}, $message"
            }
            assertNull(warpDrive.getPropertyOrNull(warpDrive.schema.description.name)) {
                "getPropertyOrNull(description.name) should return null for absent property, ${WarpDriveElementSchema.description}, $message"
            }
        }
    }

    private fun assertGetWarpTurbo(expected: Boolean?, warpDrive: WarpDriveElement, message: String) {
        assertEquals(expected ?: false, warpDrive[warpDrive.schema.turbo]) {
            "get(turbo): ${WarpDriveElementSchema.turbo}, $message"
        }
        assertEquals(expected ?: false, warpDrive.props[ { turbo }]) {
            "props.get[{turbo}]: ${WarpDriveElementSchema.turbo}, $message"
        }
        assertEquals((expected ?: false).toString(), warpDrive.getString(warpDrive.schema.turbo)) {
            "getString(turbo): ${WarpDriveElementSchema.turbo}, $message"
        }
        assertEquals(expected?.toString() ?: "", warpDrive.getPropertyAsString(warpDrive.schema.turbo.name)) {
            "getPropertyAsString(turbo): ${WarpDriveElementSchema.turbo}, $message"
        }
        if (expected == null) {
            assertNull(warpDrive.getPropertyOrNull(warpDrive.schema.turbo)) {
                "getPropertyOrNull(turbo) should return null for absent property, ${WarpDriveElementSchema.turbo}, $message"
            }
            assertNull(warpDrive.getPropertyOrNull(warpDrive.schema.turbo.name)) {
                "getPropertyOrNull(turbo.name) should return null for absent property, ${WarpDriveElementSchema.turbo}, $message"
            }
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
