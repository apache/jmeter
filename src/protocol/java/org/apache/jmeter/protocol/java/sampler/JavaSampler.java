/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002,2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/**
 * A sampler for executing custom Java code in each sample.  See
 * {@link JavaSamplerClient} and {@link AbstractJavaSamplerClient} for
 * information on writing Java code to be executed by this sampler.
 * 
 * @author <a href="mailto:jeremy_a@bigfoot.com">Jeremy Arnold</a>
 * @version $Revision$
 */
public class JavaSampler extends AbstractSampler implements TestListener
{
    /**
     * Property key representing the classname of the JavaSamplerClient to
     * user.
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
     * The JavaSamplerContext instance used by this sampler to hold
     * information related to the test run, such as the parameters
     * specified for the sampler client.
     */
    private transient JavaSamplerContext context = null;

    /**
     * Logging
     */
    private static transient Logger log =
        Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.protocol.java");

    /**
     * Set used to register all active JavaSamplers.  This is used so that the
     * samplers can be notified when the test ends.
     */
    private static Set allSamplers = new HashSet();

    /**
     * Create a JavaSampler.
     */
    public JavaSampler()
    {
        setArguments(new Arguments());
        synchronized (allSamplers)
        {
            allSamplers.add(this);
        }
    }

    /**
     * Set the arguments (parameters) for the JavaSamplerClient to be executed
     * with.
     *
     * @param args  the new arguments.  These replace any existing arguments.
     */
    public void setArguments(Arguments args)
    {
        setProperty(new TestElementProperty(ARGUMENTS, args));
    }

    /**
     * Get the arguments (parameters) for the JavaSamplerClient to be executed
     * with.
     *
     * @return the arguments
     */
    public Arguments getArguments()
    {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }

    /**
     * Releases Java Client.
     */
    private void releaseJavaClient ()
    {
        if (javaClient != null)
        {
            javaClient.teardownTest(context);
        }
        javaClient = null;
        context = null;
    }

    /**
     * Sets the Classname attribute of the JavaConfig object
     *
     * @param  classname  the new Classname value
     */
    public void setClassname(String classname)
    {
        setProperty(CLASSNAME, classname);
    }

    /**
     * Gets the Classname attribute of the JavaConfig object
     *
     * @return  the Classname value
     */
    public String getClassname()
    {
        return getPropertyAsString(CLASSNAME);
    }

    /**
     * Performs a test sample.
     * 
     * The <code>sample()</code> method retrieves the reference to the
     * Java client and calls its <code>runTest()</code> method.
     *
     * @see JavaSamplerClient#runTest(JavaSamplerContext)
     * 
     * @param entry  the Entry for this sample
     * @return       test SampleResult
     */
    public SampleResult sample(Entry entry)
    {
        if (javaClient == null)
        {
            log.debug(whoAmI() + "Creating Java Client");
            createJavaClient();
            javaClient.setupTest(context);
        }

        return createJavaClient().runTest(context);
    }

    /**
     * Returns reference to <code>JavaSamplerClient</code>.
     * 
     * The <code>createJavaClient()</code> method uses reflection
     * to create an instance of the specified Java protocol client.
     * If the class can not be found, the method returns a reference
     * to <code>this</code> object.
     * 
     * @return JavaSamplerClient reference.
     */
    private JavaSamplerClient createJavaClient()
    {
        if (javaClient == null)
        {
            try
            {
                Class javaClass = Class.forName(getClassname().trim(),
                        false,
                        Thread.currentThread().getContextClassLoader());
                javaClient = (JavaSamplerClient) javaClass.newInstance();
                context = new JavaSamplerContext(getArguments());

                if (log.isDebugEnabled())
                {
                    log.debug(
                        whoAmI()
                            + "\tCreated:\t"
                            + getClassname()
                            + "@"
                            + Integer.toHexString(javaClient.hashCode()));
                }
            }
            catch (Exception e)
            {
                log.error(
                    whoAmI() + "\tException creating: " + getClassname(),
                    e);
                javaClient = new ErrorSamplerClient();
            }
        }
        return javaClient;
    }

    /**
     * Retrieves reference to JavaSamplerClient.
     * 
     * Convience method used to check for null reference without
     * actually creating a JavaSamplerClient
     * 
     * @return reference to JavaSamplerClient
     */
    private JavaSamplerClient retrieveJavaClient()
    {
        return javaClient;
    }

    /**
     * Generate a String identifier of this instance for debugging
     * purposes.
     * 
     * @return  a String identifier for this sampler instance
     */
    private String whoAmI()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(Thread.currentThread().getName());
        sb.append("@");
        sb.append(Integer.toHexString(hashCode()));
        return sb.toString();
    }

    //  TestListener implementation
    /* Implements TestListener.testStarted() */
    public void testStarted()
    {
        log.debug(whoAmI() + "\ttestStarted");
    }

    /* Implements TestListener.testStarted(String) */
    public void testStarted(String host)
    {
        log.debug(whoAmI() + "\ttestStarted(" + host + ")");
    }

    /**
     * Method called at the end of the test.  This is called only on one
     * instance of JavaSampler.  This method will loop through all of the
     * other JavaSamplers which have been registered (automatically in the
     * constructor) and notify them that the test has ended, allowing the
     * JavaSamplerClients to cleanup.
     */
    public void testEnded()
    {
        log.debug(whoAmI() + "\ttestEnded");
        synchronized (allSamplers)
        {
            Iterator i = allSamplers.iterator();
            while (i.hasNext())
            {
                JavaSampler sampler = (JavaSampler) i.next();
                sampler.releaseJavaClient();
                i.remove();
            }
        }
    }

    /* Implements TestListener.testEnded(String) */
    public void testEnded(String host)
    {
        testEnded();
    }

    /* Implements TestListener.testIterationStart(LoopIterationEvent) */
    public void testIterationStart(LoopIterationEvent event)
    {
    }

    /**
     * A {@link JavaSamplerClient} implementation used for error handling.  If
     * an error occurs while creating the real JavaSamplerClient object, it is
     * replaced with an instance of this class.  Each time a sample occurs with
     * this class, the result is marked as a failure so the user can see that
     * the test failed.
     */
    class ErrorSamplerClient extends AbstractJavaSamplerClient
    {
        /**
         * Return SampleResult with data on error.
         * @see JavaSamplerClient#runTest()
         */
        public SampleResult runTest(JavaSamplerContext context)
        {
            log.debug(whoAmI() + "\trunTest");
            Thread.yield();
            SampleResult results = new SampleResult();
            results.setTime(0);
            results.setSuccessful(false);
            results.setResponseData(
                ("Class not found: " + getClassname()).getBytes());
            results.setSampleLabel("ERROR: " + getClassname());
            return results;
        }
    }
}
