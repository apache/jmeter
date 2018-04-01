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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.TestCompilerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class is the basis for all the controllers.
 * It also implements SimpleController.
 * </p>
 * <p>
 * The main entry point is next(), which is called by JMeterThread as follows:
 * </p>
 * <p>
 * <code>while (running &amp;&amp; (sampler = controller.next()) != null)</code>
 * </p>
 */
public class GenericController extends AbstractTestElement implements Controller, Serializable, TestCompilerHelper {

    private static final long serialVersionUID = 235L;

    private static final Logger log = LoggerFactory.getLogger(GenericController.class);

    static final String INDEX_VAR_NAME_SUFFIX = "__idx";

    private transient LinkedList<LoopIterationListener> iterationListeners = new LinkedList<>();

    // Only create the map if it is required
    private transient ConcurrentMap<TestElement, Object> children = new ConcurrentHashMap<>();

    private static final Object DUMMY = new Object();

    // May be replaced by RandomOrderController
    protected transient List<TestElement> subControllersAndSamplers = new ArrayList<>();

    /**
     * Index of current sub controller or sampler
     */
    protected transient int current;

    /**
     * Current iteration
     */
    private transient int iterCount;
    
    /**
     * Controller has ended
     */
    private transient boolean done;
    
    /**
     * First sampler or sub-controller
     */
    private transient boolean first;

    /**
     * Creates a Generic Controller
     */
    public GenericController() {
    }

    @Override
    public void initialize() {
        resetCurrent();
        resetIterCount();
        done = false; // TODO should this use setDone()?
        first = true; // TODO should this use setFirst()?        
        initializeSubControllers();
    }

    /**
     * (re)Initializes sub controllers
     * See Bug 50032
     */
    protected void initializeSubControllers() {
        for (TestElement te : subControllersAndSamplers) {
            if(te instanceof GenericController) {
                ((Controller) te).initialize();
            }
        }
    }

    /**
     * Resets the controller (called after execution of last child of controller):
     * <ul>
     * <li>resetCurrent() (i.e. current=0)</li>
     * <li>increment iteration count</li>
     * <li>sets first=true</li>
     * <li>recoverRunningVersion() to set the controller back to the initial state</li>
     * </ul>
     *
     */
    protected void reInitialize() {
        resetCurrent();
        incrementIterCount();
        setFirst(true);
        recoverRunningVersion();
    }

    /**
     * <p>
     * Determines the next sampler to be processed.
     * </p>
     *
     * <p>
     * If {@link #isDone()} is <code>true</code>, returns null.
     * </p>
     *
     * <p>
     * Gets the list element using current pointer.
     * If this is <code>null</code>, calls {@link #nextIsNull()}.
     * </p>
     *
     * <p>
     * If the list element is a {@link Sampler}, calls {@link #nextIsASampler(Sampler)},
     * otherwise calls {@link #nextIsAController(Controller)}
     * </p>
     *
     * <p>
     * If any of the called methods throws {@link NextIsNullException}, returns <code>null</code>,
     * otherwise the value obtained above is returned.
     * </p>
     *
     * @return the next sampler or <code>null</code>
     */
    @Override
    public Sampler next() {
        fireIterEvents();
        log.debug("Calling next on: {}", GenericController.class);
        if (isDone()) {
            return null;
        }
        Sampler returnValue = null;
        try {
            TestElement currentElement = getCurrentElement();
            setCurrentElement(currentElement);
            if (currentElement == null) {
                returnValue = nextIsNull();
            } else {
                if (currentElement instanceof Sampler) {
                    returnValue = nextIsASampler((Sampler) currentElement);
                } else { // must be a controller
                    returnValue = nextIsAController((Controller) currentElement);
                }
            }
        } catch (NextIsNullException e) {
            // NOOP
        }
        return returnValue;
    }

    /**
     * @see org.apache.jmeter.control.Controller#isDone()
     */
    @Override
    public boolean isDone() {
        return done;
    }

    protected void setDone(boolean done) {
        this.done = done;
    }

    /**
     * @return true if it's the controller is returning the first of its children
     */
    protected boolean isFirst() {
        return first;
    }

    /**
     * If b is true, it means first is reset which means Controller has executed all its children 
     * @param b The flag, whether first is reseted
     */
    public void setFirst(boolean b) {
        first = b;
    }

    /**
     * Called by {@link #next()} if the element is a Controller, and returns the
     * next sampler from the controller. If this is <code>null</code>, then
     * updates the current pointer and makes recursive call to {@link #next()}.
     * 
     * @param controller the current <em>next</em> element
     * @return the next sampler
     * @throws NextIsNullException when the end of the list has already been reached
     */
    protected Sampler nextIsAController(Controller controller) 
            throws NextIsNullException { // NOSONAR false positive , throws is required by subclasses 
        Sampler sampler = controller.next();
        if (sampler == null) {
            currentReturnedNull(controller);
            sampler = next();
        }
        return sampler;
    }

