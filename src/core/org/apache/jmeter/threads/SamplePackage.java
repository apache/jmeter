package org.apache.jmeter.threads;

import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.assertions.Assertion;

import java.util.*;

/**
 * Title:        JMeter
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */

public class SamplePackage
{
	List sampleListeners = new LinkedList();
	List timers = new LinkedList();
	List assertions = new LinkedList();
	Sampler sampler;

	public SamplePackage()
	{
	}

	public List getSampleListeners()
	{
		return sampleListeners;
	}

	public void addSampleListener(SampleListener listener)
	{
		sampleListeners.add(listener);
	}

	public List getTimers()
	{
		return timers;
	}

	public void addTimer(Timer timer)
	{
		timers.add(timer);
	}

	public void addAssertion(Assertion asser)
	{
		assertions.add(asser);
	}

	public List getAssertions()
	{
		return assertions;
	}

	public Sampler getSampler()
	{
		return sampler;
	}

	public void setSampler(Sampler s)
	{
		sampler = s;
	}
}