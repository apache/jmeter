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

import org.apache.jmeter.testelement.AbstractTestElement
import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.schema.BasePropertyGroupSchema
import org.apache.jmeter.testelement.schema.BaseTestElementSchema
import org.apache.jmeter.testelement.schema.PropertiesAccessor
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PropertyGroupSchemaTest {
    open class ProxyConfigurationGroup<Schema : BaseTestElementSchema>(
        prefix: String
    ) : BasePropertyGroupSchema<Schema>() {
        val hostname: StringPropertyDescriptor<Schema> by string("$prefix.hostname")
        val port by int("$prefix.port")
    }
    open class WarpDriveUrlGroup<Schema : WarpDriveElementSchema> : BasePropertyGroupSchema<Schema>() {
        val hostname by string("WarpDriveElement.hostname")
        val port by int("WarpDriveElement.port")

        val proxy by ProxyConfigurationGroup<Schema>("WarpDriveElement.proxy")
    }

    open class WarpDriveElementSchema : TestElementSchema() {
        companion object INSTANCE : WarpDriveElementSchema()

        val warpFactor by int("WarpDriveElement.warpFactor", default = 7)

        val mainUrl by WarpDriveUrlGroup()
    }

    open class WarpDriveElement : AbstractTestElement() {
        override val schema: WarpDriveElementSchema
            get() = WarpDriveElementSchema
        override val props: PropertiesAccessor<WarpDriveElement, WarpDriveElementSchema>
            get() = PropertiesAccessor(this, schema)
    }

    @Test
    fun `group parameters`() {
        val warpDrive = WarpDriveElement()
        warpDrive.props {
            it[mainUrl.hostname] = "localhost"
            it[mainUrl.port] = 443
        }
        assertEquals("localhost", warpDrive[warpDrive.schema.mainUrl.hostname]) {
            "warpDrive[warpDrive.schema.mainUrl.hostname]"
        }
        assertEquals(443, warpDrive[warpDrive.schema.mainUrl.port]) {
            "warpDrive[warpDrive.schema.mainUrl.port]"
        }
    }

    @Test
    fun `nested group parameters`() {
        val warpDrive = WarpDriveElement()
        warpDrive.props {
            mainUrl {
                proxy {
                    it[hostname] = "proxyHost"
                    it[port] = 8080
                }
            }
            it[mainUrl.hostname] = "localhost"
            it[mainUrl.port] = 443
        }
        assertEquals("localhost", warpDrive[warpDrive.schema.mainUrl.hostname]) {
            "warpDrive[warpDrive.schema.mainUrl.hostname]"
        }
        assertEquals(443, warpDrive[warpDrive.schema.mainUrl.port]) {
            "warpDrive[warpDrive.schema.mainUrl.port]"
        }
        assertEquals("proxyHost", warpDrive[warpDrive.schema.mainUrl.proxy.hostname]) {
            "warpDrive[warpDrive.schema.mainUrl.proxy.hostname]"
        }
        assertEquals(8080, warpDrive[warpDrive.schema.mainUrl.proxy.port]) {
            "warpDrive[warpDrive.schema.proxy.port]"
        }
    }

    @Test
    fun `schema contains properties from all the subgroups`() {
        val schema = WarpDriveElementSchema
        assertEquals(
            """
            TestElement.name=StringPropertyDescriptor(shortName=name, name=TestElement.name, defaultValue=null)
            TestPlan.comments=StringPropertyDescriptor(shortName=comments, name=TestPlan.comments, defaultValue=null)
            TestElement.gui_class=ClassPropertyDescriptor(shortName=guiClass, klass=interface org.apache.jmeter.gui.JMeterGUIComponent, name=TestElement.gui_class, defaultValue=null)
            TestElement.test_class=ClassPropertyDescriptor(shortName=testClass, klass=class java.lang.Object, name=TestElement.test_class, defaultValue=null)
            TestElement.enabled=BooleanPropertyDescriptor(shortName=enabled, name=TestElement.enabled, defaultValue=true)
            WarpDriveElement.warpFactor=IntegerPropertyDescriptor(shortName=warpFactor, name=WarpDriveElement.warpFactor, defaultValue=7)
            WarpDriveElement.hostname=StringPropertyDescriptor(shortName=hostname, name=WarpDriveElement.hostname, defaultValue=null)
            WarpDriveElement.port=IntegerPropertyDescriptor(shortName=port, name=WarpDriveElement.port, defaultValue=null)
            WarpDriveElement.proxy.hostname=StringPropertyDescriptor(shortName=hostname, name=WarpDriveElement.proxy.hostname, defaultValue=null)
            WarpDriveElement.proxy.port=IntegerPropertyDescriptor(shortName=port, name=WarpDriveElement.proxy.port, defaultValue=null)
            """.trimIndent().replace("\r\n", "\n"),
            schema.properties.asSequence().joinToString("\n")
        ) {
            "warpDrive.schema.properties"
        }
    }

    @Test
    fun `full property path`() {
        val schema = WarpDriveElementSchema
        assertEquals(
            """
            TestElement.name = []
            TestPlan.comments = []
            TestElement.gui_class = []
            TestElement.test_class = []
            TestElement.enabled = []
            WarpDriveElement.warpFactor = []
            WarpDriveElement.hostname = [mainUrl]
            WarpDriveElement.port = [mainUrl]
            WarpDriveElement.proxy.hostname = [mainUrl, proxy]
            WarpDriveElement.proxy.port = [mainUrl, proxy]
            """.trimIndent(),
            schema.properties.asSequence().joinToString("\n") {
                it.key + " = " + schema.getGroupPath(it.value)
            }
        )
    }

    @Test
    fun `mainUrl getFullPathOrNull port`() {
        val schema = WarpDriveElementSchema
        assertEquals(
            "[]",
            schema.mainUrl.getGroupPath(schema.mainUrl.port).toString()
        )
    }

    @Test
    fun `mainUrl getFullPathOrNull proxy port`() {
        val schema = WarpDriveElementSchema
        assertEquals(
            "[proxy]",
            schema.mainUrl.getGroupPath(schema.mainUrl.proxy.port).toString()
        )
    }
}
