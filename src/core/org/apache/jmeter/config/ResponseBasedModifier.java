package org.apache.jmeter.config;

/**
 * Title:        Jakarta-JMeter
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;

public interface ResponseBasedModifier
{
	public boolean modifyEntry(Sampler sampler,SampleResult responseText);
}