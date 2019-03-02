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

package org.apache.jmeter.engine;

import java.util.Properties;

import org.apache.jorphan.collections.HashTree;

/**
 * This interface is implemented by classes that can run JMeter tests.
 */
public interface JMeterEngine {
    /**
     * Configure engine
     * @param testPlan the test plan
     */
    void configure(HashTree testPlan);

    /**
     * Runs the test
     * @throws JMeterEngineException if an error occurs
     */
    void runTest() throws JMeterEngineException;

    /**
     * Stop test immediately interrupting current samplers
     */
    default void stopTest() {
        stopTest(true);
    }
    /**
     *
     * @param now boolean that tell wether stop is immediate (interrupt) or not (wait for current sample end)
     */
    void stopTest(boolean now);

    /**
     * Stop test if running
     */
    void reset();

    /**
     * set Properties on engine
     * @param p the properties to set
     */
    void setProperties(Properties p);

    /**
     * Exit engine
     */
    void exit();

    /**
     * @return boolean Flag to show whether engine is active (true when test is running). Set to false at end of test
     */
    boolean isActive();
}
