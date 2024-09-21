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

package org.apache.jmeter.protocol.java.sampler

import org.apache.jmeter.engine.util.TestElementPropertyTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BeanShellSamplerTest {
    @Test
    fun `getScript executes only once`() {
        val sampler = BeanShellSampler().apply {
            name = "BeanShell Sampler"
            props {
                it[script] = """ResponseMessage="COUNTER=${"$"}{__counter(FALSE)}""""
                it[filename] = ""
                it[parameters] = ""
            }
            TestElementPropertyTransformer.USE_FUNCTIONS.visit(this)
            isRunningVersion = true
        }
        val result = sampler.sample(null)
        assertEquals(
            "COUNTER=1",
            result.responseMessage,
            "__counter(false) should return 1 on the first execution. If the value is different, it might mean " +
                "the script was evaluated several times"
        )
    }
}
