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

package org.apache.jmeter.threads

import org.apache.jmeter.control.Controller
import org.apache.jmeter.testelement.TestElementSchema
import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor
import org.apache.jmeter.testelement.schema.IntegerPropertyDescriptor
import org.apache.jmeter.testelement.schema.StringPropertyDescriptor
import org.apache.jmeter.testelement.schema.TestElementPropertyDescriptor
import org.apiguardian.api.API

/**
 * Lists properties of a [AbstractThreadGroup].
 * @see AbstractThreadGroup
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public abstract class AbstractThreadGroupSchema : TestElementSchema() {
    public companion object INSTANCE : AbstractThreadGroupSchema()

    public val numThreads: IntegerPropertyDescriptor<AbstractThreadGroupSchema>
        by int("ThreadGroup.num_threads")

    public val mainController: TestElementPropertyDescriptor<AbstractThreadGroupSchema, Controller>
        by testElement("ThreadGroup.main_controller")

    public val sameUserOnNextIteration: BooleanPropertyDescriptor<AbstractThreadGroupSchema>
        by boolean("ThreadGroup.same_user_on_next_iteration", default = true)

    // TODO: implement EnumPropertyDescriptor or TransformedPropertyDescriptor, so that we can use enum values
    //  in get and set
    public val onSampleError: StringPropertyDescriptor<AbstractThreadGroupSchema>
        by string("ThreadGroup.on_sample_error", default = AbstractThreadGroup.ON_SAMPLE_ERROR_CONTINUE)
}
