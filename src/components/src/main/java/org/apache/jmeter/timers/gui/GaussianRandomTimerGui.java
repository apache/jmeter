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

package org.apache.jmeter.timers.gui;

import org.apache.jmeter.timers.GaussianRandomTimer;
import org.apache.jmeter.timers.RandomTimer;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Implementation of a gaussian random timer.
 */
public class GaussianRandomTimerGui extends AbstractRandomTimerGui {

    private static final long serialVersionUID = 240L;

    private static final String DEFAULT_DELAY = "300"; // $NON-NLS-1$

    private static final String DEFAULT_RANGE = "100.0"; // $NON-NLS-1$


    /**
     * No-arg constructor.
     */
    public GaussianRandomTimerGui() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return "gaussian_timer_title";//$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected RandomTimer createRandomTimer() {
        return new GaussianRandomTimer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTimerDelayLabelKey() {
        return JMeterUtils.getResString("gaussian_timer_delay"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTimerRangeLabelKey() {
        return JMeterUtils.getResString("gaussian_timer_range"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultDelay() {
        return DEFAULT_DELAY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getDefaultRange() {
        return DEFAULT_RANGE;
    }
}
