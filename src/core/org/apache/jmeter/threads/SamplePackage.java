// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.Timer;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author Michael Stover
 * @version $Revision$
 */
public class SamplePackage {
	private static Logger log = LoggingManager.getLoggerForClass();

	List sampleListeners = new LinkedList();

	List timers = new LinkedList();

	List assertions = new LinkedList();

	List postProcessors = new LinkedList();

	List preProcessors = new LinkedList();

	List responseModifiers;

	List configs;

	List modifiers;

	List controllers;

	Sampler sampler;

	public SamplePackage() {
	}

	public SamplePackage(List configs, List modifiers, List responseModifiers, List listeners, List timers,
			List assertions, List extractors, List pres, List controllers) {
		log.debug("configs is null: " + (configs == null));
		this.configs = configs;
		this.modifiers = modifiers;
		this.responseModifiers = responseModifiers;
		this.sampleListeners = listeners;
		this.timers = timers;
		this.assertions = assertions;
		this.postProcessors = extractors;
		this.preProcessors = pres;
		this.controllers = controllers;
	}

	public void setRunningVersion(boolean running) {
		setRunningVersion(configs, running);
		setRunningVersion(modifiers, running);
		setRunningVersion(sampleListeners, running);
		setRunningVersion(assertions, running);
		setRunningVersion(timers, running);
		setRunningVersion(responseModifiers, running);
		setRunningVersion(postProcessors, running);
		setRunningVersion(preProcessors, running);
		setRunningVersion(controllers, running);
		sampler.setRunningVersion(running);
	}

	private void setRunningVersion(List list, boolean running) {
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			((TestElement) iter.next()).setRunningVersion(running);
		}
	}

	private void recoverRunningVersion(List list) {
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			((TestElement) iter.next()).recoverRunningVersion();
		}
	}

	public void recoverRunningVersion() {
		recoverRunningVersion(configs);
		recoverRunningVersion(modifiers);
		recoverRunningVersion(sampleListeners);
		recoverRunningVersion(assertions);
		recoverRunningVersion(timers);
		recoverRunningVersion(responseModifiers);
		recoverRunningVersion(postProcessors);
		recoverRunningVersion(preProcessors);
		recoverRunningVersion(controllers);
		sampler.recoverRunningVersion();
	}

	public List getSampleListeners() {
		return sampleListeners;
	}

	public void addSampleListener(SampleListener listener) {
		sampleListeners.add(listener);
	}

	public List getTimers() {
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

	public List getAssertions() {
		return assertions;
	}

	public List getPostProcessors() {
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
	public List getPreProcessors() {
		return preProcessors;
	}

	/**
	 * Sets the preProcessors.
	 * 
	 * @param preProcessors
	 *            the preProcessors to set
	 */
	public void setPreProcessors(List preProcessors) {
		this.preProcessors = preProcessors;
	}

	/**
	 * Returns the configs.
	 * 
	 * @return List
	 */
	public List getConfigs() {
		return configs;
	}

	/**
	 * Returns the modifiers.
	 */
	public List getModifiers() {
		return modifiers;
	}

	/**
	 * Returns the responseModifiers.
	 */
	public List getResponseModifiers() {
		return responseModifiers;
	}

	/**
	 * Sets the assertions.
	 * 
	 * @param assertions
	 *            the assertions to set
	 */
	public void setAssertions(List assertions) {
		this.assertions = assertions;
	}

	/**
	 * Sets the configs.
	 * 
	 * @param configs
	 *            the configs to set
	 */
	public void setConfigs(List configs) {
		this.configs = configs;
	}

	/**
	 * Sets the modifiers.
	 * 
	 * @param modifiers
	 *            the modifiers to set
	 */
	public void setModifiers(List modifiers) {
		this.modifiers = modifiers;
	}

	/**
	 * Sets the postProcessors.
	 * 
	 * @param postProcessors
	 *            the postProcessors to set
	 */
	public void setPostProcessors(List postProcessors) {
		this.postProcessors = postProcessors;
	}

	/**
	 * Sets the responseModifiers.
	 * 
	 * @param responseModifiers
	 *            the responseModifiers to set
	 */
	public void setResponseModifiers(List responseModifiers) {
		this.responseModifiers = responseModifiers;
	}

	/**
	 * Sets the sampleListeners.
	 * 
	 * @param sampleListeners
	 *            the sampleListeners to set
	 */
	public void setSampleListeners(List sampleListeners) {
		this.sampleListeners = sampleListeners;
	}

	/**
	 * Sets the timers.
	 * 
	 * @param timers
	 *            the timers to set
	 */
	public void setTimers(List timers) {
		this.timers = timers;
	}
}