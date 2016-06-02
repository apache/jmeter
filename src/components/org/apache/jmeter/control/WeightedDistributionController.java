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
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.LongProperty;

public class WeightedDistributionController extends InterleaveControl {
    private static final long serialVersionUID = 8554248250211263894L;

    public static final String PER_THREAD = "WeightedDistributionController.perThread";
    public static final String SEED = "WeightedDistributionController.seed";
    public static final String WEIGHT = "WeightedDistributionController.weight";

    public static final int MIN_WEIGHT = 0;
    public static final int MAX_WEIGHT = 999999;
    public static final int DFLT_WEIGHT = MIN_WEIGHT;

    private static final int UNSET_CUMULATIVE_PROBABILITY = -1;

    private transient int cumulativeProbability;
    private transient JMeterTreeNode node;

    private transient Randomizer randomizer = null;

    public static final boolean DFTL_PERTHREAD = true;

    public static final long DFLT_SEED = 0l;

    public WeightedDistributionController() {
        node = null;
        cumulativeProbability = UNSET_CUMULATIVE_PROBABILITY;
    }

    public boolean isPerThread() {
        return getPropertyAsBoolean(PER_THREAD,
                WeightedDistributionController.DFTL_PERTHREAD);
    }

    public void setPerThread(boolean perThread) {
        setProperty(new BooleanProperty(PER_THREAD, perThread));
        getRandomizer().setPerThread(perThread);
    }

    public long getSeed() {
        return getPropertyAsLong(SEED,
                WeightedDistributionController.DFLT_SEED);
    }

    public void setSeed(long seed) {
        setProperty(new LongProperty(SEED, seed));
        getRandomizer().setSeed(seed);
    }

    public Randomizer getRandomizer() {
        if (randomizer == null) {
            initRandomizer();
        }
        return randomizer;
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
            node = GuiPackage.getInstance().getNodeOf(this);
        }
        return node;
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

    private void initRandomizer() {
        randomizer = new Randomizer(getSeed(), isPerThread());
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
        return 0;
    }
}

class Randomizer {

    private long seed;
    private boolean perThread;
    private ThreadLocal<Random> perThreadRandom = null;
    private Random globalRandom = null;

    public Randomizer(long seed, boolean perThread) {
        this.seed = seed;
        this.perThread = perThread;
    }

    public Randomizer(long seed) {
        this(seed, WeightedDistributionController.DFTL_PERTHREAD);
    }

    public Randomizer(boolean perThread) {
        this(WeightedDistributionController.DFLT_SEED, perThread);
    }

    public Randomizer() {
        this(WeightedDistributionController.DFLT_SEED,
                WeightedDistributionController.DFTL_PERTHREAD);
    }

    public boolean isPerThread() {
        return perThread;
    }

    public synchronized void setPerThread(boolean perThread) {
        this.perThread = perThread;
    }

    public long getSeed() {
        return seed;
    }

    public synchronized void setSeed(long seed) {
        if (this.seed != seed) {
            this.seed = seed;

            if (globalRandom != null) {
                globalRandom.setSeed(seed);
            }

            if (perThreadRandom != null) {
                initPerThreadRandom();
            }
        }
    }

    public synchronized int nextInt(int n) {
        return getRandom().nextInt(n);
    }

    private Random getRandom() {
        if (perThread) {
            return getPerThreadRandom();
        }
        return getGlobalRandom();
    }

    private Random getPerThreadRandom() {
        if (perThreadRandom == null) {
            initPerThreadRandom();
        }

        return perThreadRandom.get();
    }

    private Random getGlobalRandom() {
        if (globalRandom == null) {
            initGlobalRandom();
        }

        return globalRandom;
    }

    private void initPerThreadRandom() {
        perThreadRandom = new ThreadLocal<Random>() {
            @Override
            protected Random initialValue() {
                long theSeed = seed == WeightedDistributionController.DFLT_SEED
                        ? System.currentTimeMillis()
                                * Thread.currentThread().getId()
                        : seed;
                return new Random(theSeed);
            }
        };
    }

    private void initGlobalRandom() {
        long theSeed = seed == WeightedDistributionController.DFLT_SEED
                ? System.currentTimeMillis() : seed;
        globalRandom = new Random(theSeed);
    }
}
