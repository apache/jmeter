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

package org.apache.jorphan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class StringWrapTest {
    static Stream<Arguments> data() {
        return Stream.of(
                arguments(2, 2, "0123456789", "01|23|45|67|89"),
                arguments(2, 5, "0123456789", "01234|56789"),
                arguments(3, 3, "0123456789", "012|345|678|9"),
                arguments(2, 5, "01-2-3-4-56-789-", "01-2-|3-4-|56-|789-"),
                arguments(2, 5, "012\n345\n6\n7\n8\n9", null),
                arguments(2, 5, "012\n3456789", "012\n34567|89"),
                // Single-char symbols
                arguments(2, 5, "丈丈丈丈丈a丈丈a 丈丈丈 b 丈丈", "丈丈丈丈丈|a丈丈a |丈丈丈 |b 丈丈"),
                // Two-char symbols
                arguments(2, 5, "निनिनिनिनिनिनdनिनिनिनि3निनिनिनिनिनि1 नि", "निनि|निनि|निनिन|dनिनि|निनि3|निनि|निनि|निनि1| नि"),
                // Two-char symbols
                arguments(2, 5, "😃😃😃😃😃😃😃😃a😃aa😃😃 😃😃 b 😃", "😃😃|😃😃|😃😃|😃😃a|😃aa|😃😃 |😃😃 |b 😃"),
                arguments(1, 1, "😃", null),
                // Multi-char symbols
                arguments(2, 5, "rè̑ͧ̌aͨl̘̝̙̃ͤ͂̾̆rè̑ͧ̌aͨl̘̝̙̃ͤ͂̾̆rè̑ͧ", "rè̑ͧ̌|aͨ|l̘̝̙̃ͤ͂̾̆|r|è̑ͧ̌|aͨ|l̘̝̙̃ͤ͂̾̆|rè̑ͧ"),
                arguments(2, 9, "rè̑ͧ̌aͨl̘̝̙̃ͤ͂̾̆rè̑ͧ̌aͨl̘̝̙̃ͤ͂̾̆rè̑ͧ", "rè̑ͧ̌aͨ|l̘̝̙̃ͤ͂̾̆|rè̑ͧ̌aͨ|l̘̝̙̃ͤ͂̾̆|rè̑ͧ")
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void wrap(int minWrap, int maxWrap, String input, String expected) {
        if (expected == null) {
            expected = input;
        }
        StringWrap stringWrap = new StringWrap(minWrap, maxWrap);
        String output = stringWrap.wrap(input, "|");
        assertEquals(expected, output, () -> "minWrap=" + minWrap + ", maxWrap=" + maxWrap + ", input=" + input);
    }
}
