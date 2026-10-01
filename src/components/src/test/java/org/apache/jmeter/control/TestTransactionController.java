/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.jupiter.api.Test;


public class TestTransactionController extends JMeterTestCase {

    /**
     * A snapshot of a sample result captured at notification time.
     * Because {@link SampleResult} is mutated after listeners are notified
     * (e.g. {@code setTransactionDone()} is called later), we must record
     * the values we care about inside {@code sampleOccurred} rather than
     * reading them from the result object after the test has finished.
     */
    private static class SampleSnapshot {
        final long time;
        final long idleTime;
        final String responseMessage;
        final boolean isTransactionSample;

        SampleSnapshot(SampleEvent e) {
            SampleResult r = e.getResult();
            this.time = r.getTime();
            this.idleTime = r.getIdleTime();
            this.responseMessage = r.getResponseMessage();
            this.isTransactionSample = e.isTransactionSampleEvent();
        }
    }

    /**
     * A {@link SampleListener} that records a {@link SampleSnapshot} for every
     * {@code sampleOccurred} call.  Values are captured immediately inside the
     * callback so that later mutations of the {@link SampleResult} do not affect
     * the recorded data.
     */
    private static class SnapshotListener extends AbstractTestElement implements SampleListener {
        private static final long serialVersionUID = 1L;
        private final List<SampleSnapshot> snapshots = new ArrayList<>();

        @Override
        public void sampleOccurred(SampleEvent e) {
            snapshots.add(new SampleSnapshot(e));
        }

        @Override
        public void sampleStarted(SampleEvent e) {
        }

        @Override
        public void sampleStopped(SampleEvent e) {
        }

        List<SampleSnapshot> getSnapshots() {
            return snapshots;
        }
    }

    /**
     * A simple sampler that returns a successful result with a fixed elapsed time.
     */
    private static class FixedElapsedTimeSampler extends AbstractSampler {
        private static final long serialVersionUID = 1L;
        private final long elapsedTimeMs;

        FixedElapsedTimeSampler(long elapsedTimeMs) {
            this.elapsedTimeMs = elapsedTimeMs;
        }

        @Override
        public SampleResult sample(Entry e) {
            SampleResult result = new SampleResult();
            result.setSampleLabel(getName());
            result.sampleStart();
            result.setSuccessful(true);
            result.setResponseCodeOK();
            long start = result.getStartTime();
            result.setEndTime(start + elapsedTimeMs);
            return result;
        }
    }

    /**
     * A timer that returns a fixed delay without actually sleeping.
     */
    private static class FixedDelayTimer extends AbstractTestElement implements Timer {
        private static final long serialVersionUID = 1L;
        private final long delayMs;

        FixedDelayTimer(long delayMs) {
            this.delayMs = delayMs;
        }

        @Override
        public long delay() {
            return delayMs;
        }
    }

    /**
     * Test for GitHub issue #6496: when a thread is stopped mid-transaction by the
     * scheduler (parent mode, {@code includeTimers=false}), the transaction elapsed
     * time reported to listeners must not include the timer delay that was in
     * progress when the thread was stopped.
     *
     * <p>Setup: a Transaction Controller in parent mode with two child samplers.
     * A 500 ms timer sits before the first sampler and a 5000 ms timer sits before
     * the second sampler.  The scheduler end time is set 1500 ms ahead, so the
     * thread is stopped while sleeping through the 5000 ms timer.
     *
     * <p>On the base commit (without the fix) the snapshot captured inside
     * {@code sampleOccurred} shows {@code t≈520, it=0, rm=""} because
     * {@code setTransactionDone()} is called <em>after</em> the event is fired and
     * the idle-time correction is never applied to the already-reported result.
     * With the fix the snapshot shows {@code t≈10, it≈508} (child elapsed time only,
     * timer delay moved to idle time).
     */
    @Test
    public void testIssue6496ParentMode() throws Exception {
        JMeterContextService.getContext().setVariables(new JMeterVariables());

        SnapshotListener listener = new SnapshotListener();

        TransactionController transactionController = new TransactionController();
        transactionController.setGenerateParentSample(true);
        transactionController.setIncludeTimers(false);

        // Child sampler with a very short simulated elapsed time
        long childElapsedMs = 10L;
        FixedElapsedTimeSampler firstSampler = new FixedElapsedTimeSampler(childElapsedMs);
        firstSampler.setName("First Sampler");

        // Short timer before the first sampler (will complete before scheduler fires)
        long firstTimerDelayMs = 500L;
        FixedDelayTimer firstTimer = new FixedDelayTimer(firstTimerDelayMs);
        firstTimer.setName("First Timer");
        firstTimer.setEnabled(true);

        FixedElapsedTimeSampler secondSampler = new FixedElapsedTimeSampler(childElapsedMs);
        secondSampler.setName("Second Sampler");

        // Long timer before the second sampler; the scheduler will fire while sleeping here
        long secondTimerDelayMs = 5000L;
        FixedDelayTimer secondTimer = new FixedDelayTimer(secondTimerDelayMs);
        secondTimer.setName("Second Timer");
        secondTimer.setEnabled(true);

        LoopController loop = new LoopController();
        loop.setLoops(LoopController.INFINITE_LOOP_COUNT);
        loop.setContinueForever(true);
        loop.setEnabled(true);

        // Build the tree correctly using the subtrees returned by add()
        ListedHashTree tree = new ListedHashTree();
        ListedHashTree tcTree = (ListedHashTree) tree.add(loop).add(transactionController);
        tcTree.add(listener);
        ListedHashTree firstSamplerTree = (ListedHashTree) tcTree.add(firstSampler);
        firstSamplerTree.add(firstTimer);
        ListedHashTree secondSamplerTree = (ListedHashTree) tcTree.add(secondSampler);
        secondSamplerTree.add(secondTimer);

        TestCompiler compiler = new TestCompiler(tree);
        tree.traverse(compiler);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);

