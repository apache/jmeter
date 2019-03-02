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

import org.apache.jmeter.util.JMeterUtils;

/**
 * This class implements those methods needed by RandomTimer to be instantiable
 * and implements a random delay with an average value and a gaussian
 * distributed variation.
 *
 */
public class GaussianRandomTimer extends RandomTimer implements Serializable {
    private static final long serialVersionUID = 241L;

    @Override
    public long delay() {
        return (long) Math.abs((getRandom().nextGaussian() * getRange()) + super.delay());
    }

    @Override
    public String toString() {
        return JMeterUtils.getResString("gaussian_timer_memo"); //$NON-NLS-1$
    }
}
