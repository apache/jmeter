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

package org.apache.jorphan.gui;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MinMaxLongRendererTest {

    private static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(Long.MAX_VALUE, "#N/A"),
            Arguments.of(Long.MIN_VALUE, "#N/A"),
            Arguments.of(0L, "0"),
            Arguments.of(null, "#N/A"),
            Arguments.of("invalid", "#N/A")
            );
    }

    @ParameterizedTest
    @MethodSource("data")
    void testRendering(Object value, String expected) {
        final AtomicBoolean afterInit = new AtomicBoolean(false);
        MinMaxLongRenderer renderer = new MinMaxLongRenderer("#0") {
            private static final long serialVersionUID = 2L;

            @Override
            public void setText(String text) {
                if (afterInit.get()) {
                    MatcherAssert.assertThat(text, CoreMatchers.is(expected));
                }
            }
        };
        afterInit.set(true);
        renderer.setValue(value);
    }

}
