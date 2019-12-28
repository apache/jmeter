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

package org.apache.jmeter.junit.spock

import org.apache.jmeter.junit.JMeterTestCaseJUnit
import org.apache.jmeter.junit.JMeterTestUtils

import spock.lang.Specification

/**
 * Common setup for Spock test cases.
 * <p>
 * Please only use this class if you <em>need</em> the things set up here.
 * <p>
 * Otherwise, extend {@link Specification}
 */
abstract class JMeterSpec extends Specification {

    /*
    * If not running under AllTests.java, make sure that the properties (and
    * log file) are set up correctly.
    *
    * N.B. This assumes the JUnit test are executed in the
    * project root, bin directory or one level down, and all the JMeter jars
    * (plus any others needed at run-time) need to be on the classpath.
    */
    static {
        // Initialize JMeterTestCaseJUnit which will setup JMeter properties, home, etc
        JMeterTestCaseJUnit.class.getSuperclass();
    }

    protected String getResourceFilePath(String resource) {
        return JMeterTestUtils.getResourceFilePath(getClass(), resource);
    }

    protected static boolean isHeadless() {
        System.properties['java.awt.headless'] == 'true'
    }

}