    /**
     * Increment the current pointer and return the element. Called by
     * {@link #next()} if the element is a sampler. (May be overridden by
     * sub-classes).
     *
     * @param element
     *            the current <em>next</em> element
     * @return input element
     * @throws NextIsNullException when the end of the list has already been reached
     */
    protected Sampler nextIsASampler(Sampler element) 
            throws NextIsNullException { // NOSONAR false positive , throws is required by subclasses
        incrementCurrent();
        return element;
    }

    /**
     * Called by {@link #next()} when {@link #getCurrentElement()} returns <code>null</code>.
     * Reinitialises the controller.
     *
     * @return null (always, for this class)
     * @throws NextIsNullException when the end of the list has already been reached
     */
    protected Sampler nextIsNull() 
            throws NextIsNullException { // NOSONAR false positive , throws is required by subclasses
        reInitialize();
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerEndOfLoop() {
        reInitialize();
    }
    
    /**
     * If the controller is done, remove it from the list,
     * otherwise increment to next entry in list.
     *
     * @param c controller
     */
    protected void currentReturnedNull(Controller c) {
        if (c.isDone()) {
            removeCurrentElement();
        } else {
            incrementCurrent();
        }
    }

    /**
     * Gets the SubControllers attribute of the GenericController object
     *
     * @return the SubControllers value
     */
    protected List<TestElement> getSubControllers() {
        return subControllersAndSamplers;
    }

    private void addElement(TestElement child) {
        subControllersAndSamplers.add(child);
    }

    /**
     * Empty implementation - does nothing.
     *
     * @param currentElement
     *            the current element
     * @throws NextIsNullException
     *             when the list has been completed already
     */
    protected void setCurrentElement(TestElement currentElement) throws NextIsNullException {
        // NOOP
    }

    /**
     * <p>
     * Gets the element indicated by the <code>current</code> index, if one exists,
     * from the <code>subControllersAndSamplers</code> list.
     * </p>
     * <p>
     * If the <code>subControllersAndSamplers</code> list is empty,
     * then set done = true, and throw NextIsNullException.
     * </p>
     * @return the current element - or null if current index too large
     * @throws NextIsNullException if list is empty
     */
    protected TestElement getCurrentElement() throws NextIsNullException {
        if (current < subControllersAndSamplers.size()) {
            return subControllersAndSamplers.get(current);
        }
        if (subControllersAndSamplers.isEmpty()) {
            setDone(true);
            throw new NextIsNullException();
        }
        return null;
    }

    protected void removeCurrentElement() {
        subControllersAndSamplers.remove(current);
    }

    /**
     * Increments the current pointer; called by currentReturnedNull to move the
     * controller on to its next child.
     */
    protected void incrementCurrent() {
        current++;
    }

    protected void resetCurrent() {
        current = 0;
    }

    @Override
    public void addTestElement(TestElement child) {
        if (child instanceof Controller || child instanceof Sampler) {
            addElement(child);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean addTestElementOnce(TestElement child){
        if (children.putIfAbsent(child, DUMMY) == null) {
            addTestElement(child);
            return true;
        }
        return false;
    }

    @Override
    public void addIterationListener(LoopIterationListener lis) {
        /*
         * A little hack - add each listener to the start of the list - this
         * ensures that the thread running the show is the first listener and
         * can modify certain values before other listeners are called.
         */
        iterationListeners.addFirst(lis);
    }
    
    /**
     * Remove listener
     */
    @Override
    public void removeIterationListener(LoopIterationListener iterationListener) {
        for (Iterator<LoopIterationListener> iterator = iterationListeners.iterator(); iterator.hasNext();) {
            LoopIterationListener listener = iterator.next();
            if(listener == iterationListener)
            {
                iterator.remove();
                break; // can only match once
            }
        }
    }

    protected void fireIterEvents() {
        if (isFirst()) {
            fireIterationStart();
            first = false; // TODO - should this use setFirst() ?
        }
    }

    protected void fireIterationStart() {
        LoopIterationEvent event = new LoopIterationEvent(this, getIterCount());
        for (LoopIterationListener item : iterationListeners) {
            item.iterationStart(event);
        }
    }

    protected int getIterCount() {
        return iterCount;
    }

    protected void incrementIterCount() {
        iterCount++;
    }

    protected void resetIterCount() {
        iterCount = 0;
    }
    
    protected Object readResolve(){
        iterationListeners = new LinkedList<>();
        children = new ConcurrentHashMap<>();
        subControllersAndSamplers = new ArrayList<>();

        return this;
    }
}
