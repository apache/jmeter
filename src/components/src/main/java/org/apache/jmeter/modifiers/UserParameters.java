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

package org.apache.jmeter.modifiers;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserParameters extends AbstractTestElement implements Serializable, PreProcessor, LoopIterationListener {
    private static final Logger log = LoggerFactory.getLogger(UserParameters.class);

    private static final long serialVersionUID = 234L;

    public static final String NAMES = "UserParameters.names";// $NON-NLS-1$

    public static final String THREAD_VALUES = "UserParameters.thread_values";// $NON-NLS-1$

    public static final String PER_ITERATION = "UserParameters.per_iteration";// $NON-NLS-1$

    /**
     * Although the lock appears to be an instance lock, in fact the lock is
     * shared between all threads see the clone() method below
     *
     * The lock ensures that all the variables are processed together, which is
     * important for functions such as __CSVRead and __StringFromFile.
     * But it has a performance drawback.
     */
    private transient Object lock = new Object();

    private Object readResolve(){ // Lock object must exist
        lock = new Object();
        return this;
    }

    public CollectionProperty getNames() {
        return (CollectionProperty) getProperty(NAMES);
    }

    public CollectionProperty getThreadLists() {
        return (CollectionProperty) getProperty(THREAD_VALUES);
    }

    /**
     * The list of names of the variables to hold values. This list must come in
     * the same order as the sub lists that are given to
     * {@link #setThreadLists(Collection)}.
     *
     * @param list
     *            The ordered list of names
     */
    public void setNames(Collection<?> list) {
        setProperty(new CollectionProperty(NAMES, list));
    }

    /**
     * The list of names of the variables to hold values. This list must come in
     * the same order as the sub lists that are given to
     * {@link #setThreadLists(CollectionProperty)}.
     *
     * @param list
     *            The ordered list of names
     */
    public void setNames(CollectionProperty list) {
        setProperty(list);
    }

    /**
     * The thread list is a list of lists. Each list within the parent list is a
     * collection of values for a simulated user. As many different sets of
     * values can be supplied in this fashion to cause JMeter to set different
     * values to variables for different test threads.
     *
     * @param threadLists
     *            The list of lists of values for each user thread
     */
    public void setThreadLists(Collection<?> threadLists) {
        setProperty(new CollectionProperty(THREAD_VALUES, threadLists));
    }

    /**
     * The thread list is a list of lists. Each list within the parent list is a
     * collection of values for a simulated user. As many different sets of
     * values can be supplied in this fashion to cause JMeter to set different
     * values to variables for different test threads.
     *
     * @param threadLists
     *            The list of lists of values for each user thread
     */
    public void setThreadLists(CollectionProperty threadLists) {
        setProperty(threadLists);
    }

    private CollectionProperty getValues() {
        CollectionProperty threadValues = (CollectionProperty) getProperty(THREAD_VALUES);
        if (threadValues.size() > 0) {
            return (CollectionProperty) threadValues.get(getThreadContext().getThreadNum() % threadValues.size());
        }
        return new CollectionProperty("noname", new LinkedList<>());
    }

    public boolean isPerIteration() {
        return getPropertyAsBoolean(PER_ITERATION);
    }

    public void setPerIteration(boolean perIter) {
        setProperty(new BooleanProperty(PER_ITERATION, perIter));
    }

    @Override
    public void process() {
        if (log.isDebugEnabled()) {
            log.debug("{} process {}", Thread.currentThread().getName(), isPerIteration());//$NON-NLS-1$
        }
        if (!isPerIteration()) {
            setValues();
        }
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    private void setValues() {
        synchronized (lock) {
            if (log.isDebugEnabled()) {
                log.debug("{} Running up named: {}", Thread.currentThread().getName(), getName());//$NON-NLS-1$
            }
            PropertyIterator namesIter = getNames().iterator();
            PropertyIterator valueIter = getValues().iterator();
            JMeterVariables jmvars = getThreadContext().getVariables();
            while (namesIter.hasNext() && valueIter.hasNext()) {
                String name = namesIter.next().getStringValue();
                String value = valueIter.next().getStringValue();
                if (log.isDebugEnabled()) {
                    log.debug("{} saving variable: {}={}", Thread.currentThread().getName(), name, value);//$NON-NLS-1$
                }
                jmvars.put(name, value);
            }
        }
    }

    /**
     * @see LoopIterationListener#iterationStart(LoopIterationEvent)
     */
    @Override
    public void iterationStart(LoopIterationEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("{} iteration start {}", Thread.currentThread().getName(), isPerIteration());//$NON-NLS-1$
        }
        if (isPerIteration()) {
            setValues();
        }
    }

    /**
     * A new instance is created for each thread group, and the
     * clone() method is then called to create copies for each thread in a
     * thread group. This means that the lock object is common to all instances
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        UserParameters up = (UserParameters) super.clone();
        up.lock = lock; // ensure that clones share the same lock object
        return up;
    }
}
