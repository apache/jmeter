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

package org.apache.jmeter.protocol.http.util

import org.apache.jmeter.config.ArgumentSchema
import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apiguardian.api.API

/**
 * Lists properties of a [HTTPArgument].
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public abstract class HTTPArgumentSchema : ArgumentSchema() {
    public companion object INSTANCE : HTTPArgumentSchema()

    public val alwaysEncode: BooleanPropertyDescriptor<HTTPArgumentSchema>
        by boolean("HTTPArgument.always_encode")

    public val useEquals: BooleanPropertyDescriptor<HTTPArgumentSchema>
        by boolean("HTTPArgument.use_equals")

    public val contentType: StringPropertyDescriptor<HTTPArgumentSchema>
        by string("HTTPArgument.content_type", "text/plain")
}
