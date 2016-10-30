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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The Class WeightedDistributionController.
 * 
 * The Weighted Distribution Controller randomly selects one of its child
 * elements (samplers/controllers) to be executed. The probability of a child
 * being executed depends upon the relative weight assigned to that element. An
 * expression can be used as a weight, as long as that expression evaluates to a
 * positive integer.
 */
public class WeightedDistributionController extends InterleaveControl {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8554248250211263894L;

    /** The logger */
    static final Logger log = LoggingManager.getLoggerForClass();

    /** The Constant SEED Property key */
    public static final String GENERATOR_SEED = "WeightedDistributionController.seed";

    /** The Constant WEIGHT Property key */
    public static final String WEIGHT = "WeightedDistributionController.weight";

    /** The Constant DFLT_WEIGHT. Default Weight value */
    public static final int DFLT_WEIGHT = 0;

    /** The Constant DFLT_SEED. Default Seed value */
    public static final long DFLT_GENERATOR_SEED = 0l;

    /**
     * The Constant NO_ELEMENT_FOUND. Used as flag to indicate that No elements
     * were returned when finding next element
     */
    private static final int NO_ELEMENT_FOUND = 0;

    /** The cumulative probability. */
    private transient int cumulativeProbability;

    /** The node. associated with this controller */
    private transient JMeterTreeNode node;

    /** The randomizer. */
    private transient IntegerGenerator integerGenerator;

    /** The value replacer for evaluating variable properties. */
    private transient ValueReplacer replacer;
    
    private transient boolean resetCumulativeProbability;

