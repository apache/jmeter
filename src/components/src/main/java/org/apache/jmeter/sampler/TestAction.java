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
package org.apache.jmeter.sampler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.Interruptible;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContext.TestLogicalAction;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.timers.TimerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy Sampler used to pause or stop a thread or the test;
 * intended for use in Conditional Controllers.
 *
 */
public class TestAction extends AbstractSampler implements Interruptible {

    private static final Logger log = LoggerFactory.getLogger(TestAction.class);

    private static final String MSG_STOP_CURRENT_THREAD = "Stopping current thread from element {}";

    private static final TimerService TIMER_SERVICE = TimerService.getInstance();

    private static final long serialVersionUID = 242L;

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList("org.apache.jmeter.config.gui.SimpleConfigGui"));

    // Actions
    public static final int STOP = 0;
    public static final int PAUSE = 1;
    public static final int STOP_NOW = 2;
    /**
     * Start next iteration of Thread Loop
     */
    public static final int RESTART_NEXT_LOOP = 3;
    /**
     * Start next iteration of Current Looop
     */
    public static final int START_NEXT_ITERATION_CURRENT_LOOP = 4;
    /**
     * Break Current Looop
     */
    public static final int BREAK_CURRENT_LOOP = 5;

    // Action targets
    public static final int THREAD = 0;
    public static final int TEST = 2;

    // Identifiers
    private static final String TARGET = "ActionProcessor.target"; //$NON-NLS-1$
    private static final String ACTION = "ActionProcessor.action"; //$NON-NLS-1$
    private static final String DURATION = "ActionProcessor.duration"; //$NON-NLS-1$

    private transient volatile Thread pauseThread;

    public TestAction() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleResult sample(Entry e) {
        JMeterContext context = JMeterContextService.getContext();

        int target = getTarget();
        int action = getAction();
        if (action == PAUSE) {
            pause(getDurationAsString());
        } else if (action == STOP || action == STOP_NOW) {
            if (target == THREAD) {
                if(log.isInfoEnabled()) {
                    log.info(MSG_STOP_CURRENT_THREAD, getName());
                }
                context.getThread().stop();
            } else if (target == TEST) {
                if (action == STOP_NOW) {
                    if(log.isInfoEnabled()) {
                        log.info(MSG_STOP_CURRENT_THREAD, getName());
                    }
                    context.getThread().stop();
                    if(log.isInfoEnabled()) {
                        log.info("Stopping all threads now from element {}", getName());
                    }
                    context.getEngine().stopTest();
                } else {
                    if(log.isInfoEnabled()) {
                        log.info(MSG_STOP_CURRENT_THREAD, getName());
                    }
                    context.getThread().stop();
                    if(log.isInfoEnabled()) {
                        log.info("Stopping all threads from element {}", getName());
                    }
                    context.getEngine().askThreadsToStop();
                }
            }
        } else if (action == RESTART_NEXT_LOOP) {
            log.info("Restarting next thread loop from element {}", getName());
            context.setTestLogicalAction(TestLogicalAction.START_NEXT_ITERATION_OF_THREAD);
        } else if (action == START_NEXT_ITERATION_CURRENT_LOOP) {
            log.info("Switching to next loop iteration from element {}", getName());
            context.setTestLogicalAction(TestLogicalAction.START_NEXT_ITERATION_OF_CURRENT_LOOP);
        } else if (action == BREAK_CURRENT_LOOP) {
            log.info("Breaking current loop from element {}", getName());
            context.setTestLogicalAction(TestLogicalAction.BREAK_CURRENT_LOOP);
        }

        return null; // This means no sample is saved
    }

    private void pause(String timeInMillis) {
        long millis;
        try {
            if(!StringUtils.isEmpty(timeInMillis)) {
                millis=Long.parseLong(timeInMillis);
            } else {
                log.warn("Duration value is empty, defaulting to 0");
                millis=0L;
            }
        } catch (NumberFormatException e){
            log.warn("Could not parse number: '{}'", timeInMillis);
            millis=0L;
        }
        try {
            pauseThread = Thread.currentThread();
            if(millis>0) {
                TimeUnit.MILLISECONDS.sleep(TIMER_SERVICE.adjustDelay(millis));
            } else if(millis<0) {
                throw new IllegalArgumentException("Configured sleep is negative:"+millis);
            } // else == 0 we do nothing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pauseThread = null;
        }
    }

    public void setTarget(int target) {
        setProperty(new IntegerProperty(TARGET, target));
    }

    public int getTarget() {
        return getPropertyAsInt(TARGET);
    }

    public void setAction(int action) {
        setProperty(new IntegerProperty(ACTION, action));
    }

    public int getAction() {
        return getPropertyAsInt(ACTION);
    }

    public void setDuration(String duration) {
        setProperty(new StringProperty(DURATION, duration));
    }

    public String getDurationAsString() {
        return getPropertyAsString(DURATION);
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    @Override
    public boolean interrupt() {
        Thread thrd = pauseThread; // take copy so cannot get NPE
        if (thrd!= null) {
            thrd.interrupt();
            return true;
        }
        return false;
    }
}
