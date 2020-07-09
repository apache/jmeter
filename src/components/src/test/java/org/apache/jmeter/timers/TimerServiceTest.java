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

package org.apache.jmeter.timers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class TimerServiceTest {

    TimerService sut = TimerService.getInstance();

    @Test
    public void testBigInitialDelayAndDontWait() {
        long now = System.currentTimeMillis();
        long adjustedDelay = sut.adjustDelay(Long.MAX_VALUE, now + 1000L, false);
        MatcherAssert.assertThat("TimerService should return -1 as delay would lead to a time after end time",
                adjustedDelay, CoreMatchers.is((long) -1));
    }

    @Test
    public void testBigInitialDelayAndWait() {
        long now = System.currentTimeMillis();
        long adjustedDelay = sut.adjustDelay(Long.MAX_VALUE, now + 1000L);
        MatcherAssert.assertThat("TimerService should return -1 as delay would lead to a time after end time",
                adjustedDelay, isAlmost(1000L, 200L));
    }

    private BaseMatcher<Long> isAlmost(long value, long precision) {
        return new BaseMatcher<Long>() {

            @Override
            public boolean matches(Object item) {
                if (item instanceof Long) {
                    Long other = (Long) item;
                    return Math.abs(other - value) < precision;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("the number is within a precision of " + precision + " near " + value);
            }
      };
    }

    @Test
    public void testSmallInitialDelay() {
        long now = System.currentTimeMillis();
        MatcherAssert.assertThat("TimerService should not change the delay as the end time is far away",
                sut.adjustDelay(1000L, now + 20000L), CoreMatchers.is(1000L));
    }

    @Test
    public void testNegativeEndTime() {
        MatcherAssert.assertThat("TimerService should not change the delay as the indicated end time is far away",
                sut.adjustDelay(1000L, -1), CoreMatchers.is(1000L));
    }

}
