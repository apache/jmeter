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
import java.util.Map;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.ListedHashTree;
import org.apache.jmeter.util.SearchByClass;
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
public class JMeterThread implements Runnable, java.io.Serializable {
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.engine");
	static Map samplers = new HashMap();
	int initialDelay = 0;
	Controller controller;
	private boolean running;
	ListedHashTree testTree;
	TestCompiler compiler;
	JMeterThreadMonitor monitor;
	String threadName;
	JMeterVariables threadVars;
	Collection threadListeners;
	ListenerNotifier notifier;
	
	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public JMeterThread() {}
	public JMeterThread(ListedHashTree test, JMeterThreadMonitor monitor,ListenerNotifier note) {
		this.monitor = monitor;
		threadVars = new JMeterVariables();
		testTree = test;
		compiler = new TestCompiler(testTree,threadVars);
		controller = (Controller) testTree.getArray()[0];
		SearchByClass threadListenerSearcher = new SearchByClass(ThreadListener.class);
		test.traverse(threadListenerSearcher);
		threadListeners = threadListenerSearcher.getSearchResults();
		notifier = note;
	}

	public void setThreadName(String threadName)
	{
		this.threadName = threadName;
	}
	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void run() {
		try
		{
			initializeThreadListeners();
			testTree.traverse(compiler);
			running = true;
			//listeners = controller.getListeners();
			Sampler entry = null;
			rampUpDelay();
			log.info("Thread "+Thread.currentThread().getName()+" started");
			while (running) {
				notifyThreadListeners();
				while (controller.hasNext() && running) {
					try
					{
						SamplePackage pack = compiler.configureSampler(controller.next());
						delay(pack.getTimers());
						SampleResult result = pack.getSampler().sample(null);
						result.setThreadName(threadName);
						result.setTimeStamp(System.currentTimeMillis());
						checkAssertions(pack.getAssertions(), result);
						notifyListeners(pack.getSampleListeners(), result);
					}
					catch(Exception e)
					{
						log.error("",e);
					}
				}
				if (controller.isDone()) {
					running = false;
				}
			}
		}
		finally
		{
			log.info("Thread "+threadName+" is done");
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
	public void stop() {
		running = false;		
		log.info("stopping "+threadName);
	}
	private void checkAssertions(List assertions, SampleResult result) {
		Iterator iter = assertions.iterator();
		while (iter.hasNext()) {
			AssertionResult assertion = ((Assertion)iter.next()).getResult(result);
			result.setSuccessful(result.isSuccessful() &&
					!(assertion.isError() || assertion.isFailure()));
			result.addAssertionResult(assertion);
		}
	}
	private void delay(List timers) {
		int sum = 0;
		Iterator iter = timers.iterator();
		while (iter.hasNext()) {
			sum += ((Timer) iter.next()).delay();
		}
		if(sum > 0)
		{
			try {
				Thread.sleep(sum);
			}
			catch (InterruptedException e) {
				log.error("",e);
			}
		}
	}
	
	private void initializeThreadListeners()
	{
		Iterator iter = threadListeners.iterator();
		while(iter.hasNext())
		{
			((ThreadListener)iter.next()).setJMeterVariables(threadVars);
		}
	}
	
	private void notifyThreadListeners()
	{
		threadVars.incIteration();
		Iterator iter = threadListeners.iterator();
		while(iter.hasNext())
		{
			((ThreadListener)iter.next()).iterationStarted(threadVars.getIteration());
		}
	}
	
	private void notifyListeners(List listeners, SampleResult result) {
		SampleEvent event =
			new SampleEvent(result, (String) controller.getProperty(TestElement.NAME));
		compiler.sampleOccurred(event);
		notifier.addLast(event,listeners);
	}
	public void setInitialDelay(int delay) {
		initialDelay = delay;
	}
	/****************************************
	 * Initial delay if ramp-up period is active for this group
	 ***************************************/
	private void rampUpDelay() {
		if (initialDelay > 0) {
			try {
				Thread.sleep(initialDelay);
			}
			catch (InterruptedException e) {
			}
		}
	}
}