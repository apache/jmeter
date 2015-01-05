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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.TransactionSampler;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.ConfigMergabilityIndicator;
import org.apache.jmeter.engine.util.NoConfigMerge;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * HashTreeTraverser implementation that traverses the Test Tree to build:
 * <ul>
 *  <li>A map with key Sampler and as value the associated SamplePackage</li>
 *  <li>A map with key TransactionController and as value the associated SamplePackage</li>
 * </ul>
 */
public class TestCompiler implements HashTreeTraverser {

    private static final Logger LOG = LoggingManager.getLoggerForClass();

    /**
     * Set this property {@value} to true to revert to using a shared static set.
     */
    private static final String USE_STATIC_SET = "TestCompiler.useStaticSet";
    
    /**
     * The default value - {@value} - assumed for {@link #USE_STATIC_SET}. 
     */
    private static final boolean USE_STATIC_SET_DEFAULT = false;

    public static final boolean IS_USE_STATIC_SET = JMeterUtils.getPropDefault(USE_STATIC_SET, USE_STATIC_SET_DEFAULT);

    /**
     * This set keeps track of which ObjectPairs have been seen.
     * It seems to be used to prevent adding a child to a parent if the child has already been added.
     * If the ObjectPair (child, parent) is present, then the child has been added.
     * Otherwise, the child is added to the parent and the pair is added to the Set.
     */
    private static final Set<ObjectPair> PAIRING = new HashSet<ObjectPair>();

    private final LinkedList<TestElement> stack = new LinkedList<TestElement>();

    private final Map<Sampler, SamplePackage> samplerConfigMap = new HashMap<Sampler, SamplePackage>();

    private final Map<TransactionController, SamplePackage> transactionControllerConfigMap =
        new HashMap<TransactionController, SamplePackage>();

    private final HashTree testTree;

    public TestCompiler(HashTree testTree) {
        this.testTree = testTree;
    }

    /**
     * Clears the pairing Set Called by StandardJmeterEngine at the start of a
     * test run.
     */
    public static void initialize() {
        // synch is probably not needed as only called before run starts
        synchronized (PAIRING) {
            PAIRING.clear();
        }
    }

    /**
     * Configures sampler from SamplePackage extracted from Test plan and returns it
     * @param sampler {@link Sampler}
     * @return {@link SamplePackage}
     */
    public SamplePackage configureSampler(Sampler sampler) {
        SamplePackage pack = samplerConfigMap.get(sampler);
        pack.setSampler(sampler);
        configureWithConfigElements(sampler, pack.getConfigs());
        return pack;
    }

    /**
     * Configures Transaction Sampler from SamplePackage extracted from Test plan and returns it
     * @param transactionSampler {@link TransactionSampler}
     * @return {@link SamplePackage}
     */
    public SamplePackage configureTransactionSampler(TransactionSampler transactionSampler) {
        TransactionController controller = transactionSampler.getTransactionController();
        SamplePackage pack = transactionControllerConfigMap.get(controller);
        pack.setSampler(transactionSampler);
        return pack;
    }

    /**
     * Reset pack to its initial state
     * @param pack the {@link SamplePackage} to reset
     */
    public void done(SamplePackage pack) {
        pack.recoverRunningVersion();
    }

    /** {@inheritDoc} */
    @Override
    public void addNode(Object node, HashTree subTree) {
        stack.addLast((TestElement) node);
    }

    /** {@inheritDoc} */
    @Override
    public void subtractNode() {
        LOG.debug("Subtracting node, stack size = " + stack.size());
        TestElement child = stack.getLast();
        trackIterationListeners(stack);
        if (child instanceof Sampler) {
            saveSamplerConfigs((Sampler) child);
        }
        else if(child instanceof TransactionController) {
            saveTransactionControllerConfigs((TransactionController) child);
        }
        stack.removeLast();
        if (stack.size() > 0) {
            TestElement parent = stack.getLast();
            boolean duplicate = false;
            // Bug 53750: this condition used to be in ObjectPair#addTestElements()
            if (parent instanceof Controller && (child instanceof Sampler || child instanceof Controller)) {
                if (!IS_USE_STATIC_SET && parent instanceof TestCompilerHelper) {
                    TestCompilerHelper te = (TestCompilerHelper) parent;
                    duplicate = !te.addTestElementOnce(child);
                } else { // this is only possible for 3rd party controllers by default
                    ObjectPair pair = new ObjectPair(child, parent);
                    synchronized (PAIRING) {// Called from multiple threads
                        if (!PAIRING.contains(pair)) {
                            parent.addTestElement(child);
                            PAIRING.add(pair);
                        } else {
                            duplicate = true;
                        }
                    }
                }
            }
            if (duplicate) {
                LOG.warn("Unexpected duplicate for " + parent.getClass().getName() + " and " + child.getClass().getName());
            }
        }
    }

