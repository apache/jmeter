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

package org.apache.jmeter.threads;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.timers.ConstantTimer;
import org.apache.jorphan.collections.HashTree;
import org.junit.Test;

/**
 * Tests for {@link JMeterThread}
 */
public class TestJMeterThread {

    private static final class DummySampler extends AbstractSampler {
        private static final long serialVersionUID = 1L;
        private boolean called = false;

        public boolean isCalled() {
            return called;
        }

        @Override
        public SampleResult sample(Entry e) {
            called = true;
            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + (called ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DummySampler other = (DummySampler) obj;
            return called == other.called;
        }

    }

    private static class ThrowingThreadListener implements ThreadListener {

        private boolean throwError;

        public ThrowingThreadListener(boolean throwError) {
            this.throwError = throwError;
        }

        @Override
        public void threadStarted() {
            if(throwError) {
                throw new NoClassDefFoundError("Throw for Bug TestJMeterThread");
            } else {
                throw new RuntimeException("Throw for Bug TestJMeterThread");
            }
        }

        @Override
        public void threadFinished() {
            if(throwError) {
                throw new NoClassDefFoundError("Throw for Bug TestJMeterThread");
            } else {
                throw new RuntimeException("Throw for Bug TestJMeterThread");
            }
        }
    }

    @Test(expected=NoClassDefFoundError.class)
    public void testBug61661OnError(){
        HashTree hashTree =new HashTree();
        hashTree.add("Test", new ThrowingThreadListener(true));
        JMeterThread.ThreadListenerTraverser traverser =
                new JMeterThread.ThreadListenerTraverser(true);
        hashTree.traverse(traverser);
    }

    @Test
    public void testBug61661OnException(){
        HashTree hashTree =new HashTree();
        hashTree.add("Test", new ThrowingThreadListener(false));
        JMeterThread.ThreadListenerTraverser traverser =
                new JMeterThread.ThreadListenerTraverser(true);
        hashTree.traverse(traverser);
    }

    @Test
    public void testBug63490EndTestWhenDelayIsTooLongForScheduler() {

        JMeterContextService.getContext().setVariables(new JMeterVariables());

        HashTree testTree = new HashTree();
        LoopController samplerController = createLoopController();
        testTree.add(samplerController);
        testTree.add(samplerController, createConstantTimer("3000"));
        DummySampler dummySampler = createSampler();
        testTree.add(samplerController, dummySampler);

        TestCompiler compiler = new TestCompiler(testTree);
        testTree.traverse(compiler);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);
        long maxDuration = 2000L;
        threadGroup.setDuration(maxDuration);

        JMeterThread jMeterThread = new JMeterThread(testTree, threadGroup, null);
        jMeterThread.setScheduled(true);
        jMeterThread.setEndTime(System.currentTimeMillis() + maxDuration);
        jMeterThread.setThreadGroup(threadGroup);
        long startTime = new Date().getTime();
        jMeterThread.run();
        long duration = new Date().getTime() - startTime;

        assertFalse("Sampler should not be called", dummySampler.isCalled());

        // the duration of this test plan should currently be around zero seconds,
        // but it is allowed to take up to maxDuration amount of time
        assertTrue("Test plan should not run for longer than duration", duration <= maxDuration);
    }

    private LoopController createLoopController() {
        LoopController result = new LoopController();
        result.setLoops(LoopController.INFINITE_LOOP_COUNT);
        result.setEnabled(true);
        return result;
    }

    private DummySampler createSampler() {
        DummySampler result = new DummySampler();
        result.setName("Call me");
        return result;
    }

    private ConstantTimer createConstantTimer(String delay) {
        ConstantTimer constantTimer = new ConstantTimer();
        constantTimer.setEnabled(true);
        constantTimer.setDelay(delay);
        constantTimer.setName("Long delay");
        return constantTimer;
    }
}
