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

package org.apache.jmeter.timers.poissonarrivals;

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

/**
 * @since 4.0
 */
public class PreciseThroughputTimerBeanInfo extends BeanInfoSupport {
    public PreciseThroughputTimerBeanInfo() {
        super(PreciseThroughputTimer.class);
        createPropertyGroup(
                "delay", //$NON-NLS-1$
                new String[]{
                        "throughput", //$NON-NLS-1$
                        "throughputPeriod",    //$NON-NLS-1$
                        "duration",    //$NON-NLS-1$
                }
        );

        PropertyDescriptor p;
        p = property("throughput"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Double.valueOf(100));

        p = property("throughputPeriod"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 3600);

        p = property("duration"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Long.valueOf(3600));

        createPropertyGroup(
                "batching", //$NON-NLS-1$
                new String[] {
                        "batchSize"
                        , "batchThreadDelay"
                }
        );

        p = property("batchSize"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 1);

        p = property("batchThreadDelay"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, 0);


        createPropertyGroup(
                "accuracy", //$NON-NLS-1$
                new String[]{
                        "exactLimit", //$NON-NLS-1$
                        "allowedThroughputSurplus"    //$NON-NLS-1$
                }
        );

        p = property("exactLimit"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Integer.valueOf(10000));

        p = property("allowedThroughputSurplus"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Double.valueOf(1.0d));

        createPropertyGroup(
                "repeatability", //$NON-NLS-1$
                new String[]{
                        "randomSeed" //$NON-NLS-1$
                }
        );

        p = property("randomSeed"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Long.valueOf(0));
    }
}
