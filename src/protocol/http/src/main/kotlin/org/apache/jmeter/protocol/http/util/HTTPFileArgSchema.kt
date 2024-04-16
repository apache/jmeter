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

import org.apache.jmeter.config.ConfigTestElementSchema
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apiguardian.api.API

/**
 * Lists properties of a [HTTPFileArg].
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public abstract class HTTPFileArgSchema : ConfigTestElementSchema() {
    public companion object INSTANCE : HTTPFileArgSchema()

    /** File's path */
    public val path: StringPropertyDescriptor<HTTPFileArgSchema>
        by string("File.path")

    /** Name of the parameter that stores the file */
    public val parameterName: StringPropertyDescriptor<HTTPFileArgSchema>
        by string("File.paramname")

    /** File's mimetype */
    public val mimeType: StringPropertyDescriptor<HTTPFileArgSchema>
        by string("File.mimetype")
}
