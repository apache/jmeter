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

package org.apache.jmeter.protocol.http.sampler

import org.apache.jmeter.config.ConfigTestElement
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.ResponseProcessingMode
import org.apache.jmeter.testelement.property.BooleanProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Verifies how `responseProcessingMode` is resolved: an explicit value wins, otherwise the legacy
 * `HTTPSampler.md5` property (own or inherited from HTTP Request Defaults) is used, otherwise the
 * default applies. This keeps old test plans working and preserves the boolean override semantics.
 */
class ResponseProcessingModeInheritanceTest {
    private val md5 = "HTTPSampler.md5"
    private val mode = HTTPSamplerBaseSchema.INSTANCE.responseProcessingMode.name

    @Test
    fun legacyMd5TrueResolvesToChecksumDecodedMd5() {
        val sampler = HTTPSamplerProxy()
        sampler.setProperty(BooleanProperty(md5, true))
        assertEquals(ResponseProcessingMode.CHECKSUM_DECODED_MD5, sampler.responseProcessingMode)
    }

    @Test
    fun legacyMd5FalseResolvesToStoreCompressed() {
        val sampler = HTTPSamplerProxy()
        sampler.setProperty(BooleanProperty(md5, false))
        assertEquals(ResponseProcessingMode.STORE_COMPRESSED, sampler.responseProcessingMode)
    }

    @Test
    fun unsetResolvesToStoreCompressed() {
        assertEquals(ResponseProcessingMode.STORE_COMPRESSED, HTTPSamplerProxy().responseProcessingMode)
    }

    @Test
    fun defaultsMd5TrueIsInheritedWhenSamplerHasNoSetting() {
        val defaults = ConfigTestElement()
        defaults.setProperty(BooleanProperty(md5, true))

        val sampler = HTTPSamplerProxy()
        sampler.addTestElement(defaults)

        assertEquals(
            ResponseProcessingMode.CHECKSUM_DECODED_MD5,
            sampler.responseProcessingMode,
            "a sampler without its own setting must inherit md5=true from HTTP Request Defaults"
        )
    }

    @Test
    fun samplerMd5FalseOverridesDefaultsMd5True() {
        val defaults = ConfigTestElement()
        defaults.setProperty(BooleanProperty(md5, true))

        val sampler = HTTPSamplerProxy()
        sampler.setProperty(BooleanProperty(md5, false))
        sampler.addTestElement(defaults)

        assertEquals(
            ResponseProcessingMode.STORE_COMPRESSED,
            sampler.responseProcessingMode,
            "an explicit md5=false on the sampler must override md5=true from the defaults"
        )
    }

    @Test
    fun samplerInheritsProcessingModeFromDefaults() {
        val defaults = ConfigTestElement()
        defaults.setProperty(mode, ResponseProcessingMode.FETCH_AND_DISCARD.resourceKey)

        val sampler = HTTPSamplerProxy()
        sampler.addTestElement(defaults)

        assertEquals(ResponseProcessingMode.FETCH_AND_DISCARD, sampler.responseProcessingMode)
    }

    @Test
    fun explicitModeWinsOverLegacyMd5() {
        val sampler = HTTPSamplerProxy()
        sampler.setProperty(BooleanProperty(md5, true))
        sampler.setProperty(mode, ResponseProcessingMode.FETCH_AND_DISCARD.resourceKey)

        assertEquals(ResponseProcessingMode.FETCH_AND_DISCARD, sampler.responseProcessingMode)
    }
}