    /**
     * Instantiates a new weighted distribution controller.
     */
    public WeightedDistributionController() {
        node = null;
        resetCumulativeProbability = true;
        integerGenerator = null;
        replacer = initValueReplacer();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.control.GenericController#resetCurrent()
     */
    @Override
    protected void resetCurrent() {
        current = determineCurrentTestElement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.control.InterleaveControl#incrementCurrent()
     */
    @Override
    protected void incrementCurrent() {
        super.incrementCurrent();
        current = determineCurrentTestElement();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.control.GenericController#addTestElement(org.apache.
     * jmeter.testelement.TestElement)
     */
    @Override
    public void addTestElement(TestElement child) {
        // if you do not filter enabled elements, if no elements are found by
        // determineCurrentTestElement() it will return the first element
        if (child.isEnabled()
                && child.getPropertyAsInt(WEIGHT, DFLT_WEIGHT) > 0) {
            super.addTestElement(child);
        }

    }

    /**
     * Gets the seed.
     *
     * @return the seed
     */
    public long getGeneratorSeed() {
        return getPropertyAsLong(GENERATOR_SEED,
                WeightedDistributionController.DFLT_GENERATOR_SEED);
    }

    /**
     * Sets the seed.
     *
     * @param seed
     *            the new seed
     */
    public void setGeneratorSeed(long seed) {
        if (getGeneratorSeed() != seed) {
            setProperty(new LongProperty(GENERATOR_SEED, seed));
            integerGenerator = null;
        }
    }

    /**
     * Gets the randomizer.
     *
     * @return the randomizer
     */
    public IntegerGenerator getIntegerGenerator() {
        if (integerGenerator == null) {
            initIntegerGenerator();
        }
        return integerGenerator;
    }

    /**
     * Sets the randomizer.
     *
     * @param intgen
     *            the new randomizer
     */
    public void setIntegerGenerator(IntegerGenerator intgen) {
        integerGenerator = intgen;
    }

    /**
     * Gets the child node.
     *
     * @param idx
     *            the index of the child node
     * @return the child node
     */
    public JMeterTreeNode getChildNode(int idx) {
        JMeterTreeNode childNode;

        try {
            childNode = (JMeterTreeNode) getNode().getChildAt(idx);
        } catch (Exception ex) {
            log.error("Unable to retreive child node at index: " + idx, ex);
            childNode = null;
        }

        return childNode;
    }

    /**
     * Gets the child test element.
     *
     * @param idx
     *            the index of the child node
     * @return the child test element
     */
    public TestElement getChildTestElement(int idx) {
        JMeterTreeNode childNode = getChildNode(idx);
        return childNode == null ? null : childNode.getTestElement();
    }

    /**
     * Gets the cumulative probability.
     *
     * @return the cumulative probability
     */
    public int getCumulativeProbability() {
        // recalculate if reset flag is set
        if (resetCumulativeProbability) {
            cumulativeProbability = 0;
            resetCumulativeProbability = false;

            SubControllerIterator subControllerIter = new SubControllerIterator(this);

            while (subControllerIter.hasNext()) {
                TestElement currElement = subControllerIter.next();
                TestElement currEvalSubCtrl = evaluateTestElement(
                        currElement);
                int currWeight = currEvalSubCtrl.getPropertyAsInt(WEIGHT,
                        DFLT_WEIGHT);

                // filter negative weights
                cumulativeProbability += (currWeight > 0 ? currWeight : 0);
            }
        }

        return cumulativeProbability;
    }

    /**
     * Reset cumulative probability.
     */
    public void resetCumulativeProbability() {
        resetCumulativeProbability = true;
        cumulativeProbability = 0;
    }

    /**
     * Calculate probability.
     *
     * @param weight
     *            the weight
     * @return the float
     */
    public float calculateProbability(int weight) {
        if (getCumulativeProbability() > 0) {
            return ((float) (weight < 0 ? 0 : weight)
                    / getCumulativeProbability());
        }

        return 0.0f;
    }

    /**
     * Returns a clone of the test element with any variable properties
     * evaluated.
     *
     * @param testElement
     *            the source test element
     * @return the cloned and evaluated test element
     */
    public TestElement evaluateTestElement(TestElement testElement) {
        TestElement clonedTestElem = (TestElement) testElement.clone();

        try {
            replacer.replaceValues(clonedTestElem);
        } catch (InvalidVariableException e) {
            return testElement;
        }

        clonedTestElem.setRunningVersion(true);
        return clonedTestElem;
    }

    /**
     * Gets the node associated with this weighted distribution controller
     *
     * @return the node
     */
    public JMeterTreeNode getNode() {
        if (node == null || node.getTestElement() != this
                || node.getParent() == null) {
            try {
                node = GuiPackage.getInstance().getNodeOf(this);
            } catch (NullPointerException npe) {
                // This NPE is typically caused by GuiPackage not being
                // initialized when
                // Running unit tests
                node = null;
            }
        }
        return node;
    }

    /**
     * Initialized the randomizer with the seed, if set.
     */
    private void initIntegerGenerator() {
        if (getGeneratorSeed() == DFLT_GENERATOR_SEED) {
            integerGenerator = new RandomIntegerGenerator();
        } else {
            integerGenerator = new RandomIntegerGenerator(getGeneratorSeed());
        }
    }

    /**
     * Randomly determine the index of current test element.
     *
     * @return the index of the current test element
     */
    private int determineCurrentTestElement() {
        if (getCumulativeProbability() > 0) {
            int currentRandomizer = getIntegerGenerator()
                    .nextInt(getCumulativeProbability());
            List<TestElement> subControllers = getSubControllers();

            for (int currSubCtrlIdx = 0; currSubCtrlIdx < subControllers
                    .size(); currSubCtrlIdx++) {
                TestElement currSubController = subControllers
                        .get(currSubCtrlIdx);
                if (currSubController.isEnabled()) {
                    int currWeight = currSubController.getPropertyAsInt(WEIGHT,
                            DFLT_WEIGHT);
                    if (currWeight > currentRandomizer) {
                        return currSubCtrlIdx;
                    } else {
                        currentRandomizer -= currWeight;
                    }
                }
            }
        }
        return NO_ELEMENT_FOUND;
    }

    private ValueReplacer initValueReplacer() {
        ValueReplacer replacer;

        try {
            replacer = GuiPackage.getInstance().getReplacer();
        } catch (Exception e) {
            replacer = new ValueReplacer();
        }

        return replacer;
    }
}

/**
 * SubControllerIterator
 * 
 * The list of subcontrollers is generated differently if called during test execution rather
 * Test plan building gui mode, this abstracts the way that are accessed
 */
class SubControllerIterator implements Iterator<TestElement> {
    
    @SuppressWarnings("rawtypes")
    private Enumeration subControllerEnumeration = null;
        
    private TestElement nextSubController;
    
    public SubControllerIterator(WeightedDistributionController wdc) {
        subControllerEnumeration = null;
        nextSubController = null;
        
        // When calling in during test execution, the child elements are
        // listed in getSubControllers, but during test design time,
        // elements need to be pulled from the node tree
        if (wdc.getSubControllers().size() > 0) {
            subControllerEnumeration =  new Vector<TestElement>(wdc.getSubControllers()).elements();
        } else if (wdc.getNode() != null
                && wdc.getNode().children().hasMoreElements()) {
            subControllerEnumeration =  wdc.getNode().children();
        } else {
            WeightedDistributionController.log.warn("Unable to find subcontrollers");
            subControllerEnumeration = null;
        }
    }

    @Override
    public boolean hasNext() {
        if (nextSubController != null) {
            return true;
        } else {
            setNextSubController();
            return nextSubController != null;
        }
    }

    @Override
    public TestElement next() {
        if (nextSubController == null) {
            setNextSubController();
            if (nextSubController == null) {
                throw new NoSuchElementException();
            }
        }
        
        TestElement returnSubController = nextSubController;
        nextSubController = null;
        
        return returnSubController;
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Iterator.remove() not implemented");
    }
    
    private void setNextSubController() {
        if (subControllerEnumeration != null && subControllerEnumeration.hasMoreElements()) {
            Object currSubCtrl = subControllerEnumeration.nextElement();
            
            if (currSubCtrl instanceof JMeterTreeNode) {
                nextSubController = ((JMeterTreeNode) currSubCtrl).getTestElement();
            } else if (currSubCtrl instanceof TestElement) {
                nextSubController = (TestElement) currSubCtrl;
            }
            
            if (!(nextSubController.isEnabled()) && (nextSubController instanceof Controller || nextSubController instanceof Sampler)) {
                setNextSubController();
            }
        } else {
            nextSubController = null;
        }
    }
}

/**
 * This abstraction is necessary for unit tests, in which case a deterministic
 * generator replaces the random one
 */
interface IntegerGenerator {
    int nextInt(int n);
}

/**
 * Implementation of the IntegerGenerator used to provide random values at
 * execution time
 */
class RandomIntegerGenerator implements IntegerGenerator {

    private Random rnd;

    public RandomIntegerGenerator() {
        this(new Random());
    }

    public RandomIntegerGenerator(long seed) {
        this(new Random(seed));
    }

    private RandomIntegerGenerator(Random rnd) {
        this.rnd = rnd;
    }

    @Override
    public int nextInt(int n) {
        return rnd.nextInt(n);
    }
}
