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

package org.apache.jmeter.samplers;

import java.lang.reflect.Constructor;

import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleSenderFactory {

    private static final Logger log = LoggerFactory.getLogger(SampleSenderFactory.class);

    private static final String MODE_STANDARD = "Standard"; // $NON-NLS-1$
    private static final String MODE_BATCH = "Batch"; // $NON-NLS-1$
    private static final String MODE_STATISTICAL = "Statistical"; // $NON-NLS-1$
    private static final String MODE_STRIPPED = "Stripped"; // $NON-NLS-1$
    private static final String MODE_STRIPPED_BATCH = "StrippedBatch"; // $NON-NLS-1$
    private static final String MODE_ASYNCH = "Asynch"; // $NON-NLS-1$
    private static final String MODE_STRIPPED_ASYNCH = "StrippedAsynch"; // $NON-NLS-1$
    private static final String MODE_DISKSTORE = "DiskStore"; // $NON-NLS-1$
    private static final String MODE_STRIPPED_DISKSTORE = "StrippedDiskStore"; // $NON-NLS-1$

    /**
     * Checks for the JMeter property mode and returns the required class.
     *
     * @return the appropriate class. Standard JMeter functionality,
     * hold_samples until end of test or batch samples.
     */
    static SampleSender getInstance(RemoteSampleListener listener) {
        // Extended property name
        final String type = JMeterUtils.getPropDefault("mode", MODE_STRIPPED_BATCH); // $NON-NLS-1$

        SampleSender s;
        if (type.equalsIgnoreCase(MODE_BATCH)) {
            s = new BatchSampleSender(listener);
        } else if (type.equalsIgnoreCase(MODE_STRIPPED_BATCH)) {
            s = new DataStrippingSampleSender(new BatchSampleSender(listener));
        } else if (type.equalsIgnoreCase(MODE_STATISTICAL)) {
            s = new StatisticalSampleSender(listener);
        } else if (type.equalsIgnoreCase(MODE_STANDARD)) {
            s = new StandardSampleSender(listener);
        } else if (type.equalsIgnoreCase(MODE_STRIPPED)) {
            s = new DataStrippingSampleSender(listener);
        } else if (type.equalsIgnoreCase(MODE_ASYNCH)) {
            s = new AsynchSampleSender(listener);
        } else if (type.equalsIgnoreCase(MODE_STRIPPED_ASYNCH)) {
            s = new DataStrippingSampleSender(new AsynchSampleSender(listener));
        } else if (type.equalsIgnoreCase(MODE_DISKSTORE)) {
            s = new DiskStoreSampleSender(listener);
        } else if (type.equalsIgnoreCase(MODE_STRIPPED_DISKSTORE)) {
            s = new DataStrippingSampleSender(new DiskStoreSampleSender(listener));
        } else {
            // should be a user provided class name
            try {
                Class<?> clazz = Class.forName(type);
                Constructor<?> cons = clazz.getConstructor(RemoteSampleListener.class);
                s = (SampleSender) cons.newInstance(new Object[]{listener});
            } catch (Exception e) {
                // houston we have a problem !!
                log.error(
                        "Unable to create a sample sender from class:'{}', search for "
                                + "mode property in jmeter.properties for correct configuration options",
                        type);
                throw new IllegalArgumentException("Unable to create a sample sender from mode or class:'"
                        + type + "', search for mode property in jmeter.properties for correct configuration options, "
                        + "message:" + e.getMessage(), e);
            }

        }
        return s;
    }
}
