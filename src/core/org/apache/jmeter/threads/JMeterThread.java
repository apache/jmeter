/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.threads;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
/****************************************
 * The JMeter interface to the sampling process, allowing JMeter to see the
 * timing, add listeners for sampling events and to stop the sampling process.
 *
 *@author    $Author$
 *@created   $Date$
 *@version   $Revision$
 ***************************************/
public class JMeterThread implements Runnable, java.io.Serializable
{
    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.engine");
    static Map samplers = new HashMap();
    int initialDelay = 0;
    Controller controller;
    private boolean running;
    HashTree testTree;
    TestCompiler compiler;
    JMeterThreadMonitor monitor;
    String threadName;
    JMeterContext threadContext;
    JMeterVariables threadVars;
    Collection testListeners;
    ListenerNotifier notifier;
    int threadNum = 0;

    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public JMeterThread()
    {}
    public JMeterThread(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note)
    {
        this.monitor = monitor;
        threadVars = new JMeterVariables();
        testTree = test;
        compiler = new TestCompiler(testTree, threadVars);
        controller = (Controller) testTree.getArray()[0];
        SearchByClass threadListenerSearcher = new SearchByClass(TestListener.class);
        test.traverse(threadListenerSearcher);
        testListeners = threadListenerSearcher.getSearchResults();
        notifier = note;
    }

    public void setInitialContext(JMeterContext context)
    {
        threadVars.putAll(context.getVariables());
    }

    public void setThreadName(String threadName)
    {
        this.threadName = threadName;
    }
    /****************************************
     * !ToDo (Method description)
     ***************************************/
    public void run()
    {
        try
        {
            threadContext = JMeterContextService.getContext();
            threadContext.setVariables(threadVars);
            threadContext.setThreadNum(getThreadNum());
            testTree.traverse(compiler);
            running = true;
            //listeners = controller.getListeners();
            Sampler entry = null;
            rampUpDelay();
            log.info("Thread " + Thread.currentThread().getName() + " started");
            controller.initialize();
            int i=0;
            while (running)
            {
            	Sampler sam;
                while (running && (sam=controller.next())!=null)
                {
                    try
                    {
                        if (i==0)
                        {
                            notifyTestListeners();
                        }
                        threadContext.setCurrentSampler(sam);
                        SamplePackage pack = compiler.configureSampler(sam);
                        delay(pack.getTimers());
                        SampleResult result = pack.getSampler().sample(null);
                        result.setThreadName(threadName);
                        result.setTimeStamp(System.currentTimeMillis());
                        threadContext.setPreviousResult(result);
                        checkAssertions(pack.getAssertions(), result);
                        runPostProcessors(pack.getPostProcessors());
                        notifyListeners(pack.getSampleListeners(), result);
                        compiler.done(pack);
                        i++;
                    }
                    catch (Exception e)
                    {
                        log.error("", e);
                    }
                }
                if (controller.isDone())
                {
                    running = false;
                }
                else
                {
                	i=0;
                	controller.initialize();
                }
            }
        }
        finally
        {
            log.info("Thread " + threadName + " is done");
            monitor.threadFinished(this);
        }
    }

    public String getThreadName()
    {
        return threadName;
    }

    /****************************************
     * !ToDo (Method description)
     ***************************************/
    public void stop()
    {
        running = false;
        log.info("stopping " + threadName);
    }
    private void checkAssertions(List assertions, SampleResult result)
    {
        Iterator iter = assertions.iterator();
        while (iter.hasNext())
        {
            AssertionResult assertion = ((Assertion) iter.next()).getResult(result);
            result.setSuccessful(result.isSuccessful() && !(assertion.isError() || assertion.isFailure()));
            result.addAssertionResult(assertion);
        }
    }

    private void runPostProcessors(List extractors)
    {
        ListIterator iter = extractors.listIterator(extractors.size());
        while (iter.hasPrevious())
        {
            PostProcessor ex = (PostProcessor) iter.previous();
            ex.process();
        }
    }

    private void delay(List timers)
    {
        int sum = 0;
        Iterator iter = timers.iterator();
        while (iter.hasNext())
        {
            sum += ((Timer) iter.next()).delay();
        }
        if (sum > 0)
        {
            try
            {
                Thread.sleep(sum);
            }
            catch (InterruptedException e)
            {
                log.error("", e);
            }
        }
    }

    private void notifyTestListeners()
    {
        threadVars.incIteration();
        Iterator iter = testListeners.iterator();
        while (iter.hasNext())
        {
            TestListener listener = (TestListener)iter.next();
            if(listener instanceof TestElement)
            {
                listener.testIterationStart(new LoopIterationEvent(controller,threadVars.getIteration()));
                ((TestElement)listener).recoverRunningVersion();
            }
            else
            {
                listener.testIterationStart(new LoopIterationEvent(controller,threadVars.getIteration()));
            }
            
        }
    }

    private void notifyListeners(List listeners, SampleResult result)
    {
        SampleEvent event = new SampleEvent(result,controller.getPropertyAsString(TestElement.NAME));
        compiler.sampleOccurred(event);
        notifier.notifyListeners(event, listeners);

    }
    public void setInitialDelay(int delay)
    {
        initialDelay = delay;
    }
    /****************************************
     * Initial delay if ramp-up period is active for this group
     ***************************************/
    private void rampUpDelay()
    {
        if (initialDelay > 0)
        {
            try
            {
                Thread.sleep(initialDelay);
            }
            catch (InterruptedException e)
            {}
        }
    }
    /**
     * Returns the threadNum.
     * @return int
     */
    public int getThreadNum()
    {
        return threadNum;
    }

    /**
     * Sets the threadNum.
     * @param threadNum The threadNum to set
     */
    public void setThreadNum(int threadNum)
    {
        this.threadNum = threadNum;
    }

}