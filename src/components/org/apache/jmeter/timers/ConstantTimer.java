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

import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * This class implements a constant timer with its own panel and fields for
 * value update and user interaction.
 *
 */
public class ConstantTimer extends AbstractTestElement implements Timer, Serializable, LoopIterationListener {

    private static final long serialVersionUID = 240L;

    public static final String DELAY = "ConstantTimer.delay"; //$NON-NLS-1$

    private long delay = 0;

    /**
     * No-arg constructor.
     */
    public ConstantTimer() {
    }

    /**
     * Set the delay for this timer.
     * @param delay The delay for this timer
     */
    public void setDelay(String delay) {
        setProperty(DELAY, delay);
    }

    /**
     * Set the range (not used for this timer).
     * @param range Not used
     *
     */
    public void setRange(double range) {
        // NOOP
    }

    /**
     * Get the delay value for display.
     *
     * @return the delay value for display.
     */
    public String getDelay() {
        return getPropertyAsString(DELAY);
    }

    /**
     * Retrieve the range (not used for this timer).
     *
     * @return the range (always zero for this timer).
     */
    public double getRange() {
        return 0;
    }

    /**
     * Retrieve the delay to use during test execution.
     *
     * @return the delay.
     */
    @Override
    public long delay() {
        return delay;
    }

    /**
     * Provide a description of this timer class.
     *
     * @return the description of this timer class.
     */
    @Override
    public String toString() {
        return JMeterUtils.getResString("constant_timer_memo"); //$NON-NLS-1$
    }

    /**
     * Gain access to any variables that have been defined.
     *
     * @see LoopIterationListener#iterationStart(LoopIterationEvent)
     */
    @Override
    public void iterationStart(LoopIterationEvent event) {
        delay = getPropertyAsLong(DELAY);

    }
}
