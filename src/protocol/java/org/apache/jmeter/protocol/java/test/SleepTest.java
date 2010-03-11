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
package org.apache.jmeter.protocol.java.test;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * The <code>SleepTest</code> class is a simple example class for a JMeter
 * Java protocol client. The class implements the <code>JavaSamplerClient</code>
 * interface.
 * <p>
 * During each sample, this client will sleep for some amount of time. The
 * amount of time to sleep is determined from the two parameters SleepTime and
 * SleepMask using the formula:
 *
 * <pre>
 * totalSleepTime = SleepTime + (System.currentTimeMillis() % SleepMask)
 * </pre>
 *
 * Thus, the SleepMask provides a way to add a random component to the sleep
 * time.
 *
 * @version $Revision$
 */
public class SleepTest extends AbstractJavaSamplerClient implements Serializable {
    private static final long serialVersionUID = 240L;

    /**
     * The default value of the SleepTime parameter, in milliseconds.
     */
    public static final long DEFAULT_SLEEP_TIME = 1000;

    /**
     * The default value of the SleepMask parameter.
     */
    public static final long DEFAULT_SLEEP_MASK = 0x3ff;

    /**
     * The base number of milliseconds to sleep during each sample.
     */
    private long sleepTime;

    /**
     * A mask to be applied to the current time in order to add a random
     * component to the sleep time.
     */
    private long sleepMask;

    /**
     * Default constructor for <code>SleepTest</code>.
     *
     * The Java Sampler uses the default constructor to instantiate an instance
     * of the client class.
     */
    public SleepTest() {
        getLogger().debug(whoAmI() + "\tConstruct");
    }

    /**
     * Do any initialization required by this client. In this case,
     * initialization consists of getting the values of the SleepTime and
     * SleepMask parameters. It is generally recommended to do any
     * initialization such as getting parameter values in the setupTest method
     * rather than the runTest method in order to add as little overhead as
     * possible to the test.
     *
     * @param context
     *            the context to run with. This provides access to
     *            initialization parameters.
     */
    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().debug(whoAmI() + "\tsetupTest()");
        listParameters(context);

        sleepTime = context.getLongParameter("SleepTime", DEFAULT_SLEEP_TIME);
        sleepMask = context.getLongParameter("SleepMask", DEFAULT_SLEEP_MASK);
    }

    /**
     * Perform a single sample. In this case, this method will simply sleep for
     * some amount of time. Perform a single sample for each iteration. This
     * method returns a <code>SampleResult</code> object.
     * <code>SampleResult</code> has many fields which can be used. At a
     * minimum, the test should use <code>SampleResult.sampleStart</code> and
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
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult results = new SampleResult();

        try {
            // Record sample start time.
            results.sampleStart();

            long sleep = sleepTime;
            // Only do the calculation if it is needed
            if (sleepTime > 0 && sleepMask > 0) {
                long start = System.currentTimeMillis();
                // Generate a random-ish offset value using the current time.
                sleep = sleepTime + (start % sleepMask);
            }

            results.setSampleLabel("Sleep Test: time = " + sleep);

            // Execute the sample. In this case sleep for the
            // specified time.
            Thread.sleep(sleep);

            results.setSuccessful(true);
        } catch (InterruptedException e) {
            getLogger().warn("SleepTest: interrupted.");
            results.setSuccessful(true);
        } catch (Exception e) {
            getLogger().error("SleepTest: error during sample", e);
            results.setSuccessful(false);
        } finally {
            results.sampleEnd();
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug(whoAmI() + "\trunTest()" + "\tTime:\t" + results.getTime());
            listParameters(context);
        }

        return results;
    }

    /**
     * Do any clean-up required by this test. In this case no clean-up is
     * necessary, but some messages are logged for debugging purposes.
     *
     * @param context
     *            the context to run with. This provides access to
     *            initialization parameters.
     */
    @Override
    public void teardownTest(JavaSamplerContext context) {
        getLogger().debug(whoAmI() + "\tteardownTest()");
        listParameters(context);
    }

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
    @Override
    public Arguments getDefaultParameters() {
        Arguments params = new Arguments();
        params.addArgument("SleepTime", String.valueOf(DEFAULT_SLEEP_TIME));
        params.addArgument("SleepMask", "0x" + (Long.toHexString(DEFAULT_SLEEP_MASK)).toUpperCase(java.util.Locale.ENGLISH));
        return params;
    }

    /**
     * Dump a list of the parameters in this context to the debug log.
     *
     * @param context
     *            the context which contains the initialization parameters.
     */
    private void listParameters(JavaSamplerContext context) {
        if (getLogger().isDebugEnabled()) {
            Iterator<String> argsIt = context.getParameterNamesIterator();
            while (argsIt.hasNext()) {
                String name = argsIt.next();
                getLogger().debug(name + "=" + context.getParameter(name));
            }
        }
    }

    /**
     * Generate a String identifier of this test for debugging purposes.
     *
     * @return a String identifier for this test instance
     */
    private String whoAmI() {
        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().toString());
        sb.append("@");
        sb.append(Integer.toHexString(hashCode()));
        return sb.toString();
    }
}
