/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.config;

import java.text.DecimalFormat;
import java.util.Random;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoConfigMerge;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomVariableConfig extends ConfigTestElement
    implements TestBean, LoopIterationListener, NoThreadClone, NoConfigMerge
{
    private static final Logger log = LoggerFactory.getLogger(RandomVariableConfig.class);

    private static final long serialVersionUID = 234L;

    /*
     *  N.B. this class is shared between threads (NoThreadClone) so all access to variables
     *  needs to be protected by a lock (either sync. or volatile) to ensure safe publication.
     */

    private String minimumValue;

    private String maximumValue;

    private String variableName;

    private String outputFormat;

    private String randomSeed;

    private boolean perThread;

    private int range;

    private long minimum;

    // This class is not cloned per thread, so this is shared
    private Random globalRandom = null;

    // Used for per-thread/user numbers
    // Cannot be static, as random numbers are not to be shared between instances
    private transient ThreadLocal<Random> perThreadRandom = initThreadLocal();

    private ThreadLocal<Random> initThreadLocal() {
        return new ThreadLocal<Random>() {
                @Override
                protected Random initialValue() {
                    init();
                    return createRandom();
                }};
    }

    private Object readResolve(){
        perThreadRandom = initThreadLocal();
        return this;
    }

    /*
     * nextInt(n) returns values in the range [0,n),
     * so n must be set to max-min+1
     */
    private void init(){
        final String minAsString = getMinimumValue();
        minimum = NumberUtils.toLong(minAsString);
        final String maxAsString = getMaximumValue();
        long maximum = NumberUtils.toLong(maxAsString);
        long rangeL=maximum-minimum+1; // This can overflow
        if (minimum > maximum){
            log.error("maximum({}) must be >= minimum({})", maxAsString, minAsString);
            range=0;// This is used as an error indicator
            return;
        }
        if (rangeL > Integer.MAX_VALUE || rangeL <= 0){// check for overflow too
            log.warn("maximum({}) - minimum({}) must be <= {}", maxAsString, minAsString, Integer.MAX_VALUE);
            rangeL=Integer.MAX_VALUE;
        }
        range = (int)rangeL;
    }

    /** {@inheritDoc} */
    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        Random randGen;
        if (getPerThread()){
            randGen = perThreadRandom.get();
        } else {
            synchronized(this){
                if (globalRandom == null){
                    init();
                    globalRandom = createRandom();
                }
                randGen=globalRandom;
            }
        }
        if (range <=0){
            return;
        }
       long nextRand = minimum + randGen.nextInt(range);
       // Cannot use getThreadContext() as we are not cloned per thread
       JMeterVariables variables = JMeterContextService.getContext().getVariables();
       variables.put(getVariableName(), formatNumber(nextRand));
    }

    // Use format to create number; if it fails, use the default
    private String formatNumber(long value){
        String format = getOutputFormat();
        if (format != null && format.length() > 0) {
            try {
                DecimalFormat myFormatter = new DecimalFormat(format);
                return myFormatter.format(value);
            } catch (IllegalArgumentException ignored) {
                log.warn("Exception formatting value: {} at format: {}, using default", value, format);
            }
        }
        return Long.toString(value);
    }

    /**
     * @return the minValue
     */
    public synchronized String getMinimumValue() {
        return minimumValue;
    }

    /**
     * @param minValue the minValue to set
     */
    public synchronized void setMinimumValue(String minValue) {
        this.minimumValue = minValue;
    }

    /**
     * @return the maxvalue
     */
    public synchronized String getMaximumValue() {
        return maximumValue;
    }

    /**
     * @param maxvalue the maxvalue to set
     */
    public synchronized void setMaximumValue(String maxvalue) {
        this.maximumValue = maxvalue;
    }

    /**
     * @return the variableName
     */
    public synchronized String getVariableName() {
        return variableName;
    }

    /**
     * @param variableName the variableName to set
     */
    public synchronized void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    /**
     * @return the randomSeed
     */
    public synchronized String getRandomSeed() {
        return randomSeed;
    }

    private Random createRandom() {
        if (randomSeed.length()>0){
            Long seed = getRandomSeedAsLong();
            if(seed != null) {
                return new Random(seed.longValue());
            }
        }
        return new Random();
    }

    /**
     * @return the randomSeed as a long
     */
    private synchronized Long getRandomSeedAsLong() {
        Long seed = null;
        try {
            seed = Long.parseLong(randomSeed);
        } catch (NumberFormatException e) {
            if(log.isWarnEnabled()) {
                log.warn("Cannot parse random seed: '{}' in element {}", randomSeed, getName());
            }
        }
        return seed;
    }

    /**
     * @param randomSeed the randomSeed to set
     */
    public synchronized void setRandomSeed(String randomSeed) {
        this.randomSeed = randomSeed;
    }

    /**
     * @return the perThread
     */
    public synchronized boolean getPerThread() {
        return perThread;
    }

    /**
     * @param perThread the perThread to set
     */
    public synchronized void setPerThread(boolean perThread) {
        this.perThread = perThread;
    }
    /**
     * @return the outputFormat
     */
    public synchronized String getOutputFormat() {
        return outputFormat;
    }
    /**
     * @param outputFormat the outputFormat to set
     */
    public synchronized void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

}