        ListenerNotifier notifier = new ListenerNotifier();

        // Scheduler end time: 1500 ms from now.
        // The first timer (500 ms) + first sampler (~10 ms) will complete (~510 ms total).
        // The second timer (5000 ms) will be cut short by the scheduler at ~1500 ms.
        long maxDuration = 1500L;
        JMeterThread thread = new JMeterThread(tree, threadGroup, notifier);
        thread.setScheduled(true);
        thread.setEndTime(System.currentTimeMillis() + maxDuration);
        thread.setThreadGroup(threadGroup);
        thread.run();

        assertFalse(listener.getSnapshots().isEmpty(),
                "At least one transaction sample should have been collected");

        // The last snapshot is the transaction interrupted during ramp-down.
        // Its elapsed time must equal the child sample time, not be inflated by
        // the timer delay that was in progress when the thread was stopped.
        SampleSnapshot lastSnapshot = listener.getSnapshots().get(listener.getSnapshots().size() - 1);

        // The response message must be set (isFromTransactionController() must return true)
        assertNotNull(lastSnapshot.responseMessage,
                "Response message must not be null for an interrupted transaction");
        assertTrue(lastSnapshot.responseMessage.startsWith(
                        TransactionController.NUMBER_OF_SAMPLES_IN_TRANSACTION_PREFIX),
                "Response message should start with the transaction prefix; got: " + lastSnapshot.responseMessage);

        // The elapsed time must be close to the child sample time, not inflated by the
        // 5000 ms timer that was in progress when the thread was stopped.
        assertTrue(lastSnapshot.time < secondTimerDelayMs,
                "Transaction elapsed time (" + lastSnapshot.time + " ms) must not include "
                        + "the timer delay (" + secondTimerDelayMs + " ms) when thread is stopped mid-transaction");

