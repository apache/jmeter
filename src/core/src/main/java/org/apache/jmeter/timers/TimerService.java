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

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;

/**
 * Manages logic related to timers and pauses
 * @since 3.2
 */
public class TimerService {

    private TimerService() {
        super();
    }

    /**
     * Initialization On Demand Holder pattern
     */
    private static class TimerServiceHolder {
        public static final TimerService INSTANCE = new TimerService();
    }

    /**
     * @return ScriptEngineManager singleton
     */
    public static TimerService getInstance() {
        return TimerServiceHolder.INSTANCE;
    }

    /**
     * Adjust delay so that initialDelay does not exceed end of test
     * @param initialDelay initial delay in milliseconds
     * @return initialDelay or adjusted delay
     */
    public long adjustDelay(final long initialDelay) {
        return adjustDelay(initialDelay, true);
    }

    /**
     * Adjust delay so that initialDelay does not exceed end of test<br>
     * If {@code runTillEnd} is {@code false} the delay will be shortened
     * to {@code -1} if the delay would be longer than the remaining run time.
     *
     * @param initialDelay initial delay in milliseconds
     * @param runTillEnd adjust delay to match the scheduled end
     * @return initialDelay or adjusted delay
     */
    public long adjustDelay(final long initialDelay, boolean runTillEnd) {
        JMeterThread thread = JMeterContextService.getContext().getThread();
        long endTime = thread != null ? thread.getEndTime() : 0;
        return adjustDelay(initialDelay, endTime, runTillEnd);
    }


    /**
     * Adjust delay so that initialDelay does not exceed end of test
     *
     * @param initialDelay initial delay in milliseconds
     * @param endTime End time of JMeterThread
     * @return initialDelay or a shortened delay if delay is longer than the test is
     *         scheduled
     */
    public long adjustDelay(final long initialDelay, long endTime) {
        return adjustDelay(initialDelay, endTime, true);
    }

    /**
     * Adjust delay so that initialDelay does not exceed end of test<br>
     * If {@code runTillEnd} is {@code false} the delay will be shortened
     * to {@code -1} if the delay would be longer than the remaining run time.
     *
     * @param initialDelay initial delay in milliseconds
     * @param endTime End time of JMeterThread
     * @param runTillEnd adjust delay to match the scheduled end
     * @return initialDelay or a shortened delay if delay is longer than the test is
     *         scheduled
     */
    public long adjustDelay(final long initialDelay, long endTime, boolean runTillEnd) {
        if (endTime > 0) {
            long now = System.currentTimeMillis();
            if (initialDelay > endTime - now) {
                if (runTillEnd) {
                    // shorten the delay to match endTime
                    return endTime - now;
                }
                return -1;
            }
        }
        return initialDelay;
    }

}
