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

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a counter per-thread(user) or per-thread group.
 */
public class CounterConfig extends ConfigTestElement
    implements TestBean, Serializable, LoopIterationListener, NoThreadClone {

    private static final long serialVersionUID = 234L;
    
    private String startValue;

    private String maxValue;

    private String varName;

    private String format;

    private String increment;

    private boolean perUser;

    private boolean resetPerTGIteration;

    // This class is not cloned per thread, so this is shared
    //@GuardedBy("this")
    private long globalCounter = Long.MIN_VALUE;

    // Used for per-thread/user numbers
    private transient ThreadLocal<Long> perTheadNumber;

    // Used for per-thread/user storage of increment in Thread Group Main loop
    private transient ThreadLocal<Long> perTheadLastIterationNumber;

    private static final Logger log = LoggerFactory.getLogger(CounterConfig.class);

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        if (NumberUtils.toLong(getStartValue()) > NumberUtils.toLong(getMaxValue())){
            log.error("maximum({}) must be > minimum({})", getMaxValue(), getStartValue());
            return;
        }
        perTheadNumber = new ThreadLocal<Long>() {
            @Override
            protected Long initialValue() {
                return Long.valueOf(getStartValue());
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
        long start = Long.valueOf(getStartValue());
        long end = Long.valueOf(getMaxValue());
        long increment = Long.valueOf(getIncrement());
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
            if(isResetPerTGIteration()) {
                int iteration = variables.getIteration();
                Long lastIterationNumber = perTheadLastIterationNumber.get();
                if(iteration != lastIterationNumber.longValue()) {
                    // reset
                    current = Long.valueOf(getStartValue());
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



    public String getStartValue() {
        return startValue;
    }


    public void setStartValue(String startValue) {
        this.startValue = startValue;
    }


    public String getMaxValue() {
        if ("".equals(maxValue)) {
            maxValue = String.valueOf(Long.MAX_VALUE);
        }
        return maxValue;
    }


    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }


    public boolean isResetPerTGIteration() {
        return resetPerTGIteration;
    }


    public void setResetPerTGIteration(boolean resetPerTGIteration) {
        this.resetPerTGIteration = resetPerTGIteration;
    }

    public boolean isPerUser() {
        return perUser;
    }

    public void setPerUser(boolean perUser) {
        this.perUser = perUser;
    }


    public String getVarName() {
        return varName;
    }


    public void setVarName(String varName) {
        this.varName = varName;
    }


    public String getFormat() {
        return format;
    }


    public void setFormat(String format) {
        this.format = format;
    }


    public String getIncrement() {
        return increment;
    }


    public void setIncrement(String increment) {
        this.increment = increment;
    }
}
