/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.threads;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.SearchByClass;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JMeterStopTestException;
import org.apache.jorphan.util.JMeterStopThreadException;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * The JMeter interface to the sampling process, allowing JMeter to see the
 * timing, add listeners for sampling events and to stop the sampling process.
 * 
 */
public class JMeterThread implements Runnable, java.io.Serializable {
	private static final Logger log = LoggingManager.getLoggerForClass();

	// NOT USED private static Map samplers = new HashMap();
	private int initialDelay = 0;

	private Controller controller;

	private boolean running;

	private HashTree testTree;

	private transient TestCompiler compiler;

	private JMeterThreadMonitor monitor;

	private String threadName;

	private transient JMeterContext threadContext;

	private transient JMeterVariables threadVars;

	private Collection testListeners;

	private transient ListenerNotifier notifier;

	private int threadNum = 0;

	private long startTime = 0;

	private long endTime = 0;

	private boolean scheduler = false;

	// based on this scheduler is enabled or disabled

	private ThreadGroup threadGroup; // Gives access to parent thread
										// threadGroup

	private StandardJMeterEngine engine = null; // For access to stop methods.

	private boolean onErrorStopTest;

	private boolean onErrorStopThread;

	public static final String PACKAGE_OBJECT = "JMeterThread.pack"; // $NON-NLS-1$

	public static final String LAST_SAMPLE_OK = "JMeterThread.last_sample_ok"; // $NON-NLS-1$

	public JMeterThread() {
	}

	public JMeterThread(HashTree test, JMeterThreadMonitor monitor, ListenerNotifier note) {
		this.monitor = monitor;
		threadVars = new JMeterVariables();
		testTree = test;
		compiler = new TestCompiler(testTree, threadVars);
		controller = (Controller) testTree.getArray()[0];
		SearchByClass threadListenerSearcher = new SearchByClass(TestListener.class);
		test.traverse(threadListenerSearcher);
		testListeners = threadListenerSearcher.getSearchResults();
		notifier = note;
		running = true;
	}

	public void setInitialContext(JMeterContext context) {
		threadVars.putAll(context.getVariables());
	}

	/**
	 * Checks whether the JMeterThread is Scheduled. author
	 * T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	public boolean isScheduled() {
		return this.scheduler;
	}

	/**
	 * Enable the scheduler for this JMeterThread. author
	 * T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	public void setScheduled(boolean sche) {
		this.scheduler = sche;
	}

	/**
	 * Set the StartTime for this Thread.
	 * 
	 * @param stime
	 *            the StartTime value. author
	 *            T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	public void setStartTime(long stime) {
		startTime = stime;
	}

	/**
	 * Get the start time value.
	 * 
	 * @return the start time value. author
	 *         T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Set the EndTime for this Thread.
	 * 
	 * @param etime
	 *            the EndTime value. author
	 *            T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	public void setEndTime(long etime) {
		endTime = etime;
	}

	/**
	 * Get the end time value.
	 * 
	 * @return the end time value. author
	 *         T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * Check the scheduled time is completed.
	 * 
	 * author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	private void stopScheduler() {
		long delay = System.currentTimeMillis() - endTime;
		if ((delay >= 0)) {
			running = false;
		}
	}

	/**
	 * Wait until the scheduled start time if necessary
	 * 
	 * Author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	private void startScheduler() {
		long delay = (startTime - System.currentTimeMillis());
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (Exception e) {
			}
		}
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	/*
	 * See below for reason for this change. Just in case this causes problems,
	 * allow the change to be backed out
	 */
	private static final boolean startEarlier = org.apache.jmeter.util.JMeterUtils.getPropDefault(
			"jmeterthread.startearlier", true);

	static {
		if (startEarlier) {
			log.warn("jmeterthread.startearlier=true (see jmeter.properties)");
		} else {
			log.info("jmeterthread.startearlier=false (see jmeter.properties)");
		}
	}

