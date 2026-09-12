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

import com.google.auto.service.AutoService
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase.ResponseProcessingMode
import org.apache.jmeter.save.TestElementUpgrader
import org.apache.jmeter.testelement.TestElement
import org.apiguardian.api.API

/**
 * Upgrades the legacy `HTTPSampler.md5` boolean property to `HTTPSampler.responseProcessingMode`.
 *
 * It keys off the property name, not the element class, so it upgrades both HTTP samplers and HTTP
 * Request Defaults (a `ConfigTestElement`). `md5=true` becomes the decoded-MD5 checksum mode (the
 * historical "Save as MD5" behaviour); `md5=false` becomes "store the response", which keeps it as
 * an explicit choice that still overrides a value inherited from HTTP Request Defaults.
 *
 * @since 6.0.0
 */
@AutoService(TestElementUpgrader::class)
@API(status = API.Status.INTERNAL, since = "6.0.0")
public class ResponseProcessingModeUpgrader : TestElementUpgrader {
    override fun upgrade(element: TestElement): Boolean {
        val schema = HTTPSamplerBaseSchema.INSTANCE

        @Suppress("DEPRECATION")
        val md5 = element.getPropertyOrNull(schema.storeAsMD5)?.booleanValue ?: return false
        // Keep an already-set mode (idempotent, and a newer property wins over the legacy flag).
        if (element.getPropertyOrNull(schema.responseProcessingMode.name) == null) {
            val mode = if (md5) {
                ResponseProcessingMode.CHECKSUM_DECODED_MD5
            } else {
                ResponseProcessingMode.STORE_COMPRESSED
            }
            element[schema.responseProcessingMode] = mode.resourceKey
        }
        @Suppress("DEPRECATION")
        element.removeProperty(schema.storeAsMD5)
        return true
    }
}
