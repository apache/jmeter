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

package org.apache.jmeter.extractor

import org.apache.jmeter.testelement.AbstractScopedTestElementSchema
import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor
import org.apache.jmeter.testelement.schema.IntegerPropertyDescriptor
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apiguardian.api.API

/**
 * Lists properties of a [RegexExtractor].
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public abstract class RegexExtractorSchema : AbstractScopedTestElementSchema() {
    public companion object INSTANCE : RegexExtractorSchema()

    public val matchTarget: StringPropertyDescriptor<RegexExtractorSchema>
    // Note: useHeaders is a historic naming, false means "use body for the match"
        by string("RegexExtractor.useHeaders", default = "false")

    public val regularExpression: StringPropertyDescriptor<RegexExtractorSchema>
        by string("RegexExtractor.regex")

    public val referenceName: StringPropertyDescriptor<RegexExtractorSchema>
        by string("RegexExtractor.refname")

    public val matchNumber: IntegerPropertyDescriptor<RegexExtractorSchema>
        by int("RegexExtractor.match_number")

    public val default: StringPropertyDescriptor<RegexExtractorSchema>
        by string("RegexExtractor.default")

    public val defaultIsEmpty: BooleanPropertyDescriptor<RegexExtractorSchema>
        by boolean("RegexExtractor.default_empty_value", default = false)

    public val template: StringPropertyDescriptor<RegexExtractorSchema>
        by string("RegexExtractor.template")
}
