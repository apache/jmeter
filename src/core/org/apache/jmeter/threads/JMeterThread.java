/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The JMeter interface to the sampling process, allowing JMeter to see the
 * timing, add listeners for sampling events and to stop the sampling process.
 *
 * @author    unattributed
 * @version   $Revision$ Last updated: $Date$
 */
public class JMeterThread implements Runnable, java.io.Serializable
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
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
    long startTime = 0; 
    long endTime = 0;
    private boolean scheduler = false;
    //based on this scheduler is enabled or disabled
    
    
    private StandardJMeterEngine engine = null; // For access to stop methods.
	private boolean onErrorStopTest;
	private boolean onErrorStopThread;
    

    public JMeterThread()
    {
    }

    public JMeterThread(
        HashTree test,
        JMeterThreadMonitor monitor,
        ListenerNotifier note)
    {
        this.monitor = monitor;
        threadVars = new JMeterVariables();
        testTree = test;
        compiler = new TestCompiler(testTree, threadVars);
        controller = (Controller) testTree.getArray()[0];
        SearchByClass threadListenerSearcher =
            new SearchByClass(TestListener.class);
        test.traverse(threadListenerSearcher);
        testListeners = threadListenerSearcher.getSearchResults();
        notifier = note;
    }

    public void setInitialContext(JMeterContext context)
    {
        threadVars.putAll(context.getVariables());
    }


    /**
     * Checks whether the JMeterThread is Scheduled.
     * author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    public boolean isScheduled()
    {
        return this.scheduler;
    }

    /**
     * Enable the scheduler for this JMeterThread.
     * author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    public void setScheduled(boolean sche)
    {
        this.scheduler = sche;
    }


    /**
     * Set the StartTime for this Thread.
     *
     * @param stime the StartTime value.
     * author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    public void setStartTime(long stime)
    {
        startTime = stime;
    }

    /**
     * Get the start time value.
     *
     * @return the start time value.
     * author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    public long getStartTime()
    {
        return startTime;
    }

    /**
     * Set the EndTime for this Thread.
     *
     * @param etime the EndTime value.
     * author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    public void setEndTime(long etime)
    {
        endTime = etime;
    }
    
    /**
     * Get the end time value.
     *
     * @return the end time  value.
     * author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    public long getEndTime()
    {
        return endTime;
    }


    /**
     * Check the scheduled time is completed.
     *
     * author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    private void stopScheduler()
    {
        long delay = System.currentTimeMillis() - endTime;
        if ((delay >= 0))
        {
            running = false;
        }
    }

    /**
     * Wait until the scheduled start time if necessary
     *
     * Author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    private void startScheduler()
    {
        long delay = (startTime - System.currentTimeMillis());
        if (delay > 0)
        {
            try
            {
                Thread.sleep(delay);
            }
            catch (Exception e)
            {
            }
        }
    }

    public void setThreadName(String threadName)
    {
        this.threadName = threadName;
    }

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

            if (scheduler)
            {
                //set the scheduler to start
                startScheduler();
            }

			rampUpDelay();
            
            log.info("Thread " + Thread.currentThread().getName() + " started");
            controller.initialize();
            controller.addIterationListener(new IterationListener());
            threadContext.setSamplingStarted(true);
            while (running)
            {
                Sampler sam;
                while (running && (sam=controller.next())!=null)
                {
                    try
                    {
                        threadContext.setCurrentSampler(sam);
                        SamplePackage pack = compiler.configureSampler(sam);
                        delay(pack.getTimers());                        
                        SampleResult result = pack.getSampler().sample(null);
                        result.setThreadName(threadName);
                        threadContext.setPreviousResult(result);
                        runPostProcessors(pack.getPostProcessors());
                        checkAssertions(pack.getAssertions(), result);
                        notifyListeners(pack.getSampleListeners(), result);
                        compiler.done(pack);
                        if (result.isStopThread() || (!result.isSuccessful() && onErrorStopThread)){
                        	stopThread();
                        }
                        if (result.isStopTest() || (!result.isSuccessful() && onErrorStopTest)){
                        	stopTest();
                        }
                        if (scheduler)
                        {
                            //checks the scheduler to stop the iteration
                            stopScheduler();
                        }

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
            }
        }
        finally
        {
            threadContext.clear();
            log.info("Thread " + threadName + " is done");
            monitor.threadFinished(this);
        }
    }

    public String getThreadName()
    {
        return threadName;
    }

    public void stop()
    {
        running = false;
        log.info("Stopping " + threadName);
    }
	private void stopTest()
	{
		running = false;
		log.info("Stop Test detected by thread " + threadName);
		//engine.stopTest();
		if (engine != null ) engine.askThreadsToStop();
	}
	private void stopThread()
	{
		running = false;
		log.info("Stop Thread detected by thread " + threadName);
	}


    private void checkAssertions(List assertions, SampleResult result)
    {
        Iterator iter = assertions.iterator();
        while (iter.hasNext())
        {
            AssertionResult assertion =
                ((Assertion) iter.next()).getResult(result);
            result.setSuccessful(
                result.isSuccessful()
                    && !(assertion.isError() || assertion.isFailure()));
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
                listener.testIterationStart(
                    new LoopIterationEvent(
                        controller,
                        threadVars.getIteration()));
                ((TestElement)listener).recoverRunningVersion();
            }
            else
            {
                listener.testIterationStart(
                    new LoopIterationEvent(
                        controller,
                        threadVars.getIteration()));
            }
        }
    }

    private void notifyListeners(List listeners, SampleResult result)
    {
        SampleEvent event =
            new SampleEvent(
                result,
                controller.getPropertyAsString(TestElement.NAME));
        compiler.sampleOccurred(event);
        notifier.notifyListeners(event, listeners);

    }
    public void setInitialDelay(int delay)
    {
        initialDelay = delay;
    }

    /**
     * Initial delay if ramp-up period is active for this group.
     */
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
     */
    public int getThreadNum()
    {
        return threadNum;
    }

    /**
     * Sets the threadNum.
     * @param threadNum the threadNum to set
     */
    public void setThreadNum(int threadNum)
    {
        this.threadNum = threadNum;
    }
    
    private class IterationListener implements LoopIterationListener
    {
        /* (non-Javadoc)
         * @see LoopIterationListener#iterationStart(LoopIterationEvent)
         */
        public void iterationStart(LoopIterationEvent iterEvent)
        {
            notifyTestListeners();
        }
    }
    /**
     * Save the engine instance for access to the stop methods
     * 
     * @param engine
     */
    public void setEngine(StandardJMeterEngine engine)
    {
        this.engine = engine;
    }

    /**
     * Should Test stop on sampler error?
     * 
     * @param b - true or false
     */
    public void setOnErrorStopTest(boolean b)
    {
        onErrorStopTest = b;
    }

    /**
     * Should Thread stop on Sampler error?
     * 
     * @param b - true or false
     */
    public void setOnErrorStopThread(boolean b)
    {
        onErrorStopThread = b;
    }

}