    @SuppressWarnings("deprecation") // TestBeanHelper.prepare() is OK
    private void trackIterationListeners(LinkedList<TestElement> p_stack) {
        TestElement child = p_stack.getLast();
        if (child instanceof LoopIterationListener) {
            ListIterator<TestElement> iter = p_stack.listIterator(p_stack.size());
            while (iter.hasPrevious()) {
                TestElement item = iter.previous();
                if (item == child) {
                    continue;
                }
                if (item instanceof Controller) {
                    TestBeanHelper.prepare(child);
                    ((Controller) item).addIterationListener((LoopIterationListener) child);
                    break;
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void processPath() {
    }

    private void saveSamplerConfigs(Sampler sam) {
        List<ConfigTestElement> configs = new LinkedList<ConfigTestElement>();
        List<Controller> controllers = new LinkedList<Controller>();
        List<SampleListener> listeners = new LinkedList<SampleListener>();
        List<Timer> timers = new LinkedList<Timer>();
        List<Assertion> assertions = new LinkedList<Assertion>();
        LinkedList<PostProcessor> posts = new LinkedList<PostProcessor>();
        LinkedList<PreProcessor> pres = new LinkedList<PreProcessor>();
        for (int i = stack.size(); i > 0; i--) {
            addDirectParentControllers(controllers, stack.get(i - 1));
            List<PreProcessor>  tempPre = new LinkedList<PreProcessor> ();
            List<PostProcessor> tempPost = new LinkedList<PostProcessor>();
            for (Object item : testTree.list(stack.subList(0, i))) {
                if ((item instanceof ConfigTestElement)) {
                    configs.add((ConfigTestElement) item);
                }
                if (item instanceof SampleListener) {
                    listeners.add((SampleListener) item);
                }
                if (item instanceof Timer) {
                    timers.add((Timer) item);
                }
                if (item instanceof Assertion) {
                    assertions.add((Assertion) item);
                }
                if (item instanceof PostProcessor) {
                    tempPost.add((PostProcessor) item);
                }
                if (item instanceof PreProcessor) {
                    tempPre.add((PreProcessor) item);
                }
            }
            pres.addAll(0, tempPre);
            posts.addAll(0, tempPost);
        }

        SamplePackage pack = new SamplePackage(configs, listeners, timers, assertions,
                posts, pres, controllers);
        pack.setSampler(sam);
        pack.setRunningVersion(true);
        samplerConfigMap.put(sam, pack);
    }

    private void saveTransactionControllerConfigs(TransactionController tc) {
        List<ConfigTestElement> configs = new LinkedList<ConfigTestElement>();
        List<Controller> controllers = new LinkedList<Controller>();
        List<SampleListener> listeners = new LinkedList<SampleListener>();
        List<Timer> timers = new LinkedList<Timer>();
        List<Assertion> assertions = new LinkedList<Assertion>();
        LinkedList<PostProcessor> posts = new LinkedList<PostProcessor>();
        LinkedList<PreProcessor> pres = new LinkedList<PreProcessor>();
        for (int i = stack.size(); i > 0; i--) {
            addDirectParentControllers(controllers, stack.get(i - 1));
            for (Object item : testTree.list(stack.subList(0, i))) {
                if (item instanceof SampleListener) {
                    listeners.add((SampleListener) item);
                }
                if (item instanceof Assertion) {
                    assertions.add((Assertion) item);
                }
            }
        }

        SamplePackage pack = new SamplePackage(configs, listeners, timers, assertions,
                posts, pres, controllers);
        pack.setSampler(new TransactionSampler(tc, tc.getName()));
        pack.setRunningVersion(true);
        transactionControllerConfigMap.put(tc, pack);
    }

    /**
     * @param controllers
     * @param i
     */
    private void addDirectParentControllers(List<Controller> controllers, TestElement maybeController) {
        if (maybeController instanceof Controller) {
            LOG.debug("adding controller: " + maybeController + " to sampler config");
            controllers.add((Controller) maybeController);
        }
    }

    private static class ObjectPair
    {
        private final TestElement child;
        private final TestElement parent;

        public ObjectPair(TestElement child, TestElement parent) {
            this.child = child;
            this.parent = parent;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return child.hashCode() + parent.hashCode();
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object o) {
            if (o instanceof ObjectPair) {
                return child == ((ObjectPair) o).child && parent == ((ObjectPair) o).parent;
            }
            return false;
        }
    }

    private void configureWithConfigElements(Sampler sam, List<ConfigTestElement> configs) {
        sam.clearTestElementChildren();
        for (ConfigTestElement config  : configs) {
            if (!(config instanceof NoConfigMerge)) 
            {
                if(sam instanceof ConfigMergabilityIndicator) {
                    if(((ConfigMergabilityIndicator)sam).applies(config)) {
                        sam.addTestElement(config);
                    }
                } else {
                    // Backward compatibility
                    sam.addTestElement(config);
                }
            }
        }
    }
}
