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

import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements the Loop Controller, ie iterate infinitely or a configured number of times
 */
public class LoopController extends GenericController implements Serializable, IteratingController, LoopIterationListener {
    
    public static final int INFINITE_LOOP_COUNT = -1; // $NON-NLS-1$
    
    public static final String LOOPS = "LoopController.loops"; // $NON-NLS-1$

    private static final long serialVersionUID = 7833960784370272300L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoopController.class);
    /**
     * In spite of the name, this is actually used to determine if the loop controller is repeatable.
     *
     * The value is only used in nextIsNull() when the loop end condition has been detected:
     * If forever==true, then it calls resetLoopCount(), otherwise it calls setDone(true).
     *
     * Loop Controllers always set forever=true, so that they will be executed next time
     * the parent invokes them.
     *
     * Thread Group sets the value false, so nextIsNull() sets done, and the Thread Group will not be repeated.
     * However, it's not clear that a Thread Group could ever be repeated.
     */
    private static final String CONTINUE_FOREVER = "LoopController.continue_forever"; // $NON-NLS-1$

    private transient int loopCount = 0;

    /**
     * Cached loop value 
     * see Bug 54467
     */
    private transient Integer nbLoops;

    private boolean breakLoop;

    public LoopController() {
        setContinueForeverPrivate(true);
    }

    public void setLoops(int loops) {
        setProperty(new IntegerProperty(LOOPS, loops));
    }

    public void setLoops(String loopValue) {
        setProperty(new StringProperty(LOOPS, loopValue));
    }

    public int getLoops() {
        // Evaluation occurs when nbLoops is not yet evaluated 
        // or when nbLoops is equal to special value INFINITE_LOOP_COUNT
        if (nbLoops==null || // No evaluated yet
                nbLoops.intValue()==0 || // Last iteration led to nbLoops == 0,
                                         // in this case as resetLoopCount will not be called,
                                         // it leads to no further evaluations if we don't evaluate, see BUG 56276
                nbLoops.intValue()==INFINITE_LOOP_COUNT // Number of iteration is set to infinite
                ) {
            try {
                JMeterProperty prop = getProperty(LOOPS);
                nbLoops = Integer.valueOf(prop.getStringValue());
            } catch (NumberFormatException e) {
                nbLoops = Integer.valueOf(0);
            }
        }
        return nbLoops.intValue();
    }

    public String getLoopString() {
        return getPropertyAsString(LOOPS);
    }

    /**
     * Determines whether the loop will return any samples if it is rerun.
     *
     * @param forever
     *            true if the loop must be reset after ending a run
     */
    public void setContinueForever(boolean forever) {
        setContinueForeverPrivate(forever);
    }

    private void setContinueForeverPrivate(boolean forever) {
        setProperty(new BooleanProperty(CONTINUE_FOREVER, forever));
    }

    private boolean getContinueForever() {
        return getPropertyAsBoolean(CONTINUE_FOREVER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sampler next() {
        try {
            if(endOfLoop()) {
                if (!getContinueForever()) {
                    setDone(true);
                }
                resetBreakLoop();
                return null;
            }
            return super.next();
        } finally {
            updateIterationIndex(getName(), loopCount);
        }
    }
    
    private boolean endOfLoop() {
        final int loops = getLoops();
        return breakLoop || (loops > INFINITE_LOOP_COUNT) && (loopCount >= loops);
    }

    @Override
    protected void setDone(boolean done) {
        resetBreakLoop();
        nbLoops = null;
        super.setDone(done);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Sampler nextIsNull() throws NextIsNullException {
        reInitialize();
        if (endOfLoop()) {
            if (!getContinueForever()) {
                setDone(true);
            } else {
                resetLoopCount();
            }
            return null;
        }
        return next();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerEndOfLoop() {
        super.triggerEndOfLoop();
        resetLoopCount();
    }
    
    protected void incrementLoopCount() {
        loopCount++;
    }

    protected void resetLoopCount() {
        loopCount = 0;
        nbLoops = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getIterCount() {
        return loopCount + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reInitialize() {
        setFirst(true);
        resetCurrent();
        incrementLoopCount();
        recoverRunningVersion();
    }
    
    /**
     * Start next iteration
     */
    @Override
    public void startNextLoop() {
        reInitialize();
    }

    private void resetBreakLoop() {
        if(breakLoop) {
            breakLoop = false;
        }
    }

    @Override
    public void breakLoop() {
        breakLoop = true;
        setFirst(true);
        resetCurrent();
        resetLoopCount();
        recoverRunningVersion();
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("iterationStart called on {} with source {} and iteration {}", getName(), iterEvent.getSource(), iterEvent.getIteration());
        }
        reInitialize();
        resetLoopCount();
    }
}
