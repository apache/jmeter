package org.apache.jmeter.monitor.parser;

import org.apache.jmeter.monitor.model.Status;
import org.apache.jmeter.samplers.SampleResult;

public interface Parser
{
	Status parseBytes(byte[] bytes);
	Status parseString(String content);
	Status parseSampleResult(SampleResult result);
}
