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
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.LongProperty;

public class WeightedDistributionController extends InterleaveControl {
    private static final long serialVersionUID = 8554248250211263894L;

    public static final String SEED = "WeightedDistributionController.seed";
    public static final String WEIGHT = "WeightedDistributionController.weight";

    public static final int MIN_WEIGHT = 0;
    public static final int MAX_WEIGHT = 999999;
    public static final int DFLT_WEIGHT = MIN_WEIGHT;
    public static final long DFLT_SEED = 0l;
    
    private static final int UNSET_CUMULATIVE_PROBABILITY = -1;
    private static final int NO_ELEMENT_FOUND = 0;

    private transient int cumulativeProbability;
    private transient JMeterTreeNode node;
    private transient IntegerGenerator randomizer;

    public WeightedDistributionController() {
        node = null;
        cumulativeProbability = UNSET_CUMULATIVE_PROBABILITY;
        randomizer = null;
    }

    @Override
    protected void resetCurrent() {
        current = determineCurrentTestElement();
    }

    @Override
    protected void incrementCurrent() {
        super.incrementCurrent();
        current = determineCurrentTestElement();
    }
    
    @Override
    public void addTestElement(TestElement child) {
        if (child.isEnabled() && child.getPropertyAsInt(WEIGHT, DFLT_WEIGHT) > 0) {
            super.addTestElement(child);
        }

    }
    
    public long getSeed() {
        return getPropertyAsLong(SEED,
                WeightedDistributionController.DFLT_SEED);
    }

    public void setSeed(long seed) {
        if (getSeed() != seed) {
            setProperty(new LongProperty(SEED, seed));
            getRandomizer().setSeed(seed);
        }
    }

    public IntegerGenerator getRandomizer() {
        if (randomizer == null) {
            initRandomizer();
        }
        return randomizer;
    }
    
    public void setRandomizer(IntegerGenerator intgen) {
        randomizer = intgen;
    }
    
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
                    cumulativeProbability += currElement
                            .getPropertyAsInt(WEIGHT, DFLT_WEIGHT);
                }
            }
        }

        return cumulativeProbability;
    }

    public void resetCumulativeProbability() {
        cumulativeProbability = UNSET_CUMULATIVE_PROBABILITY;
    }

    public float calculateProbability(int weight) {
        if (getCumulativeProbability() > 0) {
            return ((float) weight / getCumulativeProbability());
        }

        return 0.0f;
    }

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

    private void initRandomizer() {
        Random rnd;
        if (getSeed() != DFLT_SEED) {
            rnd = new Random(getSeed());
        } else {
            rnd = new Random();
        }
        randomizer = new RandomIntegerGenerator(rnd);
    }

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