	public void run() {
		try {
			initRun();
			while (running) {
				Sampler sam;
				while (running && (sam = controller.next()) != null) {
					try {
						threadContext.setCurrentSampler(sam);
						SamplePackage pack = compiler.configureSampler(sam);

						// Hack: save the package for any transaction
						// controllers
						threadContext.getVariables().putObject(PACKAGE_OBJECT, pack);

						delay(pack.getTimers());
						Sampler sampler = pack.getSampler();
						sampler.setThreadContext(threadContext);
						sampler.setThreadName(threadName);
						TestBeanHelper.prepare(sampler);
						SampleResult result = sampler.sample(null); // TODO:
																	// remove
																	// this
																	// useless
																	// Entry
																	// parameter
						if (result != null) {
							result.setThreadName(threadName);
							threadContext.setPreviousResult(result);
							runPostProcessors(pack.getPostProcessors());
							checkAssertions(pack.getAssertions(), result);
							notifyListeners(pack.getSampleListeners(), result);
							compiler.done(pack);
							if (result.isStopThread() || (!result.isSuccessful() && onErrorStopThread)) {
								stopThread();
							}
							if (result.isStopTest() || (!result.isSuccessful() && onErrorStopTest)) {
								stopTest();
							}
						}
						if (scheduler) {
							// checks the scheduler to stop the iteration
							stopScheduler();
						}

					} catch (JMeterStopTestException e) {
						log.info("Stopping Test: " + e.toString());
						stopTest();
					} catch (JMeterStopThreadException e) {
						log.info("Stopping Thread: " + e.toString());
						stopThread();
					} catch (Exception e) {
						log.error("", e);
					}
				}
				if (controller.isDone()) {
					running = false;
				}
			}
		}
		// Might be found by contoller.next()
		catch (JMeterStopTestException e) {
			log.info("Stopping Test: " + e.toString());
			stopTest();
		} catch (JMeterStopThreadException e) {
			log.info("Stop Thread seen: " + e.toString());
		} catch (Exception e) {
			log.error("Test failed!", e);
		} catch (ThreadDeath e) {
			throw e; // Must not ignore this one
		} catch (Error e) {// Make sure errors are output to the log file
			log.error("Test failed!", e);
		} finally {
			threadContext.clear();
			log.info("Thread " + threadName + " is done");
			monitor.threadFinished(this);
			threadFinished();
		}
	}

	/**
	 * 
	 */
	protected void initRun() {
		threadContext = JMeterContextService.getContext();
		threadContext.setVariables(threadVars);
		threadContext.setThreadNum(getThreadNum());
		threadContext.getVariables().put(LAST_SAMPLE_OK, "true");
		threadContext.setThread(this);
        threadContext.setThreadGroup(threadGroup);
		testTree.traverse(compiler);
		// listeners = controller.getListeners();
		if (scheduler) {
			// set the scheduler to start
			startScheduler();
		}
		rampUpDelay();
		log.info("Thread " + Thread.currentThread().getName() + " started");
        JMeterContextService.incrNumberOfThreads();
        GuiPackage.getInstance().getMainFrame().updateCounts();
        threadGroup.incrNumberOfThreads();
		/*
		 * Setting SamplingStarted before the contollers are initialised allows
		 * them to access the running values of functions and variables (however
		 * it does not seem to help with the listeners)
		 */
		if (startEarlier)
			threadContext.setSamplingStarted(true);
		controller.initialize();
		controller.addIterationListener(new IterationListener());
		if (!startEarlier)
			threadContext.setSamplingStarted(true);
		threadStarted();
	}

	/**
	 * 
	 */
	private void threadStarted() {
		Traverser startup = new Traverser(true);
		testTree.traverse(startup);
	}

	/**
	 * 
	 */
	private void threadFinished() {
		Traverser shut = new Traverser(false);
		testTree.traverse(shut);
		JMeterContextService.decrNumberOfThreads();
        GuiPackage.getInstance().getMainFrame().updateCounts();
        threadGroup.decrNumberOfThreads();
	}

	private static class Traverser implements HashTreeTraverser {
		private boolean isStart = false;

		private Traverser(boolean start) {
			isStart = start;
		}

		public void addNode(Object node, HashTree subTree) {
			if (node instanceof TestElement) {
				TestElement te = (TestElement) node;
				if (isStart) {
					te.threadStarted();
				} else {
					te.threadFinished();
				}
			}
		}

