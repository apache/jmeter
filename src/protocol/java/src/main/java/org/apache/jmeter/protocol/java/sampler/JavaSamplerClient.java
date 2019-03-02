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

package org.apache.jmeter.protocol.java.sampler;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;

/**
 * This interface defines the interactions between the JavaSampler and external
 * Java programs which can be executed by JMeter. Any Java class which wants to
 * be executed as a JMeter test must implement this interface (either directly
 * or indirectly through AbstractJavaSamplerClient).
 * <p>
 * JMeter will create one instance of a JavaSamplerClient implementation for
 * each user/thread in the test. Additional instances may be created for
 * internal use by JMeter (for example, to find out what parameters are
 * supported by the client).
 * <p>
 * When the test is started, setupTest() will be called on each thread's
 * JavaSamplerClient instance to initialize the client. Then runTest() will be
 * called for each iteration of the test. Finally, teardownTest() will be called
 * to allow the client to do any necessary clean-up.
 * <p>
 * The JMeter JavaSampler GUI allows a list of parameters to be defined for the
 * test. These are passed to the various test methods through the
 * {@link JavaSamplerContext}. A list of default parameters can be defined
 * through the getDefaultParameters() method. These parameters and any default
 * values associated with them will be shown in the GUI. Users can add other
 * parameters as well.
 * <p>
 * When possible, Java tests should extend {@link AbstractJavaSamplerClient
 * AbstractJavaSamplerClient} rather than implementing JavaSamplerClient
 * directly. This should protect your tests from future changes to the
 * interface. While it may be necessary to make changes to the JavaSamplerClient
 * interface from time to time (therefore requiring changes to any
 * implementations of this interface), we intend to make this abstract class
 * provide reasonable default implementations of any new methods so that
 * subclasses do not necessarily need to be updated for new versions.
 * Implementing JavaSamplerClient directly will continue to be supported for
 * cases where extending this class is not possible (for example, when the
 * client class is already a subclass of some other class).
 * <p>
 * See {@link org.apache.jmeter.protocol.java.test.SleepTest} for an example of
 * how to implement this interface.
 *
 */
public interface JavaSamplerClient {
    /**
     * Do any initialization required by this client. It is generally
     * recommended to do any initialization such as getting parameter values in
     * the setupTest method rather than the runTest method in order to add as
     * little overhead as possible to the test.
     *
     * @param context
     *            the context to run with. This provides access to
     *            initialization parameters.
     */
    void setupTest(JavaSamplerContext context);

    /**
     * Perform a single sample for each iteration. This method returns a
     * <code>SampleResult</code> object. <code>SampleResult</code> has many
     * fields which can be used. At a minimum, the test should use
     * <code>SampleResult.sampleStart</code> and
     * <code>SampleResult.sampleEnd</code>to set the time that the test
     * required to execute. It is also a good idea to set the sampleLabel and
     * the successful flag.
     *
     * @see org.apache.jmeter.samplers.SampleResult#sampleStart()
     * @see org.apache.jmeter.samplers.SampleResult#sampleEnd()
     * @see org.apache.jmeter.samplers.SampleResult#setSuccessful(boolean)
     * @see org.apache.jmeter.samplers.SampleResult#setSampleLabel(String)
     *
     * @param context
     *            the context to run with. This provides access to
     *            initialization parameters.
     *
     * @return a SampleResult giving the results of this sample.
     */
    SampleResult runTest(JavaSamplerContext context);

    /**
     * Do any clean-up required by this test at the end of a test run.
     *
     * @param context
     *            the context to run with. This provides access to
     *            initialization parameters.
     */
    void teardownTest(JavaSamplerContext context);

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
     *         be listed in the GUI, or null if no parameters should be listed.
     */
    Arguments getDefaultParameters();
}
