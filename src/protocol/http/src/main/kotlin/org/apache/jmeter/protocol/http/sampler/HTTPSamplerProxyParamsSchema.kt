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

import org.apache.jmeter.testelement.schema.BasePropertyGroupSchema
import org.apache.jmeter.testelement.schema.BaseTestElementSchema
import org.apache.jmeter.testelement.schema.IntegerPropertyDescriptor
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apiguardian.api.API

/**
 * Lists properties of a [HTTPSamplerBase].
 * @see HTTPSamplerBase
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public open class HTTPSamplerProxyParamsSchema<Schema : BaseTestElementSchema> : BasePropertyGroupSchema<Schema>() {
    public val scheme: StringPropertyDescriptor<Schema>
        by string("HTTPSampler.proxyScheme")

    public val host: StringPropertyDescriptor<Schema>
        by string("HTTPSampler.proxyHost")

    public val port: IntegerPropertyDescriptor<Schema>
        by int("HTTPSampler.proxyPort")

    public val username: StringPropertyDescriptor<Schema>
        by string("HTTPSampler.proxyUser")

    public val password: StringPropertyDescriptor<Schema>
        by string("HTTPSampler.proxyPass")
}
