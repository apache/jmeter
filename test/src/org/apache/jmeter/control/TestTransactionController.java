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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.ListedHashTree;
import org.junit.Test;

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

    /**
     * @see "http://bz.apache.org/bugzilla/show_bug.cgi?id=61466"
     */
    @Test
    public void testIssue61466_dont_global_use() throws Exception {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
        JMeterUtils.getJMeterProperties();


        // transactioncontroller.use_comment_on_all value defined as "false" per default
        JMeterUtils.setProperty("transactioncontroller.use_comments_on_all", "false");
        testLog.debug("\"transactioncontroller.use_comments_on_all\" set to \"false\"");
        assertEquals("\"transactioncontroller.use_comments_on_all\" set to \"true\"", "false", 
                JMeterUtils.getProperty("transactioncontroller.use_comments_on_all"));

        TestSampleListener listener1 = new TestSampleListener();
        TestSampleListener listener2 = new TestSampleListener();
        TestSampleListener listener3 = new TestSampleListener();

        TransactionController transactionController1 = new TransactionController();
        transactionController1.setComment("Test with default use of comments");
        transactionController1.setGenerateParentSample(true);

        TransactionController transactionController2 = new TransactionController();
        transactionController2.setComment("Test with forced use of comments");
        transactionController2.setGenerateParentSample(true);
        transactionController2.setUseComment(true);

        TransactionController transactionController3 = new TransactionController();
        transactionController3.setComment("Test with discarded use of comments");
        transactionController3.setGenerateParentSample(true);
        transactionController3.setUseComment(false);

        DebugSampler debugSampler1 = new DebugSampler();
        DebugSampler debugSampler2 = new DebugSampler();
        DebugSampler debugSampler3 = new DebugSampler();

        LoopController loop = new LoopController();
        loop.setLoops(1);
        loop.setContinueForever(false);

        ListedHashTree hashTree = new ListedHashTree();
        hashTree.add(loop);
        hashTree.add(loop, transactionController1);
        hashTree.add(transactionController1, debugSampler1);
        hashTree.add(transactionController1, listener1);
        hashTree.add(loop, transactionController2);
        hashTree.add(transactionController2);
        hashTree.add(transactionController2, debugSampler2);
        hashTree.add(transactionController2, listener2);
        hashTree.add(loop, transactionController3);
        hashTree.add(transactionController3);
        hashTree.add(transactionController3, debugSampler3);
        hashTree.add(transactionController3, listener3);
        
        TestCompiler compiler = new TestCompiler(hashTree);
        hashTree.traverse(compiler);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);

        ListenerNotifier notifier = new ListenerNotifier();

        JMeterThread thread = new JMeterThread(hashTree, threadGroup, notifier);
        thread.setThreadGroup(threadGroup);
        thread.setOnErrorStopThread(true);
        thread.run();

        assertEquals("First TC must discard 'Comments' value from its SampleResult", "", listener1.events.get(0).getResult().getSampleComments());
        assertEquals("Second TC must force 'Comments' value from its SampleResult", "Test with forced use of comments", 
                listener2.events.get(0).getResult().getSampleComments());
        assertEquals("Third TC must discard 'Comments' value from its SampleResult", "", listener3.events.get(0).getResult().getSampleComments());
    }
    
    @Test
    public void testIssue61466_do_global_use() throws Exception {
        JMeterContextService.getContext().setVariables(new JMeterVariables());
        JMeterUtils.getJMeterProperties();

        // transactioncontroller.use_comment_on_all value defined as "true"
        JMeterUtils.setProperty("transactioncontroller.use_comments_on_all", "true");
        testLog.info("\"transactioncontroller.use_comments_on_all\" set to \"true\"");
        assertEquals("\"transactioncontroller.use_comments_on_all\" set to \"true\"", "true", 
                JMeterUtils.getProperty("transactioncontroller.use_comments_on_all"));

        TestSampleListener listener1 = new TestSampleListener();
        TestSampleListener listener2 = new TestSampleListener();
        TestSampleListener listener3 = new TestSampleListener();
        
        TransactionController transactionController1 = new TransactionController();
        transactionController1.setComment("Test with default use of comments");
        transactionController1.setGenerateParentSample(true);

        TransactionController transactionController2 = new TransactionController();
        transactionController2.setComment("Test with forced use of comments");
        transactionController2.setGenerateParentSample(true);
        transactionController2.setUseComment(true);

        TransactionController transactionController3 = new TransactionController();
        transactionController3.setComment("Test with discarded use of comments");
        transactionController3.setGenerateParentSample(true);
        transactionController3.setUseComment(false);

        DebugSampler debugSampler1 = new DebugSampler();
        DebugSampler debugSampler2 = new DebugSampler();
        DebugSampler debugSampler3 = new DebugSampler();

        LoopController loop = new LoopController();
        loop.setLoops(1);
        loop.setContinueForever(false);

        ListedHashTree hashTree = new ListedHashTree();
        hashTree.add(loop);
        hashTree.add(loop, transactionController1);
        hashTree.add(transactionController1, debugSampler1);
        hashTree.add(transactionController1, listener1);
        hashTree.add(loop, transactionController2);
        hashTree.add(transactionController2);
        hashTree.add(transactionController2, debugSampler2);
        hashTree.add(transactionController2, listener2);
        hashTree.add(loop, transactionController3);
        hashTree.add(transactionController3);
        hashTree.add(transactionController3, debugSampler3);
        hashTree.add(transactionController3, listener3);
        
        TestCompiler compiler = new TestCompiler(hashTree);
        hashTree.traverse(compiler);

        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setNumThreads(1);

        ListenerNotifier notifier = new ListenerNotifier();

        JMeterThread thread = new JMeterThread(hashTree, threadGroup, notifier);
        thread.setThreadGroup(threadGroup);
        thread.setOnErrorStopThread(true);
        thread.run();

        assertEquals("First TC must force 'Comments' value from its SampleResult", "Test with default use of comments", 
                listener1.events.get(0).getResult().getSampleComments());
        assertEquals("Second TC must force 'Comments' value from its SampleResult", "Test with forced use of comments", 
                listener2.events.get(0).getResult().getSampleComments());
        assertEquals("Third TC must discard 'Comments' value from its SampleResult", "", listener3.events.get(0).getResult().getSampleComments());
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
