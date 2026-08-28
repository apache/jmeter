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
import org.apache.jmeter.save.TestElementUpgraders
import org.apache.jmeter.testelement.property.BooleanProperty
import org.apache.jorphan.collections.ListedHashTree
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * End-to-end check of the [TestElementUpgraders] driver with the ServiceLoader-registered
 * [ResponseProcessingModeUpgrader], mirroring what SaveService runs after loading a test plan.
 */
class ResponseProcessingModeUpgradeOnLoadTest {
    private val md5 = "HTTPSampler.md5"
    private val mode = HTTPSamplerBaseSchema.INSTANCE.responseProcessingMode.name

    @Test
    fun upgradesBothSamplerAndHttpRequestDefaults() {
        val sampler = HTTPSamplerProxy().apply { setProperty(BooleanProperty(md5, true)) }
        // HTTP Request Defaults are a ConfigTestElement; the upgrader keys off the property name,
        // so it must be upgraded too.
        val defaults = ConfigTestElement().apply { setProperty(BooleanProperty(md5, false)) }

        val tree = ListedHashTree()
        tree.add(sampler)
        tree.add(defaults)

        TestElementUpgraders.upgrade(tree)

        assertEquals(ResponseProcessingMode.CHECKSUM_DECODED_MD5.resourceKey, sampler.getPropertyAsString(mode))
        assertEquals("", sampler.getPropertyAsString(md5), "the sampler's legacy md5 must be removed")

        assertEquals(ResponseProcessingMode.STORE_COMPRESSED.resourceKey, defaults.getPropertyAsString(mode))
        assertEquals("", defaults.getPropertyAsString(md5), "the defaults' legacy md5 must be removed")
    }
}
