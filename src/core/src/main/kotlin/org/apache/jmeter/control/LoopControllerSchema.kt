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

package org.apache.jmeter.control

import org.apache.jmeter.testelement.schema.BooleanPropertyDescriptor
import org.apache.jmeter.testelement.schema.IntegerPropertyDescriptor
import org.apiguardian.api.API

/**
 * Lists properties of a [LoopController].
 * @since 5.6
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.6")
public abstract class LoopControllerSchema : GenericControllerSchema() {
    public companion object INSTANCE : LoopControllerSchema()

    public val loops: IntegerPropertyDescriptor<LoopControllerSchema>
        by int("LoopController.loops")

    /**
     * In spite of the name, this is actually used to determine if the loop controller is repeatable.
     *
     * The value is only used in nextIsNull() when the loop end condition has been detected:
     * If forever==true, then it calls resetLoopCount(), otherwise it calls setDone(true).
     *
     * Loop Controllers always set forever=true, so that they will be executed next time
     * the parent invokes them.
     *
     * Thread Group sets the value false, so nextIsNull() sets done, and the Thread Group will not be repeated.
     * However, it's not clear that a Thread Group could ever be repeated.
     */
    public val continueForever: BooleanPropertyDescriptor<LoopControllerSchema>
        by boolean("LoopController.continue_forever")
}
