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

package org.apache.jmeter.control;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.sampler.DebugSampler;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ListenerNotifier;
import org.apache.jmeter.threads.TestCompiler;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class TestTransactionController extends JMeterTestCase {

    /**
     * @see "http://bz.apache.org/bugzilla/show_bug.cgi?id=57958"
     * @throws Exception when something breaks
     */
    @Test
    public void testIssue57958() throws Exception {
        JMeterContextService.getContext().setVariables(new JMeterVariables());


        TestSampleListener listener = new TestSampleListener();

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

        ListedHashTree hashTree = new ListedHashTree();
        hashTree.add(loop);
        hashTree.add(loop, transactionController);
        hashTree.add(transactionController, debugSampler);
        hashTree.add(transactionController, listener);
        hashTree.add(debugSampler, assertion);

        TestCompiler compiler = new TestCompiler(hashTree);
        hashTree.traverse(compiler);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);

        ListenerNotifier notifier = new ListenerNotifier();

        JMeterThread thread = new JMeterThread(hashTree, threadGroup, notifier);
        thread.setThreadGroup(threadGroup);
        thread.setOnErrorStopThread(true);
        thread.run();

        assertEquals("Must one transaction samples with parent debug sample", 1, listener.events.size());
        assertEquals("Number of samples in transaction : 1, number of failing samples : 1", listener.events.get(0).getResult().getResponseMessage());
    }



    public class TestSampleListener extends ResultCollector implements SampleListener {
        public List<SampleEvent> events = new ArrayList<>();

        @Override
        public void sampleOccurred(SampleEvent e) {
            events.add(e);
        }

        @Override
        public void sampleStarted(SampleEvent e) {
            events.add(e);
        }

        @Override
        public void sampleStopped(SampleEvent e) {
            events.add(e);
        }
    }
}
