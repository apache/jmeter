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

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

/**
 * BeanInfo for the ConstantThroughputTimer.
 */
public class ConstantThroughputTimerBeanInfo extends BeanInfoSupport {

    public ConstantThroughputTimerBeanInfo() {
        super(ConstantThroughputTimer.class);

        createPropertyGroup("delay",  //$NON-NLS-1$
                new String[] { "throughput", //$NON-NLS-1$
                "calcMode" }); //$NON-NLS-1$

        PropertyDescriptor p = property("throughput"); //$NON-NLS-1$
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p.setValue(DEFAULT, Double.valueOf(0.0));

        p = property("calcMode", ConstantThroughputTimer.Mode.class); //$NON-NLS-1$
        p.setValue(DEFAULT, Integer.valueOf(ConstantThroughputTimer.Mode.ThisThreadOnly.ordinal()));
        p.setValue(NOT_UNDEFINED, Boolean.TRUE); // must be defined
    }

}
