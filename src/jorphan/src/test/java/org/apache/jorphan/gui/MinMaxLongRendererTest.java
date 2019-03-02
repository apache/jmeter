/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jorphan.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MinMaxLongRendererTest {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { Long.valueOf(Long.MAX_VALUE), "#N/A" },
            { Long.valueOf(Long.MIN_VALUE), "#N/A" },
            { Long.valueOf(0), "0" },
            { null, "#N/A" },
            { "invalid", "#N/A" },
            });
    }

    private final Object value;
    private final String expected;

    public MinMaxLongRendererTest(Object value, String expected) {
        this.value = value;
        this.expected = expected;
    }

    @Test
    public void testRendering() {
        final AtomicBoolean afterInit = new AtomicBoolean(false);
        MinMaxLongRenderer renderer = new MinMaxLongRenderer("#0") {
            private static final long serialVersionUID = 2L;

            @Override
            public void setText(String text) {
                if (afterInit.get()) {
                    Assert.assertThat(text, CoreMatchers.is(expected));
                }
            }
        };
        afterInit.set(true);
        renderer.setValue(value);
    }

}
