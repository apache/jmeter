// $Header$
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author    unascribed
 * @version   $Revision$
 */
public class TestCompiler implements HashTreeTraverser, SampleListener
{
    transient private static Logger log = LoggingManager.getLoggerForClass();
    
    //TODO - should these variables be made private?
    LinkedList stack = new LinkedList();
    Map samplerConfigMap = new HashMap();
    //Set objectsWithFunctions = new HashSet();
    HashTree testTree;
    SampleResult previousResult;//TODO is this needed?
    Sampler currentSampler;//TODO is this needed?
    JMeterVariables threadVars;//TODO is this needed?
    
    /*
     * This set keeps track of which ObjectPairs have been seen
     * Its purpose is not entirely clear (please document if you know!)
     * but it is needed,..
     */
    private static Set pairing = new HashSet();

    List loopIterListeners = new ArrayList();

    public TestCompiler(HashTree testTree, JMeterVariables vars)
    {
        threadVars = vars;
        this.testTree = testTree;
    }
    
    /**
     * Clears the pairing Set
     * Called by StandardJmeterEngine at the start of a test run.
     */
    public static void initialize()
    {
        // synch is probably not needed as only called before run starts
    	synchronized(pairing){
			pairing.clear();
    	}
    }

    public void sampleOccurred(SampleEvent e)
    {
        previousResult = e.getResult();
    }

    public void sampleStarted(SampleEvent e)
    {
    }

    public void sampleStopped(SampleEvent e)
    {
    }

    public SamplePackage configureSampler(Sampler sampler)
    {
        currentSampler = sampler;
        SamplePackage pack = (SamplePackage) samplerConfigMap.get(sampler);
        pack.setSampler(sampler);
        configureWithConfigElements(sampler, pack.getConfigs());
        runPreProcessors(pack.getPreProcessors());
        //replaceStatics(ret);
        return pack;
    }

    private void runPreProcessors(List preProcessors)
    {
        Iterator iter = preProcessors.iterator();
        while (iter.hasNext())
        {
            PreProcessor ex = (PreProcessor) iter.next();
            if (log.isDebugEnabled())
            {
            	log.debug(
                	"Running preprocessor: "
                    	+ ((AbstractTestElement) ex).getName());
            }
             if (ex instanceof TestBean) ((TestBean)ex).prepare();
            ex.process();
        }
    }

    public void done(SamplePackage pack)
    {
        pack.recoverRunningVersion();
    }

    public void addNode(Object node, HashTree subTree)
    {
        stack.addLast(node);
    }

    public void subtractNode()
    {
        log.debug("Subtracting node, stack size = " + stack.size());
        TestElement child = (TestElement) stack.getLast();
        trackIterationListeners(stack);
        if (child instanceof Sampler)
        {
            saveSamplerConfigs((Sampler) child);
        }
        stack.removeLast();
        if (stack.size() > 0)
        {
            ObjectPair pair =
                new ObjectPair(
                    (TestElement) child,
                    (TestElement) stack.getLast());
			synchronized (pairing){//Called from multiple threads
                if (!pairing.contains(pair))
                {
                    pair.addTestElements();
					pairing.add(pair);
                }
            }
        }
    }

    private void trackIterationListeners(LinkedList stack)
    {
        TestElement child = (TestElement) stack.getLast();
        if (child instanceof LoopIterationListener)
        {
            ListIterator iter = stack.listIterator(stack.size());
            while (iter.hasPrevious())
            {
                TestElement item = (TestElement) iter.previous();
                if (item == child)
                {
                    continue;
                }
                else
                {
                    if (item instanceof Controller)
                    {
                        ((Controller) item).addIterationListener(
                            (LoopIterationListener) child);
                        break;
                    }
                }
            }
        }
    }

    public void processPath()
    {
    }

    private void saveSamplerConfigs(Sampler sam)
    {
        List configs = new LinkedList();
        List modifiers = new LinkedList();
        List responseModifiers = new LinkedList();
        List listeners = new LinkedList();
        List timers = new LinkedList();
        List assertions = new LinkedList();
        LinkedList posts = new LinkedList();
        LinkedList pres = new LinkedList();
        for (int i = stack.size(); i > 0; i--)
        {
            Iterator iter = testTree.list(stack.subList(0, i)).iterator();
            List tempPre = new LinkedList();
            List tempPost = new LinkedList();
            while (iter.hasNext())
            {
                TestElement item = (TestElement) iter.next();
                if ((item instanceof ConfigTestElement))
                {
                    configs.add(item);
                }
                if (item instanceof SampleListener)
                {
                    listeners.add(item);
                }
                if (item instanceof Timer)
                {
                    timers.add(item);
                }
                if (item instanceof Assertion)
                {
                    assertions.add(item);
                }
                if (item instanceof PostProcessor)
                {
                    tempPost.add(item);
                }
                if (item instanceof PreProcessor)
                {
                    tempPre.add(item);
                }
            }
            pres.addAll(0,tempPre);
            posts.addAll(0,tempPost);
        }

        SamplePackage pack =
            new SamplePackage(
                configs,
                modifiers,
                responseModifiers,
                listeners,
                timers,
                assertions,
                posts,
                pres);
        pack.setSampler(sam);
        pack.setRunningVersion(true);
        samplerConfigMap.put(sam, pack);
    }

    /**
     * @version   $Revision$
     */
    public static class Test extends junit.framework.TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testConfigGathering() throws Exception
        {
            ListedHashTree testing = new ListedHashTree();
            GenericController controller = new GenericController();
            ConfigTestElement config1 = new ConfigTestElement();
            config1.setName("config1");
            config1.setProperty("test.property", "A test value");
            TestSampler sampler = new TestSampler();
            sampler.setName("sampler");
            testing.add(controller, config1);
            testing.add(controller, sampler);
            TestCompiler.initialize();

            TestCompiler compiler =
                new TestCompiler(testing, new JMeterVariables());
            testing.traverse(compiler);
            sampler =
                (TestSampler) compiler.configureSampler(sampler).getSampler();
            assertEquals(
                "A test value",
                sampler.getPropertyAsString("test.property"));
        }

        class TestSampler extends AbstractSampler
        {
            public SampleResult sample(org.apache.jmeter.samplers.Entry e)
            {
                return null;
            }
            public Object clone()
            {
                return new TestSampler();
            }
        }
    }

    private class ObjectPair
    {
        TestElement child, parent;

        public ObjectPair(TestElement one, TestElement two)
        {
            this.child = one;
            this.parent = two;
        }

        public void addTestElements()
        {
            if (parent instanceof Controller
                && (child instanceof Sampler || child instanceof Controller))
            {
                parent.addTestElement(child);
            }
        }

        public int hashCode()
        {
            return child.hashCode() + parent.hashCode();
        }

        public boolean equals(Object o)
        {
            if (o instanceof ObjectPair)
            {
                return child == ((ObjectPair) o).child
                    && parent == ((ObjectPair) o).parent;
            }
            return false;
        }
    }

    private void configureWithConfigElements(Sampler sam, List configs)
    {
        Iterator iter = configs.iterator();
        while (iter.hasNext())
        {
            ConfigTestElement config = (ConfigTestElement)iter.next();
            sam.addTestElement(config);
        }
    }
}
