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

package org.apache.jorphan.collections

import org.apache.jmeter.threads.AbstractThreadGroup
import org.apache.jmeter.threads.ThreadGroup
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SearchByClassTest {
    @Test
    fun `search finds all matching elements`() {
        val tree = ListedHashTree()
        val count = 100000
        for (i in 0 until count) {
            tree.add(ThreadGroup())
        }
        val searcher = SearchByClass(
            AbstractThreadGroup::class.java
        )
        tree.traverse(searcher)
        Assertions.assertEquals(count, searcher.searchResults.size) {
            "Test plan included $count ThreadGroup elements, so SearchByClass should find all of them"
        }
    }
}
