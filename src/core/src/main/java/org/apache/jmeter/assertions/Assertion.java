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

package org.apache.jmeter.assertions;

import org.apache.jmeter.samplers.SampleResult;

/**
 * An Assertion checks a SampleResult to determine whether or not it is
 * successful. The resulting success status can be obtained from a corresponding
 * Assertion Result. For example, if a web response doesn't contain an expected
 * expression, it would be considered a failure.
 *
 */
public interface Assertion {
    /**
     * Returns the AssertionResult object encapsulating information about the
     * success or failure of the assertion.
     *
     * @param response
     *            the SampleResult containing information about the Sample
     *            (duration, success, etc)
     *
     * @return the AssertionResult containing the information about whether the
     *         assertion passed or failed.
     */
    AssertionResult getResult(SampleResult response);
}
