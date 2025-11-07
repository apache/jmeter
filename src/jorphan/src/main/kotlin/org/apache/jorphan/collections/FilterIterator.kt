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

/**
 * Iterator that filters elements from another iterator based on a predicate.
 *
 * This iterator lazily evaluates the predicate as elements are requested,
 * only advancing through the source iterator when needed.
 *
 * @param Element the type of elements returned by this iterator
 * @property iterator the source iterator to filter
 * @property predicate the predicate that determines which elements to include
 */
public class FilterIterator<Element>(
    private val iterator: Iterator<Element>,
    private val predicate: (Element) -> Boolean
) : Iterator<Element> {

    private var nextElement: Element? = null
    private var hasNextElement = false

    override fun hasNext(): Boolean {
        return hasNextElement || prepareNext()
    }

    /**
     * Prepares the next element by advancing through the iterator
     * until an element matching the predicate is found.
     * This method is idempotent - calling it multiple times without
     * consuming the element has no effect.
     */
    private fun prepareNext(): Boolean {
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (predicate(element)) {
                nextElement = element
                hasNextElement = true
                return true
            }
        }
        return false
    }

    override fun next(): Element {
        if (!hasNextElement && !prepareNext()) {
            throw NoSuchElementException("No more elements matching the predicate")
        }

        hasNextElement = false
        @Suppress("UNCHECKED_CAST")
        val result = nextElement as Element
        nextElement = null
        return result
    }
}
