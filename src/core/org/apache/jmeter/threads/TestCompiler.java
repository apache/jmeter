package org.apache.jmeter.threads;
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
import org.apache.jmeter.config.Modifier;
import org.apache.jmeter.config.ResponseBasedModifier;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.PerSampleClonable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.HashTreeTraverser;
import org.apache.jorphan.collections.ListedHashTree;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * <p>
 *
 * Title: </p> <p>
 *
 * Description: </p> <p>
 *
 * Copyright: Copyright (c) 2001</p> <p>
 *
 * Company: </p>
 *
 *@author    unascribed
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class TestCompiler implements HashTreeTraverser, SampleListener
{
    transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor("jmeter.engine");
    LinkedList stack = new LinkedList();
    Map samplerConfigMap = new HashMap();
    Set objectsWithFunctions = new HashSet();
    HashTree testTree;
    SampleResult previousResult;
    Sampler currentSampler;
    JMeterVariables threadVars;
    private static Set pairing = new HashSet();

    /****************************************
     * !ToDo (Constructor description)
     *
     *@param testTree  !ToDo (Parameter description)
     ***************************************/
    public TestCompiler(HashTree testTree, JMeterVariables vars)
    {
        threadVars = vars;
        this.testTree = testTree;
    }

    /****************************************
     * !ToDo (Method description)
     ***************************************/
    public static void initialize()
    {
        pairing.clear();
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param e  !ToDo (Parameter description)
     ***************************************/
    public void sampleOccurred(SampleEvent e)
    {
        previousResult = e.getResult();
    }

    /****************************************
     * !ToDo (Method description)
     *
     *@param e  !ToDo (Parameter description)
     ***************************************/
    public void sampleStarted(SampleEvent e)
    {}

    /****************************************
     * !ToDo (Method description)
     *
     *@param e  !ToDo (Parameter description)
     ***************************************/
    public void sampleStopped(SampleEvent e)
    {}

    /****************************************
     * !ToDo (Method description)
     *
     *@param sampler  !ToDo (Parameter description)
     *@return         !ToDo (Return description)
     ***************************************/
    public SamplePackage configureSampler(Sampler sampler)
    {
        currentSampler = sampler;
        SamplePackage pack = (SamplePackage) samplerConfigMap.get(sampler);
        pack.setRunningVersion(true);
        pack.setSampler(sampler);
        runPreProcessors(pack.getPreProcessors());
        configureWithConfigElements(sampler, pack.getConfigs());
        configureWithResponseModifiers(sampler, pack.getResponseModifiers());
        configureWithModifiers(sampler, pack.getModifiers());
        //replaceStatics(ret);
        return pack;
    }
    
    private void runPreProcessors(List preProcessors)
       {
           ListIterator iter = preProcessors.listIterator(preProcessors.size());
           while (iter.hasPrevious())
           {
               PreProcessor ex = (PreProcessor) iter.previous();
               ex.process();
           }
       }
    
    public void done(SamplePackage pack)
    {
        pack.recoverRunningVersion();
        pack.setRunningVersion(false);
    }

    /****************************************
     * !ToDo
     *
     *@param node     !ToDo
     *@param subTree  !ToDo
     ***************************************/
    public void addNode(Object node, HashTree subTree)
    {
        stack.addLast(node);
        log.debug("Added " + node + " to stack.  Stack size = " + stack.size());
    }

    /****************************************
     * !ToDo (Method description)
     ***************************************/
    public void subtractNode()
    {
        log.debug("Subtracting node, stack size = " + stack.size());
        TestElement child = (TestElement) stack.getLast();
        if (child instanceof Sampler)
        {
            log.debug("Saving configs for sampler: " + child);
            saveSamplerConfigs((Sampler) child);
        }
        stack.removeLast();
        if (stack.size() > 0)
        {
            ObjectPair pair = new ObjectPair((TestElement)child, (TestElement)stack.getLast());
            if (!pairing.contains(pair))
            {
                pair.addTestElements();
                pairing.add(pair);
            }
        }
    }

    /****************************************
     * !ToDo (Method description)
     ***************************************/
    public void processPath()
    {}

    private void saveSamplerConfigs(Sampler sam)
    {
        List configs = new LinkedList();
        List modifiers = new LinkedList();
        List responseModifiers = new LinkedList();
        List listeners = new LinkedList();
        List timers = new LinkedList();
        List assertions = new LinkedList();
        List extractors = new LinkedList();
        List pres = new LinkedList();
        for (int i = stack.size(); i > 0; i--)
        {
            Iterator iter = testTree.list(stack.subList(0, i)).iterator();
            while (iter.hasNext())
            {
                TestElement item = (TestElement) iter.next();
                if ((item instanceof ConfigTestElement))
                {
                    configs.add(item);
                }
                if (item instanceof Modifier)
                {
                    modifiers.add(item);
                }
                if (item instanceof ResponseBasedModifier)
                {
                    responseModifiers.add(item);
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
                    extractors.add(item);
                }
                if (item instanceof PreProcessor)
                                {
                                    pres.add(item);
                                }
            }
        }
        SamplePackage pack = new SamplePackage(configs, modifiers, responseModifiers, listeners, timers, assertions, extractors,pres);
        pack.setSampler(sam);
        samplerConfigMap.put(sam, pack);
    }

    

    /****************************************
     * !ToDo (Class description)
     *
     *@author    $Author$
     *@created   $Date$
     *@version   $Revision$
     ***************************************/
    public static class Test extends junit.framework.TestCase
    {
        /****************************************
         * !ToDo (Constructor description)
         *
         *@param name  !ToDo (Parameter description)
         ***************************************/
        public Test(String name)
        {
            super(name);
        }

        /****************************************
         * !ToDo
         *
         *@exception Exception  !ToDo (Exception description)
         ***************************************/
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

            TestCompiler compiler = new TestCompiler(testing, new JMeterVariables());
            testing.traverse(compiler);
            sampler = (TestSampler) compiler.configureSampler(sampler).getSampler();
            assertEquals("A test value", sampler.getPropertyAsString("test.property"));
        }

        class TestSampler extends AbstractSampler implements PerSampleClonable {
            public void addCustomTestElement(TestElement t)
            {}
            public org.apache.jmeter.samplers.SampleResult sample(org.apache.jmeter.samplers.Entry e)
            {
                return null;
            }
            public Object clone()
            {
                return new TestSampler();
            }
        }
    }

    /****************************************
     * !ToDo (Class description)
     *
     *@author    $Author$
     *@created   $Date$
     *@version   $Revision$
     ***************************************/
    private class ObjectPair
    {
        TestElement child, parent;

        /****************************************
         * !ToDo (Constructor description)
         *
         *@param one  !ToDo (Parameter description)
         *@param two  !ToDo (Parameter description)
         ***************************************/
        public ObjectPair(TestElement one, TestElement two)
        {
            this.child = one;
            this.parent = two;
        }
        
        public void addTestElements()
        {
            if(parent instanceof Controller && child instanceof Sampler)
            {
                parent.addTestElement(child);
            }
        }

        /****************************************
         * !ToDo (Method description)
         *
         *@return   !ToDo (Return description)
         ***************************************/
        public int hashCode()
        {
            return child.hashCode() + parent.hashCode();
        }

        /****************************************
         * !ToDo (Method description)
         *
         *@param o  !ToDo (Parameter description)
         *@return   !ToDo (Return description)
         ***************************************/
        public boolean equals(Object o)
        {
            if (o instanceof ObjectPair)
            {
                return child == ((ObjectPair) o).child && parent == ((ObjectPair) o).parent;
            }
            return false;
        }
    }

    private void configureWithConfigElements(Sampler sam, List configs)
    {
        Iterator iter = configs.iterator();
        while (iter.hasNext())
        {
            sam.addTestElement((ConfigTestElement) iter.next());
        }
    }

    private void configureWithModifiers(Sampler sam, List modifiers)
    {
        Iterator iter = modifiers.iterator();
        while (iter.hasNext())
        {
            Modifier mod = (Modifier) iter.next();
            mod.modifyEntry(sam);
        }
    }

    private void configureWithResponseModifiers(Sampler sam, List responseModifiers)
    {
        Iterator iter = responseModifiers.iterator();
        while (iter.hasNext())
        {
            ResponseBasedModifier mod = (ResponseBasedModifier) iter.next();
            if (previousResult != null)
            {
                mod.modifyEntry(sam, previousResult);
            }
        }
    }
}
