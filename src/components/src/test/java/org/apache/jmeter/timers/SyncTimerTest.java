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

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.Assert;
import org.junit.Test;

public class SyncTimerTest {

    @Test
    public void testTimerWithScheduledEndpoint() {
        long schedulerDuration = 200L;
        setupScheduledThread(schedulerDuration);
        SyncTimer timer = new SyncTimer();
        timer.setGroupSize(2);
        timer.testStarted();
        long duration = timeDelay(timer);
        Assert.assertTrue(
                "Calculating delay takes less then " + schedulerDuration * 2
                        + " ms (took: " + duration + " ms)",
                duration < schedulerDuration * 2);
    }

    @Test
    public void testTimerWithLongerScheduledEndpointThanTimeoutForTimer() {
        long schedulerDuration = 2000L;
        long timerTimeout = 200L;
        setupScheduledThread(schedulerDuration);
        SyncTimer timer = new SyncTimer();
        timer.setGroupSize(2);
        timer.testStarted();
        timer.setTimeoutInMs(timerTimeout);
        long duration = timeDelay(timer);
        Assert.assertTrue(
                "Calculating delay takes less then " + timerTimeout * 2
                        + " ms (took: " + duration + " ms)",
                duration < timerTimeout * 2);
    }

    @Test
    public void testTimerWithShorterScheduledEndpointThanTimeoutForTimer() {
        long schedulerDuration = 200L;
        long timerTimeout = 2000L;
        setupScheduledThread(schedulerDuration);
        SyncTimer timer = new SyncTimer();
        timer.setGroupSize(2);
        timer.testStarted();
        timer.setTimeoutInMs(timerTimeout);
        long duration = timeDelay(timer);
        Assert.assertTrue(
                "Calculating delay takes less then " + schedulerDuration * 2
                        + " ms (took: " + duration + " ms)",
                duration < schedulerDuration * 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimerWithInvalidTimeout() {
        long schedulerDuration = 200L;
        long timerTimeout = -1L;
        setupScheduledThread(schedulerDuration);
        SyncTimer timer = new SyncTimer();
        timer.setGroupSize(2);
        timer.testStarted();
        timer.setTimeoutInMs(timerTimeout);
        timer.delay();
    }

    private long timeDelay(SyncTimer timer) {
        long start = System.currentTimeMillis();
        timer.delay();
        long duration = System.currentTimeMillis() - start;
        return duration;
    }

    private void setupScheduledThread(long schedulerDuration) {
        ListedHashTree hashTree = new ListedHashTree();
        hashTree.add(new LoopController());
        JMeterThread thread = new JMeterThread(hashTree, null, null);
        JMeterContextService.getContext().setThread(thread);
        thread.setEndTime(System.currentTimeMillis() + schedulerDuration);
    }

}
