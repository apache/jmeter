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

package org.apache.jmeter.protocol.http.control.gui

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.ResponseProcessingMode
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseSchema
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy
import org.apache.jmeter.testelement.property.BooleanProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Round-trips the response-processing-mode combo of [HttpTestSampleGui] through
 * `configure` and `modifyTestElement`.
 */
class HttpTestSampleGuiResponseProcessingModeTest {
    private val modePropertyName = HTTPSamplerBaseSchema.INSTANCE.responseProcessingMode.name
    private val md5PropertyName = "HTTPSampler.md5"

    private lateinit var gui: HttpTestSampleGui

    @BeforeEach
    fun setUp() {
        gui = HttpTestSampleGui()
    }

    @Test
    fun defaultEntryMapsToAbsentProperty() {
        // A fresh sampler has no responseProcessingMode property, so the combo shows "Default".
        gui.configure(HTTPSamplerProxy())

        val target = HTTPSamplerProxy()
        gui.modifyTestElement(target)

        assertEquals(
            "",
            target.getPropertyAsString(modePropertyName),
            "selecting 'Default' must leave responseProcessingMode absent"
        )
    }

    @Test
    fun concreteModeRoundTripsThroughResourceKey() {
        val source = HTTPSamplerProxy()
        source.setProperty(modePropertyName, ResponseProcessingMode.FETCH_AND_DISCARD.resourceKey)
        gui.configure(source)

        val target = HTTPSamplerProxy()
        gui.modifyTestElement(target)

        assertEquals(
            ResponseProcessingMode.FETCH_AND_DISCARD.resourceKey,
            target.getPropertyAsString(modePropertyName),
            "a concrete mode must round-trip as its resource key"
        )
        assertEquals(ResponseProcessingMode.FETCH_AND_DISCARD, target.responseProcessingMode)
    }

    @Test
    fun legacyMd5IsReflectedAndMigratedOnSave() {
        // An old test plan has only HTTPSampler.md5=true; the combo should show the checksum mode.
        val source = HTTPSamplerProxy()
        source.setProperty(BooleanProperty(md5PropertyName, true))
        gui.configure(source)

        val target = HTTPSamplerProxy()
        target.setProperty(BooleanProperty(md5PropertyName, true))
        gui.modifyTestElement(target)

        assertEquals(
            ResponseProcessingMode.CHECKSUM_DECODED_MD5.resourceKey,
            target.getPropertyAsString(modePropertyName),
            "legacy md5=true should be reflected and saved as the checksum mode"
        )
        assertEquals(
            "",
            target.getPropertyAsString(md5PropertyName),
            "the legacy md5 property should be dropped on save"
        )
    }
}
