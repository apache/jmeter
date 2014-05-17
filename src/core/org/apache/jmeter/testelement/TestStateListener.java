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

package org.apache.jmeter.testelement;

/**
 * @since 2.8
 */
public interface TestStateListener {

    /**
     * <p>
     * Called just before the start of the test from the main engine thread.
     *
     * This is before the test elements are cloned.
     *
     * Note that not all the test
     * variables will have been set up at this point.
     * </p>
     *
     * <p>
     * <b>
     * N.B. testStarted() and testEnded() are called from different threads.
     * </b>
     * </p>
     * @see org.apache.jmeter.engine.StandardJMeterEngine#run()
     *
     */
    void testStarted();

    /**
     * <p>
     * Called just before the start of the test from the main engine thread.
     *
     * This is before the test elements are cloned.
     *
     * Note that not all the test
     * variables will have been set up at this point.
     * </p>
     *
     * <p>
     * <b>
     * N.B. testStarted() and testEnded() are called from different threads.
     * </b>
     * </p>
     * @see org.apache.jmeter.engine.StandardJMeterEngine#run()
     * @param host name of host
     */
    void testStarted(String host);

    /**
     * <p>
     * Called once for all threads after the end of a test.
     *
     * This will use the same element instances as at the start of the test.
     * </p>
     *
     * <p>
     * <b>
     * N.B. testStarted() and testEnded() are called from different threads.
     * </b>
     * </p>
     * @see org.apache.jmeter.engine.StandardJMeterEngine#stopTest()
     *
     */
    void testEnded();

    /**
     * <p>
     * Called once for all threads after the end of a test.
     *
     * This will use the same element instances as at the start of the test.
     * </p>
     *
     * <p>
     * <b>
     * N.B. testStarted() and testEnded() are called from different threads.
     * </b>
     * </p>
     * @see org.apache.jmeter.engine.StandardJMeterEngine#stopTest()
     * @param host name of host
     *
     */

    void testEnded(String host);

}
