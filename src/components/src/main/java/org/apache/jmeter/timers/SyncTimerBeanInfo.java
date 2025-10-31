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

import java.beans.PropertyDescriptor;

import org.apache.jmeter.testbeans.BeanInfoSupport;

public class SyncTimerBeanInfo extends BeanInfoSupport {

    public SyncTimerBeanInfo() {
        super(SyncTimer.class);

        createPropertyGroup("grouping", new String[] { "groupSize", "timeoutInMs" });

        PropertyDescriptor p = property("groupSize");
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, 0);

        p = property("timeoutInMs");
        p.setValue(NOT_UNDEFINED, true);
        p.setValue(DEFAULT, 0L);

    }

}
