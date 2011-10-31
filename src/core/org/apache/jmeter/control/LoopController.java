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

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * Class that implements the Loop Controller.
 */
public class LoopController extends GenericController implements Serializable {

    private final static String LOOPS = "LoopController.loops"; // $NON-NLS-1$

    private static final long serialVersionUID = 232L;

    /*
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
    private final static String CONTINUE_FOREVER = "LoopController.continue_forever"; // $NON-NLS-1$

    private transient int loopCount = 0;

    public LoopController() {
        setContinueForever_private(true);
    }

    public void setLoops(int loops) {
        setProperty(new IntegerProperty(LOOPS, loops));
    }

    public void setLoops(String loopValue) {
        setProperty(new StringProperty(LOOPS, loopValue));
    }

    public int getLoops() {
        try {
            JMeterProperty prop = getProperty(LOOPS);
            return Integer.parseInt(prop.getStringValue());
        } catch (NumberFormatException e) {
            return 0;
        }
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
        setContinueForever_private(forever);
    }

    private void setContinueForever_private(boolean forever) {
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
        if(endOfLoop()) {
            if (!getContinueForever()) {
                setDone(true);
            }
            return null;
        }
        return super.next();
    }

    private boolean endOfLoop() {
        final int loops = getLoops();
        return (loops > -1) && (loopCount >= loops);
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
    public void startNextLoop() {
        reInitialize();
    }
}