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

package org.apache.jmeter.visualizers;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestSampleCompareTo {

    private static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(0L, 0L, 0),
                Arguments.of(1L, 0L, 1),
                Arguments.of(0L, 1L, -1),
                Arguments.of(Long.MAX_VALUE, Long.MIN_VALUE, 1),
                Arguments.of(Long.MIN_VALUE, Long.MAX_VALUE, -1),
                Arguments.of(1000L, -1000L, 1),
                Arguments.of(-1000L, 1000L, -1),
                Arguments.of(Long.MIN_VALUE, Long.MIN_VALUE, 0),
                Arguments.of(Long.MAX_VALUE, Long.MAX_VALUE, 0));
    }

    @ParameterizedTest
    @MethodSource("data")
    void testCompareTo(long thisCount, long otherCount, int compareResult) {
        assertThat(sample(thisCount).compareTo(sample(otherCount)),
                CoreMatchers.is(compareResult));
    }

    private Sample sample(long count) {
        return new Sample("dummy", 0L, 0L, 0L, 0L, 0L, 0.0, 0L, true, count, 0L);
    }

}
