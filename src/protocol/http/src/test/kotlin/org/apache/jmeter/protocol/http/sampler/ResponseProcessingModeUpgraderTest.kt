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

import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.ResponseProcessingMode
import org.apache.jmeter.testelement.property.BooleanProperty
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ResponseProcessingModeUpgraderTest {
    private val upgrader = ResponseProcessingModeUpgrader()
    private val md5 = "HTTPSampler.md5"
    private val mode = HTTPSamplerBaseSchema.INSTANCE.responseProcessingMode.name

    @Test
    fun md5TrueBecomesChecksumDecodedMd5() {
        val element = HTTPSamplerProxy()
        element.setProperty(BooleanProperty(md5, true))

        assertTrue(upgrader.upgrade(element))
        assertEquals(ResponseProcessingMode.CHECKSUM_DECODED_MD5.resourceKey, element.getPropertyAsString(mode))
        assertEquals("", element.getPropertyAsString(md5), "the legacy md5 property must be removed")
        assertFalse(upgrader.upgrade(element), "a second run is a no-op (idempotent)")
    }

    @Test
    fun md5FalseBecomesStoreCompressed() {
        val element = HTTPSamplerProxy()
        element.setProperty(BooleanProperty(md5, false))

        assertTrue(upgrader.upgrade(element))
        assertEquals(ResponseProcessingMode.STORE_COMPRESSED.resourceKey, element.getPropertyAsString(mode))
        assertEquals("", element.getPropertyAsString(md5))
    }

    @Test
    fun elementWithoutMd5IsUntouched() {
        val element = HTTPSamplerProxy()
        assertFalse(upgrader.upgrade(element))
        assertEquals("", element.getPropertyAsString(mode))
    }

    @Test
    fun existingModeIsKeptAndLegacyMd5Removed() {
        val element = HTTPSamplerProxy()
        element.setProperty(mode, ResponseProcessingMode.FETCH_AND_DISCARD.resourceKey)
        element.setProperty(BooleanProperty(md5, true))

        assertTrue(upgrader.upgrade(element))
        assertEquals(
            ResponseProcessingMode.FETCH_AND_DISCARD.resourceKey,
            element.getPropertyAsString(mode),
            "an explicit mode must win over the legacy md5"
        )
        assertEquals("", element.getPropertyAsString(md5))
    }
}
