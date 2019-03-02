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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.java.test.JavaTest;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A sampler for executing custom Java code in each sample. See
 * {@link JavaSamplerClient} and {@link AbstractJavaSamplerClient} for
 * information on writing Java code to be executed by this sampler.
 *
 */
public class JavaSampler extends AbstractSampler implements TestStateListener, Interruptible {

    private static final Logger log = LoggerFactory.getLogger(JavaTest.class);

    private static final long serialVersionUID = 233L; // Remember to change this when the class changes ...

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList(
                    "org.apache.jmeter.protocol.java.config.gui.JavaConfigGui",
                    "org.apache.jmeter.config.gui.SimpleConfigGui"
            ));

    /**
     * Set used to register instances which implement tearDownTest.
     * This is used so that the JavaSamplerClient can be notified when the test ends.
     */
    private static final Set<JavaSampler> TEAR_DOWN_SET =
            Collections.newSetFromMap(new ConcurrentHashMap<JavaSampler,Boolean>());

    /**
     * Property key representing the classname of the JavaSamplerClient to user.
     */
    public static final String CLASSNAME = "classname";

    /**
     * Property key representing the arguments for the JavaSamplerClient.
     */
    public static final String ARGUMENTS = "arguments";

    /**
     * The JavaSamplerClient class used by this sampler.
     * Created by testStarted; copied to cloned instances.
     */
    private Class<?> javaClass;

    /**
     * If true, the JavaSamplerClient class implements tearDownTest.
     * Created by testStarted; copied to cloned instances.
     */
    private boolean isToBeRegistered;

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
     * Create a JavaSampler.
     */
    public JavaSampler() {
        setArguments(new Arguments());
    }

    /*
     * Ensure that the required class variables are cloned,
     * as this is not currently done by the super-implementation.
     */
    @Override
    public Object clone() {
        JavaSampler clone = (JavaSampler) super.clone();
        clone.javaClass = this.javaClass;
        clone.isToBeRegistered = this.isToBeRegistered;
        return clone;
    }

    private void initClass() {
        String name = getClassname().trim();
        try {
            javaClass = Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            Method method = javaClass.getMethod("teardownTest", JavaSamplerContext.class);
            isToBeRegistered = !method.getDeclaringClass().equals(AbstractJavaSamplerClient.class);
            log.info("Created class: {}. Uses tearDownTest: ", name, isToBeRegistered);
        } catch (Exception e) {
            log.error("{}\tException initialising: ", whoAmI(), name, e);
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
    @Override
    public SampleResult sample(Entry entry) {
        Arguments args = getArguments();
        args.addArgument(TestElement.NAME, getName()); // Allow Sampler access
                                                        // to test element name
        context = new JavaSamplerContext(args);
        if (javaClient == null) {
            if (log.isDebugEnabled()) {
                log.debug("{}\tCreating Java Client", whoAmI());
            }
            javaClient = createJavaClient();
            javaClient.setupTest(context);
        }

        SampleResult result = javaClient.runTest(context);

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
        if (javaClass == null) { // failed to initialise the class
            return new ErrorSamplerClient();
        }
        JavaSamplerClient client;
        try {
            client = (JavaSamplerClient) javaClass.getDeclaredConstructor().newInstance();

            if (log.isDebugEnabled()) {
                log.debug("{}\tCreated:\t{}@{}", whoAmI(), getClassname(), Integer.toHexString(client.hashCode()));
            }

            if(isToBeRegistered) {
                TEAR_DOWN_SET.add(this);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("{}\tException creating: {}", whoAmI(), getClassname(), e);
            }
            client = new ErrorSamplerClient();
        }
        return client;
    }

    /**
     * Retrieves reference to JavaSamplerClient.
     *
     * Convenience method used to check for null reference without actually
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

    // TestStateListener implementation
    /* Implements TestStateListener.testStarted() */
    @Override
    public void testStarted() {
        if (log.isDebugEnabled()) {
            log.debug("{}\ttestStarted", whoAmI());
        }
        initClass();
    }

    /* Implements TestStateListener.testStarted(String) */
    @Override
    public void testStarted(String host) {
        if (log.isDebugEnabled()) {
            log.debug("{}\ttestStarted({})", whoAmI(), host);
        }
        initClass();
    }

    /**
     * Method called at the end of the test. This is called only on one instance
     * of JavaSampler. This method will loop through all of the other
     * JavaSamplers which have been registered (automatically in the
     * constructor) and notify them that the test has ended, allowing the
     * JavaSamplerClients to cleanup.
     */
    @Override
    public void testEnded() {
        if (log.isDebugEnabled()) {
            log.debug("{}\ttestEnded", whoAmI());
        }
        synchronized (TEAR_DOWN_SET) {
            for (JavaSampler javaSampler : TEAR_DOWN_SET) {
                JavaSamplerClient client = javaSampler.javaClient;
                if (client != null) {
                    client.teardownTest(javaSampler.context);
                }
            }
            TEAR_DOWN_SET.clear();
        }
    }

    /* Implements TestStateListener.testEnded(String) */
    @Override
    public void testEnded(String host) {
        testEnded();
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
        @Override
        public SampleResult runTest(JavaSamplerContext p_context) {
            if (log.isDebugEnabled()) {
                log.debug("{}\trunTest", whoAmI());
            }
            Thread.yield();
            SampleResult results = new SampleResult();
            results.setSuccessful(false);
            results.setResponseData("Class not found: " + getClassname(), null);
            results.setSampleLabel("ERROR: " + getClassname());
            return results;
        }
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    @Override
    public boolean interrupt() {
        if (javaClient instanceof Interruptible) {
            return ((Interruptible) javaClient).interrupt();

        }
        return false;
    }
}
