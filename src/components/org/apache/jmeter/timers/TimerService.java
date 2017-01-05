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

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jorphan.util.JMeterStopThreadException;

public class TimerService {

    public static void checkDelay(long delay) {
        JMeterThread thread = JMeterContextService.getContext().getThread();
        long endTime = thread != null ? thread.getEndTime() : 0;
        if (endTime != 0 && System.currentTimeMillis() + delay > thread.getEndTime()) {
            throw new JMeterStopThreadException("Wait is over thread end time [" + thread.getThreadName() + "]");
        }
    }

}
