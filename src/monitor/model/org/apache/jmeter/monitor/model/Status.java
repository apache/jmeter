package org.apache.jmeter.monitor.model;

public interface Status
{
	Jvm getJvm();
	
	void setJvm(Jvm vm);
	
	java.util.List getConnector();
}
