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
import java.text.DecimalFormat;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.LongProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a counter per-thread(user) or per-thread group.
 */
public class CounterConfig extends AbstractTestElement
    implements Serializable, LoopIterationListener, NoThreadClone {

    private static final long serialVersionUID = 234L;

    private static final String START = "CounterConfig.start"; // $NON-NLS-1$

    private static final String END = "CounterConfig.end"; // $NON-NLS-1$

    private static final String INCREMENT = "CounterConfig.incr"; // $NON-NLS-1$

    private static final String FORMAT = "CounterConfig.format"; // $NON-NLS-1$

    private static final String PER_USER = "CounterConfig.per_user"; // $NON-NLS-1$

    private static final String VAR_NAME = "CounterConfig.name"; // $NON-NLS-1$

    private static final String RESET_ON_THREAD_GROUP_ITERATION = "CounterConfig.reset_on_tg_iteration"; // $NON-NLS-1$

    private static final boolean RESET_ON_THREAD_GROUP_ITERATION_DEFAULT = false;

    // This class is not cloned per thread, so this is shared
    private long globalCounter = Long.MIN_VALUE;

    // Used for per-thread/user numbers
    private transient ThreadLocal<Long> perTheadNumber;

    // Used for per-thread/user storage of increment in Thread Group Main loop
    private transient ThreadLocal<Long> perTheadLastIterationNumber;

    private static final Logger log = LoggerFactory.getLogger(CounterConfig.class);

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        perTheadNumber = new ThreadLocal<Long>() {
            @Override
            protected Long initialValue() {
                return Long.valueOf(getStart());
            }
        };
        perTheadLastIterationNumber = new ThreadLocal<Long>() {
            @Override
            protected Long initialValue() {
                return Long.valueOf(1);
            }
        };
    }


    public CounterConfig() {
        super();
        init();
    }

    private Object readResolve(){
        init();
        return this;
    }
    /**
     * @see LoopIterationListener#iterationStart(LoopIterationEvent)
     */
    @Override
    public void iterationStart(LoopIterationEvent event) {
        // Cannot use getThreadContext() as not cloned per thread
        JMeterVariables variables = JMeterContextService.getContext().getVariables();
        long start = getStart();
        long end = getEnd();
        long increment = getIncrement();
        if (!isPerUser()) {
            synchronized (this) {
                if (globalCounter == Long.MIN_VALUE || globalCounter > end) {
                    globalCounter = start;
                }
                variables.put(getVarName(), formatNumber(globalCounter));
                globalCounter += increment;
            }
        } else {
            long current = perTheadNumber.get().longValue();
            if(isResetOnThreadGroupIteration()) {
                int iteration = variables.getIteration();
                Long lastIterationNumber = perTheadLastIterationNumber.get();
                if(iteration != lastIterationNumber.longValue()) {
                    // reset
                    current = getStart();
                }
                perTheadLastIterationNumber.set(Long.valueOf(iteration));
            }
            variables.put(getVarName(), formatNumber(current));
            current += increment;
            if (current > end) {
                current = start;
            }
            perTheadNumber.set(Long.valueOf(current));
        }
    }

    // Use format to create number; if it fails, use the default
    private String formatNumber(long value){
        String format = getFormat();
        if (format != null && format.length() > 0) {
            try {
                DecimalFormat myFormatter = new DecimalFormat(format);
                return myFormatter.format(value);
            } catch (IllegalArgumentException ignored) {
                log.warn("Error formatting {} at format {}, using default", value, format);
            }
        }
        return Long.toString(value);
    }

    public void setStart(long start) {
        setProperty(new LongProperty(START, start));
    }

    public void setStart(String start) {
        setProperty(START, start);
    }

    public long getStart() {
        return getPropertyAsLong(START);
    }

    public String getStartAsString() {
        return getPropertyAsString(START);
    }

    public void setEnd(long end) {
        setProperty(new LongProperty(END, end));
    }

    public void setEnd(String end) {
        setProperty(END, end);
    }

    /**
     * @param value boolean indicating if counter must be reset on Thread Group Iteration
     */
    public void setResetOnThreadGroupIteration(boolean value) {
        setProperty(RESET_ON_THREAD_GROUP_ITERATION, value, RESET_ON_THREAD_GROUP_ITERATION_DEFAULT);
    }

    /**
     * @return true if counter must be reset on Thread Group Iteration
     */
    public boolean isResetOnThreadGroupIteration() {
        return getPropertyAsBoolean(RESET_ON_THREAD_GROUP_ITERATION, RESET_ON_THREAD_GROUP_ITERATION_DEFAULT);
    }

    /**
     *
     * @return counter upper limit (default Long.MAX_VALUE)
     */
    public long getEnd() {
       long propertyAsLong = getPropertyAsLong(END);
       if (propertyAsLong == 0 && "".equals(getProperty(END).getStringValue())) {
          propertyAsLong = Long.MAX_VALUE;
       }
       return propertyAsLong;
    }

    public String getEndAsString(){
        return getPropertyAsString(END);
    }

    public void setIncrement(long inc) {
        setProperty(new LongProperty(INCREMENT, inc));
    }

    public void setIncrement(String incr) {
        setProperty(INCREMENT, incr);
    }

    public long getIncrement() {
        return getPropertyAsLong(INCREMENT);
    }

    public String getIncrementAsString() {
        return getPropertyAsString(INCREMENT);
    }

    public void setIsPerUser(boolean isPer) {
        setProperty(new BooleanProperty(PER_USER, isPer));
    }

    public boolean isPerUser() {
        return getPropertyAsBoolean(PER_USER);
    }

    public void setVarName(String name) {
        setProperty(VAR_NAME, name);
    }

    public String getVarName() {
        return getPropertyAsString(VAR_NAME);
    }

    public void setFormat(String format) {
        setProperty(FORMAT, format);
    }

    public String getFormat() {
        return getPropertyAsString(FORMAT);
    }
}