		public void subtractNode() {
		}

		public void processPath() {
		}
	}

	public String getThreadName() {
		return threadName;
	}

	public void stop() {
		running = false;
		log.info("Stopping " + threadName);
	}

	private void stopTest() {
		running = false;
		log.info("Stop Test detected by thread " + threadName);
		// engine.stopTest();
		if (engine != null)
			engine.askThreadsToStop();
	}

	private void stopThread() {
		running = false;
		log.info("Stop Thread detected by thread " + threadName);
	}

	public void pauseThread(int milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
		}
	}

	private void checkAssertions(List assertions, SampleResult result) {
		Iterator iter = assertions.iterator();
		while (iter.hasNext()) {
			Assertion assertion = (Assertion) iter.next();
			TestBeanHelper.prepare((TestElement) assertion);
			AssertionResult assertionResult = assertion.getResult(result);
			result.setSuccessful(result.isSuccessful() && !(assertionResult.isError() || assertionResult.isFailure()));
			result.addAssertionResult(assertionResult);
		}
		threadContext.getVariables().put(LAST_SAMPLE_OK, JOrphanUtils.booleanToString(result.isSuccessful()));
	}

	private void runPostProcessors(List extractors) {
		ListIterator iter = extractors.listIterator(extractors.size());
		while (iter.hasPrevious()) {
			PostProcessor ex = (PostProcessor) iter.previous();
			TestBeanHelper.prepare((TestElement) ex);
			ex.process();
		}
	}

	private void delay(List timers) {
		long sum = 0;
		Iterator iter = timers.iterator();
		while (iter.hasNext()) {
			Timer timer = (Timer) iter.next();
			TestBeanHelper.prepare((TestElement) timer);
			sum += timer.delay();
		}
		if (sum > 0) {
			try {
				Thread.sleep(sum);
			} catch (InterruptedException e) {
				log.error("", e);
			}
		}
	}

	private void notifyTestListeners() {
		threadVars.incIteration();
		Iterator iter = testListeners.iterator();
		while (iter.hasNext()) {
			TestListener listener = (TestListener) iter.next();
			if (listener instanceof TestElement) {
				listener.testIterationStart(new LoopIterationEvent(controller, threadVars.getIteration()));
				((TestElement) listener).recoverRunningVersion();
			} else {
				listener.testIterationStart(new LoopIterationEvent(controller, threadVars.getIteration()));
			}
		}
	}

	private void notifyListeners(List listeners, SampleResult result) {
		SampleEvent event = new SampleEvent(result, controller.getPropertyAsString(TestElement.NAME));
		compiler.sampleOccurred(event);
		notifier.notifyListeners(event, listeners);

	}

	public void setInitialDelay(int delay) {
		initialDelay = delay;
	}

	/**
	 * Initial delay if ramp-up period is active for this threadGroup.
	 */
	private void rampUpDelay() {
		if (initialDelay > 0) {
			try {
				Thread.sleep(initialDelay);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Returns the threadNum.
	 */
	public int getThreadNum() {
		return threadNum;
	}

	/**
	 * Sets the threadNum.
	 * 
	 * @param threadNum
	 *            the threadNum to set
	 */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	private class IterationListener implements LoopIterationListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see LoopIterationListener#iterationStart(LoopIterationEvent)
		 */
		public void iterationStart(LoopIterationEvent iterEvent) {
			notifyTestListeners();
		}
	}

	/**
	 * Save the engine instance for access to the stop methods
	 * 
	 * @param engine
	 */
	public void setEngine(StandardJMeterEngine engine) {
		this.engine = engine;
	}

	/**
	 * Should Test stop on sampler error?
	 * 
	 * @param b -
	 *            true or false
	 */
	public void setOnErrorStopTest(boolean b) {
		onErrorStopTest = b;
	}

	/**
	 * Should Thread stop on Sampler error?
	 * 
	 * @param b -
	 *            true or false
	 */
	public void setOnErrorStopThread(boolean b) {
		onErrorStopThread = b;
	}

	public ThreadGroup getThreadGroup() {
		return threadGroup;
	}

	public void setThreadGroup(ThreadGroup group) {
		this.threadGroup = group;
	}

}