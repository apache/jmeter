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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A sampler for executing custom Java code in each sample. See
 * {@link JavaSamplerClient} and {@link AbstractJavaSamplerClient} for
 * information on writing Java code to be executed by this sampler.
 *
 */
public class JavaSampler extends AbstractSampler implements TestListener {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 232L; // Remember to change this when the class changes ...

    /**
     * Property key representing the classname of the JavaSamplerClient to user.
     */
    public static final String CLASSNAME = "classname";

    /**
     * Property key representing the arguments for the JavaSamplerClient.
     */
    public static final String ARGUMENTS = "arguments";

    /**
     * The JavaSamplerClient instance used by this sampler to actually perform
     * the sample.
     */
    private transient JavaSamplerClient javaClient = null;

    /**
     * The JavaSamplerContext instance used by this sampler to hold information
     * related to the test run, such as the parameters specified for the sampler
     * client.
     */
    private transient JavaSamplerContext context = null;

    /**
     * Set used to register all active JavaSamplers. This is used so that the
     * samplers can be notified when the test ends.
     */
    private static final Set<JavaSampler> allSamplers = new HashSet<JavaSampler>();

    /**
     * Create a JavaSampler.
     */
    public JavaSampler() {
        setArguments(new Arguments());
        synchronized (allSamplers) {
            allSamplers.add(this);
        }
    }

    /**
     * Set the arguments (parameters) for the JavaSamplerClient to be executed
     * with.
     *
     * @param args
     *            the new arguments. These replace any existing arguments.
     */
    public void setArguments(Arguments args) {
        setProperty(new TestElementProperty(ARGUMENTS, args));
    }

    /**
     * Get the arguments (parameters) for the JavaSamplerClient to be executed
     * with.
     *
     * @return the arguments
     */
    public Arguments getArguments() {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }

    /**
     * Releases Java Client.
     */
    private void releaseJavaClient() {
        if (javaClient != null) {
            javaClient.teardownTest(context);
        }
        javaClient = null;
        context = null;
    }

    /**
     * Sets the Classname attribute of the JavaConfig object
     *
     * @param classname
     *            the new Classname value
     */
    public void setClassname(String classname) {
        setProperty(CLASSNAME, classname);
    }

    /**
     * Gets the Classname attribute of the JavaConfig object
     *
     * @return the Classname value
     */
    public String getClassname() {
        return getPropertyAsString(CLASSNAME);
    }

    /**
     * Performs a test sample.
     *
     * The <code>sample()</code> method retrieves the reference to the Java
     * client and calls its <code>runTest()</code> method.
     *
     * @see JavaSamplerClient#runTest(JavaSamplerContext)
     *
     * @param entry
     *            the Entry for this sample
     * @return test SampleResult
     */
    public SampleResult sample(Entry entry) {
        Arguments args = getArguments();
        args.addArgument(TestElement.NAME, getName()); // Allow Sampler access
                                                        // to test element name
        context = new JavaSamplerContext(args);
        if (javaClient == null) {
            log.debug(whoAmI() + "Creating Java Client");
            createJavaClient();
            javaClient.setupTest(context);
        }

        SampleResult result = createJavaClient().runTest(context);

        // Only set the default label if it has not been set
        if (result != null && result.getSampleLabel().length() == 0) {
            result.setSampleLabel(getName());
        }

        return result;
    }

    /**
     * Returns reference to <code>JavaSamplerClient</code>.
     *
     * The <code>createJavaClient()</code> method uses reflection to create an
     * instance of the specified Java protocol client. If the class can not be
     * found, the method returns a reference to <code>this</code> object.
     *
     * @return JavaSamplerClient reference.
     */
    private JavaSamplerClient createJavaClient() {
        if (javaClient == null) {
            try {
                Class<?> javaClass = Class.forName(getClassname().trim(), false, Thread.currentThread()
                        .getContextClassLoader());
                javaClient = (JavaSamplerClient) javaClass.newInstance();
                context = new JavaSamplerContext(getArguments());

                if (log.isDebugEnabled()) {
                    log.debug(whoAmI() + "\tCreated:\t" + getClassname() + "@"
                            + Integer.toHexString(javaClient.hashCode()));
                }
            } catch (Exception e) {
                log.error(whoAmI() + "\tException creating: " + getClassname(), e);
                javaClient = new ErrorSamplerClient();
            }
        }
        return javaClient;
    }

    /**
     * Retrieves reference to JavaSamplerClient.
     *
     * Convience method used to check for null reference without actually
     * creating a JavaSamplerClient
     *
     * @return reference to JavaSamplerClient NOTUSED private JavaSamplerClient
     *         retrieveJavaClient() { return javaClient; }
     */

    /**
     * Generate a String identifier of this instance for debugging purposes.
     *
     * @return a String identifier for this sampler instance
     */
    private String whoAmI() {
        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().getName());
        sb.append("@");
        sb.append(Integer.toHexString(hashCode()));
        sb.append("-");
        sb.append(getName());
        return sb.toString();
    }

    // TestListener implementation
    /* Implements TestListener.testStarted() */
    public void testStarted() {
        log.debug(whoAmI() + "\ttestStarted");
    }

    /* Implements TestListener.testStarted(String) */
    public void testStarted(String host) {
        log.debug(whoAmI() + "\ttestStarted(" + host + ")");
    }

    /**
     * Method called at the end of the test. This is called only on one instance
     * of JavaSampler. This method will loop through all of the other
     * JavaSamplers which have been registered (automatically in the
     * constructor) and notify them that the test has ended, allowing the
     * JavaSamplerClients to cleanup.
     */
    public void testEnded() {
        log.debug(whoAmI() + "\ttestEnded");
        synchronized (allSamplers) {
            Iterator<JavaSampler> i = allSamplers.iterator();
            while (i.hasNext()) {
                JavaSampler sampler = i.next();
                sampler.releaseJavaClient();
                i.remove();
            }
        }
    }

    /* Implements TestListener.testEnded(String) */
    public void testEnded(String host) {
        testEnded();
    }

    /* Implements TestListener.testIterationStart(LoopIterationEvent) */
    public void testIterationStart(LoopIterationEvent event) {
    }

    /**
     * A {@link JavaSamplerClient} implementation used for error handling. If an
     * error occurs while creating the real JavaSamplerClient object, it is
     * replaced with an instance of this class. Each time a sample occurs with
     * this class, the result is marked as a failure so the user can see that
     * the test failed.
     */
    class ErrorSamplerClient extends AbstractJavaSamplerClient {
        /**
         * Return SampleResult with data on error.
         *
         * @see JavaSamplerClient#runTest(JavaSamplerContext)
         */
        public SampleResult runTest(JavaSamplerContext p_context) {
            log.debug(whoAmI() + "\trunTest");
            Thread.yield();
            SampleResult results = new SampleResult();
            results.setSuccessful(false);
            results.setResponseData(("Class not found: " + getClassname()), null);
            results.setSampleLabel("ERROR: " + getClassname());
            return results;
        }
    }
}
