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

package org.apache.jmeter.protocol.http.config.gui

import org.apache.jmeter.config.ConfigTestElement
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.ResponseProcessingMode
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseSchema
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Round-trips the response-processing-mode combo of [HttpDefaultsGui] through
 * `configure` and `modifyTestElement`.
 */
class HttpDefaultsGuiResponseProcessingModeTest {
    private val modePropertyName = HTTPSamplerBaseSchema.INSTANCE.responseProcessingMode.name

    private lateinit var gui: HttpDefaultsGui

    @BeforeEach
    fun setUp() {
        gui = HttpDefaultsGui()
    }

    // createTestElement() yields a fully populated defaults element (with the URL config the GUI expects).
    private fun newDefaults() = gui.createTestElement() as ConfigTestElement

    @Test
    fun defaultEntryMapsToAbsentProperty() {
        // A fresh defaults element has no responseProcessingMode property, so the combo shows "Default".
        gui.configure(newDefaults())

        val target = newDefaults()
        gui.modifyTestElement(target)

        assertEquals(
            "",
            target.getPropertyAsString(modePropertyName),
            "selecting 'Default' must leave responseProcessingMode absent"
        )
    }

    @Test
    fun concreteModeRoundTripsThroughResourceKey() {
        val source = newDefaults()
        source.setProperty(modePropertyName, ResponseProcessingMode.FETCH_AND_DISCARD.resourceKey)
        gui.configure(source)

        val target = newDefaults()
        gui.modifyTestElement(target)

        assertEquals(
            ResponseProcessingMode.FETCH_AND_DISCARD.resourceKey,
            target.getPropertyAsString(modePropertyName),
            "a concrete mode must round-trip as its resource key"
        )
    }
}
