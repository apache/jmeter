// $Header$
/*
 * Copyright 2000-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.engine;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author     ?
 * @version    $Revision$ Updated on: $Date$
 */
public class StandardJMeterEngine
    implements JMeterEngine, JMeterThreadMonitor, Runnable, Serializable
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    private transient Thread runningThread;
    private static long WAIT_TO_DIE = 5 * 1000; //5 seconds
    transient Map allThreads;
    boolean running = false;
    boolean serialized = false;
    boolean schcdule_run = false;
    HashTree test;
    transient SearchByClass testListeners;
    String host = null;
    transient ListenerNotifier notifier;

    public StandardJMeterEngine()
    {
        allThreads = new HashMap();
    }

    public StandardJMeterEngine(String host)
    {
        this();
        this.host = host;
    }

    public void configure(HashTree testTree)
    {
        test = testTree;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    protected HashTree getTestTree()
    {
        return test;
    }

    protected void compileTree()
    {
        PreCompiler compiler = new PreCompiler();
        getTestTree().traverse(compiler);
    }

    public void runTest() throws JMeterEngineException
    {
        try
        {
            runningThread = new Thread(this);
            runningThread.start();
        }
        catch (Exception err)
        {
            stopTest();
            StringWriter string = new StringWriter();
            PrintWriter writer = new PrintWriter(string);
            err.printStackTrace(writer);
            throw new JMeterEngineException(string.toString());
        }
    }

    private void removeThreadGroups(List elements)
    {
        Iterator iter = elements.iterator();
        while (iter.hasNext())
        {
            Object item = iter.next();
            if (item instanceof ThreadGroup)
            {
                iter.remove();
            }
            else if (!(item instanceof TestElement))
            {
                iter.remove();
            }
        }
    }

    protected void setMode()
    {
        SearchByClass testPlan = new SearchByClass(TestPlan.class);
        getTestTree().traverse(testPlan);
        Object[] plan = testPlan.getSearchResults().toArray();
        ResultCollector.enableFunctionalMode(
            ((TestPlan) plan[0]).isFunctionalMode());
    }

    protected void notifyTestListenersOfStart()
    {
        Iterator iter = testListeners.getSearchResults().iterator();
        while (iter.hasNext())
        {
        	TestListener it = (TestListener)iter.next();
        	log.info("Notifying test listener: " + it.getClass().getName());
            if (host == null)
            {
                it.testStarted();
            }
            else
            {
                it.testStarted(host);
            }
        }
    }

    protected void notifyTestListenersOfEnd()
    {
        Iterator iter = testListeners.getSearchResults().iterator();
        while (iter.hasNext())
        {
            if (host == null)
            {
                ((TestListener) iter.next()).testEnded();
            }
            else
            {
                ((TestListener) iter.next()).testEnded(host);
            }
        }
        log.info("Test has ended");
    }

    private ListedHashTree cloneTree(ListedHashTree tree)
    {
        TreeCloner cloner = new TreeCloner(true);
        tree.traverse(cloner);
        return cloner.getClonedTree();
    }

    public void reset()
    {
        if (running)
        {
            stopTest();
        }
    }

    public synchronized void threadFinished(JMeterThread thread)
    {
        allThreads.remove(thread);
        if (!serialized && allThreads.size() == 0 && !schcdule_run )
        {
            stopTest();
        }
    }

    public synchronized void stopTest()
    {
        Thread stopThread = new Thread(new StopTest());
        stopThread.start();
    }

    private class StopTest implements Runnable
    {
        public void run()
        {
            if (running)
            {
                running = false;
                tellThreadsToStop();
                try
                {
                    Thread.sleep(10 * allThreads.size());
                }
                catch (InterruptedException e)
                {}
                verifyThreadsStopped();
                notifyTestListenersOfEnd();
            }
        }
    }

    public void run()
    {
        log.info("Running the test!");
        running = true;

        SearchByClass testPlan = new SearchByClass(TestPlan.class);
        getTestTree().traverse(testPlan);
        Object[] plan = testPlan.getSearchResults().toArray();
        if (plan.length == 0){
			System.err.println("Could not find the TestPlan!");
        	log.error("Could not find the TestPlan!");
        	System.exit(1);
        }
        if (((TestPlan) plan[0]).isSerialized())
        {
            serialized = true;
        }
        compileTree();
        
        /** 
         * Notification of test listeners needs to happen after function replacement, but before
         * setting RunningVersion to true.
         */
        testListeners = new SearchByClass(TestListener.class);
        getTestTree().traverse(testListeners);
        log.info("About to call test listeners");
        notifyTestListenersOfStart();
        
        getTestTree().traverse(new TurnElementsOn());
        
        List testLevelElements =
            new LinkedList(getTestTree().list(getTestTree().getArray()[0]));
        removeThreadGroups(testLevelElements);
        SearchByClass searcher = new SearchByClass(ThreadGroup.class);
        
        setMode();
        getTestTree().traverse(searcher);
        TestCompiler.initialize();
        //for each thread group, generate threads
        // hand each thread the sampler controller
        // and the listeners, and the timer
        JMeterThread[] threads;
        Iterator iter = searcher.getSearchResults().iterator();

        /*
         * Here's where the test really starts. Run a Full GC now: it's no
         * harm at all (just delays test start by a tiny amount) and
         * hitting one too early in the test can impair results for short
         * tests.
         */
        System.gc();
        
        
        notifier = new ListenerNotifier();
        
        schcdule_run = true;
        JMeterContextService.getContext().setSamplingStarted(true);
        int groupCount = 0;
        while (iter.hasNext())
        {
        	groupCount++;
            ThreadGroup group = (ThreadGroup) iter.next();
			int numThreads = group.getNumThreads();
			boolean onErrorStopTest = group.getOnErrorStopTest();
			boolean onErrorStopThread = group.getOnErrorStopThread();
			String groupName = group.getName();
			int rampUp = group.getRampUp();
			float perThreadDelay = ((float) (rampUp * 1000) / (float) numThreads);
            threads = new JMeterThread[numThreads];
			
            log.info("Starting " + numThreads + " threads for group "+ groupName
                + ". Ramp up = "+ rampUp + ".");
            
			if (onErrorStopTest) {
				log.info("Test will stop on error");
			} else if (onErrorStopThread) {
				log.info("Thread will stop on error");
			} else {
				log.info("Continue on error");
			}

            for (int i = 0; running && i < threads.length; i++)
            {
                ListedHashTree threadGroupTree =
                    (ListedHashTree) searcher.getSubTree(group);
                threadGroupTree.add(group, testLevelElements);
                threads[i] =
                    new JMeterThread(
                        cloneTree(threadGroupTree),
                        this,
                        notifier);
                threads[i].setThreadNum(i);
                threads[i].setInitialContext(JMeterContextService.getContext());
                threads[i].setInitialDelay((int) (perThreadDelay * (float) i));
                threads[i].setThreadName(groupName + (groupCount) + "-" + (i + 1));

                scheduleThread(threads[i], group);
                
                // Set up variables for stop handling
                threads[i].setEngine(this);
				threads[i].setOnErrorStopTest(onErrorStopTest);
				threads[i].setOnErrorStopThread(onErrorStopThread);
				
                Thread newThread = new Thread(threads[i]);
                newThread.setName(threads[i].getThreadName());
                allThreads.put(threads[i], newThread);
                if (serialized
                    && !iter.hasNext()
                    && i == threads.length - 1) //last thread
                {
                    serialized = false;
                }
                newThread.start();
            }
            schcdule_run = false;
            if (serialized)
            {
                while (running && allThreads.size() > 0)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {}
                }
            }
        }
    }

    /**
     * This will  schedule the time for the JMeterThread.
     * 
     * @param thread
     * @param group
     */
    private void scheduleThread(JMeterThread thread, ThreadGroup group)
    {            
        //if true the Scheduler is enabled
        if (group.getScheduler())
        {
			//set the starttime for the Thread
        	if (group.getDelay() > 0 ){// Duration is  in seconds
				thread.setStartTime(group.getDelay()*1000+(new Date().getTime()));
        	} else {
				thread.setStartTime(group.getStartTime());
        	}
            
			//set the endtime for the Thread
            if (group.getDuration() > 0){// Duration is  in seconds
				thread.setEndTime(group.getDuration()*1000+(new Date().getTime()));
            } else {
				thread.setEndTime(group.getEndTime());
            }

            //Enables the scheduler
            thread.setScheduled(true);
        }
    }

    private void verifyThreadsStopped()
    {
        Iterator iter = new HashSet(allThreads.keySet()).iterator();
        while (iter.hasNext())
        {
            Thread t = (Thread) allThreads.get(iter.next());
            if (t != null && t.isAlive())
            {
                try
                {
                    t.join(WAIT_TO_DIE);
                }
                catch (InterruptedException e)
                {}
                if (t.isAlive())
                {
                    log.info("Thread won't die: " + t.getName());
                }
            }
        }
    }

    private void tellThreadsToStop()
    {
        Iterator iter = new HashSet(allThreads.keySet()).iterator();
        while (iter.hasNext())
        {
            JMeterThread item = (JMeterThread) iter.next();
            item.stop();
            Thread t = (Thread) allThreads.get(item);
            if (t != null)
            {
                t.interrupt();
            }
            else
            {
                log.warn("Lost thread: " + item.getThreadName());
                allThreads.remove(item);
            }
        }
    }
    
	public void askThreadsToStop()
	{
		Iterator iter = new HashSet(allThreads.keySet()).iterator();
		while (iter.hasNext())
		{
			JMeterThread item = (JMeterThread) iter.next();
			item.stop();
		}
		verifyThreadsStopped();
	}

    // Remote exit
    public void exit()
    {
    	// Needs to be run in a separate thread to allow RMI call to return OK
		Thread t = new Thread(){
			public void run(){
				//log.info("Pausing");
				try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                }
				log.info("Bye");
				System.exit(0);
			};
		};
		log.info("Starting Closedown");
		t.start();
    }
}
