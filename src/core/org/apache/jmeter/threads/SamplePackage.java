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

package org.apache.jmeter.threads;

import java.util.List;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.Timer;

/**
 * Packages methods related to sample handling.<br>
 * A SamplePackage contains all elements associated to a Sampler:
 * <ul>
 *  <li>SampleListener(s)</li>
 *  <li>Timer(s)</li>
 *  <li>Assertion(s)</li>
 *  <li>PreProcessor(s)</li>
 *  <li>PostProcessor(s)</li>
 *  <li>ConfigTestElement(s)</li>
 *  <li>Controller(s)</li>
 * </ul>
 */
public class SamplePackage {

    private final List<SampleListener> sampleListeners;

    private final List<Timer> timers;

    private final List<Assertion> assertions;

    private final List<PostProcessor> postProcessors;

    private final List<PreProcessor> preProcessors;

    private final List<ConfigTestElement> configs;

    private final List<Controller> controllers;

    private Sampler sampler;

    public SamplePackage(
            List<ConfigTestElement> configs,
            List<SampleListener> listeners,
            List<Timer> timers,
            List<Assertion> assertions, 
            List<PostProcessor> postProcessors, 
            List<PreProcessor> preProcessors,
            List<Controller> controllers) {
        this.configs = configs;
        this.sampleListeners = listeners;
        this.timers = timers;
        this.assertions = assertions;
        this.postProcessors = postProcessors;
        this.preProcessors = preProcessors;
        this.controllers = controllers;
    }

    /**
     * Make the SamplePackage the running version, or make it no longer the
     * running version. This tells to each element of the SamplePackage that it's current state must
     * be retrievable by a call to recoverRunningVersion(). 
     * @param running boolean
     * @see TestElement#setRunningVersion(boolean)
     */
    public void setRunningVersion(boolean running) {
        setRunningVersion(configs, running);
        setRunningVersion(sampleListeners, running);
        setRunningVersion(assertions, running);
        setRunningVersion(timers, running);
        setRunningVersion(postProcessors, running);
        setRunningVersion(preProcessors, running);
        setRunningVersion(controllers, running);
        sampler.setRunningVersion(running);
    }

    private void setRunningVersion(List<?> list, boolean running) {
        @SuppressWarnings("unchecked") // all implementations extend TestElement
        List<TestElement> telist = (List<TestElement>)list;
        for (TestElement te : telist) {
            te.setRunningVersion(running);
        }
    }

    private void recoverRunningVersion(List<?> list) {
        @SuppressWarnings("unchecked") // All implementations extend TestElement
        List<TestElement> telist = (List<TestElement>)list;
        for (TestElement te : telist) {
            te.recoverRunningVersion();
        }
    }

    /**
     * Recover each member of SamplePackage to the state before the call of setRunningVersion(true)
     * @see TestElement#recoverRunningVersion()
     */
    public void recoverRunningVersion() {
        recoverRunningVersion(configs);
        recoverRunningVersion(sampleListeners);
        recoverRunningVersion(assertions);
        recoverRunningVersion(timers);
        recoverRunningVersion(postProcessors);
        recoverRunningVersion(preProcessors);
        recoverRunningVersion(controllers);
        sampler.recoverRunningVersion();
    }

    /**
     * @return List of {@link SampleListener}s
     */
    public List<SampleListener> getSampleListeners() {
        return sampleListeners;
    }

    /**
     * Add Sample Listener
     * @param listener {@link SampleListener}
     */
    public void addSampleListener(SampleListener listener) {
        sampleListeners.add(listener);
    }

    /**
     * @return List of {@link Timer}s
     */
    public List<Timer> getTimers() {
        return timers;
    }

    
    /**
     * Add Post processor
     * @param ex {@link PostProcessor}
     */
    public void addPostProcessor(PostProcessor ex) {
        postProcessors.add(ex);
    }

    /**
     * Add Pre processor
     * @param pre {@link PreProcessor}
     */
    public void addPreProcessor(PreProcessor pre) {
        preProcessors.add(pre);
    }

    /**
     * Add Timer
     * @param timer {@link Timer}
     */
    public void addTimer(Timer timer) {
        timers.add(timer);
    }

    /**
     * Add Assertion
     * @param asser {@link Assertion}
     */
    public void addAssertion(Assertion asser) {
        assertions.add(asser);
    }

    /**
     * @return List of {@link Assertion}
     */
    public List<Assertion> getAssertions() {
        return assertions;
    }

    /**
     * @return List of {@link PostProcessor}s
     */
    public List<PostProcessor> getPostProcessors() {
        return postProcessors;
    }

    /**
     * @return {@link Sampler}
     */
    public Sampler getSampler() {
        return sampler;
    }

    /**
     * @param s {@link Sampler}
     */
    public void setSampler(Sampler s) {
        sampler = s;
    }

    /**
     * Returns the preProcessors.
     * @return List of {@link PreProcessor}
     */
    public List<PreProcessor> getPreProcessors() {
        return preProcessors;
    }

    /**
     * Returns the configs.
     *
     * @return List of {@link ConfigTestElement}
     */
    public List<ConfigTestElement> getConfigs() {
        return configs;
    }

}