        // The idle time must account for the timer delays that were excluded
        assertTrue(lastSnapshot.idleTime > 0,
                "Idle time (" + lastSnapshot.idleTime + " ms) must be positive when timers are excluded");
    }

    /**
     * Test for GitHub issue #6496: when a thread is stopped mid-transaction by the
     * scheduler (non-parent / additional-sample mode, {@code includeTimers=false}),
     * the transaction elapsed time reported to listeners must not include the timer
     * delay that was in progress when the thread was stopped.
     *
     * <p>This exercises the {@code triggerEndOfLoop()} path in
     * {@link TransactionController} (non-parent mode).  A Flow Control Action
     * "Start next thread loop" is not needed here because the scheduler stop
     * already causes {@code triggerEndOfLoop()} to be called on the
     * TransactionController.
     */
    @Test
    public void testIssue6496NonParentMode() throws Exception {
        JMeterContextService.getContext().setVariables(new JMeterVariables());

        SnapshotListener listener = new SnapshotListener();

        TransactionController transactionController = new TransactionController();
        transactionController.setGenerateParentSample(false);
        transactionController.setIncludeTimers(false);

        long childElapsedMs = 10L;
        FixedElapsedTimeSampler firstSampler = new FixedElapsedTimeSampler(childElapsedMs);
        firstSampler.setName("First Sampler");

        long firstTimerDelayMs = 500L;
        FixedDelayTimer firstTimer = new FixedDelayTimer(firstTimerDelayMs);
        firstTimer.setName("First Timer");
        firstTimer.setEnabled(true);

        FixedElapsedTimeSampler secondSampler = new FixedElapsedTimeSampler(childElapsedMs);
        secondSampler.setName("Second Sampler");

        long secondTimerDelayMs = 5000L;
        FixedDelayTimer secondTimer = new FixedDelayTimer(secondTimerDelayMs);
        secondTimer.setName("Second Timer");
        secondTimer.setEnabled(true);

        LoopController loop = new LoopController();
        loop.setLoops(LoopController.INFINITE_LOOP_COUNT);
        loop.setContinueForever(true);
        loop.setEnabled(true);

        // Build the tree correctly using the subtrees returned by add()
        ListedHashTree tree = new ListedHashTree();
        ListedHashTree tcTree = (ListedHashTree) tree.add(loop).add(transactionController);
        // In non-parent mode the listener must be a child of the TransactionController
        tcTree.add(listener);
        ListedHashTree firstSamplerTree = (ListedHashTree) tcTree.add(firstSampler);
        firstSamplerTree.add(firstTimer);
        ListedHashTree secondSamplerTree = (ListedHashTree) tcTree.add(secondSampler);
        secondSamplerTree.add(secondTimer);

        TestCompiler compiler = new TestCompiler(tree);
        tree.traverse(compiler);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);

        ListenerNotifier notifier = new ListenerNotifier();

        long maxDuration = 1500L;
        JMeterThread thread = new JMeterThread(tree, threadGroup, notifier);
        thread.setScheduled(true);
        thread.setEndTime(System.currentTimeMillis() + maxDuration);
        thread.setThreadGroup(threadGroup);
        thread.run();

        // Filter for transaction snapshots (non-parent mode fires both child and transaction events)
        List<SampleSnapshot> txSnapshots = new ArrayList<>();
        for (SampleSnapshot s : listener.getSnapshots()) {
            if (s.responseMessage != null
                    && s.responseMessage.startsWith(TransactionController.NUMBER_OF_SAMPLES_IN_TRANSACTION_PREFIX)) {
                txSnapshots.add(s);
            }
        }

        assertFalse(txSnapshots.isEmpty(),
                "At least one transaction sample should have been collected");

        SampleSnapshot lastSnapshot = txSnapshots.get(txSnapshots.size() - 1);

        assertTrue(lastSnapshot.time < secondTimerDelayMs,
                "Transaction elapsed time (" + lastSnapshot.time + " ms) must not include "
                        + "the timer delay (" + secondTimerDelayMs + " ms) when thread is stopped mid-transaction");

        assertTrue(lastSnapshot.idleTime > 0,
                "Idle time (" + lastSnapshot.idleTime + " ms) must be positive when timers are excluded");
    }

    @Test
    public void testIssue57958() throws Exception {
        JMeterContextService.getContext().setVariables(new JMeterVariables());

        SnapshotListener listener = new SnapshotListener();

        TransactionController transactionController = new TransactionController();
        transactionController.setGenerateParentSample(true);

        ResponseAssertion assertion = new ResponseAssertion();
        assertion.setTestFieldResponseCode();
        assertion.setToEqualsType();
        assertion.addTestString("201");

        DebugSampler debugSampler = new DebugSampler();
        debugSampler.addTestElement(assertion);

        LoopController loop = new LoopController();
        loop.setLoops(1);
        loop.setContinueForever(false);

        ListedHashTree tree = new ListedHashTree();
        ListedHashTree tcTree = (ListedHashTree) tree.add(loop).add(transactionController);
        tcTree.add(listener);
        ListedHashTree samplerTree = (ListedHashTree) tcTree.add(debugSampler);
        samplerTree.add(assertion);

        TestCompiler compiler = new TestCompiler(tree);
        tree.traverse(compiler);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);

        ListenerNotifier notifier = new ListenerNotifier();

        JMeterThread thread = new JMeterThread(tree, threadGroup, notifier);
        thread.setThreadGroup(threadGroup);
        thread.setOnErrorStopThread(true);
        thread.run();

        List<SampleSnapshot> txSnapshots = new ArrayList<>();
        for (SampleSnapshot s : listener.getSnapshots()) {
            if (s.responseMessage != null
                    && s.responseMessage.startsWith(TransactionController.NUMBER_OF_SAMPLES_IN_TRANSACTION_PREFIX)) {
                txSnapshots.add(s);
            }
        }

        assertEquals(1, txSnapshots.size(),
                "Must have one transaction sample with parent debug sample");
        assertEquals("Number of samples in transaction : 1, number of failing samples : 1",
                txSnapshots.get(0).responseMessage);
    }
}
