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

import org.apache.jmeter.timers.PoissonRandomTimer;
import org.apache.jmeter.timers.RandomTimer;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Implementation of a Poisson random timer.
 */
public class PoissonRandomTimerGui extends AbstractRandomTimerGui {

    private static final long serialVersionUID = -3218002787832805275L;

    private static final String DEFAULT_DELAY = "300"; // $NON-NLS-1$

    private static final String DEFAULT_RANGE = "100"; // $NON-NLS-1$

    public PoissonRandomTimerGui() {
        super();
    }

    @Override
    public String getLabelResource() {
        return "poisson_timer_title";//$NON-NLS-1$
    }

    @Override
    protected RandomTimer createRandomTimer() {
        return new PoissonRandomTimer();
    }

    @Override
    protected String getTimerDelayLabelKey() {
        return JMeterUtils.getResString("poisson_timer_delay"); //$NON-NLS-1$
    }

    @Override
    protected String getTimerRangeLabelKey() {
        return JMeterUtils.getResString("poisson_timer_range"); //$NON-NLS-1$
    }

    @Override
    protected String getDefaultDelay() {
        return DEFAULT_DELAY;
    }

    @Override
    protected String getDefaultRange() {
        return DEFAULT_RANGE;
    }
}
