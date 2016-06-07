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
import java.util.List;
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

// TODO: Auto-generated Javadoc
/**
 * The Class WeightedDistributionController.
 */
public class WeightedDistributionController extends InterleaveControl {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 8554248250211263894L;

    /** The Constant SEED. */
    public static final String SEED = "WeightedDistributionController.seed";
    
    /** The Constant WEIGHT. */
    public static final String WEIGHT = "WeightedDistributionController.weight";

    /** The Constant MIN_WEIGHT. */
    public static final int MIN_WEIGHT = 0;
    
    /** The Constant MAX_WEIGHT. */
    public static final int MAX_WEIGHT = 999999;
    
    /** The Constant DFLT_WEIGHT. */
    public static final int DFLT_WEIGHT = MIN_WEIGHT;
    
    /** The Constant DFLT_SEED. */
    public static final long DFLT_SEED = 0l;
    
    /** The Constant UNSET_CUMULATIVE_PROBABILITY. */
    private static final int UNSET_CUMULATIVE_PROBABILITY = -1;
    
    /** The Constant NO_ELEMENT_FOUND. */
    private static final int NO_ELEMENT_FOUND = 0;

    /** The cumulative probability. */
    private transient int cumulativeProbability;
    
    /** The node. */
    private transient JMeterTreeNode node;
    
    /** The randomizer. */
    private transient IntegerGenerator randomizer;

    /**
     * Instantiates a new weighted distribution controller.
     */
    public WeightedDistributionController() {
        node = null;
        cumulativeProbability = UNSET_CUMULATIVE_PROBABILITY;
        randomizer = null;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#resetCurrent()
     */
    @Override
    protected void resetCurrent() {
        current = determineCurrentTestElement();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.InterleaveControl#incrementCurrent()
     */
    @Override
    protected void incrementCurrent() {
        super.incrementCurrent();
        current = determineCurrentTestElement();
    }
    
    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#addTestElement(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void addTestElement(TestElement child) {
        if (child.isEnabled() && child.getPropertyAsInt(WEIGHT, DFLT_WEIGHT) > 0) {
            super.addTestElement(child);
        }

    }
    
    /**
     * Gets the seed.
     *
     * @return the seed
     */
    public long getSeed() {
        return getPropertyAsLong(SEED,
                WeightedDistributionController.DFLT_SEED);
    }

    /**
     * Sets the seed.
     *
     * @param seed the new seed
     */
    public void setSeed(long seed) {
        if (getSeed() != seed) {
            setProperty(new LongProperty(SEED, seed));
            getRandomizer().setSeed(seed);
        }
    }

    /**
     * Gets the randomizer.
     *
     * @return the randomizer
     */
    public IntegerGenerator getRandomizer() {
        if (randomizer == null) {
            initRandomizer();
        }
        return randomizer;
    }
    
    /**
     * Sets the randomizer.
     *
     * @param intgen the new randomizer
     */
    public void setRandomizer(IntegerGenerator intgen) {
        randomizer = intgen;
    }
    
    /**
     * Gets the cumulative probability.
     *
     * @return the cumulative probability
     */
    public int getCumulativeProbability() {
        if (cumulativeProbability == UNSET_CUMULATIVE_PROBABILITY) {
            cumulativeProbability = 0;
            @SuppressWarnings("rawtypes")
            Enumeration subControllers = null;
            if (this.getSubControllers().size() > 0) {
                subControllers = new Vector<TestElement>(
                        this.getSubControllers()).elements();
            } else if (getNode() != null
                    && getNode().children().hasMoreElements()) {
                subControllers = getNode().children();
            }
            ValueReplacer replacer = GuiPackage.getInstance().getReplacer();
            while (subControllers != null && subControllers.hasMoreElements()) {
                Object currSubCtrl = subControllers.nextElement();
                TestElement currElement;
                if (currSubCtrl instanceof JMeterTreeNode) {
                    currElement = ((JMeterTreeNode) currSubCtrl)
                            .getTestElement();
                } else {
                    currElement = (TestElement) currSubCtrl;
                }       
                
                if (currElement.isEnabled()
                        && (currElement instanceof Controller
                                || currElement instanceof Sampler)) {
                    TestElement currEvalSubCtrl = (TestElement) currElement.clone();                    
                    try {
                        replacer.replaceValues(currEvalSubCtrl);
                    } catch (InvalidVariableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    currEvalSubCtrl.setRunningVersion(true);
                    
                    cumulativeProbability += currEvalSubCtrl
                            .getPropertyAsInt(WEIGHT, DFLT_WEIGHT);
                }
            }
        }

        return cumulativeProbability;
    }

    /**
     * Reset cumulative probability.
     */
    public void resetCumulativeProbability() {
        cumulativeProbability = UNSET_CUMULATIVE_PROBABILITY;
    }

    /**
     * Calculate probability.
     *
     * @param weight the weight
     * @return the float
     */
    public float calculateProbability(int weight) {
        if (getCumulativeProbability() > 0) {
            return ((float) weight / getCumulativeProbability());
        }

        return 0.0f;
    }

    /**
     * Gets the node.
     *
     * @return the node
     */
    public JMeterTreeNode getNode() {
        if (node == null || node.getTestElement() != this
                || node.getParent() == null) {
            try {
                node = GuiPackage.getInstance().getNodeOf(this);
            } catch (NullPointerException npe) {
                // This NPE is typically caused by GuiPackage not being initialized when
                // Running unit tests
                node = null;
            }
        }
        return node;
    }

    /**
     * Inits the randomizer.
     */
    private void initRandomizer() {
        Random rnd;
        if (getSeed() != DFLT_SEED) {
            rnd = new Random(getSeed());
        } else {
            rnd = new Random();
        }
        randomizer = new RandomIntegerGenerator(rnd);
    }

    /**
     * Determine current test element.
     *
     * @return the int
     */
    private int determineCurrentTestElement() {
        if (getCumulativeProbability() > 0) {
            int currentRandomizer = getRandomizer()
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
}

interface IntegerGenerator {
    int nextInt(int n);
    void setSeed(long seed);
}

class RandomIntegerGenerator implements IntegerGenerator {

    private Random rnd;
    
    public RandomIntegerGenerator(Random rnd) {
        this.rnd = rnd;
    }

    @Override
    public int nextInt(int n) {
        return rnd.nextInt(n);
    }

    @Override
    public void setSeed(long seed) {
        rnd.setSeed(seed);  
    }
}