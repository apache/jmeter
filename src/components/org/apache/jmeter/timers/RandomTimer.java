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
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.testelement.property.DoubleProperty;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * This class implements a random timer with its own panel and fields for value
 * update and user interaction. Since this class does not define the delay()
 * method, is abstract and must be extended to provide full functionality.
 *
 */
public abstract class RandomTimer extends ConstantTimer implements ModifiableTimer, Serializable {
    private static final long serialVersionUID = 241L;

    public static final String RANGE = "RandomTimer.range";

    /**
     * No-arg constructor.
     */
    public RandomTimer() {
    }

    /**
     * Set the range value.
     */
    @Override
    public void setRange(double range) {
        setProperty(new DoubleProperty(RANGE, range));
    }

    public void setRange(String range) {
        setProperty(new StringProperty(RANGE, range));
    }

    /**
     * Get the range value.
     *
     * @return double
     */
    @Override
    public double getRange() {
        return this.getPropertyAsDouble(RANGE);
    }
    
    /**
     * @return {@link Random} Thread local Random
     */
    protected Random getRandom() {
        return ThreadLocalRandom.current();
    }
}
