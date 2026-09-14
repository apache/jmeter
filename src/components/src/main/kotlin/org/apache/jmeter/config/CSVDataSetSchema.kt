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

package org.apache.jmeter.config

import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apiguardian.api.API

/**
 * Lists properties of a [CSVDataSet].
 * @since 6.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "6.0.0")
public abstract class CSVDataSetSchema : ConfigTestElementSchema() {
    public companion object INSTANCE : CSVDataSetSchema()

    public val filename: StringPropertyDescriptor<CSVDataSetSchema>
        by string("filename", default = "")

    public val fileEncoding: StringPropertyDescriptor<CSVDataSetSchema>
        by string("fileEncoding", default = "")

    public val variableNames: StringPropertyDescriptor<CSVDataSetSchema>
        by string("variableNames", default = "")

    public val ignoreFirstLine: BooleanPropertyDescriptor<CSVDataSetSchema>
        by boolean("ignoreFirstLine", default = false)

    public val delimiter: StringPropertyDescriptor<CSVDataSetSchema>
        by string("delimiter", default = ",")

    public val quotedData: BooleanPropertyDescriptor<CSVDataSetSchema>
        by boolean("quotedData", default = false)

    public val recycle: BooleanPropertyDescriptor<CSVDataSetSchema>
        by boolean("recycle", default = true)

    public val stopThread: BooleanPropertyDescriptor<CSVDataSetSchema>
        by boolean("stopThread", default = false)

    public val shareMode: StringPropertyDescriptor<CSVDataSetSchema>
        by string("shareMode", default = "shareMode.all")
}
