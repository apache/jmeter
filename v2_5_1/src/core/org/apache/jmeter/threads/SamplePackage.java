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

import java.util.Iterator;
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
 * Packages methods related to sample handling.
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
        @SuppressWarnings("unchecked") // All implementations extend TestElement
        Iterator<? extends TestElement> iter = (Iterator<? extends TestElement>) list.iterator();
        while (iter.hasNext()) {
            iter.next().setRunningVersion(running);
        }
    }

    private void recoverRunningVersion(List<?> list) {
        @SuppressWarnings("unchecked") // All implementations extend TestElement
        Iterator<? extends TestElement> iter = (Iterator<? extends TestElement>) list.iterator();
        while (iter.hasNext()) {
            iter.next().recoverRunningVersion();
        }
    }

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

    public List<SampleListener> getSampleListeners() {
        return sampleListeners;
    }

    public void addSampleListener(SampleListener listener) {
        sampleListeners.add(listener);
    }

    public List<Timer> getTimers() {
        return timers;
    }

    public void addPostProcessor(PostProcessor ex) {
        postProcessors.add(ex);
    }

    public void addPreProcessor(PreProcessor pre) {
        preProcessors.add(pre);
    }

    public void addTimer(Timer timer) {
        timers.add(timer);
    }

    public void addAssertion(Assertion asser) {
        assertions.add(asser);
    }

    public List<Assertion> getAssertions() {
        return assertions;
    }

    public List<PostProcessor> getPostProcessors() {
        return postProcessors;
    }

    public Sampler getSampler() {
        return sampler;
    }

    public void setSampler(Sampler s) {
        sampler = s;
    }

    /**
     * Returns the preProcessors.
     */
    public List<PreProcessor> getPreProcessors() {
        return preProcessors;
    }

    /**
     * Returns the configs.
     *
     * @return List
     */
    public List<ConfigTestElement> getConfigs() {
        return configs;
    }

}