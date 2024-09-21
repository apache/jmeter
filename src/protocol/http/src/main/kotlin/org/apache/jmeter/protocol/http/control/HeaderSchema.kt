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

package org.apache.jmeter.protocol.http.control

import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apiguardian.api.API

/**
 * Lists properties of a [Header].
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public abstract class HeaderSchema : TestElementSchema() {
    public companion object INSTANCE : HeaderSchema()

    public val headerName: StringPropertyDescriptor<HeaderSchema>
        by string("Header.name")

    public val value: StringPropertyDescriptor<HeaderSchema>
        by string("Header.value")
}
