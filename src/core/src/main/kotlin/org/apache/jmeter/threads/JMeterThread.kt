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

package org.apache.jmeter.threads

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.jmeter.assertions.Assertion
import org.apache.jmeter.assertions.AssertionResult
import org.apache.jmeter.control.Controller
import org.apache.jmeter.control.IteratingController
import org.apache.jmeter.control.TransactionSampler
import org.apache.jmeter.engine.StandardJMeterEngine
import org.apache.jmeter.engine.event.LoopIterationEvent
import org.apache.jmeter.engine.event.LoopIterationListener
import org.apache.jmeter.gui.GuiPackage
import org.apache.jmeter.processor.PostProcessor
import org.apache.jmeter.processor.PreProcessor
import org.apache.jmeter.samplers.Interruptible
import org.apache.jmeter.samplers.SampleEvent
import org.apache.jmeter.samplers.SampleListener
import org.apache.jmeter.samplers.SampleMonitor
import org.apache.jmeter.samplers.SampleResult
import org.apache.jmeter.samplers.Sampler
import org.apache.jmeter.samplers.SuspendingSampler
import org.apache.jmeter.testbeans.TestBeanHelper
import org.apache.jmeter.testelement.AbstractScopedAssertion
import org.apache.jmeter.testelement.AbstractTestElement
import org.apache.jmeter.testelement.TestElement
import org.apache.jmeter.testelement.TestIterationListener
import org.apache.jmeter.testelement.ThreadListener
import org.apache.jmeter.threads.JMeterContext.TestLogicalAction
import org.apache.jmeter.timers.Timer
import org.apache.jmeter.timers.TimerService
import org.apache.jmeter.util.JMeterUtils
import org.apache.jorphan.collections.HashTree
import org.apache.jorphan.collections.HashTreeTraverser
import org.apache.jorphan.collections.ListedHashTree
import org.apache.jorphan.collections.SearchByClass
import org.apache.jorphan.util.JMeterError
import org.apache.jorphan.util.JMeterStopTestException
import org.apache.jorphan.util.JMeterStopTestNowException
import org.apache.jorphan.util.JMeterStopThreadException
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.roundToLong

/**
 * The JMeter interface to the sampling process, allowing JMeter to see the
 * timing, add listeners for sampling events and to stop the sampling process.
 */
