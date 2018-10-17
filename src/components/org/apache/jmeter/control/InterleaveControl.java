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
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;

/**
 * Alternate among each of the children controllers or samplers for each loop iteration
 */
public class InterleaveControl extends GenericController implements Serializable {
    
    private static final long serialVersionUID = 234L;

    private static final String STYLE = "InterleaveControl.style";// $NON-NLS-1$
    
    private static final String ACCROSS_THREADS = "InterleaveControl.accrossThreads";// $NON-NLS-1$

    public static final int IGNORE_SUB_CONTROLLERS = 0;

    public static final int USE_SUB_CONTROLLERS = 1;

    private boolean skipNext;

    private transient TestElement searchStart = null;

    private boolean currentReturnedAtLeastOne;

    private boolean stillSame = true;

    /***************************************************************************
     * Constructor for the InterleaveControl object
     **************************************************************************/
    public InterleaveControl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reInitialize() {
        setFirst(true);
        currentReturnedAtLeastOne = false;
        searchStart = null;
        stillSame = true;
        skipNext = false;
        incrementIterCount();
        recoverRunningVersion();
    }

    public void setStyle(int style) {
        setProperty(new IntegerProperty(STYLE, style));
    }

    public int getStyle() {
        return getPropertyAsInt(STYLE);
    }
    
    public void setInterleaveAccrossThreads(boolean accrossThreads) {
        setProperty(new BooleanProperty(ACCROSS_THREADS, accrossThreads));
    }

    public boolean getInterleaveAccrossThreads() {
        return getPropertyAsBoolean(ACCROSS_THREADS, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sampler next() {
        if (isSkipNext()) {
            reInitialize();
            return null;
        }
        return super.next();
    }

   /**
     * {@inheritDoc}
     */
    @Override
    protected Sampler nextIsAController(Controller controller) throws NextIsNullException {
        Sampler sampler = controller.next();
        if (sampler == null) {
            currentReturnedNull(controller);
            return next();
        }
        currentReturnedAtLeastOne = true;
        if (getStyle() == IGNORE_SUB_CONTROLLERS) {
            incrementCurrent();
            skipNext = true;
        } else {
            searchStart = null;
        }
        return sampler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Sampler nextIsASampler(Sampler element) throws NextIsNullException {
        skipNext = true;
        incrementCurrent();
        return element;
    }

    /**
     * If the current is null, reset and continue searching. The searchStart
     * attribute will break us off when we start a repeat.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected Sampler nextIsNull() {
        resetCurrent();
        return next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setCurrentElement(TestElement currentElement) throws NextIsNullException {
        // Set the position when next is first called, and don't overwrite
        // until reInitialize is called.
        if (searchStart == null) {
            searchStart = currentElement;
        } else if (searchStart == currentElement && !stillSame) {
            // We've gone through the whole list and are now back at the start
            // point of our search.
            reInitialize();
            throw new NextIsNullException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void currentReturnedNull(Controller c) {
        if (c.isDone()) {
            removeCurrentElement();
        } else if (getStyle() == USE_SUB_CONTROLLERS) {
            incrementCurrent();
        }
    }

    protected boolean isSkipNext() {
        return skipNext;
    }

    protected void setSkipNext(boolean skipNext) {
        this.skipNext = skipNext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void incrementCurrent() {
        if (currentReturnedAtLeastOne) {
            skipNext = true;
        }
        stillSame = false;
        super.incrementCurrent();
    }

    /**
     * @see org.apache.jmeter.control.GenericController#initialize()
     */
    @Override
    public void initialize() {
        super.initialize();
        // get a different start index
        if(getInterleaveAccrossThreads()) {
            this.current = getThreadContext().getThreadNum() % getSubControllers().size();
        }
    }
}
