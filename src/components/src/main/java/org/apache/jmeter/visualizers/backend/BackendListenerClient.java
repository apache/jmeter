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

package org.apache.jmeter.visualizers.backend;

import java.util.List;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.reflect.JMeterService;

/**
 * This interface defines the interactions between the {@link BackendListener}
 * and external Java programs which can be executed by JMeter. Any Java class
 * which wants to be executed using the {@link BackendListener} test element
 * must implement this interface (either directly or through
 * {@link AbstractBackendListenerClient}).
 * <p>
 * JMeter will create one instance of a BackendListenerClient implementation for
 * each user/thread in the test. Additional instances may be created for
 * internal use by JMeter (for example, to find out what parameters are
 * supported by the client).
 * <p>
 * When the test is started, setupTest() will be called on each thread's
 * BackendListenerClient instance to initialize the client.
 * Then {@link #handleSampleResults(List, BackendListenerContext)} will be
 * called for each {@link SampleResult} notification. Finally,
 * {@link #teardownTest(BackendListenerContext)}
 * will be called to allow the client to do any necessary clean-up.
 * <p>
 * The JMeter BackendListener GUI allows a list of parameters to be defined for
 * the test. These are passed to the various test methods through the
 * {@link BackendListenerContext}. A list of default parameters can be defined
 * through the {@link #getDefaultParameters()} method. These parameters and any
 * default values associated with them will be shown in the GUI. Users can add
 * other parameters as well.
 * <p>
 * Listeners should extend {@link AbstractBackendListenerClient}
 * rather than implementing {@link BackendListenerClient} directly to protect
 * your code from future changes to the interface.
 * <p>
 * While it may be necessary to make changes to the {@link BackendListenerClient}
 * interface from time to time (therefore requiring changes to any
 * implementations of this interface), we intend to make this abstract class
 * provide reasonable default implementations of any new methods so that
 * subclasses do not necessarily need to be updated for new versions.
 * Implementing BackendListenerClient directly will continue to be supported for
 * cases where extending this class is not possible (for example, when the
 * client class is already a subclass of some other class).
 *
 * @since 2.13
 */
@JMeterService
public interface BackendListenerClient {

    /**
     * Do any initialization required by this client. It is generally
     * recommended to do any initialization such as getting parameter values
     * here rather than {@link #handleSampleResults(List, BackendListenerContext)}
     * in order to add as little overhead as possible to the test.
     *
     * @param context provides access to initialization parameters.
     * @throws Exception when setup fails
     */
    void setupTest(BackendListenerContext context) throws Exception; // NOSONAR

    /**
     * Handle sampleResults, this can be done in many ways:
     * <ul>
     * <li>Write to a file</li>
     * <li>Write to a remote server</li>
     * <li>...</li>
     * </ul>
     *
     * @param sampleResults List of {@link SampleResult}
     * @param context       provides access to initialization parameters.
     */
    void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context);

    /**
     * Do any clean-up required at the end of a test run.
     *
     * @param context provides access to initialization parameters.
     * @throws Exception when tear down fails
     */
    void teardownTest(BackendListenerContext context) throws Exception; // NOSONAR

    /**
     * Provide a list of parameters which this test supports. Any parameter
     * names and associated values returned by this method will appear in the
     * GUI by default so the user doesn't have to remember the exact names. The
     * user can add other parameters which are not listed here. If this method
     * returns null then no parameters will be listed. If the value for some
     * parameter is null then that parameter will be listed in the GUI with an
     * empty value.
     *
     * @return a specification of the parameters used by this test which should
     * be listed in the GUI, or null if no parameters should be listed.
     */
    default Arguments getDefaultParameters() {
        return null;
    }

    /**
     * Create a copy of SampleResult, this method is here to allow customizing
     * what is kept in the copy, for example copy could remove some useless fields.
     * Note that if it returns null, the sample result is not put in the queue.
     * Defaults to returning result.
     *
     * @param context {@link BackendListenerContext}
     * @param result  {@link SampleResult}
     * @return {@link SampleResult}
     */
    default SampleResult createSampleResult(
            BackendListenerContext context, SampleResult result) {
        return result;
    }
}