class JMeterThread @JvmOverloads constructor(
    private val testTree: HashTree,
    private val monitor: JMeterThreadMonitor?,
    /**
     * @return [ListenerNotifier]
     */
    val notifier: ListenerNotifier?,
    private val isSameUserOnNextIteration: Boolean = false
) : Runnable, Interruptible {

    private val threadGroupLoopController = testTree.array[0] as Controller

    private val compiler = TestCompiler(testTree)

    private val threadVars = JMeterVariables()

    // Note: this is only used to implement TestIterationListener#testIterationStart
    // Since this is a frequent event, it makes sense to create the list once rather than scanning each time
    // The memory used will be released when the thread finishes
    private val testIterationStartListeners = testTree.find<TestIterationListener>()

    private val sampleMonitors = testTree.find<SampleMonitor>()

    /*
     * The following variables are set by StandardJMeterEngine.
     * This is done before start() is called, so the values will be published to the thread safely
     * TODO - consider passing them to the constructor, so that they can be made final
     * (to avoid adding lots of parameters, perhaps have a parameter wrapper object.
     */
    var threadName: String? = null

    private var initialDelay = 0

    /**
     * Index of the thread within its ThreadGroup
     */
    var threadNum = 0

    /**
     * Get the start time value.
     */
    var startTime: Long = 0

    /**
     * Thread will stop running when the time reaches [endTime]
     */
    var endTime: Long = 0

    // based on this scheduler is enabled or disabled
    private var scheduler = false

    // Gives access to parent thread threadGroup
    private lateinit var threadGroup: AbstractThreadGroup

    private var engine: StandardJMeterEngine? = null // For access to stop methods.

    /*
     * The following variables may be set/read from multiple threads.
     */
    @Volatile
    private var running: Boolean = true // may be set from a different thread

    @Volatile
    private var onErrorStopTest: Boolean = false

    @Volatile
    private var onErrorStopTestNow: Boolean = false

    @Volatile
    private var onErrorStopThread: Boolean = false

    @Volatile
    private var onErrorStartNextLoop: Boolean = false

    @Volatile
    private var currentSamplerForInterruption: Sampler? = null

    private val interruptLock =
        ReentrantLock() // ensure that interrupt cannot overlap with shutdown

    private inline fun <reified T> HashTree.find() =
        SearchByClass(T::class.java).let {
            traverse(it)
            it.searchResults
        }

    fun setInitialContext(context: JMeterContext) {
        threadVars.putAll(context.variables)
    }

    /**
     * Enable the scheduler for this JMeterThread.
     *
     * @param sche
     * flag whether the scheduler should be enabled
     */
    fun setScheduled(sche: Boolean) {
        this.scheduler = sche
    }

    /**
     * Check if the scheduled time is completed.
     */
    private fun stopSchedulerIfNeeded() {
        val now = System.currentTimeMillis()
        if (now >= endTime) {
            running = false
            log.info("Stopping because end time detected by thread: {}", threadName)
        }
    }

    /**
     * Wait until the scheduled start time if necessary
     */
    private suspend fun startScheduler() {
        val delay = startTime - System.currentTimeMillis()
        delayBy(delay, "startScheduler")
    }

    override fun run() {
        // By default, runBlocking uses "current thread" for dispatching the coroutines
        // So it inherits ThreadLocals, and we don't need to pass JMeterContext explicitly
        runBlocking {
            runSuspend()
        }
    }

    private fun CoroutineScope.interruptSamplerOnCancellation() =
        launch(Dispatchers.Default) {
            suspendCancellableCoroutine<Unit> { continuation ->
                continuation.invokeOnCancellation {
                    // Interrupt interruptible samplers
                    try {
                        interrupt()
                    } catch (e: Exception) {
                        log.info("Unable to interrupt sampler in thread $threadName", e)
                    }
                }
            }
        }

    internal suspend fun runSuspend() = coroutineScope {
        // threadContext is not thread-safe, so keep within thread
        val threadContext = JMeterContextService.getContext()
        var iterationListener: LoopIterationListener? = null
        val interruptOnCancellation = interruptSamplerOnCancellation()
        try {
            iterationListener = initRun(threadContext)
            // This coroutine handles cancellation and interrupts interruptible sap
            val job = coroutineContext[Job]!!
            while (running && job.isActive) {
                var sam = threadGroupLoopController.next()
                while (running && job.isActive && sam != null) {
                    processSampler(sam, null, threadContext)
                    threadContext.cleanAfterSample()

                    val lastSampleOk = TRUE == threadContext.variables.get(LAST_SAMPLE_OK)
                    // restart of the next loop
                    // - was requested through threadContext
                    // - or the last sample failed AND the onErrorStartNextLoop option is enabled
                    if (threadContext.testLogicalAction != TestLogicalAction.CONTINUE || onErrorStartNextLoop && !lastSampleOk) {
                        if (log.isDebugEnabled && onErrorStartNextLoop &&
                            threadContext.testLogicalAction != TestLogicalAction.CONTINUE
                        ) {
                            log.debug("Start Next Thread Loop option is on, Last sample failed, starting next thread loop")
                        }
                        if (onErrorStartNextLoop && !lastSampleOk) {
                            triggerActionOnParentControllers(sam, threadContext) {
                                continueOnThreadLoop()
                            }
                        } else {
                            when (threadContext.testLogicalAction) {
                                TestLogicalAction.BREAK_CURRENT_LOOP ->
                                    triggerActionOnParentControllers(sam, threadContext) {
                                        breakOnCurrentLoop()
                                    }
                                TestLogicalAction.START_NEXT_ITERATION_OF_THREAD ->
                                    triggerActionOnParentControllers(sam, threadContext) {
                                        continueOnThreadLoop()
                                    }
                                TestLogicalAction.START_NEXT_ITERATION_OF_CURRENT_LOOP ->
                                    triggerActionOnParentControllers(sam, threadContext) {
                                        continueOnCurrentLoop()
                                    }
                                else -> Unit
                            }
                        }
                        threadContext.testLogicalAction = TestLogicalAction.CONTINUE
                        sam = null
                        threadContext.variables.put(LAST_SAMPLE_OK, TRUE)
                    } else {
                        sam = threadGroupLoopController.next()
                    }
                }

                // It would be possible to add finally for Thread Loop here
                if (threadGroupLoopController.isDone) {
                    running = false
                    log.info("Thread is done: {}", threadName)
                }
            }
        } catch (e: JMeterStopTestException) { // NOSONAR
            // Might be found by controller.next()
            log.info("Stopping Test: {}", e)
            shutdownTest()
        } catch (e: JMeterStopTestNowException) { // NOSONAR
            log.info("Stopping Test Now: {}", e)
            stopTestNow()
        } catch (e: JMeterStopThreadException) { // NOSONAR
            log.info("Stop Thread seen for thread {}, reason: {}", threadName, e)
        } catch (e: Exception) {
            log.error("Test failed!", e)
        } catch (e: JMeterError) {
            log.error("Test failed!", e)
        } catch (e: ThreadDeath) {
            throw e // Must not ignore this one
        } finally {
            interruptOnCancellation.cancel()
            currentSamplerForInterruption = null // prevent any further interrupts
            try {
                interruptLock.lock() // make sure current interrupt is finished, prevent another starting yet
                threadContext.clear()
                log.info("Thread finished: {}", threadName)
                threadFinished(iterationListener)
                monitor?.threadFinished(this@JMeterThread) // Tell the monitor we are done
                JMeterContextService.removeContext() // Remove the ThreadLocal entry
            } finally {
                interruptLock.unlock() // Allow any pending interrupt to complete (OK because currentSampler == null)
            }
        }
    }

    /**
     * Trigger break/continue/switch to next thread Loop  depending on consumer implementation
     * @param sampler Sampler Base sampler
     * @param threadContext
     * @param action Consumer that will process the tree of elements up to root node
     */
    private fun triggerActionOnParentControllers(
        sampler: Sampler,
        threadContext: JMeterContext,
        action: FindTestElementsUpToRootTraverser.() -> Unit
    ) {
        val realSampler = findRealSampler(sampler)
            ?: throw IllegalStateException(
                "Got null subSampler calling findRealSampler for: ${sampler.name}, sampler: $sampler"
            )
        // Find parent controllers of current sampler
        val pathToRootTraverser = FindTestElementsUpToRootTraverser(realSampler)
        testTree.traverse(pathToRootTraverser)

        pathToRootTraverser.action()

        // bug 52968
        // When using Start Next Loop option combined to TransactionController.
        // if an error occurs in a Sample (child of TransactionController)
        // then we still need to report the Transaction in error (and create the sample result)
        if (sampler is TransactionSampler) {
            val transactionPack = compiler.configureTransactionSampler(sampler)
            doEndTransactionSampler(sampler, null, transactionPack, threadContext)
        }
    }

    /**
     * Find the Real sampler (Not TransactionSampler) that really generated an error
     * The Sampler provided is not always the "real" one, it can be a TransactionSampler,
     * if there are some other controllers (SimpleController or other implementations) between this TransactionSampler and the real sampler,
     * triggerEndOfLoop will not be called for those controllers leaving them in "ugly" state.
     * the following method will try to find the sampler that really generate an error
     * @return [Sampler]
     */
    private fun findRealSampler(sampler: Sampler): Sampler? {
        var realSampler = sampler
        while (realSampler is TransactionSampler) {
            realSampler = realSampler.subSampler
        }
        return realSampler
    }

    /**
     * Process the current sampler, handling transaction samplers.
     *
     * @param current sampler
     * @param parent sampler
     * @param threadContext
     * @return SampleResult if a transaction was processed
     */
    private suspend fun processSampler(
        current: Sampler?,
        parent: Sampler?,
        threadContext: JMeterContext
    ): SampleResult? {
        var currentSampler = current
        var transactionResult: SampleResult? = null
        // Check if we are running a transaction
        var transactionSampler: TransactionSampler? = null
        // Find the package for the transaction
        var transactionPack: SamplePackage? = null
        try {
            if (currentSampler is TransactionSampler) {
                transactionSampler = currentSampler
                transactionPack = compiler.configureTransactionSampler(transactionSampler)

                // Check if the transaction is done
                if (transactionSampler.isTransactionDone) {
                    transactionResult = doEndTransactionSampler(
                        transactionSampler,
                        parent,
                        transactionPack!!,
                        threadContext
                    )
                    // Transaction is done, we do not have a sampler to sample
                    currentSampler = null
                } else {
                    val prev = currentSampler
                    // It is the sub sampler of the transaction that will be sampled
                    currentSampler = transactionSampler.subSampler
                    if (currentSampler is TransactionSampler) {
                        val res = processSampler(currentSampler, prev, threadContext) // recursive call
                        threadContext.currentSampler = prev
                        currentSampler = null
                        if (res != null) {
                            transactionSampler.addSubSamplerResult(res)
                        }
                    }
                }
            }

            // Check if we have a sampler to sample
            if (currentSampler != null) {
                executeSamplePackage(currentSampler, transactionSampler, transactionPack, threadContext)
            }

            if (scheduler) {
                // checks the scheduler to stop the iteration
                stopSchedulerIfNeeded()
            }
        } catch (e: JMeterStopTestException) { // NOSONAR
            log.info("Stopping Test: {}", e)
            shutdownTest()
        } catch (e: JMeterStopTestNowException) { // NOSONAR
            log.info("Stopping Test with interruption of current samplers: {}", e)
            stopTestNow()
        } catch (e: JMeterStopThreadException) { // NOSONAR
            log.info("Stopping Thread: {}", e)
            stopThread()
        } catch (e: Exception) {
            if (currentSampler != null) {
                log.error("Error while processing sampler: '{}'.", currentSampler.name, e)
            } else {
                log.error("Error while processing sampler.", e)
            }
        }

        if (!running &&
            transactionResult == null &&
            transactionSampler != null &&
            transactionPack != null
        ) {
            transactionResult =
                doEndTransactionSampler(transactionSampler, parent, transactionPack, threadContext)
        }

        return transactionResult
    }

    private fun fillThreadInformation(
        result: SampleResult,
        nbActiveThreadsInThreadGroup: Int,
        nbTotalActiveThreads: Int
    ) {
        result.setGroupThreads(nbActiveThreadsInThreadGroup)
        result.setAllThreads(nbTotalActiveThreads)
        result.setThreadName(threadName)
    }

    /**
     * Execute the sampler with its pre/post processors, timers, assertions
     * Broadcast the result to the sample listeners
     */
    private suspend fun executeSamplePackage(
        current: Sampler,
        transactionSampler: TransactionSampler?,
        transactionPack: SamplePackage?,
        threadContext: JMeterContext
    ) {
        threadContext.currentSampler = current
        // Get the sampler ready to sample
        val pack = compiler.configureSampler(current)
        runPreProcessors(pack.preProcessors)

        // Hack: save the package for any transaction controllers
        threadVars.putObject(PACKAGE_OBJECT, pack)

        delay(pack.timers)
        val result = when {
            running -> doSampling(threadContext, pack.sampler)
            else -> null
        }
        // If we got any results, then perform processing on the result
        if (result != null && !result.isIgnore) {
            val nbActiveThreadsInThreadGroup = threadGroup.numberOfThreads
            val nbTotalActiveThreads = JMeterContextService.getNumberOfThreads()
            fillThreadInformation(result, nbActiveThreadsInThreadGroup, nbTotalActiveThreads)
            val subResults = result.subResults
            if (subResults != null) {
                for (subResult in subResults) {
                    fillThreadInformation(subResult, nbActiveThreadsInThreadGroup, nbTotalActiveThreads)
                }
            }
            threadContext.previousResult = result
            runPostProcessors(pack.postProcessors)
            checkAssertions(pack.assertions, result, threadContext)
            if (!result.isIgnore) {
                // Do not send subsamples to listeners which receive the transaction sample
                val sampleListeners = getSampleListeners(pack, transactionPack, transactionSampler)
                notifyListeners(sampleListeners, result)
            }
            compiler.done(pack)
            // Add the result as subsample of transaction if we are in a transaction
            if (!result.isIgnore) {
                transactionSampler?.addSubSamplerResult(result)
            }

            // Check if thread or test should be stopped
            if (result.isStopThread || !result.isSuccessful && onErrorStopThread) {
                stopThread()
            }
            if (result.isStopTest || !result.isSuccessful && onErrorStopTest) {
                shutdownTest()
            }
            if (result.isStopTestNow || !result.isSuccessful && onErrorStopTestNow) {
                stopTestNow()
            }
            threadContext.testLogicalAction = result.testLogicalAction
        } else {
            compiler.done(pack) // Finish up
        }
    }

    /**
     * Call sample on Sampler handling:
     *
     *  * setting up ThreadContext
     *  * initializing sampler if needed
     *  * positionning currentSamplerForInterruption for potential interruption
     *  * Playing SampleMonitor before and after sampling
     *  * resetting currentSamplerForInterruption
     *
     * @param threadContext [JMeterContext]
     * @param sampler [Sampler]
     * @return [SampleResult]
     */
    private suspend fun doSampling(threadContext: JMeterContext, sampler: Sampler): SampleResult? {
        sampler.threadContext = threadContext
        sampler.threadName = threadName
        TestBeanHelper.prepare(sampler)

        // Perform the actual sample
        currentSamplerForInterruption = sampler
        if (!sampleMonitors.isEmpty()) {
            for (sampleMonitor in sampleMonitors) {
                if (sampleMonitor is TestElement) {
                    TestBeanHelper.prepare(sampleMonitor)
                }
                sampleMonitor.sampleStarting(sampler)
            }
        }
        try {
            if (sampler is SuspendingSampler) {
                return sampler.suspendingSample()
            }
            return sampler.sample(null)
        } finally {
            if (!sampleMonitors.isEmpty()) {
                for (sampleMonitor in sampleMonitors) {
                    sampleMonitor.sampleEnded(sampler)
                }
            }
            currentSamplerForInterruption = null
        }
    }

    private fun doEndTransactionSampler(
        transactionSampler: TransactionSampler,
        parent: Sampler?,
        transactionPack: SamplePackage,
        threadContext: JMeterContext
    ): SampleResult {
        // Get the transaction sample result
        val transactionResult = transactionSampler.transactionResult.also {
            fillThreadInformation(it, threadGroup.numberOfThreads, JMeterContextService.getNumberOfThreads())
        }

        // Check assertions for the transaction sample
        checkAssertions(transactionPack.assertions, transactionResult, threadContext)
        // Notify listeners with the transaction sample result
        if (parent !is TransactionSampler) {
            notifyListeners(transactionPack.sampleListeners, transactionResult)
        }
        compiler.done(transactionPack)
        return transactionResult
    }

    /**
     * Get the SampleListeners for the sampler. Listeners who receive transaction sample
     * will not be in this list.
     *
     * @param samplePack
     * @param transactionPack
     * @param transactionSampler
     * @return the listeners who should receive the sample result
     */
    private fun getSampleListeners(
        samplePack: SamplePackage,
        transactionPack: SamplePackage?,
        transactionSampler: TransactionSampler?
    ): List<SampleListener> {
        val sampleListeners = samplePack.sampleListeners
        // Do not send subsamples to listeners which receive the transaction sample
        return when {
            transactionSampler != null -> sampleListeners.filterNot {
                // Check if this instance is present in transaction listener list
                transactionPack!!.sampleListeners.contains(it)
            }
            else -> sampleListeners
        }
    }

    /**
     * @param threadContext
     * @return the iteration listener
     */
    internal suspend fun initRun(threadContext: JMeterContext): LoopIterationListener {
        threadVars.putObject(JMeterVariables.VAR_IS_SAME_USER_KEY, isSameUserOnNextIteration)
        threadContext.variables = threadVars
        threadContext.threadNum = threadNum
        threadContext.variables.put(LAST_SAMPLE_OK, TRUE)
        threadContext.thread = this
        threadContext.threadGroup = threadGroup
        threadContext.engine = engine
        testTree.traverse(compiler)
        if (scheduler) {
            // set the scheduler to start
            startScheduler()
        }

        rampUpDelay()
        log.info("Thread started: {}", threadName)
        /*
         * Setting SamplingStarted before the controllers are initialised allows
         * them to access the running values of functions and variables (however
         * it does not seem to help with the listeners)
         */
        threadContext.isSamplingStarted = true

        threadGroupLoopController.initialize()
        val iterationListener = LoopIterationListener { notifyTestListeners() }
        threadGroupLoopController.addIterationListener(iterationListener)

        threadStarted()
        return iterationListener
    }

    private fun threadStarted() {
        JMeterContextService.incrNumberOfThreads()
        threadGroup.incrNumberOfThreads()
        val gp = GuiPackage.getInstance()
        gp?.mainFrame?.updateCounts()
        val startup = ThreadListenerTraverser(true)
        testTree.traverse(startup) // call ThreadListener.threadStarted()
    }

    private fun threadFinished(iterationListener: LoopIterationListener?) {
        val shut = ThreadListenerTraverser(false)
        testTree.traverse(shut) // call ThreadListener.threadFinished()
        JMeterContextService.decrNumberOfThreads()
        threadGroup.decrNumberOfThreads()
        val gp = GuiPackage.getInstance()
        gp?.mainFrame?.updateCounts()
        if (iterationListener != null) { // probably not possible, but check anyway
            threadGroupLoopController.removeIterationListener(iterationListener)
        }
    }

    // N.B. This is only called at the start and end of a thread, so there is not
    // necessary to cache the search results, thus saving memory
    internal class ThreadListenerTraverser(private val isStart: Boolean) : HashTreeTraverser {
        override fun addNode(node: Any, subTree: HashTree) {
            if (node is ThreadListener) {
                if (isStart) {
                    try {
                        node.threadStarted()
                    } catch (e: Exception) {
                        log.error("Error calling threadStarted", e)
                    }
                } else {
                    try {
                        node.threadFinished()
                    } catch (e: Exception) {
                        log.error("Error calling threadFinished", e)
                    }
                }
            }
        }

        override fun subtractNode() {
            // NOOP
        }

        override fun processPath() {
            // NOOP
        }
    }

    /**
     * Set running flag to false which will interrupt JMeterThread on next flag test.
     * This is a clean shutdown.
     */
    fun stop() { // Called by StandardJMeterEngine, TestAction and AccessLogSampler
        running = false
        log.info("Stopping: {}", threadName)
    }

    /** {@inheritDoc}  */
    override fun interrupt(): Boolean {
        try {
            interruptLock.lock()
            val samp = currentSamplerForInterruption // fetch once; must be done under lock
            if (samp is Interruptible) { // (also protects against null)
                log.warn("Interrupting: {} sampler: {}", threadName, samp.name)
                try {
                    val found = samp.interrupt()
                    log.warn("No operation pending")
                    return found
                } catch (e: Exception) { // NOSONAR
                    log.warn("Caught Exception interrupting sampler: {}", e)
                }
            } else if (samp != null) {
                log.warn("Sampler is not Interruptible: {}", samp.name)
            }
        } finally {
            interruptLock.unlock()
        }
        return false
    }

    /**
     * Clean shutdown of test, which means wait for end of current running samplers
     */
    private fun shutdownTest() {
        running = false
        log.info("Shutdown Test detected by thread: {}", threadName)
        engine?.askThreadsToStop()
    }

    /**
     * Stop test immediately by interrupting running samplers
     */
    private fun stopTestNow() {
        running = false
        log.info("Stop Test Now detected by thread: {}", threadName)
        engine?.stopTest()
    }

    /**
     * Clean Exit of current thread
     */
    private fun stopThread() {
        running = false
        log.info("Stop Thread detected by thread: {}", threadName)
    }

    private fun checkAssertions(
        assertions: List<Assertion>,
        parent: SampleResult,
        threadContext: JMeterContext
    ) {
        for (assertion in assertions) {
            TestBeanHelper.prepare(assertion as TestElement)
            if (assertion is AbstractScopedAssertion) {
                val scope = assertion.fetchScope()
                if (assertion.isScopeParent(scope) ||
                    assertion.isScopeAll(scope) ||
                    assertion.isScopeVariable(scope)
                ) {
                    processAssertion(parent, assertion)
                }
                if (assertion.isScopeChildren(scope) || assertion.isScopeAll(scope)) {
                    recurseAssertionChecks(parent, assertion, 3)
                }
            } else {
                processAssertion(parent, assertion)
            }
        }
        threadContext.variables.put(
            LAST_SAMPLE_OK,
            parent.isSuccessful.toString()
        )
    }

    private fun recurseAssertionChecks(
        parent: SampleResult,
        assertion: Assertion,
        level: Int
    ) {
        if (level < 0) {
            return
        }
        val children = parent.getSubResults()
        var childError = false
        for (childSampleResult in children) {
            processAssertion(childSampleResult, assertion)
            recurseAssertionChecks(childSampleResult, assertion, level - 1)
            if (!childSampleResult.isSuccessful()) {
                childError = true
            }
        }
        // If parent is OK, but child failed, add a message and flag the parent as failed
        if (childError && parent.isSuccessful()) {
            val assertionResult =
                AssertionResult((assertion as AbstractTestElement).getName())
            assertionResult.setResultForFailure("One or more sub-samples failed")
            parent.addAssertionResult(assertionResult)
            parent.setSuccessful(false)
        }
    }

    private fun processAssertion(result: SampleResult, assertion: Assertion) {
        val assertionResult =
            try {
                assertion.getResult(result)
            } catch (e: AssertionError) {
                log.debug("Error processing Assertion.", e)
                AssertionResult("Assertion failed! See log file (debug level, only).").apply {
                    isFailure = true
                    failureMessage = e.toString()
                }
            } catch (e: JMeterError) {
                log.error("Error processing Assertion.", e)
                AssertionResult("Assertion failed! See log file.").apply {
                    isError = true
                    failureMessage = e.toString()
                }
            } catch (e: Exception) {
                log.error("Exception processing Assertion.", e)
                AssertionResult("Assertion failed! See log file.").apply {
                    isError = true
                    failureMessage = e.toString()
                }
            }

        result.isSuccessful =
            result.isSuccessful && !(assertionResult.isError || assertionResult.isFailure)
        result.addAssertionResult(assertionResult)
    }

    private fun runPostProcessors(extractors: List<PostProcessor>) {
        for (ex in extractors) {
            TestBeanHelper.prepare(ex as TestElement)
            ex.process()
        }
    }

    private fun runPreProcessors(preProcessors: List<PreProcessor>) {
        for (ex in preProcessors) {
            if (log.isDebugEnabled) {
                log.debug("Running preprocessor: {}", (ex as AbstractTestElement).name)
            }
            TestBeanHelper.prepare(ex as TestElement)
            ex.process()
        }
    }

    /**
     * Run all configured timers and sleep the total amount of time.
     *
     * If the amount of time would amount to an ending after endTime, then
     * end the current thread by setting `running` to `false` and
     * return immediately.
     *
     * @param timers to be used for calculating the delay
     */
    private suspend fun delay(timers: List<Timer>) {
        var totalDelay: Long = 0
        for (timer in timers) {
            TestBeanHelper.prepare(timer as TestElement)
            var delay = timer.delay()
            if (APPLY_TIMER_FACTOR && timer.isModifiable) {
                if (log.isDebugEnabled) {
                    log.debug(
                        "Applying TIMER_FACTOR:{} on timer:{} for thread:{}", TIMER_FACTOR,
                        (timer as TestElement).name, threadName
                    )
                }
                delay = (delay * TIMER_FACTOR).roundToLong()
            }
            totalDelay += delay
        }
        if (totalDelay > 0) {
            try {
                if (scheduler) {
                    // We reduce pause to ensure end of test is not delayed by a sleep ending after test scheduled end
                    // See Bug 60049
                    totalDelay = TIMER_SERVICE.adjustDelay(totalDelay, endTime, false)
                    if (totalDelay < 0) {
                        log.debug("The delay would be longer than the scheduled period, so stop thread now.")
                        running = false
                        return
                    }
                }
                kotlinx.coroutines.delay(totalDelay)
            } catch (e: InterruptedException) {
                log.warn("The delay timer was interrupted - probably did not wait as long as intended.")
                Thread.currentThread().interrupt()
            }
        }
    }

    private fun notifyTestListeners() {
        threadVars.incIteration()
        for (listener in testIterationStartListeners) {
            listener.testIterationStart(
                LoopIterationEvent(
                    threadGroupLoopController,
                    threadVars.iteration
                )
            )
            if (listener is TestElement) {
                listener.recoverRunningVersion()
            }
        }
    }

    private fun notifyListeners(listeners: List<SampleListener>, result: SampleResult) {
        val notifier = notifier ?: return
        val event = SampleEvent(result, threadGroup.name, threadVars)
        notifier.notifyListeners(event, listeners)
    }

    /**
     * Set rampup delay for JMeterThread Thread
     *
     * @param delay Rampup delay for JMeterThread
     */
    fun setInitialDelay(delay: Int) {
        initialDelay = delay
    }

    /**
     * Initial delay if ramp-up period is active for this threadGroup.
     */
    private suspend fun rampUpDelay() {
        delayBy(initialDelay.toLong(), "RampUp")
    }

    /**
     * Wait for delay with RAMPUP_GRANULARITY
     *
     * @param delay delay in ms
     * @param type Delay type
     */
    private suspend fun delayBy(delay: Long, type: String) {
        kotlinx.coroutines.delay(delay)
    }

    /**
     * Save the engine instance for access to the stop methods
     *
     * @param engine the engine which is used
     */
    fun setEngine(engine: StandardJMeterEngine) {
        this.engine = engine
    }

    /**
     * Should Test stop on sampler error?
     *
     * @param b true or false
     */
    fun setOnErrorStopTest(b: Boolean) {
        onErrorStopTest = b
    }

    /**
     * Should Test stop abruptly on sampler error?
     *
     * @param b true or false
     */
    fun setOnErrorStopTestNow(b: Boolean) {
        onErrorStopTestNow = b
    }

    /**
     * Should Thread stop on Sampler error?
     *
     * @param b true or false
     */
    fun setOnErrorStopThread(b: Boolean) {
        onErrorStopThread = b
    }

    /**
     * Should Thread start next loop on Sampler error?
     *
     * @param b true or false
     */
    fun setOnErrorStartNextLoop(b: Boolean) {
        onErrorStartNextLoop = b
    }

    fun setThreadGroup(group: AbstractThreadGroup) {
        this.threadGroup = group
    }

    /**
     * @return [ListedHashTree]
     */
    fun getTestTree(): ListedHashTree {
        return testTree as ListedHashTree
    }

    companion object {
        private val log = LoggerFactory.getLogger(JMeterThread::class.java)

        const val PACKAGE_OBJECT = "JMeterThread.pack" // $NON-NLS-1$

        const val LAST_SAMPLE_OK = "JMeterThread.last_sample_ok" // $NON-NLS-1$

        private val TRUE = java.lang.Boolean.toString(true) // i.e. "true"

        private val TIMER_FACTOR = JMeterUtils.getPropDefault("timer.factor", 1.0f)

        private val TIMER_SERVICE = TimerService.getInstance()

        private const val ONE_AS_FLOAT = 1.0f

        private val APPLY_TIMER_FACTOR = TIMER_FACTOR.compareTo(ONE_AS_FLOAT) != 0

        /**
         * Executes a continue of current loop, equivalent of "continue" in algorithm.
         * As a consequence it ends the first loop it finds on the path to root
         * @param pathToRootTraverser [FindTestElementsUpToRootTraverser]
         */
        private fun FindTestElementsUpToRootTraverser.continueOnCurrentLoop() {
            val controllersToReinit = controllersToRoot
            for (parentController in controllersToReinit) {
                if (parentController is AbstractThreadGroup) {
                    parentController.startNextLoop()
                } else if (parentController is IteratingController) {
                    parentController.startNextLoop()
                    break
                } else {
                    parentController.triggerEndOfLoop()
                }
            }
        }

        /**
         * Executes a break of current loop, equivalent of "break" in algorithm.
         * As a consequence it ends the first loop it finds on the path to root
         * @param pathToRootTraverser [FindTestElementsUpToRootTraverser]
         */
        private fun FindTestElementsUpToRootTraverser.breakOnCurrentLoop() {
            val controllersToReinit = controllersToRoot
            for (parentController in controllersToReinit) {
                if (parentController is AbstractThreadGroup) {
                    parentController.breakThreadLoop()
                } else if (parentController is IteratingController) {
                    parentController.breakLoop()
                    break
                } else {
                    parentController.triggerEndOfLoop()
                }
            }
        }

        /**
         * Executes a restart of Thread loop, equivalent of "continue" in algorithm but on Thread Loop.
         * As a consequence it ends all loop on the path to root
         * @param pathToRootTraverser [FindTestElementsUpToRootTraverser]
         */
        private fun FindTestElementsUpToRootTraverser.continueOnThreadLoop() {
            val controllersToReinit = controllersToRoot
            for (parentController in controllersToReinit) {
                if (parentController is AbstractThreadGroup) {
                    parentController.startNextLoop()
                } else {
                    parentController.triggerEndOfLoop()
                }
            }
        }
    }
}
