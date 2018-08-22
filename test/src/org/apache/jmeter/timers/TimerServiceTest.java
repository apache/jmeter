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

package org.apache.jmeter.timers;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class TimerServiceTest {

    TimerService sut = TimerService.getInstance();

    @Test
    public void testBigInitialDelay() {
        long now = System.currentTimeMillis();
        long adjustedDelay = sut.adjustDelay(Long.MAX_VALUE, now + 1000L);
        // As #adjustDelay uses System#currentTimeMillis we can't be sure, that the value is exact 1000L
        Assert.assertThat(Math.abs(adjustedDelay - 1000L) < 150L, CoreMatchers.is(true));
    }

}
